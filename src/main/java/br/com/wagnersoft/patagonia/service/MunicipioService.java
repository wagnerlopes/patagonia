package br.com.wagnersoft.patagonia.service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import br.com.wagnersoft.patagonia.ibge.Municipio;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** 
 * MunicipioService.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Service
public class MunicipioService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MunicipioService.class);

  private final List<Municipio> municipios;

  private final Map<Long, Municipio> municipiosPorId;

  public MunicipioService(){

    ObjectMapper mapper = new ObjectMapper();

    try {
      ClassPathResource resource = new ClassPathResource("municipios.json");

      this.municipios = Collections.unmodifiableList(mapper.readValue(resource.getFile(), new TypeReference<List<Municipio>>() {}));

      this.municipiosPorId = municipios.stream().collect(Collectors.toMap(Municipio::getId, Function.identity()));

      LOGGER.info("Total de municípios: {}", municipios.size());

    } catch (JacksonException | IOException e) {
      throw new RuntimeException(e);
    }

  }

  public List<Municipio> findAll() {
    return municipios;
  }

  public Map<Long, String> mapAll() {
    return this.findAll().stream()
        .collect(Collectors.toMap(Municipio::getId, Municipio::getNome, (existing, replacement) -> existing, LinkedHashMap::new));
  }
  
  public Municipio findById(Long id) {
    return municipiosPorId.get(id);
  }

  public Optional<Municipio> findByNome(String nome) {
    return municipios.stream()
        .filter(m -> m.getNome().equalsIgnoreCase(nome))
        .findFirst();
  }  

}
