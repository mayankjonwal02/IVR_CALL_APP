package com.example.ivr_call_app_20.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.ivr_audio_app_20.android.permission.getPermissions
import com.example.ivr_call_app_20.Greeting
import com.example.ivr_call_app_20.android.Bluetooth.BluetoothViewModel
import com.example.ivr_call_app_20.android.Bluetooth.demo
import com.example.ivr_calling_app.android.commonui
import com.example.ivr_calling_app.android.getsharedpreferences
import com.example.ivr_calling_app.android.ipkey
import com.google.firebase.database.FirebaseDatabase
import viewmodel

lateinit var mybluetooth : BluetoothViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var context = LocalContext.current
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        getPermissions()
                    }
                    mybluetooth   = BluetoothViewModel()
                    mybluetooth.initialiseBluetooth(context)


                    var sharedPreferences = getsharedpreferences(context)
                    sharedPreferences.edit().putInt("callstate",0).apply()
                    try{ var viewmodel = viewmodel(context) } catch (e:Exception){
                        sharedPreferences.edit().putString(ipkey,"0.0.0.0").apply()
                    }

                    commonui()

                }
            }
        }
    }
}

@Composable
fun GreetingView(text: String) {
    Text(text = text)
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}
