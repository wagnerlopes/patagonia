package br.com.wagnersoft.patagonia.ibge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

/**
 * Região padrão IBGE.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Regiao {

  private Integer id;

  private String sigla;

  private String nome;

}
