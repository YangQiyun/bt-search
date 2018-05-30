package com.edu.seu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KRPCErrorEnum implements CodeEnum<Integer>{
    GENERIC_ERROR(201,"Generic Error"),
    SERVER_ERROR(202,"Server Error"),
    PROTOCOL_ERROR(203,"Protocol Error,such as malformed packet,invalid arguments,or bad token"),
    MethodUNKNOWN_ERROR(204,"Method Unknown"),
    UNKNOWN(0,"Undefined error type")
    ;

    private Integer code;
    private String Message;

}
