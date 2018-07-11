/*
 * Created on 11.07.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;
import java.util.stream.Stream;

import com.sun.javafx.scene.control.ContextMenuContent;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ContextMenuSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Limit # of visible MenuItems.
 * 
 * https://stackoverflow.com/q/51272738/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ContextMenuWithLimitedItems extends Application {

    /**
     * Ideally, a custom ContextMenuContent would have a better implementation
     * than to simply return screenHeight as max. Could support a property
     * visibleRowCount like combo (does the latter have it? yes, checked)
     * 
     * No way, as ContextMenuSkin creates it manually in the constructor and
     * stores in a private final field.
     * 
     * 
     */
    public static class LimitedContextMenuContent extends ContextMenuContent {

        /**
         * @param popupMenu
         */
        public LimitedContextMenuContent(ContextMenu popupMenu) {
            super(popupMenu);
        }

        /**
         * Super's implemented to return screenheight.
         */
        @Override
        protected double computeMaxHeight(double height) {
            return 300;
        }
        
    }
    
    /**
     * We could leave it to a custom skin if we want more fancy support
     * like visibleRowCount. If simply copying the menu's value, we 
     * can just as well do it on a custom ContextMenu.
     * 
     */
    public static class MaxSizedContextMenuSkin extends ContextMenuSkin {

        public MaxSizedContextMenuSkin(ContextMenu control) {
            super(control);
            control.addEventHandler(Menu.ON_SHOWING, e -> {
                Node cmContent = control.getSkin().getNode();
                if (cmContent != null) {
                    if (cmContent instanceof ContextMenuContent) {
                        ((ContextMenuContent) cmContent).setMaxHeight(control.getMaxHeight());
                    }
                }

            });
        }
       
    }
    
    public static class MaxSizedContextMenu extends ContextMenu {
        
        public MaxSizedContextMenu() {
            addEventHandler(Menu.ON_SHOWING, e -> {
                Node content = getSkin().getNode();
                if (content instanceof Region) {
                    ((Region) content).setMaxHeight(getMaxHeight());
                }
            });
            
        }
    }
    private Parent createContent() {
        Button button = new Button("Dummy");
        ContextMenu menu = 
                new MaxSizedContextMenu();
        // on the fly creation
                new ContextMenu() {
            {
                addEventHandler(Menu.ON_SHOWING, e -> {
                    Node content = getSkin().getNode();
                    if (content instanceof Region) {
                        ((Region) content).setMaxHeight(getMaxHeight());
                    }
                });
            }
            // use custom skin
//            @Override
//            protected Skin<?> createDefaultSkin() {
//                return new MaxSizedContextMenuSkin(this);
//            }
            
        };
        menu.setMaxHeight(200);
        Stream.iterate(0, i -> i+1).limit(50).forEach(i -> {
            menu.getItems().add(new MenuItem("item " + i));
            
        });
        menu.skinProperty().addListener((src, ov, nv) -> LOG.info("" + nv));
        button.setContextMenu(menu);
        return new BorderPane(button);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ContextMenuWithLimitedItems.class.getName());

}
