package exception;

import javax.ws.rs.core.Response;

public class InvalidDataException extends Exception {
    
    private final int errorCode;
    
    public InvalidDataException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}