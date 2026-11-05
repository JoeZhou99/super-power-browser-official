package com.superpower.official;

public class RegistrationFailure extends RuntimeException {
    private final int status;
    public RegistrationFailure(int status, String message) { super(message); this.status = status; }
    public int status() { return status; }
}
