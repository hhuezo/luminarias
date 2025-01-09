package com.dgehm.luminarias.ui.login

import DatabaseHelper
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dgehm.luminarias.HttpClient
import com.dgehm.luminarias.databinding.FragmentSincronizarBinding


class SincronizarFragment : Fragment() {

    private var _binding: FragmentSincronizarBinding? = null
    private val binding get() = _binding!!
    private val client by lazy { HttpClient(requireContext()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSincronizarBinding.inflate(inflater, container, false)

        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Sincronizar"


        val btnSincronizar = binding.btnSincronizar

        btnSincronizar.setOnClickListener {
            // Asegúrate de que el contexto no sea nulo
            context?.let { nonNullContext ->
                val dbHelper = DatabaseHelper(nonNullContext)

                // Verificar si la base de datos existe
                if (dbHelper.databaseExists()) {
                    println("Base de datos existente.")
                    // Eliminar la base de datos si existe
                    if (dbHelper.deleteDatabase()) {
                        println("Base de datos existente eliminada correctamente.")
                    } else {
                        println("No se pudo eliminar la base de datos existente.")
                        Toast.makeText(
                            nonNullContext,
                            "Error al eliminar la base de datos.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@let
                    }
                }

                // Copiar la nueva base de datos
                dbHelper.copyDatabase()
                Toast.makeText(nonNullContext, "Base de datos sincronizada exitosamente.", Toast.LENGTH_SHORT).show()
            } ?: run {
                // En caso de que `context` sea nulo, maneja el error aquí
                Toast.makeText(requireContext(), "El contexto es nulo", Toast.LENGTH_SHORT).show()
            }
        }





        return binding.root
    }


}