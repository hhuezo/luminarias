package com.dgehm.luminarias.ui.reporte_falla_offline

import DatabaseHelper
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentReporteFallaIngresoOfflineBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


import android.location.Location
import android.location.LocationListener
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.navigation.fragment.findNavController


class ReporteFallaIngresoOfflineFragment : Fragment() {

    private var _binding: FragmentReporteFallaIngresoOfflineBinding? = null
    private val binding get() = _binding!!

    private lateinit var btnAdjuntarFoto: ImageView
    private lateinit var btnTomarFoto: ImageView
    private lateinit var imageViewFoto: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2
    private var photoUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imagenBase64: String? = null

    private var departamentoId: Int = 0
    private var distritoId: Int = 0
    private var municipioId: Int = 0
    private var tipoFallaId: Int = 0
    private var tipoImagen: String? = null

    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText
    private lateinit var loadingProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReporteFallaIngresoOfflineBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Reporte falla"

        val sharedPreferences = requireContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getInt("usuarioId", 0)
        val correoPreference = sharedPreferences.getString("correo", "")
        val usuarioPreference = sharedPreferences.getString("usuario", "")

        if (usuarioId == 0) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_LONG).show()
        }



        val departamentoSpinner: Spinner? = view.findViewById(R.id.departamentoSpinner)
        val tipoFallaSpinner: Spinner? = view.findViewById(R.id.spinnerTipoFalla)
        val municipioSpinner: Spinner? = view.findViewById(R.id.spinnerMunicipio)
        val distritoSpinner: Spinner? = view.findViewById(R.id.spinnerDistrito)

        val editTelefono: EditText = view.findViewById(R.id.editTelefono)
        val editDescripcion: EditText = view.findViewById(R.id.editDescripcion)
        val editNombreContacto: EditText = view.findViewById(R.id.editNombreContacto)
        val editCorreoContacto: EditText = view.findViewById(R.id.editCorreoContacto)
        val btnGetLocation: ImageButton = view.findViewById(R.id.btnGetLocation)

        editLatitude = view.findViewById(R.id.editLatitude)
        editLongitude = view.findViewById(R.id.editLongitude)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)


        if(correoPreference != "")
        {
            editCorreoContacto.setText(correoPreference)
        }

        if(usuarioPreference != "")
        {
            editNombreContacto.setText(usuarioPreference)
        }





        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            obtenerUbicacion()
        }





        val btnAceptar: Button = view.findViewById(R.id.btnAceptar)

        btnGetLocation.setOnClickListener {
            obtenerUbicacion()
        }


        var isEditing = false
        editTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitamos hacer nada aquí
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                // Evitar la recursividad
                if (isEditing) return
                isEditing = true

                // Eliminar todos los caracteres no numéricos
                val rawText = charSequence.toString().replace("[^0-9]".toRegex(), "")

                // Formatear el texto según la longitud
                val formattedText = when {
                    rawText.length <= 4 -> rawText
                    rawText.length <= 7 -> "${rawText.substring(0, 4)}-${rawText.substring(4)}"
                    else -> "${rawText.substring(0, 4)}-${rawText.substring(4, 8)}" // Solo hasta 8 caracteres
                }

                // Establecer el texto con la máscara
                editTelefono.setText(formattedText)
                editTelefono.setSelection(formattedText.length) // Colocar el cursor al final

                // Restablecer la variable de control
                isEditing = false
            }

            override fun afterTextChanged(editable: Editable?) {
                // No necesitamos hacer nada aquí
            }
        })

        val dbHelper = DatabaseHelper(requireContext())

        // Llama a getDepartamentos
        val departamentos = dbHelper.getDepartamentos()

        if (departamentos.isNotEmpty()) {
            // Agrega el elemento inicial "SELECCIONE" al principio de la lista
            val departamentosActualizados = listOf("SELECCIONE") + departamentos

            // Crea el adaptador con la lista actualizada
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, departamentosActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Asigna el adaptador al Spinner
            departamentoSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron departamentos.", Toast.LENGTH_SHORT).show()
        }

        // Aquí se obtiene el id del departamento seleccionado
        departamentoSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedDepartamento = parent.getItemAtPosition(position) as String
                    val departamentoIdSeleccionado = dbHelper.getDepartamentoId(selectedDepartamento)
                    departamentoId = departamentoIdSeleccionado
                    getMunicipioDep(departamentoId, municipioSpinner) // Pasar municipioSpinner
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        municipioSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val municipioSeleccionado = parent.getItemAtPosition(position).toString()

                // Solo llamamos a getDistritosMunicipio si el municipio seleccionado no es "SELECCIONE"
                if (municipioSeleccionado != "SELECCIONE") {
                    val municipioIdSeleccionado = dbHelper.getMunicipioId(departamentoId,municipioSeleccionado)
                    municipioId = municipioIdSeleccionado
                    getDistritosMunicipio(municipioIdSeleccionado, distritoSpinner)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        distritoSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val distritoSeleccionado = parent.getItemAtPosition(position).toString()

                // Solo ejecutamos lógica adicional si el distrito seleccionado no es "SELECCIONE"
                if (distritoSeleccionado != "SELECCIONE") {
                    val distritoIdSeleccionado = dbHelper.getDistritoId(municipioId, distritoSeleccionado)
                    distritoId = distritoIdSeleccionado

                    //Toast.makeText(requireContext(), "Distrito. $distritoId", Toast.LENGTH_SHORT).show()
                }
                else{
                    distritoId = 0

                    //Toast.makeText(requireContext(), "Distrito. $distritoId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        tipoFallaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) { // Ignorar la primera posición ("SELECCIONE", etc.)
                    val selectedTipoFalla = parent.getItemAtPosition(position) as String
                    val tipoFallaIdSeleccionado = dbHelper.getTipoFallaId(selectedTipoFalla)

                    // Asignar el valor válido al tipoFallaId
                   tipoFallaId = tipoFallaIdSeleccionado


                    // Aquí puedes realizar las acciones necesarias con el ID del tipo de falla
                    //Toast.makeText(requireContext(), "Tipo de Falla ID: $tipoFallaIdSeleccionado", Toast.LENGTH_SHORT).show()
                }
                else{
                    tipoFallaId = 0
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }




        // Llama a getTiposFalla
        val tiposFalla = dbHelper.getTiposFalla()

        if (tiposFalla.isNotEmpty()) {
            // Agrega el elemento inicial "SELECCIONE" al principio de la lista
            val tiposFallaActualizados = listOf("SELECCIONE") + tiposFalla

            // Crea el adaptador con la lista actualizada
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposFallaActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Asigna el adaptador al Spinner
            tipoFallaSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron tipos de falla.", Toast.LENGTH_SHORT).show()
        }




        btnAdjuntarFoto = view.findViewById(R.id.btnAdjuntarFoto)
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto)
        imageViewFoto = view.findViewById(R.id.imageViewFoto)

        btnAdjuntarFoto.setOnClickListener {
            openGallery()
        }

        btnTomarFoto.setOnClickListener {
            checkPermissions()
        }




        btnAceptar.setOnClickListener {
            // Acción a realizar cuando se hace clic en el botón
            Log.d("Debug", "Botón Aceptar presionado")

            // Obtener los valores de los campos de texto
            val telefono = editTelefono.text.toString().trim()
            val descripcion = editDescripcion.text.toString().trim()
            val nombreContacto = editNombreContacto.text.toString().trim()
            val correoContacto = editCorreoContacto.text.toString().trim()

            val latitud = editLatitude.text.toString().trim()
            val longitud = editLongitude.text.toString().trim()



            if (latitud.isEmpty() || longitud.isEmpty()) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Debe ingresar la ubicación.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            //Toast.makeText(requireContext(), "Tipo de Falla ID: $tipoFallaId", Toast.LENGTH_SHORT).show()
            if (distritoId <= 0) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Seleccione un distrito válido.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            if (tipoFallaId <= 0) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Seleccione un tipo de falla válido.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }



            if (descripcion.isEmpty()) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "La descripción no puede estar vacía.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            if (nombreContacto.isEmpty()) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "El nombre del contacto no puede estar vacío.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }




            if (correoContacto.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correoContacto).matches()) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Ingrese un correo válido.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            if (telefono.isEmpty() || !telefono.matches(Regex("\\d{4}-\\d{4}"))) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "El teléfono debe tener el formato 9999-9999.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            if (photoUri == null) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Debe seleccionar una foto.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }



            // Llamar al método de inserción
            val dbHelper = DatabaseHelper(requireContext())
            val newRowId = dbHelper.insertReporteFalla(
                distritoId,
                tipoFallaId,
                descripcion,
                latitud,
                longitud,
                telefono,
                nombreContacto,
                correoContacto,
                usuarioId,
                photoUri.toString(),
                tipoImagen
            )

            // Verificar si la inserción fue exitosa
            if (newRowId != -1L) {
                Toast.makeText(requireContext(), "Registro guardado correctamente", Toast.LENGTH_SHORT).show()
                //redicreccion al reporte de falla
                val action = ReporteFallaIngresoOfflineFragmentDirections.actionReporteFallaIngresoOfflineFragmentToReporteFallaOfflineFragment()
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Error al guardar el registro", Toast.LENGTH_SHORT).show()
            }

        }


    }


    // Método para obtener la ubicación
    private fun obtenerUbicacion() {
        loadingProgressBar.visibility = View.VISIBLE
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Verificar si el GPS está habilitado
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(requireContext(), "Por favor, activa el GPS", Toast.LENGTH_SHORT).show()
            loadingProgressBar.visibility = View.GONE // Ocultar el ProgressBar si el GPS no está habilitado
            return
        }

        // Verificar si se tienen los permisos necesarios
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen permisos, solicitarlos
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            loadingProgressBar.visibility = View.GONE // Ocultar el ProgressBar si no se tienen permisos
            return
        }

        // Si ya se tienen permisos, pedir las actualizaciones de ubicación
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude

                // Mostrar las coordenadas en los EditText
                editLatitude.setText(latitude.toString())
                editLongitude.setText(longitude.toString())

                // Dejar de recibir actualizaciones de ubicación
                locationManager.removeUpdates(this)

                // Ocultar el ProgressBar después de obtener la ubicación
                loadingProgressBar.visibility = View.GONE
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Solicitar actualizaciones de ubicación
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,  // Tiempo mínimo entre actualizaciones (en milisegundos)
            0f,  // Distancia mínima entre actualizaciones (en metros)
            locationListener
        )
    }




    fun getMunicipioDep(id: Int, municipioSpinner: Spinner?) {
        val dbHelper = DatabaseHelper(requireContext())
        val municipios = dbHelper.getMunicipios(id)

        if (municipios.isNotEmpty()) {
            val municipiosActualizados = listOf("SELECCIONE") + municipios

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, municipiosActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            municipioSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron municipios.", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDistritosMunicipio(municipioId: Int, distritoSpinner: Spinner?) {
        val dbHelper = DatabaseHelper(requireContext())
        val distritos = dbHelper.getDistritos(municipioId)

        if (distritos.isNotEmpty()) {
            val distritosActualizados = listOf("SELECCIONE") + distritos

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, distritosActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            distritoSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron distritos.", Toast.LENGTH_SHORT).show()
        }
    }




    private fun openGallery() {
        // Intent para seleccionar una imagen de la galería
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                // Para galería
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        photoUri = uri
                        tipoImagen = "1"
                        imageViewFoto.setImageURI(uri)
                        // Hacer visible el ImageView
                        imageViewFoto.visibility = View.VISIBLE
                    }
                }

                // Para cámara
                CAMERA_REQUEST -> {
                    photoUri?.let { uri ->
                        imageViewFoto.setImageURI(uri)
                        tipoImagen = "2"
                        // Hacer visible el ImageView
                        imageViewFoto.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    // Método para verificar permisos
    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
        } else {
            openCamera()  // Si el permiso está concedido, abrir la cámara
        }
    }

    // Método para abrir la cámara
    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "com.dgehm.luminarias.fileprovider",
            photoFile
        )
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    // Método para convertir la imagen de la URI a Base64
    private fun createImageFile(): File {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_",
            ".jpg",
            storageDir
        )
    }

}