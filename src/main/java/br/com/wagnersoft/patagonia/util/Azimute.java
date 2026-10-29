package br.com.wagnersoft.patagonia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Azimute {

  private static final Logger LOGGER = LoggerFactory.getLogger(Azimute.class);
  
  private double lat1;

  private double lon1;

  private double lat2;

  private double lon2;

  private Geomagnetism gm;

  public Azimute(double lat1, double lon1, double lat2, double lon2) {
    this.lat1 = lat1;
    this.lon1 = lon1;
    this.lat2 = lat2;
    this.lon2 = lon2;
    this.gm = new Geomagnetism(lat1, lon1);
    LOGGER.info("Declinação: {}", String.format("%.6f°%n", gm.getDeclination()));
  }

  public static double normalizarAzimute(double graus) {
    return (graus % 360.0 + 360.0) % 360.0;
  }

  public double getAzimuteVerdadeiro() {

    double phi1 = Math.toRadians(lat1);
    double phi2 = Math.toRadians(lat2);
    double deltaLon = Math.toRadians(lon2 - lon1);

    double y = Math.sin(deltaLon) * Math.cos(phi2);
    double x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLon);
    double azV = Math.toDegrees(Math.atan2(y, x));

    LOGGER.info("Azimute Verdadeiro: {}", String.format("%.3f°%n", azV));
    
    return Azimute.normalizarAzimute(azV);
  }

  public double getAzimuteMagnetico() {
    double azM = this.getAzimuteVerdadeiro() - gm.getDeclination();
    LOGGER.info("Azimute Magnético: {}", String.format("%.3f°%n", azM));
    return Azimute.normalizarAzimute(azM);
  }

}
