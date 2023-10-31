package com.example.ivr_calling_app.android;

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.TimeUnit
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Message
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ivr_call_app_20.android.Bluetooth.msgupdate
import com.example.ivr_call_app_20.android.mybluetooth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import viewmodel
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class mycallservice : Service()
{

    lateinit var viewmodel : viewmodel
    lateinit var valueeventlistener : ValueEventListener
    lateinit var sharedPreferences : SharedPreferences
    var job : Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
    suspend fun data(): List<patientdata> {
        return viewmodel.data()
    }

    fun sendsms(contact : String , message: String?)
    {
//        FirebaseDatabase.getInstance().reference.child("message").child("key").setValue(1)
        val sentIntent = Intent("SMS_SENT")
        val deliveredIntent = Intent("SMS_DELIVERED")

        val sentPendingIntent = PendingIntent.getBroadcast(
            this, 0, sentIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val deliveredPendingIntent = PendingIntent.getBroadcast(
            this, 0, deliveredIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                contact,
                null,
                message,
                sentPendingIntent,
                deliveredPendingIntent
            )
        }
        catch (e:Exception)
        {
            Toast.makeText(this,e.message.toString(),Toast.LENGTH_SHORT).show()
        }
//        FirebaseDatabase.getInstance().reference.child("message").child("key").setValue(0)
    }

    override fun onCreate() {
        super.onCreate()
        viewmodel = viewmodel(this)
        sharedPreferences = getsharedpreferences(this)

        sharedPreferences.edit().putInt("callstate",0).apply()


    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("callservice", "call service started")
        Toast.makeText(this, "Service started",Toast.LENGTH_LONG).show()
        var date = datetoday()


        job = CoroutineScope(Dispatchers.Main).launch{
            mybluetooth.mymessage.collect { msg ->
                when (msg)
                {
                    msgupdate(0,"") ->
                    {
                        try {
                            var job = CoroutineScope(Dispatchers.IO).launch {

                                delay(4000)
                                var data = date?.let { viewmodel.databyid(it) }
                                var modifieddata = data?.filter { daysbwdates(date, it.duedate) in 1..7 }

                                // Check if modifieddata is null or empty
                                if (modifieddata.isNullOrEmpty()) {
                                    // No data available or no matching data found
                                    return@launch // Exit the coroutine
                                }

                                try {
                                    sharedPreferences.edit().putString("engagementid", modifieddata[0].engagementid).apply()
                                    sharedPreferences.edit().putString("patientcno", modifieddata[0].patientcno).apply()
                                    Log.e("TAG1","${modifieddata[0].engagementid}  ${modifieddata[0].patientcno}")
                                    Log.e("TAG1",modifieddata[0].toString())
                                    modifieddata[0].let { var responce = viewmodel.update("calledon", it.engagementid, date)
                                    Log.e("TAG1",responce.toString())}


                                    if (daysbwdates(date, modifieddata[0].duedate) in 5..7) {
                                        mybluetooth._mymessage.value = msgupdate(1,"")
                                        viewmodel.update(
                                            "day${daysbwdates(date, modifieddata[0].duedate)}",
                                            modifieddata[0].engagementid,
                                            "SMS Sent"
                                        )
                                        var sp = getsharedpreferences(this@mycallservice)
                                        var mymessage = sp.getString("msg_type","")
                                        mymessage = mymessage?.replace("@name@",modifieddata[0].patientname, ignoreCase = true)
                                        mymessage = mymessage?.replace("@duedate@",modifieddata[0].duedate, ignoreCase = true)
                                        mymessage = mymessage?.replace("@operationtype@",modifieddata[0].operationtype, ignoreCase = true)
                                        sendsms(
                                            modifieddata[0].patientcno,
                                            mymessage
                                        )
                                        mybluetooth._mymessage.value = msgupdate(0,"")
                                    } else if (daysbwdates(date, modifieddata[0].duedate) in 1..4) {
                                        sharedPreferences.edit().putString(
                                            "callday",
                                            daysbwdates(date, modifieddata[0].duedate).toString()
                                        ).apply()
                                        mybluetooth._language.value = modifieddata[0].language
                                        mybluetooth._patientcategory.value = modifieddata[0].operationtype
                                        mybluetooth._duedate.value = modifieddata[0].duedate
                                        Log.e("TAG1","calling ${modifieddata[0]}")
                                        delay(10000)
                                        var sp = getsharedpreferences(this@mycallservice)
                                        var mymessage = sp.getString("msg_type","")
                                        mymessage = mymessage?.replace("@name@",modifieddata[0].patientname, ignoreCase = true)
                                        mymessage = mymessage?.replace("@duedate@",modifieddata[0].duedate, ignoreCase = true)
                                        mymessage = mymessage?.replace("@operationtype@",modifieddata[0].operationtype, ignoreCase = true)
                                        sendsms(
                                            modifieddata[0].patientcno,
                                            mymessage
                                        )
                                        makecall(modifieddata[0].patientcno)
                                        Log.e("TAG1","call function returned to ${modifieddata[0]}")
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(this@mycallservice, e.message.toString(), Toast.LENGTH_LONG).show()
                                    Log.e("TAG1","error 1 " + e.toString())
                                }
                            }
                            job.join()
                        } catch (e: Exception) {
                            Toast.makeText(this@mycallservice, e.message.toString(), Toast.LENGTH_LONG).show()
                            Log.e("TAG1","error 2 "+e.toString())
                        }
                    }


                }
            }
        }

//        valueeventlistener = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.child("call").child("key").getValue(Long::class.java) == 0L &&
//                    snapshot.child("audio").child("key").getValue(Long::class.java) == 0L &&
//                    snapshot.child("message").child("key").getValue(Long::class.java) == 0L &&
//                    snapshot.child("powercall").getValue(Long::class.java) == 1L &&
//                    snapshot.child("poweraudio").getValue(Long::class.java) == 1L
//                ) {
//                    try {
//                        CoroutineScope(Dispatchers.IO).launch {
//
//                            delay(4000)
//                            var data = date?.let { viewmodel.databyid(it) }
//                            var modifieddata = data?.filter { daysbwdates(date, it.duedate) in 1..7 }
//
//                            // Check if modifieddata is null or empty
//                            if (modifieddata.isNullOrEmpty()) {
//                                // No data available or no matching data found
//                                return@launch // Exit the coroutine
//                            }
//
//                            try {
//                                sharedPreferences.edit().putString("engagementid", modifieddata[0].engagementid).apply()
//                                sharedPreferences.edit().putString("patientcno", modifieddata[0].patientcno).apply()
//                                modifieddata[0].let { viewmodel.update("calledon", it.engagementid, date) }
//
//                                if (daysbwdates(date, modifieddata[0].duedate) in 5..7) {
//                                    viewmodel.update(
//                                        "day${daysbwdates(date, modifieddata[0].duedate)}",
//                                        modifieddata[0].engagementid,
//                                        "SMS Sent"
//                                    )
//                                    sendsms(
//                                        modifieddata[0].patientcno,
//                                        "Hey ${modifieddata[0].patientname}, you have a ${modifieddata[0].operationtype} operation on ${modifieddata[0].duedate}"
//                                    )
//                                } else if (daysbwdates(date, modifieddata[0].duedate) in 1..4) {
//                                    sharedPreferences.edit().putString(
//                                        "callday",
//                                        daysbwdates(date, modifieddata[0].duedate).toString()
//                                    ).apply()
//                                    makecall(modifieddata[0].patientcno)
//                                }
//                            } catch (e: Exception) {
//                                Toast.makeText(this@mycallservice, e.message.toString(), Toast.LENGTH_LONG).show()
//                            }
//                        }
//                    } catch (e: Exception) {
//                        Toast.makeText(this@mycallservice, e.message.toString(), Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle onCancelled if needed
//            }
//        }
        mybluetooth._mymessage.value = msgupdate(0,"")
//        FirebaseDatabase.getInstance().reference.addValueEventListener(valueeventlistener)

        return super.onStartCommand(intent, flags, startId)
    }



    override fun onDestroy() {
        Log.v("callservice","call service stopped")
        mybluetooth._mymessage.value = msgupdate(-1,"")
//        FirebaseDatabase.getInstance().reference.removeEventListener(valueeventlistener)
//        FirebaseDatabase.getInstance().reference.child("call").child("key").setValue(0)
        job?.cancel()
        job = null
        super.onDestroy()
    }


    fun makecall(contact: String)
    {
        try{
            var intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$contact")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
        catch (e:IOException)
        {
            Log.e("TAG1","calling error  " + e.toString())
        }
    }




}


@RequiresApi(Build.VERSION_CODES.O)
fun datetoday(): String
{
    val date = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    return formatter.format(date)
}

fun daysbwdates(date1 : String , date2 : String): Long {
    var dateformat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    var startdate = dateformat.parse(date1)
    var enddate = dateformat.parse(date2)
    var diffinmillis = enddate.time - startdate.time

    return java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffinmillis)
}


