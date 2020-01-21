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

        Log.wtf(TAG, myData.playerList.toString())
        adapter = PlayerAdapter(myData.playerList)
        val totalRoundtext = "/ ${myData.totalRound.toString()}"
        totalRound.text = totalRoundtext
        curRound.text = "1"
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
                    var live_players = 0
                    myData.playerList.forEachIndexed { index, player ->
                        if (player.getId() == myData.yourId) {
                            player.folded = true
                        } else if (!player.folded) {
                            live_players++
                        }
                    }
                    if (live_players == 1) {
                        var winner_index = -1
                        myData.playerList.forEachIndexed { index, player ->
                            if (!player.folded) {
                                val winner_budget = player.getbudget()!! + myData.pool
                                player.setbudget(winner_budget)
                                broadcast_winner(index)
                                winner_index = index

                                if (player.getId() == "host") {
                                    budget.text = winner_budget.toString()
                                }
                            }
                        }
                        adapter.playerList = myData.playerList
                        adapter.notifyDataSetChanged()

                        val winner_name = myData.playerList[winner_index].getname()
                        MaterialDialog.Builder(this@BettingActivity)
                            .title("승자는 ${winner_name} 입니다.\n 축하합니다.")
                            .positiveText("확인")
                            .show()
                    } else {
                        myData.turn++
                        broadcast_updated_betinfo()

                        one_turn(myData.turn % myData.playerList.size)
                    }
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
                curRound.text = myData.roundNum.toString()

                one_turn(myData.turn % myData.playerList.size)
            }

            override fun update_folded_user(endpointId: String) {
                var live_players = 0
                myData.playerList.forEachIndexed { index, player ->
                    if (player.getId() == endpointId) {
                        player.folded = true
                    } else if (!player.folded) {
                        live_players++
                    }
                }
                if (live_players == 1) {
                    var winner_index = -1
                    myData.playerList.forEachIndexed { index, player ->
                        if (!player.folded) {
                            val winner_budget = player.getbudget()!! + myData.pool
                            player.setbudget(winner_budget)
                            broadcast_winner(index)
                            winner_index = index

                            if (player.getId() == "host") {
                                budget.text = winner_budget.toString()
                            }
                        }
                    }
                    adapter.playerList = myData.playerList
                    adapter.notifyDataSetChanged()

                    restart_game()

                    val winner_name = myData.playerList[winner_index].getname()
                    MaterialDialog.Builder(this@BettingActivity)
                        .title("승자는 ${winner_name} 입니다.\n 축하합니다.")
                        .positiveText("확인")
                        .show()
                } else {
                    myData.turn++
                    broadcast_updated_betinfo()

                    one_turn(myData.turn % myData.playerList.size)
                }
            }

            override fun receive_winner(winner_id: Int) {
                if (myData.playerList[winner_id].getId() == myData.yourId) {
                    mybudget = myData.playerList[winner_id].getbudget()!!
                    budget.text = mybudget.toString()
                }
                adapter.playerList = myData.playerList
                adapter.notifyDataSetChanged()

                val winner_name = myData.playerList[winner_id].getname()

                restart_game()

                MaterialDialog.Builder(this@BettingActivity)
                    .title("승자는 ${winner_name} 입니다.\n 축하합니다.")
                    .positiveText("확인")
                    .onPositive { dialog, which ->
                        dialog.dismiss()
                        /*
                        MaterialDialog.Builder(this@BettingActivity)
                            .title("계속 하시겠습니까?")
                            .positiveText("당연하지")
                            .negativeText("난 쫄려서...")
                            .onPositive { dialog_in, which_in ->
                                dialog_in.dismiss()
                            }
                            .onNegative { dialog_in, which_in ->
                                user_left_game(host_endpoint_id)

                                Nearby.getConnectionsClient(this@BettingActivity).stopAllEndpoints()
                                val intent = Intent(this@BettingActivity, MainActivity::class.java)
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                finish()
                            }
                            .show() */
                    }
                    .show()
            }

            override fun user_left(left_id: String) {
                var left_index = -1
                myData.playerList.forEachIndexed { index, player ->
                    if (player.getId() == left_id) {
                        left_index = index
                    }
                }
                myData.playerList.removeAt(left_index)
                adapter.notifyItemRemoved(left_index)
                broadcast_updated_betinfo()
            }

            override fun receive_game_winner(winner_id: Int) {
                if (myData.playerList[winner_id].getId() == myData.yourId) {
                    mybudget = myData.playerList[winner_id].getbudget()!!
                    budget.text = mybudget.toString()
                }
                adapter.playerList = myData.playerList
                adapter.notifyDataSetChanged()

                val winner_name = myData.playerList[winner_id].getname()

                restart_game()

                MaterialDialog.Builder(this@BettingActivity)
                    .title("승자는 ${winner_name} 입니다.\n 축하합니다.")
                    .positiveText("확인")
                    .onPositive { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        one_turn(myData.start_player)

    }

    fun one_turn(player_index: Int) {
        if (myData.roundNum < myData.totalRound) {

                if (myData.turn - myData.start_player >= myData.playerList.size) {

                    var isFinished = true

                    myData.playerList.forEach {
                        if (!it.folded && it.toCall != 0) isFinished = false
                    }

                    if (isFinished) {
                        if (myData.yourId == "host") {
                            round_finished()
                        }

                    }
                    else {
                        resume_turn(player_index)
                    }
                }
                else {
                    resume_turn(player_index)
                }
        }
        else if (myData.roundNum >= myData.totalRound) {
            if (myData.turn - myData.start_player >= myData.playerList.size) {

                var isFinished = true

                myData.playerList.forEach {
                    if (!it.folded && it.toCall != 0) isFinished = false
                }

                if (isFinished) {
                    folded = false
                    if (myData.yourId == "host") {
                        game_finished()
                    }

                }
                else {
                    resume_turn(player_index)
                }
            }
            else {
                resume_turn(player_index)
            }
        }
    }

    fun resume_turn(player_index: Int) {
        adapter.playerList = myData.playerList
        adapter.highlight_player(player_index)
        if (player_index == mynumber) {
            if (folded) {
                if (myData.yourId == "host") {
                    myData.turn++
                    broadcast_updated_betinfo()
                    one_turn(myData.turn % myData.playerList.size)
                } else {
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

    override fun onBackPressed() {
        Toast.makeText(this, "벌써 가게?", Toast.LENGTH_SHORT).show()
    }

    fun round_finished() {
        //endpointId: String
        myData.turn = 0
        myData.roundNum++
        curRound.text = myData.roundNum.toString()

        MaterialDialog.Builder(this@BettingActivity)
            .title("다음 라운드 첫번째 플레이어를 선택해주세요")
            .autoDismiss(false)
            .input(0, 0, false) { dialog, input ->
                var flag = false
                myData.playerList.forEachIndexed { index, player ->
                    Log.wtf(TAG, input.toString())
                    if (player.getname() == input.toString()) {
                        myData.start_player = index
                        flag = true
                    }
                }
                if (flag) {
                    dialog.dismiss()
                    myData.turn = myData.start_player
                    broadcast_updated_betinfo()
                    one_turn(myData.start_player)
                }
                else {
                    Toast.makeText(this@BettingActivity, "올바른 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                }

            }
            .inputType(android.text.InputType.TYPE_CLASS_TEXT)
            .show()
    }

    fun game_finished() {
        myData.playerList.forEachIndexed { index, player ->
            myData.playerList[index].folded = false
        }
        MaterialDialog.Builder(this@BettingActivity)
            .title("승자를 입력해주세요.")
            .autoDismiss(false)
            .input(0, 0, false) { dialog, input ->
                var flag = false
                var winner_index = -1
                myData.playerList.forEachIndexed { index, player ->
                    if (player.getname() == input.toString()) {
                        val winner_budget = player.getbudget()!! + myData.pool
                        player.setbudget(winner_budget)
                        winner_index = index
                        flag = true

                        if (player.getId() == "host") {
                            budget.text = winner_budget.toString()
                        }
                    }
                }

                if (flag) {
                    dialog.dismiss()

                    adapter.playerList = myData.playerList
                    adapter.notifyDataSetChanged()

                    val winner_name = myData.playerList[winner_index].getname()
                    MaterialDialog.Builder(this@BettingActivity)
                        .title("승자는 ${winner_name} 입니다.\n 축하합니다.")
                        .positiveText("확인")
                        .show()

                    myData.turn = winner_index
                    myData.start_player = winner_index
                    broadcast_game_winner(winner_index)

                    restart_game()
                }
                else {
                    Toast.makeText(this@BettingActivity, "올바른 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                }

            }
            .inputType(android.text.InputType.TYPE_CLASS_TEXT)
            .show()
    }

    fun restart_game() {
        myData.pool = 0
        myData.roundNum = 1
        myData.turn = myData.start_player
        pool.text = myData.pool.toString()
        curRound.text = myData.roundNum.toString()
        one_turn(myData.start_player)
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


    fun user_left_game(endpointId: String) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.USER_LEFT
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
        data.start_player = myData.start_player
        data.roundNum = myData.roundNum
        data.pool = myData.pool
        data.turn = myData.turn
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@BettingActivity).sendPayload(endpointId, payload)
    }

    fun broadcast_winner(winner_id: Int) {
        myData.playerList.forEach {
            if (it.getId()!! != "host")
                send_winner(it.getId()!!, winner_id)
        }
    }

    fun send_winner(endpointId: String, winner_id: Int) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.BROADCAST_WINNER
        data.playerList = ArrayList(myData.playerList)
        data.start_player = winner_id
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@BettingActivity).sendPayload(endpointId, payload)
    }

    fun broadcast_game_winner(winner_id: Int) {
        myData.playerList.forEach {
            if (it.getId()!! != "host")
                send_game_winner(it.getId()!!, winner_id)
        }
    }

    fun send_game_winner(endpointId: String, winner_id: Int) {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        val data = PayloadData()
        data.flag = PayloadData.Action.BROADCAST_GAME_WINNER
        data.playerList = ArrayList(myData.playerList)
        data.start_player = winner_id
        data.yourId = endpointId
        oos.writeObject(data)
        oos.flush()
        oos.close()

        val payload = Payload.fromBytes(bos.toByteArray())
        Nearby.getConnectionsClient(this@BettingActivity).sendPayload(endpointId, payload)
    }
}
