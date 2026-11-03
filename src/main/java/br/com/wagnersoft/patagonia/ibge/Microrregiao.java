package br.com.wagnersoft.patagonia.ibge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

/**
 * Microrregião padrão IBGE.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Microrregiao {

  private Integer id;

  private String nome;

  private Mesorregiao mesorregiao;

}
