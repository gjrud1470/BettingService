package com.example.bettingservice

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createButton.setOnClickListener {
            if (nameInputEditText.text.isNullOrEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userName = nameInputEditText.text.toString()

            Nearby.getConnectionsClient(this)
                .startAdvertising(
                    userName,
                    "com.example.bettingservice",
                    connCallback,
                    AdvertisingOptions(Strategy.P2P_CLUSTER)
                )
        }

        joinButton.setOnClickListener {
            if (nameInputEditText.text.isNullOrEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userName = nameInputEditText.text.toString()
            
            Nearby.getConnectionsClient(this)
                .startDiscovery(
                    "com.example.bettingservice",
                    discoverCallback,
                    DiscoveryOptions(Strategy.P2P_CLUSTER)
                )
        }
    }

    val connCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String?, connectionInfo: ConnectionInfo?) {
            AlertDialog.Builder(this@MainActivity)
                .setTitle(connectionInfo?.endpointName + "님과 연결하겠습니까?")
                .setMessage("토큰이 일치하는지 확인해주세요\n" + connectionInfo?.authenticationToken)
                .setPositiveButton("연결") { _, _ ->
                    Nearby.getConnectionsClient(this@MainActivity)
                        .acceptConnection(endpointId.toString(), payloadCallback)
                }
                .setNegativeButton("거부") { _, _ ->
                    Nearby.getConnectionsClient(this@MainActivity)
                        .rejectConnection(endpointId.toString())
                }
                .show()
        }

        override fun onConnectionResult(endpointId: String?, result: ConnectionResolution?) {
            when (result?.status?.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {}
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {}
                ConnectionsStatusCodes.STATUS_ERROR -> {}
                else -> {}
            }
        }

        override fun onDisconnected(endpointId: String?) {

        }

    }
    
    val discoverCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String?, discoveredEndpointInfo: DiscoveredEndpointInfo?) {
            Log.d("check endpoint", endpointId.toString())
            Nearby.getConnectionsClient(this@MainActivity)
                .requestConnection(userName, endpointId.toString(), connCallback)
        }

        override fun onEndpointLost(endpointId: String?) {
            Log.d("check endpoint", "lost")
        }

    }

    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String?, payload: Payload?) {

        }

        override fun onPayloadTransferUpdate(endpointId: String?, update: PayloadTransferUpdate?) {

        }

    }

}
