package com.dgehm.luminarias.ui.censo_offline

import DatabaseHelper
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentCensoIngresoOfflineBinding
import com.dgehm.luminarias.model.PotenciaPromedio
import com.dgehm.luminarias.model.PotenciaPromedioResponse
import com.dgehm.luminarias.ui.censo.CensoIngresoFragmentDirections
import com.dgehm.luminarias.ui.reporte_falla_offline.ReporteFallaIngresoOfflineFragmentDirections
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class CensoIngresoOfflineFragment : Fragment() {

    private var _binding: FragmentCensoIngresoOfflineBinding? = null
    private val binding get() = _binding!!



    private lateinit var editLatitude: EditText
    private lateinit var editLongitude: EditText
    private lateinit var loadingProgressBar: ProgressBar

    private lateinit var potenciaPromedioSpinner: Spinner

    private var departamentoId: Int = 0
    private var distritoId: Int = 0
    private var municipioId: Int = 0
    private var tipoFallaId: Int = 0
    private var companiaId: Int = 0
    private var tipoLuminariaId: Int = 0
    private var potenciaPromedioId: Int = 0
    private var potenciaPromedio: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCensoIngresoOfflineBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Censo"

        val sharedPreferences = requireContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getInt("usuarioId", 0)

        if (usuarioId == 0) {
            Toast.makeText(requireContext(), "Error: Usuario no encontrado", Toast.LENGTH_LONG)
                .show()
        }


        val departamentoSpinner: Spinner? = view.findViewById(R.id.departamentoSpinner)
        val tipoFallaSpinner: Spinner? = view.findViewById(R.id.spinnerTipoFalla)
        val municipioSpinner: Spinner? = view.findViewById(R.id.spinnerMunicipio)
        val distritoSpinner: Spinner? = view.findViewById(R.id.spinnerDistrito)
        val companiaSpinner: Spinner? = view.findViewById(R.id.spinnerCompania)
        val tipoLuminariaSpinner: Spinner? = view.findViewById(R.id.spinnerTipoLuminaria)

        potenciaPromedioSpinner = view.findViewById(R.id.spinnerPotenciaPromedio)


        val editPotenciaNominal: EditText = view.findViewById(R.id.editPotenciaNominal)
        val editConsumoMensual: EditText = view.findViewById(R.id.editConsumoMensual)
        val switchCondicion: Switch = view.findViewById(R.id.switchCondicion)
        val editObservacion: EditText = view.findViewById(R.id.editObservacion)
        val editDireccion: EditText? = view.findViewById(R.id.editDireccion)


        val btnGetLocation: ImageButton = view.findViewById(R.id.btnGetLocation)



        editLatitude = view.findViewById(R.id.editLatitude)
        editLongitude = view.findViewById(R.id.editLongitude)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)


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


        // Llama a get companias
        val companias = dbHelper.getCompanias()

       if (companias.isNotEmpty()) {
            // Agrega el elemento inicial "SELECCIONE" al principio de la lista
            val companiasActualizados = listOf("SELECCIONE") + companias

            // Crea el adaptador con la lista actualizada
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, companiasActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Asigna el adaptador al Spinner
            companiaSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron compañias.", Toast.LENGTH_SHORT).show()
        }


        // Llama a get ipo luminarias
        val tipoLuminarias = dbHelper.getTipoLuminarias()

        if (tipoLuminarias.isNotEmpty()) {
            // Agrega el elemento inicial "SELECCIONE" al principio de la lista
            val tiposActualizados = listOf("SELECCIONE") + tipoLuminarias

            // Crea el adaptador con la lista actualizada
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Asigna el adaptador al Spinner
            tipoLuminariaSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron tipo de luminarias.", Toast.LENGTH_SHORT).show()
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


        //onchange para obtener id de compania seleccionada
        companiaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedCompania = parent.getItemAtPosition(position) as String
                    val companiaIdSeleccionado = dbHelper.getCompaniaId(selectedCompania)
                    companiaId = companiaIdSeleccionado


                    // Mostrar Toast con el ID de la compañía seleccionada
                    //Toast.makeText(parent.context, "ID: $companiaId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        //onchange para obtener id de tipo luminaria seleccionada
        tipoLuminariaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedTipoLuminaria = parent.getItemAtPosition(position) as String
                    val tipoLuminariaIdSeleccionado = dbHelper.getTipoLuminariaId(selectedTipoLuminaria)
                    tipoLuminariaId = tipoLuminariaIdSeleccionado

                    getPotencia(tipoLuminariaId)


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
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }



        //onchange para obtener id de compania seleccionada
        tipoFallaSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedTipoFalla = parent.getItemAtPosition(position) as String
                    val tipoFallaIdSeleccionado = dbHelper.getTiposFallaId(selectedTipoFalla)
                    tipoFallaId = tipoFallaIdSeleccionado


                    // Mostrar Toast con el ID de la compañía seleccionada
                    //Toast.makeText(parent.context, "ID: $companiaId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


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



        //onchange para potencia promedio y obtener el consumo mensual
        potenciaPromedioSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {


                    val selectedPotencia = parent.getItemAtPosition(position) as String

                    val potenciaIdSeleccionado = dbHelper.getPotenciaId(selectedPotencia, tipoLuminariaId)

                     potenciaPromedioId = potenciaIdSeleccionado

                    val consumoSeleccionado = dbHelper.getConsumoPromedio(selectedPotencia, tipoLuminariaId)
                    //Toast.makeText(requireContext(), "Por favor, verificar posicion:  $potenciaPromedioId ,  consumo $consumoSeleccionado", Toast.LENGTH_SHORT).show()

                    //dejarmos en blanco la editPotenciaNominal que es donde digitamos la potencia en las led
                    editPotenciaNominal.setText("")

                    //mostramos el consumo mensual calculado
                    editConsumoMensual.setText(consumoSeleccionado)


                }
                else{
                    editConsumoMensual.setText("")
                    potenciaPromedioId = 0
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
            val potenciaNominal = editPotenciaNominal.text.toString().toIntOrNull() ?: 0
            val consumoMensual = editConsumoMensual.text.toString().toDoubleOrNull() ?: 0.0
            val lamparaCondicion = if (switchCondicion.isChecked) 1 else 0
            val observacion = editObservacion.text.toString()
            val latitud = editLatitude.text.toString()
            val longitud = editLongitude.text.toString()


            val dbHelper = DatabaseHelper(requireContext())

            val newRowId = dbHelper.insertCenso(
                tipoLuminariaId,
                potenciaNominal,
                consumoMensual,
                distritoId,
                usuarioId,
                latitud,
                longitud,
                usuarioId,
                direccion,
                observacion,
                tipoFallaId,
                lamparaCondicion,
                companiaId
            )

            // Verificar si la inserción fue exitosa
            if (newRowId != -1L) {
                Toast.makeText(requireContext(), "Registro guardado correctamente", Toast.LENGTH_SHORT).show()
                val action = CensoIngresoOfflineFragmentDirections.actionCensoIngresoOfflineFragmentToCensoOfflineFragment()
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


    fun getPotencia(id: Int) {
        val dbHelper = DatabaseHelper(requireContext())
        // Llama a getTiposFalla
        val potencias = dbHelper.getPotencias(id)

        if (potencias.isNotEmpty()) {
            // Agrega el elemento inicial "SELECCIONE" al principio de la lista
            val tiposFallaActualizados = listOf("SELECCIONE") + potencias

            // Crea el adaptador con la lista actualizada
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposFallaActualizados)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Asigna el adaptador al Spinner
            potenciaPromedioSpinner?.adapter = adapter
        } else {
            Toast.makeText(requireContext(), "No se encontraron potencias.", Toast.LENGTH_SHORT).show()
        }

    }


}