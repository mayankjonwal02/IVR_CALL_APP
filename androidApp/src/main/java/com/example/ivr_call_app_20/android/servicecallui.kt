package com.example.ivr_calling_app.android

import android.app.PendingIntent
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import viewmodel
import java.lang.reflect.Modifier

@Preview
@Composable
fun serviceui() {
    var mytext by remember {
        mutableStateOf("service stopped")
    }
    var context = LocalContext.current
    var permissionlauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = {
        isgranted ->
        if(isgranted)
        {

            Toast.makeText(context, "permission granted", Toast.LENGTH_SHORT).show()
        }
    })
    var myScope = rememberCoroutineScope()


    fun manipulate() {
        permissionlauncher.launch(android.Manifest.permission.CALL_PHONE)
        if (mytext == "service stopped") {

            mytext = "service started"

            var intent = Intent(context, mycallservice::class.java)
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startService(intent)
        } else {
            mytext = "service stopped"
            var intent = Intent(context, mycallservice::class.java)
            context.stopService(intent)

        }
    }
    var viewmodel = viewmodel(context)

    Box(modifier = androidx.compose.ui.Modifier
        .fillMaxSize()
        .background(Color.White), contentAlignment = Alignment.Center)
    {

        Button(onClick = {
            manipulate()


        }) {
            Text(text = "call " + mytext)
        }
    }




}