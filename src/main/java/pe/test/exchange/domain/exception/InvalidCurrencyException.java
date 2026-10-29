package pe.test.exchange.domain.exception;

/**
 * El código de moneda no tiene un formato válido ISO 4217 (3 letras).
 */
public class InvalidCurrencyException extends DomainException {

    public InvalidCurrencyException(String message) {
        super("MONEDA_INVALIDA", message);
    }
}
