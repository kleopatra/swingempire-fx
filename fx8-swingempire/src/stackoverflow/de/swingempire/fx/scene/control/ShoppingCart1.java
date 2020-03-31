/*
 * Created on 19.03.2020
 *
 */
package de.swingempire.fx.scene.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Quick check of answer to https://stackoverflow.com/q/60697392/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ShoppingCart1 extends Application
{
    private Label answer;
    private Label price;
    ListView <String> listView;
    ListView <String> listView2;
    private String[] listArray = new String[7];
    private String[] listArray2 = new String[7];
    private List<String> cartItems = new ArrayList<>();

    private final double salesTax = 0.07;

    public static void main(String[] args) throws FileNotFoundException
    {
        launch(args);
    }

    
    @Override
    public void start(Stage primaryStage) throws FileNotFoundException
    {
        answer = new Label("Price: ");
        price = new Label("");
        String line;
        int index = 0;
        File file = new File("BookPrices.txt");
//        try (Scanner fileReader = new Scanner(file))
//        {
//            while (fileReader.hasNext())
//            {
//                line = fileReader.nextLine();
//                String[] titles = line.split(",");
//                listArray[index] = titles[0];
//                index++;
//            }
//        }
        //list view items book
        listView = new ListView<>();
        listView.setPrefSize(200, 170);
        listView.getItems().addAll(listArray);

        //list view items book
        listView2 = new ListView<>();
        listView2.setPrefSize(200, 170);
        listView2.getItems();

        // create label to display the selection
        Label selectedNameLabel = new Label("Select a Book");
        Label price = answer;

        //Button for selection
        Button addButton = new Button("Add to Cart");
        addButton.setOnAction(new AddButtonListener());

        //Delete button
        Button removeButton = new Button("Remove Item");
        removeButton.setOnAction(new RemoveButtonListener());

        //Delete button
        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(new ClearButtonListener());

        //Checkout
        Button checkoutButton = new Button("Check Out");
        checkoutButton.setOnAction(new CheckoutButtonListener());

        //Controls to HBox
        HBox hbox = new HBox(listView, listView2);

        //Controls to HBox2
        HBox hbox2 = new HBox(10, addButton, removeButton, clearButton, checkoutButton);
        hbox2.setAlignment(Pos.CENTER);

        //Controls to VBox
        VBox vbox = new VBox(10, hbox, selectedNameLabel, price, hbox2);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        //Show
        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.show();
        double d = 35.3456;
        DecimalFormat myFormatter = new DecimalFormat("###.##");
        System.out.println(String.format("%.2f", d) + " formatter " + myFormatter.format(d));
    }

    // Add button
    public class AddButtonListener implements EventHandler < ActionEvent >
    {
        @Override
        public void handle(ActionEvent e)
        {
            String value = listView.getSelectionModel().getSelectedItem();
            listView2.getItems().add(value);
            cartItems.add(value);
            answer.setText("Price: " + Calc());
        }
    }
    // Subtract Button
    public class RemoveButtonListener implements EventHandler < ActionEvent >
    {
        ClassCastException c; // rte
        NullPointerException n; // rte
        UnsupportedOperationException uo; // rte
        @Override
        public void handle(ActionEvent e)
        {
            String value = listView.getSelectionModel().getSelectedItem();
            try {
                cartItems.remove(value);
                listView2.getItems().remove(value);
                answer.setText("Price: " + Calc());
            }
            catch (IllegalArgumentException ex) {
                //do nothing
            }
        }
    }
    //Clearbutton
    public class ClearButtonListener implements EventHandler < ActionEvent > 
    {

        @Override
        public void handle(ActionEvent e) 
        {
            cartItems.clear(); //removeAll();
            listView2.getItems().clear();
            answer.setText("Price: " + Calc());
        }
    }

    //Checkout
    public class CheckoutButtonListener implements EventHandler < ActionEvent >
    {
        @Override
        public void handle(ActionEvent e) 
        {
            answer.setText("Price: " + Calc());
        }
    }

    // Button Calculations
    private String Calc() {
        String line;
        double totalCost = 0.0, costOfItem = 0.0;
        File file = new File("BookPrices.txt");
        Scanner fileReader = null;
        try
        {
            fileReader = new Scanner(file);
        }
        catch (FileNotFoundException el)
        {
            el.printStackTrace();
        }

        while (fileReader.hasNextLine())
        {
            line = fileReader.nextLine();
            String[] cost = line.split(",");

            String title = cost[0];
            costOfItem = Double.parseDouble(cost[1]);

            for (int i = 0; i < cartItems.size() /*getItemCount()*/; i++)
            {
                if (title.equals(cartItems.get/*Item*/(i)))
                    totalCost += costOfItem;
            }
        }
        DecimalFormat myFormatter = new DecimalFormat("###.##");
        return myFormatter.format((salesTax * totalCost) + totalCost).toString();
    }
}

