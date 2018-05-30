package com.edu.seu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KRPCYEnum implements CodeEnum<String>{
    QUERY("q","query"),
    RESPONSE("r","response"),
    ERROR("e","error"),
    UNKNOWN("u","undefined"),
    ;

    private String code;
    private String message;
}
