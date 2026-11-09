package br.com.wagnersoft.patagonia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.wagnersoft.patagonia.ibge.Municipio;

public class MunicipioServiceTest {

  private MunicipioService svc;

  private Municipio m1;

  @BeforeEach
  void setup() {
    svc = new MunicipioService();
    m1 = new Municipio();
    m1.setId(1L);
    m1.setNome("XYZ");
  }

  @Test
  void devePesquisarPorId() {
    Municipio mun = svc.findById(3525904L);
    assertEquals(mun.getId(), 3525904L);
  }

  @Test
  void devePesquisarPorNome() {
    Optional<Municipio> mun = svc.findByNome("Jundiaí");
    assertTrue(mun.isPresent());
    assertEquals("Jundiaí", mun.get().getNome());
  }

}
