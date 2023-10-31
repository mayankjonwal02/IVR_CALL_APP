package com.example.ivr_calling_app.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ivr_call_app_20.android.DBviewmodel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import viewmodel

const val ipkey = "ipaddr"
const val department = "mydatabase"


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ipscreen() {

    var context = LocalContext.current
    var sharedPreferences =  remember {
        getsharedpreferences(context)
    }
    var ipaddress by remember {
        mutableStateOf(sharedPreferences.getString(ipkey,"0.0.0.0"))
    }

    var mydepartment by remember {
        mutableStateOf(sharedPreferences.getString(department,"No Department Found"))
    }

    var proceedkey by remember {
        mutableStateOf(false)
    }




    var status by remember {
        mutableStateOf("")
    }

    var viewmodel by remember {
            mutableStateOf(viewmodel(context))
        }


    var myscreen by remember {
        mutableStateOf(0)
    }




    Crossfade(targetState = myscreen,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    ) {
        mystate ->
        when(mystate)
        {
            0 -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White), contentAlignment = Alignment.TopCenter)
                {

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color.White).verticalScroll(
                        rememberScrollState())){
                        Text(text = "IVR Call App", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, fontStyle = FontStyle.Normal, fontFamily = FontFamily.Default, modifier = Modifier.padding(top = 50.dp))
                        Spacer(modifier = Modifier.height(90.dp))
                        ipaddress?.let {
                            OutlinedTextField(
                                value = it,
                                onValueChange = { ipaddress = it },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Green,
                                    textColor = Color.Black
                                ),
                                label = { Text(text = "IP Address")},
                                placeholder = { Text(text = "Enter IP Address")}
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))


                        var dblist = remember {
                            mutableStateOf(emptyList<String>())
                        }
                        var isexpanded by remember { mutableStateOf(false) }
                        Column(){
                            OutlinedTextField(modifier = Modifier.onFocusChanged
                            { focusState ->
                                isexpanded = focusState.isFocused

                            },
                                value = mydepartment.toString(),
                                onValueChange = { mydepartment = it },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color.Green,
                                    textColor = Color.Black
                                ),
                                label = { Text(text = "Department Name") },
                                placeholder = { Text(text = "Enter Department Name") })

                            if (isexpanded) {
                                ipaddress?.let { getmydbs(context, dblist, it) }
                                DropdownMenu(modifier = Modifier.height(200.dp),
                                    expanded = isexpanded,
                                    onDismissRequest = { isexpanded = false }) {
                                    dblist.value.forEach { item ->
                                        DropdownMenuItem(onClick = {
                                            mydepartment = item
                                            isexpanded = false
                                        }) {
                                            Text(text = item)
                                        }
                                    }
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Button(onClick = {
                            sharedPreferences.edit().putString(ipkey,ipaddress).apply()
                            sharedPreferences.edit().putString(department,mydepartment).apply()
                            CoroutineScope(Dispatchers.IO).launch{
                                viewmodel = viewmodel(context)
                                try{
                                    status = viewmodel.connect()
                                    proceedkey = true
                                }
                                catch (e:Exception)
                                {
                                    status = e.message.toString()
                                    proceedkey = false
                                }}
                        } , colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)) {
                            Text(text = "Connect" , color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(70.dp))
                        Text(text = status)
                        Spacer(modifier = Modifier.height(90.dp))
                        if(proceedkey){
                            Button(
                                onClick = { myscreen = 1 },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Text(text = "Proceed",color = Color.Black)
                            }
                        }
                    } }

            }

            1 -> {
                UI()

            }


        }

    }

}

fun getmydbs(context: Context, dblist: MutableState<List<String>>, ipaddress: String){
    var dbviewmodel = DBviewmodel(context,ipaddress)

    CoroutineScope(Dispatchers.IO).launch{
        dblist.value = dbviewmodel.getdbs()

//        withContext(Dispatchers.Main) { Toast.makeText(context,dblist.value.toString(),Toast.LENGTH_SHORT).show()  }


    }


}


fun getsharedpreferences(context: Context) : SharedPreferences
{
    return context.getSharedPreferences("my_shared_prefs",Context.MODE_PRIVATE)
}