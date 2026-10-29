package pe.test.exchange.domain.exception;

/**
 * El código de moneda tiene formato válido pero el proveedor de tipo de cambio no lo soporta.
 */
public class UnsupportedCurrencyException extends DomainException {

    public UnsupportedCurrencyException(String currencyCode) {
        super("MONEDA_NO_SOPORTADA", "La moneda '" + currencyCode + "' no está soportada por el proveedor de tipo de cambio");
    }
}
