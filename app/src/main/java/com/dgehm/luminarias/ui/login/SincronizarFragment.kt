package com.dgehm.luminarias.ui.login

import DatabaseHelper
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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


        btnSincronizar.setOnClickListener {
            // Asegúrate de que el contexto no sea nulo
            context?.let { nonNullContext ->

                // Verificar si la base de datos existe
                if (dbHelper.databaseExists()) {
                    println("Base de datos existente.")


                    // Obtener los reportes de falla
                    val reportesFalla = dbHelper.getReportesFalla()
                    // Mostrar los reportes (ejemplo: Log, RecyclerView, etc.)
                    for (reporte in reportesFalla) {



                        val base64String: String?

                        if (reporte.tipoImagen == "1") {
                            val uri = Uri.parse(reporte.urlFoto)
                            base64String = convertirImagenUriABase64(requireContext(), uri)
                        } else if (reporte.tipoImagen == "2") {
                            base64String = convertirFotoUriABase64(requireContext(),
                                Uri.parse(reporte.urlFoto).toString()
                            )
                        } else {
                            base64String = null
                        }

                        Log.d("ReporteFalla", "Reporte ID: ${base64String}")


                        val json = JSONObject().apply {
                            put(
                                "distrito_id", reporte.distritoId
                            ) // Si 'distrito' es un nombre, asegúrate de que 'distritoId' esté disponible
                            put(
                                "tipo_falla_id", reporte.tipoFallaId
                            ) // Asegúrate de que tipoFalla sea el ID del tipo de falla
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

                                val dialog = MaterialDialog(requireContext()).show {
                                    title(text = "Error")
                                    message(text = "Error al hacer la petición: ${e.message}")
                                    icon(R.drawable.baseline_error_24)
                                    positiveButton(text = "Aceptar")
                                }

                                // Cerrar el diálogo después de 2 segundos
                                Handler(Looper.getMainLooper()).postDelayed({
                                    dialog.dismiss()
                                }, 2000)

                            }

                            override fun onResponse(call: Call, response: Response) {
                                val responseData = response.body?.string()
                                Log.d("Response", responseData ?: "No se recibió ninguna respuesta")

                                requireActivity().runOnUiThread {
                                    if (response.isSuccessful) {
                                        val reportesFalla =   dbHelper.deleteReporteFallaById(reporte.id)


                                        if (reporte.tipoImagen == "2") {
                                            val uri = Uri.parse(reporte.urlFoto) // Convertir String a Uri
                                            eliminarArchivo(requireContext(), uri) // Pasar el contexto y la URI
                                        }




                                    } else {
                                        val dialog = MaterialDialog(requireContext()).show {
                                            title(text = "Error")
                                            message(text = "Error al enviar los datos")
                                            icon(R.drawable.baseline_error_24)
                                            positiveButton(text = "Aceptar")
                                        }

                                        // Cerrar el diálogo después de 2 segundos
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            dialog.dismiss()
                                        }, 2000)
                                    }
                                }
                            }
                        })

                        Log.d("body ", "body $json")
                    }



                } else {
                    dbHelper.copyDatabase()
                    Toast.makeText(
                        nonNullContext, "Base de datos creó exitosamente.", Toast.LENGTH_SHORT
                    ).show()
                }

                // Copiar la nueva base de datos
                //dbHelper.copyDatabase()

            } ?: run {
                // En caso de que `context` sea nulo, maneja el error aquí
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

        val uri = android.net.Uri.parse(uriString)
        Log.d("Base64Conversion", "Intentando convertir la imagen desde la URI: $uri")

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                Log.d("Base64Conversion", "Imagen convertida a Base64 con éxito")
                base64String
            }
        } catch (e: IOException) {
            Log.e("Base64Error", "Error al convertir la imagen: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Función para convertir una imagen de la galeria en Base64
    fun convertirImagenUriABase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } else {
                null
            }
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





}