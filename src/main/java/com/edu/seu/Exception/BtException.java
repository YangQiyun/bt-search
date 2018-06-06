package com.edu.seu.Exception;

import lombok.Data;

@Data
public class BtException extends RuntimeException{

    private ERROR_CODE error_code;

    public BtException(String messge){
        super(messge);
        this.error_code=ERROR_CODE.NORMAL_ERROR;
    }

    public static enum ERROR_CODE{
        FINDNODE_LEAK,
        NORMAL_ERROR,
        FINDNODE_NO_NODES,
        PARSET_ERROR,
        FORMAT_ERROR
    }

    public BtException(ERROR_CODE error,String message){
        this(message);
        this.error_code=error;
    }
}
