package com.example.bettingservice;

import com.example.bettingservice.Host.Player;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;

public class PayloadData implements Serializable {

    public Action getFlag() {
        return flag;
    }

    public void setFlag(Action flag) {
        this.flag = flag;
    }

    public Integer getRoundNum() {
        return roundNum;
    }

    public void setRoundNum(Integer roundNum) {
        this.roundNum = roundNum;
    }

    public enum Action {
        ENTER_ROOM_INFO,
        UPDATE_ROOM,
        UPDATE_USER_BUDGET,
        START_GAME,
        UPDATE_GAME,
        SEND_BET_INFO,
        USER_FOLD,
        BROADCAST_WINNER,
        USER_LEFT,
        BROADCAST_GAME_WINNER
    }

    // From Host to Client
    private ArrayList<Player> playerList;
    private Integer pool;
    private Integer toCall;
    private Integer turn;
    private Integer start_player;
    private Integer player_number;
    private Integer totalRound;
    private String yourId;
    private String roomName;
    private Integer roundNum = 1;

    // From Client to Host
    private Integer user_initial_budget;
    private Integer bet;

    private Action flag;


    public ArrayList<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(ArrayList<Player> playerList) {
        this.playerList = playerList;
    }

    public Integer getPool() {
        return pool;
    }

    public void setPool(Integer pool) {
        this.pool = pool;
    }

    public Integer getToCall() {
        return toCall;
    }

    public void setToCall(Integer toCall) {
        this.toCall = toCall;
    }

    public Integer getTurn() {
        return turn;
    }

    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public Integer getTotalRound() {
        return totalRound;
    }

    public void setTotalRound(Integer totalRound) {
        this.totalRound = totalRound;
    }

    public String getYourId() {
        return yourId;
    }

    public void setYourId(String yourId) {
        this.yourId = yourId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Integer getUser_initial_budget() { return user_initial_budget; }

    public void setUser_initial_budget(Integer user_initial_budget) {
        this.user_initial_budget = user_initial_budget;
    }

    public Integer getPlayer_number() { return player_number; }

    public void setPlayer_number(Integer player_number) { this.player_number = player_number; }

    public Integer getStart_player() { return start_player; }

    public void setStart_player(Integer start_player) { this.start_player = start_player; }

    public Integer getBet() { return bet; }

    public void setBet(Integer bet) { this.bet = bet; }
}
