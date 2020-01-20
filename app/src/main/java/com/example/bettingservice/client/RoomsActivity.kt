package com.example.bettingservice.client

import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bettingservice.R
import com.example.bettingservice.userName
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_rooms.*

class RoomsActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "RoomsActivity"
    private lateinit var adapter: RoomsAdapter
    private var hostId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rooms)
        swipeRefresh.setOnRefreshListener(this)

        loading.setOnTouchListener { _, _ -> true }

        adapter = RoomsAdapter(arrayListOf<Pair<String, String>>())
        adapter.setOnItemClkListener {
            Nearby.getConnectionsClient(this@RoomsActivity).stopDiscovery()
            loading.visibility = View.VISIBLE
            hostId = it
            Nearby.getConnectionsClient(this@RoomsActivity)
                .requestConnection(userName, it, connCallback)
            Log.wtf("WTF", userName)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        setRcView()
    }

    private fun setRcView() {
        Nearby.getConnectionsClient(this).stopDiscovery()

        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()

        Nearby.getConnectionsClient(this).startDiscovery(
            "com.example.bettingservice",
            discoverCallback,
            options
        )
    }


    private val connCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Nearby.getConnectionsClient(this@RoomsActivity).acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Nearby.getConnectionsClient(this@RoomsActivity).stopDiscovery()
            loading.visibility = View.GONE
            hostId = ""
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {

                }
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

    private val discoverCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo) {
            Log.wtf(TAG, "check endpoint ${endpointId}")
            adapter.addItem(Pair(endpointId, discoveredEndpointInfo.endpointName))
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

    override fun onRefresh() {
        adapter.clearData()
        setRcView()
        Handler().postDelayed({
            swipeRefresh.isRefreshing = false
        }, 2000)
    }

    override fun onBackPressed() {
        if (loading.visibility == View.VISIBLE) {
            Nearby.getConnectionsClient(this).disconnectFromEndpoint(hostId)
            loading.visibility = View.GONE
        }
        else super.onBackPressed()
    }
}