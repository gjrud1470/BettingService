package com.example.bettingservice

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.example.bettingservice.client.ClientRoomActivity
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

class MyPayloadCallback : PayloadCallback() {

    lateinit var mContext: Context
    lateinit var updateRoom : UpdateRoom

    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        val byteArray = payload.asBytes()
        val bis = ByteArrayInputStream(byteArray)
        val ois = ObjectInputStream(bis)

        myData = ois.readObject() as PayloadData

        when (myData.flag) {
            PayloadData.Action.ENTER_ROOM_INFO -> {
                mContext.startActivity(Intent(mContext, ClientRoomActivity::class.java))
            }
            PayloadData.Action.UPDATE_ROOM -> {
                updateRoom!!.update()
            }
            else -> {}
        }

    }

    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

    }

    interface UpdateRoom {
        fun update()
    }
}