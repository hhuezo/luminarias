package com.dgehm.luminarias.ui.censo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.dgehm.luminarias.GlobalUbicacion
import com.dgehm.luminarias.HttpClient
import com.dgehm.luminarias.R
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaMapaFragment
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaMapaFragment.Companion
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaMapaFragmentDirections
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CensoMapaFragment : Fragment() , OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var btnConfirmarUbicacion: Button
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null // Guardar referencia al marcador actual
    private lateinit var currentLatLng: LatLng // Guardar las coordenadas actuales

    private var departamentoId: String? = ""
    private var distritoId: String? = ""
    private var municipioId: String? = ""


    private var  departamentoNombre: String? = ""
    private var  distritoNombre: String? = ""
    private var  direccion: String? = ""

    private val clientGet by lazy { HttpClient(requireContext()) }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        val rootView = inflater.inflate(R.layout.fragment_censo_mapa, container, false)

        mapView = rootView.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        loadingProgressBar = rootView.findViewById(R.id.loadingProgressBar)

        loadingProgressBar.visibility = View.VISIBLE



        btnConfirmarUbicacion = rootView.findViewById<Button>(R.id.btnConfirmarUbicacion)
        btnConfirmarUbicacion.visibility = View.GONE

        btnConfirmarUbicacion.setOnClickListener {

            GlobalUbicacion.latitud = currentLatLng.latitude.toFloat()
            GlobalUbicacion.longitud = currentLatLng.longitude.toFloat()

            GlobalScope.launch(Dispatchers.Main) {
                // Primero obtener la ubicación
                val ubicacion = obtenerUbicacion()

                // Ahora que la ubicación fue obtenida, obtener el departamento
                if (departamentoNombre != null) {
                    val departamentoResult = getDepartamento(departamentoNombre!!)
                    if (departamentoResult != null) {
                        departamentoId = departamentoResult.toString()
                    } else {
                        Log.e("Departamento", "No se pudo obtener el departamento")
                    }
                }




                if (distritoNombre != null) {
                    val distritoResult = getDistrito(distritoNombre!!)
                    if (distritoResult != null) {
                        distritoId = distritoResult.toString()
                        // Log.d("Distrito", "Distrito $distritoId")

                        if (distritoId != null) { // Verifica si distritoId no está vacío
                            val municipioResult = getMunicipio(distritoId!!.toIntOrNull() ?: 0)
                            if (municipioResult != null) {
                                municipioId = municipioResult.toString()
                                Log.d("Municipio", "Municipio $municipioId")
                            } else {
                                Log.e("Municipio", "No se pudo obtener el municipio")
                            }
                        }

                    } else {
                        Log.e("Distrito", "No se pudo obtener el distrito")
                    }
                }

                GlobalUbicacion.departamentoId = departamentoId?.toIntOrNull() ?: 0
                GlobalUbicacion.distritoId = distritoId?.toIntOrNull() ?: 0
                GlobalUbicacion.municipioId = municipioId?.toIntOrNull() ?: 0
                GlobalUbicacion.direccion = direccion

                Log.d("ubicacion ","departamen $departamentoNombre id: $departamentoId , distrito $distritoNombre id: $distritoId , id municipio $municipioId")

                navigateToReporteFallaIngresoFragment()

            }


        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Censo luminaria"
            setDisplayHomeAsUpEnabled(true) // Muestra el botón de retroceso
        }

        // Manejar la acción del botón de retroceso
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            parentFragmentManager.popBackStack()
        }
    }


    private suspend fun obtenerUbicacion(): String? {

        requireActivity().runOnUiThread {
            btnConfirmarUbicacion.isEnabled = false
        }

        // Crear el endpoint con los parámetros de latitud y longitud
        val endpoint = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${currentLatLng.latitude.toFloat()}&lon=${currentLatLng.longitude.toFloat()}"

        // Crear el cliente OkHttp
        val client = OkHttpClient()

        // Crear la solicitud HTTP
        val request = Request.Builder()
            .url(endpoint)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                // Ejecutar la solicitud de manera asincrónica con espera
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    Log.d("aa","response "+responseBody)

                    // Intentar convertir la respuesta JSON en un JSONObject
                    val jsonObject = JSONObject(responseBody)

                    // Extraer los valores del estado (departamento) y municipio
                    val address = jsonObject.optJSONObject("address")



                    val departamento = address?.optString("state")
                    val distrito = address?.optString("town")
                        ?: address?.optString("city")
                        ?: address?.optString("village")
                        ?: address?.optString("county")

                    // Asignar los valores a las variables correspondientes
                    departamentoNombre = departamento
                    distritoNombre = (distrito?.toString() ?: "").trim()


                    // Extraer el valor de "display_name"
                    val displayName = jsonObject.getString("display_name")

                    direccion = displayName


                    return@withContext "Departamento: $departamento, Municipio:  $distrito"
                } else {
                    requireActivity().runOnUiThread {
                        btnConfirmarUbicacion.isEnabled = true
                    }
                    return@withContext null
                }
            } catch (e: IOException) {
                Log.e("API Error", "Failed to fetch location data", e)
                requireActivity().runOnUiThread {
                    btnConfirmarUbicacion.isEnabled = true
                }
                return@withContext null
            }
        }
    }


    private suspend fun getDepartamento(nombre: String): String? {
        return suspendCoroutine { continuation ->
            try {
                Log.e("API Error", "obteniendo dep $nombre")
                // Realizamos la solicitud HTTP
                clientGet.get("/api_get_departamento_id/$nombre", object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        // En caso de error, continuamos con una excepción
                        Log.e("API Error", "Error en onFailure: ${e.message}", e)
                        continuation.resumeWithException(IOException("Fallo al obtener los datos"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val responseData = response.body?.string()
                            if (responseData != null) {
                                try {
                                    // Procesamos la respuesta
                                    val jsonObject = JSONObject(responseData)

                                    // Accedemos al departamentoId como un número entero
                                    val departamentoId = jsonObject.optString("departamentoId", "0")

                                    // Continuamos la corutina con el valor de departamentoId
                                    continuation.resume(departamentoId)
                                } catch (e: JsonSyntaxException) {
                                    Log.e("API Error", "Error al parsear JSON: ${e.message}", e)
                                    // Si hay un error al parsear JSON, reanudamos la corutina con una excepción
                                    continuation.resumeWithException(e)
                                }
                            } else {
                                // Si no hay respuesta, reanudamos con una excepción
                                Log.e("API Error", "No hay datos en la respuesta")
                                continuation.resumeWithException(IOException("No hay datos: ${response.message}"))
                            }
                        } catch (e: Exception) {
                            // Captura errores generales en onResponse
                            Log.e("API Error", "Error en onResponse: ${e.message}", e)
                            continuation.resumeWithException(e)
                        }
                    }
                })
            } catch (e: Exception) {
                // En caso de excepción general, reanudamos con la excepción
                Log.e("API Error", "Error durante la solicitud: ${e.message}", e)
                continuation.resumeWithException(e)
            }
        }
    }



    private suspend fun getMunicipio(id: Int): Int {
        return suspendCoroutine { continuation ->
            try {
                Log.e("API Error", "obteniendo distrito $id")

                // Realizamos la solicitud HTTP
                clientGet.get("/api_get_municipio_id/$id", object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        // En caso de fallo en la solicitud, reanudamos con el valor predeterminado 0
                        continuation.resume(0)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val responseData = response.body?.string()

                            // Si hay respuesta, la procesamos
                            if (responseData != null) {
                                val jsonObject = JSONObject(responseData)

                                // Log para ver la respuesta completa de la API
                                //Log.e("response municipio", "Response: $municipioId")

                                // Accedemos al distritoId como un número entero, si no existe, retornamos 0
                                municipioId = jsonObject.optInt("municipioId", 0).toString()

                                var municipioIdInt = jsonObject.optInt("municipioId", 0)

                                // Continuamos la corutina con el valor de distritoId
                                continuation.resume(municipioIdInt)

                            } else {
                                // Si no hay datos, reanudamos con el valor predeterminado 0
                                continuation.resume(0)
                            }

                        } catch (e: JsonSyntaxException) {
                            Log.e("JSON_PARSE_ERROR", "Error al parsear JSON: ${e.message}")
                            // Si hay error al parsear, reanudamos con el valor predeterminado 0
                            continuation.resume(0)
                        } catch (e: Exception) {
                            Log.e("API Error", "Error al procesar la respuesta: ${e.message}")
                            // Si hay un error al procesar la respuesta, reanudamos con el valor predeterminado 0
                            continuation.resume(0)
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("API Error", "Error durante la solicitud: ${e.message}", e)
                // Si ocurre un error general en el bloque try, reanudamos con el valor predeterminado 0
                continuation.resume(0)
            }
        }
    }


    private suspend fun getDistrito(nombre: String): Int {
        return suspendCoroutine { continuation ->
            try {
                Log.e("API Error", "obteniendo distrito $nombre")

                // Realizamos la solicitud HTTP
                clientGet.get("/api_get_distrito_id/$nombre", object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                        // En caso de fallo en la solicitud, reanudamos con el valor predeterminado 0
                        continuation.resume(0)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            val responseData = response.body?.string()

                            // Si hay respuesta, la procesamos
                            if (responseData != null) {
                                val jsonObject = JSONObject(responseData)

                                // Log para ver la respuesta completa de la API
                                //Log.e("response", "Response: $jsonObject")

                                // Accedemos al distritoId como un número entero, si no existe, retornamos 0
                                val distritoId = jsonObject.optInt("distritoId", 0)

                                // Continuamos la corutina con el valor de distritoId
                                continuation.resume(distritoId)

                            } else {
                                // Si no hay datos, reanudamos con el valor predeterminado 0
                                continuation.resume(0)
                            }

                        } catch (e: JsonSyntaxException) {
                            Log.e("JSON_PARSE_ERROR", "Error al parsear JSON: ${e.message}")
                            // Si hay error al parsear, reanudamos con el valor predeterminado 0
                            continuation.resume(0)
                        } catch (e: Exception) {
                            Log.e("API Error", "Error al procesar la respuesta: ${e.message}")
                            // Si hay un error al procesar la respuesta, reanudamos con el valor predeterminado 0
                            continuation.resume(0)
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("API Error", "Error durante la solicitud: ${e.message}", e)
                // Si ocurre un error general en el bloque try, reanudamos con el valor predeterminado 0
                continuation.resume(0)
            }
        }
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                CensoMapaFragment.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    currentLatLng = currentLocation // Guarda las coordenadas iniciales
                    addOrMoveMarker(currentLocation, "Mi Ubicación")
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))

                    mapView.visibility = View.VISIBLE
                    btnConfirmarUbicacion.visibility = View.VISIBLE

                } else {
                    promptEnableLocation()
                }
            }
        }

        // Configurar listener para clics en el mapa
        mMap.setOnMapClickListener { latLng ->
            currentLatLng = latLng // Actualiza las coordenadas seleccionadas
            addOrMoveMarker(latLng, "Nueva Ubicación")
        }
    }


    private fun addOrMoveMarker(latLng: LatLng, title: String) {
        if (currentMarker == null) {
            // Si no hay marcador, crea uno
            currentMarker = mMap.addMarker(MarkerOptions().position(latLng).title(title))
        } else {
            // Si ya existe un marcador, cámbialo de posición
            currentMarker?.position = latLng
            currentMarker?.title = title
        }

        val currentZoom = mMap.cameraPosition.zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentZoom))

        currentLatLng = latLng // Guarda las coordenadas seleccionadas
    }


    private fun promptEnableLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == com.dgehm.luminarias.ui.censo.CensoMapaFragment.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    private fun navigateToReporteFallaIngresoFragment() {
        val action = CensoMapaFragmentDirections.actionCensoMapaFragmentToCensoIngresoFragment()
        findNavController().navigate(action)
    }

}