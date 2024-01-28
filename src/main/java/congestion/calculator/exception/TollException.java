package congestion.calculator.exception;

import lombok.Getter;

@Getter
public class TollException extends Exception{
    private final String licensePlate;
    private final Long errorCode;
    private final String reason;

    private TollException(String licensePlate, Long errorCode, String reason) {
        this.licensePlate = licensePlate;
        this.errorCode = errorCode;
        this.reason = reason;
    }

    public static Exception createTollException(String licensePlate, Long errorCode, String message) {
        return new TollException(licensePlate, errorCode, message);
    }
}
