package com.dgehm.luminarias.ui.censo

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
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
import android.widget.Switch
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.dgehm.luminarias.GlobalUbicacion
import com.dgehm.luminarias.HttpClient
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentCensoIngresoBinding
import com.dgehm.luminarias.model.Compania
import com.dgehm.luminarias.model.Departamento
import com.dgehm.luminarias.model.Distrito
import com.dgehm.luminarias.model.Municipio
import com.dgehm.luminarias.model.PotenciaPromedio
import com.dgehm.luminarias.model.PotenciaPromedioResponse
import com.dgehm.luminarias.model.ResponseCensoCreate
import com.dgehm.luminarias.model.ResponseDistrito
import com.dgehm.luminarias.model.ResponseMunicipio
import com.dgehm.luminarias.model.Tipo
import com.dgehm.luminarias.model.TipoFalla
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CensoIngresoFragment : Fragment() {

    private var _binding: FragmentCensoIngresoBinding? = null
    private val binding get() = _binding!!

    private val client by lazy { HttpClient(requireContext()) }


    private var latitud: Float = 0f
    private var longitud: Float = 0f
    private var departamentoId: Int = 0
    private var distritoId: Int = 0
    private var municipioId: Int = 0
    private var direccion: String = ""
    private var tipoFallaId: Int = 0
    private var usuarioId: Int = 0
    private var tipoLuminariaId: Int = 0
    private var companiaId: Int = 0
    private var potenciaPromedioId: Int = 0
    private var potenciaPromedio: String = ""

    private var departamentosList: List<Departamento> = emptyList()
    private var companiaList: List<Compania> = emptyList()

    private var municipiosList: List<Municipio> = emptyList()
    private var distritosList: List<Distrito> = emptyList()

    private var tipoLuminariaList: List<Tipo> = emptyList()
    private var tipoFallaList: List<TipoFalla> = emptyList()
    private var potenciaPromedioList: List<PotenciaPromedio> = emptyList()


    private lateinit var btnAdjuntarFoto: ImageView
    private lateinit var btnTomarFoto: ImageView
    private lateinit var imageViewFoto: ImageView

    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST = 2
    private var photoUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imagenBase64: String? = null


    private var isFirstSelection = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCensoIngresoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Censo luminaria"

        // Obtener los argumentos pasados desde el fragmento anterior
        latitud = GlobalUbicacion.latitud!!
        longitud = GlobalUbicacion.longitud!!
        departamentoId = GlobalUbicacion.departamentoId!!
        distritoId = GlobalUbicacion.distritoId!!
        municipioId = GlobalUbicacion.municipioId!!
        usuarioId = GlobalUbicacion.usuarioId!!
        direccion = GlobalUbicacion.direccion.toString()

        usuarioId = 1

        val editDireccion: EditText? = view.findViewById(R.id.editDireccion)
        editDireccion?.setText(direccion)

        val departamentoSpinner: Spinner? = view.findViewById(R.id.departamentoSpinner)
        val municipioSpinner: Spinner? = view.findViewById(R.id.spinnerMunicipio)
        val distritoSpinner: Spinner? = view.findViewById(R.id.spinnerDistrito)
        val companiaSpinner: Spinner? = view.findViewById(R.id.spinnerCompania)
        val tipoLuminariaSpinner: Spinner? = view.findViewById(R.id.spinnerTipoLuminaria)
        val tipoFallaSpinner: Spinner? = view.findViewById(R.id.spinnerTipoFalla)
        val potenciaPromedioSpinner: Spinner? = view.findViewById(R.id.spinnerPotenciaPromedio)


        val editPotenciaNominal: EditText = view.findViewById(R.id.editPotenciaNominal)
        val editConsumoMensual: EditText = view.findViewById(R.id.editConsumoMensual)
        val switchCondicion: Switch = view.findViewById(R.id.switchCondicion)
        val editObservacion: EditText = view.findViewById(R.id.editObservacion)
        val cardMensaje: CardView = view.findViewById(R.id.cardMensaje)


        val btnAceptar: Button = view.findViewById(R.id.btnAceptar)


        if (departamentoId != 0)
        {
            getMunicipio(departamentoId)
        }


        if (distritoId != 0)
        {
            getDistrito(municipioId)
        }

        var isEditing = false



        editPotenciaNominal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Aquí puedes realizar acciones antes de que el texto cambie.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Aquí puedes realizar acciones mientras el texto está cambiando.
            }

            override fun afterTextChanged(s: Editable?) {
                if (tipoLuminariaId == 1) {
                    // Convertir el texto a String y luego a Double
                    val potenciaText = s?.toString() ?: "" // Usamos `s` directamente
                    val potencia: Double = potenciaText.toDoubleOrNull() ?: 0.0 // Convierte o asigna 0.0

                    // Calcular el consumo
                    val consumo = (potencia * 360) / 1000

                    editConsumoMensual.setText(consumo.toString())
                }
            }

        })



        departamentoSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                // Imprimir la lista de departamentos para verificar los datos
                departamentosList.forEachIndexed { index, departamento ->
                    Log.d("Debug", "Índice: $index, Departamento: ${departamento.nombre}, ID: ${departamento.id}")
                }

                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                // Validar índice y obtener ID
                if (position in departamentosList.indices) {
                    val departamentoSeleccionado = departamentosList[position]
                    departamentoId = departamentoSeleccionado.id
                    Log.d(
                        "Debug",
                        "Departamento seleccionado: ${departamentoSeleccionado.nombre}, " +
                                "ID: $departamentoId, Posición: $position"
                    )

                    if (departamentoId > 0) {
                        getMunicipio(departamentoId)
                    }
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada si no se selecciona nada
            }
        }


        municipioSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Imprimir la lista de municipios para verificar los datos
                municipiosList.forEachIndexed { index, municipio ->
                    Log.d("Debug", "Índice: $index, Municipio: ${municipio.nombre}, ID: ${municipio.id}")
                }

                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                // Validar índice y obtener ID
                if (position in municipiosList.indices) {
                    val municipioSeleccionado = municipiosList[position]
                    municipioId = municipioSeleccionado.id
                    Log.d(
                        "Debug",
                        "Municipio seleccionado: ${municipioSeleccionado.nombre}, " +
                                "ID: $municipioId, Posición: $position"
                    )

                    if (municipioId > 0) {
                        // Llamada a la función o lógica para manejar la selección del municipio
                        // Por ejemplo, puedes cargar más información basada en el municipio seleccionado
                        getDistrito(municipioId)
                    }
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada si no se selecciona nada
            }
        }


        distritoSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Imprimir la lista de distritos para verificar los datos
                distritosList.forEachIndexed { index, distrito ->
                    Log.d("Debug", "Índice: $index, Distrito: ${distrito.nombre}, ID: ${distrito.id}")
                }

                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                Log.d("Debug","posicion: $position")

                // Validar índice y obtener ID
                if (position in distritosList.indices) {
                    val distritoSeleccionado = distritosList[position]
                    distritoId = distritoSeleccionado.id
                    Log.d(
                        "Debug",
                        "Distrito seleccionado: ${distritoSeleccionado.nombre}, " +
                                "ID: $distritoId, Posición: $position"
                    )
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada si no se selecciona nada
            }
        }

        companiaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {



                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                Log.d("Debug","posicion: $position")

                // Validar índice y obtener ID
                if (position in companiaList.indices) {
                    val companiaSeleccionado = companiaList[position]
                    companiaId = companiaSeleccionado.id

                    Log.d(
                        "Debug",
                        "Compañia seleccionada: ${companiaSeleccionado.nombre}, " +
                                "ID: $companiaId, Posición: $position"
                    )
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        tipoFallaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                Log.d("Debug", "posicion: $position")

                // Validar índice y obtener ID
                if (position in tipoFallaList.indices) {
                    val tipoFallaSeleccionado = tipoFallaList[position]
                    tipoFallaId = tipoFallaSeleccionado.id





                    Log.d(
                        "Debug",
                        "Tipo de falla seleccionada: ${tipoFallaSeleccionado.nombre}, " +
                                "ID: $tipoFallaId, Posición: $position"
                    )
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción opcional cuando no se selecciona nada
            }
        }

        tipoLuminariaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                Log.d("Debug", "posicion: $position")

                // Validar índice y obtener ID
                if (position in tipoLuminariaList.indices) {
                    val tipoLuminariaSeleccionado = tipoLuminariaList[position]
                    tipoLuminariaId = tipoLuminariaSeleccionado.id

                    getPotencia(tipoLuminariaId)

                    /*Toast.makeText(
                        requireContext(),
                        "Tipo de luminaria seleccionada: ${tipoLuminariaSeleccionado.nombre}, ID: $tipoLuminariaId, Posición: $position",
                        Toast.LENGTH_SHORT
                    ).show()*/

                    requireActivity().runOnUiThread {
                        if (tipoLuminariaId == 1) {
                            // Usa View.VISIBLE en lugar de view.VISIBLE
                            potenciaPromedioSpinner?.visibility = View.GONE
                            editPotenciaNominal.visibility = View.VISIBLE
                        }
                        else{
                            potenciaPromedioSpinner?.visibility = View.VISIBLE
                            editPotenciaNominal.visibility = View.GONE
                        }

                        potenciaPromedioId = 0
                        potenciaPromedio = ""
                        editConsumoMensual.setText("")
                        editPotenciaNominal.setText("")
                    }

                    Log.d(
                        "Debug",
                        "Tipo de luminaria seleccionada: ${tipoLuminariaSeleccionado.nombre}, " +
                                "ID: $tipoLuminariaId, Posición: $position"
                    )
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción opcional cuando no se selecciona nada
            }
        }


        potenciaPromedioSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Evitar que se ejecute en la carga inicial
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }

                Log.d("Debug", "Posición seleccionada: $position")

                // Validar índice y obtener ID
                if (position in potenciaPromedioList.indices) {
                    val potenciaPromedioSeleccionada = potenciaPromedioList[position]
                    potenciaPromedioId = potenciaPromedioSeleccionada.id
                    potenciaPromedio = potenciaPromedioSeleccionada.potencia
                    if(potenciaPromedioSeleccionada.consumo_promedio == "0")
                    {
                        editConsumoMensual.setText("")
                    }
                    else{
                        editConsumoMensual.setText(potenciaPromedioSeleccionada.consumo_promedio)
                    }

                    Log.d(
                        "Debug",
                        "Potencia promedio seleccionada: ${potenciaPromedioSeleccionada.potencia}, " +
                                "ID: $potenciaPromedioId, Posición: $position"
                    )
                } else {
                    Log.e("Error", "Posición fuera de rango: $position")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción opcional cuando no se selecciona nada
            }
        }


        client.get("/api_censo_luminaria/create?departamento_id=$departamentoId&distrito_id=$distritoId&latitude=$latitud&longitude=$longitud&usuario_id=$usuarioId", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Fallo al obtener los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                //Log.d("url","url: /api_censo_luminaria/create?departamento_id=$departamentoId&distrito_id=$distritoId&latitude=$latitud&longitude=$longitud&usuario_id=$usuarioId")
                //Log.d("respose","response $responseData")
                if (responseData != null) {
                    try {
                        val gson = Gson()
                        val dataCensoCreate = gson.fromJson(responseData, ResponseCensoCreate::class.java)

                        val dataResponse = dataCensoCreate.data

                        val puntosCercanos = dataResponse.puntosCercanos

                        departamentosList = dataResponse?.departamentos ?: emptyList()

                        // Agregar la opción "Seleccione" al inicio
                        departamentosList = listOf(Departamento(id = 0, nombre = "SELECCIONE")) + departamentosList

                        Log.d("id", "departamentos $departamentosList")

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            departamentosList.map { it.nombre } // Muestra los nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)




                        //compañia
                        companiaList = dataResponse?.companias ?: emptyList()

                        // Agregar la opción "Seleccione" al inicio
                        companiaList = listOf(Compania(id = 0, nombre = "SELECCIONE")) + companiaList

                        val adapterCompania = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            companiaList.map { it.nombre } // Muestra los nombres
                        )
                        adapterCompania.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


                        //tipoLuminariaSpinner
                        tipoLuminariaList = dataResponse?.tipos ?: emptyList()

                        // Agregar la opción "Seleccione" al inicio
                        tipoLuminariaList = listOf(Tipo(id = 0, nombre = "SELECCIONE")) + tipoLuminariaList
                        val adapterTipoLuminaria= ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            tipoLuminariaList.map { it.nombre } // Muestra los nombres
                        )
                        adapterTipoLuminaria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)



                        //tipos de falla
                        tipoFallaList = dataResponse?.tipos_falla ?: emptyList()

                        // Agregar la opción "Seleccione" al inicio
                        tipoFallaList = listOf(TipoFalla(id = 0, nombre = "SELECCIONE")) + tipoFallaList

                        val adapterTipoFalla = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            tipoFallaList.map { it.nombre } // Muestra los nombres
                        )
                        adapterTipoFalla.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)



                        requireActivity().runOnUiThread {

                            if(puntosCercanos > 0)
                            {
                                cardMensaje.visibility = View.VISIBLE
                            }

                            if (departamentoSpinner != null) {
                                // Asignar el adaptador al Spinner
                                departamentoSpinner.adapter = adapter

                                if (companiaSpinner != null) {
                                    companiaSpinner.adapter = adapterCompania
                                }

                                if (tipoLuminariaSpinner != null) {
                                    tipoLuminariaSpinner.adapter = adapterTipoLuminaria
                                }

                                if (tipoFallaSpinner != null) {
                                    tipoFallaSpinner.adapter = adapterTipoFalla
                                }

                                // Seleccionar automáticamente la opción correspondiente a departamentoId
                                if (departamentoId != 0) {
                                    val selectedIndex = departamentosList.indexOfFirst { it.id == departamentoId }
                                    if (selectedIndex >= 0) {
                                        departamentoSpinner.setSelection(selectedIndex)
                                    }
                                }
                            }
                        }

                    } catch (e: JsonSyntaxException) {
                        Log.e("JSON_PARSE_ERROR", "Error al parsear JSON: ${e.message}")
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Error al parsear JSON",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "No hay datos: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })





        btnAdjuntarFoto = view.findViewById(R.id.btnAdjuntarFoto)
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto)
        imageViewFoto = view.findViewById(R.id.imageViewFoto)

        btnAdjuntarFoto.setOnClickListener {
            openGallery()
        }

        btnTomarFoto.setOnClickListener {
            //openCamera()
            checkPermissions()
        }




        btnAceptar.setOnClickListener {
            // Acción a realizar cuando se hace clic en el botón
            Log.d("Debug", "Botón Aceptar presionado")



            // Validaciones para los campos requeridos


            // Validar distrito (debe ser diferente de 0)
            if (distritoId == 0) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Por favor, seleccione un distrito.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }

                // Cerrar el diálogo después de 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }


            // Validar compañia (debe ser diferente de 0)
            if (companiaId == 0) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Por favor, seleccione una compañía.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }

                // Cerrar el diálogo después de 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            // Validar tipo de luminaria (debe ser diferente de 0)
            if (tipoLuminariaId == 0) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Por favor, seleccione un tipo de luminaria.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }

                // Cerrar el diálogo después de 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }



            // Validar dirección
            if (editDireccion?.text.isNullOrEmpty()) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "La dirección no puede estar vacía.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }

                // Cerrar el diálogo después de 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            // Validar potencia nominal
            if (tipoLuminariaId == 1) {

                if (editPotenciaNominal.text.isNullOrEmpty()) {
                    val dialog = MaterialDialog(requireContext()).show {
                        title(text = "Error")
                        message(text = "La potencia no puede estar vacía.")
                        icon(R.drawable.baseline_error_24)
                        positiveButton(text = "Aceptar")
                    }

                    // Cerrar el diálogo después de 2 segundos
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 2000)
                    return@setOnClickListener
                }

            }
            else{
                if (potenciaPromedioId == 0) {
                    val dialog = MaterialDialog(requireContext()).show {
                        title(text = "Error")
                        message(text = "La potencia no puede estar vacía.")
                        icon(R.drawable.baseline_error_24)
                        positiveButton(text = "Aceptar")
                    }

                    // Cerrar el diálogo después de 2 segundos
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 2000)
                    return@setOnClickListener
                }
            }


            // Validar consumo mensual
            if (editConsumoMensual.text.isNullOrEmpty()) {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "El consumo mensual no puede estar vacío.")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }

                // Cerrar el diálogo después de 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }

            if (switchCondicion.isChecked == false && tipoFallaId == 0)
            {
                val dialog = MaterialDialog(requireContext()).show {
                    title(text = "Error")
                    message(text = "Debe seleccionar un tipo de falla")
                    icon(R.drawable.baseline_error_24)
                    positiveButton(text = "Aceptar")
                }

                // Cerrar el diálogo después de 2 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                }, 2000)
                return@setOnClickListener
            }



            // Capturar los IDs seleccionados de los Spinners
            val direccion = editDireccion?.text.toString()
            val potenciaNominal = editPotenciaNominal.text.toString()
            val consumoMensual = editConsumoMensual.text.toString()
            val lamparaCondicion = switchCondicion.isChecked
            val observacion = editObservacion.text.toString()



            if (photoUri != null) {
                try {
                    requireContext().contentResolver.openInputStream(photoUri!!)
                        .use { inputStream ->
                            val originalBytes = inputStream?.readBytes()
                            val originalSize = originalBytes?.size ?: 0

                            if (originalSize > 800 * 1024) {  // 800 KB in bytes
                                // Compress image
                                val bitmap = originalBytes?.let { it1 -> BitmapFactory.decodeByteArray(originalBytes, 0, it1.size) }
                                val compressedBitmap = bitmap?.let { it1 -> compressBitmap(it1) }

                                // Convert compressed bitmap to Base64
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                if (compressedBitmap != null) {
                                    compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                                }
                                val compressedBytes = byteArrayOutputStream.toByteArray()
                                imagenBase64 = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                            } else {
                                // No compression needed, just Base64 encode original image
                                imagenBase64 = Base64.encodeToString(originalBytes, Base64.DEFAULT)
                            }
                            // Log.d("ImagenBase64", imagenBase64 ?: "La cadena es nula")
                        }
                } catch (e: IOException) {
                    Log.e("ImagenBase64", "Error al leer la imagen", e)
                }
            }

            val json = JSONObject().apply {
                put("usuario", usuarioId)
                put("latitud", latitud)
                put("longitud", longitud)
                put("distrito_id", distritoId)
                put("tipo_falla_id", tipoFallaId)
                put("compania_id", companiaId)
                put("direccion", direccion)
                put("tipo_luminaria_id", tipoLuminariaId)
                put("potencia_nominal", potenciaNominal)
                put("potencia_promedio", potenciaPromedioId)
                put("consumo_mensual", consumoMensual)
                put("condicion_lampara", lamparaCondicion)
                put("tipo_falla", tipoFallaId)
                put("observacion", observacion)
                put(
                    "imagen",
                    if (imagenBase64.isNullOrEmpty()) JSONObject.NULL else imagenBase64
                )
            }.toString()

            Log.d("json", "Botón Aceptar presionado $json")

            // Realizar la petición POST
            val endpoint = "/api_censo_luminaria"
            client.post(endpoint, json, object : Callback {
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
                            val dialog = MaterialDialog(requireContext()).show {
                                title(text = "Ok")
                                message(text = "Registro guardado correctamente")
                                icon(R.drawable.baseline_check_circle_24)
                                positiveButton(text = "Aceptar")
                            }

                            // Cerrar el diálogo después de 2 segundos
                            Handler(Looper.getMainLooper()).postDelayed({
                                dialog.dismiss()
                            }, 2000)

                            //redicreccion al censo index
                            val action = CensoIngresoFragmentDirections.actionCensoIngresoFragmentToCensoFragment()
                            findNavController().navigate(action)

                            //requireActivity().supportFragmentManager.popBackStack()
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

        }






    }





    fun getMunicipio(id: Int) {
        Log.d("Munucipio id: ","Munucipio id: "+id)
        client.get("/api_get_municipios/$id", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Fallo al obtener los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val gson = Gson()
                        val dataResponseMunicipio = gson.fromJson(responseData, ResponseMunicipio::class.java)

                        Log.d("municipios","municipios "+dataResponseMunicipio)

                        municipiosList = dataResponseMunicipio?.municipios ?: emptyList()

                        // Agregar la opción "Seleccione" al inicio
                        municipiosList = listOf(Municipio(id = 0, nombre = "SELECCIONE")) + municipiosList

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            municipiosList.map { it.nombre } // Muestra los nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        val municipioSpinner: Spinner? = view?.findViewById(R.id.spinnerMunicipio)
                        requireActivity().runOnUiThread {
                            if (municipioSpinner != null) {
                                municipioSpinner.adapter = adapter

                                // Seleccionar automáticamente la opción correspondiente a departamentoId
                                if (municipioId != 0) {
                                    val selectedIndex = municipiosList.indexOfFirst { it.id == municipioId }
                                    if (selectedIndex >= 0) {
                                        municipioSpinner.setSelection(selectedIndex)
                                    }
                                }
                            }
                        }



                    } catch (e: JsonSyntaxException) {
                        Log.e("JSON_PARSE_ERROR", "Error al parsear JSON: ${e.message}")
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Error al parsear JSON",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "No hay datos: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }


    fun getDistrito(id: Int) {
        Log.d("distrito id: ","distrito id: "+id)
        client.get("/api_get_distritos/$id", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Fallo al obtener los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val gson = Gson()
                        val dataResponseDistritos = gson.fromJson(responseData, ResponseDistrito::class.java)

                        distritosList = dataResponseDistritos?.distritos?: emptyList()

                        Log.d("distritos","distritos list "+distritosList)

                        // Agregar la opción "Seleccione" al inicio
                        distritosList = listOf(Distrito(id = 0, nombre = "SELECCIONE")) + distritosList

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            distritosList.map { it.nombre } // Muestra los nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        val distritoSpinner: Spinner? = view?.findViewById(R.id.spinnerDistrito)
                        requireActivity().runOnUiThread {
                            if (distritoSpinner != null) {
                                distritoSpinner.adapter = adapter

                                // Seleccionar automáticamente la opción correspondiente a departamentoId
                                if (distritoId != 0) {
                                    val selectedIndex = distritosList.indexOfFirst { it.id == distritoId }
                                    if (selectedIndex >= 0) {
                                        distritoSpinner.setSelection(selectedIndex)
                                    }
                                }
                            }
                        }

                    } catch (e: JsonSyntaxException) {
                        Log.e("JSON_PARSE_ERROR", "Error al parsear JSON: ${e.message}")
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Error al parsear JSON",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "No hay datos: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    fun getPotencia(id: Int) {
        client.get("/api_censo_luminaria/get_potencia_promedio/$id", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Fallo al obtener los datos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val gson = Gson()
                        val dataResponsePotencia = gson.fromJson(responseData, PotenciaPromedioResponse::class.java)

                        potenciaPromedioList = dataResponsePotencia?.potencia_promedio?: emptyList()

                        Log.d("potenciaPromedioList","potenciaPromedioList "+potenciaPromedioList)

                        // Agregar la opción "Seleccione" al inicio
                        potenciaPromedioList = listOf(PotenciaPromedio(id = 0, potencia = "SELECCIONE", consumo_promedio = "0")) + potenciaPromedioList

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            potenciaPromedioList.map { it.potencia } // Muestra los nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        val potenciaPromedioSpinner: Spinner? = view?.findViewById(R.id.spinnerPotenciaPromedio)
                        requireActivity().runOnUiThread {
                            if (potenciaPromedioSpinner != null) {
                                potenciaPromedioSpinner.adapter = adapter

                                // Seleccionar automáticamente la opción correspondiente a departamentoId
                                if (potenciaPromedioId != 0) {
                                    val selectedIndex = potenciaPromedioList.indexOfFirst { it.id == potenciaPromedioId }
                                    if (selectedIndex >= 0) {
                                        potenciaPromedioSpinner.setSelection(selectedIndex)
                                    }
                                }
                            }
                        }

                    } catch (e: JsonSyntaxException) {
                        Log.e("JSON_PARSE_ERROR", "Error al parsear JSON: ${e.message}")
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Error al parsear JSON",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "No hay datos: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
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
                        imageViewFoto.setImageURI(uri)
                        // Hacer visible el ImageView
                        imageViewFoto.visibility = View.VISIBLE
                    }
                }

                // Para cámara
                CAMERA_REQUEST -> {
                    photoUri?.let { uri ->
                        imageViewFoto.setImageURI(uri)
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



    // Compress image by resizing it (you can adjust the size as per your needs)
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 800  // Max width for the compressed image
        val maxHeight = 800  // Max height for the compressed image

        val width = bitmap.width
        val height = bitmap.height

        val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }





    override fun onResume() {
        super.onResume()

        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Reporte de falla"
            setDisplayHomeAsUpEnabled(true) // Muestra el botón de retroceso
        }

        // Manejar la acción del botón de retroceso
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberar el binding cuando la vista sea destruida
        _binding = null
    }



}