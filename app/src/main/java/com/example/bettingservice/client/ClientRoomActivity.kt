package com.example.bettingservice.client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.bettingservice.*
import com.example.bettingservice.Host.Player
import com.example.bettingservice.PayloadData
import com.example.bettingservice.R
import com.example.bettingservice.thisUser
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import kotlinx.android.synthetic.main.activity_host_room.*
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class ClientRoomActivity : AppCompatActivity() {

    val TAG = "HostRoomActivity"

    // Define global mutable variables
    lateinit var RoomRecyclerView: RecyclerView
    private lateinit var viewAdapter: ClientRoomAdpater
    private lateinit var viewManager: RecyclerView.LayoutManager

    var room_name: String = ""
    var player_number = 0
    var betting_rounds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_room)

        room_name = myData.roomName
        player_number = myData.player_number
        betting_rounds = myData.totalRound
        thisUser.setId(myData.yourId)

        display_room_name.text = room_name
        display_username.text = thisUser.getname()
        display_player_number.text = player_number.toString()
        display_betting_round.text = betting_rounds.toString()

        val initial_player_list = myData.playerList

        viewAdapter = ClientRoomAdpater(this, initial_player_list)
        viewManager = LinearLayoutManager(this)
        RoomRecyclerView = player_recycler_list.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        mPayloadCallback.updateRoom = object: MyPayloadCallback.UpdateRoom {
            override fun update() {
                viewAdapter.update_roominfo(myData.playerList)
                display_room_name.text = myData.roomName
                display_player_number.text = myData.player_number.toString()
                display_betting_round.text = myData.totalRound.toString()
            }
        }

        set_initial_budget.setOnClickListener {
            MaterialDialog.Builder(this)
                .title("초기 자금 설정")
                .input(0, 0, true) { dialog, input ->
                    if (input.isNotEmpty()) {
                        thisUser.setbudget(input.toString().toInt())
                        initial_budget.text = input.toString()
                        viewAdapter.update_budget(input.toString().toInt(), thisUser.getId()!!)

                        send_updated_budget(host_endpoint_id, input.toString().toInt())
                    }
                }
                .inputType(android.text.InputType.TYPE_CLASS_NUMBER
                        or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
                .show()
        }
    }

    fun send_updated_budget(endpointId: String, budget: Int) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.UPDATE_USER_BUDGET
        data.user_initial_budget = budget
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@ClientRoomActivity).sendPayload(endpointId, payload)
    }

}