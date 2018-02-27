/*
 * Created on 25.02.2018
 *
 */
package control.performance;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48963777/203657
 * memory leak when adding/removing textFields
 * 
 */
public class TextfieldMemoryLeak extends Application
{
    private ScrollPane scroll;
    private FlowPane pane;
    private Scene scene;
    private Stage stage;
    @Override
    public void start(Stage stage) throws Exception
    {
        try
        {
            this.stage=stage;
            pane = new FlowPane();
            Button b1 = new Button("Add 2000");
            Button b = new Button("Remove 2000");

            b1.setOnAction(new EventHandler<ActionEvent>() {

                @Override public void handle(ActionEvent e) {
                    addTextFields();

                }});        
            b.setOnAction(new EventHandler<ActionEvent>() {

                    @Override public void handle(ActionEvent e) {
                        removeTextFields();
                        System.gc();

            }});
            pane.getChildren().add(b);
            pane.getChildren().add(b1);



            scroll = new ScrollPane();



            scroll.setContent(pane);


            addTextFields();

            scene = new Scene(scroll,800,600);

            stage.setScene(scene);
            stage.show();


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    private void addTextFields()
    {
        for(int i=0; i < 2000; i++)
        {
            //Text text = new Text("T " + i);
            TextField textField = new TextField("T "+i);

            this.pane.getChildren().add(textField); 
        }
    }

    private void removeTextFields()
    {
        for(int i=2001; i>1; i--)
        {       
        //  Text f = (Text) this.pane.getChildren().get(i);
            TextField f = (TextField) this.pane.getChildren().get(i);

            this.pane.getChildren().remove(f);          
        }
//        System.gc();

    }
    // vm arguments - doesn't start up, error
//    Fehler: Kennwortdatei nicht gefunden: C:\java\jdk\190-64\conf\management\jmxremote.password
//    jdk.internal.agent.AgentConfigurationError
//            at jdk.management.agent/sun.management.jmxremote.ConnectorBootstrap.checkPasswordFile(ConnectorBootstrap.java:564)
//            at jdk.management.agent/sun.management.jmxremote.ConnectorBootstrap.startRemoteConnectorServer(ConnectorBootstrap.java:427)
//            at jdk.management.agent/jdk.internal.agent.Agent.startAgent(Agent.java:450)
//            at jdk.management.agent/jdk.internal.agent.Agent.startAgent(Agent.java:621)
//    -Dcom.sun.management.jmxremote
//
//    -Dcom.sun.management.jmxremote.port=<Port>
//
//    -Dcom.sun.management.jmxremote.
//
//    -Dcom.sun.management.jmxremote.



    public static void main(String[] args)
    {
        launch(args);
    }

}

