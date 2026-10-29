# Desafío Técnico – API de Tipo de Cambio

Microservicio en **Java 17 + Spring Boot** que consume **ExchangeRate-API** mediante **OpenFeign**, expone un endpoint REST para convertir montos entre monedas y registra cada conversión en **H2** (en memoria). Diseñado con **DDD** y arquitectura por capas (hexagonal / puertos y adaptadores).

---

## Stack

| Tecnología | Versión |
|---|---|
| Java | 17+ |
| Spring Boot | 4.0.6 |
| Spring Cloud OpenFeign | 2025.1.1 |
| Spring Data JPA + H2 | en memoria |
| Bean Validation (Hibernate Validator) | incluido |
| JUnit 5 + Mockito + AssertJ + MockMvc | pruebas |
| Maven | wrapper incluido (`./mvnw`) |

---

## Cómo ejecutar

**Requisitos:** JDK 17 o superior. No es necesario instalar Maven (se usa el wrapper).

```bash
# 1. Clonar
git clone https://github.com/<tu-usuario>/test-exchange-service.git
cd test-exchange-service

# 2. Ejecutar pruebas
./mvnw test                 # Windows: mvnw.cmd test

# 3. Levantar la aplicación
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

La API key del desafío viene configurada por defecto en `application.yaml`. Para usar otra:

```bash
EXCHANGE_RATE_API_KEY=<tu-api-key> ./mvnw spring-boot:run
```

### Consola H2

| Parámetro | Valor |
|---|---|
| URL | `http://localhost:8080/h2-console` |
| JDBC URL | `jdbc:h2:mem:testdb` |
| Usuario / Password | `sa` / `password` |
| Tabla | `CONVERSION_MONEDA` |

---

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/conversion` | Convierte un monto y registra la operación |
| `GET` | `/api/conversion` | Lista las conversiones registradas (más recientes primero) |

### Ejemplo – conversión exitosa

```bash
curl -X POST http://localhost:8080/api/conversion \
  -H "Content-Type: application/json" \
  -d '{"monto": 100, "monedaOrigen": "USD", "monedaDestino": "PEN"}'
```

**Request**
```json
{
  "monto": 100,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN"
}
```

**Response `200 OK`** (el tipo de cambio es el real devuelto por la API al momento de la consulta)
```json
{
  "monto": 100,
  "montoConvertido": 370.00,
  "monedaOrigen": "USD",
  "monedaDestino": "PEN",
  "tipoCambio": 3.70
}
```

### Ejemplo – historial

```bash
curl http://localhost:8080/api/conversion
```

```json
[
  {
    "id": 1,
    "monto": 100,
    "montoConvertido": 370.00,
    "monedaOrigen": "USD",
    "monedaDestino": "PEN",
    "tipoCambio": 3.70,
    "fechaOperacion": "2026-10-29T09:00:00.123"
  }
]
```

### Ejemplos de error

Todas las respuestas de error tienen el mismo formato:

**`400` – validación del request** (`{"monto": -5, "monedaOrigen": "US", "monedaDestino": "PEN"}`)
```json
{
  "timestamp": "2026-10-29T09:00:00.123",
  "status": 400,
  "codigo": "SOLICITUD_INVALIDA",
  "mensaje": "La solicitud contiene campos inválidos",
  "path": "/api/conversion",
  "detalles": [
    "monedaOrigen: La moneda origen debe tener 3 letras (ISO 4217)",
    "monto: El monto debe ser mayor a cero"
  ]
}
```

**`400` – moneda no soportada** (`"monedaDestino": "XYZ"`)
```json
{
  "timestamp": "2026-10-29T09:00:00.123",
  "status": 400,
  "codigo": "MONEDA_NO_SOPORTADA",
  "mensaje": "La moneda 'XYZ' no está soportada por el proveedor de tipo de cambio",
  "path": "/api/conversion"
}
```

**`503` – proveedor externo no disponible** (timeout, error de red, cuota agotada, API key inválida)
```json
{
  "timestamp": "2026-10-29T09:00:00.123",
  "status": 503,
  "codigo": "SERVICIO_TIPO_CAMBIO_NO_DISPONIBLE",
  "mensaje": "El servicio de tipo de cambio no está disponible en este momento. Intente nuevamente más tarde",
  "path": "/api/conversion"
}
```

| HTTP | Código | Cuándo |
|---|---|---|
| 400 | `SOLICITUD_INVALIDA` | Campos faltantes o con formato inválido (Bean Validation) |
| 400 | `CUERPO_INVALIDO` | JSON malformado o tipos incorrectos (ej. `"monto": "abc"`) |
| 400 | `MONTO_INVALIDO` | Monto ≤ 0, o tan pequeño que convertido redondea a 0 |
| 400 | `MONEDA_INVALIDA` | Código de moneda que no cumple ISO 4217 (3 letras) |
| 400 | `MONEDA_NO_SOPORTADA` | Moneda origen o destino no soportada por ExchangeRate-API |
| 415 | `TIPO_CONTENIDO_NO_SOPORTADO` | `Content-Type` distinto de `application/json` |
| 503 | `SERVICIO_TIPO_CAMBIO_NO_DISPONIBLE` | Proveedor caído, timeout, cuota agotada o API key inválida |
| 500 | `ERROR_INTERNO` | Error no previsto (se registra en log, no se expone el detalle) |

---

## Arquitectura

```
pe.test.exchange
├── domain                         ← Núcleo de negocio. Java puro, sin Spring ni JPA
│   ├── model
│   │   ├── CurrencyCode           (Value Object – código ISO 4217 validado y normalizado)
│   │   ├── Money                  (Value Object – monto > 0 + moneda)
│   │   ├── ExchangeRate           (Value Object – tipo de cambio; regla de conversión y redondeo)
│   │   └── CurrencyConversion     (Entidad / raíz de agregado – la operación de conversión)
│   ├── service
│   │   ├── CurrencyConverterService (Servicio de dominio)
│   │   └── ExchangeRateProvider     (Puerto de salida hacia el proveedor de tipos de cambio)
│   ├── repository
│   │   └── CurrencyConversionRepository (Puerto de persistencia)
│   └── exception                  (DomainException y subclases con código de error)
├── application                    ← Casos de uso y borde HTTP
│   ├── controller                 (CurrencyConversionController, GlobalExceptionHandler)
│   ├── service                    (CurrencyConversionApplicationService – caso de uso, @Transactional)
│   ├── dto                        (ConversionRequest/Response, ConversionHistoryResponse, ErrorResponse)
│   └── mapper                     (ConversionMapper – dominio → DTO)
└── infrastructure                 ← Detalles técnicos (adaptadores)
    ├── external                   (ExchangeRateApiClient Feign, ExchangeRateApiAdapter, ErrorDecoder)
    ├── persistence                (Entidad JPA, Spring Data repo, RepositoryAdapter, mapper)
    └── config                     (DomainConfig, ExchangeRateApiProperties)
```

**Flujo de una conversión**

```
POST /api/conversion
  → Controller (@Valid: formato del request)
  → ApplicationService.convert()
      → crea Money y CurrencyCode (el dominio vuelve a validar sus invariantes)
      → CurrencyConverterService.convert()
          → misma moneda: tipo de cambio 1, sin llamar a la API
          → otra moneda: ExchangeRateProvider.getRate()  ──►  ExchangeRateApiAdapter ──► Feign ──► ExchangeRate-API
          → CurrencyConversion.perform()  (aplica ExchangeRate.convert, fecha desde Clock)
      → CurrencyConversionRepository.save()  ──►  RepositoryAdapter ──► Spring Data JPA ──► H2
  ← ConversionResponse
```

### Decisiones de diseño

- **Dominio independiente de frameworks.** Las clases de `domain` no tienen anotaciones de Spring ni JPA. `CurrencyConverterService` se registra como bean en `infrastructure/config/DomainConfig`. Esto permite probar el dominio con JUnit puro y cambiar de tecnología sin tocar reglas de negocio.
- **Modelo de persistencia separado del de dominio.** `CurrencyConversionJpaEntity` vive en infraestructura y se mapea al agregado `CurrencyConversion`. El dominio sólo conoce el puerto `CurrencyConversionRepository`.
- **Puertos y adaptadores.** `ExchangeRateProvider` y `CurrencyConversionRepository` son interfaces del dominio; `ExchangeRateApiAdapter` y `CurrencyConversionRepositoryAdapter` las implementan (inversión de dependencias).
- **Anti-corruption layer.** `ExchangeRateApiAdapter` traduce el formato de ExchangeRate-API (`conversion_rates`, `error-type`) y sus fallos a conceptos y excepciones del dominio; el resto de la aplicación no conoce Feign.
- **Value Objects auto-validados.** `CurrencyCode`, `Money` y `ExchangeRate` validan sus invariantes en el constructor (records inmutables): no puede existir un monto negativo ni una moneda mal formada en el dominio. La validación con Bean Validation en el DTO es un *fail-fast* en el borde HTTP con mensajes por campo; el dominio no depende de ella.
- **Dinero con `BigDecimal`.** Nunca `double`. El monto convertido se redondea a 2 decimales con `HALF_UP`; el tipo de cambio se guarda con la precisión que entrega la API.
- **Tiempo inyectable.** La fecha de operación se obtiene de un `Clock` inyectado, lo que hace las pruebas deterministas.
- **Configuración validada al arranque.** `ExchangeRateApiProperties` (`@Validated`) impide levantar la app sin `base-url` o `key`. La API key puede sobreescribirse por variable de entorno.
- **Timeouts del cliente Feign** (`connect-timeout: 3s`, `read-timeout: 5s`) para que una API lenta no bloquee los hilos indefinidamente; se responde `503`.

### Manejo de errores y casos borde

- Request sin campos, con `null`, vacío, monto ≤ 0, más de 4 decimales o monedas que no tienen 3 letras → `400` con detalle por campo.
- JSON malformado o tipos incorrectos → `400 CUERPO_INVALIDO`.
- Monedas en minúscula o con espacios (`" usd "`) → se normalizan a `USD`.
- Misma moneda origen y destino → tipo de cambio `1`, no consume cuota de la API.
- Moneda origen no soportada (la API responde `unsupported-code`) o moneda destino ausente en `conversion_rates` → `400 MONEDA_NO_SOPORTADA`.
- Monto que convertido redondea a `0.00` → `400 MONTO_INVALIDO`.
- API key inválida, cuenta inactiva, cuota agotada, timeout, error de red, respuesta vacía o sin tipos de cambio → `503`. Son fallas del proveedor, no del cliente, y el detalle técnico sólo va al log.
- Cualquier error no previsto → `500` con mensaje genérico (sin *stack trace* ni detalles internos).
- Si la conversión falla, no se persiste nada (la operación es transaccional).

---

## Pruebas

```bash
./mvnw test
```

| Clase de prueba | Tipo | Qué cubre |
|---|---|---|
| `CurrencyCodeTest`, `MoneyTest`, `ExchangeRateTest` | Unitarias de dominio | Invariantes de los Value Objects, normalización, conversión y redondeo `HALF_UP`, monto que redondea a 0 |
| `CurrencyConverterServiceTest` | Unitaria de dominio | Uso del proveedor, misma moneda sin llamar a la API, fecha desde `Clock` fijo |
| `CurrencyConversionApplicationServiceTest` | Unitaria de aplicación | Orquestación, persistencia del agregado, no persistir ante errores, historial |
| `CurrencyConversionControllerTest` | MockMvc *standalone* | Contrato JSON, validaciones `400`, JSON malformado, `415`, `503`, `500` |
| `ExchangeRateApiAdapterTest` | Unitaria de infraestructura | Traducción de respuestas y errores de la API (`unsupported-code`, `invalid-key`, `quota-reached`, timeouts…) |
| `ExchangeRateApiErrorDecoderTest` | Unitaria de infraestructura | Lectura de `error-type` desde respuestas HTTP de error |
| `CurrencyConversionPersistenceMapperTest` | Unitaria de infraestructura | Mapeo entidad JPA ↔ agregado, sin ceros de relleno de la columna |
| `ExchangeServiceApplicationTests` | Integración (`@SpringBootTest` + H2) | Flujo completo HTTP → dominio → H2 con el cliente Feign simulado; verifica lo persistido |

Las pruebas no llaman a la API real (el cliente Feign se simula), así no dependen de la red ni consumen cuota.

---

## Posibles mejoras

- **Caché de tipos de cambio** (ej. Caffeine con TTL): la API actualiza los tipos una vez al día en el plan gratuito, así que cachear por moneda base reduce latencia y consumo de cuota.
- **Resiliencia** con Resilience4j (circuit breaker + retry con *backoff*) sobre el adaptador.
- **Paginación** del historial y filtros por moneda/fecha.
- **Documentación OpenAPI** (springdoc) y **Testcontainers/WireMock** para probar el cliente Feign contra respuestas HTTP reales.
- Migraciones con Flyway en lugar de `ddl-auto` para una base de datos persistente.
