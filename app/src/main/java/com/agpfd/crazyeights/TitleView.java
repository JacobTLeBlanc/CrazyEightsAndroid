package com.agpfd.crazyeights;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

// Extends View as we need a custom View
public class TitleView extends View {

    // Declare bitMap object for title graphic
    private Bitmap titleGraphic;

    // Button
    private Bitmap playButtonUp;
    private Bitmap playButtonDown;
    private boolean playButtonPressed;

    // Keep track of screen width/height
    private int screenW;
    private int screenH;

    // Context for intent
    private Context myContext;

    public TitleView(Context context) {
        super(context);

        // Keep track of context
        myContext = context;

        // Get image from resources and load it
        titleGraphic = BitmapFactory.decodeResource(getResources(), R.drawable.title_graphic);

        // Get button from resources and load it
        playButtonUp = BitmapFactory.decodeResource(getResources(), R.drawable.play_button_up);
        playButtonDown = BitmapFactory.decodeResource(getResources(), R.drawable.play_button_down);
    }

    // Called after constructor but before anything is drawn
    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenW = w;
        screenH = h;
    }

    // Override onDraw to add graphics
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw image to screen
        canvas.drawBitmap(titleGraphic, ((screenW - titleGraphic.getWidth()) / 2), 0, null);

        // Draw button states depending if it's pressed
        if (playButtonPressed) {
            canvas.drawBitmap(playButtonDown, ((screenW - playButtonUp.getWidth()) / 2), (int) (screenH * 0.7), null);
        } else {
            canvas.drawBitmap(playButtonUp, ((screenW - playButtonUp.getWidth()) / 2), (int) (screenH * 0.7), null);
        }
    }

    // Logic for where the player touches the screen
    public boolean onTouchEvent (MotionEvent event) {
        int eventaction = event.getAction();
        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventaction) {

            case MotionEvent.ACTION_DOWN:
                // Check if player is pressing button
                if (X > (screenW - playButtonUp.getWidth()) / 2 &&
                    X < ((screenW - playButtonUp.getWidth()) / 2) + playButtonUp.getWidth() &&
                    Y > (int) (screenH * 0.7) &&
                    Y < (int) (screenH * 0.7) + playButtonUp.getHeight()) {
                    playButtonPressed = true;
                }

                break;

            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_UP:
                // When player takes finger off button
                if (playButtonPressed) {
                    // Create intent
                    Intent gameIntent = new Intent(myContext, GameActivity.class);
                    // Use intent to start activity
                    myContext.startActivity(gameIntent);
                }
                playButtonPressed = false;

                break;
        }

        invalidate();
        return true;
    }
}
