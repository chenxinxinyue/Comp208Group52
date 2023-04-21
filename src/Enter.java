/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.simsilica.lemur.Button;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author lsgb
 */
public class Enter extends BaseMenu {

    public Enter(SimpleApplication app) {
        super(app);
    }

//    @Override
//    protected List<Label> getTitles() {
//        Label theme = new Label("Rhythm Hazard.");
//        return Arrays.asList(theme);
//    }
    
    @Override
    protected String getTitle() {
        return "Rhythm Hazard.";
    }
    
    @Override
    protected List<Button> getButtons() {
        Button onlineBtn = new Button("Online");
        onlineBtn.addClickCommands((Button source) -> {
            System.out.println("Enter Online Mode.");
            stateManager.detach(this);
            stateManager.attach(new Login((SimpleApplication) this.getApplication()));
        });

        Button offlineBtn = new Button("Offline");        
        offlineBtn.addClickCommands((Button source) -> {
            stateManager.detach(this);            
            MainMenu state= new MainMenu((SimpleApplication) this.getApplication());
            stateManager.attach(state);
            state.setIsOnline(false);
        });       
        return Arrays.asList(onlineBtn, offlineBtn);
    }

}
