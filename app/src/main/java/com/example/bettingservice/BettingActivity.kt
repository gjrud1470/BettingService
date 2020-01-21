package com.example.bettingservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.bettingservice.Host.Player
import com.example.bettingservice.client.RoomsActivity
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_betting.*
import kotlinx.android.synthetic.main.activity_host_room.*
import kotlinx.android.synthetic.main.activity_rooms.*
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class BettingActivity : AppCompatActivity() {

    private val TAG = "BettingActivity"

    private lateinit var adapter: PlayerAdapter

    private var mybudget: Int = 0
    private var mynumber: Int = -1
    private var pool_value: Int = 0
    private var toCall_value: Int = 0
    private var folded: Boolean = false

    private var my_turn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_betting)

        // Connect to all other players
        /*
        if (myData.yourId != "host") {
            var request_flag = false
            val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER)
                .build()
            Nearby.getConnectionsClient(this).startAdvertising(
                thisUser.getname()!!,
                "com.example.bettingservice",
                connCallback,
                options
            )
            myData.playerList.forEach {
                if (it.getId() == myData.yourId) {
                    request_flag = true
                } else if (request_flag) {
                    Nearby.getConnectionsClient(this)
                        .requestConnection(thisUser.getname()!!, it.getId()!!, connCallback)
                }
            } */


        Log.wtf(TAG, myData.playerList.toString())
        adapter = PlayerAdapter(myData.playerList)
        userList.adapter = adapter
        userList.layoutManager = LinearLayoutManager(this)

        myData.playerList.forEachIndexed { index, player ->
            if (player.getId() == myData.yourId) {
                mybudget = player.getbudget()!!
                toCall_value = player.toCall
                folded = player.folded
                mynumber = index
            }
        }

        room_title.text = myData.roomName

        pool_value = myData.pool
        pool.text = pool_value.toString()
        toCall.text = toCall_value.toString()
        budget.text = mybudget.toString()

        call.setOnClickListener {
            if (my_turn && !folded) {
                if (mybudget < toCall_value) {
                    Toast.makeText(this, "돈이 부족합니다 충전하세요", Toast.LENGTH_SHORT).show()
                } else {
                    if (myData.yourId == "host") {
                        myData.pool += toCall_value
                        mybudget -= toCall_value

                        myData.playerList.forEachIndexed { index, player ->
                            if (player.getId() == "host") {
                                player.setbudget(mybudget)
                                player.toCall = 0
                            }
                        }
                        toCall_value = 0
                        myData.turn++

                        pool_value = myData.pool
                        pool.text = pool_value.toString()
                        toCall.text = toCall_value.toString()
                        budget.text = mybudget.toString()

                        broadcast_updated_betinfo()
                        one_turn(myData.turn % myData.playerList.size)
                    } else {
                        user_bet_info(host_endpoint_id, 0)
                    }
                }
            }
        }
        raise.setOnClickListener {
            if (my_turn && !folded) {
                MaterialDialog.Builder(this)
                    .title("얼마더낼래?")
                    .input(0, 0, true, MaterialDialog.InputCallback { dialog, input ->
                        if (input.isNotEmpty()
                            && input.toString().toInt() > 0
                        ) {
                            if (input.toString().toInt() + toCall_value > mybudget) {
                                Toast.makeText(this, "돈이 부족합니다 충전하세요", Toast.LENGTH_SHORT).show()
                            } else {
                                if (myData.yourId == "host") {
                                    val raise_value = input.toString().toInt() + toCall_value
                                    myData.pool += raise_value
                                    mybudget -= raise_value

                                    myData.playerList.forEachIndexed { index, player ->
                                        if (player.getId() == "host") {
                                            player.setbudget(mybudget)
                                            player.toCall = 0
                                        } else {
                                            player.toCall += input.toString().toInt()
                                            if (player.folded) player.toCall = 0
                                        }
                                    }
                                    toCall_value = 0
                                    myData.turn++

                                    pool_value = myData.pool
                                    pool.text = pool_value.toString()
                                    toCall.text = toCall_value.toString()
                                    budget.text = mybudget.toString()

                                    broadcast_updated_betinfo()
                                    one_turn(myData.turn % myData.playerList.size)
                                } else {
                                    user_bet_info(host_endpoint_id, input.toString().toInt())
                                }
                            }
                        }
                    })
                    .inputType(
                        android.text.InputType.TYPE_CLASS_NUMBER
                                or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    )
                    .show()
            }
        }
        fold.setOnClickListener {
            if (my_turn && !folded) {
                if (myData.yourId == "host") {
                    folded = true
                    myData.playerList.forEachIndexed { index, player ->
                        if (player.getId() == myData.yourId) {
                            player.folded = true
                        }
                    }
                    myData.turn++
                    broadcast_updated_betinfo()

                    one_turn(myData.turn % myData.playerList.size)
                } else {
                    folded = true
                    user_folded(host_endpoint_id)
                }
            }
        }

        mPayloadCallback.updateBet = object : MyPayloadCallback.UpdateUserBet {
            override fun update_user_bet(endpointId: String, bet: Int) {
                var original_tocall: Int = 0
                myData.playerList.forEach {
                    if (it.getId() == endpointId) {
                        original_tocall = it.toCall
                    }
                }
                myData.playerList.forEach {
                    if (it.getId() == endpointId) {
                        val player_budget = it.getbudget()!!
                        it.setbudget(player_budget - bet - original_tocall)
                        it.toCall = 0
                    } else if (it.getId() == "host") {
                        it.toCall += bet
                        toCall_value = it.toCall
                    } else {
                        it.toCall += bet
                        if (it.folded) it.toCall = 0
                    }
                }
                if (folded) toCall_value = 0

                myData.pool = myData.pool + bet + original_tocall
                myData.turn++

                pool_value = myData.pool
                pool.text = pool_value.toString()
                toCall.text = toCall_value.toString()

                broadcast_updated_betinfo()

                one_turn(myData.turn % myData.playerList.size)
            }

            override fun update_pool() {
                myData.playerList.forEachIndexed { index, player ->
                    if (player.getId() == myData.yourId) {
                        mybudget = player.getbudget()!!
                        toCall_value = player.toCall
                        folded = player.folded
                        mynumber = index
                    }
                }

                pool_value = myData.pool
                pool.text = pool_value.toString()
                toCall.text = toCall_value.toString()
                budget.text = mybudget.toString()

                one_turn(myData.turn % myData.playerList.size)
            }

            override fun update_folded_user(endpointId: String) {
                myData.playerList.forEachIndexed { index, player ->
                    if (player.getId() == endpointId) {
                        player.folded = true
                    }
                }
                myData.turn++
                broadcast_updated_betinfo()

                one_turn(myData.turn % myData.playerList.size)
            }
        }

        one_turn(myData.start_player)

    }

    fun one_turn(player_index: Int) {
        adapter.playerList = myData.playerList
        adapter.highlight_player(player_index)
        if (player_index == mynumber) {
            if (folded) {
                if (myData.yourId == "host") {
                    myData.turn++
                    broadcast_updated_betinfo()
                    one_turn(myData.turn % myData.playerList.size)
                }
                else {
                    user_folded(host_endpoint_id)
                }
            } else {
                enable_turn()
            }
        } else {
            disable_turn()
        }

    }

    fun enable_turn() {
        my_turn = true

        // Pop up my turn dialog
        // Change Button Colors
        call.setBackgroundColor(resources.getColor(R.color.highlight))
        raise.setBackgroundColor(resources.getColor(R.color.highlight))
        fold.setBackgroundColor(resources.getColor(R.color.highlight))
    }

    fun disable_turn() {
        my_turn = false

        // Change Button Colors
        call.setBackgroundColor(resources.getColor(R.color.button))
        raise.setBackgroundColor(resources.getColor(R.color.button))
        fold.setBackgroundColor(resources.getColor(R.color.button))
    }

    fun betting_round(start_player: Int): Int {
        adapter.highlight_player(start_player)
        return 0
    }

    override fun onBackPressed() {
        Toast.makeText(this, "벌써 가게?", Toast.LENGTH_SHORT).show()
    }


    fun user_bet_info(endpointId: String, bet: Int) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.SEND_BET_INFO
        data.bet = bet
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@BettingActivity).sendPayload(endpointId, payload)
    }

    fun user_folded(endpointId: String) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.USER_FOLD
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@BettingActivity).sendPayload(endpointId, payload)
    }

    // For Host use ONLY
    fun broadcast_updated_betinfo() {
        myData.playerList.forEach {
            if (it.getId()!! != "host")
                send_betinfo(it.getId()!!)
        }
    }

    fun send_betinfo(endpointId: String) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.UPDATE_GAME
        data.playerList = ArrayList(myData.playerList)
        data.pool = myData.pool
        data.turn = myData.turn
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@BettingActivity).sendPayload(endpointId, payload)
    }
}
