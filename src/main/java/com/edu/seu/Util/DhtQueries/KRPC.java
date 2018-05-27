package com.edu.seu.Util.DhtQueries;


import com.edu.seu.Exception.BtException;
import com.edu.seu.Util.ConvertUtil;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

/*
*对于每一个KRPC信息是由一个简单的三个keys(t,y,v)共同组成的字典集合，如若有其他信息根据信息类型增加相应的keys值。
* */
public class KRPC {

    private String className=this.getClass().getName();

    //y键对应的三个值
    public static enum yType{
        QUERY,
        RESPONSE,
        ERROR,
        UNKNOWN
    }

    //定义键值对类型
    public class Entry implements Map.Entry<String,Object>{

        private String key=null;

        private Object value=null;

        public Entry(String key,Object value){
            this.key=key;
            this.value=value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object setValue(Object value) {
            Object old=this.value;
            this.value=value;
            return old;
        }

    }

    //代表可KRPC协议下的一个事务ID，value的要求是只能是两个字符，即两个byte
    private Entry t=new Entry("t",null);

    //描述该请求的信息类型，value的值只能是yType的三种之一
    private Entry y=new Entry("y",null);

    //代表客户端版本号鉴别，该value值是用作客户端版本识别，识别号是注册在BEP 20协议中
    private Entry v=new Entry("v",null);

    //当yType为query时，附带的参数放置键值对处
    private Entry q=new Entry("q",null);
    private Entry a=new Entry("a",null);

    //当yType为Response时，附带的参数放置键值对处
    private Entry r=new Entry("r",null);

    //当yType为Errors时，附带的参数放置键值对处
    private Entry e=new Entry("e",null);


    //返回的是事务ID编号的HexString
    public String getT() {
        return (String)t.getValue();
    }

    //设置事务的ID编号
    public void setT(String value) {
        if(value.getBytes(CharsetUtil.ISO_8859_1).length!=2)
            throw new BtException(className+" - setT: KRPC协议事务编号t格式要求不匹配");
        t.setValue(value);
    }

    //获得请求的类型
    public yType getY() {
        if(y.getValue()==null)
            return yType.UNKNOWN;
        switch ((String)y.getValue()){
            case "q":
                return yType.QUERY;
            case "r":
                return yType.RESPONSE;
            case "e":
                return yType.ERROR;
                default:
                    return yType.UNKNOWN;
        }
    }

    //设置请求类型
    public void setY(yType type) {
        if(type==yType.QUERY)
            y.setValue("q");
        if(type==yType.RESPONSE)
            y.setValue("r");
        if(type==yType.ERROR)
            y.setValue("e");
        throw new BtException(className+" -setY: 类型不符合要求");
    }

    //获取版本号
    public String getV() {
        return v.getValue()==null?null:(String)v.getValue();
    }

    //设置版本号
    public void setV(String version) {
        v.setValue(version);
    }

    public String getQ() {
        return (String) q.value;
    }

    public void setQ(String value) {
        q.value=value;
    }


    public List getE() {
        return (List) e.getValue();
    }

    public void setE(List value) {
        e.setValue(value);
    }

    public String getA() {
        return (String)a.getValue();
    }

    public void setA(Object value) {
        a.setValue(value);
    }
}
