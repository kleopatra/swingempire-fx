/*
 * Created on 09.03.2019
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55077121/203657
 * 
 * control contextMenu with nav buttons
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ContextMenuNavigation extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = new BorderPane();
        AutoCompleteTextField actf = new AutoCompleteTextField();
        pane.setTop(actf);
        stage.setScene(new Scene(pane));
        stage.show();
    }
    
    public static class AutoCompleteTextField extends TextField {
        private ContextMenu suggestionMenu;

        public AutoCompleteTextField(){
            super();
            suggestionMenu = new ContextMenu();
            for(int i = 0; i<5; i++) {
                CustomMenuItem item = new CustomMenuItem(new Label("Item "+i), true);
                item.setOnAction(event -> {
                    setText("selected");
                    positionCaret(getText().length());
                    suggestionMenu.hide();
                });
                suggestionMenu.getItems().add(item);
            }

            textProperty().addListener((observable, oldValue, newValue) -> {
                if(getText().length()>0){
                    if (!suggestionMenu.isShowing())
                        suggestionMenu.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                } else {
                    suggestionMenu.hide();
                }
            });

            setOnKeyPressed(event -> {
                System.out.println("pressed " + event.getCode());
                switch (event.getCode()) {
                    case DOWN:
                        if(getText().length()>0) {
                            if (!suggestionMenu.isShowing()) {
                                suggestionMenu.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                            }
                            suggestionMenu.getSkin().getNode().lookup(".menu-item").requestFocus();
                        }
                        break;
                }
            });


        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
