package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.Button;
import java.util.Arrays;
import java.util.List;

public class MainMenu extends BaseMenu {

    String username;
    String password;
    private boolean isOnline;

    /**
     *
     * @param app
     */
    public MainMenu(SimpleApplication app) {
        super(app);

    }

    public String getUsername() {
        return username;
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        System.out.println(username + ": " + password);
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    protected String getTitle() {
        if (isOnline) {
            return "Online";
        } else {
            return "Offline";
        }
    }

    @Override
    protected List<Button> getButtons() {

        Button pvpButton = new Button("PVP");
        Button pveButton = new Button("PVE");
        Button setButton = new Button("SETTING");
        Button ttlButton = new Button("TOTURIAL");
        Button checkBtn = new Button("HIGH SCORE");
        Button backButton1 = new Button("CHANGE ACCOUNT");
        Button backButton2 = new Button("BACK");

        pvpButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new PVP_Enter((SimpleApplication) this.getApplication(), this));
        });

        pveButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new PveChooseState((SimpleApplication) this.getApplication(), this));
        });

        setButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new SettingPage((SimpleApplication) this.getApplication(), this));
        });

        ttlButton.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new ToturialPage((SimpleApplication) this.getApplication(), this));
        });
        checkBtn.addClickCommands((Button source) -> {
            stateManager.detach(this);
            stateManager.attach(new CheckPage((SimpleApplication) this.getApplication(), this));
        });
        backButton2.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new Enter((SimpleApplication) this.getApplication()));
        });

        backButton1.addClickCommands((Button source) -> {
            stateManager.detach((AppState) this);
            stateManager.attach(new Login((SimpleApplication) this.getApplication()));
        });

//        List<Command<? super Button>> command = backButton1.getClickCommands();
        if (isOnline) {
            return Arrays.asList(pvpButton, pveButton, setButton, ttlButton, checkBtn, backButton1);
        } else {
            return Arrays.asList(pveButton, setButton, ttlButton, checkBtn, backButton2);
        }
    }

    public class PveChooseState extends BaseMenu {

        private Button level1Btn;
        private Button level2Btn;
        private Button level3Btn;
        int level;
        private Button backBtn;
        private BaseMenu originState;

        public PveChooseState(SimpleApplication app, BaseMenu state) {
            super(app, state);
            this.originState = state;
        }

        @Override
        protected void initialize(Application app) {
            super.initialize(app);

        }

        @Override
        protected String getTitle() {
            return "Choose the Level";
        }

        @Override
        protected List<Button> getButtons() {
            level1Btn = new Button("Level 1");
            level2Btn = new Button("Level 2");
            level3Btn = new Button("Level 3");
            backBtn = new Button("Back");

            level1Btn.addClickCommands((Button source) -> {
                level = 1;
                stateManager.detach((AppState) this);
                stateManager.attach(new GameplayState((SimpleApplication) this.getApplication()));
            });
            level2Btn.addClickCommands((Button source) -> {
                level = 2;
                stateManager.detach((AppState) this);
                stateManager.attach(new PVE((SimpleApplication) this.getApplication(), level));
            });
            level3Btn.addClickCommands((Button source) -> {
                level = 3;
                stateManager.detach((AppState) this);
                stateManager.attach(new PVE((SimpleApplication) this.getApplication(), level));
            });
            backBtn.addClickCommands((Button source) -> {
                level = 3;
                stateManager.detach((AppState) this);
                stateManager.attach(originState);
            });

            return Arrays.asList(level1Btn, level2Btn, level3Btn, backBtn);
        }

    }

    private static class SettingPage extends BaseAppState {

        private SettingPage(SimpleApplication par, MainMenu aThis) {
        }

        @Override
        protected void initialize(Application aplctn) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void cleanup(Application aplctn) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void onEnable() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void onDisable() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }

    private static class ToturialPage extends BaseAppState {

        private ToturialPage(SimpleApplication par, MainMenu aThis) {
        }

        @Override
        protected void initialize(Application aplctn) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void cleanup(Application aplctn) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void onEnable() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void onDisable() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }

    private static class CheckPage extends BaseAppState {

        private CheckPage(SimpleApplication par, MainMenu aThis) {
        }

        @Override
        protected void initialize(Application aplctn) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void cleanup(Application aplctn) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void onEnable() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        protected void onDisable() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

    }

}
