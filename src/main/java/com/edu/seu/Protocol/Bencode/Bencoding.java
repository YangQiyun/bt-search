package com.edu.seu.Protocol.Bencode;

import com.edu.seu.Exception.BtException;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.*;

import static com.edu.seu.Util.ConvertUtil.HexString2Byte;

/*
* 实现BE的编码方式
* */
public class Bencoding {

    public static  enum beType{
        ByteString,
        Integer,
        List,
        Dictionary,
        End  //表示解析到allend
    }

    private String className=this.getClass().getName();

    private  Charset becharset= CharsetUtil.ISO_8859_1;

    private static final String interval=":";
    private static final String allend="e";
    private static final String integerStart="i";
    private static final String listStart="l";
    private static final String dictionaryStart="d";



    /*
    * Encoding
    * */

    //Byte String 字节串的格式为 字节串长度:内容，其中 字节串长度 是 ASCII 编码格式的整数字符串，单位为字节
    private  String encodingByteString(String value){
        return value.getBytes(becharset).length+interval+value;
    }

    //Integer 整数的格式为 i整数e，其中 整数 是 ASCII 编码格式的整数字符串
    private  String encodingInteger(int value){
        return integerStart+String.valueOf(value)+allend;
    }

    //List 列表的格式为 l不限数量个BE量e（小写L开头）
    private  String encodingList(List<Object> value){
        StringBuilder result=new StringBuilder();
        result.append(listStart);
        for(Object child:value){
           result.append(encodingObject(child));
        }
        result.append(allend);
        return result.toString();
    }

    //Dictionary,字典的格式为 d不限数量个字段e（小写D开头）。字段 是指一种 key-value 结构，其中 key 是一个BE字节串，一个字段的格式为 一个BE字节串+一个BE量
    private String encodingDictionary(Map<String,Object> value){
        StringBuilder result=new StringBuilder();
        result.append(dictionaryStart);
        for(Map.Entry<String,Object> entry:value.entrySet())
            result.append(encodingByteString(entry.getKey())+encodingObject(entry.getValue()));
        result.append(allend);
        return result.toString();
    }

    //进行类型选择编码
    public String encodingObject(Object value){
        if(value instanceof String)
            return encodingByteString((String)value);
        if(value instanceof Integer)
            return encodingInteger((Integer)value);
        if(value instanceof List)
            return encodingList((List<Object>) value);
        if(value instanceof Map)
            return encodingDictionary((Map<String,Object>)value);
        if(value instanceof btDecodeResult) {
            if (((btDecodeResult) value).type == beType.ByteString)
                return encodingByteString((String) ((btDecodeResult) value).value);
            if (((btDecodeResult) value).type == beType.Integer)
                return encodingInteger((Integer)((btDecodeResult) value).value);
            if (((btDecodeResult) value).type == beType.List)
                return encodingList((List<Object>) ((btDecodeResult) value).value);
            if (((btDecodeResult) value).type == beType.Dictionary)
                return encodingDictionary((Map<String, Object>) ((btDecodeResult) value).value);
        }
        throw new RuntimeException("encoding becoding format error");
    }

    /*
     * Decoding
     * */

    //定义返回类型
    @AllArgsConstructor
    @NoArgsConstructor
    public static class btDecodeResult{
        public btDecodeResult(beType type){
            this.type=type;
        }

        public btDecodeResult(beType type,Object value){
            this.type=type;
            this.value=value;
        }

        public Object value;
        public beType type;
        private int endIndex;

    }

    //ByteString 不需要再验证格式正确性
    private btDecodeResult decodingByteString(byte[] value,int start) throws BtException{
        btDecodeResult result=new btDecodeResult(beType.ByteString);

        //首个字符提取进行判断
        char firstChar=new String(value,start,1,becharset).charAt(0);

        //如果该类型是bytestring类型,通过首个字符进行判断
        if(firstChar>='0'&&firstChar<='9'){
            //寻找':'interval字段
            int intervalIndex=start+1;
            while (intervalIndex!=value.length){
                if(interval.getBytes()[0]==value[intervalIndex])
                    break;
                else {
                    if(value[intervalIndex]<48||value[intervalIndex]>57)
                        throw new BtException(className+" - readUtilEnd: error in bytestring head 存在非数字在interval前");
                }
                intervalIndex++;
            }
            if(intervalIndex==value.length)
                throw new BtException(className+"- readUtilEnd: error in bytestring head 找不到interval字段");
            //若结构正常则找到所属的长度
            int length=Integer.parseInt(new String(value,start,intervalIndex-start));
            result.value=new String(value,intervalIndex+1,length,becharset);
            result.endIndex=intervalIndex+length;
            return result;
        }
        throw new BtException(className+"- readUtilEnd: 非bytestring正常head结构");
    }

    //Integer
    private btDecodeResult decodingInteger(byte[] value,int start){
        btDecodeResult result=new btDecodeResult(beType.Integer);

        //首个字符提取进行判断
        char firstChar=new String(value,start,1,becharset).charAt(0);

        if(integerStart.charAt(0)==firstChar){
            //寻找'e'allend字段
            int endIndex=start+1;
            while (endIndex!=value.length){
                if(allend.getBytes()[0]==value[endIndex])
                    break;
                else {
                    //多加一个负号的判断条件
                    if(!((value[endIndex]>=48&&value[endIndex]<=57)||(value[endIndex]==45&&endIndex==start+1)))
                        throw new BtException(className+" - decodingInteger: error in Integer head 存在非数字在end前");
                }
                endIndex++;
            }
            if(endIndex==value.length||endIndex<=start+1)
                throw new BtException(className+"- decodingInteger: end位置出错");
            if(value[start+1]==45&&endIndex>start+2&&value[start+2]==48)
                throw new BtException(className+"- decodingInteger: -0无效编码");
            if(value[start+1]==45&&endIndex!=start+2)
                throw new BtException(className+"- decodingInteger: 以0作为开头的无效编码");
            int cotent=Integer.parseInt(new String(value,start+1,endIndex-(start+1),becharset));
            result.value=cotent;
            result.endIndex=endIndex;
            return result;
        }else
            throw new BtException(className+"- decodingInteger: 非integer结构");
    }

    //进行任意类型选解码
    public   btDecodeResult decodingObject(byte[] value,int start) throws BtException {
        btDecodeResult result=new btDecodeResult();
        //首个字符提取进行判断
        char firstChar=new String(value,start,1,becharset).charAt(0);

        //如果是结束标志
        if(allend.charAt(0)==firstChar){
            result.type=beType.End;
            result.endIndex=start;
            result.value=null;
            return result;
        }
        //如果可能是bytestring
        if(firstChar>='0'&&firstChar<='9') {
            return decodingByteString(value,start);
        }
        //如果是integer
        if(integerStart.charAt(0)==firstChar){
            return decodingInteger(value,start);
        }
        //如果是list
        if(listStart.charAt(0)==firstChar){
            result.type=beType.List;
            List<btDecodeResult> lists=new ArrayList<>();
            btDecodeResult element=null;
            int elementEnd=start;
            while (true){
                element=decodingObject(value,elementEnd+1);
                elementEnd=element.endIndex;
                if(element.type!=beType.End)
                    lists.add(element);
                else{
                    result.endIndex=elementEnd;
                    result.value=lists;
                    return result;
                }
            }
        }
        //如果是map
        if(dictionaryStart.charAt(0)==firstChar){
            result.type=beType.Dictionary;
            Map<String,btDecodeResult> map=new TreeMap<>();
            btDecodeResult element=null;
            int elementEnd=start;
            while (true){
                //首先定位key
               element=decodingObject(value,elementEnd+1);
               elementEnd=element.endIndex;
               String key=null;
               //如果是betystring是合理结构，如果是end则map结束，如果是其他则error
               if(element.type==beType.ByteString){
                   key=(String)element.value;
               }else {
                   if(element.type==beType.End) {
                       result.endIndex = elementEnd;
                       result.value = map;
                       return result;
                   }else
                       throw new BtException(className+" - decodingObject: error to locate the key-value");
               }
               //判断value是否合法
               try {
                   element=decodingObject(value,elementEnd+1);
                   elementEnd=element.endIndex;
                   if(element.type==beType.End)
                       throw new BtException("end could not happen here!");
               }catch (BtException e){
                   throw new BtException(className+" - decodingObject: error to locate the key-value");
               }
               map.put(key,element);
            }
        }
        //其他表示结构出错
        throw new BtException(className+" - decodingObject: error in structure");
    }

    public static Bencoding.btDecodeResult newStringResult(String value){
        return new Bencoding.btDecodeResult(Bencoding.beType.ByteString,value);
    }


    public static void main(String [] args){
        Bencoding bEncoding=new Bencoding();
        Map<String,Object> content=new HashMap<>();
        content.put("name","yangyangyangyangyang");
        content.put("age",123);
        System.out.println(bEncoding.encodingObject(content));
        //byte[] a=bEncoding.encodingInteger(1).getBytes();
        /*
        Integer c=0;
        byte[] t=bEncoding.encodingObject(c).getBytes(CharsetUtil.ISO_8859_1);

        System.out.println(t[1]==48?true:false);
        int num=-0;
        String tt=String.valueOf(num);
        */
        Bencoding.btDecodeResult result=bEncoding.decodingObject(bEncoding.encodingObject(content).getBytes(CharsetUtil.ISO_8859_1),0);
        if(result.type==beType.Dictionary){
            Map<String,btDecodeResult> temp= (Map<String, btDecodeResult>) result.value;
            for(Map.Entry<String,btDecodeResult> entry:temp.entrySet()){
                if(entry.getValue().type==beType.Integer)
                    System.out.println(entry.getKey()+" "+(Integer)entry.getValue().value);
                if(entry.getValue().type==beType.ByteString)
                    System.out.println(entry.getKey()+" "+(String) entry.getValue().value);
            }
        }



        String target="1619ecc9373c3639f4ee3e261638f29b33a6cbd6";
        byte[] gg=HexString2Byte(target);
        String temp=new String(gg, CharsetUtil.ISO_8859_1);

        Map<String,Bencoding.btDecodeResult> map=new TreeMap<>();
        map.put("t",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,"aa"));
        map.put("y",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,"q"));
        map.put("q",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,"find_node"));
        Map<String,Bencoding.btDecodeResult> small=new TreeMap<>();
        small.put("id",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,temp));
        small.put("target",new Bencoding.btDecodeResult(Bencoding.beType.ByteString,temp));
        map.put("a",new Bencoding.btDecodeResult(Bencoding.beType.Dictionary,small));
        String what=bEncoding.encodingObject(map);
    }
}
