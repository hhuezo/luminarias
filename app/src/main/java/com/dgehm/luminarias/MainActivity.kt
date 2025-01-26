package com.dgehm.luminarias

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dgehm.luminarias.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Obtener el valor de la preferencia "desconectado"
        val sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val desconectado = sharedPreferences.getInt("desconectado", 0)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Configurar los destinos de navegación según el valor de la preferencia "desconectado"
        val appBarConfiguration = if (desconectado == 1) {
            // Modo offline activado, cambiar censoFragment por censoOfflineFragment
            AppBarConfiguration(setOf(
                R.id.reporteFallaOfflineFragment, R.id.censoOfflineFragment, R.id.loginFragment
            ))
        } else {
            // Modo online activado
            AppBarConfiguration(setOf(
                R.id.reporteFallaFragment, R.id.censoFragment, R.id.loginFragment
            ))
        }



        // Configurar la barra de acción con el navController y la nueva configuración
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Cambiar el fragmento inicial en función de la preferencia "desconectado"
        val initialFragment = if (desconectado == 1) {
            R.id.reporteFallaOfflineFragment
        } else {
            R.id.reporteFallaFragment
        }

        // Establecer el fragmento inicial
        navController.navigate(initialFragment)

        // Si se cambia el valor de "desconectado", actualizar los fragmentos de la barra de navegación

        // Si se cambia el valor de "desconectado", actualizar los fragmentos de la barra de navegación
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.reporteFallaFragment, R.id.reporteFallaOfflineFragment -> {
                    navController.navigate(item.itemId)
                    true
                }
                R.id.censoFragment, R.id.censoOfflineFragment -> {
                    val censoFragment = if (desconectado == 1) {
                        R.id.censoOfflineFragment
                    } else {
                        R.id.censoFragment
                    }
                    navController.navigate(censoFragment)
                    true
                }
                R.id.loginFragment -> {
                    navController.navigate(R.id.loginFragment)  // Navegar a loginFragment
                    true
                }
                else -> false
            }
        }



    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
