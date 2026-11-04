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

@Service
public class PopulacaoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PopulacaoService.class);

  private final HttpClient httpClient;
  
  private final ObjectMapper mapper;

  private final Map<Long, Long> cache = new ConcurrentHashMap<>();

  public PopulacaoService() {
    this.httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    
    this.mapper = new ObjectMapper();
  }

  public Long obterPopulacao(Long codigoMunicipio) {

    if (cache.containsKey(codigoMunicipio)) {
      return cache.get(codigoMunicipio);
    }

    try {
      String url = "https://api.sidra.ibge.gov.br/values/t/6579/n6/" + codigoMunicipio + "/v/9324";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      JsonNode root = mapper.readTree(response.body());

      if (root.size() < 2) {
        throw new RuntimeException("Nenhum dado encontrado para o município " + codigoMunicipio);
      }

      // População retornada
      Long populacao = root.get(1).get("V").asLong();
      cache.put(codigoMunicipio, populacao);
      LOGGER.info("População carregada para município {}: {}", codigoMunicipio, populacao);

      return populacao;
    } catch (Exception e) {
      throw new RuntimeException("Erro ao consultar população do município " + codigoMunicipio, e);
    }
  }

}
