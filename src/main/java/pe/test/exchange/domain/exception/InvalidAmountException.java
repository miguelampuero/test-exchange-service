package pe.test.exchange.domain.exception;

/**
 * El monto a convertir no cumple las reglas del dominio (nulo, cero o negativo).
 */
public class InvalidAmountException extends DomainException {

    public InvalidAmountException(String message) {
        super("MONTO_INVALIDO", message);
    }
}
