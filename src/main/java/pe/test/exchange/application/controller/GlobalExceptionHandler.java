package pe.test.exchange.application.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.test.exchange.application.dto.ErrorResponse;
import pe.test.exchange.domain.exception.DomainException;
import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.InvalidAmountException;
import pe.test.exchange.domain.exception.InvalidCurrencyException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;

import java.util.List;

/**
 * Traduce excepciones (de dominio y de Spring MVC) a respuestas HTTP con un formato uniforme.
 * Los mensajes internos de errores inesperados nunca se exponen al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .toList();
        return build(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA",
                "La solicitud contiene campos inválidos", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO",
                "El cuerpo de la solicitud no es un JSON válido o tiene tipos de datos incorrectos", request, List.of());
    }

    @ExceptionHandler({InvalidAmountException.class, InvalidCurrencyException.class, UnsupportedCurrencyException.class})
    public ResponseEntity<ErrorResponse> handleBusinessValidation(DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ExchangeRateUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleProviderUnavailable(ExchangeRateUnavailableException ex, HttpServletRequest request) {
        log.warn("Proveedor de tipo de cambio no disponible: {}", ex.getMessage(), ex.getCause());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getCode(),
                "El servicio de tipo de cambio no está disponible en este momento. Intente nuevamente más tarde",
                request, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_CONTENIDO_NO_SOPORTADO",
                "El Content-Type debe ser application/json", request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "Método HTTP no permitido: " + ex.getMethod(), request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", "Recurso no encontrado", request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado procesando {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado", request, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                HttpServletRequest request, List<String> details) {
        ErrorResponse body = ErrorResponse.of(status.value(), code, message, request.getRequestURI(), details);
        return ResponseEntity.status(status).body(body);
    }
}
