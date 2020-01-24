/*
 * Created on 23.01.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.function.Supplier;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/47643298/203657
 * oldish layout requirement: have wrapping text at exact size that it requires
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class LabelTextWrapping extends Application {

    private Parent createContent() {
        
        TabPane root = new TabPane();
        
        // use reasonable layout for outer/inner parent
        // HBox as root: 
        // behaves as expected without inner pane or reasonable layout
        root.getTabs().add(new Tab("HBox - none", createRoot(HBox::new, () -> null)));
        root.getTabs().add(new Tab("HBox - HBox", createRoot(HBox::new, HBox::new)));
        
        // does not behave as expected with Anchor/Stack as inner
        root.getTabs().add(new Tab("HBox - AnchorPane", createRoot(HBox::new, AnchorPane::new)));
        root.getTabs().add(new Tab("HBox - StackPane", createRoot(HBox::new, StackPane::new)));
        
        // Pane as root: 
        // doesn't much - just size child at its pref, no matter if/what the inner layout is
        root.getTabs().add(new Tab("Pane - none", createRoot(Pane::new, () -> null)));
        root.getTabs().add(new Tab("Pane - AnchorPane", createRoot(Pane::new, AnchorPane::new)));
        root.getTabs().add(new Tab("Pane - StackPane", createRoot(Pane::new, StackPane::new)));
        root.getTabs().add(new Tab("Pane - HBox", createRoot(Pane::new, HBox::new)));
        
        return root;
    }

    private Parent createRoot(Supplier<Pane> rootProvider, Supplier<Pane> innerProvider) {
        Pane rootPane = rootProvider.get();
        Pane inner = innerProvider.get();
        return configureRoot(rootPane, inner);
    }
    
    private Parent configureRoot(Pane rootPane, Pane inner) {
        rootPane.setBackground(new Background(new BackgroundFill(Color.FIREBRICK, 
                CornerRadii.EMPTY, Insets.EMPTY)));
        rootPane.getChildren().add(createInnerContent(inner));
        return rootPane;
    }
    
    private Parent createInnerContent(Pane textParent) {
        Region text = createTextNode();
        // no inner layout
        if (textParent == null) { 
            text.setBackground(new Background(new BackgroundFill(Color.FORESTGREEN, 
                    CornerRadii.EMPTY, Insets.EMPTY)));
            return text;
        } 
        // inner layout: add children, restrict max size of inner layout
        textParent.getChildren().add(text);
        textParent.setBackground(new Background(new BackgroundFill(Color.FORESTGREEN, 
                CornerRadii.EMPTY, Insets.EMPTY)));
        // restrict max size of parent to pref
        textParent.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return textParent;
    }
    
    private Region createTextNode() {
        Label text = new Label("text text text text text text text text text text text");
        // make text wrappable
        text.setWrapText(true);
        System.out.println("max of text? " + text.getMaxHeight());
        return text;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
        stage.setHeight(stage.getHeight() + 200);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
