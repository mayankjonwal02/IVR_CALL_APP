package com.example.ivr_calling_app.android

import android.content.SharedPreferences
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.ivr_call_app_20.android.Bluetooth.msgupdate
import com.example.ivr_call_app_20.android.mybluetooth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import viewmodel


data class cat_lan( var category : String , var language : String , var duedate : String )


class incallservice : InCallService()
{
    lateinit var sharedPreferences: SharedPreferences
    lateinit var viewmodel: viewmodel

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getsharedpreferences(this)
        viewmodel = viewmodel(this)

    }
    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)

        call?.registerCallback(object : Call.Callback()
        {
            override fun onStateChanged(call: Call?, state: Int) {
                super.onStateChanged(call, state)

                when(state)
                {
                    Call.STATE_DIALING -> {
                        var job = CoroutineScope(Dispatchers.IO).launch{
                            sharedPreferences.getString("engagementid", "ID not found")?.let {
                                viewmodel.update(
                                    "day${
                                        sharedPreferences.getString(
                                            "callday",
                                            "day not found"
                                        )
                                    }",
                                    it,
                                    "call not picked"
                                )
                            }
                        }


                        Log.e("TAG1","dialing : " + sharedPreferences.getString("engagementid" , "ID not Found"))
                        sharedPreferences.edit().putInt("callstate",1).apply()

                        Toast.makeText(this@incallservice,"Dialing ${call?.details?.handle?.schemeSpecificPart}",Toast.LENGTH_SHORT).show()

                        mybluetooth._mymessage.value = msgupdate(2,"dialing")

                    }

                    Call.STATE_ACTIVE -> {

                        sharedPreferences.edit().putInt("callstate",2).apply()
                        Log.e("TAG1","connected with  : " + sharedPreferences.getString("engagementid" , "ID not Found"))
                        Toast.makeText(this@incallservice,sharedPreferences.getString("patientcno" ,"contact not found") + " connected",Toast.LENGTH_LONG).show()

                        Toast.makeText(this@incallservice,"${mybluetooth.patientcategory.value} ${mybluetooth.language.value}",Toast.LENGTH_SHORT).show()
                        mybluetooth.shareit.write(msgupdate(3, Gson().toJson(cat_lan(mybluetooth.patientcategory.value , mybluetooth.language.value , mybluetooth.duedate.value))))
                        "${mybluetooth.patientcategory.value} ${mybluetooth.language.value}"

                    }

                    Call.STATE_DISCONNECTED -> {
                        Log.e("TAG1","disconnected with : " + sharedPreferences.getString("engagementid" , "ID not Found"))
                        sharedPreferences.edit().putInt("callstate",0).apply()

                        mybluetooth.shareit.write(msgupdate(2, "disconnected"))

                        mybluetooth._mymessage.value = msgupdate(0,"")

                    }
                }
            }

        })

        CoroutineScope(Dispatchers.Main).launch{
            mybluetooth.mymessage.collect { msgupdate ->
                when (msgupdate)
                {
                    msgupdate(4,"Perfectly Responded") ->
                    {
                        var job1 = CoroutineScope(Dispatchers.IO).launch {
                            sharedPreferences.getString("engagementid","id not found")?.let {
                                viewmodel.update("day${sharedPreferences.getString(
                                    "callday",
                                    "day not found"
                                )}", it,"perfectly responded")
                            }
                        }

                        job1.join()
                        Toast.makeText(this@incallservice,"perfectly responded",Toast.LENGTH_SHORT).show()
                        call?.disconnect()
                    }
                    msgupdate(4,"Listened Half") ->
                    {
                        var job1 = CoroutineScope(Dispatchers.IO).launch {
                            sharedPreferences.getString("engagementid","id not found")?.let {
                                viewmodel.update("day${sharedPreferences.getString(
                                    "callday",
                                    "day not found"
                                )}", it,"listened half")
                            }
                        }
                        job1.join()
                        mybluetooth._mymessage.value = msgupdate(0,"")

                        Toast.makeText(this@incallservice,"listened half",Toast.LENGTH_SHORT).show()
                    }
                    msgupdate(4,"Error") ->
                    {
                        var job1 = CoroutineScope(Dispatchers.IO).launch {
                            sharedPreferences.getString("engagementid","id not found")?.let {
                                viewmodel.update("day${sharedPreferences.getString(
                                    "callday",
                                    "day not found"
                                )}", it,"error while playing audio")
                            }
                        }

                        job1.join()

                        Toast.makeText(this@incallservice,"error while playing audio",Toast.LENGTH_SHORT).show()
                        call?.disconnect()
                    }
                    msgupdate(4,"Audio doesn't Exist") ->
                    {
                        var job1 = CoroutineScope(Dispatchers.IO).launch {
                            sharedPreferences.getString("engagementid","id not found")?.let {
                                viewmodel.update("day${sharedPreferences.getString(
                                    "callday",
                                    "day not found"
                                )}", it,"audio doesn't exist")
                            }
                        }
                        job1.join()
                        Toast.makeText(this@incallservice,"error while playing audio",Toast.LENGTH_SHORT).show()
                        call?.disconnect()
                    }
                    msgupdate(4,"Category doesn't Exist") ->
                    {
                        var job1 = CoroutineScope(Dispatchers.IO).launch {
                            sharedPreferences.getString("engagementid","id not found")?.let {
                                viewmodel.update("day${sharedPreferences.getString(
                                    "callday",
                                    "day not found"
                                )}", it,"category doesn't exist")
                            }
                        }
                        job1.join()
                        Toast.makeText(this@incallservice,"error while playing audio",Toast.LENGTH_SHORT).show()
                        call?.disconnect()
                    }

                }
            }
        }




    }


    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
    }




}