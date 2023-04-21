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
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TextField;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mygame.Global.getGlobalLocalPlayerID;

/**
 *
 * @author lsgb
 */
public class PVP_Enter extends BaseMenu {

    private final MainMenu originState;
    private Label roomLabel;
    private TextField roomField;
    private Button createBtn;
    private Button joinBtn;
    private Button backBtn;
    private Socket clientSocket;
    private PrintWriter out;
    private Scanner inputScanner;
    private String playerID;
//    private CreateRoom createRoom;

    public PVP_Enter(SimpleApplication app, BaseAppState state) {
        super(app);
        this.originState = (MainMenu) state;
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app);
        BitmapFont font = assetManager.loadFont("Interface/Fonts/HanziPenSC.fnt");

//        for (Label label : getLabels()) {
//            label.setFont(font);
//            label.setColor(ColorRGBA.Black);
//        }

        roomField = new TextField("");
//            passwordField = new TextField("");

        roomField.setFont(font);
//            passwordField.setFont(font);

        myWindow.addChild(roomLabel);
        myWindow.addChild(roomField);

        myWindow.addChild(createBtn);
        myWindow.addChild(joinBtn);
        myWindow.addChild(backBtn);

        playerID = getGlobalLocalPlayerID();
    }

    @Override
    protected String getTitle() {
        return "";
    }

    @Override
    protected List<Button> getButtons() {
        createBtn = new Button("Create a room");
        joinBtn = new Button("Join a room");
        backBtn = new Button("Back");

        createBtn.addClickCommands((Button source) -> {
//            stateManager.detach(this);
//            stateManager.attach(new CreateRoom((SimpleApplication) this.getApplication(), this));
            setConnection();
            System.out.println("Connect success");
//            closeTheSocket();
//            setConnection();
            out.println("RequestForNewLobby," + playerID + "," + roomField.getText());
        });
        joinBtn.addClickCommands((Button source) -> {
            
            setConnection();

//            closeTheSocket();
//            setConnection();

            out.println("EnterLobbyRequest," + playerID + "," + roomField.getText());
//            stateManager.detach((AppState) this);
//            stateManager.attach(new StartState((SimpleApplication) this.getApplication(),this));    

        });

        backBtn.addClickCommands((Button source) -> {
            closeTheSocket();
            stateManager.attach(originState);
            stateManager.detach((AppState) this);
        });

//        return Arrays.asList(hosterBtn, clientBtn, backBtn);
        return Arrays.asList(createBtn, joinBtn, backBtn);
    }

//    protected List<Label> getLabels() {
//        roomLabel = new Label("Room id or title:");
//        tipLabel = new Label("");
//        return Arrays.asList(roomLabel, tipLabel);
//    }

    @Override
    public void update(float tpf) {

        String receivedMessage = "";
        try {
//            super.update(tpf);

//        System.out.println("HERE");
//            setConnection();
            if (clientSocket != null && clientSocket.getInputStream().available() > 0) {

                receivedMessage = inputScanner.nextLine();
                if (!receivedMessage.equals("")) {
                    System.out.println(receivedMessage);

                }
            }

//            System.out.println("HERE1" + receivedMessage);
            String[] messageParts = receivedMessage.split(",");
            if (messageParts[0].equals("NewLobbyCreated")) {
//                System.out.println("HERE2" + receivedMessage);
                LobbyState newLobbyState = new LobbyState((SimpleApplication) this.getApplication(), this);
                newLobbyState.setLobbyID(playerID);
//                newLobbyState.addFirstPlayer(playerID);
                stateManager.attach(newLobbyState);

                closeTheSocket();

                System.out.println("detaching");
                stateManager.detach((AppState) this);
                System.out.println("detached");
            }
            if (messageParts[0].equals("EnteringLobby")) {
                LobbyState newLobbyState = new LobbyState((SimpleApplication) this.getApplication(), this);
                newLobbyState.setLobbyID(roomField.getText());
//                newLobbyState.setPlayerIDList();
                stateManager.attach(newLobbyState);

                closeTheSocket();

                System.out.println("detaching");
                stateManager.detach((AppState) this);
                System.out.println("detached");

            }
        } catch (IOException ex) {
            Logger.getLogger(PVP_Enter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setConnection() {
        if (clientSocket == null) {
            try {
                clientSocket = new Socket("34.125.1.151", 1234);
//                clientSocket = new Socket("localhost", 1234);
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                inputScanner = new Scanner(clientSocket.getInputStream());
            } catch (IOException ex) {
                System.out.println("connect failed");
                System.out.println(ex.toString());
            }
        }
    }

    private void closeTheSocket() {
        out.close();
        inputScanner.close();
        try {
            clientSocket.close();
            clientSocket = null;
        } catch (IOException ex) {
            Logger.getLogger(PVP_Enter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

//    private class CreateRoom extends BaseMenu {
//
//        PVP_Enter originState;
//        private Button okBtn;
//        private Button returnBtn;
//        private TextField playerNumField;
//
//        @Override
//        protected void initialize(Application app) {
//            super.initialize(app);
//            
//            playerNumField = new TextField("");
//           
//            myWindow.addChild(playerNumField);
//            myWindow.addChild(okBtn);
//            myWindow.addChild(returnBtn);            
//            
//        }
//        
//        public CreateRoom(SimpleApplication app, PVP_Enter state) {
//            super(app, state);
//            this.originState = state;
//        }
//
//        @Override
//        protected String getTitle() {
//            return "Enter player num:";            
//        }
//
//        @Override
//        protected List<Button> getButtons() {
//            okBtn = new Button("OK");
//            
//            returnBtn = new Button("Return");
//            
//            okBtn.addClickCommands((Button source) -> {
//
//                setConnection();
//                System.out.println("Connect success");
//                closeTheSocket();
//                setConnection();
//                out.println("RequestForNewLobby," + playerID + "," + roomField.getText());
//                
//            });
//            
//            returnBtn.addClickCommands((Button source) -> {
//
//                stateManager.detach(this);
//                stateManager.attach(originState);
//                out.println("RequestForNewLobby," + playerID + "," + roomField.getText());
//                
//            });
//            return Arrays.asList(okBtn,returnBtn);
//        }
//    }

}
