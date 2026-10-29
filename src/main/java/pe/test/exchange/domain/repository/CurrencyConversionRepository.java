package pe.test.exchange.domain.repository;

import pe.test.exchange.domain.model.CurrencyConversion;

import java.util.List;

/**
 * Puerto de persistencia del agregado {@link CurrencyConversion}.
 * El dominio define el contrato; la infraestructura lo implementa (JPA + H2).
 */
public interface CurrencyConversionRepository {

    CurrencyConversion save(CurrencyConversion conversion);

    List<CurrencyConversion> findAll();
}
