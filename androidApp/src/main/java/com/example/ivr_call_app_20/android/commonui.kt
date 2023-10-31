package com.example.ivr_calling_app.android

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.ivr_call_app_20.android.bottomSheet
import viewmodel



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun commonui() {



    var context = LocalContext.current

    var mystate by remember {
        mutableStateOf(0)
    }
    Crossfade(targetState = mystate,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    ) { state ->
        when (state)
        {
            0 -> {
                splash(){
                    mystate = 1
                }
            }
            1-> {

                bottomSheet()
            }
        }
    }

}