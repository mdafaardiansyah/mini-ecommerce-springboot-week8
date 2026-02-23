package edts.week7_practice1.exception;

import java.util.List;

public class BusinessException extends RuntimeException {
    private final String code;
    private final List<String> details;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    public BusinessException(String code, String message, List<String> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}
