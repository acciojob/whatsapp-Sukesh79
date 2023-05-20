package com.driver;

public class UserAlreadyExistException extends Exception{
    public UserAlreadyExistException(String message){
        super(message);
    }
}
