package com.example.bettingservice

import com.example.bettingservice.client.RoomsActivity
import android.content.DialogInterface
import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.example.bettingservice.Host.HostRoomActivity
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.create_game_layout.*

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

            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.create_game_layout, null)

            MaterialStyledDialog.Builder(this@MainActivity)
                .setTitle("Create Room")
                .setDescription("Please fill in Room Name, Player Number, and Betting Rounds")
                .setCustomView(itemView)
                .setPositiveText("Create")
                .setNegativeText("Cancel")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setHeaderColor(R.color.header_color)
                .setCancelable(true)
                .setScrollable(true)
                .withDialogAnimation(true)
                .autoDismiss(false)
                .onPositive(
                    MaterialDialog.SingleButtonCallback { _, _ ->
                        val input_room_name = itemView.findViewById<View>(R.id.input_room_name) as EditText
                        val input_player_number = itemView.findViewById<View>(R.id.input_player_number) as EditText
                        val input_betting_round = itemView.findViewById<View>(R.id.input_betting_round) as EditText

                        if (input_room_name.text.toString().isEmpty()) {
                            Toast.makeText(this, "방 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        else if (input_player_number.text.toString().isEmpty()) {
                            Toast.makeText(this, "플레이어 수를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        else if (input_player_number.text.toString().toInt() > 5) {
                            Toast.makeText(this, "플레이어수는 5명 이하이여야 합니다.", Toast.LENGTH_SHORT).show()
                        }
                        else if (input_betting_round.text.toString().isEmpty()) {
                            Toast.makeText(this, "베팅 라운드 수를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        else if (input_betting_round.text.toString().toInt() <= 0) {
                            Toast.makeText(this, "베팅 라운드 수는 1라운드 이상이여야 합니다.", Toast.LENGTH_SHORT)
                                .show()
                        }

                        else {
                            //Start Receiving Player Activity
                            startActivity(Intent(this, HostRoomActivity::class.java))
                        }
                    })
                .onNegative { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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

}
