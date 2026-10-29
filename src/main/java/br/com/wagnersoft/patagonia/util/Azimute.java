package br.com.wagnersoft.patagonia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Azimute {

  private static final Logger LOGGER = LoggerFactory.getLogger(Azimute.class);

  private double lat1;

  private double lon1;

  private double lat2;

  private double lon2;

  private Geomagnetismo gm;

  public Azimute(double lat1, double lon1, double lat2, double lon2) {
    this.lat1 = lat1;
    this.lon1 = lon1;
    this.lat2 = lat2;
    this.lon2 = lon2;
    this.gm = new Geomagnetismo(lat1, lon1);
    LOGGER.debug("Declinação: {}", String.format("%.6f°", gm.getDeclination()));
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

    LOGGER.debug("Azimute Verdadeiro (RAW): {}", String.format("%.3f°", azV));

    return Azimute.normalizarAzimute(azV);
  }

  public double getAzimuteMagnetico() {
    double azM = this.getAzimuteVerdadeiro() - gm.getDeclination();
    LOGGER.debug("Azimute Magnético (RAW): {}", String.format("%.3f°", azM));
    return Azimute.normalizarAzimute(azM);
  }

  public String toString() {
    return String.format("Coor1: %.6f, %.6f%nCoor2: %.6f, %.6f%nDM: %.6f°%nAzV: %.1f°%nAzM: %.1f°%n", lat1, lon1, lat2, lon2, gm.getDeclination(), this.getAzimuteVerdadeiro(), this.getAzimuteMagnetico());
  }

  public static void main(String[] args) {
    Azimute az = new Azimute(-23.166864, -47.061320, -15.79408736, -47.88790548);
    System.out.println(az);
  }

}
