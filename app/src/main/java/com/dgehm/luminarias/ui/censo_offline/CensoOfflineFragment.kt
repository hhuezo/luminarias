package com.dgehm.luminarias.ui.censo_offline

import DatabaseHelper
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentCensoOfflineBinding
import com.dgehm.luminarias.model.CensoOfflineAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CensoOfflineFragment : Fragment() {

    private var _binding: FragmentCensoOfflineBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCensoOfflineBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Censo luminaria"

        val sharedPreferences = requireContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getInt("usuarioId", -1)

          dbHelper = DatabaseHelper(requireContext())

        if (dbHelper.databaseExists()) {



            // Obtener los reportes de falla
            val censos = dbHelper.getLIstarCensos()

            // Mostrar los reportes (ejemplo: Log, RecyclerView, etc.)
            for (censo in censos) {
                Log.d(
                    "Censo",
                    "censo ID: ${censo}"
                )
            }


            // Configurar el RecyclerView
            val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = CensoOfflineAdapter(censos)


            val fab: FloatingActionButton = view.findViewById(R.id.fab)
            if (usuarioId > 0)
            {
                fab.visibility = View.VISIBLE
            }

            binding.fab.setOnClickListener {
                //redicreccion al reporte de falla
                val action =
                    CensoOfflineFragmentDirections.actionCensoOfflineFragmentToCensoIngresoOfflineFragment()
                findNavController().navigate(action)
            }


        }


    }

}