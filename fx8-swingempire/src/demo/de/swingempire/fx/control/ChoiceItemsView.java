/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.control;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

/**
 * http://stackoverflow.com/q/25583707/203657
 * app freezing after exchanging items from one choicebox to another.
 * 
 * Might be user error: from choicebox api doc "ChoiceBox is used for 
 * presenting the user with a relatively small set of
 * predefined choices"
 */
public class ChoiceItemsView {
    @FXML
    private ChoiceBox<String> firstCB;
    @FXML
    private ChoiceBox<String> secondCB;
    public void init() {
      Set<String> keySet = Charset.availableCharsets().keySet();
      List<String> list = new ArrayList<>(keySet);
      // smaller subset is just fine
//      list = list.subList(0, 10);
      firstCB.getItems().addAll(list);
      secondCB.getItems().addAll(list);
      LOG.info("item count: " + secondCB.getItems().size());
      firstCB.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldVal, newVal) -> {
            System.out.printf("[%s]firstCB selection changed%n", Thread.currentThread().getName());
            if (newVal != null)
            secondCB.getItems().remove(newVal);
            if (oldVal != null)
            secondCB.getItems().add(oldVal);
            LOG.info("old/new " + oldVal + "/" + newVal + "/" + secondCB.getItems().size());
          });
      // removing one of the event listeners doesn't help
      secondCB.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldVal, newVal) -> {
            System.out.printf("[%s]secondCB selection changed%n", Thread.currentThread().getName());
            if (newVal != null)
            firstCB.getItems().remove(newVal);
            if (oldVal != null)
            firstCB.getItems().add(oldVal);
            LOG.info("old/new " + oldVal + "/" + newVal + "/" + firstCB.getItems().size());
          });
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ChoiceItemsView.class
            .getName());
  }

