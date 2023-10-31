package com.example.ivr_call_app_20.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import com.example.ivr_calling_app.android.getsharedpreferences
import java.io.IOException

@SuppressLint("MissingPermission")
@Composable
fun connection()
{
    var context = LocalContext.current
    val status by mybluetooth.status.collectAsState()
    val devicelist by mybluetooth.paireddevices.collectAsState()
    var showpopup = remember {
        mutableStateOf(false)
    }
    
    var intentlauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult())
    {
        result ->

        if(result.resultCode == Activity.RESULT_OK)
        {
            mybluetooth.fetchdevices()
        }
        else
        {
            Toast.makeText(context,"Bluetooth Not Enabled",Toast.LENGTH_SHORT).show()
        }
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White), contentAlignment = Alignment.TopCenter)
    {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.White), horizontalAlignment = Alignment.CenterHorizontally)
        {
            OutlinedButton(onClick = {
                fetchdevices(context,intentlauncher)
//                                     mybluetooth.fetchdevices()
                                     },colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Green, contentColor = Color.Black),
                modifier = Modifier.padding(65.dp) )
            {
                Text(text = "Scan Devices", fontSize = 20.sp)
            }

            if(devicelist.size > 0)
            {
                Card(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .height(300.dp),
                    backgroundColor = Color.Green,
                    shape = RoundedCornerShape(20.dp)
                )
                {
                    LazyColumn(modifier = Modifier.padding(20.dp))
                    {
                        items(devicelist)
                        { item ->
                            Card(modifier = Modifier
                                .clickable {
                                    if (item != null) {
                                        try {
                                            mybluetooth
                                                .client(item)
                                                .start()
                                        } catch (e: IOException) {
                                            Toast
                                                .makeText(
                                                    context,
                                                    e.message.toString(),
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                        }
                                    }
                                }
                                .padding(10.dp)
                                .fillMaxWidth()
                                .wrapContentHeight(),
                                backgroundColor = Color.White,
                                contentColor = Color.Black,
                                shape = RoundedCornerShape(20.dp)) {

                                item?.name?.let {
                                    Text(
                                        text = it,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }


            Text(
                text = "Status : ${status}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                textAlign = TextAlign.Center,
                color = Color.Black
            )


        }
    }
    @Composable
    fun mydialog(showpopup: MutableState<Boolean>)
    {
        var sp = getsharedpreferences(context)
        var messagetype by remember {
            mutableStateOf(sp.getString("msg_type","Hey @name@, you have a @operationtype@ operation on @duedate@"))
        }
        Dialog(onDismissRequest = { showpopup.value = !showpopup.value },
            properties = DialogProperties(dismissOnBackPress = true , dismissOnClickOutside = true)
        ) {
            Card(modifier = Modifier
                .wrapContentSize()
                .background(Color.Transparent)
                .shadow(elevation = 0.dp, shape = RoundedCornerShape(20.dp)), shape = RoundedCornerShape(20.dp), backgroundColor = Color.White, border = BorderStroke(2.dp, color = Color.Magenta)
            )
            {
                Column(modifier = Modifier
                    .padding(20.dp)
                    .background(Color.Transparent)
                    .verticalScroll(rememberScrollState())) {
                    Text(text = "Customize Message", color = Color.Black, fontFamily = FontFamily.Default, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontStyle = FontStyle.Normal)
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(value = messagetype.toString() , placeholder = { Text(text = "Set Message")}, label = { Text(
                        text = "Message Prototype"
                    )}, onValueChange = {messagetype = it}, colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.Black, cursorColor = Color.Black, backgroundColor = Color.White, placeholderColor = Color.LightGray, focusedLabelColor = Color.Magenta, focusedBorderColor = Color.Magenta, unfocusedLabelColor = Color.LightGray, unfocusedBorderColor = Color.LightGray))

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Instructions :", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    Text(text = "write @name@ for name", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    Text(text = "write @operationtype@ for operationtype", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    Text(text = "write @duedate@ for date", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(40.dp))
                    Row( horizontalArrangement = Arrangement.SpaceBetween){
                        OutlinedButton(
                            onClick = {
                                sp.edit().putString("msg_type",messagetype).apply()
                                Toast.makeText(context,"Message Prototype Updated",Toast.LENGTH_SHORT).show()
                                showpopup.value = !showpopup.value
                                      },
                            modifier = Modifier
                                .heightIn(min = 48.dp)

                                .weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.Transparent,
                                contentColor = Color.Magenta
                            ),
                            border = BorderStroke(
                                2.dp,
                                Color.Magenta
                            )
                        ) {
                            Text(text = "Set Message")
                        }
                        Spacer(modifier = Modifier.width(20.dp))

                            OutlinedButton(
                                onClick = {
                                    showpopup.value = !showpopup.value
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.Transparent,
                                    contentColor = Color.Magenta
                                ),
                                border = BorderStroke(
                                    2.dp,
                                    Color.Magenta
                                )
                            ) {
                                Text(text = "Dismiss")
                            }



                    }



                }
            }
        }
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Transparent), contentAlignment = Alignment.BottomEnd) {
        OutlinedButton(onClick =  { showpopup.value = !showpopup.value  }, modifier = Modifier.padding(end = 20.dp, bottom = 20.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, backgroundColor = Color.Transparent), border = BorderStroke(3.dp,Color.Black)) {
            Text(text = "Set Message Type")
        }
    }

    if(showpopup.value)
    {
        mydialog(showpopup)
    }
}


fun fetchdevices(
    context: Context,
    intentlauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
)
{
    fun handleintent()
    {
        var intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        intentlauncher.launch(intent)
    }

    if(mybluetooth.bluetoothadapter.isEnabled)
    {
        mybluetooth.fetchdevices()
    }
    else
    {
        handleintent()
    }

//    handleintent()
}