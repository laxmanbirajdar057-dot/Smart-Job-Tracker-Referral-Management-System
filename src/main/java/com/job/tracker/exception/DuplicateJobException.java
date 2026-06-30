package com.job.tracker.exception;

public class DuplicateJobException extends RuntimeException {
    public DuplicateJobException(String message){
        super(message);
    }
}
