package exception;


public class NoServerConnectionFoundException extends Exception {

    private final int errorCode;

    
    public NoServerConnectionFoundException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}