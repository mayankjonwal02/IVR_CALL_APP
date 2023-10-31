import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ivr_calling_app.android.*
import com.google.gson.JsonObject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class viewmodel(context : Context) : ViewModel() {
    var apiService: ApiService

    var sharedPreferences = getsharedpreferences(context)
    var ipaddress = sharedPreferences.getString(ipkey,"0.0.0.0")
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
            .baseUrl("https://$ipaddress/IVRapi/$mydepartment/")
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

    suspend fun connect(): String {
        return withContext(Dispatchers.IO) {
            apiService.getconnection().string()
        }
    }

    suspend fun data() : List<patientdata>
    {
        return withContext(Dispatchers.IO){
            try {
                apiService.getdata()
            }
            catch (e:Exception)
            {
                emptyList<patientdata>()
            }
        }

    }

    suspend fun databyid(calledon : String) : List<patientdata>
    {
        return withContext(Dispatchers.IO)
        {
          try {
              apiService.getdatabyid(calledon)
          }
          catch (e:Exception)
          {
              emptyList<patientdata>()
          }
        }

    }


    suspend fun send(patientdata: patientdata) : String
    {
       try {

           var responce = apiService.senddata(patientdata.engagementid,patientdata.patientid,patientdata.patientname,patientdata.patientcno,patientdata.operationtype,patientdata.language,patientdata.duedate,"","","","","","","","")
           return responce.string()
       }
       catch (e:Exception)
       {
           return e.message.toString()
       }

    }

    suspend fun delete(engagementid: String) : String
    {
        try {
            var response = apiService.deletedata(engagementid)
            return response.string()
        }
        catch (e:Exception)
        {
            return e.message.toString()
        }
    }


    suspend fun update(fieldToUpdate: String, engagementid: String, newvalue: String): String {
        var response: String = ""
        try {
            val data = JsonObject().apply {
                addProperty("engagementid", engagementid)
                addProperty(fieldToUpdate, newvalue)
            }
            val call = apiService.updatefield(fieldToUpdate, data)
            val retrofitResponse: Response<ResponseBody?> = (call?.execute() ?: null) as Response<ResponseBody?>

            if (retrofitResponse.isSuccessful) {
                val responseBody: ResponseBody? = retrofitResponse.body()
                response = responseBody?.string() ?: ""
            } else {
                response = retrofitResponse.errorBody()?.string() ?: "Request failed"
            }
        } catch (e: Exception) {
            response = e.message.toString()
        }
        return response
    }


}



class sharedviewmodel : ViewModel()
{
    var contactno = MutableLiveData<String>()
}

