package br.com.wagnersoft.patagonia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.com.wagnersoft.patagonia.geoinfo.Azimute;

public class AzimuteTest {

  @Test
  void deveCalcularAzimuteVerdadeiro() {

    Azimute az = new Azimute(-23.166864, -47.061320, -23.2310352, -46.9986411);

    assertEquals(138.096, az.getAzimuteVerdadeiro(), 0.01);
  }

  @Test
  void deveCalcularAzimuteMagnetico() {

    double azVerdadeiro = 138.096;
    double declinacao = -21.818594080209905;

    double azMagnetico = (azVerdadeiro - declinacao + 360.0) % 360.0;

    assertEquals(159.914594, azMagnetico, 0.01);
  }

}
