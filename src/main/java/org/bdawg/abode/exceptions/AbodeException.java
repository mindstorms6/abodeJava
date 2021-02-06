package org.bdawg.abode.exceptions;

public class AbodeException extends Exception {
    public AbodeException(String message) {
        super(message);
    }

    public AbodeException(String message, Exception cause) {
        super(message, cause);
    }
}
