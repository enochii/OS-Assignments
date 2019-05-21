package Controller;

import Elevator.Elevator.EleState;

//Task在Controller和Floors之间传递，由一次Floors的OuterButton触发一次Task的Commit
public class Task {
    int floor;
    EleState direction;

    public static void CommitTask(int floor, int direction){
        //这里好像可以直接写Commit到Controller的逻辑
        System.out.println("我要去找Controller爸爸了！");
    }
    public Task(int floor, int direction){
        this.floor = floor;
        this.direction = direction == 1?EleState.UP:EleState.DOWN;
    }
}
