package ai.zuva.exception;

public class ZdaiException extends Exception{
    public ZdaiException(){
        super();
    }
    public ZdaiException(String message){
        super(message);
    }
    public ZdaiException(String message, Throwable e){
        super(message, e);
    }

}
