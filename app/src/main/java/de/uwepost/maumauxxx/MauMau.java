/*
 * Copyright (c) 2018. Uwe Post <autor@uwepost.de>. All rights reserved. See licence.txt file for actual license.
 *
 */

package de.uwepost.maumauxxx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MauMau implements Serializable {



    public static final String KARO = "karo";
    public static final String HERZ = "herz";
    public static final String PIK = "pik";
    public static final String KREUZ = "kreuz";

    public enum SpecialEvent { none, next_player_skipped, next_player_draws_2, choose_color};


    public enum Card {karo_7,karo_8,karo_9,karo_10,karo_bube,karo_dame,karo_koenig,karo_ass,
    herz_7,herz_8,herz_9,herz_10,herz_bube,herz_dame,herz_koenig,herz_ass,
    pik_7,pik_8,pik_9,pik_10,pik_bube,pik_dame,pik_koenig,pik_ass,
    kreuz_7,kreuz_8,kreuz_9,kreuz_10,kreuz_bube,kreuz_dame,kreuz_koenig,kreuz_ass};

    private List<String> players=new ArrayList<>();
    private List<Card> deck = new ArrayList<>();
    private Map<String,List<Card>> hands = new HashMap<>();
    private List<Card> pile = new ArrayList<>();

    private String currentPlayer;

    private String wishedColor;

    public void addPlayer(String name) {
        players.add(name);
    }

    public void clearPlayers() {
        players.clear();
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void init() {
        deck.clear();
        deck.addAll(Arrays.asList(Card.values()));
        pile.clear();
        hands.clear();
        for (String p : players) {
            hands.put(p,new ArrayList<>());
        }
        Collections.shuffle(deck);
        for(int i=1; i<=7; i++) {
            for (String p : players) {
                drawCard(p);
            }
        }
        cardToPile();
        Collections.shuffle(players);
        currentPlayer=players.get(0);
    }

    public void cardToPile() {
        Card drawn = deck.remove(0);
        pile.add(drawn);
    }

    public Card drawCard(String player)
    {
        if(deck.isEmpty()) reshuffleDeck();

        Card drawn = deck.remove(0);
        hands.get(player).add(drawn);
        return drawn;
    }

    public List<Card> getHand(String player) {
        return hands.get(player);
    }

    public boolean canPlayCard(Card card) {
        Card topOnPile = getPileTopCard();
        if(wishedColor!=null) {
            return cardColor(card).equals(wishedColor) || isBube(card);
        }
        return (isBube(card) || sameColor(card,topOnPile) || sameValue(card,topOnPile) );
    }

    public boolean canPlayAnyCard(String player) {
        for(Card c  : getHand(player)) {
            if(canPlayCard(c)) return true;
        }
        return false;
    }

    public SpecialEvent playCard(String player, Card card) {
        if(canPlayCard(card)) {
            wishedColor=null;
            pile.add(card);
            getHand(player).remove(card);
            // hand not empty: special event (otherwise game is over, no special event needed)
            if(!getHand(player).isEmpty()) return eventForCard(card);
        }
        return SpecialEvent.none;
    }

    public String getWishedColor() {
        return wishedColor;
    }

    public void setWishedColor(String wishedColor) {
        this.wishedColor = wishedColor;
    }

    private SpecialEvent eventForCard(Card card) {
        if("7".equals(cardValue(card))) {
            // nextplayer draws 2
            return SpecialEvent.next_player_draws_2;
        }
        if("8".equals(cardValue(card))) {
            // nextplayer skipped
            return SpecialEvent.next_player_skipped;
        }
        if("bube".equals(cardValue(card))) {
            // player chooses color
            return SpecialEvent.choose_color;
        }
        return SpecialEvent.none;
    }

    public boolean isLastCard(String player) {
        return getHand(player).size()==1;
    }

    public boolean hasWon(String player) {
        return getHand(player).size()==0;
    }

    public boolean deckHasCards() {
        return !deck.isEmpty();
    }

    public void reshuffleDeck() {
        if(deck.isEmpty()) {
            Card c = getPileTopCard();
            deck.addAll(pile);
            deck.remove(c);
            pile.clear();
            pile.add(c);
        }
    }

    public boolean sameValue(Card card1, Card card2) {
        return cardValue(card1).equals(cardValue(card2));
    }

    public static String cardValue(Card card) {
        int u = card.name().lastIndexOf('_');
        return card.name().substring(u+1);
    }

    public boolean sameColor(Card card1, Card card2) {
        return cardColor(card1).equals(cardColor(card2));
    }

    public static String cardColor(Card card) {
        if(card.name().startsWith(KARO)) return KARO;
        if(card.name().startsWith(HERZ)) return HERZ;
        if(card.name().startsWith(PIK)) return PIK;
        if(card.name().startsWith(KREUZ)) return KREUZ;
        return null;
    }

    public boolean isBube(Card card) {
        return card.name().endsWith("bube");
    }

    public Card getPileTopCard() {
        return pile.get(pile.size()-1);
    }

    public List<Card> getPile() {
        return pile;
    }

    public int getDeckSize() {
        return deck.size();
    }

    public int getPileSize() {
        return pile.size();
    }


    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String nextPlayer() {
        int i = players.indexOf(currentPlayer);
        i++;
        if(i>=players.size()) i=0;
        currentPlayer = players.get(i);
        return currentPlayer;
    }

}
