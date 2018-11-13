/*
 * Copyright (c) 2018. Uwe Post <autor@uwepost.de>. All rights reserved. See licence.txt file for actual license.
 *
 */

package de.uwepost.maumauxxx;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MauMauBot {


    public enum Action {draw,play};

    private String name;

    class AvailablePlay implements Comparable<AvailablePlay> {
        MauMau.Card card;
        int strength;

        public AvailablePlay(MauMau.Card card, int strength) {
            this.card = card;
            this.strength = strength;
        }

        @Override
        public int compareTo(MauMauBot.AvailablePlay availablePlay) {
            return availablePlay.strength-this.strength;
        }
    }

    public MauMauBot(String name) {
        this.name = name;
    }

    public Pair<Action,MauMau.Card> play(MauMau mauMau, boolean mayDraw) {
        List<AvailablePlay> availablePlays = new ArrayList<>();
        List<MauMau.Card> hand = mauMau.getHand(name);
        for(MauMau.Card c : hand) {
            if(mauMau.canPlayCard(c)) availablePlays.add(new AvailablePlay(c,playStrength(c)));
        }
        if(availablePlays.isEmpty()) {
            if(mayDraw) {
                MauMau.Card c = mauMau.drawCard(name);
                return new Pair<>(Action.draw, c);
            } else {
                return null; // no play
            }
        }
        Collections.sort(availablePlays);
        return new Pair<>(Action.play,availablePlays.get(0).card);
    }

    private int playStrength(MauMau.Card c) {
        if("7".equals(MauMau.cardValue(c))) return 10;
        if("8".equals(MauMau.cardValue(c))) return 7;
        if("bube".equals(MauMau.cardValue(c))) return 1;
        return 5;
    }

    public String wishColor(MauMau mauMau) {
        List<MauMau.Card> hand = mauMau.getHand(name);
        List<AvailablePlay> availablePlays = new ArrayList<>();
        for(MauMau.Card c : hand) {
            availablePlays.add(new AvailablePlay(c,playStrength(c)));
        }
        Collections.sort(availablePlays);
        return MauMau.cardColor(availablePlays.get(0).card);
    }


}
