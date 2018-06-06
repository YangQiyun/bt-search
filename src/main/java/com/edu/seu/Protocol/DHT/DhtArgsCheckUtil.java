package com.edu.seu.Protocol.DHT;

import com.edu.seu.Configuration.InitConfig;
import com.edu.seu.Exception.BtException;
import com.edu.seu.Protocol.Bencode.Bencoding;
import com.edu.seu.Protocol.KRPC.Errors;
import com.edu.seu.Protocol.KRPC.Queries;
import com.edu.seu.Protocol.KRPC.Responses;
import com.edu.seu.enums.DHTMethodQvalue;
import com.edu.seu.enums.KRPCErrorEnum;
import com.edu.seu.enums.KRPCYEnum;

import java.util.List;
import java.util.Map;

public class DhtArgsCheckUtil {

    //根据网络字节序解码获得的Result结果，同时进行初步的解析判断格式，主要KRPC协议结构要求的 t y 两个字段
    public static Map<String, Bencoding.btDecodeResult> decodeAndCheckResp(byte[] response) {
        return checkKRPCFormat(InitConfig.bEncoding.decodingObject(response, 0));
    }

    //判断是否正常解析，同时判断是否存在KRPC协议所要求的t和y两个字段
    private static Map<String, Bencoding.btDecodeResult> checkKRPCFormat(Bencoding.btDecodeResult result) {
        if (result.type != Bencoding.beType.Dictionary)
            throw new BtException("解析字节序出错非字典结构");
        Map<String, Bencoding.btDecodeResult> resultMap = (Map<String, Bencoding.btDecodeResult>) result.value;
        if (!(resultMap.containsKey("t") && resultMap.containsKey("y")))
            throw new BtException(BtException.ERROR_CODE.FORMAT_ERROR,"format error: lack of t or y");
        return resultMap;
    }

    //解析出t对应的值
    public static String parseT(Map<String, Bencoding.btDecodeResult> result) {
        Bencoding.btDecodeResult tValue = result.get("t");
        if (tValue != null) {
            //t的对应值必须为bytestring类型，并且是2个byte大小
            if (tValue.type == Bencoding.beType.ByteString && ((String) tValue.value).length() == 2)
                return (String) tValue.value;
        }
        throw new BtException(BtException.ERROR_CODE.PARSET_ERROR,"format error: t对应value值格式出错 :当前出错值长度是：{},值为：{}"+((String)tValue.value).length()+tValue.value);
    }

    //解析出y对应的值
    public static KRPCYEnum parseY(Map<String, Bencoding.btDecodeResult> result) {
        Bencoding.btDecodeResult yValue = result.get("y");
        if (yValue != null) {
            //y的对应值必须为bytestring类型，并且是指定的三种代表值q，r，e，同时进行初步的KRPC协议结构要求判断是否存在相应的字段
            if (yValue.type == Bencoding.beType.ByteString) {
                if (yValue.value.equals(KRPCYEnum.QUERY.getCode()) && result.containsKey("q") && result.containsKey("a"))
                    return KRPCYEnum.QUERY;
                if (yValue.value.equals(KRPCYEnum.RESPONSE.getCode()) && result.containsKey("r"))
                    return KRPCYEnum.RESPONSE;
                if (yValue.value.equals(KRPCYEnum.ERROR.getCode()) && result.containsKey("e"))
                    return KRPCYEnum.ERROR;
            }
        }
        throw new BtException("format error: y对应value值格式出错");
    }

    //解析出q对应的值
    public static DHTMethodQvalue parseQ(Map<String, Bencoding.btDecodeResult> result) {
        Bencoding.btDecodeResult qValue = result.get(KRPCYEnum.QUERY.getCode());
        if (qValue != null) {
            //q的对应值必须为bytestring类型，并且是指定的四种代表值
            if (qValue.type == Bencoding.beType.ByteString) {
                for (DHTMethodQvalue dhtMethodQvalue : DHTMethodQvalue.values())
                    if (qValue.value.equals(dhtMethodQvalue.getCode()))
                        return dhtMethodQvalue;
            }
        }
        throw new BtException("format error: q对应value值格式出错");
    }

    //解析出a对应的值,根据KRPC协议的规定a所对应的应该只能是query请求
    public static Queries parseA(Map<String, Bencoding.btDecodeResult> result, DHTMethodQvalue method) {
        Bencoding.btDecodeResult aValue = result.get("a");
        if (aValue != null) {
            //a的对应值必须为dictionary类型
            if (aValue.type == Bencoding.beType.Dictionary) {
                Map<String, Bencoding.btDecodeResult> value = (Map<String, Bencoding.btDecodeResult>) aValue.value;
                if (method.getCode().equals(DHTMethodQvalue.PING.getCode())) {
                    return PingQuery.decodeArgs(value);
                }
                if (method.getCode().equals(DHTMethodQvalue.FINDNODE.getCode())) {
                    return FindNodeQuery.decodeArgs(value);
                }
                if (method.getCode().equals(DHTMethodQvalue.GETPEERS.getCode())) {
                    return GetPeersQuery.decodeArgs(value);
                }
                if (method.getCode().equals(DHTMethodQvalue.ANNOUNCEPEER.getCode())) {
                    return AnnouncePeerQuery.decodeArgs(value);
                }
            }
        }
        throw new BtException("format error: a对应value值格式出错");
    }

    //解析出r对应的值,根据KRPC协议的规定r所对应的应该只能是response请求
    public static Responses parseR(Map<String, Bencoding.btDecodeResult> result, DHTMethodQvalue method) {
        Bencoding.btDecodeResult rValue = result.get(KRPCYEnum.RESPONSE.getCode());
        if (rValue != null) {
            //r的对应值必须为dictionary类型
            if(rValue.type==Bencoding.beType.Dictionary){
                Map<String, Bencoding.btDecodeResult> value = (Map<String, Bencoding.btDecodeResult>) rValue.value;
                if(method==DHTMethodQvalue.PING){
                    return PingResponse.decodeArgs(value);
                }
                if(method==DHTMethodQvalue.FINDNODE){
                    return FindNodeResponse.decodeArgs(value);
                }
                if(method==DHTMethodQvalue.GETPEERS){
                    return GetPeersResponse.decodeArgs(value);
                }
                if(method==DHTMethodQvalue.ANNOUNCEPEER){
                    return AnnouncePeerResponse.decodeArgs(value);
                }
                return null;
            }
        }
        throw new BtException("format error: r对应value值格式出错");
    }

    //解析出e对应的值
    public static Errors parseE(Map<String, Bencoding.btDecodeResult> result) {
        Bencoding.btDecodeResult eValue = result.get(KRPCYEnum.ERROR.getCode());
        Errors errorResp=new Errors();
        if (eValue != null) {
            //e的对应值必须为List类型，并且是指定的四种代表值
            if (eValue.type == Bencoding.beType.List) {
                List<Bencoding.btDecodeResult> list= (List<Bencoding.btDecodeResult>) eValue.value;
                if(list.get(0).type==Bencoding.beType.Integer){
                    for(KRPCErrorEnum errorEnum:KRPCErrorEnum.values()){
                        if(errorEnum.getCode()==(int)list.get(0).value)
                            errorResp.setError(errorEnum);
                    }
                }
                if(list.get(1).type==Bencoding.beType.ByteString){
                    errorResp.setReceivedContent((String) list.get(1).value);
                }
                return errorResp;
            }
        }
        throw new BtException("format error: e对应value值格式出错");
    }

}
