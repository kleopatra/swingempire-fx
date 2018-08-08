/*
 * Created on 08.08.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Timing of memory game.
 * https://stackoverflow.com/q/51713357/203657
 * 
 * answered just with barest change
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MemoryMatchingGame extends Application{
    // This is to save a reference for the first card to use in comparison
    
//    private CardModel firstCard;
//    private CardModel secondCard;

    @Override
    public void start(Stage primaryStage) throws Exception {

        ArrayList<Card> cards = new ArrayList<Card>();
        
        ArrayList<Button> cardButtons = new ArrayList<>();
        ArrayList<CardModel> cardModels = new ArrayList<>();
        
        for(int i=1; i<=8 ; i++) {                                            // This for loop will add each image twice to the array list
            cards.add(new Card("" + i));
            cards.add(new Card("" + i));
            
//            CardModel cm1 = new CardModel("" + i);
//            cardModels.add(cm1);
//            Button b1 = new Button();
//            b1.textProperty().bind(cm1.openTextProperty());
//            b1.disableProperty().bind(cm1.disposedProperty());
//            b1.setOnAction(e -> openCard(b1));
//            
//            CardModel cm2 = new CardModel("" + i);
//            cardModels.add(cm2);
//            Button b2 = new Button();
//            b2.textProperty().bind(cm2.openTextProperty());
//            b2.disableProperty().bind(cm2.disposedProperty());
//            
//            cardModels.add(cm1);
//            cardModels.add(cm2);
//            
//            cardButtons.add(b1);
//            cardButtons.add(b2);
        }
        Collections.shuffle(cards);                                               // Shuffling the deck of cards


        primaryStage.setTitle("Memory Matching Game");
        HBox hb = new HBox();

        VBox firstColoumn = new VBox();
            for(int i=0; i<4; i++) 
                firstColoumn.getChildren().add(cards.get(i));
        VBox secondColoumn = new VBox();
            for(int i=4; i<8; i++) 
                secondColoumn.getChildren().add(cards.get(i));
        VBox thirdColoumn = new VBox();
            for(int i=8; i<12; i++) 
                thirdColoumn.getChildren().add(cards.get(i));
        VBox fourthColoumn = new VBox();
            for(int i=12; i<16; i++) 
                fourthColoumn.getChildren().add(cards.get(i));

        hb.getChildren().addAll(firstColoumn, secondColoumn, thirdColoumn, fourthColoumn);

        Scene scene = new Scene(hb, 460, 450);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
    
    private Card firstCard; 
    private Card secondCard;

    private Timeline holdTimer = new Timeline(new KeyFrame(
            Duration.seconds(2),  e -> closeCards()));
    
    public void closeCards() {
        if (firstCard == null || secondCard == null) {
            System.out.println("error!!");
        }
        if (firstCard.isEqual(secondCard)) {
            System.out.println("success");
            firstCard.setDisable(true);
            secondCard.setDisable(true);
            firstCard = null;
            secondCard = null;
        } else {
            firstCard.close();
            secondCard.close();
            firstCard = null;
            secondCard = null;
        }
    }
    
    public void openCard(Card card) {
        if (card.isCardOpen()) return;
        if (holdTimer.getStatus() == Status.RUNNING) return;
        if (firstCard ==  null) {
            firstCard = card;
            card.open();
        } else if (secondCard == null) {
            secondCard = card;
            secondCard.open();
            holdTimer.playFromStart();
        }
    }
    
    public static class CardModel {
        private String text;
        private ReadOnlyStringWrapper openText;
        private ReadOnlyBooleanWrapper open;
        private ReadOnlyBooleanWrapper disposed;
        
        public CardModel(String text) {
            this.text = text;
            openText = new ReadOnlyStringWrapper(this, "openText", null);
            open = new ReadOnlyBooleanWrapper(this, "open", false);
            disposed = new ReadOnlyBooleanWrapper(this, "disposed", false);
        }
        
        public void open() {
            open.set(true);
            openText.set(text);
        }
        
        public void close() {
            open.set(false);
            openText.set(null);
        }
        
        public void dispose() {
            if (!open.get()) {
                System.out.println("error - must be open to dispose");
                return;
            }
            disposed.set(true);
        }
        public boolean isEqual(CardModel other) {
            if (other == null) return false;
            return other.text.equals(this.text);
        }
        
        public ReadOnlyStringProperty openTextProperty() {
            return openText.getReadOnlyProperty();
        }
        
        public ReadOnlyBooleanProperty openProperty() {
            return open.getReadOnlyProperty();
        }
        
        public ReadOnlyBooleanProperty disposedProperty() {
            return disposed.getReadOnlyProperty();
        }
        
    }
    
    // Don't extend Controls! Instead configure them as needed
    private class Card extends Button {
        private String imageLocation;       // To store the destination of the image
       // private Image img;                  // To store a reference of the image to be used when setting graphic on a button

        public Card(String imageLocation) throws FileNotFoundException {
            this.imageLocation = imageLocation;
            setPrefSize(150, 150);
            setOnAction(e -> openCard(this));
        }

        public void close() {
            setText("");
        }
        
        public void open() {
            setText(imageLocation);
            System.out.println("Open");
        }

        public boolean isCardOpen() {
            return getText() !=  null && getText().length() > 0;//this.getGraphic()!=null;
        }

        private boolean isEqual(Card selectedCard) {
            if (selectedCard == null) return false;
            return this.imageLocation.equals(selectedCard.imageLocation);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


}

