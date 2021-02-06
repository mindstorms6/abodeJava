package org.bdawg.abode.exceptions;

public class AbodeAuthenticationException extends AbodeException {
    public AbodeAuthenticationException(String message) {
        super(message);
    }

    public AbodeAuthenticationException(int statusCode, String message) {
        super("Failed to login with status " + statusCode + ": " + message);
    }
}
