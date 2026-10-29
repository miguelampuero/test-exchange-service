package pe.test.exchange.application.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.test.exchange.application.dto.ConversionHistoryResponse;
import pe.test.exchange.application.dto.ConversionRequest;
import pe.test.exchange.application.dto.ConversionResponse;
import pe.test.exchange.application.service.CurrencyConversionApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api/conversion")
public class CurrencyConversionController {

    private final CurrencyConversionApplicationService conversionService;

    public CurrencyConversionController(CurrencyConversionApplicationService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Convierte un monto entre dos monedas y registra la operación.
     */
    @PostMapping
    public ResponseEntity<ConversionResponse> convert(@Valid @RequestBody ConversionRequest request) {
        return ResponseEntity.ok(conversionService.convert(request));
    }

    /**
     * Lista las conversiones registradas (permite verificar la persistencia).
     */
    @GetMapping
    public ResponseEntity<List<ConversionHistoryResponse>> findAll() {
        return ResponseEntity.ok(conversionService.findAll());
    }
}
