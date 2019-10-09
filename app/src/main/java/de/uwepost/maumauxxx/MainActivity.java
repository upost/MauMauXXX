/*
 * Copyright (c) 2018. Uwe Post <autor@uwepost.de>. All rights reserved. See licence.txt file for actual license.
 *
 */

package de.uwepost.maumauxxx;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends BaseGameActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addTypeface("atari");
        setTypefaceRecursive(findViewById(R.id.root),"atari");
        findViewById(R.id.start).setOnClickListener(this::startGame);
        findViewById(R.id.book).setOnClickListener(this::showBook);
        findViewById(R.id.close_popup).setOnClickListener(this::closePopup);
        findViewById(R.id.by_the_way).setOnClickListener(this::showPopup);
        TextView tv = findViewById(R.id.copyright);
        tv.setText("v"+BuildConfig.VERSION_NAME+" - Copyright © 1983");
    }

    private void closePopup(View view) {
        findViewById(R.id.popup).setVisibility(View.GONE);
    }

    private void showPopup(View view) {

        findViewById(R.id.popup).setVisibility(View.VISIBLE);
        findViewById(R.id.popup).startAnimation(AnimationUtils.loadAnimation(this,R.anim.scalein));
    }

    private void showBook(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://upcenter.de/wordpress/fuer-immer-8-bit/"));
        startActivity(browserIntent);
    }

    private void startGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("status","start");
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(this::moveBook);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.postDelayed(this::moveBook,5000);


    }


    private void moveBook() {
        findViewById(R.id.book).startAnimation(AnimationUtils.loadAnimation(this,R.anim.rotatein));
    }
}
