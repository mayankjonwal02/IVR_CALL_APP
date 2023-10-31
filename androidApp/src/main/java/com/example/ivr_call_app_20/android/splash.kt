package com.example.ivr_calling_app.android

import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ivr_call_app_20.android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Preview
@Composable
fun splash(myfunction: () -> Unit = {}) {

    var context = LocalContext.current

    var iconsize = remember {
        androidx.compose.animation.core.Animatable(0f)
    }


    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White), contentAlignment = Alignment.Center)
    {
        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.outerplus), contentDescription ="",Modifier.size(iconsize.value.dp), tint = Color.Green )
    }

    LaunchedEffect(Unit)
    {
        iconsize.animateTo(targetValue = 200f, animationSpec = tween(
            durationMillis = 2000,
            delayMillis = 1000,
            easing = FastOutSlowInEasing
        ))



            myfunction()



    }


}