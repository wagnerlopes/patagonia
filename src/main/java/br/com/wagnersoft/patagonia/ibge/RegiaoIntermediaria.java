package br.com.wagnersoft.patagonia.ibge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

/**
 * Região intermediária padrão IBGE.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegiaoIntermediaria {

  private Integer id;

  private String nome;

  @JsonProperty("UF")
  private Uf uf;

}
