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
    lateinit var updateBet : UpdateUserBet

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
            PayloadData.Action.START_GAME -> {
                myData = receivedData
                mContext.startActivity(Intent(mContext, BettingActivity::class.java))
            }
            PayloadData.Action.UPDATE_GAME -> {
                myData.playerList = receivedData.playerList
                myData.turn = receivedData.turn
                myData.pool = receivedData.pool
                updateBet.update_pool()
            }
            PayloadData.Action.SEND_BET_INFO -> {
                myData.bet = receivedData.bet
                updateBet.update_user_bet(endpointId, myData.bet)
            }
            PayloadData.Action.USER_FOLD -> {
                updateBet.update_folded_user(endpointId)
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

    interface UpdateUserBet {
        fun update_pool ()
        fun update_user_bet(endpointId:String, bet: Int)
        fun update_folded_user(endpointId: String)
    }
}