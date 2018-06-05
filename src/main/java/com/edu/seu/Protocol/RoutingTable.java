package com.edu.seu.Protocol;


import com.edu.seu.Exception.BtException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.edu.seu.Util.ConvertUtil.*;


/**
* RoutingTable记录每一个单体node id的路由表，考虑到并发问题,参考了{@link java.util.concurrent.ConcurrentHashMap}
*/

public class RoutingTable {

    private final String className=this.getClass().getName();

    /**
     * 本路由表的mCompactNode
     */
    private byte[] mCompactNode;

    /**
     * bucket路由表的容器
     */
    volatile Bucket[] buckets;


    /**
     * 每一个bucket最多存储的K个数
     */
    private static final int MAXI_K = 8;


    /**
     * 一个路由表最大容量只有160个bucket
     */
    private static final int MAXIMUM_BUCKET = 160;


    /* ---------------- Nodes -------------- */

    @AllArgsConstructor
    static class Bucket{
        volatile int deleteIndex;
        volatile int contentSize;
        volatile List<byte[]> content;
    }

    /* ---------------- Construct operations -------------- */

    public RoutingTable(String CompactNode){
        this.mCompactNode=HexString2Byte(CompactNode);
        init();
    }

    public RoutingTable(byte[] CompactNode){
        this.mCompactNode=CompactNode;
        init();
    }

    /* ---------------- Init operations -------------- */

    /**
     * 初始化bucket的表，生成160个bucket
     */
    public void init(){
        buckets=new Bucket[MAXIMUM_BUCKET];
        List<byte[]> content=new ArrayList<>(MAXI_K);
        content.add(mCompactNode);
        buckets[MAXIMUM_BUCKET-1]=new Bucket(0,1,content);
    }




    /* ---------------- Public operations -------------- */

    /**
     * 插入指定的bucket中
     */
    public boolean put(byte[] id){

        if(id==null||id.length==0)
            throw new BtException(className+" - put: id的值为空");
        int index=indexCode(id);
        //可能需要分裂node节点
        try {
            for(Bucket[] bus=buckets;;){
                Bucket b;
                if((b=tabAt(bus,index))==null){
                    List<byte[]> content=new ArrayList<>();
                    content.add(id);
                    return casTabAt(buckets, index, null,
                            new Bucket(0,1,content));
                }else {//如果本node节点存在
                    synchronized (b){
                        if(b==tabAt(bus,index)) {
                            //如果近邻记录个数少于八个直接加入
                            if (b.contentSize<MAXI_K) {
                                b.content.add(id);
                                b.contentSize++;
                            }else { //根据循环的删除因子进行更新
                                b.content.set(b.deleteIndex,id);
                                b.deleteIndex=++b.deleteIndex%MAXI_K;
                            }
                            return true;
                        }
                    }

                }
            }

        }catch (Exception e){
            throw new BtException(className+"- put:出现未知错误! "+e.getMessage());
        }
    }

    /**
     * 获取近邻的八个节点信息
     * @return 返回能找的的最近的八个节点，如果找不到或者不到八个返回null
     */
    public List<byte[]> get(byte[] id){

        if(id==null||id.length==0)
            throw new BtException(className+" - put: id的值为空");
        int index=indexCode(id);
        //存储返回节点的数组
        List<byte[]> target=new ArrayList<>(MAXI_K);

        Bucket b=tabAt(buckets,index);
        //节点存在加入到返回的数组中,如果达到八个直接返回，不够就向前后寻找
        if(b!=null){
            target.addAll(b.content);
            if(target.size()==MAXI_K)
                return target;
        }

        //如果该node节点是不存在的,向前后寻找近邻的可能存在的节点数
        for(int pre=index-1,last=index+1;pre>=0||last<=MAXIMUM_BUCKET-1;pre--,last++){
            Bucket preb=pre>=0?tabAt(buckets,pre):null,lastb=last<MAXIMUM_BUCKET?tabAt(buckets,last):null;
            if(preb!=null&&preb.contentSize>0){
                if(preb.contentSize>MAXI_K-target.size())
                    target.addAll(preb.contentSize-(MAXI_K-target.size()),preb.content);
                else
                    target.addAll(preb.content);
            }
            if(lastb!=null&&lastb.contentSize>0){
                if(lastb.contentSize>MAXI_K-target.size())
                    target.addAll(lastb.contentSize-(MAXI_K-target.size()),lastb.content);
                else
                    target.addAll(lastb.content);
            }
            if(target.size()==MAXI_K)
                return target;
        }
        //找不到八个
        return null;

    }

    /* ---------------- Private operations -------------- */

    /**
     * 获取bucket的位置，通过XOR进行，判断位数不同的bit最高位即为bucket的index
     */
    private int indexCode(byte[] id){

        if(id==null||id.length!=26)
            throw new BtException(className+" - indexCode: id格式不符合规范");

        //i 20位byte t 临时变量 k 表示128 64 32 16 8 4 2 1 q 表示第几位1
        for(int i=0;i<20;i++){
            int t=(Byte2Int(id[i])^Byte2Int(mCompactNode[i]));
            if(t!=0) {
                for (int k = 0x080,q=0; k != 0; k >>= 1, q++) {
                    if ((t&k) == k) {
                        return i*MAXI_K+q;
                    }
                }
            }

        }
        //该id为mNodeId
        return MAXIMUM_BUCKET-1;
    }

    /**
     * 原子性替换掉相应的位置
     */
    static final boolean casTabAt(Bucket[] tab, int i,
                                        Bucket c, Bucket v) {
        return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
    }

    /**
     * 原子性获取对象
     */
    @SuppressWarnings("unchecked")
    static final Bucket tabAt(Bucket[] tab, int i) {
        return (Bucket)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U ;
    //private static final long SIZECTL;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        try {
            U = getUnsafeInstance();
            Class<?> k = RoutingTable.class;
            //SIZECTL = U.objectFieldOffset
             //       (k.getDeclaredField("sizeCtl"));
            Class<?> ak = Bucket[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

    public static Unsafe getUnsafeInstance() throws Exception
    {
        // 通过反射获取rt.jar下的Unsafe类
        Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeInstance.setAccessible(true);
        // return (Unsafe) theUnsafeInstance.get(null);是等价的
        return (Unsafe) theUnsafeInstance.get(Unsafe.class);
    }

    @AllArgsConstructor
    static class mThread extends Thread{

        private RoutingTable table;
        private byte[] id;

        @Override
        public void run() {
            table.put(id);
        }
    }

    public static void main(String[] args){

        String target="1619ecc9373c3639f4ee3e261638f29b33a6cbd6d6d6d6d6d6";
        String append="1619ecc9373c3639f4ee3e261638f29b33a6cb";
        RoutingTable routingTable=new RoutingTable(HexString2Byte(target));
        //routingTable.put(HexString2Byte("4615ecc9373c3639f4ee3e261638f29b33a6cbd6"));
        List<byte[]> result=null;
        for(int i=10;i<100;i++){
            String c=append+String.valueOf(i)+"d6d6d6d661d6";
            new mThread(routingTable,HexString2Byte(c)).start();
            if(i==48)
                result=routingTable.get(HexString2Byte("1619ecc9373c3639e3f634ee3e2e361638e3f29b33e3a7cbd6e3"));
        }
        try {
            Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> stringList=new ArrayList<>();
        for (byte[] argument:result){
            stringList.add(Byte2HexString(argument));
        }
        int a=1;
       // Assert.assertEquals(true,stringList.contains(target));

//        byte[] re=HexString2Byte("909f9cbdedf4f7e29e820e3fd5e00a2965450b8a");
//        ByteBuf resultBuf= Unpooled.wrappedBuffer(re);
//        byte[] compactInfo=new byte[1];
//        for(;;){
//            resultBuf.readBytes(compactInfo);
            //routingTable.put(compactInfo);

    }

}
