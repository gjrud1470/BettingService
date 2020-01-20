package com.example.bettingservice;

import com.example.bettingservice.Host.Player;

import java.io.Serializable;
import java.util.ArrayList;

public class PayloadData implements Serializable {
    private ArrayList<Player> playerList;
    private Integer pool;
    private Integer toCall;
}
