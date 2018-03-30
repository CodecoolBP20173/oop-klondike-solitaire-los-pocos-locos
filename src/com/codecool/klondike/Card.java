package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suit suit;
    private Rank rank;
    private Colour color;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;


    public Card(Suit suit, Rank rank, Colour color, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.color = color;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Colour getColor() { return color; }

    public boolean isFaceDown() {
        return faceDown;
    }

    private String getShortName() {
        return "S" + suit.getValue() + "R" + rank.getValue();
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + " of " + "Suit" + suit;
    }

    public static Colour translateSuitToColor(Suit cardSuit) {
        Colour cardColor;
        if (cardSuit == Suit.HEARTS || cardSuit == Suit.DIAMONDS ) {
            cardColor = Colour.RED;
        }
        else {
            cardColor = Colour.BLACK;
            }
        return cardColor;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        return card1.getColor() != card2.getColor();
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        Colour colour;
        for (Suit suit: Suit.values()) {
            colour = translateSuitToColor(suit);
            for (Rank rank: Rank.values()) {
                result.add(new Card(suit, rank, colour,true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        for (Suit suit: Suit.values()) {
            for (Rank rank: Rank.values()) {
                String cardName = suit.toString().toLowerCase() + rank.getValue();
                String cardId = "S" + suit.getValue() + "R" + rank.getValue();
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }
    public enum Suit {
        HEARTS(1), DIAMONDS(2), SPADES(3), CLUBS(4);

        public int getValue() { return value; }

        private final int value;

        Suit(int value){
            this.value = value;
        }
    }

    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13), ACE(1);

        public int getValue() { return value; }

        private final int value;

        Rank(int value){
            this.value = value;
        }
    }

    public enum Colour {
        RED, BLACK
    }

}
