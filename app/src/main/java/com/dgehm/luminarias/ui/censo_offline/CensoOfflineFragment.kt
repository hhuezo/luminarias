package com.dgehm.luminarias.ui.censo_offline

import DatabaseHelper
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentCensoOfflineBinding
import com.dgehm.luminarias.ui.reporte_falla_offline.ReporteFallaOfflineFragmentDirections
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



        val fab: FloatingActionButton = view.findViewById(R.id.fab)

        binding.fab.setOnClickListener {
            //redicreccion al reporte de falla
            val action = CensoOfflineFragmentDirections.actionCensoOfflineFragmentToCensoIngresoOfflineFragment()
            findNavController().navigate(action)
        }


    }

}