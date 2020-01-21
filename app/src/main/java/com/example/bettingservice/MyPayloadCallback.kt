package com.example.bettingservice

import android.content.Context
import android.content.Intent
import android.util.Log
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
    lateinit var updateBudget : UpdateUserBudget

    override fun onPayloadReceived(endpointId: String, payload: Payload) {
        val byteArray = payload.asBytes()
        val bis = ByteArrayInputStream(byteArray)
        val ois = ObjectInputStream(bis)

        val receivedData = ois.readObject() as PayloadData

        when (receivedData.flag) {
            PayloadData.Action.ENTER_ROOM_INFO -> {
                myData = receivedData
                mContext.startActivity(Intent(mContext, ClientRoomActivity::class.java))
            }
            PayloadData.Action.UPDATE_ROOM -> {
                myData = receivedData
                updateRoom.update()
            }
            PayloadData.Action.UPDATE_USER_BUDGET -> {
                updateBudget.update_user_budget(endpointId, receivedData.user_initial_budget)
            }
            else -> {}
        }

    }

    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {

    }

    interface UpdateRoom {
        fun update()
    }

    interface UpdateUserBudget {
        fun update_user_budget(endpointId: String, budget: Int)
    }
}