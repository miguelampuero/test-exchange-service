package pe.test.exchange.infrastructure.external;

/**
 * Error técnico devuelto por ExchangeRate-API (respuesta HTTP no exitosa).
 * Es una excepción de infraestructura: el adaptador la traduce a excepciones de dominio.
 */
public class ExchangeRateApiException extends RuntimeException {

    private final int status;
    private final String errorType;

    public ExchangeRateApiException(int status, String errorType) {
        super("ExchangeRate-API respondió HTTP " + status + (errorType != null ? " (" + errorType + ")" : ""));
        this.status = status;
        this.errorType = errorType;
    }

    public int getStatus() {
        return status;
    }

    /**
     * Valor del campo {@code error-type} de la API (ej. unsupported-code, invalid-key, quota-reached), o null.
     */
    public String getErrorType() {
        return errorType;
    }
}
