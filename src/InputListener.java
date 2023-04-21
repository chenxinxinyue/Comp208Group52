/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mygame;

import com.jme3.input.KeyInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author lsgb
 */
public class InputListener implements RawInputListener {

    private Node guiNode;
    List<Button> buttons = new ArrayList<>();
    private int buttonNum;
    private int selectedButton;
    private boolean isKeyPressed = false;
    private boolean isSelected = false;
    
    public InputListener(Node guiNode) {
        this.guiNode = guiNode;
    }
    
    @Override
    public void onKeyEvent(KeyInputEvent event) {
        if (event.isReleased()) {
            // Key has been released, reset flag
            isKeyPressed = false;
            return;
        }    
        
        if (isKeyPressed) {
            // Key is already pressed, ignore this event
            return;
        }   
        
        if (event.getKeyCode() == KeyInput.KEY_SPACE || event.getKeyCode() == KeyInput.KEY_RETURN) {
            event.setConsumed();            
        }
//        
//        if ( event.getKeyCode() == KeyInput.KEY_UP ) {
//            isKeyPressed = true;
//            if( selectedButton > 0 ){
//                selectedButton--;
//                setSelectedButton(selectedButton);
//            }     
//        }
//        if ( event.getKeyCode() == KeyInput.KEY_DOWN ) {
//            isKeyPressed = true;
//            if( selectedButton < buttonNum ) {                
//                setSelectedButton(selectedButton);
//                selectedButton++;
//            }
//        }
//        if (event.getKeyCode() == KeyInput.KEY_RETURN) {
//            isKeyPressed = true;
//            for (int i = 0; i < buttonNum; i++) {
//                if ( i == selectedButton ){
////                    System.out.println(buttons.get(i).getClickCommands());
////                    buttons.get(i).addClickCommands(command);                    
//                } 
//            }
//        }
//  
    }
    
    private void setSelectedButton(int i){
        for ( i = 0; i < buttonNum; i++) {
            if ( i == selectedButton ){
                buttons.get(i).setColor(ColorRGBA.Yellow);
            } else {
                buttons.get(i).setColor(ColorRGBA.Black);
            } 
        } 
    }

    @Override
    public void beginInput() {

    }

    @Override
    public void endInput() {

    }

    @Override
    public void onJoyAxisEvent(JoyAxisEvent jae) {

    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent jbe) {

    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent mme) {

        
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent mbe) {

    }

    @Override
    public void onTouchEvent(TouchEvent te) {

    }
}
