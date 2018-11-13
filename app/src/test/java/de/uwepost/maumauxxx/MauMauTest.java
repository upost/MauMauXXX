/*
 * Copyright (c) 2018. Uwe Post <autor@uwepost.de>. All rights reserved. See licence.txt file for actual license.
 *
 */

package de.uwepost.maumauxxx;

import org.junit.Before;
import org.junit.Test;

public class MauMauTest {

    public static final String ME = "me";
    public static final String YOU = "you";
    private MauMau maumau;

    @Before
    public void setUp() throws Exception {
        maumau = new MauMau();
        
    }

    private void setupPlayers() {
        String player1 = ME;
        String player2 = YOU;
        maumau.clearPlayers();
        maumau.addPlayer(player1);
        maumau.addPlayer(player2);
    }

    @Test
    public void testAddPlayers() {
        setupPlayers();
        assert maumau.getPlayerCount()==2;
    }

    @Test
    public void testInit() {
        setupPlayers();
        maumau.init();
        assert maumau.getHand(ME).size()==7;
        assert maumau.getHand(YOU).size()==7;
        assert maumau.getDeckSize()==32-7-7-1;
        assert maumau.getPileTopCard()!=null;
        assert maumau.getCurrentPlayer()!=null;
    }

    @Test
    public void testDrawCard() {
        setupPlayers();
        maumau.init();
        maumau.drawCard(ME);
        assert maumau.getHand(ME).size()==8;
        assert maumau.getDeckSize()==32-7-7-1-1;
    }

    @Test
    public void testPlayCard() {
        setupPlayers();
        maumau.init();
        for(MauMau.Card c : maumau.getHand(ME)) {
            if(maumau.canPlayCard(c)) {
                maumau.playCard(ME,c);
                assert maumau.getPileTopCard().equals(c);
                assert maumau.getPileSize()==2;
                break;
            }
        }
        assert maumau.getHand(ME).size()==6;
    }

    @Test
    public void testNextPlayer() {
        setupPlayers();
        maumau.init();
        if(maumau.getCurrentPlayer().equals(ME)) {
            assert maumau.nextPlayer().equals(YOU);
        } else {
            assert maumau.nextPlayer().equals(ME);
        }
    }

    @Test
    public void testWishedColor() {
        setupPlayers();
        maumau.init();
        maumau.setWishedColor(MauMau.KREUZ);
        MauMau.Card c = MauMau.Card.kreuz_7;
        assert maumau.canPlayCard(c);
        MauMau.Card not = MauMau.Card.karo_10;
        assert !maumau.canPlayCard(not);

    }
}
