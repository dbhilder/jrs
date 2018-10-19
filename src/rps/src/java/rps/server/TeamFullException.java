/*
 * TeamFullException.java
 *
 */

package rps.server;

/**
 *
 * @author dhilder
 */
public class TeamFullException extends Exception {
    
    public TeamFullException() {
        super();
    }
    
    public TeamFullException(String msg) {
        super(msg);
    }
    
    public TeamFullException(Throwable cause) {
        super(cause);
    }
    
    public TeamFullException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
