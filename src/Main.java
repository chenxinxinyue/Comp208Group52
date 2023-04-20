/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;
import com.jme3.math.Vector3f;
import java.sql.Connection;

/**
 *
 * @author lsgb
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
//        AppSettings settings = new AppSettings(true);
//        settings.setResolution(800, 600);
//        settings.setFullscreen(false);
//        settings.setVSync(true);
//        settings.setSettingsDialogImage(null); // Disable the display settings dialog        
//        app.setSettings(settings);
//        app.setShowSettings(false); // Disable the display settings dialog
        Main app = new Main();
        app.setShowSettings(false);
        AppSettings settings = new AppSettings(true);
        settings.put("Width", 1280);
        settings.put("Height", 720);
        settings.put("Title", "My awesome Game");
        settings.put("VSync", true);
        //Anti-Aliasing
        settings.put("Samples", 4);
        app.setSettings(settings);
        app.start();
    }
    private Connection conn;
    private String userIP;

    @Override
    public void simpleInitApp() {

        inputManager = getInputManager();
        inputManager.clearMappings();
        inputManager.addRawInputListener(new InputListener(guiNode));

        Picture background = new Picture("Background");
        background.setImage(assetManager, "Textures/background.jpg", true);
        background.setWidth(settings.getWidth());
        background.setHeight(settings.getHeight());
        guiNode.attachChild(background);
        stateManager.attach(new Enter(this));
        cam.setLocation(new Vector3f((float) 8f, (float) 4.5f, 11));
        this.flyCam.setEnabled(false);

    }
}
