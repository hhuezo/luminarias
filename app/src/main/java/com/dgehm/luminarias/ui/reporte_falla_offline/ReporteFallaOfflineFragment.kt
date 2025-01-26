package com.dgehm.luminarias.ui.reporte_falla_offline

import DatabaseHelper
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentReporteFallaOfflineBinding
import com.dgehm.luminarias.model.ReporteFallaOfflineAdapter
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaFragmentDirections
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaIngresoFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ReporteFallaOfflineFragment : Fragment() {

    private var _binding: FragmentReporteFallaOfflineBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReporteFallaOfflineBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Reporte falla"

        val sharedPreferences = requireContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val desconectado = sharedPreferences.getInt("desconectado", 0) // Valor por defecto -1 si no existe

        //Toast.makeText(requireContext(), "Valor de desconectado: $desconectado", Toast.LENGTH_SHORT).show()
        if(desconectado == 0)
        {
            val action = ReporteFallaOfflineFragmentDirections.actionReporteFallaOfflineFragmentToReporteFallaIngresoOfflineFragment()
            findNavController().navigate(action)
        }

        dbHelper = DatabaseHelper(requireContext())

        if (dbHelper.databaseExists()) {

            // Obtener los reportes de falla
            val reportesFalla = dbHelper.getLIstarReportesFalla()

            // Mostrar los reportes (ejemplo: Log, RecyclerView, etc.)
            for (reporte in reportesFalla) {
                Log.d(
                    "ReporteFalla",
                    "Reporte ID: ${reporte.id}, Descripción: ${reporte.descripcion}"
                )
            }


            // Configurar el RecyclerView
            val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = ReporteFallaOfflineAdapter(reportesFalla)


            val fab: FloatingActionButton = view.findViewById(R.id.fab)

            binding.fab.setOnClickListener {
                //redicreccion al reporte de falla
                val action =
                    ReporteFallaOfflineFragmentDirections.actionReporteFallaOfflineFragmentToReporteFallaIngresoOfflineFragment()
                findNavController().navigate(action)
            }
        }
        else{
            Toast.makeText(context, "No se ha encontrado la base de datos", Toast.LENGTH_LONG).show()
        }

    }


    override fun onResume() {
        super.onResume()
        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Reporte de falla"
    }

}