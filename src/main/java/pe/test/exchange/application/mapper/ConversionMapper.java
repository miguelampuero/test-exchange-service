package pe.test.exchange.application.mapper;

import org.springframework.stereotype.Component;
import pe.test.exchange.application.dto.ConversionHistoryResponse;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.domain.model.CurrencyConversion;

/**
 * Traduce el modelo de dominio a los DTOs expuestos por la API.
 * Evita que el contrato HTTP dependa de la estructura interna del dominio.
 */
@Component
public class ConversionMapper {

    public ConversionResponse toResponse(CurrencyConversion conversion) {
        return new ConversionResponse(
                conversion.getOriginalAmount().amount(),
                conversion.getConvertedAmount().amount(),
                conversion.getSourceCurrency().value(),
                conversion.getTargetCurrency().value(),
                conversion.getRate()
        );
    }

    public ConversionHistoryResponse toHistoryResponse(CurrencyConversion conversion) {
        return new ConversionHistoryResponse(
                conversion.getId(),
                conversion.getOriginalAmount().amount(),
                conversion.getConvertedAmount().amount(),
                conversion.getSourceCurrency().value(),
                conversion.getTargetCurrency().value(),
                conversion.getRate(),
                conversion.getOperationDate()
        );
    }
}
