package com.dgehm.luminarias.ui.censo

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.dgehm.luminarias.GlobalUbicacion
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentCensoBinding
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CensoFragment : Fragment() {

    private var _binding: FragmentCensoBinding? = null
    private val binding get() = _binding!!

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

        val usuarioId: Int? = GlobalUbicacion.usuarioId


        val fab: FloatingActionButton = view.findViewById(R.id.fab)

        binding.fab.setOnClickListener {

            //redicreccion al mapa
            val action = CensoFragmentDirections.actionCensoFragmentToCensoMapaFragment()
            findNavController().navigate(action)

        }



    }



    override fun onResume() {
        super.onResume()
        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Censo luminaria"
    }

}


