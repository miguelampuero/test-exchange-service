package pe.test.exchange.domain.exception;

/**
 * No fue posible obtener el tipo de cambio (proveedor caído, timeout, cuota agotada, credenciales inválidas, etc.).
 */
public class ExchangeRateUnavailableException extends DomainException {

    public ExchangeRateUnavailableException(String message) {
        super("SERVICIO_TIPO_CAMBIO_NO_DISPONIBLE", message);
    }

    public ExchangeRateUnavailableException(String message, Throwable cause) {
        super("SERVICIO_TIPO_CAMBIO_NO_DISPONIBLE", message, cause);
    }
}
