package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mygame.Global.getGlobalLocalPlayerID;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class GameplayState extends BaseAppState {

    private final AppStateManager stateManager;

    /**
     * @param playerIDList the playerIDList to set
     */
    public void setPlayerIDList(ArrayList<String> playerIDList) {
        this.playerIDList = playerIDList;
    }

    /**
     * @param localPlayerIDSeq the localPlayerIDSeq to set
     */
    public void setLocalPlayerID(int localPlayerIDSeq) {
        this.localPlayerIDSeq = localPlayerIDSeq;
    }

    private SimpleApplication app;
    private AssetManager assetManager;
    private Node rootNode;
    private BitmapFont guiFont;
    private InputManager inputManager;
//    private BaseAppState baseMenuState;
    private AppStateManager appStateManager;

    private int width, height;
    private Node objectRootNode;
    boolean[][] obstacleMap;

    private Socket clientSocket;
    private String lobbyID;
    private String[] playerActionList = new String[4];

//    public GameplayState(SimpleApplication app, BaseAppState state) {
    public GameplayState(SimpleApplication app) {
        this.app = app;
        assetManager = this.app.getAssetManager();
        stateManager = app.getStateManager();
        rootNode = this.app.getRootNode();
        inputManager = this.app.getInputManager();
//        this.baseMenuState = state;
        this.appStateManager = this.app.getStateManager();
    }

    private Node localPlayer;
    private Node playerRootNode;

    private int playerAmount;
    private int localPlayerIDSeq;
    private ArrayList<String> playerIDList;

    private final Vector3f[] playerLocationList = {new Vector3f(2.0f, 2.0f, 0.001f), new Vector3f(4.0f, 6.0f, 0.001f), new Vector3f(8.0f, 2.0f, 0.001f), new Vector3f(9.0f, 5.0f, 0.001f),};

    private float loopTimer;
    private int currentSection;

//    Vector3f initialPosition;
//    float initialX, initialY;
    float playerMovementTimeElapsed, projectileMovementTimeElapsed;
    float lastPositionIncrement;

    private MovementDirectionCase[] selectedMovementDirection;
    private ProjectileDirectionCase[] selectedProjectileDirection;
    private String[] selectedActionMessage;
    private actionCase playerAction;
//    Geometry movementDirectionPointers, projectileDirectionPointers, timeIndicator;
    int timerStage;

    ArrayList<Spatial> projectileToBeRemovedList = new ArrayList<>();

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private long lastKeyPressTime;
    private final long KEY_PRESS_DELAY = 50; // milliseconds
    private int newProjectileTimeToLive;

    private boolean newLocalPlayProjectileCreated[];
    private Node projectileRootNode;

    private Scanner inputScanner;
    private PrintWriter printWriter;

    private float updateCheckLoopTimer;

    public Node getPlayerRootNode() {
        return playerRootNode;
    }

    public Node getLocalPlayer() {
        return localPlayer;
    }

//    private boolean collisionDetectionDone;
    @Override
    public void initialize(Application app) {
        this.app.getGuiNode().detachAllChildren();

        Node backgroundNode = new Node("Background");
        this.app.getRootNode().attachChild(backgroundNode);

        objectRootNode = new Node("objectRootNode");
        rootNode.attachChild(objectRootNode);

//        guiFont = app.getAssetManager().loadFont("Interface/Fonts/ArialBlackItalic.fnt");
        width = 16;
        height = 9;
        obstacleMap = new boolean[width][height];
        // ======
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                obstacleMap[i][j] = i == 0 || j == 0 || i == width - 1 || j == height - 1;
            }
        }

        obstacleMap[4][2] = true;
        obstacleMap[4][3] = true;
        obstacleMap[4][4] = true;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (obstacleMap[i][j]) {
                    createObstacle(i, j, -1);
                }
            }
        }

        // ======
        Geometry paperBackgroundGeom = new Geometry("BackgroundGeom", new Quad(width, height));
        Material paperBackgroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        Texture backgroundTex = assetManager.loadTexture("Textures/chessBoard.bmp");
        Texture paperBackgroundTex = assetManager.loadTexture("Textures/background.jpg");
        paperBackgroundMat.setTexture("ColorMap", paperBackgroundTex);
        paperBackgroundGeom.setMaterial(paperBackgroundMat);
        backgroundNode.attachChild(paperBackgroundGeom);

        Geometry backgroundGeom = new Geometry("BackgroundGeom", new Quad(width, height));
        Material backgroundMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
//        Texture backgroundTex = assetManager.loadTexture("Textures/chessBoard.bmp");
        Texture backgroundTex = assetManager.loadTexture("Textures/Picture1.png");
        backgroundMat.setTexture("ColorMap", backgroundTex);
        backgroundGeom.setMaterial(backgroundMat);
        backgroundNode.attachChild(backgroundGeom);

        this.app.getCamera().setLocation(new Vector3f((float) width / 2, (float) height / 2, 11));
        this.app.getFlyByCamera().setEnabled(false);

        playerRootNode = new Node("playerRootNode");
        objectRootNode.attachChild(playerRootNode);

        if (playerAmount == 0) {
            playerAmount = 1;
        }

        int playerCounter = 0;
        for (String playerID : playerIDList) {
            if (!playerID.equals("")) {
                if (playerID.equals(getGlobalLocalPlayerID())) {

                    localPlayer = new Node("localPlayer");
//                    localPlayer.setUserData("alive", true);
                    createLocalPlayer(playerCounter + 1);
//                    localPlayer.setLocalTranslation(new Vector3f(2f, 2f, 0.001f));
                } else {
                    Node newPlayer = createPlayer(playerCounter + 1);
                    newPlayer.setName("remotePlayer: " + playerID);
                    playerRootNode.attachChild(newPlayer);
                }
                playerRootNode.getChild(playerCounter).setLocalTranslation(playerLocationList[playerCounter]);
                playerCounter++;
            }
        }
        timerStage = 1;

        guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");

        playerMovementTimeElapsed = 0;
        projectileMovementTimeElapsed = 0;

        loopTimer = 4.0f;
        currentSection = 0;

        selectedMovementDirection = new MovementDirectionCase[4];
        for (int i = 0; i < 4; i++) {
            selectedMovementDirection[i] = MovementDirectionCase.NONE;
        }

        playerAction = actionCase.MOVE;

        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        lastKeyPressTime = 0;
        initKeys();

        newProjectileTimeToLive = 3;
        selectedProjectileDirection = new ProjectileDirectionCase[4];
        newLocalPlayProjectileCreated = new boolean[4];
        for (int i = 0; i < 4; i++) {
            selectedProjectileDirection[i] = ProjectileDirectionCase.NONE;
            newLocalPlayProjectileCreated[i] = false;
        }

        projectileRootNode = new Node();
        objectRootNode.attachChild(projectileRootNode);
        lastPositionIncrement = 0;

        setConnection();

        for (int i = 0; i < 4; i++) {
            playerActionList[i] = "None";
        }

        updateCheckLoopTimer = 0.0f;
    }

    private Node createPlayer(int number) {
        Node newPlayer = new Node();

        Geometry playerGeom = new Geometry("playerGeom", new Quad(1, 1));
        Material playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture playerTex = assetManager.loadTexture("Textures/p" + number + ".png");
        playerMat.setTexture("ColorMap", playerTex);
        playerMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        playerGeom.setQueueBucket(Bucket.Transparent);
        playerGeom.setMaterial(playerMat);
//        playerGeom.move(0, 0, 0.01f);
        newPlayer.attachChild(playerGeom);
        playerRootNode.attachChild(newPlayer);

        Geometry timeIndicator = new Geometry("time indicator", new Quad(2f, 2f));
        updateTimeIndicator(timeIndicator, 1);
        newPlayer.attachChild(timeIndicator);
        timeIndicator.setLocalTranslation(new Vector3f(-0.5f, -0.5f, 0.001f));

        return newPlayer;
    }

    private void createLocalPlayer(int number) {
        localPlayer = createPlayer(number);
        localPlayer.setName("localPlayer");

        Geometry movementDirectionPointers = new Geometry("movement direction pointers", new Quad(3, 3));
        Material movementDirectionPointersMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture movementDirectionPointersTex = assetManager.loadTexture("Textures/moveNone.png");
        movementDirectionPointersMat.setTexture("ColorMap", movementDirectionPointersTex);
        movementDirectionPointersMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        movementDirectionPointers.setQueueBucket(Bucket.Transparent);
        movementDirectionPointers.setMaterial(movementDirectionPointersMat);
        localPlayer.attachChild(movementDirectionPointers);
        movementDirectionPointers.setLocalTranslation(new Vector3f(-1f, -1f, 0.001f));

        Geometry projectileDirectionPointers = new Geometry("projectile direction pointers", new Quad(3, 3));
        Material projectileDirectionPointersMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture projectileDirectionPointersTex = assetManager.loadTexture("Textures/blank.png");
        projectileDirectionPointersMat.setTexture("ColorMap", projectileDirectionPointersTex);
        projectileDirectionPointersMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        projectileDirectionPointers.setQueueBucket(Bucket.Transparent);
        projectileDirectionPointers.setMaterial(projectileDirectionPointersMat);
        projectileDirectionPointers.setLocalTranslation(new Vector3f(-1f, -1f, 0.001f));
        localPlayer.attachChild(projectileDirectionPointers);
    }

    @Override
    protected void cleanup(Application aplctn) {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        rootNode.detachAllChildren();
    }

    @Override
    protected void onEnable() {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    protected void onDisable() {
//        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public enum MovementDirectionCase {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        NONE,
        UNAVAILABLE
    }

    public enum ProjectileDirectionCase {
        UP,
        UPLEFT,
        UPRIGHT,
        DOWN,
        DOWNLEFT,
        DOWNRIGHT,
        LEFT,
        RIGHT,
        NONE,
        UNAVAILABLE
    }

    public enum actionCase {
        NONE,
        MOVE,
        SHOOT
    }

    private enum projectionReflectionCase {
        VERTICAL_OR_HORIZONTAL,
        DIAGONAL_CASE_XY,
        DIAGONAL_CASE_X,
        DIAGONAL_CASE_Y,
        NONE
    }

    @Override
    public void update(float tpf) {

        if (playerRootNode.getChildren().size() == 1) {
            stateManager.detach(this);
            stateManager.attach(new GameOverState((SimpleApplication) this.getApplication(), this));
        }

        loopTimer -= tpf;
        String receivedMessage = "";

        updateCheckLoopTimer -= tpf;

        if (updateCheckLoopTimer < 0) {
            updateCheckLoopTimer = 0.3f;
        }

        if (updateCheckLoopTimer < 0.15f && updateCheckLoopTimer > 0.10f) {
            printWriter.println("GameStatusRequest," + lobbyID + "," + getGlobalLocalPlayerID() + "," + currentSection);
            System.out.println("Sending GST");
        }

        if (updateCheckLoopTimer > 0.25f) {
            System.out.println("Sending updating info");
            printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
        }

        try {
            if (clientSocket != null && clientSocket.getInputStream().available() > 0) {

                receivedMessage = inputScanner.nextLine();
                if (!receivedMessage.equals("")) {
                    System.out.println(receivedMessage);
                    String[] messageParts = receivedMessage.split(",");
                    if (messageParts[0].equals("PlayerActionReply") && messageParts.length == 5) {
                        receiveOnlinePlayerActionMessage(messageParts);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GameplayState.class.getName()).log(Level.SEVERE, null, ex);
        }

        // ========== Switches to MOVE ====================
        if (loopTimer < 0 && playerAction == actionCase.SHOOT) {
            loopTimer = 4.0f;
            currentSection++;

            for (int i = 0; i < 4; i++) {
                selectedMovementDirection[i] = MovementDirectionCase.NONE;
            }

            playerAction = actionCase.MOVE;

            Spatial movementDirectionPointers = localPlayer.getChild("movement direction pointers");
            Spatial projectileDirectionPointers = localPlayer.getChild("projectile direction pointers");
            updateProjectileDirectionPointers((Geometry) projectileDirectionPointers, ProjectileDirectionCase.UNAVAILABLE);
            updateMovementDirectionPointers((Geometry) movementDirectionPointers, MovementDirectionCase.NONE);

            // Cleaning
            upPressed = false;
            downPressed = false;
            leftPressed = false;
            rightPressed = false;
            for (int i = 0; i < 4; i++) {
                selectedProjectileDirection[i] = ProjectileDirectionCase.NONE;
                newLocalPlayProjectileCreated[i] = false;
            }

            projectileMovementTimeElapsed = 0;

            playerMovementTimeElapsed = 0;
            System.out.println("Switches to MOVE");
            lastPositionIncrement = 0;

            ArrayList<Spatial> playerToBeRemoved = new ArrayList<>();
            ArrayList<Spatial> crashedProjectiles = new ArrayList<>();

            for (Spatial projectile : projectileRootNode.getChildren()) {
                Vector3f projectileLocation = projectile.getLocalTranslation();
                int roundedX = Math.round(projectileLocation.x);
                int roundedY = Math.round(projectileLocation.y);
                projectile.setLocalTranslation(roundedX, roundedY, 0);
                System.out.println(projectile.getLocalTranslation().toString());

                // --------------------------- player got shot ------------------------------
                int tombIndex = 0;
                for (Spatial player : playerRootNode.getChildren()) {
                    tombIndex++;
                    if (player.getLocalTranslation().x == roundedX && player.getLocalTranslation().y == roundedY && !player.getName().equals(projectile.getName())) {
                        playerToBeRemoved.add(player);
                        crashedProjectiles.add(projectile);
                        createObstacle(roundedX, roundedY, tombIndex);
                    }
                }
            }

            for (Spatial player : playerToBeRemoved) {
                playerRootNode.detachChild(player);
            }

            for (Spatial projectile : crashedProjectiles) {
                projectileRootNode.detachChild(projectile);
            }

            // --------------------------- player got shot ------------------------------
        }
        // ========== Switches to MOVE ====================

        // ========== Switches to SHOOT ===================
        if (loopTimer < 0 && playerAction == actionCase.MOVE) {
            loopTimer = 4.0f;

            // -----------Cleaning----------------------------
            for (Spatial player : playerRootNode.getChildren()) {
                Vector3f currentPosition = player.getLocalTranslation();

                player.setLocalTranslation(Math.round(currentPosition.x), Math.round(currentPosition.y), currentPosition.z);
            }

//            System.out.println("Current position: " + localPlayer.getLocalTranslation());
            for (Spatial projectile : projectileRootNode.getChildren()) {
                int currentTTL = (int) projectile.getUserData("time to live") - 1;

                Vector3f projPos = projectile.getLocalTranslation();
                projectile.setLocalTranslation(Math.round(projPos.x), Math.round(projPos.y), 0);
                System.out.println("updated projectile location: " + projectile.getLocalTranslation());

                projectile.setUserData("collision detection done", false);

                projectile.setUserData("time to live", currentTTL);
                if (currentTTL <= 0) {
                    projectileToBeRemovedList.add(projectile);
                }
            }

            for (Spatial projectile : projectileToBeRemovedList) {
                projectileRootNode.detachChild(projectile);
            }

            for (int i = 0; i < 4; i++) {
                selectedProjectileDirection[0] = ProjectileDirectionCase.NONE;
            }

            Spatial movementDirectionPointers = localPlayer.getChild("movement direction pointers");
            Spatial projectileDirectionPointers = localPlayer.getChild("projectile direction pointers");
            updateMovementDirectionPointers((Geometry) movementDirectionPointers, MovementDirectionCase.UNAVAILABLE);
            updateProjectileDirectionPointers((Geometry) projectileDirectionPointers, ProjectileDirectionCase.NONE);
//            playerMovementTimeElapsed = 0;
            lastPositionIncrement = 0;
//            collisionDetectionDone = false;
            // -----------Cleaning----------------------------

            playerAction = actionCase.SHOOT;
            for (int i = 0; i < 4; i++) {
                newLocalPlayProjectileCreated[i] = false;
            }
            System.out.println("Switches to SHOOT");

            ArrayList<Spatial> playerToBeRemoved = new ArrayList<>();
            ArrayList<Spatial> crashedProjectiles = new ArrayList<>();

            for (Spatial projectile : projectileRootNode.getChildren()) {
                Vector3f projectileLocation = projectile.getLocalTranslation();
                int roundedX = Math.round(projectileLocation.x);
                int roundedY = Math.round(projectileLocation.y);
                projectile.setLocalTranslation(roundedX, roundedY, 0);
                System.out.println(projectile.getLocalTranslation().toString());

                // --------------------------- player got shot ------------------------------
                int tombIndex = 0;
                for (Spatial player : playerRootNode.getChildren()) {
                    tombIndex++;
                    if (player.getLocalTranslation().x == roundedX && player.getLocalTranslation().y == roundedY && !player.getName().equals(projectile.getName())) {
                        playerToBeRemoved.add(player);
                        crashedProjectiles.add(projectile);
                        createObstacle(roundedX, roundedY, tombIndex);
                    }
                }
            }

            for (Spatial player : playerToBeRemoved) {

                playerRootNode.detachChild(player);
            }

            for (Spatial projectile : crashedProjectiles) {
                projectileRootNode.detachChild(projectile);
            }
            // --------------------------- player got shot ------------------------------
        }
        // ========== Switches to SHOOT ===================

        // ********** MOVES *******************************
        if (loopTimer < 1 && playerAction == actionCase.MOVE) {
            playerMovementTimeElapsed += tpf;

            float newPositionIncrement = playerMovementTimeElapsed < 0.5f ? 2 * playerMovementTimeElapsed * playerMovementTimeElapsed : (-2 * playerMovementTimeElapsed * playerMovementTimeElapsed) + (4 * playerMovementTimeElapsed) - 1;
//            System.out.println(newPositionIncrement - lastPositionIncrement);

            int movementAnimationloopCounter = 0;
            for (Spatial player : playerRootNode.getChildren()) {
//                System.out.println("playerID: " + player.getName() + "uses Info #" + movementAnimationloopCounter);
                switch (selectedMovementDirection[movementAnimationloopCounter]) {
                    case NONE:
                        break;
                    case UP:
                        player.move(0, newPositionIncrement - lastPositionIncrement, 0);
                        break;
                    case LEFT:
                        player.move(-newPositionIncrement + lastPositionIncrement, 0, 0);
                        break;
                    case DOWN:
                        player.move(0, -newPositionIncrement + lastPositionIncrement, 0);
                        break;
                    case RIGHT:
                        player.move(newPositionIncrement - lastPositionIncrement, 0, 0);
                        break;
                }
                movementAnimationloopCounter++;
            }

            lastPositionIncrement = newPositionIncrement;
        }
        // ********** MOVES *******************************

        // ########## Creates projectiles #################
        int projectileCreationLoopCounter = 0;
        for (Spatial player : playerRootNode.getChildren()) {

            if (!newLocalPlayProjectileCreated[projectileCreationLoopCounter] && playerAction == actionCase.SHOOT && loopTimer <= 1.0f) {
                newLocalPlayProjectileCreated[projectileCreationLoopCounter] = true;
                if (selectedProjectileDirection[projectileCreationLoopCounter] != ProjectileDirectionCase.NONE) {
                    int playerPosX = (int) player.getLocalTranslation().x;
                    int playerPosY = (int) player.getLocalTranslation().y;
                    createProjectile(playerPosX, playerPosY, selectedProjectileDirection[projectileCreationLoopCounter], newProjectileTimeToLive, player.getName(), projectileCreationLoopCounter);
                }
            }
            projectileCreationLoopCounter++;
        }
        // ########## Creates projectiles #################

        // ********** Updates projectiles *****************
        if (loopTimer < 1.0f && loopTimer >= 0.0f && playerAction == actionCase.SHOOT) {
            projectileMovementTimeElapsed += tpf;
            // Collision Detection
            for (Spatial projectile : projectileRootNode.getChildren()) {
                Vector3f projPos = projectile.getLocalTranslation();
                int xMovement = (int) ((Vector3f) projectile.getUserData("velocity")).x;
                int yMovement = (int) ((Vector3f) projectile.getUserData("velocity")).y;

                if (!(boolean) projectile.getUserData("collision detection done")) {
                    projectile.setUserData("collision detection done", true);
                    projectionReflectionCase reflectionType = projectionReflectionCase.NONE;

                    if (obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y + yMovement)]
                            && (projectile.getUserData("type").equals("UP")
                            || projectile.getUserData("type").equals("DOWN")
                            || projectile.getUserData("type").equals("LEFT")
                            || projectile.getUserData("type").equals("RIGHT"))) {
                        reflectionType = projectionReflectionCase.VERTICAL_OR_HORIZONTAL;
                        changeProjectileDirection(projectile, reflectionType);
                    }

                    if ((projectile.getUserData("type").equals("UPLEFT")
                            || projectile.getUserData("type").equals("DOWNRIGHT")
                            || projectile.getUserData("type").equals("DOWNLEFT")
                            || projectile.getUserData("type").equals("UPRIGHT"))) {

                        if ((obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y + yMovement)]
                                && !obstacleMap[Math.round(projPos.x)][Math.round(projPos.y + yMovement)]
                                && !obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y)])
                                || (obstacleMap[Math.round(projPos.x)][Math.round(projPos.y + yMovement)]
                                && obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y)])) {
                            System.out.println("xy case");
                            reflectionType = projectionReflectionCase.DIAGONAL_CASE_XY;
                            changeProjectileDirection(projectile, reflectionType);
                        }

                        if (obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y + yMovement)]
                                && obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y)]
                                && !obstacleMap[Math.round(projPos.x)][Math.round(projPos.y + yMovement)]) {
                            reflectionType = projectionReflectionCase.DIAGONAL_CASE_X;
                            changeProjectileDirection(projectile, reflectionType);
                        }

                        if (obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y + yMovement)]
                                && !obstacleMap[Math.round(projPos.x + xMovement)][Math.round(projPos.y)]
                                && obstacleMap[Math.round(projPos.x)][Math.round(projPos.y + yMovement)]) {
                            reflectionType = projectionReflectionCase.DIAGONAL_CASE_Y;
                            changeProjectileDirection(projectile, reflectionType);
                        }
                    }
                }

                projectile.move(((Vector3f) projectile.getUserData("velocity")).mult(tpf));
            }
        }
        // ********** Updates projectiles *****************

        // ########## Updates the timer ###################
        for (float i = 1; i < 19; i++) {
            if ((loopTimer > 4.0f - (i + 1) / 6 && loopTimer < 4.0f - i / 6) && timerStage != i) {
                timerStage = (int) i;

                for (Spatial player : playerRootNode.getChildren()) {
                    if (player instanceof Node) {
                        Node playerNode = (Node) player;
                        Spatial timeIndicator = playerNode.getChild("time indicator");
                        updateTimeIndicator((Geometry) timeIndicator, timerStage);
                    }
                }

            }
        }

        for (float i = 1; i < 17; i++) {
            if ((loopTimer > 0.9375 - i / 16.0f && loopTimer < 0.9375 - (i - 1) / 16.0f) && timerStage != 17 - i + 1) {
                timerStage = 17 - (int) i + 1;

                for (Spatial player : playerRootNode.getChildren()) {
                    if (player instanceof Node) {
                        Node playerNode = (Node) player;
                        Spatial timeIndicator = playerNode.getChild("time indicator");
                        updateTimeIndicator((Geometry) timeIndicator, timerStage);
                    }
                }
            }
        }
        // ########## Updates the timer ###################
    }

    private void initKeys() {
        /* You can map one or several inputs to one named mapping. */
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("W", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("A", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("S", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("D", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_Q));

        inputManager.addMapping("UP", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("DOWN", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("LEFT", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("RIGHT", new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addListener(actionListener, "Pause",
                "W", "A", "S", "D", "Quit",
                "UP", "DOWN", "LEFT", "RIGHT");
//        inputManager.addListener(analogListener, "UP", "DOWN", "LEFT", "RIGHT");

    }

    final private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Quit")) {
                System.out.println("quit");
                returnToMenu();
            }

            int playerPosX = (int) localPlayer.getLocalTranslation().x;
            int playerPosY = (int) localPlayer.getLocalTranslation().y;

            if (loopTimer > 1.0f && playerAction == actionCase.MOVE) {
                Geometry movementDirectionPointers = (Geometry) localPlayer.getChild("movement direction pointers");
                if (name.equals("UP") && !obstacleMap[playerPosX][playerPosY + 1]) {
                    selectedMovementDirection[localPlayerIDSeq] = MovementDirectionCase.UP;

                    updateMovementDirectionPointers(movementDirectionPointers, selectedMovementDirection[localPlayerIDSeq]);
                    playerActionList[localPlayerIDSeq] = "movementDirectionUP";
                    printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                }
                if (name.equals("LEFT") && !obstacleMap[playerPosX - 1][playerPosY]) {
                    selectedMovementDirection[localPlayerIDSeq] = MovementDirectionCase.LEFT;

                    updateMovementDirectionPointers(movementDirectionPointers, selectedMovementDirection[localPlayerIDSeq]);
                    playerActionList[localPlayerIDSeq] = "movementDirectionLEFT";
                    printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                }
                if (name.equals("DOWN") && !obstacleMap[playerPosX][playerPosY - 1]) {
                    selectedMovementDirection[localPlayerIDSeq] = MovementDirectionCase.DOWN;

                    updateMovementDirectionPointers(movementDirectionPointers, selectedMovementDirection[localPlayerIDSeq]);
                    playerActionList[localPlayerIDSeq] = "movementDirectionDOWN";
                    printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                }
                if (name.equals("RIGHT") && !obstacleMap[playerPosX + 1][playerPosY]) {
                    selectedMovementDirection[localPlayerIDSeq] = MovementDirectionCase.RIGHT;

                    updateMovementDirectionPointers(movementDirectionPointers, selectedMovementDirection[localPlayerIDSeq]);
                    playerActionList[localPlayerIDSeq] = "movementDirectionRIGHT";
                    printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                }
            }

            if (loopTimer > 1.0f && playerAction == actionCase.SHOOT) {
                Geometry projectileDirectionPointers = (Geometry) localPlayer.getChild("projectile direction pointers");

                if (name.equals("UP")) {
                    upPressed = keyPressed;
                    // Register vertical direction
                    if (keyPressed) {
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.UP;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionUP";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                    }
                } else if (name.equals("DOWN")) {
                    downPressed = keyPressed;
                    // Register vertical direction
                    if (keyPressed) {
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.DOWN;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionDOWN";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                    }
                } else if (name.equals("LEFT")) {
                    leftPressed = keyPressed;
                    // Register horizontal direction
                    if (keyPressed) {
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.LEFT;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionLEFT";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                    }
                } else if (name.equals("RIGHT")) {
                    rightPressed = keyPressed;
                    // Register horizontal direction
                    if (keyPressed) {
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.RIGHT;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionRIGHT";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                    }
                }

                // Check for diagonal direction
                if (keyPressed) {
                    if (upPressed && leftPressed && (System.currentTimeMillis() - lastKeyPressTime) >= KEY_PRESS_DELAY) {
                        // Up-Left direction
//                        System.out.println("Up-Left");
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.UPLEFT;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionUPLEFT";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                        lastKeyPressTime = System.currentTimeMillis();
                    } else if (upPressed && rightPressed && (System.currentTimeMillis() - lastKeyPressTime) >= KEY_PRESS_DELAY) {
                        // Up-Right direction
//                        System.out.println("Up-Right");
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.UPRIGHT;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionUPRIGHT";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                        lastKeyPressTime = System.currentTimeMillis();
                    } else if (downPressed && leftPressed && (System.currentTimeMillis() - lastKeyPressTime) >= KEY_PRESS_DELAY) {
                        // Down-Left direction
//                        System.out.println("Down-Left");
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.DOWNLEFT;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionDOWNLEFT";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                        lastKeyPressTime = System.currentTimeMillis();
                    } else if (downPressed && rightPressed && (System.currentTimeMillis() - lastKeyPressTime) >= KEY_PRESS_DELAY) {
                        // Down-Right direction
//                        System.out.println("Down-Right");
                        selectedProjectileDirection[localPlayerIDSeq] = ProjectileDirectionCase.DOWNRIGHT;
                        updateProjectileDirectionPointers(projectileDirectionPointers, selectedProjectileDirection[localPlayerIDSeq]);
                        playerActionList[localPlayerIDSeq] = "projectileDirectionDOWNRIGHT";
                        printWriter.println("PlayerAction," + lobbyID + "," + getGlobalLocalPlayerID() + "," + playerActionList[localPlayerIDSeq] + "," + currentSection);
                        lastKeyPressTime = System.currentTimeMillis();
                    }
                } else {
                    // Key released, reset corresponding flag
                    switch (name) {
                        case "UP":
                            upPressed = false;
                            break;
                        case "DOWN":
                            downPressed = false;
                            break;
                        case "LEFT":
                            leftPressed = false;
                            break;
                        case "RIGHT":
                            rightPressed = false;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };

    private void receiveOnlinePlayerActionMessage(String[] messageParts) {
        for (int i = 0; i < 4; i++) {
            playerActionList[i] = messageParts[i + 1];

            switch (playerActionList[i]) {
                case "movementDirectionUP":
                    selectedMovementDirection[i] = MovementDirectionCase.UP;
                    break;
                case "movementDirectionDOWN":
                    selectedMovementDirection[i] = MovementDirectionCase.DOWN;
                    break;
                case "movementDirectionLEFT":
                    selectedMovementDirection[i] = MovementDirectionCase.LEFT;
                    break;
                case "movementDirectionRIGHT":
                    selectedMovementDirection[i] = MovementDirectionCase.RIGHT;
                    break;
                case "projectileDirectionUP":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.UP;
                    break;
                case "projectileDirectionDOWN":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.DOWN;
                    break;
                case "projectileDirectionLEFT":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.LEFT;
                    break;
                case "projectileDirectionRIGHT":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.RIGHT;
                    break;
                case "projectileDirectionUPLEFT":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.UPLEFT;
                    break;
                case "projectileDirectionUPRIGHT":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.UPRIGHT;
                    break;
                case "projectileDirectionDOWNLEFT":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.DOWNLEFT;
                    break;
                case "projectileDirectionDOWNRIGHT":
                    selectedProjectileDirection[i] = ProjectileDirectionCase.DOWNRIGHT;
                    break;

                default:
//                    selectedMovementDirection[i] = MovementDirectionCase.NONE;
//                    selectedProjectileDirection[i] = ProjectileDirectionCase.NONE;
                    break;
            }
        }
    }

//    final private AnalogListener analogListener = new AnalogListener() {
//        @Override
//        public void onAnalog(String name, float value, float tpf) {
//
//        }
//    };
    private void createObstacle(int x, int y, int playerNum) {
        Geometry obsGeom = new Geometry("obsGeom_" + x + "_" + y, new Quad(1, 1));
        Material obsMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture obsTex;
        obsTex = assetManager.loadTexture("Textures/tileSrc3.png");
        if (playerNum != -1) {
            obsTex = assetManager.loadTexture("Textures/Gravestone" + playerNum + ".png");
        }
        obsMat.setTexture("ColorMap", obsTex);
        obsMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        obsGeom.setQueueBucket(Bucket.Transparent);
        obsGeom.setMaterial(obsMat);
        obsGeom.setLocalTranslation(new Vector3f(x, y, 0.002f));
        objectRootNode.attachChild(obsGeom);
    }

    private void updateMovementDirectionPointers(Geometry movementDirectionPointers, MovementDirectionCase dir) {

        Material newMovementDirectionPointersMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture newMovementDirectionPointersTex = assetManager.loadTexture("Textures/moveNone.png");
        switch (dir) {
            case UP:
                newMovementDirectionPointersTex = assetManager.loadTexture("Textures/moveUp" + (localPlayerIDSeq + 1) + ".png");
                break;
            case DOWN:
                newMovementDirectionPointersTex = assetManager.loadTexture("Textures/moveDown" + (localPlayerIDSeq + 1) + ".png");
                break;
            case LEFT:
                newMovementDirectionPointersTex = assetManager.loadTexture("Textures/moveLeft" + (localPlayerIDSeq + 1) + ".png");
                break;
            case RIGHT:
                newMovementDirectionPointersTex = assetManager.loadTexture("Textures/moveRight" + (localPlayerIDSeq + 1) + ".png");
                break;
            case NONE:
                break;
            case UNAVAILABLE:
                newMovementDirectionPointersTex = assetManager.loadTexture("Textures/blank.png");
                break;
        }
        newMovementDirectionPointersMat.setTexture("ColorMap", newMovementDirectionPointersTex);
        newMovementDirectionPointersMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        movementDirectionPointers.setQueueBucket(Bucket.Transparent);
        movementDirectionPointers.setMaterial(newMovementDirectionPointersMat);
    }

    private void updateProjectileDirectionPointers(Geometry projectileDirectionPointers, ProjectileDirectionCase dir) {
        Material newProjectileDirectionPointersMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootNone.png");
        switch (dir) {
            case UP:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootU" + (localPlayerIDSeq + 1) + ".png");
                break;
            case DOWN:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootD" + (localPlayerIDSeq + 1) + ".png");
                break;
            case LEFT:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootL" + (localPlayerIDSeq + 1) + ".png");
                break;
            case RIGHT:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootR" + (localPlayerIDSeq + 1) + ".png");
                break;
            case UPLEFT:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootUL" + (localPlayerIDSeq + 1) + ".png");
                break;
            case DOWNLEFT:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootDL" + (localPlayerIDSeq + 1) + ".png");
                break;
            case UPRIGHT:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootUR" + (localPlayerIDSeq + 1) + ".png");
                break;
            case DOWNRIGHT:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/shootDR" + (localPlayerIDSeq + 1) + ".png");
                break;
            case NONE:
                break;
            case UNAVAILABLE:
                newprojectileDirectionPointersTex = assetManager.loadTexture("Textures/blank.png");
                break;
        }
        newProjectileDirectionPointersMat.setTexture("ColorMap", newprojectileDirectionPointersTex);
        newProjectileDirectionPointersMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        projectileDirectionPointers.setQueueBucket(Bucket.Transparent);
        projectileDirectionPointers.setMaterial(newProjectileDirectionPointersMat);
    }

    private void updateTimeIndicator(Geometry timeIndicator, int stage) {

        Material newTimeIndicatorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture newTimeIndicatorTex = assetManager.loadTexture("Textures/timer" + stage + ".png");

        newTimeIndicatorMat.setTexture("ColorMap", newTimeIndicatorTex);
        newTimeIndicatorMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        timeIndicator.setQueueBucket(Bucket.Transparent);
        timeIndicator.setMaterial(newTimeIndicatorMat);
    }

    private void createProjectile(int playerPosX, int playerPosY, ProjectileDirectionCase dir, int ttl, String playerID, int playerSeq) {
        playerSeq++;
//        System.out.println(playerPosX + ", " + playerPosY);
        Vector3f velocity = new Vector3f();

        Geometry newProj = new Geometry("playerID", new Quad(1, 1));
        newProj.setName(playerID);
        Material newProjMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

//        if (dir == ProjectileDirectionCase.DOWN || dir == ProjectileDirectionCase.RIGHT || dir == ProjectileDirectionCase.LEFT || dir == ProjectileDirectionCase.UP) {
//            newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1.png"));
//        } else {
//            newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile2.png"));
//        }
        newProj.setLocalTranslation(0f, 0f, 0.002f);
        switch (dir) {
            case DOWN:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Down" + playerSeq + ".png"));
                velocity = new Vector3f(0, -1, 0);
                break;
            case UP:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Up" + playerSeq + ".png"));
                velocity = new Vector3f(0, 1, 0);
                break;
            case LEFT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Left" + playerSeq + ".png"));
                velocity = new Vector3f(-1, 0, 0);
                break;
            case UPLEFT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile2UL" + playerSeq + ".png"));
                velocity = new Vector3f(-1, 1, 0);
                break;
            case DOWNRIGHT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile2DR" + playerSeq + ".png"));
                velocity = new Vector3f(1, -1, 0);
                break;
            case DOWNLEFT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile2DL" + playerSeq + ".png"));
                velocity = new Vector3f(-1, -1, 0);
                break;
            case RIGHT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Right" + playerSeq + ".png"));
                velocity = new Vector3f(1, 0, 0);
                break;
            case UPRIGHT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile2UR" + playerSeq + ".png"));
                velocity = new Vector3f(1, 1, 0);
                break;
        }

        newProjMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        newProj.setQueueBucket(Bucket.Transparent);
        newProj.setMaterial(newProjMat);
        projectileRootNode.attachChild(newProj);
        newProj.move(playerPosX, playerPosY, 0f);
        newProj.setUserData("time to live", ttl);
        newProj.setUserData("velocity", velocity);
        newProj.setUserData("type", dir.toString());
        newProj.setUserData("collision detection done", false);
    }

    private void changeProjectileDirection(Spatial projectile, projectionReflectionCase rType) {
        Material newProjMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ProjectileDirectionCase dir = ProjectileDirectionCase.valueOf(projectile.getUserData("type"));
        Vector3f velocity = projectile.getUserData("velocity");

        int playerSeq = 0;
        for (Spatial player : playerRootNode.getChildren()) {
            playerSeq++;
            if (projectile.getName().equals(player.getName())) {
                break;
            }
        }

        switch (dir) {
            case DOWN:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Up" + playerSeq + ".png"));
                projectile.setUserData("velocity", new Vector3f(0, 1, 0));
                projectile.setUserData("type", "UP");
                break;
            case UP:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Down" + playerSeq + ".png"));
                projectile.setUserData("velocity", new Vector3f(0, -1, 0));
                projectile.setUserData("type", "DOWN");
                break;
            case LEFT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Right" + playerSeq + ".png"));
                projectile.setUserData("velocity", new Vector3f(1, 0, 0));
                projectile.setUserData("type", "RIGHT");
                break;
            case RIGHT:
                newProjMat.setTexture("ColorMap", assetManager.loadTexture("Textures/Projectile1Left" + playerSeq + ".png"));
                projectile.setUserData("velocity", new Vector3f(-1, 0, 0));
                projectile.setUserData("type", "LEFT");
                break;
            case UPLEFT:
                changeDiagonalProjectile(rType, newProjMat, projectile, velocity);
                break;
            case DOWNRIGHT:
                changeDiagonalProjectile(rType, newProjMat, projectile, velocity);
                break;
            case DOWNLEFT:
                changeDiagonalProjectile(rType, newProjMat, projectile, velocity);
                break;
            case UPRIGHT:
                changeDiagonalProjectile(rType, newProjMat, projectile, velocity);
                break;
        }

        newProjMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        projectile.setMaterial(newProjMat);
    }

    private void changeDiagonalProjectile(projectionReflectionCase rType, Material newProjMat, Spatial projectile, Vector3f velocity) {
        int index = 0;
        for (Spatial player : playerRootNode.getChildren()) {
            index++;
            if (player.getName().equals(projectile.getName())) {
                break;
            }
        }

        if (rType == projectionReflectionCase.DIAGONAL_CASE_XY) {
            Vector3f newVel = new Vector3f(-velocity.x, -velocity.y, 0);
            newProjMat.setTexture("ColorMap", assetManager.loadTexture(getDiagonalProjectileTexturePath(newVel, index)));
            projectile.setUserData("velocity", newVel);
            projectile.setUserData("type", getDiagonalProjectileType(newVel));
        }

        if (rType == projectionReflectionCase.DIAGONAL_CASE_X) {
            Vector3f newVel = new Vector3f(-velocity.x, velocity.y, 0);
            projectile.setUserData("velocity", newVel);
            newProjMat.setTexture("ColorMap", assetManager.loadTexture(getDiagonalProjectileTexturePath(newVel, index)));
            projectile.setUserData("type", getDiagonalProjectileType(newVel));
        }

        if (rType == projectionReflectionCase.DIAGONAL_CASE_Y) {
            Vector3f newVel = new Vector3f(velocity.x, -velocity.y, 0);
            projectile.setUserData("velocity", newVel);
            newProjMat.setTexture("ColorMap", assetManager.loadTexture(getDiagonalProjectileTexturePath(newVel, index)));
            projectile.setUserData("type", getDiagonalProjectileType(newVel));
        }

    }

    private String getDiagonalProjectileTexturePath(Vector3f vel, int index) {
        String name = "Textures/Projectile2UL" + index + ".png";

        if (vel.equals(new Vector3f(-1, -1, 0))) {
            name = "Textures/Projectile2DL" + index + ".png";
        }
        if (vel.equals(new Vector3f(1, -1, 0))) {
            name = "Textures/Projectile2DR" + index + ".png";
        }
        if (vel.equals(new Vector3f(1, 1, 0))) {
            name = "Textures/Projectile2UR" + index + ".png";
        }
        if (vel.equals(new Vector3f(-1, 1, 0))) {
            name = "Textures/Projectile2UL" + index + ".png";
        }

        return name;
    }

    private String getDiagonalProjectileType(Vector3f vel) {
        String name = "UPRIGHT";

        if (vel.equals(new Vector3f(-1, -1, 0))) {
            name = "DOWNLEFT";
        }
        if (vel.equals(new Vector3f(1, -1, 0))) {
            name = "DOWNRIGHT";
        }
        if (vel.equals(new Vector3f(1, 1, 0))) {
            name = "UPRIGHT";
        }
        if (vel.equals(new Vector3f(-1, 1, 0))) {
            name = "UPLEFT";
        }

        return name;
    }

//    @Override
//    public void stateDetached(AppStateManager stateManager) {
//        // Detach this AppState from the AppStateManager
//        stateManager.detach(this);
//    }
    public void returnToMenu() {
        rootNode.detachAllChildren();

        Picture background = new Picture("Background");
        background.setImage(assetManager, "Textures/background.jpg", true);
        background.setWidth(app.getContext().getSettings().getWidth());
        background.setHeight(app.getContext().getSettings().getHeight());
        app.getGuiNode().attachChild(background);

        appStateManager.detach(this);
        appStateManager.attach(new Enter(app));
    }

    /**
     * @param playerAmount the playerAmount to set
     */
    public void setPlayerAmount(int playerAmount) {
        this.playerAmount = playerAmount;
    }

    /**
     * @param clientSocket the clientSocket to set
     */
//    public void setClientSocket(Socket clientSocket) {
//        this.clientSocket = clientSocket;
//    }
    /**
     * @param lobbyID the lobbyID to set
     */
    public void setLobbyID(String lobbyID) {
        this.lobbyID = lobbyID;
    }

    private void setConnection() {
        if (clientSocket == null) {
            try {
                clientSocket = new Socket("34.125.1.151", 1234);
//                clientSocket = new Socket("localhost", 1234);
                printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                inputScanner = new Scanner(clientSocket.getInputStream());
            } catch (IOException ex) {
                System.out.println("connect failed");
                System.out.println(ex.toString());
            }
        }
    }

    private void closeTheSocket() {
        printWriter.close();
        inputScanner.close();
        try {
            clientSocket.close();
            clientSocket = null;
        } catch (IOException ex) {
            Logger.getLogger(PVP_Enter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
