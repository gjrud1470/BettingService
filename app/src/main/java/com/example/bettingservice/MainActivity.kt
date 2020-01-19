package com.example.bettingservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bettingservice.client.RoomsActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*

var userName : String = ""

class MainActivity : AppCompatActivity() {

    private val TAG = "NearbyConnection"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createButton.setOnClickListener {
            if (nameInputEditText.text.isNullOrEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userName = nameInputEditText.text.toString()

            val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()

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


        joinButton.setOnClickListener {
            if (nameInputEditText.text.isNullOrEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, RoomsActivity::class.java))
            finish()
        }
    }

    val connCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(connectionInfo.endpointName + "님과 연결하겠습니까?")
                .setMessage("토큰이 일치하는지 확인해주세요\n" + connectionInfo.authenticationToken)
                .setPositiveButton("연결") { _, _ ->
                    Nearby.getConnectionsClient(this@MainActivity)
                        .acceptConnection(endpointId, payloadCallback)
                }
                .setNegativeButton("거부") { _, _ ->
                    Nearby.getConnectionsClient(this@MainActivity)
                        .rejectConnection(endpointId)
                }
                .show()
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Nearby.getConnectionsClient(this@MainActivity).stopDiscovery()

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {}
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.wtf(TAG, "rejected")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {}
                else -> {}
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.wtf(TAG, "disconnected")
        }

    }
    
    val discoverCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.wtf(TAG, "check endpoint ${endpointId}")
            Nearby.getConnectionsClient(this@MainActivity)
                .requestConnection(userName, endpointId, connCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.wtf(TAG, "check endpoint lost")
        }

    }

    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }

    }

}
