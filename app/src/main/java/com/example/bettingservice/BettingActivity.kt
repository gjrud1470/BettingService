package com.example.bettingservice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bettingservice.Host.Player
import kotlinx.android.synthetic.main.activity_betting.*

class BettingActivity : AppCompatActivity() {

    private lateinit var adapter : PlayerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_betting)

        adapter = PlayerAdapter(ArrayList<Player>())
        userList.adapter = adapter
        userList.layoutManager = LinearLayoutManager(this)
    }
}
