package com.edu.seu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DHTMethodQvalue implements CodeEnum<String>{
    PING("ping"),
    FINDNODE("find_node"),
    GETPEERS("get_peers"),
    ANNOUNCEPEER("announce_peer"),
    UNKNOWN("unknown")
    ;

    private String code;

    public String getMessage(){
        return getCode();
    }

}
