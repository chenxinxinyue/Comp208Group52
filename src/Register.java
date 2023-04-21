/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.BaseAppState;
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import java.sql.PreparedStatement;


/**
 *
 * @author lsgb
 */
public class Register extends BaseMenu {

    private Label usernameLabel;
    private Label passwordLabel;
    private Label passwordLabel2;
    
    private TextField usernameField;    
    private TextField passwordField;
    private TextField passwordField2;
    
    private Container popupContainer;    
    private Label popupLabel;
    private String popupString;
    
    private Button okButton;
    private Button returnButton;
    
    private Boolean invalidRegister;
    private List<User> users;
    private Label tipLabel;
    private int userNum;
    private java.sql.Connection conn;

    public Register(SimpleApplication app) {
        super(app);  
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app);
        
        users = new ArrayList<>();
        try {
            users = getUsers();
            userNum = users.size();
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        BitmapFont font = assetManager.loadFont("Interface/Fonts/HanziPenSC.fnt");

        for (Label label : getLabels()) {
            label.setColor(ColorRGBA.Black);
            label.setFont(font);
        } 
        
        usernameField = new TextField("");        
        passwordField = new TextField("");
        passwordField2 = new TextField("");
        usernameField.setFont(font);
        passwordField.setFont(font);
        passwordField2.setFont(font);
        tipLabel.setFontSize((float) (usernameLabel.getFontSize()*0.8));
        tipLabel.setFont(font);
        
        myWindow.addChild(usernameLabel);        
        myWindow.addChild(usernameField);
        myWindow.addChild(passwordLabel);
        myWindow.addChild(passwordField);
        myWindow.addChild(passwordLabel2);
        myWindow.addChild(passwordField2);
        myWindow.addChild(tipLabel);        
        myWindow.addChild(okButton);
        myWindow.addChild(returnButton);

    }

    private void checkRegisterInfo(){
        popupLabel = new Label(popupString);
        invalidRegister = validateUsername(usernameField.getText()) && validatePassword(passwordField.getText());  
        if(!invalidRegister){         
//            stateManager.detach((AppState) this);
//            stateManager.attach(new PopUpState((SimpleApplication) this.getApplication(), popupString,"Back", this));  
            tipLabel.setText(popupString);           
        } else {
            try {
                addUser(userNum+1,usernameField.getText(),passwordField.getText());
            } catch (SQLException ex) {
                System.out.println("can't add user to database");
                Logger.getLogger(Register.class.getName()).log(Level.SEVERE, null, ex);
            }
//            File file = new File("users.txt");
//            try (FileWriter fileWriter = new FileWriter(file, true)) {
//                fileWriter.write(usernameField.getText()+","+passwordField.getText()+"\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }         
            stateManager.detach((AppState) this);
            stateManager.attach(new PopUpState((SimpleApplication) this.getApplication(), popupString,"Login",new Login((SimpleApplication) this.getApplication())));              
//            createSucccessPopUp(new Label("You created a new Account"));

        }

    }
    private Boolean validateUsername(String text) {                                
        if(usernameField.getText() == null) {
            popupString = "User name can't be empty";
            return false;
        } 
        for (User user : users) {
            if(text.equals(user.getUsername())){
                popupString = "The User name has existed";
                return false;
            }
        }
        return true;
    }
    public Boolean validatePassword(String password) {        
        
        if (password == null || password.isEmpty()) {
            popupString = "Password cannot be null or empty.";  
            return false;
        }

        if (password.length() < 8) {
            popupString = "Password must be at least 8 characters long.";
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            popupString = "Password must contain at least one digit.";            
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            popupString = "Password must contain at least one lowercase letter.";           
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            popupString = "Password must contain at least one uppercase letter.";           
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\[\\]{};':\"\\\\|,.<>/?-].*")) {
            popupString = "Password must contain at least one special character.";            
            return false;
        }
        
        if(!passwordField.getText().equals(passwordField2.getText())){
            popupString = "Two passwords are not match";
            return false;
        }

        popupString = "You created a new account!";
        return true;
    }
    private void createSucccessPopUp(Label label) {
        // pop up window
        // Create a new Container for the popup window
        popupContainer = new Container();       

        popupContainer.setPreferredSize(new Vector3f(Display.getWidth() / 2f, Display.getHeight() / 2f, 0));

        // Add child elements to the container
        // Attach the container to the guiNode
        guiNode.attachChild(popupContainer);
                                
        float centerX = Display.getWidth() / 2f;
        float centerY = Display.getHeight() / 2f;

        // Calculate the center position of the container
        float containerWidth = popupContainer.getPreferredSize().getX();
        float containerHeight = popupContainer.getPreferredSize().getY();
        float containerX = centerX - (containerWidth / 2f);
        float containerY = centerY + (containerHeight / 2f);

        // Set the position of the container to be centered on the screen
        popupContainer.setLocalTranslation(containerX, containerY, 0);
        // Add a label to the popup
        popupContainer.addChild(label);
        guiNode.attachChild(popupContainer);

        // Add a close button to the popup
        Button closeBtn = new Button("Login");
        closeBtn.addClickCommands((Button source) -> {
            popupContainer.removeFromParent();
            MainMenu state = new MainMenu((SimpleApplication) this.getApplication());            
            stateManager.attach(state);
            state.setIsOnline(true);   
            state.setUsername(usernameField.getText());
            state.setPassword(passwordField.getText());
        });
        popupContainer.addChild(closeBtn);
        label.setColor(ColorRGBA.Blue);
        closeBtn.setColor(ColorRGBA.Blue);
    }

    @Override
    protected String getTitle() {
        return "Register Page";
    }

    @Override
    protected List<Button> getButtons() {
        
        okButton = new Button("Ok");
        returnButton = new Button("Return");
        
        returnButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new Login((SimpleApplication) this.getApplication()));
        });    
        okButton.addClickCommands((Button source) -> {    
//            guiNode.detachChild(myWindow);
            checkRegisterInfo();
        });
        
        return Arrays.asList(okButton, returnButton);
        
    }
    
    protected List<Label> getLabels() {
        usernameLabel = new Label("Input Your Username:");
        passwordLabel = new Label("Input Your Password:");
        passwordLabel2 = new Label("Input Your Password Agian:");
        tipLabel = new Label("The password must be more than or equal to 8 characters\nThe password must contain at least 1 lower case, 1 upper case, 1 digit and 1 special character");
//        tipLabel = new Label("The password must more than or equal to 8 character\nmust contain at least 1 lower case, 1 upper case, 1 digit and 1 special charactThe password must more than or equal to 8 character\nmust contain at least 1 lower case, 1 upper case, 1 digit and 1 special charactThe password must more than or equal to 8 character\nmust contain at least 1 lower case, 1 upper case, 1 digit and 1 special charactThe password must more than or equal to 8 character\nmust contain at least 1 lower case, 1 upper case, 1 digit and 1 special charactThe password must more than or equal to 8 character\nmust contain at least 1 lower case, 1 upper case, 1 digit and 1 special charactThe password must more than or equal to 8 character\nmust contain at least 1 lower case, 1 upper case, 1 digit and 1 special character");
        return Arrays.asList(usernameLabel, passwordLabel, passwordLabel2, tipLabel);
    }
    
    public void connectToDatabase() throws SQLException {
        System.out.println("Start to connect");
        String url = "jdbc:mysql://35.238.202.174:3306/gamedatabase?";
        String user = "client";
        String password = "m{tvNm|:ssr\\o__/";
        conn = DriverManager.getConnection(url, user, password);
    }
    
    public List<User> getUsers() throws SQLException {
    
        connectToDatabase();
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
            System.out.printf("id=%d, name=%s, score=%s%n", id, username, password);
            users.add(new User(id,username,password));
        }
        System.out.println("Read success");

        // Close the result set, statement, and connection
        rs.close();
        stmt.close();
//        conn.close();
        return users;
    }

    private void addUser(int id, String name, String password) throws SQLException {
        connectToDatabase();

        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, password);
        pstmt.executeUpdate();

        pstmt.close();
        conn.close();
    }

    public class PopUpState extends BaseMenu {

        private final String labelString;
        private final String buttonString;
        private BaseAppState orginState;


        public PopUpState(SimpleApplication app, String labelString, String buttonString, BaseAppState originState) {
            super(app, labelString, buttonString, originState);
            this.buttonString = buttonString;
            this.labelString = labelString;
            this.orginState = originState;      
        }

        @Override
        protected String getTitle() {
            return labelString;
        }

        @Override
        protected List<Button> getButtons() {

            // Add a close button to the popup
            Button closeBtn = new Button(buttonString);
            closeBtn.addClickCommands((Button source) -> {
                stateManager.detach((AppState) this);           
                stateManager.attach(orginState);        
            });
            myWindow.addChild(closeBtn);
            closeBtn.setColor(ColorRGBA.Blue);

            return Arrays.asList(closeBtn);

        }

    }



}
