package com.example.ivr_calling_app.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class SMSStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "SMS_SENT") {
            val resultCode = resultCode
            when (resultCode) {
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    Toast.makeText(context, "SMS sending failed: Generic failure", Toast.LENGTH_SHORT).show()
                }
                SmsManager.RESULT_ERROR_NO_SERVICE -> {
                    Toast.makeText(context, "SMS sending failed: No service", Toast.LENGTH_SHORT).show()
                }
                SmsManager.RESULT_ERROR_NULL_PDU -> {
                    Toast.makeText(context, "SMS sending failed: Null PDU", Toast.LENGTH_SHORT).show()
                }
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    Toast.makeText(context, "SMS sending failed: Radio off", Toast.LENGTH_SHORT).show()
                }
                -1 -> {
                    // SMS sent successfully (optional: show a success message)
                    Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show()
//                    FirebaseDatabase.getInstance().reference.child("message").child("key").setValue(0)
                }
                else -> {
                    // Handle other error cases (if needed)
                    Toast.makeText(context, "SMS sending failed with unknown error", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (intent?.action == "SMS_DELIVERED") {
            // Handle the delivery status (if required)
            Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show()
        }
    }
}
