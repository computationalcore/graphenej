package com.luminiasoft.bitshares.models;

/**
 * Created by nelson on 11/12/16.
 */
public class BaseResponse {
    public int id;
    public Error error;

    public static class Error {
        public ErrorData data;
        public int code;
        public String message;
        public Error(String message){
            this.message = message;
        }
    }

    public static class ErrorData {
        public int code;
        public String name;
        public String message;
        //TODO: Include stack data

        public ErrorData(String message){
            this.message = message;
        }
    }
}
