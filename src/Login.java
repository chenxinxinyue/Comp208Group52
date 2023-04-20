/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mygame.Global.setGlobalLocalPlayerID;



public class Login extends BaseMenu {

    private Label usernameLabel;
    private Label passwordLabel;
    
    private TextField usernameField;
    private TextField passwordField;
    
    private Button loginButton;
    private Button registerButton;
    private Button returnButton;
    private Container popupContainer;
    private List<User> users;
    private Label tipLabel;

    
    public Login(SimpleApplication app){        
        super(app);        
    }

    public TextField getUsernameField() {
        return usernameField;
    }
    
    
    @Override
    protected void initialize(Application app) {
        super.initialize(app);
        
        users = new ArrayList<>();
        try {
            users = getUsers();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        BitmapFont font = assetManager.loadFont("Interface/Fonts/HanziPenSC.fnt");

        for (Label label : getLabels()) {
            label.setFont(font);
            label.setColor(ColorRGBA.Black);
        } 
        
        usernameField = new TextField("");        
        passwordField = new TextField("");
        usernameField.setFont(font);
        passwordField.setFont(font);
        tipLabel.setFontSize((float) (usernameLabel.getFontSize()*0.8));
        tipLabel.setFont(font);
        
        myWindow.addChild(usernameLabel);        
        myWindow.addChild(usernameField);
        myWindow.addChild(passwordLabel);
        myWindow.addChild(passwordField);
        myWindow.addChild(tipLabel);    
        
        myWindow.addChild(loginButton);
        myWindow.addChild(registerButton);
        myWindow.addChild(returnButton);
                        
    }

    @Override
    protected String getTitle() {
        return "Login Page";
    }

    @Override
    protected List<Button> getButtons() {
        loginButton = new Button("Login");
        registerButton = new Button("Register");
        returnButton = new Button("Return");
                
        loginButton.addClickCommands((Button source) -> {      
            System.out.println("Login button clicked");
            String enteredUsername = usernameField.getText();
            String enteredPassword = passwordField.getText();
            if(users.isEmpty()){
//                guiNode.detachChild(myWindow);
                tipLabel.setText("No existing user, please register one");                               
            }
            for (User user : users) {
//                System.out.println(user.getUsername()+";"+user.getPassword());
                if (user.getUsername().equals(enteredUsername) && user.getPassword().equals(enteredPassword)) {
                    setGlobalLocalPlayerID(enteredUsername);
                    stateManager.detach((AppState) this);
                    MainMenu state = new MainMenu((SimpleApplication) this.getApplication());
                    stateManager.attach(state);
                    state.setIsOnline(true);    
                    state.setPassword(enteredPassword);
                    state.setUsername(enteredUsername);
                } else {
                    tipLabel.setText("wrong user name or password");                               
                }
            }    
//            stateManager.detach((AppState) this);
//            MainMenu state = new MainMenu((SimpleApplication) this.getApplication());
//            stateManager.attach(state);
//            state.setIsOnline(true);    
//            state.setPassword(enteredPassword);
//            state.setUsername(enteredUsername);
        });    
        returnButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new Enter((SimpleApplication) this.getApplication()));
        });    
        registerButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new Register((SimpleApplication) this.getApplication()));
        });  
        return Arrays.asList(loginButton, registerButton, returnButton);
    }

    protected List<Label> getLabels() {
        usernameLabel = new Label("Username:");
        passwordLabel = new Label("Password:");
        tipLabel = new Label("");
        return Arrays.asList(usernameLabel, passwordLabel,tipLabel);
    }
    
    
    public List<User> getUsers() throws SQLException {
        String url = "jdbc:mysql://35.238.202.174:3306/gamedatabase?useSSL=false&allowPublicKeyRetrieval=true";
        String u = "client";
        String p = "m{tvNm|:ssr\\o__/";

        Connection conn = DriverManager.getConnection(url, u,p);

        // Create a statement for executing SQL queries
        Statement stmt = conn.createStatement();

        // Execute a SELECT query to read data from a table in the gamedata database
        String query = "SELECT * FROM users";
        ResultSet rs = stmt.executeQuery(query);

        // Process the results of the query
        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("username");
            String password = rs.getString("password");
            System.out.printf("id=%d, name=%s, password=%s%n", id, username, password);
            users.add(new User(id,username,password));
        }
        System.out.println("Read success");

        // Close the result set, statement, and connection
        rs.close();
        stmt.close();
        conn.close();
        return users;
    }

}