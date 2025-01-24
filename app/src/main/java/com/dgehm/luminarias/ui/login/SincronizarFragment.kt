package com.dgehm.luminarias.ui.login

import DatabaseHelper
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.dgehm.luminarias.HttpClient
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentSincronizarBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import android.util.Base64
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.InputStream


class SincronizarFragment : Fragment() {

    private var _binding: FragmentSincronizarBinding? = null
    private val binding get() = _binding!!
    private val client by lazy { HttpClient(requireContext()) }
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSincronizarBinding.inflate(inflater, container, false)

        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Sincronizar"

        dbHelper = DatabaseHelper(requireContext())

        // Solicitar permisos antes de acceder a las imágenes
        checkAndRequestPermissions()

        val btnSincronizar = binding.btnSincronizar

        val btnReiniciar = binding.btnReiniciar



        val loadingProgressBar: ProgressBar = binding.progressBar
        //loadingProgressBar.visibility = View.VISIBLE



        btnReiniciar.setOnClickListener {

            context?.let { nonNullContext ->

                // Verificar si la base de datos existe
                if (dbHelper.databaseExists()) {
                    println("Base de datos existente.")

                    // Eliminar la base de datos si existe
                    if (dbHelper.deleteDatabase()) {
                        println("Base de datos existente eliminada correctamente.")
                    } else {
                        println("No se pudo eliminar la base de datos existente.")
                        Toast.makeText(
                            nonNullContext,
                            "Error al eliminar la base de datos.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@let
                    }


                    dbHelper.copyDatabase()
                    Toast.makeText(
                        nonNullContext, "Base de datos creó exitosamente.", Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }


       /* btnSincronizar.setOnClickListener */


        btnSincronizar.setOnClickListener {
            context?.let { nonNullContext ->

                if (dbHelper.databaseExists()) {
                    println("Base de datos existente.")

                    val reportesFalla = dbHelper.getReportesFalla()
                    val totalReportes = reportesFalla.size
                    var enviadosCorrectos = 0
                    var enviadosErroneos = 0

                    // Mostrar el ProgressBar
                    loadingProgressBar.visibility = View.VISIBLE

                    for (reporte in reportesFalla) {
                        val base64String: String? = when (reporte.tipoImagen) {
                            "1" -> convertirImagenUriABase64(requireContext(), Uri.parse(reporte.urlFoto))
                            "2" -> convertirFotoUriABase64(requireContext(), Uri.parse(reporte.urlFoto).toString())
                            else -> null
                        }

                        val json = JSONObject().apply {
                            put("distrito_id", reporte.distritoId)
                            put("tipo_falla_id", reporte.tipoFallaId)
                            put("descripcion", reporte.descripcion)
                            put("latitud", reporte.latitud)
                            put("longitud", reporte.longitud)
                            put("telefono_contacto", reporte.telefonoContacto)
                            put("nombre_contacto", reporte.nombreContacto)
                            put("correo_contacto", reporte.correoContacto)
                            put("usuario_id", reporte.usuarioCreacion)
                            put("fecha", reporte.fechaCreacion)
                            put("imagen", base64String)
                        }

                        // Realizar la petición POST
                        val endpoint = "/api_reporte_falla/sincronizar"
                        client.post(endpoint, json.toString(), object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                enviadosErroneos++
                                Log.e("SyncError", "Error al enviar reporte ID ${reporte.id}: ${e.message}")

                                requireActivity().runOnUiThread {
                                    MaterialDialog(requireContext()).show {
                                        title(text = "Error")
                                        message(text = "Error al hacer la petición: ${e.message}")
                                        icon(R.drawable.baseline_error_24)
                                        positiveButton(text = "Aceptar")
                                    }
                                }

                                verificarFinalizacion(totalReportes, enviadosCorrectos, enviadosErroneos)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                requireActivity().runOnUiThread {
                                    if (response.isSuccessful) {
                                        enviadosCorrectos++
                                        dbHelper.deleteReporteFallaById(reporte.id)

                                        if (reporte.tipoImagen == "2") {
                                            val uri = Uri.parse(reporte.urlFoto)
                                            eliminarArchivo(requireContext(), uri)
                                        }
                                    } else {
                                        enviadosErroneos++
                                        Log.e("SyncError", "Error en respuesta para reporte ID ${reporte.id}")
                                    }

                                    verificarFinalizacion(totalReportes, enviadosCorrectos, enviadosErroneos)
                                }
                            }
                        })
                    }
                } else {
                    dbHelper.copyDatabase()
                    Toast.makeText(nonNullContext, "Base de datos creada exitosamente.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "El contexto es nulo", Toast.LENGTH_SHORT).show()
            }
        }








        return binding.root
    }

    // Función para convertir una foto en Base64
    fun convertirFotoUriABase64(context: Context, uriString: String?): String? {
        if (uriString.isNullOrEmpty()) {
            Log.e("Base64Error", "La URI de la imagen es nula o vacía.")
            return null
        }

        val uri = Uri.parse(uriString)
        Log.d("Base64Conversion", "Intentando convertir la imagen desde la URI: $uri")

        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            if (originalBitmap == null) {
                Log.e("Base64Error", "No se pudo decodificar la imagen.")
                return null
            }

            // Redimensionar imagen (máximo 800x800)
            val maxSize = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaleFactor = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scaleFactor).toInt(),
                (height * scaleFactor).toInt(),
                true
            )

            // Convertir imagen a Base64 sin comprimir para verificar el tamaño
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            // Verificar si el tamaño es mayor a 1MB
            if (outputStream.size() > 1024 * 1024) { // 1MB en bytes
                // Si el tamaño es mayor a 1MB, comprimir con calidad reducida (70%)
                Log.d("Base64Conversion", "La imagen es mayor a 1MB, comprimiendo calidad a 70%")
                outputStream.reset()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            }

            // Convertir a Base64 con la posible compresión
            val finalBase64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            Log.d("Base64Conversion", "Imagen convertida a Base64 con éxito")
            finalBase64String
        } catch (e: IOException) {
            Log.e("Base64Error", "Error al convertir la imagen: ${e.message}")
            e.printStackTrace()
            null
        }
    }



    // Función para convertir una imagen de la galeria en Base64
    fun convertirImagenUriABase64(context: Context, uri: Uri): String? {
        return try {
            // Abrir el InputStream desde la URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

            // Decodificar la imagen en un Bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Establecer un tamaño máximo para la imagen
            val maxWidth = 800 // Ancho máximo inicial
            val maxHeight = 600 // Alto máximo inicial

            // Calcular el factor de escala manteniendo las proporciones
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
            val scaleFactor = Math.min(maxWidth.toFloat() / originalWidth, maxHeight.toFloat() / originalHeight)

            // Redimensionar la imagen manteniendo las proporciones
            var resizedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (originalWidth * scaleFactor).toInt(),
                (originalHeight * scaleFactor).toInt(),
                true
            )

            // Comprimir la imagen en formato JPEG y verificar su tamaño
            var byteArrayOutputStream = ByteArrayOutputStream()
            var quality = 100 // Comenzar con la calidad máxima

            do {
                // Comprimir la imagen a la calidad actual
                byteArrayOutputStream.reset() // Limpiar el flujo de bytes
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

                // Verificar el tamaño de la imagen
                val imageBytes = byteArrayOutputStream.toByteArray()

                if (imageBytes.size > 1024 * 1024) { // Si la imagen es mayor que 1MB
                    quality -= 10 // Reducir la calidad
                } else {
                    break // Salir si el tamaño es menor o igual a 1MB
                }
            } while (quality > 10) // Limitar la calidad mínima para evitar demasiado deterioro

            // Convertir la imagen comprimida a Base64
            Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }







    //metodos para solicitar permiso para acceder a la galeria
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa READ_MEDIA_IMAGES en lugar de READ_EXTERNAL_STORAGE
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        } else {
            // Android 12 o menor usa READ_EXTERNAL_STORAGE
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        }
    }


    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
                // Mostrar una explicación antes de solicitar el permiso nuevamente
                AlertDialog.Builder(requireContext())
                    .setTitle("Permiso Necesario")
                    .setMessage("Este permiso es necesario para acceder a las imágenes de la galería.")
                    .setPositiveButton("Aceptar") { _, _ ->
                        requestStoragePermission()
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                requestStoragePermission()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permiso denegado. Habilítelo en ajustes.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 1001
    }

    //eliminar foto de el almacenmiento de la app despues de sincronizarla
    fun eliminarArchivo(context: Context, uri: Uri) {
        try {
            val rowsDeleted = context.contentResolver.delete(uri, null, null)
            if (rowsDeleted > 0) {
                Log.d("FileDelete", "Imagen eliminada correctamente de la galería")
            } else {
                Log.e("FileDelete", "No se pudo eliminar la imagen")
            }
        } catch (e: Exception) {
            Log.e("FileDelete", "Error al eliminar la imagen: ${e.message}")
            e.printStackTrace()
        }
    }




    /**
     * Función para verificar si ya se han procesado todos los reportes y mostrar un mensaje adecuado.
     */
    private fun verificarFinalizacion(total: Int, correctos: Int, errores: Int) {
        if (correctos + errores == total) {

            requireActivity().runOnUiThread {
                val loadingProgressBar: ProgressBar = binding.progressBar
                loadingProgressBar.visibility = View.GONE
                when {
                    correctos == total -> {
                        Toast.makeText(requireContext(), "Todos los datos se enviaron correctamente", Toast.LENGTH_LONG).show()
                    }
                    errores == total -> {
                        MaterialDialog(requireContext()).show {
                            title(text = "Error")
                            message(text = "Ningún dato se pudo enviar. Verifica tu conexión.")
                            icon(R.drawable.baseline_error_24)
                            positiveButton(text = "Aceptar")
                        }
                    }
                    else -> {
                        MaterialDialog(requireContext()).show {
                            title(text = "Envío Parcial")
                            message(text = "Algunos datos no se enviaron. $correctos enviados correctamente, $errores con error.")
                            icon(R.drawable.baseline_error_24)
                            positiveButton(text = "Aceptar")
                        }
                    }
                }
            }
        }
    }


}