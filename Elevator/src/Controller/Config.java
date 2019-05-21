package Controller;//created by sch001, 2019/4/15

//import java.awt.*;

//楼层数 按钮大小 etc
public class Config {
    //妈耶，我发现命名不一致了...
    public final static int MaxFloor = 20;
    public final static int ElevatorNum = 5;
    //ElevatorMsPerGrid是一个速度参数，指的是多少ms移动一个单位距离，设置越大电梯越慢
    //这样设置的原因是setLocation的参数都是int，没法子用double去表达速度的逻辑
    //一层楼的总高度为floorButtonHeight + floorButtonSpace
    public final static int ElevatorMsPerGrid = 20;
    public final static int WindowWidth = 1500;
    public final static int WindowHeight = 750;
    //floor Buttons
    public final static int floorButtonWidth = 50;
    public final static int floorButtonHeight = 25;
    public final static int floorButtonSpace = 10;
    public final static int floorFlagStart = 5;

    //Configs of InnerButton of Elevator
    public final static int innerButtonWidth = 25;
    public final static int innerButtonHeight = 25;
    public final static int innerSpace = 8;//电梯按钮间距
    public final static int buttonsPerLine = 4;

    //五部电梯之间的间距
    public final static int elevatorSpace = 70;
    //从左往右第一部电梯的起始位置
    public final static int elevatorStart = floorFlagStart + 3*(floorButtonSpace+floorButtonWidth);
    public final static int innerTotalButtonx = 5*(innerButtonWidth+innerSpace);
    public final static int innerTotalButtony = 2* innerSpace + 5*(innerButtonHeight+innerSpace);
}
