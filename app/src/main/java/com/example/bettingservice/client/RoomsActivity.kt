package com.example.bettingservice.client

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bettingservice.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_rooms.*
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

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
                .requestConnection(thisUser.getname()!!, it, connCallback)
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
            mPayloadCallback.mContext = this@RoomsActivity
            Nearby.getConnectionsClient(this@RoomsActivity).acceptConnection(endpointId, mPayloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Nearby.getConnectionsClient(this@RoomsActivity).stopDiscovery()
            loading.visibility = View.GONE
            hostId = ""
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {

                }
                else -> {

                }
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