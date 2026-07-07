package ar.utn.ba.ddsi.mailing.services.impl;

import ar.utn.ba.ddsi.mailing.models.entities.Clima;
import ar.utn.ba.ddsi.mailing.models.repositories.IClimaRepository;
import ar.utn.ba.ddsi.mailing.models.dto.external.weatherapi.WeatherResponse;
import ar.utn.ba.ddsi.mailing.services.IClimaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ClimaService implements IClimaService {
  private static final Logger logger = LoggerFactory.getLogger(ClimaService.class);

  private final IClimaRepository climaRepository;
  private final WebClient webClient;
  private final String apiKey;
  private final String ubicacion;

  public ClimaService(
      IClimaRepository climaRepository,
      @Value("${weather.api.key}") String apiKey,
      @Value("${weather.api.base-url}") String baseUrl,
      @Value("${weather.location}") String ubicacion) {
    this.climaRepository = climaRepository;
    this.apiKey = apiKey;
    this.ubicacion = ubicacion;
    this.webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  @Override
  public Mono<Void> actualizarClimaCiudades() {
    return obtenerClimaDeAPI(ubicacion)
        .flatMap(clima -> {
          climaRepository.save(clima);
          logger.info("Clima actualizado para: {}", clima.getCiudad());
          return Mono.empty();
        })
        .onErrorResume(e -> {
          logger.error("Error al actualizar el clima de {}: {}", ubicacion, e.getMessage());
          return Mono.empty();
        })
        .then();
  }

  private Mono<Clima> obtenerClimaDeAPI(String ubicacion) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/current.json")
            .queryParam("key", apiKey)
            .queryParam("q", ubicacion)
            .queryParam("aqi", "no")
            .build())
        .retrieve()
        .bodyToMono(WeatherResponse.class)
        .map(response -> {
          Clima clima = new Clima();
          clima.setCiudad(ubicacion);
          clima.setRegion(response.getLocation().getRegion());
          clima.setPais(response.getLocation().getCountry());
          clima.setTemperaturaCelsius(response.getCurrent().getTemp_c());
          clima.setTemperaturaFahrenheit(response.getCurrent().getTemp_f());
          clima.setCondicion(response.getCurrent().getCondition().getText());
          clima.setVelocidadVientoKmh(response.getCurrent().getWind_kph());
          clima.setHumedad(response.getCurrent().getHumidity());
          return clima;
        });
  }
}
