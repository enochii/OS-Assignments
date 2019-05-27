package Allocator;
import com.sun.istack.internal.NotNull;

public class Block {
    int startAddr;//内存块开始地址
    int blockSize;//内存块大小,KB为单位
    int jobIndex = 0; //占用该内存块的作业编号，0 表示无人占用

    @Override
    public boolean equals(Object obj){
        Block block = (Block)obj;

        return block.startAddr == startAddr &&
                block.blockSize == blockSize &&
                block.jobIndex == jobIndex;
    }

    public Block(int startAddr, int blockSize){
        this.startAddr = startAddr;
        this.blockSize = blockSize;
    }

//    public int getBlockSize() {
//        return blockSize;
//    }
//
//    public int getJobIndex() {
//        return jobIndex;
//    }
//
//    public void setJobIndex(int jobIndex) {
//        this.jobIndex = jobIndex;
//    }
//
//    public int getStartAddr() {
//        return startAddr;
//    }

    //分割内存块，第一部分是剩下来的，第二部分是分配出去的
    public static Block[] splitBlock(@NotNull Block ori, int requiredSize){
        //一个分割的Block应该是无人占用的
        assert ori.jobIndex == 0 && ori.blockSize > requiredSize;
        ori.blockSize -= requiredSize;

        Block newBlock = new Block(ori.startAddr + ori.blockSize, requiredSize);

        Block[] result = {ori, newBlock};

        return result;
    }

    //合并内存块，将第二块内存合并到第一块内存的后面
     public static Block mergeBlock(@NotNull Block fir, @NotNull Block sec){
        assert fir.startAddr+fir.blockSize == sec.startAddr;
        fir.blockSize += sec.blockSize;

        return fir;
     }

}
