package com.example.bettingservice.client

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bettingservice.Host.Player
import com.example.bettingservice.R
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlinx.android.synthetic.main.player_item_host.view.*
import java.util.*


interface itemActionListener {
    fun onItemMoved(from : Int, to : Int)
}

class ClientRoomAdpater (
    private val context: Context,
    private val list: ArrayList<Player>
) :
    RecyclerView.Adapter<ClientRoomAdpater.MyViewHolder>() {

    private val TAG = "RoomRecyclerAdapter"

    private var mContext: Context = context
    var player_list = list


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val LinearView = LayoutInflater.from(mContext) //parent.context
            .inflate(R.layout.player_item_host, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(LinearView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.itemView.player_name.text = player_list[position].getname()
        holder.itemView.initial_budget.text = player_list[position].getbudget().toString()
        holder.itemView.budget_text.visibility = View.VISIBLE
        holder.itemView.initial_budget.visibility = View.VISIBLE
        holder.itemView.accept_player.visibility = View.GONE
        holder.itemView.reject_player.visibility = View.GONE
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = player_list.size

    fun addItem(item: Player) {
        player_list.add(item)
        notifyItemInserted(itemCount-1)
        //notifyDataSetChanged()
        Log.wtf(TAG, "added item and notified with count ${itemCount}")
    }

    fun update_budget(budget: Int, id: String) {
        player_list.forEachIndexed { index, player ->
            if (player.getId() == id) {
                player.setbudget(budget)
                notifyItemChanged(index)
            }
        }
    }

    fun clearData() {
        player_list.clear()
        notifyDataSetChanged()
    }

}