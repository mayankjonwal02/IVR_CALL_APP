package com.example.ivr_call_app_20.android

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.ivr_calling_app.android.ApiService
import com.example.ivr_calling_app.android.department
import com.example.ivr_calling_app.android.getsharedpreferences
//import com.example.ivr_nurse_app.android.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DBviewmodel(context : Context , ipaddress : String) : ViewModel() {
    private lateinit var apiService: ApiService

    var sharedPreferences = getsharedpreferences(context)

    var mydepartment = sharedPreferences.getString(department,"notfound")

    private val trustAllManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustAllManager), null)
    }

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://$ipaddress/IVRapi/")
            .client(
                OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, trustAllManager)
                    .hostnameVerifier { _, _ -> true }
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun getdbs(): List<String> {
        return withContext(Dispatchers.IO) {
            try{ apiService.getalldbs() }
            catch (e:IOException){  listOf<String>("DB not found")
            }
        }
    }



}
