/*
 * Created on 28.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventDispatcher;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * https://stackoverflow.com/q/52031154/203657
 * En/disable showing of tooltip globally.
 * 
 * OP approach: add window handler to custom tooltip which consumes
 * the event if not enabled 
 * expectation: showing prevented if consumed - why? not specified
 * filters of input events on controls _do_ prevent the event from reaching the control
 * 
 * Anyway, solution is to not use windowEvent but override
 * showWindow(window, x, y)
 * 
 */
public class TooltipDisableDemo extends Application {
    public static boolean SHOW_TOOLTIP = false;


    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
        Scene sc = new Scene(root, 700, 500);
        stage.setScene(sc);
        stage.setTitle("Tooltips Disable Demo");
        stage.show();

        CheckBox showToolTip = new CheckBox("Enable tooltip displaying");
        showToolTip.selectedProperty().addListener((obs, old, show) -> SHOW_TOOLTIP = show);
        showToolTip.setSelected(true);

        TabPane tabPane = new TabPane();
        for (int i = 1; i < 11; i++) {
            Tab tab = new Tab("Tab " + i);
            tab.setClosable(false);
            FlowPane fp = new FlowPane();
            fp.setPadding(new Insets(10));
            fp.setHgap(15);
            fp.setVgap(15);
            for (int j = 1; j < 30; j++) {
                StackPane sp = new StackPane();
                sp.setStyle("-fx-background-color: gray");
                sp.setPadding(new Insets(0,5,0,5));
                sp.getChildren().add(new Label("SP T"+i+" - "+j));
                Tooltip.install(sp, new CustomTooltip("This is stack pane " + j + " in Tab " + i));

                Button btn = new Button("Button T"+i+" - "+j);
                btn.setTooltip(new CustomTooltip("This is button " + j + " in Tab " + i));
                fp.getChildren().addAll(sp, btn);
            }
            tab.setContent(fp);
            tabPane.getTabs().add(tab);
        }
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        VBox vb = new VBox();
        vb.setSpacing(10);
        vb.setPadding(new Insets(10));
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList("one", "two"));
        // equivalent on control
        combo.addEventFilter(ComboBox.ON_SHOWING, e -> {
            if(SHOW_TOOLTIP)
                e.consume();
        });
        TextField field = new TextField("dummy");
        field.addEventFilter(KeyEvent.ANY, e -> e.consume());
        vb.getChildren().addAll(showToolTip,tabPane, combo, field);
        root.getChildren().add(vb);
        
        stage.addEventFilter(WindowEvent.WINDOW_SHOWING, e->{
            LOG.info("window: " + e);
            if (!SHOW_TOOLTIP) {
                e.consume();
            }
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Custom tooltip implementation.
     */
    class CustomTooltip extends Tooltip {

        public CustomTooltip(String txt){
            super(txt);

//            setOnShowing(e -> {
//               if (!SHOW_TOOLTIP) {
//                   e.consume();
//               }
//            });
            // Approach #1
            addEventFilter(WindowEvent.WINDOW_SHOWING, e->{
//                LOG.info("window: " + e);
                if (!SHOW_TOOLTIP) {
                    hide();
                    e.consume();
                }
            });

            // Approach #2
            //setEventDispatcher();
            
            showingProperty().addListener(v -> {
                if (!SHOW_TOOLTIP) hide();
            });
        }

        
        @Override
        public void show(Window owner) {
            LOG.info("show owner window: " + owner);
//            if (!SHOW_TOOLTIP) return;
            super.show(owner);
        }


        @Override
        public void show(Node ownerNode, double anchorX, double anchorY) {
            LOG.info("show owner node at: " + ownerNode);
            super.show(ownerNode, anchorX, anchorY);
        }


        @Override
        public void show(Window ownerWindow, double anchorX, double anchorY) {
            LOG.info("show owner window at: " + ownerWindow);
            if (!SHOW_TOOLTIP) return;
            super.show(ownerWindow, anchorX, anchorY);
        }


        private void setEventDispatcher() {
            final EventDispatcher oed = getEventDispatcher();
            final EventDispatcher ned = (event, tail) -> {
                Event r = null;
                if (!SHOW_TOOLTIP) {
                    event.consume();
                } else{
                    r = oed.dispatchEvent(event, tail);
                }
                return r;
            };
            setEventDispatcher(ned);
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TooltipDisableDemo.class.getName());
}

