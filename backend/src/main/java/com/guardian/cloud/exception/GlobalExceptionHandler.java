package com.guardian.cloud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.guardian.cloud.exception.AlertAccessDeniedException;
import com.guardian.cloud.exception.GuardianAlertNotFoundException;
import com.guardian.cloud.exception.InvalidAlertStateException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceNotFound(
            DeviceNotFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateTelemetryException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateTelemetry(
            DuplicateTelemetryException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DeviceIdentityMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceIdentityMismatch(
            DeviceIdentityMismatchException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateDeviceEventException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateDeviceEvent(
            DuplicateDeviceEventException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(GuardianUserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGuardianUserNotFound(
            GuardianUserNotFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(GuardianAlertNotFoundException.class)
	public ResponseEntity<Map<String, Object>>
	handleGuardianAlertNotFound(
			GuardianAlertNotFoundException exception
	) {
	return buildResponse(
			HttpStatus.NOT_FOUND,
			exception.getMessage()
	);
	}

	@ExceptionHandler(AlertAccessDeniedException.class)
	public ResponseEntity<Map<String, Object>>
	handleAlertAccessDenied(
			AlertAccessDeniedException exception
	) {
	return buildResponse(
			HttpStatus.FORBIDDEN,
			exception.getMessage()
	);
	}

	@ExceptionHandler(InvalidAlertStateException.class)
	public ResponseEntity<Map<String, Object>>
	handleInvalidAlertState(
			InvalidAlertStateException exception
	) {
	return buildResponse(
			HttpStatus.CONFLICT,
			exception.getMessage()
	);
	}

    @ExceptionHandler(LocationAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleLocationAccessDenied(
            LocationAccessDeniedException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidLocationRangeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidLocationRange(
            InvalidLocationRangeException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidPairingCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPairingCode(
            InvalidPairingCodeException exception
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DeviceAlreadyPairedException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceAlreadyPaired(
            DeviceAlreadyPairedException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DeviceAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDeviceAccessDenied(
            DeviceAccessDeniedException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("fields", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}