package com.dgehm.luminarias.ui.censo

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.GlobalUbicacion
import com.dgehm.luminarias.HttpClient
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentCensoBinding
import com.dgehm.luminarias.model.CensoAdapter
import com.dgehm.luminarias.model.ReporteFallaAdapter
import com.dgehm.luminarias.model.ResponseCensoIndex
import com.dgehm.luminarias.model.ResponseReporteFallaIndex
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class CensoFragment : Fragment() , CensoAdapter.OnCensoClickListener{

    private var _binding: FragmentCensoBinding? = null
    private val binding get() = _binding!!
    private val client by lazy { HttpClient(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCensoBinding.inflate(inflater, container, false)
        return binding.root
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Censo luminaria"

        // Inicializar los valores de ubicacion
        GlobalUbicacion.latitud = null
        GlobalUbicacion.longitud = null
        GlobalUbicacion.departamentoId = 0
        GlobalUbicacion.distritoId = 0
        GlobalUbicacion.municipioId = 0
        GlobalUbicacion.direccion = ""

        val sharedPreferences = requireContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getInt("usuarioId", -1)
        Log.e("usuarioId", "usuarioId: $usuarioId")

        val loadingProgressBar: ProgressBar = binding.loadingProgressBar
        loadingProgressBar.visibility = View.VISIBLE

        val fechasText: TextView? = binding.fechasText

        // Obtener la fecha actual
        val currentDate = LocalDate.now()

        // Formatear la fecha en el formato deseado (por ejemplo, "dd/MM/yyyy")
        val fechaFormateada = currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        // La fecha del primer y último día del mes serán iguales, ya que se ajustan a la fecha actual
        val primerDiaFormateado = fechaFormateada
        val ultimoDiaFormateado = fechaFormateada


        fechasText?.setText(primerDiaFormateado + " - " + ultimoDiaFormateado)





        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        if (usuarioId > 0)
        {
            fab.visibility = View.VISIBLE
        }

        binding.fab.setOnClickListener {

            //redicreccion al mapa
            val action = CensoFragmentDirections.actionCensoFragmentToCensoMapaFragment()
            findNavController().navigate(action)

        }



        val reporteRecyclerView: RecyclerView? = binding.recyclerView
        reporteRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        client.get("/api_censo_luminaria?usuario_id=$usuarioId", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Fallo al obtener los datos: ${e.message}")
                requireActivity().runOnUiThread {
                    loadingProgressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Fallo al obtener los datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val gson = Gson()
                        val censosResponse = gson.fromJson(responseData, ResponseCensoIndex::class.java)
                        val censos = censosResponse?.data ?: emptyList()

                        requireActivity().runOnUiThread {
                            reporteRecyclerView?.adapter = CensoAdapter(censos, this@CensoFragment) // Aquí pasas el listener
                            loadingProgressBar.visibility = View.GONE
                        }
                    } else {
                        Log.e("API_ERROR", "El cuerpo de la respuesta es nulo.")
                        requireActivity().runOnUiThread {
                            loadingProgressBar.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Error procesando la respuesta: ${e.message}")
                    requireActivity().runOnUiThread {
                        loadingProgressBar.visibility = View.GONE
                    }
                }
            }
        })





    }



    override fun onResume() {
        super.onResume()
        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Censo luminaria"
    }

    override fun onCensoClick(id: Int) {
        TODO("Not yet implemented")
    }

}


