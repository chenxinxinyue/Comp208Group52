/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import java.util.List;
import org.lwjgl.opengl.Display;

/**
 *
 * @author lsgb
 */
public abstract class BaseMenu extends BaseAppState {

    protected final Node guiNode;
    protected final AppStateManager stateManager;
    protected Container myWindow;
    AssetManager assetManager;
    private BitmapFont font;
    private Button[] buttons;
    private int selectedButtonIndex = 0;
    private InputManager inputManager;
    private Container popupContainer;
    Application app;

    public BaseMenu(SimpleApplication app) {
        guiNode = app.getGuiNode();
        stateManager = app.getStateManager();
        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();
    }
    
    public BaseMenu(SimpleApplication app, BaseAppState state){ 
        guiNode = app.getGuiNode();
        stateManager = app.getStateManager();
        assetManager = app.getAssetManager();   
        inputManager = app.getInputManager();
    }
    
    public BaseMenu(SimpleApplication app, boolean isOnline){ 
        guiNode = app.getGuiNode();
        stateManager = app.getStateManager();
        assetManager = app.getAssetManager();        
        inputManager = app.getInputManager();
    }
    
    public BaseMenu(SimpleApplication app, String label, String button, BaseAppState originState){
        guiNode = app.getGuiNode();
        stateManager = app.getStateManager();
        assetManager = app.getAssetManager();        
        inputManager = app.getInputManager();
    }    
    
    @Override
    protected void initialize(Application app) {
        // Initialize the globals access so that the default
        // components can find what they need.
        GuiGlobals.initialize(app);        

        this.app = app;
        
        // Create a simple container for our elements
        myWindow = new Container();

        // Add child elements to the container
        // Attach the container to the guiNode
        guiNode.attachChild(myWindow);

        myWindow.setPreferredSize(new Vector3f(Display.getWidth() / 2f, Display.getHeight() / 2f, 0));

        float centerX = Display.getWidth() / 2f;
        float centerY = Display.getHeight() / 2f;

        // Calculate the center position of the container
        float containerWidth = myWindow.getPreferredSize().getX();
        float containerHeight = myWindow.getPreferredSize().getY();
        float containerX = centerX - (containerWidth / 2f);
        float containerY = centerY + (containerHeight / 2f);

//        myWindow.setBorder(new QuadBackgroundComponent(ColorRGBA.DarkGray,1,1));

        // Set the position of the container to be centered on the screen
        myWindow.setLocalTranslation(containerX, containerY, 0);    
//        InputStream fontStream = getClass().getResourceAsStream("/fonts/Gochi-Hand.ttf");
//        Font customFont = Font.createTrueTypeFont("Gochi-Hand", fontStream);
//        assetManager = new DesktopAssetManager(true);
        font = assetManager.loadFont("Interface/Fonts/HanziPenSC.fnt");

        Label title = new Label(getTitle());                               
        title.setFont(font);
        
        myWindow.addChild(title);

        for (Button button : getButtons()) {            
            myWindow.addChild(button);
            button.setFont(font);
        } 
//        initKeys();
//        inputManager.addRawInputListener(new InputListener(myWindow));

        createPopup();
        initKeys();
        setChildren();                            
    }
    
    protected void initKeys() {
        /* You can map one or several inputs to one named mapping. */
        inputManager.addMapping("Up",  new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down",  new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Enter",  new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));
        /* Add the named mappings to the action listeners. */
        inputManager.addListener(analogListener, "Up","Down", "Enter");
        inputManager.addListener(actionListener,"Exit");
    }
    
    final private ActionListener actionListener = new ActionListener() {       

        @Override
        public void onAction(String name, boolean isPressed, float f) {
            if (name.equals("Exit") && isPressed) {
//                guiNode.detachChild();

                guiNode.attachChild(popupContainer);                    
                System.out.println(guiNode.getChildren());
            }
        }        
      
    };
    
    final private AnalogListener analogListener = new AnalogListener() {        
        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (name.equals("Up")) {
                selectPreviousButton();
            }
            if (name.equals("Down")) {
                selectNextButton();
            }  
            if (name.equals("Enter")) {

            }  
        }
    };
    
    @Override
    protected void cleanup(Application app) {
        inputManager.clearMappings();
        guiNode.detachChild(myWindow);
        guiNode.detachChild(popupContainer);

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    protected abstract String getTitle();
    protected abstract List<Button> getButtons();

    protected void setChildren() {   
        // Iterate over the child elements and set the color of buttons and labels to black
        for (Spatial child : myWindow.getChildren()) {
            
            if (child instanceof Button ) {
                Button button = (Button) child;
                button.setColor(ColorRGBA.Black);
//                button.setBorder(new QuadBackgroundComponent(ColorRGBA.DarkGray,1,1));
                button.setInsets(new Insets3f(1, 0, 1, 0));
                button.setTextHAlignment(HAlignment.Center);
                button.setTextVAlignment(VAlignment.Center);            
                // Calculate the preferred size of the button
                Vector3f size = button.getPreferredSize();
                // Calculate the font size to fit the button area
                float fontSize = Math.min(size.x, size.y);
                // Set the font size for the button
                button.setFontSize(fontSize);                
            } else if (child instanceof Label) {               
                Label label = (Label) child;
                label.setColor(ColorRGBA.Black);
                label.setInsets(new Insets3f(1, 0, 1, 0));
//                label.setBorder(new QuadBackgroundComponent(ColorRGBA.DarkGray,1,1));
                label.setTextHAlignment(HAlignment.Center);
                label.setTextVAlignment(VAlignment.Center);            
                // Calculate the preferred size of the label
                Vector3f size = label.getPreferredSize();
                // Calculate the font size to fit the label area
                float fontSize = Math.min(size.x, size.y)*2;
                // Set the font size for the label
                label.setFontSize(fontSize);   
            } 
        }
    }

    private void selectPreviousButton() {
        selectedButtonIndex++;
        if (selectedButtonIndex >= buttons.length) {
            selectedButtonIndex = 0;
        }
        setSelectedButton(selectedButtonIndex);
    }

    private void selectNextButton() {
        selectedButtonIndex--;
        if (selectedButtonIndex < 0) {
            selectedButtonIndex = buttons.length - 1;
        }
        setSelectedButton(selectedButtonIndex);
    }

    private void setSelectedButton(int selectedButtonIndex) {

    }

    private void createPopup() {
        popupContainer = new Container();
        Label titleLabel = popupContainer.addChild(new Label("Are you sure you wanna leave?"));
        Button yesBtn = popupContainer.addChild(new Button("Yes"));
        Button noBtn = popupContainer.addChild(new Button("No"));


        // Set the size and position of the container
        popupContainer.setLocalTranslation(
            (Display.getWidth() - popupContainer.getPreferredSize().x) / 2,
            (Display.getHeight() - popupContainer.getPreferredSize().y) / 2,
            0
        );
        popupContainer.setBackground(new QuadBackgroundComponent(ColorRGBA.Red));
                
        // Add the container to the GUI node
        
        // When "ESC" is pressed, remove the container from the GUI node
//        inputManager.addMapping());
//        inputManager.addListener(new ActionListener() {
//            @Override
//            public void onAction(String name, boolean isPressed, float tpf) {
//                if (name.equals("Exit") && isPressed) {
//                    guiNode.detachChild(myWindow);
//                    guiNode.attachChild(popupContainer);                    
//                }
//            }
//        }, "Exit");
        
        yesBtn.addClickCommands((Button source) -> {
            app.stop();
        });  
        noBtn.addClickCommands((Button source) -> {            
//            guiNode.attachChild(myWindow);
            popupContainer.removeFromParent();           
        });  
    }
    

}


