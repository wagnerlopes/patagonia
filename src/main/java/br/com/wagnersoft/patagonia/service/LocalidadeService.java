package br.com.wagnersoft.patagonia.service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** 
 * LocalidadeService.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Service
public class LocalidadeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalidadeService.class);

  private final Map<Integer, String> cache = new ConcurrentHashMap<>();

  public LocalidadeService() {
    initCache();
    LOGGER.info("Cache iniciado");
  }

  public void initCache() {

    try {

      HttpClient httpClient = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(5))
          .followRedirects(HttpClient.Redirect.NORMAL).build();

      ObjectMapper mapper = new ObjectMapper();

      String url = "https://babilonia.local/api/v1/localidade";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Accept", "application/json")
          .header("x-api-key", "3f6988fd-feff-477a-9772-c2ceefda3b39")
          .timeout(Duration.ofSeconds(10))
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      JsonNode root = mapper.readTree(response.body());

      if (!root.isArray() || root.isEmpty()) {
        throw new RuntimeException("Nenhum dado encontrado");
      }

      root.forEachEntry((s,n) -> cache.put(n.get("id").asInt(), n.get("descricao").asString()));

    } catch (HttpTimeoutException e) {
      LOGGER.error("Timeout ao acessar API de localidade", e);
    } catch (ConnectException e) {
      LOGGER.error("API de localidade indisponível", e);
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Erro ao acessar API de localidade", e);
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }      
    } catch (Exception e) {
      LOGGER.error("Falha ao atualizar cache. Mantendo cache atual.", e);
    }
  }

  public Map<Integer, String> mapAll() {
    return this.cache;
  }

}
