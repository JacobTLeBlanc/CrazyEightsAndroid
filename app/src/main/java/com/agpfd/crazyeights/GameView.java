package com.agpfd.crazyeights;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameView extends View {

    private Context myContext;
    private int scaledCardW;
    private int scaledCardH;
    private Bitmap cardBack;

    // Cards
    private List<Card> deck = new ArrayList<Card>();
    private List<Card> myHand = new ArrayList<Card>();
    private List<Card> oppHand = new ArrayList<Card>();
    private List<Card> discardPile = new ArrayList<Card>();

    // Keep track of screen width/height
    private int screenW;
    private int screenH;

    // Scale to density of device
    private float scale;

    // Paint for text
    private Paint whitePaint;

    // Scores
    private int oppScore;
    private int myScore;
    private int scoreThisHand;

    // Keep track of turn
    private boolean myTurn;

    // Moving cards
    private int movingCardIdx = -1;
    private int movingX;
    private int movingY;

    // Valid plays
    private int validRank = 8;
    private int validSuit = 0;

    // Arrow if more than 7 cards
    private Bitmap nextCardButton;

    // Computer
    private ComputerPlayer computerPlayer = new ComputerPlayer();

    public GameView(Context context) {
        super(context);

        // Store context
        myContext = context;

        // Keep track of density
        scale = myContext.getResources().getDisplayMetrics().density;

        // Set paint properties
        whitePaint = new Paint();
        whitePaint.setAntiAlias(true);
        whitePaint.setColor(Color.WHITE);
        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setTextAlign(Paint.Align.LEFT);
        whitePaint.setTextSize(scale * 15);

        // Randomly choose turn
        myTurn = new Random().nextBoolean();
        if (!myTurn) {
            makeComputerPlay();
        }

    }

    // Called after constructor but before anything is drawn
    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenW = w;
        screenH = h;

        // Load back of card bitmap
        Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.card_back);
        scaledCardW = (int) (screenW / 8);
        scaledCardH = (int) (scaledCardW * 1.28);
        cardBack = Bitmap.createScaledBitmap(tempBitmap, scaledCardW, scaledCardH, false);

        // Load arrow
        nextCardButton = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_next);

        // Create deck
        initCards();

        // Deal cards
        dealCards();
        drawCard(discardPile);

        validSuit = discardPile.get(0).getSuit();
        validRank = discardPile.get(0).getRank();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw scores
        canvas.drawText("Computer Score: " + oppScore, 10, whitePaint.getTextSize() + 20, whitePaint);
        canvas.drawText("My Score " + myScore, 10, screenH - whitePaint.getTextSize() - 10, whitePaint);

        // Draw opponents hand
        for (int i = 0; i < oppHand.size(); i++) {
            canvas.drawBitmap(cardBack, i * (scale * 5), whitePaint.getTextSize() + (50 * scale), null);
        }

        // Draw deck
        canvas.drawBitmap(cardBack, (screenW / 2) - cardBack.getWidth() - 10, (screenH / 2) - (cardBack.getHeight() / 2), null);

        // Draw Discard Pile
        if (!discardPile.isEmpty()) {
            canvas.drawBitmap(discardPile.get(0).getBitmap(), (screenW / 2) + 10, (screenH / 2) - (cardBack.getHeight() / 2), null);
        }

        // Draw myHand
        if (myHand.size() > 7) {
            canvas.drawBitmap(nextCardButton,
                    (screenW - nextCardButton.getWidth() - (30 * scale)),
                    (screenH - nextCardButton.getHeight() - scaledCardH - (90 * scale)),
                    null);
        }
        for (int i = 0; i < myHand.size(); i++) {
            if (i == movingCardIdx) {
                canvas.drawBitmap(myHand.get(i).getBitmap(),
                        movingX,
                        movingY,
                        null);
            } else {
                if (i < 7) {
                    canvas.drawBitmap(myHand.get(i).getBitmap(),
                            i * (scaledCardW + 5),
                            screenH - scaledCardH - whitePaint.getTextSize() - (50 * scale),
                            null);
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();
        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch(eventaction) {

            case MotionEvent.ACTION_DOWN:
                // Check if player is moving a card
                if (myTurn) {
                    for (int i = 0; i < myHand.size(); i++) {
                        if (X > i * (scaledCardW + 5)               &&
                            X < i * (scaledCardW + 5) + scaledCardW &&
                            Y > screenH - scaledCardH - whitePaint.getTextSize() - (50 * scale)) {
                            movingCardIdx = i;
                            movingX = X - (int) (30*scale);
                            movingY = Y - (int) (90*scale);
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_MOVE:
                movingX = X - (int) (30*scale);
                movingY = Y - (int) (90*scale);
                break;

            case MotionEvent.ACTION_UP:
                // Check if player dropped card in center
                if (movingCardIdx > -1 &&
                    X > (screenW / 2) - (100 * scale) &&
                    X < (screenW / 2) + (100 * scale) &&
                    Y > (screenH / 2) - (100 * scale) &&
                    Y < (screenH / 2) + (100 * scale) &&
                    (myHand.get(movingCardIdx).getRank() == 8 ||
                     myHand.get(movingCardIdx).getRank() == validRank ||
                     myHand.get(movingCardIdx).getSuit() == validSuit)) {
                    validRank = myHand.get(movingCardIdx).getRank();
                    validSuit = myHand.get(movingCardIdx).getSuit();
                    discardPile.add(0, myHand.get(movingCardIdx));
                    myHand.remove(movingCardIdx);
                    if (myHand.isEmpty()) {
                        // End of hand
                        endHand();
                    } else {
                        if (validRank == 8) {
                            showChooseSuitDialog();
                        } else {
                            myTurn = false;
                            makeComputerPlay();
                        }
                    }
                }

                // Check if player touched draw pile
                if (movingCardIdx == -1 &&
                    myTurn &&
                    X > (screenW / 2) - (100 * scale) &&
                    X < (screenW / 2) + (100 * scale) &&
                    Y > (screenH / 2) - (100 * scale) &&
                    Y < (screenH / 2) + (100 * scale)) {
                    if (checkForValidDraw()) {
                        drawCard(myHand);
                    } else {
                        Toast.makeText(myContext, "You have a valid play.", Toast.LENGTH_SHORT).show();
                    }
                }

                // Check if player touched arrow
                if (myHand.size() > 7 &&
                    X > screenW - nextCardButton.getWidth() - (30 * scale) &&
                    Y > screenH - nextCardButton.getHeight() - scaledCardH - (90 * scale) &&
                    Y < screenH - nextCardButton.getHeight() - scaledCardH - (60 * scale)) {
                    Collections.rotate(myHand, 1);
                }

                movingCardIdx = -1;
                break;
        }

        invalidate();
        return true;
    }

    // Create deck of cards and load associated images
    private void initCards() {
        for (int i = 0; i < 4; i++) {
            for (int j = 102; j < 115; j++) {
                // Get id
                int tempId = j + (i * 100);

                // Create card
                Card tempCard = new Card(tempId);

                // Get resource
                int resourceId = getResources().getIdentifier("card"+tempId, "drawable", myContext.getPackageName());
                Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(), resourceId);

                // Sizes of card
                scaledCardW = (int) (screenW / 8);
                scaledCardH = (int) (scaledCardW * 1.28);

                // Scaled Bitmap
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, scaledCardW, scaledCardH, false);

                // Set Bitmap to card and add to deck
                tempCard.setBitmap(scaledBitmap);
                deck.add(tempCard);
            }
        }
    }

    // Deal cards at beginning of game
    private void dealCards() {
        // Shuffle deck
        Collections.shuffle(deck, new Random());

        // Deal 7 cards to each hand
        for (int i = 0; i < 7; i++) {
            drawCard(myHand);
            drawCard(oppHand);
        }
    }

    // Draw card method
    private void drawCard(List<Card> handToDraw) {

        // Check if deck is empty
        if (deck.isEmpty()) {
            for (int i = discardPile.size() - 1; i > 0; i--) {
                // Add all cards not used back to deck except top one
                deck.add(discardPile.get(i));
                discardPile.remove(i);
                // Shuffle deck
                Collections.shuffle(deck, new Random());
            }
        } else {
            // Grab card off top of deck
            handToDraw.add(0, deck.get(0));
            deck.remove(0);
        }
    }

    // When player plays an 8, give him a choice
    private void showChooseSuitDialog() {
        // Dialog that pops up
        final Dialog chooseSuitDialog = new Dialog(myContext);

        // Remove title and makes sure its fullscreen
        chooseSuitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        chooseSuitDialog.setContentView(R.layout.choose_suit_dialog);

        // Spinner to give the player a choice
        final Spinner suitSpinner = (Spinner) chooseSuitDialog.findViewById(R.id.suitSpinner);

        // Get array of suits and use default layout params for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(myContext, R.array.suits, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        suitSpinner.setAdapter(adapter);

        // Get okButton
        Button okButton = (Button) chooseSuitDialog.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Change current suit to one chosen
                validSuit = (suitSpinner.getSelectedItemPosition() + 1) * 100;
                String suitText = "";
                if (validSuit == 100) {
                    suitText = "Diamonds";
                } else if (validSuit == 200) {
                    suitText = "Clubs";
                } else if (validSuit == 300) {
                    suitText = "Hearts";
                } else if (validSuit == 400) {
                    suitText = "Spades";
                }
                chooseSuitDialog.dismiss();
                Toast.makeText(myContext, "You chose " + suitText, Toast.LENGTH_SHORT).show();
                myTurn = false;
                makeComputerPlay();
            }
        });

        chooseSuitDialog.show();
    }

    private boolean checkForValidDraw() {
        boolean canDraw = true;

        // Check hand for valid play
        for (int i = 0; i < myHand.size(); i++) {
            int tempId = myHand.get(i).getId();
            int tempRank = myHand.get(i).getRank();
            int tempSuit = myHand.get(i).getSuit();

            if (validSuit == tempSuit ||
                validRank == tempRank ||
                tempId == 108         ||
                tempId == 208         ||
                tempId == 308         ||
                tempId == 408) {
                canDraw = false;
            }
        }
        return canDraw;
    }

    private void makeComputerPlay() {

        int tempPlay = 0;

        while (tempPlay == 0) {
            tempPlay = computerPlayer.makePlay(oppHand, validSuit, validRank);
            if (tempPlay == 0) {
                drawCard(oppHand);
            }
        }

        if (tempPlay == 108 ||
            tempPlay == 208 ||
            tempPlay == 308 ||
            tempPlay == 408) {
            validRank = 8;
            validSuit = computerPlayer.chooseSuit(oppHand);
            String suitText = "";
            if (validSuit == 100) {
                suitText = "Diamonds";
            } else if (validSuit == 200) {
                suitText = "Clubs";
            } else if (validSuit == 300) {
                suitText = "Hearts";
            } else if (validSuit == 400) {
                suitText = "Spades";
            }
            Toast.makeText(myContext, "Computer chose " + suitText, Toast.LENGTH_SHORT).show();
        } else {
            validSuit = Math.round((tempPlay/100) * 100);
            validRank = tempPlay - validSuit;
        }
        for (int i = 0; i < oppHand.size(); i++) {
            Card tempCard = oppHand.get(i);

            if (tempPlay == tempCard.getId()) {
                discardPile.add(0, oppHand.get(i));
                oppHand.remove(i);
            }
        }
        if (oppHand.isEmpty()) {
            endHand();
        }
        myTurn = true;
    }

    // Update scores at end of hand
    private void updateScores() {
        for (int i = 0; i < myHand.size(); i++) {
            oppScore += myHand.get(i).getScoreValue();
            scoreThisHand += myHand.get(i).getScoreValue();
        }

        for (int i = 0; i < oppHand.size(); i++) {
            myScore += oppHand.get(i).getScoreValue();
            scoreThisHand += oppHand.get(i).getScoreValue();
        }
    }

    private void endHand() {
        // Create new dialog
        final Dialog endHandDialog = new Dialog(myContext);

        // Fullscreen + no title
        endHandDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        endHandDialog.setContentView(R.layout.end_hand);

        // Update scores
        updateScores();

        // Get TextView to update text
        TextView endHandText = (TextView) endHandDialog.findViewById(R.id.endHandText);

        // Check who won and set text
        if (myHand.isEmpty()) {
            if (myScore >= 300) {
                endHandText.setText("You reached " + myScore + " points. You won! Would you like to play again?");
            } else {
                endHandText.setText("You went out and got " + scoreThisHand + " points");
            }
        } else if (oppHand.isEmpty()) {
            if (oppScore >= 300) {
                endHandText.setText("The computer reached " + oppScore + " points. Sorry, you lost. Would you like to play again?");
            } else {
                endHandText.setText("The computer went out and got " + scoreThisHand + " points");
            }
        }

        // Get button by id
        Button nextHandButton = (Button) endHandDialog.findViewById(R.id.nextHandButton);

        // Check if anyone reached goal
        if (oppScore >= 300 || myScore >= 300) {
            nextHandButton.setText("New Game");
        }

        nextHandButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (oppScore >= 300 || myScore >= 300) {
                    myScore = 0;
                    oppScore = 0;
                }
                initNewHand();
                endHandDialog.dismiss();
            }
        });
        endHandDialog.show();
    }

    private void initNewHand() {

        // Reset score of current hand
        scoreThisHand = 0;

        // Whoever won goes first
        if (myHand.isEmpty()) {
            myTurn = true;
        } else if (oppHand.isEmpty()) {
            myTurn = false;
        }

        // Add all cards back to deck
        deck.addAll(discardPile);
        deck.addAll(myHand);
        deck.addAll(oppHand);

        // Remove all cards
        discardPile.clear();
        myHand.clear();
        oppHand.clear();

        // Deal cards
        dealCards();

        // Draw card to discard pile
        drawCard(discardPile);

        // Reset valid plays
        validSuit = discardPile.get(0).getSuit();
        validRank = discardPile.get(0).getRank();

        if (!myTurn) {
            makeComputerPlay();
        }

        invalidate();
    }
}
