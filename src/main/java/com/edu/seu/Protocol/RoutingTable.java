package com.edu.seu.Protocol;


import com.edu.seu.Exception.BtException;
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
     * 本路由表的NodeID
     */
    private byte[] mNodeID;

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

    public RoutingTable(String nodeId){
        this.mNodeID=HexString2Byte(nodeId);
        init();
    }

    public RoutingTable(byte[] nodeId){
        this.mNodeID=nodeId;
        init();
    }

    /* ---------------- Init operations -------------- */

    /**
     * 初始化bucket的表，生成160个bucket
     */
    public void init(){
        buckets=new Bucket[MAXIMUM_BUCKET];
        List<byte[]> content=new ArrayList<>(MAXI_K);
        content.add(mNodeID);
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
                            //如果近邻的记录个数少于八个直接加入
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
        return target;

    }

    /* ---------------- Private operations -------------- */

    /**
     * 获取bucket的位置，通过XOR进行，判断位数不同的bit最高位即为bucket的index
     */
    private int indexCode(byte[] id){

        if(id==null||id.length!=20)
            throw new BtException(className+" - indexCode: id格式不符合规范");

        //i 20位byte t 临时变量 k 表示128 64 32 16 8 4 2 1 q 表示第几位1
        for(int i=0;i<20;i++){
            int t=(Byte2Int(id[i])^Byte2Int(mNodeID[i]));
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

}
