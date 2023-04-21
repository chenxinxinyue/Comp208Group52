/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Button;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mygame.Global.getGlobalLocalPlayerID;

/**
 *
 * @author USR
 */
class LobbyState extends BaseMenu {

    /**
     * @param clientSocket the clientSocket to set
     */
//    public void setClientSocket(Socket clientSocket) {
//        this.clientSocket = clientSocket;
//    }
    Button startBtn;
    Button exitBtn;
    BaseAppState originState;
    private String lobbyID;
    private ArrayList<String> playerIDList;

    private PrintWriter lobbyPrintWriter;
    private Scanner lobbyInputScanner;
    private Socket clientSocket;
    private float loopTimer;
    private final int targetPlayerAmount = 2;

    public LobbyState(SimpleApplication app, BaseAppState originState) {
        super(app, originState);
        this.originState = originState;
        this.playerIDList = new ArrayList<>();
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app);

        try {
            setConnection();
        } catch (Exception ex) {
            Logger.getLogger(LobbyState.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.toString());
        }
        lobbyPrintWriter.println("RequestForLobbyList");
        playerIDList.add("");
        playerIDList.add("");
        playerIDList.add("");
        playerIDList.add("");

        loopTimer = 0.0f;
    }

    @Override
    protected String getTitle() {
        return "Room";
    }

    @Override
    protected List<Button> getButtons() {
        exitBtn = new Button("Exit the room");

        exitBtn.addClickCommands((Button source) -> {

            lobbyPrintWriter.println("LeaveRoomMessage," + lobbyID + "," + getGlobalLocalPlayerID());

            closeTheSocket();

            stateManager.attach(originState);
            stateManager.detach((AppState) this);
        });

        startBtn = new Button("Game start");

        startBtn.addClickCommands((Button source) -> {
//            setConnection();
//            closeTheSocket();
//            setConnection();
            lobbyPrintWriter.println("GameStart," + lobbyID);
//            closeTheSocket();
            System.out.println("Game start! shove it");
        });

        return Arrays.asList(exitBtn, startBtn);

    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        String receivedMessage = "";

        loopTimer -= tpf;
        if (loopTimer < 0) {
            loopTimer = 0.1f;
//            lobbyPrintWriter.println("UpdateClientSocket," + lobbyID + "," + getGlobalLocalPlayerID());
        lobbyPrintWriter.println("RequestForLobbyList");
        }

//        setConnection();
//        System.out.println("HERE");
        if (clientSocket != null) {

            try {
                if (clientSocket.getInputStream().available() > 0) {

                    receivedMessage = lobbyInputScanner.nextLine();
                    System.out.println("ReceivedMessage: " + receivedMessage);
                }
            } catch (IOException ex) {
                Logger.getLogger(LobbyState.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            setConnection();
        }

        String[] messageParts = receivedMessage.split(",");
        if (messageParts[0].equals("NewPlayer")) {
            if (messageParts.length == 2) {
//                playerIDList.add(messageParts[1]);

                for (int i = 0; i < 4; i++) {
                    if (playerIDList.get(i).equals("")) {
                        playerIDList.set(i, messageParts[1]);
                        break;
                    }
                }
                System.out.println("Player list updated accoding to online info to: " + playerIDList.toString());
            }
        }
        if (messageParts[0].equals("PlayerLeft")) {
            if (messageParts.length == 2) {
//                playerIDList.remove(messageParts[1]);

                for (int i = 0; i < 4; i++) {
                    if (playerIDList.get(i).equals(messageParts[1])) {
                        playerIDList.set(i, "");
                        break;
                    }
                }
                System.out.println(playerIDList.toString());
            }
        }
        if (messageParts[0].equals("LobbyListReply")) {

            System.out.println("Before these: " + playerIDList.toString());
            for (int i = 1; i < messageParts.length; i++) {
                if (messageParts[i].contains(getGlobalLocalPlayerID())) {
                    System.out.println("Players: " + messageParts[i]);

                    ArrayList<String> nameList = new ArrayList<String>(Arrays.asList(messageParts[i].split("\\|")));
                    while (nameList.size() < 4) {
                        nameList.add("");
                    }
                    playerIDList = nameList;
                }
            }
            System.out.println(playerIDList.toString());
        }

//        if (messageParts[0].equals("GameStartConfirmed")) {
        int playerAmount = 0;

        for (String id : playerIDList) {
            if (!id.equals("")) {
                playerAmount++;
            }
        }
        if (playerAmount >= targetPlayerAmount) {
            System.out.println("Starting");
            GameplayState gameplayState = new GameplayState((SimpleApplication) this.getApplication());
//            gameplayState.setClientSocket(clientSocket);
            gameplayState.setLobbyID(lobbyID);
            gameplayState.setLocalPlayerID((int) playerIDList.indexOf((String) getGlobalLocalPlayerID())); // localPlayer is the xth out of at most four
            gameplayState.setPlayerIDList(playerIDList);
            gameplayState.setPlayerAmount(playerAmount);
            System.out.println("one more step");
            stateManager.attach(gameplayState);
            stateManager.detach((AppState) this);
            System.out.println("started");
        }

    }

    /**
     * @param lobbyID the lobbyID to set
     */
    public void setLobbyID(String lobbyID) {
        this.lobbyID = lobbyID;
    }

    public void addFirstPlayer(String firstPlayerID) {
        this.playerIDList.add(firstPlayerID);
    }

    /**
     * @param playerIDList the playerIDList to set
     */
    public void setPlayerIDList(ArrayList<String> playerIDList) {
        this.playerIDList = playerIDList;
    }

    private void setConnection() {
        if (clientSocket == null) {
            try {
//                clientSocket = new Socket("localhost", 1234);
                clientSocket = new Socket("34.125.1.151", 1234);
                lobbyPrintWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                lobbyInputScanner = new Scanner(clientSocket.getInputStream());
//                lobbyPrintWriter.println("UpdateClientSocket," + lobbyID + "," + getGlobalLocalPlayerID());
            } catch (IOException ex) {
                System.out.println("connect failed");
                System.out.println(ex.toString());
            }
        }
    }

    private void closeTheSocket() {
        lobbyPrintWriter.close();
        lobbyInputScanner.close();
        try {
            clientSocket.close();
            clientSocket = null;
        } catch (IOException ex) {
            Logger.getLogger(PVP_Enter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
