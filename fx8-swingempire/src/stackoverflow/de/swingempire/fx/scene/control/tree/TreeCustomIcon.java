/*
 * Created on 21.07.2019
 *
 */
package de.swingempire.fx.scene.control.tree;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57130393/203657
 * custom icons not shown properly
 * 
 * basic problem was re-use of node
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeCustomIcon extends Application {
//    private final Image male = new Image(
//            getClass().getResourceAsStream("male.png"), 16, 16, true, true);
//
//
//    private final Image female = new Image(
//            getClass().getResourceAsStream("f"), 16, 16, true, true);
//
//
//    private final Image plus = new Image(
//            getClass().getResourceAsStream("plus-button.png"), 16, 16, true,
//            true);

//    private final Image minus = new Image(
//            getClass().getResourceAsStream("minus-button.png"), 16, 16, true,
//            true);

    private final Node maleIcon = new Button("m");//new ImageView(male);
    private final Node femaleIcon = new Button("o");//new ImageView(female);
    private final Node plusIcon = new Button("+"); //new ImageView(plus);
    
    private final Node minusIcon = new Button("-"); //new ImageView(minus);

    private TreeView<Person> tree;

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage topView) {
        createGUI(topView);
    }

    private void createGUI(Stage topView) {
        topView.setTitle("Dummy App");
        initTree();
        VBox vb = new VBox(tree);
        topView.setScene(new Scene(vb));
        topView.show();
    }

    private void initTree() {
        Person person1 = new Person("Charles", 'M', '0');
        Person person2 = new Person("John", 'M', 'A');
        Person person3 = new Person("Pearl", 'F', 'A');
        TreeItem<Person> root = new TreeItem<>(person1);
        TreeItem<Person> child1 = new TreeItem<>(person2);
        child1.getChildren().addAll(new TreeItem<Person>(new Person("dummy", 'F', 'Z')));
        TreeItem<Person> child2 = new TreeItem<>(person3);
        tree = new TreeView<>(root);
        root.setExpanded(true);
        root.getChildren().addAll(child1, child2);

        tree.setCellFactory(tv -> {
            Node maleIcon = new Button("m");
            Node femaleIcon = new Button("f");
            Node expanded = new Label("-");
            Node collapsed = new Label("+");
            TreeCell<Person> cell = new TreeCell<Person>() {
                @Override
                public void updateItem(Person item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Node icon = (item.getGender() == 'M' ? maleIcon
                                : femaleIcon);
                        setGraphic(icon);
                        setText(item.getName());
                        
                    }
                    if (getTreeItem() != null) {
                        setDisclosureNode(getTreeItem().isExpanded() ? expanded : collapsed);
                    }
                }
            };
            return cell;
        });
    }

    public class Person {
        String name;

        char gender;

        char group;

        public Person(String name, char gender, char group) {
            this.name = name;
            this.gender = gender;
            this.group = group;
        }

        public String getName() {
            return name;
        }

        public char getGender() {
            return gender;
        }

        public char getGroup() {
            return group;
        }

        public String toString() {
            return name;
        }

    }
}



