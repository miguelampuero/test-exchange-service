package pe.test.exchange.domain.exception;

/**
 * Excepción base del dominio. Cada subclase expone un código estable
 * que la capa de aplicación traduce a una respuesta HTTP.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
