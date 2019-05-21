//created by sch001, 2019/4/15

//电梯是一个实在的线程，在UI层面需要更新的状态有自己的位置/
// 电梯内部按钮的亮暗/
//楼层外部按钮的亮暗/

//在到达一个目标楼层需要把内部/外部的按钮灭了，表示完成任务
//这里可能需要与Controller.Floors交互

//感觉逻辑和UI可以拆成两部分实现，分别对应ElevatorView和Elevator
package Elevator;

import Controller.Config;
import Controller.Controller;
import Utils.BaseButton;
import Utils.FloorFLags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


//按钮好像弄个亮暗状态会好点
class InnerNumButton extends BaseButton {
    int floor = 0;
    public InnerNumButton(int floor, String val){
        super(val);
        this.floor = floor;
    }
    int getFloor(){
        return floor;
    }
}

//这里需要实现Elevator的Inner View 和逻辑
class ElevatorView extends JPanel{

    //楼层按钮，开关门，甚至报警
//    private JButton[] innerButtons = new JButton[20+2+1];
    ArrayList<InnerNumButton> innerButtons = new ArrayList<>();
    JLabel[] floorFlags = new JLabel[20];
    //当前电梯(JLabel)的实例
    JLabel curPos;
    //
    static final int othersNum = 3;
    static final String[] others = {"><",
            "<>",
            "🔔",
            };

    ActionListener NumbuttonListener= event -> {
        InnerNumButton pressButton=(InnerNumButton) event.getSource();
//        System.out.println(pressButton.getFloor());
    };
    //接收一个起始位置（横坐标），造出一个Elevator
    public ElevatorView(int buttonStarty){
        super();
        this.setBorder(BorderFactory.createEtchedBorder());
        setLayout(null);
//        this.setBackground(Color.BLACK);

        //做成一个独立的Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        add(buttonPanel);
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        buttonPanel.setBounds(0, buttonStarty, Config.innerTotalButtonx,Config.innerTotalButtony);

        //楼层按钮
        for(int i = 0;i<Config .MaxFloor;i++){
            innerButtons.add(new InnerNumButton(i+1,""+(i+1)));
            JButton ref = innerButtons.get(i);
            ref.setMargin(new Insets(1,1,1,1));
            ref.setFont(new Font(ref.getFont().getFontName(),
                    ref.getFont().getStyle(),15));

            ref.setBounds(//elevatorStartx + (i%3)*(Config.innerButtonWidth+Config.innerSpace),
                    //(i/3+1)*(Config.innerButtonHeight+Config.innerSpace),
                    (i%Config.buttonsPerLine)*Config.innerButtonWidth,Config.innerSpace+(i/Config.buttonsPerLine)*Config.innerButtonWidth,
                    Config.innerButtonWidth,Config.innerButtonHeight
                    );

            ref.setBackground(Color.GRAY);
            ref.setForeground(Color.DARK_GRAY);
            ref.addActionListener(NumbuttonListener);

            buttonPanel.add(ref);
        }
        //其他内部按钮
        for(int i = 0;i<othersNum;i++){
            innerButtons.add(new InnerNumButton(0,others[i]));
            JButton ref = innerButtons.get(i+Config.MaxFloor);
            ref.setMargin(new Insets(1,1,1,1));
            ref.setFont(new Font(ref.getFont().getFontName(),
                    ref.getFont().getStyle(),15));

            ref.setBounds(//elevatorStartx + (i%3)*(Config.innerButtonWidth+Config.innerSpace),
                    //(i/3+1)*(Config.innerButtonHeight+Config.innerSpace),
                    Config.buttonsPerLine*Config.innerButtonWidth,Config.innerSpace+(i%Config.buttonsPerLine+1)*Config.innerButtonHeight,
                    Config.innerButtonWidth*2,Config.innerButtonHeight
            );
            buttonPanel.add(ref);

            ref.setBackground(Color.GRAY);
            ref.setForeground(Color.DARK_GRAY);
            ref.addActionListener(null);
        }

        //20个楼层，以蓝色标识当前电梯的位置
//        for(int i = 0;i<Config.MaxFloor;i++){
//            floorFlags[i] = new JLabel(""+(i+1));
//            floorFlags[i].setHorizontalAlignment(SwingConstants.CENTER);
//            floorFlags[i].setFont(new Font(floorFlags[i].getFont().getFontName(),
//                    floorFlags[i].getFont().getStyle(),15));
//            floorFlags[i].setBounds(Config.innerTotalButtonx,
//                    i*(Config.floorButtonSpace+Config.floorButtonHeight),
//                    Config.floorButtonWidth,Config.floorButtonHeight
//            );
//            floorFlags[i].setBackground(Color.PINK);
//            floorFlags[i].setOpaque(true);//让颜色设置成功...
//            this.add(floorFlags[i]);
//        }

        //设置z-order好像麻烦的一批，先后add貌似会决定他们的z轴...
        curPos = new JLabel("=||=");
        curPos.setBackground(Color.GRAY);
        curPos.setOpaque(true);

        curPos.setHorizontalAlignment(SwingConstants.CENTER);

        this.add(curPos);

        FloorFLags.addFloorFLags(floorFlags,this,Config.innerTotalButtonx);

        Point floorOne = floorFlags[0].getLocation();
        curPos.setBounds(floorOne.x,floorOne.y,Config.floorButtonWidth,Config.floorButtonHeight);
    }

    //电梯的状态：上行/下行/静止   开门/关门    电梯当前楼层
    //注意开关门时电梯只能处于静止状态

    //目的地s： 这里可能根据电梯的上下行确定目的地的优先级

}


public class Elevator extends Thread{

    public enum EleState{
        DOWN, STALL, UP, PAUSE
    }

    Controller controller;
    ElevatorView elevatorView;
    JLabel curPos;

    //电梯逻辑
    int floor = 1;//起始时在第一楼
    EleState state = EleState.STALL;// -1 -> down, 0 -> stop, 1 -> up
    ArrayList<Boolean> dests = new ArrayList<Boolean>(Collections.nCopies(Config.MaxFloor + 1, Boolean.FALSE));

//    PriorityQueue<Integer> upDests = new PriorityQueue<>();
//    PriorityQueue<Integer> downDests = new PriorityQueue<Integer>(new Comparator<Integer>() {
//        @Override
//        public int compare(Integer i1, Integer i2) {
//            return i1.compareTo(i2);
//        }
//    });
//
//    PriorityQueue<Integer> destinations = upDests;
    public void setController(Controller c){
        controller = c;
    }

    public int getFloor(){
        return floor;
    }

    public Elevator(int buttonStarty, JFrame MainView){
        elevatorView = new ElevatorView(buttonStarty);

        curPos = elevatorView.curPos;
        MainView.add(elevatorView);

        //设置Listener
        ArrayList<InnerNumButton> innerButtons = elevatorView.innerButtons;
        innerButtons.get(Config.MaxFloor).addActionListener(actionEvent->{closeDoor();});
        innerButtons.get(Config.MaxFloor+1).addActionListener(actionEvent->{
            new Thread(){
                public void run(){
                    openDoorThenClose();
                }
            }.start();
        });
        innerButtons.get(Config.MaxFloor+2).addActionListener(actionEvent ->{alert();} );
        for(int i = 0;i<Config.MaxFloor;i++){
            //
            InnerNumButton buttonRef = innerButtons.get(i);
            buttonRef.addActionListener(actionEvent -> {InnerBtnListener(actionEvent);});
        }
        for(int i = 0;i<Config.MaxFloor;i++){
            taskState[i][0] = taskState[i][1] = false;
        }
    }

    //转发给对应的UI
    public void setBounds(int x, int y, int width, int height){
        elevatorView.setBounds(x,y,width,height);
    }

    //实现电梯的移动
    @Override
    public void run(){
        while (true){
            if(state == EleState.STALL){
                try {
                    sleep(1000);
                }catch (InterruptedException e){
                    System.out.println("Cant not sleep in run!");
                }
                continue;
            }
            moveOneFloor(state == EleState.UP);
        }
    }


    synchronized void moveOneFloor(boolean up){
        int distance = Config.floorButtonHeight+Config.floorButtonSpace;
//        int realSpeed = distance / (Config.ElevatorSpeed*5);//每100ms刷新一次
        for(int i = 0;i < distance;i++){
            try {
                sleep(Config.ElevatorMsPerGrid);
            }catch (InterruptedException e){}
            curPos.setLocation(curPos.getLocation().x,curPos.getLocation().y+(up?-1:1));
        }


        //楼层++--
        floor+=(up?1:-1);
        //如果在目标楼层
        if(dests.get(floor)){
            dests.set(floor, Boolean.FALSE);
            //关灯
            turnoffLight(floor);

            //是否需要继续往上/下走
            int limit = state == EleState.UP? (Config.MaxFloor+1):0;
            int step = state == EleState.UP? 1:-1;
            boolean needToContinue = search(limit,step);

            //不用继续，需要反向检查是否有投递的任务
            if(!needToContinue){
                needToContinue = search(Config.MaxFloor + 1 - limit, -step);//反向-=-
                if(!needToContinue){
                    state = EleState.STALL;
                }else {
                    assert state != EleState.STALL;
                    state = (state==EleState.UP?EleState.DOWN:EleState.UP);
                }
            }

            EleState oldState = state;
            state = EleState.PAUSE;
            openDoorThenClose();
            state = oldState;
        }
    }
    //检查某个方向是否有工作--
    private boolean search(int limit, int step){
        boolean needToContinue = false;
        for(int i = floor;i!=limit;i+=step){
            if(dests.get(i)){
                needToContinue = true;
                break;
            }
        }
        return needToContinue;
    }

    //开关门
    private void openDoorThenClose(){
        if(state!=EleState.STALL && state != EleState.PAUSE)return;
        openDoor();
        try {
            sleep(800);//停下来
        }catch (InterruptedException e){}
        closeDoor();
        try {
            sleep(300);//停下来
        }catch (InterruptedException e){}
    }
    private void openDoor(){
        curPos.setText("|   |");
    }
    private void closeDoor(){
        curPos.setText(" =||= ");
    }
    //报警
    private void alert() {
//        currentThread().interrupt();
        //开启新线程报警
        Thread thread = new Thread() {
            public void run() {
                while (true){
                    curPos.setBackground(Color.RED);
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                    }
                    curPos.setBackground(Color.GRAY);
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        thread.start();
    }

    //Commit Task
    //这里用synchronized会卡顿...
    private void InnerBtnListener(ActionEvent event){
        InnerNumButton button = (InnerNumButton) event.getSource();

        button.turnon();
        commitTask(button.floor);
    }

    void commitTask(int requiredFloor){
        //若电梯处于颓废状态，需要激活
        if(EleState.STALL == state){
            if(floor == requiredFloor){
                turnoffLight(floor);
                return;
            }
            state = (floor<requiredFloor?EleState.UP:EleState.DOWN);
        }
        dests.set(requiredFloor, Boolean.TRUE);
    }

    private boolean[][] taskState = new boolean[Config.MaxFloor+1][2];
    public void commitOuterTask(int requiredFloor,EleState dir){
        int index = dir == EleState.UP?1:0;
        taskState[requiredFloor][index] = true;
        commitTask(requiredFloor);
    }

    void turnoffLight(int floor){
        //电梯内的灯
        InnerNumButton button = elevatorView.innerButtons.get(floor - 1);
        button.turnoff();
        //楼层的灯
        for(int i = 0;i<2;i++){
            if(taskState[floor][i]){
                taskState[floor][i] = false;
                controller.turnoffLight(floor, i);
            }
        }
        //
    }

    //
    public EleState getELeState(){
        return state;
    }
}

//感觉InnerTask过于简单可能不需要单拎出来了...
//class InnerTask{
//
//}
