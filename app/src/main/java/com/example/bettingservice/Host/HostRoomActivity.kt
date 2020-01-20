package com.example.bettingservice.Host

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MadWeek2.RoomRecyclerAdapter
import com.example.bettingservice.R
import com.example.bettingservice.userName
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_host_room.*

class Player {
    private var endpointId: String? = null
    private var name: String? = null
    private var budget: Int? = null

    fun getId(): String? {
        return endpointId
    }

    fun setId(id: String) {
        endpointId = id
    }

    fun getname(): String? {
        return name
    }

    fun setname(new_name: String) {
        name = new_name
    }

    fun getbudget(): Int? {
        return budget
    }

    fun setbudget(new_budget: Int) {
        budget = new_budget
    }
}

class HostRoomActivity : AppCompatActivity() {

    val TAG = "HostRoomActivity"

    // Define global mutable variables
    lateinit var RoomRecyclerView: RecyclerView
    private lateinit var viewAdapter: RoomRecyclerAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    var room_name: String = ""
    var player_number = 0
    var betting_rounds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_room)

        room_name = intent.getStringExtra("room_name")!!
        player_number = intent.getIntExtra("player_number", 1)
        betting_rounds = intent.getIntExtra("betting_rounds", 1)

        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
            .build()
        Nearby.getConnectionsClient(this)
            .startAdvertising(
                room_name,
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

        viewAdapter = RoomRecyclerAdapter(this, arrayListOf<Player>())
        viewManager = LinearLayoutManager(this)
        RoomRecyclerView = player_list.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }


    val connCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            if (viewAdapter.itemCount < player_number) {
                val player = Player()
                player.setId(endpointId)
                player.setname(connectionInfo.endpointName)
                player.setbudget(0)
                viewAdapter.addItem(player)
            } else {
                Nearby.getConnectionsClient(this@HostRoomActivity)
                    .rejectConnection(endpointId)
            }
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
