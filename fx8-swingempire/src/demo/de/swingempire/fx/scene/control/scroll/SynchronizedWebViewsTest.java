/*
 * Created on 30.03.2018
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/49509395/203657
 * webview: synch scrolling state between two webviews
 * 
 * weird: can't programmatically update (or can, but has no effect)
 */
public class SynchronizedWebViewsTest extends Application {

    protected class DifferencePanel extends GridPane {
        private WebView actualPane;

        private WebView expectedPane;

        public DifferencePanel() {
            setPadding(new Insets(20, 20, 20, 20));
            actualPane = new WebView();
            expectedPane = new WebView();
            setResultPanes();
            addRow(0, actualPane, expectedPane);
            
            Button synch = new Button("BindBars");
            synch.setOnAction(e -> {
                synchronizeScrolls();
            });
            addRow(1, synch);
            
        }

        public void setHtml(WebView webView) {
            Platform.runLater(() -> {
                webView.getEngine().loadContent(createHtml());
            });
        }

        public void synchronizeScrolls() {
            Set<Node> actualBars = actualPane.lookupAll(".scroll-bar:vertical");
            Set<Node> expectedBars = expectedPane.lookupAll(".scroll-bar:vertical");
            
            List<ScrollBar> actBarList = actualBars.stream()
                    .filter(n -> (n instanceof ScrollBar))
                    .map(n -> (ScrollBar) n)
                    .collect(Collectors.toList());
            List<ScrollBar> expBarList = expectedBars.stream()
                    .filter(n -> (n instanceof ScrollBar))
                    .map(n -> (ScrollBar) n)
                    .collect(Collectors.toList());
            
            LOG.info("actbars: " + actBarList);
            LOG.info("expBars: " + expBarList);
            
//            actBarList.stream().forEach(sb -> {
//                LOG.info("visible? " + sb.isManaged() );
//                sb.valueProperty().addListener((src, ov, nv) ->{
//                    ScrollBar bean = (ScrollBar) ((Property) src).getBean();
//                    LOG.info("val/index act: " + nv + " " +  actBarList.indexOf(bean) + " " + bean);
//                });
//            });
//            expBarList.stream().forEach(sb -> {
//                LOG.info("visible? " + sb.isManaged() );
//                sb.valueProperty().addListener((src, ov, nv) ->{
//                    ScrollBar bean = (ScrollBar) ((Property) src).getBean();
//                    LOG.info("val/index exp: " + nv + " " + expBarList.indexOf(bean) + " " + bean);
//                });
//            });
//            
            int lastSB = 2;
            final ScrollBar actualScrollBarV = actBarList.get(lastSB);
//                    (ScrollBar) actualPane.lookup(".scroll-bar:vertical");
            final ScrollBar expectedScrollBarV = expBarList.get(lastSB);
//                    (ScrollBar) expectedPane  .lookup(".scroll-bar:vertical");
            
            actualScrollBarV.valueProperty().addListener((src, ov, nv) -> {
                ScrollBar bean = (ScrollBar) ((Property) src).getBean();
                LOG.info("in actList: " + nv + " v in exp: " + expectedScrollBarV.getValue());
                expectedScrollBarV.setValue(nv.doubleValue());
                expectedPane.layout();
            });
            expectedScrollBarV.valueProperty().addListener((src, ov, nv) -> {
              ScrollBar bean = (ScrollBar) ((Property) src).getBean();
              LOG.info("in expList: " + nv + " v in act: " + actualScrollBarV.getValue());
              actualScrollBarV.setValue(nv.doubleValue());
              actualPane.layout();  
            });
//            LOG.info("parentactual: " +actualScrollBarV.getParent()
//               + "\nparentexpected: " + expectedScrollBarV.getParent());
//            expectedScrollBarV.valueProperty().bindBidirectional(actualScrollBarV.valueProperty());
//            actualScrollBarV.valueProperty()
//                    .bindBidirectional(expectedScrollBarV.valueProperty());
//            LOG.info("bound: " + actual);
//            final ScrollBar actualScrollBarH = (ScrollBar) actualPane
//                    .lookup(".scroll-bar:horizontal");
//            final ScrollBar expectedScrollBarH = (ScrollBar) expectedPane
//                    .lookup(".scroll-bar:horizontal");
//            actualScrollBarH.valueProperty()
//                    .bindBidirectional(expectedScrollBarH.valueProperty());
        }

        private String createHtml() {
            StringBuilder sb = new StringBuilder(1000000);
            for (int i = 0; i < 100; i++) {
                sb.append(String.format(
                        "<nobr>%03d %2$s%2$s%2$s%2$s%2$s%2$s%2$s%2$s</nobr><br/>\n",
                        Integer.valueOf(i), "Lorem ipsum dolor sit amet "));
            }
            return sb.toString();
        }

        private void setResultPanes() {
            setHtml(actualPane);
            setHtml(expectedPane);
        }
    } // ---------------------------- end of DifferencePanel
      // ----------------------------

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage dummy) throws Exception {
        Stage stage = new Stage();
        stage.setTitle(this.getClass().getSimpleName());
        DifferencePanel differencePanel = new DifferencePanel();
        Scene scene = new Scene(differencePanel, 800, 400);
        stage.setScene(scene);
        stage.show();
//        differencePanel.synchronizeScrolls();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SynchronizedWebViewsTest.class.getName());
}

