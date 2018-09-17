/*
 * Created on 14.09.2018
 *
 */
package de.swingempire.fx.event;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.ContextMenuContent;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.skin.ContextMenuSkin;
import javafx.scene.control.skin.MenuBarSkin;
import javafx.scene.control.skin.MenuButtonSkin;
import javafx.scene.control.skin.MenuButtonSkinBase;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * bug, 
 * https://bugs.openjdk.java.net/browse/JDK-8092118
 * support drag-click - currently the first click into the menu 
 * triggers its action, no way to interrupt except esc (which hides
 * the menu)
 * expected: 
 * - press-drag moves the selection to new item (and release on it
 *   will fire the new action)
 * - drag to outside of menu -> no action fired on released
 * 
 * similar issue with menuBar   
 * 
 * on SO: 
 * https://stackoverflow.com/q/29283426/203657
 * 
 * has answers
 * 
 * MenuItemContainer is-the visual representation of the MenuItem
 * - is-a Region (no armed/pressed semantics)
 * - registers mouseHandlers for entered and released
 * - mouseEntered requests focus
 * - mouseReleased calls doSelect
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class MenuItemNotCanceledOnMouseExit extends Application {

    /**
     * Solution from SO
     */
    public static class ContextMenuFixer {

        public static void fix(ContextMenu contextMenu) {
            if (contextMenu.getSkin() != null) {
                fix(contextMenu, (ContextMenuContent) contextMenu.getSkin().getNode());
            } else {
                contextMenu.skinProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue != null) {
                        fix(contextMenu, (ContextMenuContent) contextMenu.getSkin().getNode());
                    }
                });
            }
        }

        private static void fix(ContextMenu menu, ContextMenuContent content) {

            content.getItemsContainer().getChildren().forEach(node -> {

                EventHandler<? super MouseEvent> releaseEventFilter = event -> {
                    if (!((Node) event.getTarget()).isFocused()) {
                        event.consume();
                        menu.hide();
                    }
                };
                node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                    node.removeEventFilter(MouseEvent.MOUSE_RELEASED, releaseEventFilter);

                });

                node.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
                    node.startFullDrag();
                    node.addEventFilter(MouseEvent.MOUSE_RELEASED, releaseEventFilter);
                });

                node.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
                    MouseEvent e = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_ENTERED);
                    node.fireEvent(e);
                });

                node.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
                    Event e = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_RELEASED);
                    node.fireEvent(e);
                });

                node.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> {
                    node.getParent().requestFocus();
                });

                node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                    menu.hide();
                });

            });
        }

        public static void fix(List<MenuItem> items) {
            items.forEach(item -> {
                Node node = item.getStyleableNode();
                if (node == null) throw new IllegalStateException("item must have styleable node: " + item);
                ContextMenu contextMenu = item.getParentPopup();
                if (contextMenu == null) throw new IllegalStateException("item must have contextMenu: " + item);
                EventHandler<? super MouseEvent> releaseEventFilter = event -> {
                    if (!((Node) event.getTarget()).isFocused()) {
                        event.consume();
                        contextMenu.hide();
                    }
                };
                node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                    node.removeEventFilter(MouseEvent.MOUSE_RELEASED, releaseEventFilter);

                });

                node.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
                    node.startFullDrag();
                    node.addEventFilter(MouseEvent.MOUSE_RELEASED, releaseEventFilter);
                });

                node.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
                    MouseEvent e = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_ENTERED);
                    node.fireEvent(e);
                });

                node.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
                    Event e = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_RELEASED);
                    node.fireEvent(e);
                });

                node.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> {
                    node.getParent().requestFocus();
                });

                node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                    contextMenu.hide();
                });

            });
        }
        
        public static void fix(MenuBar menubar) throws Exception {
            // Hack through MenuBarSkin until we get the ContextMenus
            Field container = MenuBarSkin.class.getDeclaredField("container");
            container.setAccessible(true);
            Field openMenu = MenuBarSkin.class.getDeclaredField("openMenu");
            openMenu.setAccessible(true);
            Field popup = MenuButtonSkinBase.class.getDeclaredField("popup");
            popup.setAccessible(true);

//            MenuBarSkin mBarSkin = (MenuBarSkin) menubar.getSkin();
            MenuBarSkin mBarSkin = new MenuBarSkin(menubar);
            menubar.setSkin(mBarSkin);

            // Modified code from Danis
            HBox hBox = (HBox) container.get(mBarSkin);
            hBox.getChildren().forEach(child -> {
                MenuButton mButton = (MenuButton) child;
                MenuButtonSkin mButtonSkin = new MenuButtonSkin(mButton);
                mButton.setSkin(mButtonSkin);

                ContextMenu contextMenu;
                try {
                    contextMenu = (ContextMenu) popup.get(mButtonSkin);
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    e1.printStackTrace();
                    return;
                }

                ContextMenuSkin cmSkin = new ContextMenuSkin(contextMenu);
                contextMenu.setSkin(cmSkin);
                ContextMenuContent content = (ContextMenuContent) cmSkin.getNode();

                contextMenu.setOnHiding(event -> {
                    try {
                        ((Menu) openMenu.get(mBarSkin)).hide();
                        openMenu.set(mBarSkin, null);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                        return;
                    }
                });

                content.getItemsContainer().getChildren().forEach(node -> {
                    EventHandler<? super MouseEvent> releaseEventFilter = event -> {
                        if (!((Node) event.getTarget()).isFocused()) {
                            event.consume();
                            contextMenu.hide();
                            mButton.hide();
                        }
                    };
                    node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                        node.removeEventFilter(MouseEvent.MOUSE_RELEASED, releaseEventFilter);

                    });

                    node.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
                        node.startFullDrag();
                        node.addEventFilter(MouseEvent.MOUSE_RELEASED, releaseEventFilter);
                    });

                    node.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
                        MouseEvent e = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_ENTERED);
                        node.fireEvent(e);
                    });

                    node.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
                        Event e = event.copyFor(event.getSource(), event.getTarget(), MouseEvent.MOUSE_RELEASED);
                        node.fireEvent(e);
                    });

                    node.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> {
                        node.getParent().requestFocus();
                    });

                    node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
                        contextMenu.hide();
                    });
                });
            });
        }


    }
    
    /**
     * Answer from SO for MenuBar.
     * @return
     */
    private Parent createContent() {
        MenuBar bar = new MenuBar();
//        try {
//            ContextMenuFixer.fix(bar);
//        } catch (Exception e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        Menu first = new Menu("first");
        bar.skinProperty().addListener((src, ov, nv) -> {
            if (nv != null) {
                LOG.info("styleableNode of first: " + first.getStyleableNode() + first.getStyleableParent());
//                try {
//                    ContextMenuFixer.fix(bar);
//                } catch (Exception e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
            }
        });
        first.setOnShown(e -> {
//            LOG.info("styleableNode of first: " + first.getStyleableNode() + first.getStyleableParent());
//            ContextMenuFixer.fix(first.getItems());
//            MenuItem item = first.getItems().get(0);
//            ContextMenuFixer.fix(item.getParentPopup());
//            first.getItems().forEach(item -> {
//                LOG.info("contextMenu " + item.getParentPopup() );
//                Node node = item.getStyleableNode();
//                node.addEventFilter(MouseEvent.MOUSE_EXITED, f -> {
//                    node.getProperties().put("EXITED", true);
//                });
//                node.addEventFilter(MouseEvent.MOUSE_ENTERED, f -> {
//                    node.getProperties().remove("EXITED");
//                });
//                node.addEventFilter(MouseEvent.MOUSE_RELEASED, f -> {
//                    if (Boolean.TRUE.equals(node.getProperties().get("EXITED"))) {
//                        LOG.info("found exited: " + item.getText());
//                        node.getProperties().remove("EXITED");
//                        f.consume();
//                    }
//                });
//            });
        });
        
        Menu second = new Menu("second");
        bar.getMenus().addAll(first, second);
        
        MenuItem on = new MenuItem("on");
        on.setOnAction(e -> LOG.info("on"));
        
        MenuItem other = new MenuItem("other");
        other.setOnAction(e -> LOG.info("other"));
                
        first.getItems().addAll(on, other);
        
        Button normal = new Button("dummy");
        normal.setOnAction(e -> LOG.info("normal"));
        
        CheckBox box = new CheckBox("check");
        box.setOnAction(e -> LOG.info("check"));
        
        ContextMenu context = new ContextMenu();
        MenuItem cFirst = new MenuItem("cFirst");
        cFirst.setOnAction(e -> LOG.info("cFirst"));
        MenuItem cOther = new MenuItem("cOther");
        cOther.setOnAction(e -> LOG.info("cOther"));
        
        context.getItems().addAll(cFirst, cOther);
        box.setContextMenu(context);
        ContextMenuFixer.fix(context);
        
        HBox buttons = new HBox(10, normal, box);
        
        BorderPane content = new BorderPane();
        content.setTop(bar);
        content.setBottom(buttons);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 400));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(MenuItemNotCanceledOnMouseExit.class.getName());

}
