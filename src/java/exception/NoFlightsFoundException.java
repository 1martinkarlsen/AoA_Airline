package exception;

public class NoFlightsFoundException extends Exception {
    
    private final int errorCode;
    
    public NoFlightsFoundException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
