package com.example.bettingservice.client;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettingservice.R;

import java.util.ArrayList;

import kotlin.Pair;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {

    private ArrayList<Pair<String, String>> roomList;
    private OnItemClkListener mListener = null;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.room_item, parent, false);
        RoomsAdapter.ViewHolder vh = new RoomsAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtView.setText(roomList.get(position).getSecond());
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtView = itemView.findViewById(R.id.roomName);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClk(roomList.get(pos).getFirst());
                        }
                    }
                }
            });
        }
    }

    RoomsAdapter(ArrayList<Pair<String, String>> list) {
        this.roomList = list;
    }

    public interface OnItemClkListener {
        void onItemClk(String endpointId);
    }

    public void setOnItemClkListener(OnItemClkListener listener) { mListener = listener; }
    public void addItem(Pair<String, String> item) {
        roomList.add(item);
        notifyItemChanged(getItemCount() - 1);
    }
    public void clearData(){ roomList.clear(); notifyDataSetChanged(); }
}
