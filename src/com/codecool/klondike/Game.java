package com.codecool.klondike;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import sun.font.TrueTypeFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if(e.getClickCount() == 2 && card.getContainingPile().getPileType() != Pile.PileType.STOCK && card.getContainingPile().getTopCard() == card){
            this.findPlace(card);
        }
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        if (!card.isFaceDown()) {
            Pile activePile = card.getContainingPile();
            if (activePile.getPileType() == Pile.PileType.STOCK)
                return;
            double offsetX = e.getSceneX() - dragStartX;
            double offsetY = e.getSceneY() - dragStartY;

            draggedCards.clear();
            //draggedCards.add(card);
            List<Card> cards = activePile.getCards();
            int cardIndex = cards.indexOf(card);
            for(int i=cardIndex; i < cards.size(); i++){
                Card item = cards.get(i);
                draggedCards.add(item);
                item.getDropShadow().setRadius(20);
                item.getDropShadow().setOffsetX(10);
                item.getDropShadow().setOffsetY(10);

                item.toFront();
                item.setTranslateX(offsetX);
                item.setTranslateY(offsetY);
            }



        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile tableauPile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationPile = getValidIntersectingPile(card, foundationPiles);

        //TODO onMouseReleasedHandler
        int size = draggedCards.size();
        if (foundationPile == null) {
            moveCard(card, tableauPile, size);
        } else {
            moveCard(card, foundationPile, size);
        }
    };

    private void moveCard(Card card, Pile pile, int size) {
        if (pile != null) {
            handleValidMove(card, pile);
            List<Card> cards = card.getContainingPile().getCards();
            autoFlip(pile, size, cards);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
        }
        draggedCards.clear();
    }

    private void autoFlip(Pile pile, int size, List<Card> cards) {
        if (cards.size() > size && pile.getPileType() != Pile.PileType.DISCARD) {
            Card lastNonFlippedCard = cards.get(cards.size() - size - 1);
            if (lastNonFlippedCard.isFaceDown())
                lastNonFlippedCard.flip();
        }
    }

    public boolean isGameWon() {
        for (Pile pile : foundationPiles) {
            if (pile.numOfCards() != 13) {
                return false;
            }
        }
        return true;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();

    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);

    }

    public void refillStockFromDiscard() {
        //TODO refillStockFromDiscard
        List<Card> cards = discardPile.getCards();
        Collections.reverse(cards);
        for (Card card : cards) {
            card.flip();
        }
        stockPile.addCards(cards);
        discardPile.clear();
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(foundationPiles.get(0).getPileType())) {
            if (destPile.isEmpty()) {
                return (card.getRank() == 1);
            } else
                return (destPile.getTopCard().getRank() == card.getRank() - 1 && destPile.getTopCard().getSuit() == card.getSuit());
        } else if (destPile.getPileType().equals(tableauPiles.get(0).getPileType())) {
            if (destPile.isEmpty())
                return (card.getRank() == 13);

            else
                return (destPile.getTopCard().getRank() == card.getRank() + 1 && Card.isOppositeColor(destPile.getTopCard(), card));

        }
        return false;
    }


    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);

            foundationPile.getCards().addListener((ListChangeListener<Card>) (e -> {
                if (isGameWon()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("");
                        alert.setHeaderText(null);
                        alert.setContentText("You Have Won !!!");
                        alert.showAndWait();
                        //removes all cards
                        getChildren().clear();
                        stockPile.clear();
                        tableauPiles.clear();
                        foundationPiles.clear();
                        discardPile.clear();
                        //start a new game
                        deck = Card.createNewDeck();
                        initPiles();
                        dealCards();
                    });
                }
            }));


            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Collections.shuffle(deck);
        Iterator<Card> deckIterator = deck.iterator();
        Iterator<Pile> tableauIterator = tableauPiles.iterator();
        int tableauSize = 1;
        while (tableauIterator.hasNext()) {
            Pile tableau = tableauIterator.next();
            for (int i = 0; i < tableauSize; i++) {
                Card card = deckIterator.next();
                tableau.addCard(card);
                addMouseEventHandlers(card);
                if (i == tableauSize - 1) {
                    card.flip();
                }
                getChildren().add(card);
            }
            tableauSize++;
        }


        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    private void findPlace(Card card){
        Iterator<Pile> iterator = foundationPiles.iterator();
        while (iterator.hasNext()){
            Pile pile = iterator.next();
            if(this.isMoveValid(card, pile)) {
                draggedCards.add(card);
                card.toFront();
                handleValidMove(card, pile);
                List<Card> cards = card.getContainingPile().getCards();
                autoFlip(pile, 1, cards);
                break;
            }
        }

    }

}
