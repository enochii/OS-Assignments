package Utils;

import javax.swing.*;
import java.awt.*;

public class BaseButton extends JButton {
    Color originalCol = null;
    boolean isOn = false;

    public void turnoff(){
        if(!isOn)return;
        isOn = false;
        super.setBackground(originalCol);
    }
    public void turnon(){
        if(isOn)return;
        isOn = true;
        originalCol = super.getBackground();
        super.setBackground(Color.RED);
    }
    public BaseButton(String val){
        super(val);
    }
}
