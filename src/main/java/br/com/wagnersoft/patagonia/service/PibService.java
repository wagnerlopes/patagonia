package br.com.wagnersoft.patagonia.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * PibService.
 *
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Service
public class PibService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PibService.class);

  private final HttpClient httpClient;

  private final ObjectMapper mapper;

  private final Map<Long, Double> cache = new ConcurrentHashMap<>();

  public PibService() {

    this.httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    this.mapper = new ObjectMapper();
  }

  public Double obterPib(Long munId) {

    if (cache.containsKey(munId)) {
      return cache.get(munId);
    }

    try {

      String url = "https://api.sidra.ibge.gov.br/values/t/5938/n6/" + munId + "/v/37";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      JsonNode root = mapper.readTree(response.body());

      if (root.size() < 2) {
        throw new RuntimeException("Nenhum dado encontrado: COD " + munId);
      }

      // População retornada
      Double pib = root.get(1).get("V").asDouble();
      cache.put(munId, pib);

      LOGGER.info("PIB carregado (COD {}): {}", munId, pib);
      return pib;
    } catch (Exception e) {
      throw new RuntimeException("Erro ao consultar PIB COD " + munId, e);
    }

  }

}
