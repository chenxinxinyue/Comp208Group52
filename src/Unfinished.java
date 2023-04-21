/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Button;
import java.util.Arrays;
import java.util.List;

public class Unfinished extends BaseMenu {

    BaseAppState baseMenuState;

    public Unfinished(SimpleApplication app, BaseAppState state) {
        super(app, state);
        this.baseMenuState = state;
    }

    @Override
    protected String getTitle() {
        return "The page is still under developing...";
    }

    @Override
    protected List<Button> getButtons() {

        Button backBtn = myWindow.addChild(new Button("Return"));
        backBtn.addClickCommands((Button source) -> {
            stateManager.detach(this);
            stateManager.attach(baseMenuState);

        });
        return Arrays.asList(backBtn);
    }
}
