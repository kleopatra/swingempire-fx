/*
 * Created on 16.09.2020
 *
 */
package control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.swingempire.fx.util.DebugUtils;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Copied here to verify bug on dragging: 
 * - no visual feedback when dragging off the trailing edge, 
 *    though tab is moved to somewhere
 * - not possible to drag tab off the leading edge
 * 
 * first can be fix in fx16 by calling invalidateScrollOffset in drag, 
 * second is unrelated, still persists with fix.
 * 
 * ----------
 * From dev mailinglist: preselect tab that's not visible
 * doesn't scroll the correct header into view
 * 
 * doing in button is fine as well as wrapping into 
 * runlater: looks like the skin init is incorrect/incomplete
 * 
 * reported as  https://bugs.openjdk.java.net/browse/JDK-8252236 
 * 
 * also:
 * - not kept visible after switching sides
 * - not kept visible on resizing the window
 * -from review: not kept visible on add-before, remove-after and move
 * 
 * reproduced (with initial fix): 
 * remove: before - selected scrolls off leading edge, after ?
 * add: before - selected scrolls off trailing edge, after ?
 * move: ?
 * 
 * without fix:
 * remove: before - selected scroll off leading edge, after ?
 * add: before - selected scrolls off the trailing edge, after ?
 * move: not on dragging, not on reorder
 * 
 * unrelated bug:
 * cannot drag tabHeader before leading edge
 */
public class TabPanePreselect extends Application {

    private TabPane tabPane;

    @Override
    public void start(Stage stage) {
        tabPane = new TabPane();

        for (int i = 0; i < 30; i++) {
            Tab tab = new Tab("Tab " + i);
            tab.setContent(new Label("Content for " + i));
            tabPane.getTabs().add(tab);
        }

        // set initial tab *outside* the visible range
        // Issue: tab header does *not* switch properly
        // Note: wrapping the following select statement in
//        Platform.runLater() works as expected
        // Platform.runLater(() -> {
        // });
//        tabPane.setSide(Side.BOTTOM);
//        tabPane.getSelectionModel().select(25);
        tabPane.setTabDragPolicy(TabDragPolicy.REORDER);
        Button select = new Button("select 25");
        select.setOnAction(e -> {
            int last = tabPane.getTabs().size() - 1;
            tabPane.getSelectionModel().select(Math.min(25, last));
            
        });
        ChoiceBox<Side> choice = new ChoiceBox<>();
        choice.getItems().addAll(Side.values());
        choice.valueProperty().bindBidirectional(tabPane.sideProperty());
        
        Button log = new Button("Log state");
        log.setOnAction(e -> {
            TabPaneSkin skin = (TabPaneSkin) tabPane.getSkin();
            System.out.println("offset: " + tabPane.getSide() + " / " + getHeaderAreaScrollOffset(skin));
            Node header = getHeaderFor(tabPane.getSelectionModel().getSelectedItem());
            // boundsInParent is the correct info to query, it changes with the scrollOffset
            DebugUtils.printBounds(header);
            
            Node headersRegion = header.getParent();
            DebugUtils.printBounds(headersRegion);
            
            Node headerArea = headersRegion.getParent();
            DebugUtils.printBounds(headerArea);
            
        });
        Button toggleSide = new Button("toggle side");
        // correct header location is lost again on toggling side
        toggleSide.setOnAction(e -> {
            Side oldSide = tabPane.getSide();
            List<Side> sides = Arrays.asList(Side.values());
            int index = sides.indexOf(oldSide);
            Side next = sides.get(index < sides.size() - 1 ? index + 1 : 0);
            tabPane.setSide(next);
        });
        
        // reduce maxWidth of tabPane: parent resizes tabPane, selected header kept in visible region
        Button maxTabWidth = new Button("toggle tabPane maxWidth");
        maxTabWidth.setOnAction(e -> {
            System.out.println("max tabPane: " + tabPane.getMaxWidth());
            double w = tabPane.getMaxWidth() > 0 ? -1 : 300;
            tabPane.setMaxWidth(w);
        });
        
        // reduce maxWidth of tabPane: parent resizes tabPane, selected header kept in visible region
        Button maxTabHeight = new Button("toggle tabPane maxHeight");
        maxTabHeight.setOnAction(e -> {
            System.out.println("max tabPane: " + tabPane.getMaxHeight());
            double w = tabPane.getMaxHeight() > 0 ? -1 : 300;
            tabPane.setMaxHeight(w);
        });
        
        // reduce maxWidth of parent: nothing visible happens
        Button maxContentWidth = new Button("toggle root maxWidth");
        maxContentWidth.setOnAction(e -> {
            Pane content = (Pane) tabPane.getParent();
            System.out.println("max parent: " + content.getMaxWidth());
            double w = content.getMaxWidth() > 0 ? -1 : 300;
            content.setMaxWidth(w);
        });
        
        Button maxWindowWidth = new Button("toggle stage maxWidth");
        maxWindowWidth.setOnAction(e -> {
            Stage content = (Stage) tabPane.getScene().getWindow();
            System.out.println("max parent: " + content.getMaxWidth());
            double w = content.getMaxWidth() == 300 ? 1025 : 300;
            content.setMaxWidth(w);
        });
        
        Button addTab = new Button("add tab before");
        addTab.setOnAction(e -> {
            int selected = tabPane.getSelectionModel().getSelectedIndex();
            tabPane.getTabs().add(selected, new Tab("added before", new Label("added before")));
        });
        
        Button addFirst = new Button("add first");
        addFirst.setOnAction(e -> {
            tabPane.getTabs().add(0, new Tab("first", new Label("first")));
        });
        Button addLast = new Button("add last");
        addLast.setOnAction(e -> {
            tabPane.getTabs().add(new Tab("last", new Label("last")));
        });
        Button removeTab = new Button("remove tab after");
        removeTab.setOnAction(e -> {
            int selected = tabPane.getSelectionModel().getSelectedIndex();
            int last = tabPane.getTabs().size() - 1;
            if (selected == last) return;
            tabPane.getTabs().remove(selected + 1);
        });
        
        Button removeFirst = new Button("remove first");
        removeFirst.setOnAction(e -> {
            tabPane.getTabs().remove(0);
        });
        
        Button removeLast = new Button("remove last");
        removeLast.setOnAction(e -> {
            tabPane.getTabs().remove(tabPane.getTabs().size() - 1);
        });
        
        Button reorder = new Button("reorder");
        reorder.setOnAction(e -> {
            List<Tab> tabs = new ArrayList<>(tabPane.getTabs());
            int selected = tabPane.getSelectionModel().getSelectedIndex();
            Tab tab = tabs.remove(selected);
            tabs.add(tab);
            tabPane.getTabs().setAll(tabs);
        });
        
        FlowPane buttons = new FlowPane(20, 20);
//        HBox buttons = new HBox(20, select, choice, toggleSide, log);
        buttons.getChildren().addAll(select, choice, toggleSide, log, 
                
                addTab, addFirst, addLast,
                removeTab, removeFirst, removeLast,
                reorder,
                maxTabWidth, maxTabHeight,
                maxContentWidth, maxWindowWidth
                );
        
//        BorderPane content = new BorderPane(tabPane);    
//        content.setBottom(buttons);
        
        VBox content = new VBox(10, tabPane, buttons);
        
        
        Scene scene = new Scene(content, 600, 600);
        stage.setScene(scene);

        
        
        stage.show();
        stage.setX(stage.getX() - 350);
        Screen s = Screen.getPrimary();
        System.out.println(s );
    }
    
    public double getHeaderAreaScrollOffset(TabPaneSkin skin) {
        // package scope test api
        // skin.test_getHeaderAreaScrollOffset();
        return (double) FXUtils.invokeGetMethodValue(TabPaneSkin.class, skin, "test_getHeaderAreaScrollOffset");
    }

    public Node getHeaderFor(Tab tab) {
        List<Node> headers = getTabHeaders();
        Optional<Node> header = headers.stream()
                .filter(h -> h.getProperties().get(Tab.class) == tab)
                .findFirst();
        return header.get();
    }
    public List<Node> getTabHeaders() {
        StackPane headersRegion = (StackPane) tabPane.lookup(".headers-region");
        return headersRegion.getChildren();
    }

    public static void main(String[] args) {
        Application.launch(TabPanePreselect.class, args);
    }

}