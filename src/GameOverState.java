/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Label;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.opengl.Display;

/**
 *
 * @author lsgb
 */
public class GameOverState extends BaseMenu{

    private final GameplayState state;
    String s;
    private Button b;

    public GameOverState(SimpleApplication app,GameplayState state) {
        super(app,state);
        this.state = state;
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app); 
//        boolean isAlive = state.getLocalPlayer().getUserData("alive");

        if ("localPlayer".equals(state.getPlayerRootNode().getChild(0).getName())){
            s = "YOU WIN";
        } else {
            s = "YOU LOSE";
        }
        
        Label l = new Label("");
        l.setText(s);
        l.setColor(ColorRGBA.White);
        b.setColor(ColorRGBA.White);
        myWindow.addChild(l);
        myWindow.addChild(b);
//        Picture background = new Picture("Background");
//        background.setImage(assetManager, "Textures/background.jpg", true);
//        background.setWidth(Display.getWidth());
//        background.setHeight(Display.getHeight());
//        myWindow.attachChild(background);
        l.setFontSize(60);
        
//        l.setLocalTranslation(myWindow.getLocalTranslation());
//        l.setLocalScale(3);      
        }

    @Override
    protected String getTitle() {        
        return "Game Over";
    }

    @Override
    protected List<Button> getButtons() {
        b = new Button("Back to menu");
        
        b.addClickCommands((Button source) -> {
            stateManager.detach(this);
            stateManager.attach(new MainMenu((SimpleApplication) app));
            
        });
     
        return Arrays.asList(b);
    }
    
}
