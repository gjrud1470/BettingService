package com.example.bettingservice.Host

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.MadWeek2.RoomRecyclerAdapter
import com.example.bettingservice.*
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_host_room.*
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class Player : Serializable {
    private var endpointId: String? = null
    private var name: String? = null
    private var budget: Int? = null
    var toCall: Int = 0
        get() = field
        set(value) {
            field = value
        }
    var folded: Boolean = false
        get() = field
        set(value) {
            field = value
        }

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

class HostRoomActivity : AppCompatActivity(), RoomRecyclerAdapter.itemDragListener {

    val TAG = "HostRoomActivity"

    // Define global mutable variables
    lateinit var RoomRecyclerView: RecyclerView
    private lateinit var viewAdapter: RoomRecyclerAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    var room_name: String = ""
    var player_number = 0
    var betting_rounds = 0

    var itemTouchHelper: ItemTouchHelper? = null

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

        display_room_name.text = room_name
        display_username.text = thisUser.getname()
        display_player_number.text = player_number.toString()
        display_betting_round.text = betting_rounds.toString()

        val host_player = Player()
        host_player.setname(thisUser.getname()!!)
        host_player.setbudget(0)
        host_player.setId("host")
        val initial_player_list = arrayListOf<Player>(host_player)

        viewAdapter = RoomRecyclerAdapter(this, initial_player_list, this)
        viewManager = LinearLayoutManager(this)
        RoomRecyclerView = player_recycler_list.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(viewAdapter))
        itemTouchHelper!!.attachToRecyclerView(RoomRecyclerView)

        start_game_btn.setOnClickListener {
            myData.playerList = viewAdapter.player_list
            myData.roomName = room_name
            myData.player_number = player_number
            myData.totalRound = betting_rounds
            myData.start_player = 0
            myData.pool = 0
            myData.toCall = 0
            myData.turn = 0
            myData.yourId = "host"
            broadcast_updated_roominfo(PayloadData.Action.START_GAME)
            startActivity(Intent(this, BettingActivity::class.java))
        }
        set_initial_budget.setOnClickListener {
            MaterialDialog.Builder(this)
                .title("초기 자금 설정")
                .input(0, 0, true, MaterialDialog.InputCallback { dialog, input ->
                    if (input.isNotEmpty()) {
                        host_player.setbudget(input.toString().toInt())
                        initial_budget.text = input.toString()
                        viewAdapter.update_budget("host", input.toString().toInt())

                        broadcast_updated_roominfo(PayloadData.Action.UPDATE_ROOM)
                    }
                })
                .inputType(
                    android.text.InputType.TYPE_CLASS_NUMBER
                            or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                )
                .show()
        }
        setting_button.setOnClickListener {
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.create_game_layout, null)

            MaterialDialog.Builder(this)
                .title("설정")
                .customView(itemView, true)
                .autoDismiss(false)
                .positiveText("저장")
                .onPositive { dialog, which ->
                    val input_room_name =
                        itemView.findViewById<View>(R.id.input_room_name) as EditText
                    val input_player_number =
                        itemView.findViewById<View>(R.id.input_player_number) as EditText
                    val input_betting_round =
                        itemView.findViewById<View>(R.id.input_betting_round) as EditText

                    if (input_room_name.text.toString().isNotEmpty()) {
                        room_name = input_room_name.text.toString()
                        display_room_name.text = room_name
                        Nearby.getConnectionsClient(this).stopAdvertising()
                        Nearby.getConnectionsClient(this)
                            .startAdvertising(
                                room_name,
                                "com.example.bettingservice",
                                connCallback,
                                options
                            )
                    }
                    if (input_player_number.text.toString().isNotEmpty()
                        && input_player_number.text.toString().toInt() >= viewAdapter.itemCount
                        && input_player_number.text.toString().toInt() <= 5
                    ) {
                        player_number = input_player_number.text.toString().toInt()
                        display_player_number.text = player_number.toString()
                    }
                    if (input_betting_round.text.toString().isNotEmpty()
                        && input_betting_round.text.toString().toInt() > 0
                    ) {
                        betting_rounds = input_betting_round.text.toString().toInt()
                        display_betting_round.text = betting_rounds.toString()
                    }

                    if (input_player_number.text.toString().isNotEmpty()
                        && input_player_number.text.toString().toInt() < viewAdapter.itemCount) {
                        Toast.makeText(this, "새로운 플레이어 수는 현재 플레이어 수보다 커야 합니다.", Toast.LENGTH_SHORT).show()
                    }
                    else if (input_player_number.text.toString().isNotEmpty()
                        && input_player_number.text.toString().toInt() > 5) {
                        Toast.makeText(this, "플레이어수는 5명 이하이여야 합니다.", Toast.LENGTH_SHORT).show()
                    }

                    broadcast_updated_roominfo(PayloadData.Action.UPDATE_ROOM)
                    dialog.dismiss()
                }
                .negativeText("취소")
                .onNegative { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
        leave_room_button.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Leave Room?")
                .setPositiveButton("Leave", DialogInterface.OnClickListener { dialog, which ->
                    onBackPressed()
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                .show()
        }

        mPayloadCallback.updateBudget = object : MyPayloadCallback.UpdateUserBudget {
            override fun update_user_budget(endpointId: String, budget: Int) {
                viewAdapter.update_budget(endpointId, budget)
                broadcast_updated_roominfo(PayloadData.Action.UPDATE_ROOM)
            }
        }
    }

    override fun onStartDrag(viewHolder: RoomRecyclerAdapter.MyViewHolder) {
        itemTouchHelper!!.startDrag(viewHolder)
    }


    val connCallback = object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.wtf(TAG, "connection initiated host")
            if (viewAdapter.itemCount < player_number) {
                Log.wtf(TAG, "add item")
                val player = Player()
                player.setId(endpointId)
                player.setname(connectionInfo.endpointName)
                Log.wtf(TAG, connectionInfo.endpointName)
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
                    send_roominfo(endpointId, PayloadData.Action.ENTER_ROOM_INFO)
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
            viewAdapter.removeItem(endpointId)
            Nearby.getConnectionsClient(this@HostRoomActivity).disconnectFromEndpoint(endpointId)
            Log.wtf(TAG, "disconnected from ${endpointId}")
        }

    }

    fun broadcast_updated_roominfo(flag: PayloadData.Action) {
        viewAdapter.player_list.forEach {
            if (it.getId()!! != "host")
                send_roominfo(it.getId()!!, flag)
        }
    }

    fun send_roominfo(endpointId: String, flag: PayloadData.Action) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = flag
        data.playerList = ArrayList(viewAdapter.player_list)
        if (viewAdapter.player_list.isNullOrEmpty()) Log.wtf(TAG, "player list null or empty!")
        data.player_number = player_number
        data.totalRound = betting_rounds
        data.roomName = room_name
        data.pool = 0
        data.turn = 0
        data.toCall = 0
        data.start_player = 0
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@HostRoomActivity).sendPayload(endpointId, payload)
    }

    override fun onBackPressed() {
        Nearby.getConnectionsClient(this@HostRoomActivity).stopAllEndpoints()
        Nearby.getConnectionsClient(this@HostRoomActivity).stopAdvertising()
        super.onBackPressed()
    }
}
