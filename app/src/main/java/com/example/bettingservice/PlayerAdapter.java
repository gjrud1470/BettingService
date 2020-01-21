package com.example.bettingservice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bettingservice.Host.Player;

import java.util.ArrayList;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {

    ArrayList<Player> playerList;
    private int highlight_index = 0;
    private Context mcontext;

    PlayerAdapter(ArrayList<Player> list) { playerList = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        mcontext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.player_item, parent, false);
        PlayerAdapter.ViewHolder vh = new PlayerAdapter.ViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = String.valueOf(position+1) + ". " + playerList.get(position).getname();
        holder.playerName.setText(name);
        holder.playerBudget.setText(String.valueOf(playerList.get(position).getbudget()));

        if (playerList.get(position).getFolded()) {
            holder.cardView.setCardBackgroundColor(mcontext.getResources().getColor(R.color.folded_background));
            holder.playerName.setTextColor(mcontext.getResources().getColor(R.color.folded_text));
            holder.playerBudget.setTextColor(mcontext.getResources().getColor(R.color.folded_text));
            holder.budget_text_item.setTextColor(mcontext.getResources().getColor(R.color.folded_text));
        }
        else if (position == highlight_index) {
            holder.cardView.setCardBackgroundColor(mcontext.getResources().getColor(R.color.highlight));
        }
        else {
            holder.cardView.setCardBackgroundColor(mcontext.getResources().getColor(R.color.button));
            holder.playerName.setTextColor(mcontext.getResources().getColor(R.color.white));
            holder.playerBudget.setTextColor(mcontext.getResources().getColor(R.color.white));
            holder.budget_text_item.setTextColor(mcontext.getResources().getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView playerName;
        private TextView playerBudget;
        private CardView cardView;
        private TextView budget_text_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerName);
            playerBudget = itemView.findViewById(R.id.playerBudget);
            cardView = itemView.findViewById(R.id.card_view);
            budget_text_item = itemView.findViewById(R.id.budget_text_item);
        }
    }

    public void highlight_player(int position) {
        highlight_index = position;
        notifyDataSetChanged();
    }
}
