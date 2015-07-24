/*
 * Created on 24.07.2015
 *
 */
package de.swingempire.fx.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Working on the live-scenegraph - how to?
 * http://stackoverflow.com/q/31605531/203657
 * 
 */
public class GraphGymnastic extends Application {
    final ExecutorService serv = Executors.newFixedThreadPool(2);

    Label progress = new Label("idle");
    public static void main(String argv[]) {
        launch(argv);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Setup UI
        primaryStage.setTitle("Demo");
        progress.setId("progress");
        final List<Node> nodesInGraph = new ArrayList<>();
        final FlowPane p = new FlowPane() {
            {
                setId("flowpane");
                getChildren().addAll(new Label("Label") {
                    {
                        setId("label");
                    }
                }, new Button("Button") {
                    {
                        setId("button");
                        // setOnMouseClicked(event -> handle(event,
                        // nodesInGraph)); //Uncomment and comment below to see
                        // effects!
//                        setOnMouseClicked(event -> handleAsync(event,
//                                nodesInGraph));
                        setOnMouseClicked(event -> handleWithWorker(event, nodesInGraph));
                        
                    }
                }, progress
                
                );
                setHgap(5);
            }
        };

        // Assume that this goes recursive and deep into a scene graph but still
        // returns a list
        // Here it takes the two childs for simplicity
        nodesInGraph.addAll(p.getChildrenUnmodifiable());
        System.out.println(p.getChildren().size());
        // Show stage
        primaryStage.setScene(new Scene(p));
        primaryStage.show();
    }

    public static class NodeState {
        double x, y;
        int index;
        private String name;
        public NodeState(int index) {
            this.index = index;
        }
        public void setState(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public void setID(String name) {
            this.name = name;
        }
        public void process() throws InterruptedException {
            Thread.sleep(2000);
        }
        @Override
        public String toString() {
            return "[" + name + " x: " + x + " y: " + y + "]";
        }
    }

    public static class SceneGraphWorker extends Task<NodeState> {

        private List<Node> nodes;
        private int current;
        int size;
        
        public SceneGraphWorker(List<Node> nodes) {
            this.nodes = nodes;
            size = nodes.size();
        }
        
        @Override
        protected NodeState call() throws Exception {
            NodeState lastState = null;
            while (current < size) {
                NodeState state = new NodeState(current);
                CountDownLatch latch = new CountDownLatch(1);
                Platform.runLater(() -> {
                    Node node = nodes.get(current);
                    Bounds bounds = node.localToScene(node.getBoundsInLocal());
                    state.setState(bounds.getMinX(), bounds.getMinY());
                    state.setID(node.getId());
                    latch.countDown(); 
                });
                latch.await();
                state.process();
                System.out.println(current + " " +state);
                updateValue(state);
                lastState = state;
                current++;
            }
            return lastState;
        }
        
    }
    
    public void handleWithWorker(MouseEvent ev, List<Node> nodesInGraph) {
        Task worker = new SceneGraphWorker(nodesInGraph);
        worker.valueProperty().addListener((src, ov, nv) -> {
            progress.setText(nv != null ? nv.toString() : "empty");
        });
        new Thread(worker).start();
    }
    public void handle(MouseEvent ev, List<Node> nodesInGraph) {
        if (null != nodesInGraph)
            Platform.runLater(() -> nodesInGraph.forEach(node -> {
                // This will block the UI thread, so there is the need for a
                // second
                // thread
                    System.out.println("Calculating heavy on node "
                            + node.toString() + " with event from "
                            + ev.getSource().toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
    }

    public void handleAsync(MouseEvent ev, List<Node> nodesInGraph) {
        if (null != nodesInGraph)
            serv.submit(() -> nodesInGraph.forEach(node -> {
                // Now there is a second thread but it works on a LIVE view
                // object
                // list, which is ugly
                // Option 1: Keep it like this, drawbacks? :S
                // Option 2: Copy the graph, costs performance... How deep
                // should it
                // copy? :S
                    System.out.println("Calculating heavy on node "
                            + node.toString() + " with event from "
                            + ev.getSource().toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
    }
}