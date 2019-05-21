//created by sch001, 2019/4/15

package Controller;

import Elevator.Elevator;
import Utils.BaseButton;
import Utils.FloorFLags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

class OuterButton extends BaseButton{
    private static String[] buttonFlags = new String[]{"🔻", "🔺"};

    private int floor;//楼层
    //1表示向上，0表示向下
    private int direction;//按钮只能向上或向下
    OuterButton(int floor, int up){
        super(buttonFlags[up]);
        this.floor = floor;
        this.direction = up;
    }

    int getFloor(){
        return floor;
    }
    int getDirection(){
        return direction;
    }
}

//多个楼层的按钮
public class Floors extends JPanel{
    //UI
    //上行和下行的按钮
    private OuterButton[] buttons = new OuterButton[Config.MaxFloor*2];
    //显示当前楼层
    private JLabel[] floorFlags = new JLabel[Config.MaxFloor];
    private Controller controller;

//    TODO: paintComponent()

    public Floors(Controller controller){
        //绑定Controller和Floors
        this.controller = controller;
        controller.setFloors(this);

        setLayout(null);
        //[1, 39)，对应1层没有向下和20层没有向上
        for(int i = 1;i< Config.MaxFloor*2 - 1;i++){
            buttons[i] = new OuterButton(Config.MaxFloor - i/2,(i+1)%2);
            //init
            buttons[i].setMargin(new Insets(1,1,1,1));
            buttons[i].setFont(new Font(buttons[i].getFont().getFontName(),
                    buttons[i].getFont().getStyle(),15));
            buttons[i].setBounds((i%2+1)*(Config.floorButtonWidth+Config.floorButtonSpace),
                    (i/2)*(Config.floorButtonSpace+Config.floorButtonHeight),
//                    0,0,
                    Config.floorButtonWidth,Config.floorButtonHeight
                    );
//            System.out.println((i%2)*(Config.floorButtonWidth+Config.floorButtonSpace));
//            System.out.println((i/2+1)*(Config.floorButtonSpace+Config.floorButtonHeight));
            buttons[i].setBackground(Color.WHITE);
            buttons[i].setForeground(Color.BLACK);
            buttons[i].addActionListener(buttonListener);

            this.add(buttons[i]);
        }

        FloorFLags.addFloorFLags(floorFlags,this,Config.floorFlagStart);
    }
    //TODO: MOdify this fuction to commit a Task
    ActionListener buttonListener= event -> {
        OuterButton pressButton=(OuterButton) event.getSource();
//        System.out.println(pressButton.getFloor()+" "+pressButton.getDirection());
        pressButton.turnon();//为你亮灯
        controller.commitTask(new Task(pressButton.getFloor(),pressButton.getDirection()));
    };

    public void turnoffLight(int floor, int up){
//        System.out.println("Turn off" + ((1-up) + 2*(floor - 1)));
        buttons[(Config.MaxFloor - floor)*2+(1-up)].turnoff();
    }
}

//
