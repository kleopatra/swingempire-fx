/*
 * Created on 22.07.2019
 *
 */
package de.swingempire.fx.event;

import java.io.File;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

/**
 * Get hold of pressed/not of Ctrl in dragEvent
 * https://stackoverflow.com/q/57131756/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DragEventKeyboardState extends Application {
    final SimpleBooleanProperty isControlDown = new SimpleBooleanProperty(false);

    private String files;
    
   public static void main(String[] args) {
       launch(args);
   }

   @Override
   public void start(Stage primaryStage) {
       primaryStage.setTitle("Test CTRL Down");
       final TextArea ta = new TextArea();

//       ta.addEventHandler(MouseEvent.ANY, e -> {
//           LOG.info("from any" + e.getEventType());
//           
//       });
       ta.setOnMouseEntered(e -> {
           MouseEvent m;
           LOG.info("from entered" + e.getEventType() + e.isControlDown());
           isControlDown.set(e.isControlDown());
           
           if(isControlDown.getValue())
           {
               ta.appendText(files + " with control down\n");
               System.out.println("Drop files when contol is down");
               
           } else {
               ta.appendText(files + " with NOT control down\n");
               System.out.println("Drop files when contol is NOT down");
           }

       });
       
//       ta.setOnKeyPressed(e -> {
//           if (e.isControlDown()) {
//               System.out.println("Contol is down en property set");
//               isControlDown.setValue(true);
//           }
//       });
//       ta.setOnKeyReleased(event -> isControlDown.set(false));

       ta.setOnDragOver(event -> {
           Dragboard db = event.getDragboard();
           if (db.hasFiles()) {
               ta.requestFocus();
               event.acceptTransferModes(TransferMode.COPY);
           }
       });

       ta.setOnDragDropped(event -> {
           Dragboard db = event.getDragboard();
           boolean success = false;
           if (db.hasFiles()) {
               success = true;
               files =
                       db.getFiles().stream().map(File::getAbsolutePath).collect(Collectors.joining("\n"));
               event.setDropCompleted(success);
               event.consume();
               
               Platform.runLater(( ) -> {
//                   
//                   if(isControlDown.getValue())
//                   {
//                       ta.appendText(files + " with control down\n");
//                       System.out.println("Drop files when contol is down");
//                       
//                   } else {
//                       ta.appendText(files + " with NOT control down\n");
//                       System.out.println("Drop files when contol is NOT down");
//                   }
               });
           }
       });

       ScrollPane root = new ScrollPane();
       root.setContent(ta);
       primaryStage.setScene(new Scene(root, 600, 300));
       primaryStage.show();
   }
   
   @SuppressWarnings("unused")
   private static final Logger LOG = Logger
        .getLogger(DragEventKeyboardState.class.getName());
}

