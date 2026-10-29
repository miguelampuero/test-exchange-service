package pe.test.exchange.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.test.exchange.application.dto.ConversionHistoryResponse;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.mapper.ConversionMapper;
import pe.test.exchange.domain.model.CurrencyCode;
import pe.test.exchange.domain.model.CurrencyConversion;
import pe.test.exchange.domain.model.Money;
import pe.test.exchange.domain.repository.CurrencyConversionRepository;
import pe.test.exchange.domain.service.CurrencyConverterService;

import java.util.List;

/**
 * Servicio de aplicación (caso de uso): convierte un monto y registra la operación.
 * Coordina dominio y persistencia; no contiene reglas de negocio.
 */
@Service
public class CurrencyConversionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyConversionApplicationService.class);

    private final CurrencyConverterService converterService;
    private final CurrencyConversionRepository repository;
    private final ConversionMapper mapper;

    public CurrencyConversionApplicationService(CurrencyConverterService converterService,
                                                CurrencyConversionRepository repository,
                                                ConversionMapper mapper) {
        this.converterService = converterService;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public ConversionResponse convert(ConversionRequest request) {
        Money amount = Money.of(request.monto(), request.monedaOrigen());
        CurrencyCode target = CurrencyCode.of(request.monedaDestino());

        CurrencyConversion conversion = converterService.convert(amount, target);
        CurrencyConversion saved = repository.save(conversion);

        log.info("Conversión registrada id={} {} {} -> {} {} (tc={})",
                saved.getId(), saved.getOriginalAmount().amount(), saved.getSourceCurrency(),
                saved.getConvertedAmount().amount(), saved.getTargetCurrency(), saved.getRate());

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ConversionHistoryResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toHistoryResponse)
                .toList();
    }
}
