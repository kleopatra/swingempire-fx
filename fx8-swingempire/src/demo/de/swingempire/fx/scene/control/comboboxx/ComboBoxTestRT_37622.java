/*
 * Created on 26.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.util.DebugUtils;

/**
 * https://javafx-jira.kenai.com/browse/RT-37622
 * ComboBox size of popup not updated
 * 
 * orig:
 * - open popup to see three items
 * - click change content 
 * - open popup to see four items
 * - expected: seeing four items without scrollbars (aka: size adjusted in both dimensions)
 * - actual: three items with vertical scrollbar, long item with ellipse
 * 
 * happens in report (Mac) always, can be reproduced in win7 machines only
 * if selecting "three" on first opening 
 * 
 * Unexpected notification: 
 * setAll fires replace, independent of old vs. new size
 * ListViewSkin listChange listener: 
 * special cased replace, marking cells dirty for c.getFrom <= i < getTo
 * with the unexpected notification above, this
 * leads to _not_ dirty cells that were removed without being added
 * (aka removedSize > addedSize
 * but: we see the behaviour for the other direction, that is when
 * removedSize < addedSize that is we mark a cell dirty that had not
 * been used before
 * virulent the very first time only but then sticky: if the first
 * selection was an item other than three, it will behave as expected
 * always. If the very first select is three, then it selecting others
 * will still keep the misbehaviour.
 * 
 * Behaviour related to acutally opening the popup: select programatically is
 * okay.
 * 
 * Behaviour related to size of selected: if < initial (pref?) width, okay.
 * If selected requires resize of the box, not updated again when setting the
 * model.
 * 
 * Decision: give up for now, don't understand why selected vs nothing selected
 * makes a difference.
 *  
 * Note: listeners
 * - on combo not registered twice in this example because the items
 *   are replaced in the original
 * - on listView _are_ registered twice because skin calls setItems
 *     
 * 
 */
public class ComboBoxTestRT_37622 extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
       
       primaryStage.centerOnScreen();
//       primaryStage.setHeight(200);
//       primaryStage.setWidth(300);
       List<String> list1 = new ArrayList<>();
       list1.add("one");
       list1.add("two");
       list1.add("three");
       
       List<String> list2 = new ArrayList<>();
       list2.add("one");
       list2.add("two");
       list2.add("three three");
       list2.add("four");
       
//       final ComboBox<String> combo = new ComboBox<String>();
       // layout invoke added, same issue as core
       final ComboBoxX<String> combo = new ComboBoxX<String>();
       combo.getItems().setAll(list1);
       
//       combo.getItems().addListener(new MyListener());
//       ListChangeListener l = c -> {
//           // look for added
//           while(c.next()) {
//               String range = "[" + c.getFrom() + "," + c.getTo() + ") ";
//               if (c.wasAdded()) {
//                   LOG.info("added: " + range + c);
//               }    
//           }
//           c.reset();
//           while(c.next()) {
//               String range = "[" + c.getFrom() + "," + c.getTo() + ") ";
//               if (c.wasRemoved()) {
//                   LOG.info("removed: " + range + c);
//               }    
//           }
//           c.reset();
//           while(c.next()) {
//               String range = "[" + c.getFrom() + "," + c.getTo() + ") ";
//               if (c.wasReplaced()) {
//                   LOG.info("replaced: " + range + c);
//               }    
//           }
//           c.reset();
//           
//       };
//       combo.getItems().addListener(l);
       Button button = new Button("Change combo contents");
       button.setOnAction(event -> {
           String oldInfo = DebugUtils.widthInfo(combo);
          if ( combo.getItems().size() == 3 ) {
             combo.getItems().setAll(list2);
          } else {
             combo.getItems().setAll(list1);
          }
          LOG.info(oldInfo + "\n" + DebugUtils.widthInfo(combo));
       });
       
//       Button select = new Button("Select 2");
//       select.setOnAction(e -> combo.getSelectionModel().select(2));
//       Button clearSelection = new Button("Clear selection");
//       clearSelection.setOnAction(e -> combo.getSelectionModel().clearSelection());
       VBox box = new VBox(20, combo, button); //, select, clearSelection );
//       box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
       primaryStage.setScene(new Scene(box ));

       primaryStage.show();

    }

    public static void main(String[] args) throws Exception {
       launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxTestRT_37622.class.getName());
 }

