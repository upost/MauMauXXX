/*
 * Copyright (c) 2018. Uwe Post <autor@uwepost.de>. All rights reserved. See licence.txt file for actual license.
 *
 */

package de.uwepost.maumauxxx;

import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.text.WordUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

public class GameActivity extends BaseGameActivity {

    private final static String TAG="MauMau";
    private final static String PLAYER ="player";
    private final static String LISA ="lisa";
    private static final long LISA_PLAY_DELAY_MS = 1500;
    private static final long NEXT_ROUND_DELAY_MS = 3000;
    public static final int PLAYER_CLOTHES = 4;
    public static final int LISA_CLOTHES = 6;
    private static final int CURSE_PROPABILITY = 100;
    private int lisa_won,player_won;
    private MauMau maumau;
    private ViewGroup root;
    private Random rnd = new Random();
    private Handler handler = new Handler();
    private boolean drewThisTurn;
    private boolean showCurse;
    private boolean continueGame;
    private boolean showHappyComment;
    private View draggingView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        addTypeface("atari");
        root = findViewById(R.id.root);
        setTypefaceRecursive(root,"atari");
        findViewById(R.id.choose_herz).setOnClickListener(this::onColorChosen);
        findViewById(R.id.choose_karo).setOnClickListener(this::onColorChosen);
        findViewById(R.id.choose_pik).setOnClickListener(this::onColorChosen);
        findViewById(R.id.choose_kreuz).setOnClickListener(this::onColorChosen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if("start".equalsIgnoreCase(getIntent().getStringExtra("status")) && !continueGame) {
            lisa_won=player_won=0;
            startNewRound();
            continueGame=true;
        } else {

        }
    }

    private void startNewRound() {

        maumau = new MauMau();
        maumau.addPlayer(PLAYER);
        maumau.addPlayer(LISA);
        maumau.init();
        drewThisTurn=false;

        // TEST CODE, removes 6 cards from player's hand
        //for(int i=0; i<6; i++) maumau.getHand(PLAYER).remove(0);
        Log.d(TAG, "new round, start player " + maumau.getCurrentPlayer());
        findViewById(R.id.lisa).startAnimation(AnimationUtils.loadAnimation(this,R.anim.fadein));
        updateUI();
        if(maumau.getCurrentPlayer().equals(LISA)) handler.postDelayed(this::lisaPlays,LISA_PLAY_DELAY_MS);

    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(this::lisaPlays);
        handler.removeCallbacks(this::lisaPlaysAfterDraw);
        handler.removeCallbacks(this::startNewRound);
        handler.removeCallbacks(this::updateUI);
        super.onPause();
    }

    private void lisaPlays() {
        Log.d(TAG, "Lisa plays...");
        MauMauBot lisa = new MauMauBot(LISA);
        Pair<MauMauBot.Action, MauMau.Card> play = lisa.play(maumau,true);
        if(play.first.equals(MauMauBot.Action.draw)) {
            showBubbleText(getString(R.string.i_draw));
            handler.postDelayed(this::lisaPlaysAfterDraw,LISA_PLAY_DELAY_MS);
            findViewById(R.id.deck).startAnimation(AnimationUtils.loadAnimation(this,R.anim.to_top));
        } else {
            lisaPlayedCard(play);
        }
    }

    private void lisaPlayedCard(Pair<MauMauBot.Action, MauMau.Card> play) {
        if(maumau.getHand(LISA).size()==1)
            showBubbleText(getString(R.string.last_card));
        else
            showBubbleText(WordUtils.capitalizeFully( play.second.name().replace("_"," ")));
        playCard(LISA,play.second);
        drewThisTurn=false;
        updatePile();
        findViewById(R.id.pile).startAnimation(AnimationUtils.loadAnimation(this,R.anim.from_top));
    }

    private void lisaPlaysAfterDraw() {
        MauMauBot lisa = new MauMauBot(LISA);
        Pair<MauMauBot.Action, MauMau.Card> play = lisa.play(maumau,false);
        if(play==null) {
            // lisa could not play
            showBubbleText(getString(R.string.i_pass));
            maumau.nextPlayer();
            handler.postDelayed(this::continuePlay,LISA_PLAY_DELAY_MS);
        } else {
            lisaPlayedCard(play);
        }

    }

    private void continuePlay() {
        drewThisTurn=false;
        updateUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        runOnUiThread( this::updateUI );
    }


    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("lisa_won",lisa_won);
        state.putInt("player_won",player_won);
        state.putBoolean("drewThisTurn",drewThisTurn);
        state.putSerializable("maumau",maumau);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        lisa_won = savedInstanceState.getInt("lisa_won");
        player_won = savedInstanceState.getInt("player_won");
        drewThisTurn = savedInstanceState.getBoolean("drewThisTurn");
        maumau = (MauMau) savedInstanceState.getSerializable("maumau");

        updateUI();
    }


    private void updateUI() {
        updateCardsInfo();

        updatePile();

        findViewById(R.id.pile).setOnDragListener(this::onDragListener);
        root.setOnDragListener(this::onDragListener);

        // deck
        boolean playersTurn = maumau.getCurrentPlayer().equals(PLAYER);
        if(playersTurn && !drewThisTurn)
            findViewById(R.id.deck).setOnTouchListener(this::onDeckTouchListener);
        else
            findViewById(R.id.deck).setOnTouchListener(null);

        // lisa
        updateLisa();

        updatePlayerCards();

        if(playersTurn) {
            if(maumau.getHand(LISA).size()==1)
                showBubbleText(getString(R.string.last_card));
            else {
                if(showCurse) {
                    String[] comment = getResources().getStringArray(R.array.curses);
                    showBubbleText(comment[rnd.nextInt(comment.length)]);
                } else if(showHappyComment) {
                    String[] comment = getResources().getStringArray(R.array.happy);
                    showBubbleText(comment[rnd.nextInt(comment.length)]);
                } else {
                    showBubbleText(getString(R.string.your_turn));
                }
            }
        } else {
            if(showCurse) {
                String[] curses = getResources().getStringArray(R.array.curses);
                showBubbleText(curses[rnd.nextInt(curses.length)]);
            } else if(showHappyComment) {
                String[] comment = getResources().getStringArray(R.array.happy);
                showBubbleText(comment[rnd.nextInt(comment.length)]);
            }else {
                showBubbleText(getString(R.string.my_turn));
            }
        }
        showCurse=false;
        showHappyComment=false;
    }

    private void updateLisa() {
        int id = getResources().getIdentifier("lisa"+ (1 + player_won),"mipmap",BuildConfig.APPLICATION_ID);
        ((ImageView)findViewById(R.id.lisa)).setImageResource(id);
    }

    private void updateCardsInfo() {
        // cards info
        ((TextView)findViewById(R.id.cards_info)).setText(getString(R.string.cards_info,maumau.getHand(LISA).size()));
    }

    private void updatePlayerCards() {
        // remove player cards
        Collection<View> remove=new HashSet<>();
        for(int i=0; i<root.getChildCount(); i++) {
            if(root.getChildAt(i).getTag(R.id.TAG_CARD)!=null) {
                remove.add(root.getChildAt(i));
            }
        }
        for(View v : remove) {
            root.removeView(v);
        }
        // add player cards
        int i=0;
        for(MauMau.Card c : maumau.getHand(PLAYER)) {
            ImageView iv = new ImageView(this);
            fillCardFace(iv,c);
            int w = root.getWidth()/5;
            int h = w*4/3;
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w,h, Gravity.BOTTOM|Gravity.LEFT);
            lp.setMargins(i*w/3 , 0,0,-3*h/4);
            iv.setTag(R.id.TAG_CARD,c);
            if(maumau.getCurrentPlayer().equals(PLAYER))
                iv.setOnTouchListener(this::onCardInHandTouchListener);

            //iv.setOnLongClickListener(this::onLongClick);
            root.addView(iv,lp);
            i++;
        }
    }

    private void updatePile() {
        // pile
        // add card beneath topmost if size>1
        findViewById(R.id.pile_beneath).setVisibility(maumau.getPileSize()>1 ? View.VISIBLE : View.INVISIBLE);
        if(maumau.getPileSize()>1)
            fillCardFace(findViewById(R.id.pile_beneath),maumau.getPile().get(maumau.getPile().size()-2));
        fillCardFace(findViewById(R.id.pile),maumau.getPileTopCard());
    }

    private boolean onDragListener(View droppedOnView, DragEvent event) {


        if(draggingView!=null) {

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    if(droppedOnView.getId()==R.id.pile) {
                        MauMau.Card card = (MauMau.Card) draggingView.getTag(R.id.TAG_CARD);
                        if (card == null || !maumau.canPlayCard(card)) {
                            // cannot play this card, show something
                            handler.post(()->{droppedOnView.setBackgroundColor(getResources().getColor(R.color.lred));});
                        }
                    }
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    // reset whatever has been done on DRAG_ENTERED
                    handler.post(()->{droppedOnView.setBackgroundColor(getResources().getColor(R.color.transparent));});
                    return true;

                case DragEvent.ACTION_DROP:
                    Log.d(TAG, "action_drop");
                    if( droppedOnView.getId()==R.id.pile) {
                        Log.d(TAG, "dropped on pile");
                        MauMau.Card card = (MauMau.Card) draggingView.getTag(R.id.TAG_CARD);
                        if(maumau.canPlayCard(card)) {
                            playCard(PLAYER,card);
                        } else {
                            handler.post(this::lisaSaysNo);
                        }
                    }  else {
                        if(draggingView.getId()==R.id.deck && (droppedOnView==root||droppedOnView==findViewById(R.id.lisa))) {
                            // drawn from deck
                            playerDrawsCard();
                        }


                        // not played, return card to hand
                        if(draggingView!=null)
                            handler.post(()->{draggingView.setVisibility(View.VISIBLE);});
                    }
                    if(droppedOnView!=null && droppedOnView!=root)
                        handler.post(()->{droppedOnView.setBackgroundColor(getResources().getColor(R.color.transparent));});
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
//                    Log.d(TAG, "drag_ended");
//                    if(draggingView!=null) {
//                        if(draggingView.getId()==R.id.deck && (droppedOnView==root||droppedOnView==findViewById(R.id.lisa))) {
//                            // drawn from deck
//                            playerDrawsCard();
//                        }
//                        handler.post(() -> {
//                            draggingView.setVisibility(View.VISIBLE);
//                        });
//                    }


                    return true;

                default:
                    break;
            }
        }

        return true;
    }

    private void lisaSaysNo() {
        String[] comment = getResources().getStringArray(R.array.card_not_allowed);
        showBubbleText(comment[rnd.nextInt(comment.length)]);
    }

    private void playerDrawsCard() {
        MauMau.Card card = maumau.drawCard(PLAYER);
        Log.d(TAG,"player draws " + card);
        drewThisTurn=true;
        if(maumau.canPlayAnyCard(PLAYER)) {
            // player may play card but NOT draw again
            Log.d(TAG,"player can play (drawn) card...");
            handler.post(this::updateUI);
        } else {
            // player cannot play, pass to lisa
            maumau.nextPlayer();
            handler.post(this::updateUI);
            if (maumau.getCurrentPlayer().equals(LISA))
                handler.postDelayed(this::lisaPlays, LISA_PLAY_DELAY_MS);
        }
    }

    private void playCard(String name, MauMau.Card card) {
        Log.d(TAG, name+" plays " + card);
        MauMau.SpecialEvent specialEvent = maumau.playCard(name, card);
        maumau.nextPlayer();
        if(specialEvent.equals(MauMau.SpecialEvent.next_player_skipped)) {
            Log.d(TAG, name+" skipped: " + maumau.getCurrentPlayer());
            if(LISA.equals(maumau.getCurrentPlayer())) triggerCurse(); //else triggerHappyComment(); // won't work because Lisa updates before
            maumau.nextPlayer();
            drewThisTurn=false;
        } else if(specialEvent.equals(MauMau.SpecialEvent.next_player_draws_2)) {
            Log.d(TAG, name+" draws 2: " + maumau.getCurrentPlayer());
            maumau.drawCard(maumau.getCurrentPlayer());
            maumau.drawCard(maumau.getCurrentPlayer());
            animate2DeckCards(maumau.getCurrentPlayer());
            if(LISA.equals(maumau.getCurrentPlayer())) triggerCurse(); else triggerHappyComment();
        } else if(specialEvent.equals(MauMau.SpecialEvent.choose_color)) {
            if(LISA.equals(maumau.getCurrentPlayer())) {
                // player gets to choose, Lisa must play that color
                handler.post(this::showColorChooser);
            }
            else {
                // Lisa may choose
                MauMauBot bot = new MauMauBot(LISA);
                maumau.setWishedColor(bot.wishColor(maumau));
                drewThisTurn=false;
                handler.post(this::updateUI);
                handler.postDelayed(()->{showBubbleText(getString(R.string.i_wish, WordUtils.capitalizeFully(maumau.getWishedColor())));},LISA_PLAY_DELAY_MS/2);
            }
            return;
        }

        // check if game ended
        if(maumau.hasWon(PLAYER)) {
            Log.d(TAG,"player wins");
            player_won++;

            if(player_won < LISA_CLOTHES) {
                handler.removeCallbacks(null);
                handler.postDelayed(this::startNewRound, NEXT_ROUND_DELAY_MS);
            } else {
                updateLisa();
            }
            // Lisa's text has to be delayed because we posted an updateUI above
            handler.post(()->{showBubbleText(getString(getResources().getIdentifier("undress_lisa_"+player_won,"string",BuildConfig.APPLICATION_ID)));});
            handler.post(this::updatePlayerCards);
            return;
        }
        if(maumau.hasWon(LISA)) {
            Log.d(TAG,"Lisa wins");
            lisa_won++;
            showBubbleText(getString(getResources().getIdentifier("undress_user_"+lisa_won,"string",BuildConfig.APPLICATION_ID)));
            handler.removeCallbacks(null);
            if(lisa_won< PLAYER_CLOTHES) {
                handler.postDelayed(this::startNewRound, NEXT_ROUND_DELAY_MS);
            }
            handler.post(this::updateCardsInfo);
            return;
        }

        if(LISA.equals(name))
            handler.postDelayed(this::updateUI,LISA_PLAY_DELAY_MS);
        else
            handler.post(this::updateUI);

        // make Lisa do her turn
        if(maumau.getCurrentPlayer().equals(LISA)) handler.postDelayed(this::lisaPlays,LISA_PLAY_DELAY_MS);
    }

    private void triggerHappyComment() {
        if(rnd.nextInt(100)<CURSE_PROPABILITY) {
            showHappyComment=true;
        }
    }

    private void triggerCurse() {
        if(rnd.nextInt(100)<CURSE_PROPABILITY) {
            showCurse=true;
        }
    }

    private void showColorChooser() {
        updateUI();
        showBubbleText(getString(R.string.wish_color));
        findViewById(R.id.color_chooser).setVisibility(View.VISIBLE);

    }

    private void onColorChosen(View view) {
        findViewById(R.id.color_chooser).setVisibility(View.GONE);
        switch(view.getId()) {
            case R.id.choose_herz: maumau.setWishedColor(MauMau.HERZ); break;
            case R.id.choose_karo: maumau.setWishedColor(MauMau.KARO); break;
            case R.id.choose_pik: maumau.setWishedColor(MauMau.PIK); break;
            case R.id.choose_kreuz: maumau.setWishedColor(MauMau.KREUZ); break;
        }
        Log.d(TAG,"player chose " + maumau.getWishedColor());

        // let Lisa continue
        if(maumau.getCurrentPlayer().equals(LISA)) handler.postDelayed(this::lisaPlays,LISA_PLAY_DELAY_MS);
    }



    private void animate2DeckCards(String player) {
        // shows two draw card effects for top deck card
        Animation animation = AnimationUtils.loadAnimation(this, LISA.equals(player) ? R.anim.to_top : R.anim.to_bottom);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation a) {
                animation.setAnimationListener(new SimpleAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(LISA.equals(player))
                            updateCardsInfo();
                        else
                            updatePlayerCards();
                    }
                });
                findViewById(R.id.deck).startAnimation(animation);
            }
        });
        findViewById(R.id.deck).startAnimation(animation);

    }

    private boolean onCardInHandTouchListener(View view, MotionEvent motionEvent) {
        MauMau.Card card = (MauMau.Card) view.getTag(R.id.TAG_CARD);
        if(card !=null) {
            view.setVisibility(View.INVISIBLE);
            ClipData data = ClipData.newPlainText("card", card.name());
            draggingView=view;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, new View.DragShadowBuilder(view), view, 0);
            } else {
                view.startDrag(data, new View.DragShadowBuilder(view), view, 0);
            }

            return true;

        }
        return false;
    }


    private boolean onDeckTouchListener(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
            findViewById(R.id.deck).setOnTouchListener(null);
            ClipData data = ClipData.newPlainText("card", "deck");
            draggingView=findViewById(R.id.deck);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, new View.DragShadowBuilder(view), view, 0);
            } else   {
                view.startDrag(data, new View.DragShadowBuilder(view), view, 0);
            }
            return true;
        }
        return false;
    }


    private void fillCardFace(ImageView imageView, MauMau.Card card) {
        int id = getResources().getIdentifier("card_"+card.name(),"mipmap",BuildConfig.APPLICATION_ID);
        imageView.setImageResource(id);
    }

    private void showBubbleText(String txt) {
        findViewById(R.id.bubble).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.bubble_text)).setText(txt);

    }


}
