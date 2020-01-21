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

    public enum Action {
        ENTER_ROOM_INFO,
        UPDATE_ROOM,
        START_GAME,
        UPDATE_GAME
    }

    private ArrayList<Player> playerList;
    private Integer pool;
    private Integer toCall;
    private Integer turn;
    private Integer totalRound;
    private String yourId;
    private String roomName;
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
}
