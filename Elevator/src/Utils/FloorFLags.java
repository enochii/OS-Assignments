package Utils;

import Controller.Config;

import javax.swing.*;
import java.awt.*;

//其实这就是个标签额，不过在两个Package都会用到，就抽出来写了
public class FloorFLags{
    JLabel[] floorFlags = new JLabel[Config.MaxFloor];

    public static void addFloorFLags(JLabel[] floorFlags, JPanel parent, int startPos){
        for(int i = 0;i<Config.MaxFloor;i++){
            floorFlags[i] = new JLabel(""+(i+1));
            floorFlags[i].setHorizontalAlignment(SwingConstants.CENTER);
            floorFlags[i].setFont(new Font(floorFlags[i].getFont().getFontName(),
                    floorFlags[i].getFont().getStyle(),15));
            floorFlags[i].setBounds(startPos,
                    (Config.MaxFloor - 1 - i)*(Config.floorButtonSpace+Config.floorButtonHeight),
                    Config.floorButtonWidth,Config.floorButtonHeight
            );
            floorFlags[i].setBackground(Color.PINK);
            floorFlags[i].setOpaque(true);//让颜色设置成功...
            parent.add(floorFlags[i]);
        }
    }
}

//感觉可以做个亮暗的interface