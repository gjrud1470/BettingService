package com.example.bettingservice.Host

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.bettingservice.R
import com.example.bettingservice.userName
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

class HostRoomActivity : AppCompatActivity() {

    val TAG = "HostRoomActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_room)

        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
                .build()
        Nearby.getConnectionsClient(this)
            .startAdvertising(
                userName,
                "com.example.bettingservice",
                connCallback,
                options
            )
            .addOnSuccessListener {
                Log.wtf(TAG, "success")
            }
            .addOnFailureListener {
                Log.wtf(TAG, "failue")
            }
    }


    val connCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            AlertDialog.Builder(this@HostRoomActivity)
                .setTitle(connectionInfo.endpointName + "님과 연결하겠습니까?")
                .setMessage("토큰이 일치하는지 확인해주세요\n" + connectionInfo.authenticationToken)
                .setPositiveButton("연결") { _, _ ->
                    Nearby.getConnectionsClient(this@HostRoomActivity)
                        .acceptConnection(endpointId, payloadCallback)
                }
                .setNegativeButton("거부") { _, _ ->
                    Nearby.getConnectionsClient(this@HostRoomActivity)
                        .rejectConnection(endpointId)
                }
                .show()
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Nearby.getConnectionsClient(this@HostRoomActivity).stopDiscovery()

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.wtf(TAG, "rejected")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                }
                else -> {
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.wtf(TAG, "disconnected")
        }

    }

    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }

    }
}
