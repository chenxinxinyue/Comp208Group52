/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

/**
 *
 * @author CZF
 */
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.DefaultRangedValueModel;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import org.lwjgl.opengl.Display;

public class Settingspage extends BaseMenu {

    private Checkbox soundCheckbox;
    private Label brightnessLabel;
    private Slider brightnessSlider;
    private TextField resolutionTextField;
    BaseAppState originState;
    private Button okButton;
    private Button cancelButton;
    SimpleApplication app;
    
    public Settingspage(SimpleApplication app, BaseAppState originState) {
        super(app,  originState);
        this.originState = originState;
        this.app = app;
    }

    @Override
    protected void initialize(Application app) {
        super.initialize(app);     
        
        // Create the settings screen container
        myWindow.setBackground(new QuadBackgroundComponent(new ColorRGBA(0.2f, 0.2f, 0.2f, 1)));

//        Label titlelabel = new Label("settings page");
        // Create the sound checkbox
        soundCheckbox = new Checkbox("Sound Enabled");
        soundCheckbox.setChecked(true);

        // Create the brightness label and slider
        brightnessLabel = new Label("Brightness:");

        DefaultRangedValueModel brightnessModel = new DefaultRangedValueModel(0, 1, 0.5f);
        brightnessSlider = new Slider(brightnessModel, Axis.X);
        brightnessSlider.setPreferredSize(new Vector3f(200, 0, 0));

        // Create the resolution text field
        Label resolutionLabel = new Label("Resolution:");

        resolutionTextField = new TextField("800x600");
        resolutionTextField.setPreferredWidth(200);

        // Set up the layout of the settings container
//        SpringGridLayout layout;
//        layout = new SpringGridLayout(Axis.Y, Axis.X,FillMode.None, FillMode.None);
        myWindow.setPreferredSize(new Vector3f(Display.getWidth() / 2f, Display.getHeight() / 2f, 0));
       
        float centerX = Display.getWidth() / 2f;
        float centerY = Display.getHeight() / 2f;

        // Calculate the center position of the container
        float containerWidth = myWindow.getPreferredSize().getX();
        float containerHeight = myWindow.getPreferredSize().getY();
        float containerX = centerX - (containerWidth / 2f);
        float containerY = centerY + (containerHeight / 2f);

//        // Set the position of the container to be centered on the screen
        myWindow.setLocalTranslation(containerX, containerY, 0);


        // Create a new Container and set its layout to the SpringGridLayout

        
//        myWindow.addChild(titlelabel);
        myWindow.addChild(soundCheckbox);
        myWindow.addChild(brightnessLabel);
        myWindow.addChild(brightnessSlider);
        myWindow.addChild(resolutionLabel);
        myWindow.addChild(resolutionTextField);
        myWindow.addChild(okButton);
        myWindow.addChild(cancelButton);

        // Add the settings container to the GUI node
        guiNode.attachChild(myWindow);

        // Position the settings container in the center of the screen
//        myWindow.setLocalTranslation(myWindow.getPreferredSize().divide(2).mult(-1));


    }


    private void saveSettings() {
        
        AppSettings settings = new AppSettings(true);
                
        String resolution = resolutionTextField.getText();
        String[] results = resolution.split("x");
        int h = Integer.parseInt(results[0]);
        int w = Integer.parseInt(results[1]);
        
        settings.put("Width", h);
        settings.put("Height", w);
        
        app.setSettings(settings);       
        
        app.restart();            
        
        System.out.println(Display.getHeight());
        System.out.println(Display.getWidth());
//        
//        // Get the current settings values
//        boolean soundEnabled = soundCheckbox.isChecked();
////        double brightnessValue = 
//        String resolutionValue = resolutionTextField.getText();
//
//        // Save the settings values to a preferences file
//        Preferences prefs = Preferences.userNodeForPackage(Main.class);
//        prefs.putBoolean("soundEnabled", soundEnabled);
////        prefs.putDouble("brightnessValue", brightnessValue);
//        prefs.put("resolutionValue", resolutionValue);
//
//        // Notify the user that the settings have been saved
//        System.out.println("Settings saved.");
    }  

    @Override
    protected String getTitle() {
        return "Setting Page";        
    }

    @Override
    protected List<Button> getButtons() {
        okButton = new Button("OK");
        okButton.addClickCommands(source -> saveSettings());

        cancelButton = new Button("Cancel");
        cancelButton.addClickCommands((Button source) -> {
            stateManager.detach(this);
            stateManager.attach(originState);
        });
        
        return Arrays.asList(okButton, cancelButton);      
    }
}

