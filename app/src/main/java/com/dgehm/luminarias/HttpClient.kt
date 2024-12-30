package com.dgehm.luminarias

import android.content.Context
import android.util.Log
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class HttpClient(context: Context) {
    private val client: OkHttpClient
    private val baseUrl = context.getString(R.string.API_HOST
    )
    private val KEY = context.getString(R.string.KEY)

    init {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .build()
    }

    fun get(endpoint: String, callback: Callback) {
        val url = baseUrl + endpoint
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", KEY)
            .build()

        client.newCall(request).enqueue(callback)
    }

    fun post(endpoint: String, json: String, callback: Callback) {
        val url = baseUrl + endpoint
        Log.d("url: ", url ?: "No se recibió ninguna url")

        // Crear el cuerpo de la solicitud
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)

        // Construir la solicitud
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", KEY)
            .build()

        // Log para verificar los encabezados antes de enviar
        Log.d("Request Headers", "Authorization Header: ${request.header("Authorization")}")

        // Realizar la solicitud
        client.newCall(request).enqueue(callback)
    }



    fun put(endpoint: String, json: String, callback: Callback) {
        val url = baseUrl + endpoint
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        client.newCall(request).enqueue(callback)
    }

}