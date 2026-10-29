package pe.test.exchange.domain.model;

import pe.test.exchange.domain.exception.InvalidCurrencyException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value Object: código de moneda ISO 4217 (ej. USD, PEN, EUR).
 * Se normaliza a mayúsculas y se valida el formato al construirse,
 * por lo que una instancia siempre es válida.
 */
public record CurrencyCode(String value) {

    private static final Pattern ISO_4217 = Pattern.compile("^[A-Z]{3}$");

    public CurrencyCode {
        if (value == null || value.isBlank()) {
            throw new InvalidCurrencyException("El código de moneda es obligatorio");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!ISO_4217.matcher(value).matches()) {
            throw new InvalidCurrencyException("El código de moneda '" + value + "' no es válido; debe tener 3 letras (ISO 4217)");
        }
    }

    public static CurrencyCode of(String value) {
        return new CurrencyCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
