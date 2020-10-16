package common;

public class WhackException  extends  Exception{

    /**
     * Constructing a new whack.WhackException
     * @param message message to potentially be printed in output
     */
    public WhackException(String message){super(message);}

    /**
     * Constructing a new whack.WhackException in the case that it's a result of another exception
     * @param cause / reason / message to be potentially printed in output
     */
    public WhackException(Throwable cause){super(cause);}


    /**
     * Constructing a new whack.WhackException in the case that it's a result of another exception
     * @param message The message associated with the exception.
     * @param cause The original cause of the exception.
     */
    public WhackException(String message, Throwable cause){super(message, cause);}


}
