package congestion.calculator.exception;

import org.openapitools.model.TollErrorResponseTo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({TollException.class})
    public ResponseEntity<TollErrorResponseTo> handleException(TollException exception) {
        TollErrorResponseTo tollErrorResponseTo = new TollErrorResponseTo(exception.getLicensePlate(),
                exception.getErrorCode(), exception.getReason());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(tollErrorResponseTo);
    }
}
