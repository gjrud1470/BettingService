package com.example.MadWeek2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
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

class RoomRecyclerAdapter(
    private val context: Context,
    private val list: ArrayList<Player>,
    private val listener: itemDragListener
) :
    RecyclerView.Adapter<RoomRecyclerAdapter.MyViewHolder>(), itemActionListener {

    private val TAG = "RoomRecyclerAdapter"

    private var mContext: Context = context
    var player_list = list

    interface itemDragListener {
        fun onStartDrag(viewHolder : MyViewHolder)
    }

    override fun onItemMoved(from: Int, to: Int) {
        if (from == to) {
            return
        }
        val fromItem = player_list.removeAt(from)
        player_list.add(to, fromItem)
        notifyItemMoved(from, to)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            val accept_button = itemView.findViewById<Button>(R.id.accept_player)
            val reject_button = itemView.findViewById<Button>(R.id.reject_player)
            val drag_handle = itemView.findViewById<ImageView>(R.id.drag_handle)

            accept_button.setOnClickListener {
                val position = adapterPosition
                Nearby.getConnectionsClient(mContext)
                    .acceptConnection(player_list[position].getId()!!, payloadCallback)

                accept_button.visibility = View.GONE
                reject_button.visibility = View.GONE
                itemView.findViewById<TextView>(R.id.budget_text).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.initial_budget).visibility = View.VISIBLE
                itemView.findViewById<ImageView>(R.id.drag_handle).visibility = View.VISIBLE
                //notifyItemChanged(position)
            }
            reject_button.setOnClickListener {
                val position = adapterPosition
                Nearby.getConnectionsClient(mContext)
                    .rejectConnection(player_list[position].getId()!!)

                player_list.removeAt(position)
                notifyItemRemoved(position)
            }
            drag_handle.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    listener.onStartDrag(this)
                }
                false
            }
        }
    }

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

        if (player_list[position].getId() == "host") {
            holder.itemView.accept_player.visibility = View.GONE
            holder.itemView.reject_player.visibility = View.GONE
            holder.itemView.budget_text.visibility = View.VISIBLE
            holder.itemView.initial_budget.visibility = View.VISIBLE
            holder.itemView.drag_handle.visibility = View.VISIBLE
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = player_list.size

    fun addItem(item: Player) {
        player_list.add(item)
        notifyItemInserted(itemCount-1)
        //notifyDataSetChanged()
        Log.wtf(TAG, "added item and notified with count ${itemCount}")
    }

    fun clearData() {
        player_list.clear()
        notifyDataSetChanged()
    }

    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }

    }
}