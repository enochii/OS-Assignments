package Allocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


// 适应算法的接口
interface fitAlgorithm{
   public Block malloc(int jobIndex, int requiredSize);
   public void free(int jobIndex, int blockSize);
   public List<Block> getEmptyList();
    public List<Block> getBusyList();
}

//首次适应
class firstFit implements fitAlgorithm{
    @Override
    public Block malloc(int jobIndex, int requiredSize){
        return null;
    }

    @Override
    public void free(int jobIndex, int blockSize){

    }
    public List<Block> getEmptyList(){
        return null;
    }
    public List<Block> getBusyList(){
        return null;
    }
}

//最佳适应
class bestFit implements fitAlgorithm{
    public List<Block> getEmptyList(){
        return emptyList;
    }
    public List<Block> getBusyList(){
        return busyList;
    }

    // 空闲链表
    List<Block> emptyList = new LinkedList<Block>();

    //已分配链表
    List<Block> busyList = new LinkedList<Block>();

    public bestFit(){
        emptyList.add(new Block(0, 640));
        System.out.println("BestFit!:"+emptyList.size());
    }

    @Override
    public Block malloc(int jobIndex, int requiredSize){
        Iterator<Block> iterator = emptyList.iterator();
        while (iterator.hasNext()){
            Block block = iterator.next();
            if(block.blockSize > requiredSize){
                iterator.remove();

                Block[] results = Block.splitBlock(block, requiredSize);
                results[1].jobIndex = jobIndex;

                malloc(results[1]);//把后部加入占用链表
                free(results[0]);//把前部重新加入空闲链表

                return results[1];
            }else if (block.blockSize == requiredSize){
                iterator.remove();
                block.jobIndex = jobIndex;
                malloc(block);
                return block;
            }
        }

        System.out.println("No enough Memory!");
        return null;
    }

    @Override
    //找出要释放的内存块，放入emptyList中
    public void free(int jobIndex, int blockSize){
        Iterator<Block> it = busyList.iterator();
        while (it.hasNext()){
            Block block = it.next();
            if(block.blockSize == blockSize && block.jobIndex == jobIndex){
                it.remove();
                free(block);
            }
        }
    }

    //这里的block可能是分割后产生的，也可能是某个Job free的
    private void  free(Block insertedBlock){
        Block prev = null, next = null;
        Iterator<Block> it = emptyList.iterator();

        //找到可能存在的可合并内存块
        while (it.hasNext()){
            Block block = it.next();
            if(block.startAddr + block.blockSize == insertedBlock.startAddr){
                prev = block;
                it.remove();
            }else if(block.startAddr == insertedBlock.startAddr + insertedBlock.blockSize){
                next = block;
                it.remove();
            }
        }

        //合并内存块
        if(prev != null){
            insertedBlock = Block.mergeBlock(prev, insertedBlock);
        }
        if(next != null){
            insertedBlock = Block.mergeBlock(insertedBlock, next);
        }
        insertedBlock.jobIndex = 0;

        //重新插入空闲链表
        int insertIndex = 0;
        it = emptyList.iterator();
        while (it.hasNext()){
            Block block = it.next();
            if(block.blockSize >= insertedBlock.blockSize){
                break;
            }
            insertIndex++;
        }

        emptyList.add(insertIndex, insertedBlock);
    }
    private void malloc(Block block){
        busyList.add(block);
    }
}

public class Allocator {
    fitAlgorithm algorithm;

    public void setAlgorithm(fitAlgorithm algorithm){
        this.algorithm =algorithm;
    }

    void malloc(int jobIndex, int requiredSize){
        assert algorithm != null;
        algorithm.malloc(jobIndex, requiredSize);
    }
    void free(int jobIndex, int requiredSize){
        assert algorithm != null;
        algorithm.free(jobIndex, requiredSize);
    }

    public Allocator(){}
    public Allocator(fitAlgorithm algo){
        this.algorithm = algo;
    }

    public static String[][] getRequests(){
        String[][] requests = {
                {"Job 1 Request 130K"},
                {"Job 2 Request 60K"},
                {"Job 3 Request 100K"},
                {"Job 2 Release 60K"},
                {"Job 4 Request 200K"},
                {"Job 3 Release 100K"},
                {"Job 1 Release 130K"},
                {"Job 5 Request 140K"},
                {"Job 6 Request 60K"},
                {"Job 7 Request 50K"},
                {"Job 6 Release 60K"},
                };
        return requests;
    }
}
