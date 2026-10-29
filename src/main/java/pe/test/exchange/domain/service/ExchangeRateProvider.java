package pe.test.exchange.domain.service;

import pe.test.exchange.domain.exception.ExchangeRateUnavailableException;
import pe.test.exchange.domain.exception.UnsupportedCurrencyException;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.ExchangeRate;

/**
 * Puerto de salida: fuente de tipos de cambio.
 * El dominio no sabe si viene de una API REST, de una caché o de una base de datos.
 */
public interface ExchangeRateProvider {

    /**
     * @throws UnsupportedCurrencyException     si alguna de las monedas no es soportada
     * @throws ExchangeRateUnavailableException si el proveedor no está disponible
     */
    ExchangeRate getRate(CurrencyCode source, CurrencyCode target);
}
