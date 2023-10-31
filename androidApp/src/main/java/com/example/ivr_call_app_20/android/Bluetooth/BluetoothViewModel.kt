package com.example.ivr_call_app_20.android.Bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Message
import android.util.JsonReader
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.ivr_audio_app_20.android.permission.getPermissions
import com.example.ivr_call_app_20.android.connection
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

val uuid = UUID.fromString("ec3c6260-41dd-11ee-be56-0242ac120002")

//        "fcdf1a72-608c-11ee-8c99-0242ac120002"
const val devicename = "IVR"

data class msgupdate(var key: Int , var message: String)


class BluetoothViewModel() : ViewModel() {

    private lateinit var context: Context
    lateinit var shareit : sendreceive
    var bluetoothadapter = BluetoothAdapter.getDefaultAdapter()
    val _paireddevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    var paireddevices : StateFlow<List<BluetoothDevice>> = _paireddevices

   lateinit var device : BluetoothDevice

    val _mymessage = MutableStateFlow<msgupdate>(msgupdate(0,""))
    val mymessage : StateFlow<msgupdate> = _mymessage

    val _patientcategory = MutableStateFlow<String>("Category")
    val patientcategory : StateFlow<String> = _patientcategory

    val _language = MutableStateFlow<String>("Language")
    val language : StateFlow<String> = _language

    val _duedate = MutableStateFlow<String>("00-00-0000")
    val duedate : StateFlow<String> = _duedate

    val _status = MutableStateFlow<String>("Disconnected")
    val status : StateFlow<String> = _status

    val _blestatus = MutableStateFlow<String>("Disconnected")
    val blestatus : StateFlow<String> = _blestatus

    private var STATE_LISTENING = 1
    private var STATE_CONNECTING = 2
    private var STATE_CONNECTED = 3
    private var STATE_CONNECTION_FAILED = 4
    private var STATE_MESSAGE_RECEIVED = 5
    private var STATE_ALREADY_CONNECTED = 6

    fun initialiseBluetooth(context: Context) {
        this.context = context
    }

    fun enableBluetooth() {
        if (!bluetoothadapter.isEnabled) {
            var intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "Permissions Not Granted", Toast.LENGTH_SHORT).show()
                return
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchdevices()
    {
        if(bluetoothadapter != null)
        {

            _paireddevices.value = bluetoothadapter.bondedDevices.toList()
        }
    }

    @SuppressLint("MissingPermission")
    var handler = Handler{
        msg : Message ->
        when(msg.what)
        {
            STATE_CONNECTED ->
            {
                _blestatus.value = "Connected"
                _status.value= "Connected to ${device.name}"
                false
            }
            STATE_CONNECTING ->
            {
                _blestatus.value = "Connecting"
                _status.value= "Connecting..."
                false
            }
            STATE_LISTENING ->
            {
                _blestatus.value = "Listening"
                _status.value= "Listening..."
                false
            }
            STATE_CONNECTION_FAILED ->
            {
                _blestatus.value = "Connection Error"
                _status.value= "Connection Error"
                false
            }
            STATE_MESSAGE_RECEIVED ->
            {
                val readbuffer = msg.obj as ByteArray
                var data = String(readbuffer , 0 ,msg.arg1 , Charsets.UTF_8)
                var formatteddata = jsonstringtodataclass(data)
                _mymessage.value = formatteddata
                false
            }


            else ->
            {
                _status.value = "error"
                false
            }

        }

    }

    fun jsonstringtodataclass(data : String): msgupdate {
        try {
            var modified_data = Gson().fromJson(data, msgupdate::class.java)
            return modified_data
        }
        catch (e:IOException)
        {
            Toast.makeText(context,"error while fetching data",Toast.LENGTH_SHORT).show()
            return msgupdate(-1,"")
        }
    }

    fun dataclasstojsnstring(data : msgupdate): String {
        var jsonstring = Gson().toJson(data)
        return jsonstring
    }


    inner class server() : Thread()
    {
        private lateinit var serverSocket: BluetoothServerSocket

        init {
            serverclass()
        }

        fun serverclass()
        {
            try
            {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(context,"Permission Not Granted",Toast.LENGTH_SHORT).show()
                    return
                }
                serverSocket = bluetoothadapter.listenUsingRfcommWithServiceRecord(devicename, uuid)
            }
            catch (e:IOException)
            {
                Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
            }

        }

        override fun run() {
            super.run()
            var socket : BluetoothSocket? = null


                while (socket == null) {
                    try {
                        var handlermessage = Message.obtain()
                        handlermessage.what = STATE_CONNECTING
                        handler.sendMessage(handlermessage)

                        socket = serverSocket.accept()
                        device = serverSocket.accept().remoteDevice



                    } catch (e: IOException) {
                        var handlermessage = Message.obtain()
                        handlermessage.what = STATE_CONNECTION_FAILED
                        handler.sendMessage(handlermessage)
                    }

                    if (socket != null) {
                        var handlermessage = Message.obtain()
                        handlermessage.what = STATE_CONNECTED
                        handler.sendMessage(handlermessage)

                        shareit = sendreceive(socket)
                        shareit.start()
                    }
                }



        }
    }

    inner class client(var mydevice : BluetoothDevice) : Thread()
    {
        private lateinit var socket : BluetoothSocket


        init {
            clientclass()
        }

        @SuppressLint("MissingPermission")
        fun clientclass()
        {
            device = mydevice
            try {

                socket = device.createRfcommSocketToServiceRecord(uuid)
            }
            catch (e:IOException)
            {
                Toast.makeText(context,e.message.toString(),Toast.LENGTH_SHORT).show()
            }
        }

        @SuppressLint("MissingPermission")
        override fun run() {
            super.run()

            try {

                socket.connect()
                var handlermessage = Message.obtain()
                handlermessage.what = STATE_CONNECTED
                handler.sendMessage(handlermessage)

                shareit = sendreceive(socket)
                shareit.start()
            }
            catch (e:IOException)
            {
                var handlermessage = Message.obtain()
                handlermessage.what = STATE_CONNECTION_FAILED
                handler.sendMessage(handlermessage)
            }
        }
    }

    inner class sendreceive( var socket: BluetoothSocket) : Thread()
    {
        private lateinit var inputstream : InputStream
        private lateinit var outputstream : OutputStream

        init {
            setupstream()
        }

        fun setupstream()
        {
            try{
                inputstream = socket.inputStream
                outputstream = socket.outputStream
            }
            catch (e:IOException)
            {
                e.printStackTrace()
            }

        }

        override fun run() {
            super.run()

            var buffer = ByteArray(1024)
            var bytes : Int

            while (true)
            {
                try {
                    bytes = inputstream.read(buffer)
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget()
                }
                catch (e:IOException){
                    e.printStackTrace()
                    handler.post {
                        Toast.makeText(context, "Error while receiving message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun write(data : msgupdate)
        {
            var formatted_data = dataclasstojsnstring(data)
            var bytearray = formatted_data.toByteArray()
            try {
                outputstream.write(bytearray)
            }
            catch (e:IOException)
            {
                Toast.makeText(context,"Error while sending message",Toast.LENGTH_SHORT).show()
            }
        }


    }
    


}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun demo() {
    
    val sheetstate = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetstate)
    BottomSheetScaffold(
        modifier = Modifier
            ,
        sheetContent = {


            Card(shape = RoundedCornerShape(30.dp), modifier = Modifier.padding(15.dp).shadow(30.dp, shape = RoundedCornerShape(30.dp)), border = BorderStroke(3.dp,
                Color.Green)) {
                connection()
            }


        },
        sheetBackgroundColor = Color.Transparent,
        sheetElevation = 0.dp,




        ) {
        Text(text = "hello")
    }
}