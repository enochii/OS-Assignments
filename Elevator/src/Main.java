//created by sch001, 2019/4/15

import Controller.Controller;
import Controller.Floors;

import javax.swing.*;
import java.awt.*;
import Controller.Config;
import Elevator.Elevator;

class View extends JFrame{
    View(){
        super();
        //init
        setSize(1500,750);

        //五部电梯
        Elevator[] elevators = new Elevator[Config.ElevatorNum];
        for(int i = 0;i<Config.ElevatorNum;i++){
            elevators[i] = new Elevator(Config.WindowHeight - Config.innerTotalButtony, this);
            elevators[i].setBounds(Config.elevatorStart + i*(Config.innerTotalButtonx+Config.elevatorSpace),
                    0 ,Config.innerTotalButtonx+Config.elevatorSpace,Config.WindowHeight);
            elevators[i].start();
        }

        Controller controller = new Controller(elevators);
        controller.start();
        //
        Floors floorView = new Floors(controller);
        floorView.setBackground(Color.WHITE);
        floorView.setBounds(1180,0,(Config.floorButtonWidth+Config.floorButtonSpace)*3,
                (Config.floorButtonHeight+Config.floorButtonSpace)*Config.MaxFloor);
        add(floorView);

        this.setVisible(true);
        this.setResizable(false);
    }

}

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to Sch001's Elevator Simulator!");

        View frame = new View();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
