//created by sch001, 2019/4/15

package Controller;

//import java.util.LinkedList;
import Elevator.Elevator;
import Elevator.Elevator.*;

import java.util.LinkedList;
import java.util.Queue;

/*TODO: 理清synchronized 的逻辑，在这里实现Task到InnerTask的转化，并投递到电梯 */
public class Controller extends Thread{
    //拥有的电梯
    Elevator[] elevators;
    //任务队列
    Queue<Task> tasks = new LinkedList<Task>();
    //上一个被调度的电梯，让任务尽量分布均匀
    int lastElevator = 0;

    Floors floors;

    public void setFloors(Floors floors) {
        this.floors = floors;
    }

    public Controller(Elevator[] elevators){
        this.elevators = elevators;
        for(int i =0;i<Config.ElevatorNum;i++){
            this.elevators[i].setController(this);
        }
    }
    //根据投递的任务选择最佳电梯
    @Override
    public void run(){
        //轮询，当无任务就睡眠一手
        while (true){
            if(tasks.isEmpty()){
                try{
                    sleep(1000);
                }catch (InterruptedException e){
                    System.out.println("Controller!");
                }
                continue;
            }
            //调度Task到合适的电梯
            while (!tasks.isEmpty()){
                Task task = tasks.remove();
                schedule(task);
            }
        }
    }
    //
    public synchronized void commitTask(Task task){
        tasks.add(task);
    }

    //调度的逻辑
    void schedule(Task task){
        int start = lastElevator;
        int distance = Config.MaxFloor + 1;//候选电梯离要接的客的距离

        for(int i= 0;i<Config.ElevatorNum;i++){
            start = (start+1)%Config.ElevatorNum;
            if(elevators[start].getELeState() == task.direction){
                //
                if(task.direction==EleState.UP && task.floor>=elevators[start].getFloor()||
                    task.direction==EleState.DOWN && task.floor<=elevators[start].getFloor()
                ){
                    lastElevator = start;
                    break;
                }
            }
            if(elevators[start].getELeState() == EleState.STALL){
                int ndistance = Math.abs(elevators[start].getFloor() - task.floor);
                if(distance > ndistance){
                    lastElevator = start;
                    distance = ndistance;
                }
            }
        }
//        lastElevator = start;
        elevators[lastElevator].commitOuterTask(task.floor, task.direction);
    }

    //关灯
    public void turnoffLight(int floor, int up){
        floors.turnoffLight(floor, up);
    }
}
