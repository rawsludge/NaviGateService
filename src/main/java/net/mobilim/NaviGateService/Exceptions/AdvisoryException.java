package net.mobilim.NaviGateService.Exceptions;

public class AdvisoryException extends Exception {
    public AdvisoryException(String code, String text) {
        super(String.format("Code: %s, description: %s", code, text));
    }
}
