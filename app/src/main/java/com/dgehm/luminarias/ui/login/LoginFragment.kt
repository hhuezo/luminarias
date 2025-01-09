package com.dgehm.luminarias.ui.login

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.dgehm.luminarias.GlobalUbicacion
import com.dgehm.luminarias.HttpClient
import com.dgehm.luminarias.R
import com.dgehm.luminarias.databinding.FragmentLoginBinding
import com.dgehm.luminarias.model.ResponseLogin
import com.dgehm.luminarias.ui.reporte_falla.ReporteFallaIngresoFragmentDirections
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val client by lazy { HttpClient(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Cambiar el título de la ActionBar
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Login"

        // Inicializar los elementos
        val editTextEmail = binding.editTextEmail
        val editTextPassword = binding.editTextPassword
        val buttonLogin = binding.buttonLogin
        val buttonLogout = binding.buttonLogout
        val buttonSincronizar = binding.buttonSincronizar



        val cardLogin = binding.cardLogin
        val cardHome = binding.cardHome
        val texHome = binding.texHome

        val userName = GlobalUbicacion.usuario
        val userId: Int? = GlobalUbicacion.usuarioId

        val switchOffline = binding.switchOffline


        if (userId != null) {
            if(userId > 0)
            {
                texHome.setText("Bienvenido $userName")
                cardLogin.visibility = View.GONE
                cardHome.visibility = View.VISIBLE
            }
        }

        buttonLogout.setOnClickListener {
            GlobalUbicacion.usuarioId = 0
            GlobalUbicacion.usuario = ""

            texHome.setText("Bienvenido $userName")


            cardLogin.visibility = View.VISIBLE
            cardHome.visibility = View.GONE
        }

        buttonSincronizar.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSincronizarFragment()
            findNavController().navigate(action)
        }



        switchOffline.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                GlobalUbicacion.desconectado = 1

                val toast = Toast.makeText(requireContext(), "Modo offline activado", Toast.LENGTH_LONG)
                toast.show()
            } else {
                GlobalUbicacion.desconectado = 0

                val toast = Toast.makeText(requireContext(), "Modo offline desactivado", Toast.LENGTH_LONG)
                toast.show()
            }
        }



        // Listener para el botón de login
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Validar que los campos no estén vacíos
            if (email.isEmpty()) {
                showErrorDialog("El correo no puede estar vacío.")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showErrorDialog("Ingrese un correo válido.")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showErrorDialog("La contraseña no puede estar vacía.")
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }.toString()

            // Realizar la petición POST
            val endpoint = "/login"
            client.post(endpoint, json, object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    val dialog = MaterialDialog(requireContext()).show {
                        title(text = "Error")
                        message(text = "Error al hacer la petición: ${e.message}")
                        icon(R.drawable.baseline_error_24)
                        positiveButton(text = "Aceptar")
                    }

                    // Cerrar el diálogo después de 2 segundos
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                    }, 2000)

                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    Log.d("Response", responseData ?: "No se recibió ninguna respuesta")

                    requireActivity().runOnUiThread {
                        if (response.isSuccessful) {
                            val dialog = MaterialDialog(requireContext()).show {
                                title(text = "Ok")
                                message(text = "Bienvenido")
                                icon(R.drawable.baseline_check_circle_24)
                                positiveButton(text = "Aceptar")
                            }

                            // Cerrar el diálogo después de 2 segundos
                            Handler(Looper.getMainLooper()).postDelayed({
                                dialog.dismiss()
                            }, 2000)

                            val apiResponse = Gson().fromJson(responseData, ResponseLogin::class.java)

                            // Acceder a los valores
                            val userId = apiResponse.user.id
                            val userName = apiResponse.user.name

                            GlobalUbicacion.usuarioId = userId
                            GlobalUbicacion.usuario = userName


                            // Guardar en SharedPreferences
                            val sharedPreferences = requireContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putInt("usuarioId", userId)  // Guardar usuarioId
                            editor.putString("usuario", userName)  // Guardar usuario
                            editor.apply()  // Aplicar los cambios




                            texHome.setText("Bienvenido $userName")


                            cardLogin.visibility = View.GONE
                            cardHome.visibility = View.VISIBLE
                            //redicreccion al reporte de falla


                        } else {
                            val dialog = MaterialDialog(requireContext()).show {
                                title(text = "Error")
                                message(text = "Credenciales incorrectas")
                                icon(R.drawable.baseline_error_24)
                                positiveButton(text = "Aceptar")
                            }

                            // Cerrar el diálogo después de 2 segundos
                            Handler(Looper.getMainLooper()).postDelayed({
                                dialog.dismiss()
                            }, 2000)
                        }
                    }
                }
            })

        }

        return binding.root
    }

    private fun showErrorDialog(message: String) {
        val dialog = MaterialDialog(requireContext()).show {
            title(text = "Error")
            message(text = message)
            icon(R.drawable.baseline_error_24)
            positiveButton(text = "Aceptar")
        }

        // Cerrar el diálogo después de 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
