/*
 * Created on 28.06.2019
 *
 */
package de.swingempire.fx.fxml;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * https://stackoverflow.com/q/56803049/203657
 * problem with propertyValueFactory: 
 * <p>
 * module javafx.base cannot access class fx.model.user (in module openfx10) 
 * because module openfx10 does not open fx.model to javafx.base
 * <p>
 * 
 * (package names here, op has slightly different names)
 * Violating of naming conventions look like the obvious candidate for a reason. But
 * actually this works in not-modular context. In modular context, the package that
 * contains the model must be opened in the module-info. 
 * <p>
 * 
 * Also, the package containing classes with private fields to inject must be opened, otherwise
 * we the following runtime error:
 * <p>
 * 
 * Unable to make field private javafx.scene.control.TableColumn fx.UserController.tableView accessible: 
 * module openfx10 does not "opens fx" to module javafx.fxml
 * 
 * <p>
 * 
 * To open individual packages, the module-info must contain all of this:
 * 
 *   exports fx;
 *   opens fx;
 *   opens fx.model;
 *   
 * Alternatively, the complete module can be opened:
 * 
 * open module openfx {
 * 
 *   exports fx;
 * }
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class UserController implements Initializable {
    @FXML private TableView<user> tableView;
    @FXML private TableColumn<user, String> UserId;
    @FXML private TableColumn<user, String> UserName;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserId.setCellValueFactory(new PropertyValueFactory<user, String>("userId"));
        UserName.setCellValueFactory(new PropertyValueFactory<user, String>("userName"));


        tableView.getItems().setAll(parseUserList());
    }
    private List<user> parseUserList(){

        List<user> l_u = new ArrayList<user>();

        user u = new user();
        u.setUserId(1);
        u.setUserName("test1");
        l_u.add(u);

        u.setUserId(2);
        u.setUserName("test2");
        l_u.add(u);

        u.setUserId(3);
        u.setUserName("test3");
        l_u.add(u);


        return l_u;
    }

    public class user {
        private int userId;

        public int getUserId() { return this.userId; }
        public void setUserId(int userId) { this.userId = userId; }

        private String userName;

        public String getUserName() { return this.userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}
