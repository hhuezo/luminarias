package com.dgehm.luminarias.ui.reporte_falla

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.dgehm.luminarias.R

class ReporteFallaFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = inflater.inflate(R.layout.fragment_reporte_falla, container, false)

        // Obtener la referencia del ImageView
        val imageView: ImageView = binding.findViewById(R.id.imageView)

        // Configurar el OnClickListener
        imageView.setOnClickListener {
            findNavController().navigate(R.id.action_reporteFallaFragment_to_reporteFallaMapaFragment)
        }

        return binding
    }

}