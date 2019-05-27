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
class firstFit extends fitInstance{
//    // 空闲链表
//    List<Block> emptyList = new LinkedList<Block>();
//
//    //已分配链表
//    List<Block> busyList = new LinkedList<Block>();

    public firstFit(){
        //在头部加入哨兵节点，设置大小为0，这样就不会被分配出去
        //一个注意的点是bestFit插入方法不同于firstFit（取决于emptyList的排列基准），故bestFit不需要哨兵节点
        //在显示时需要做下判断
        emptyList.add(new Block(-1, 0));
        emptyList.add(new Block(0, 640));
    }

    @Override
    public Block malloc(int jobIndex, int requiredSize){
        //找出足够大的第一块，分割后留下前部
        //注意到这里我们是按地址排序，分割后若有剩余不用调整emptyList
        //如果刚好分配一整块出去，那么我们直接从emptyList把这个块删除
        Iterator<Block> it = emptyList.iterator();
        while (it.hasNext()){
            Block block = it.next();
            if(block.blockSize < requiredSize)continue;
            //
            if(block.blockSize == requiredSize){
                it.remove();

                makeBusy(block, jobIndex);
                return block;
            }else{
                assert block.blockSize > requiredSize;
                Block[] result = Block.splitBlock(block, requiredSize);

                makeBusy(result[1], jobIndex);
                return result[1];
            }
        }

        System.out.println("No Enough Memory for First Fit!");
        return null;
    }

    void makeBusy(Block block ,int jobIndex){
        block.jobIndex = jobIndex;//设置标记
        busyList.add(block);//加入busyList
    }

    @Override
    public void free(int jobIndex, int blockSize){
        //先在busyList中找出要释放的内存块，这点和bestFit算法类似
        Block insertedBlock = super.findBlockInBusyList(jobIndex, blockSize);
        assert insertedBlock != null;

        //将找到的释放块重新放入emptyList中
        //注意到这里放入的时候是按照地址排序
        //找到该内存块应该放入的位置后，确定两个内存块是否能合并只要判断插入位置前后两个内存块的地址即可

        int insertIndex = 1;//插入位置

        Iterator<Block> it = emptyList.iterator();
        Block prev = it.next(), next = null; // 插入位置的前驱和后继
//        //如果emptyList为空
//        if(it.hasNext()){
//            prev = it.next();
////            next = prev;
//        }

        while (it.hasNext()){
            next = it.next();

            if(next.startAddr>insertedBlock.startAddr){
                break;
            }
            insertIndex ++;//注意因为存在哨兵节点，被释放的block至少放在第二个位置上
            prev = next;
        }

        //....我感觉这部分合并的逻辑还可以再抽出来...
        if(prev.startAddr + prev.blockSize == insertedBlock.startAddr){
            insertedBlock = Block.mergeBlock(prev, insertedBlock);
            emptyList.remove(prev);
            insertIndex--;
        }
        if(next != null && insertedBlock.startAddr + insertedBlock.blockSize == next.startAddr){
            insertedBlock = Block.mergeBlock(insertedBlock, next);
            emptyList.remove(next);
        }

        insertedBlock.jobIndex = 0;//消除作业编号
        emptyList.add(insertIndex, insertedBlock);
    }
}

//最佳适应
class bestFit extends fitInstance{

    public bestFit(){

        emptyList.add(new Block(0, 640));
//        System.out.println("BestFit!:"+emptyList.size());
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
//        Iterator<Block> it = busyList.iterator();
//        while (it.hasNext()){
//            Block block = it.next();
//            if(block.blockSize == blockSize && block.jobIndex == jobIndex){
//                it.remove();
//                free(block);
//            }
//        }
        Block block = super.findBlockInBusyList(jobIndex, blockSize);
        free(block);
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
        insertedBlock.jobIndex = 0;//消除作业编号的标记

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
        //TODO: 是否还有更高效的插入方法
        emptyList.add(insertIndex, insertedBlock);
    }
    private void malloc(Block block){
        busyList.add(block);
    }
}

public class Allocator {
    fitAlgorithm algorithm;//适应算法

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

//适应算法的抽象基类，用于实现公共逻辑
abstract class fitInstance implements fitAlgorithm{
    // 空闲链表
    protected List<Block> emptyList = new LinkedList<Block>();

    //已分配链表
    protected List<Block> busyList = new LinkedList<Block>();

    @Override
    public List<Block> getEmptyList(){
        return emptyList;
    }
    @Override
    public List<Block> getBusyList(){
        return busyList;
    }

    //这里的查找其实是不太精确的，考虑到一个作业有可能申请多个相同大小的内存块
    //而不给出地址的话在我们看来这两个内存块应该是等价的，故此我们在此直接返回第一个符合大小和作业编号的内存块

    //注意到其实两个算法在找一个特定的即将要释放的块时是相同的，所以直接在基类实现
    protected Block findBlockInBusyList(int jobIndex, int blockSize){
        Iterator<Block> it = busyList.iterator();
        Block ret = null;

        while (it.hasNext()){
            Block block = it.next();
            if(block.blockSize == blockSize && block.jobIndex == jobIndex){
                it.remove();//移除指定内存块
                ret = block;
            }
        }

        if(ret == null){
            //找不到指定的内存块
            System.out.println("No such Block with size " + blockSize + "and jobIndex " + jobIndex +"!");
        }
        return ret;
    }
}