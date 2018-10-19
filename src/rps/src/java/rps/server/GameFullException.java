/*
 * GameFullException.java
 *
 */

package rps.server;

/**
 *
 * @author dhilder
 */
public class GameFullException extends Exception {
    
    /** Creates a new instance of GameFullException */
    public GameFullException() {
        super();
    }
    
    public GameFullException(String msg) {
        super(msg);
    }
    
    public GameFullException(Throwable cause) {
        super(cause);
    }
    
    public GameFullException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
