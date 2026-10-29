package br.com.wagnersoft.patagonia.util;

/*
 * License Statement from the NOAA
 * -------------------------------
 * The WMM source code is in the public domain and not licensed or
 * under copyright. The information and software may be used freely
 * by the public. As required by 17 U.S.C. 403, third parties producing
 * copyrighted works consisting predominantly of the material produced
 * by U.S. government agencies must provide notice with such work(s)
 * identifying the U.S. Government material incorporated and stating
 * that such material is not subject to copyright protection.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to calculate magnetic declination, magnetic field strength,
 * inclination etc. for any point on the earth.
 * 
 * <p>Adapted from the geomagc software and World Magnetic Model of the NOAA
 * Satellite and Information Service, National Geophysical Data Center</p>
 * 
 * <a href='http://www.ngdc.noaa.gov/geomag/WMM/DoDWMM.shtml'>World Magnetic Model - NOAA</a>
 *
 * @since 0.0.1
 * @version 0.0.1
 * @author © Deep Pradhan, 2017.
 */
public class Geomagnetism {

  /** Mean radius of IAU-66 ellipsoid, in km */
  private static final double IAU66_RADIUS = 6371.2;

  /** Semi-major axis of WGS-1984 ellipsoid, in km */
  private static final double WGS84_A = 6378.137;

  /** Semi-minor axis of WGS-1984 ellipsoid, in km */
  private static final double WGS84_B = 6356.7523142;

  /** The maximum number of degrees of the spherical harmonic model */
  private static final int MAX_DEG = 12;

  /** The arrays size in local code. */
  private static final int ARRAY_SIZE = MAX_DEG + 1;

  /** Geomagnetic declination (decimal degrees) [opposite of variation, positive Eastward/negative Westward] */
  private double declination = 0;

  /** Geomagnetic inclination/dip angle (degrees) [positive downward] */
  private double inclination = 0;

  /** Geomagnetic field intensity/strength (nano Teslas) */
  private double intensity = 0;

  /** Geomagnetic horizontal field intensity/strength (nano Teslas) */
  private double bh;

  /** Geomagnetic vertical field intensity/strength (nano Teslas) [positive downward] */
  private double bz;

  /** Geomagnetic North South (northerly component) field intensity/strength (nano Tesla) */
  private double bx;

  /** Geomagnetic East West (easterly component) field intensity/strength (nano Teslas) */
  private double by;

  /** The maximum order of spherical harmonic model */
  private int maxord;

  /** The Gauss coefficients of main geomagnetic model (nt) */
  private double c[][] = new double[ARRAY_SIZE][ARRAY_SIZE];

  /** The Gauss coefficients of secular geomagnetic model (nt/yr) */
  private double cd[][] = new double[ARRAY_SIZE][ARRAY_SIZE];

  /** The time adjusted geomagnetic gauss coefficients (nt) */
  private double tc[][] = new double[ARRAY_SIZE][ARRAY_SIZE];

  /** The theta derivative of p(n,m) (unnormalized) */
  private double dp[][] = new double[ARRAY_SIZE][ARRAY_SIZE];

  /** The Schmidt normalization factors */
  private double snorm[] = new double[169];

  /** The sine of (m*spherical coordinate longitude) */
  private double sp[] = new double[ARRAY_SIZE];

  /** The cosine of (m*spherical coordinate longitude) */
  private double cp[] = new double[ARRAY_SIZE];
  private double fn[] = new double[ARRAY_SIZE];
  private double fm[] = new double[ARRAY_SIZE];

  /** The associated Legendre polynomials for m = 1 (unnormalized) */
  private double pp[] = new double[ARRAY_SIZE];

  private double k[][] = new double[ARRAY_SIZE][ARRAY_SIZE];

  /** The variables otime (old time), oalt (old altitude),
   *  olat (old latitude), olon (old longitude), are used to
   *  store the values used from the previous calculation to
   *  save on calculation time if some inputs don't change
   */
  private double otime, oalt, olat, olon;

  /** The date in years, for the start of the valid time of the fit coefficients */
  private double epoch;

  /** The input string array which contains each line of input for the wmm.cof input file.
   *  <p>The columns in this file are as follows:  n,  m,  gnm,  hnm,  dgnm, dhnm. </p>
   */
  private final String [] WMM_COF;

  private double r, d, ca, sa, ct, st;

  /** Initialise the instance without calculations. */
  Geomagnetism() {

    // Load WMM.COF file
    WMM_COF = this.loadFile();

    // Initialize constants
    maxord = MAX_DEG;
    sp[0] = 0;
    cp[0] = snorm[0] = pp[0] = 1;
    dp[0][0] = 0;

    c[0][0] = 0;
    cd[0][0] = 0;

    epoch = Double.parseDouble(WMM_COF[0].trim().split("\\s+")[0]);

    String[] tokens;

    double gnm, hnm, dgnm, dhnm;
    for (int i = 1, m, n; i < WMM_COF.length; i++) {
      tokens = WMM_COF[i].trim().split("\\s+");
      n = Integer.parseInt(tokens[0]);
      m = Integer.parseInt(tokens[1]);
      gnm = Double.parseDouble(tokens[2]);
      hnm = Double.parseDouble(tokens[3]);
      dgnm = Double.parseDouble(tokens[4]);
      dhnm = Double.parseDouble(tokens[5]);
      if (m <= n) {
        c[m][n] = gnm;
        cd[m][n] = dgnm;
        if (m != 0) {
          c[n][m - 1] = hnm;
          cd[n][m - 1] = dhnm;
        }
      }			
    }
    // Convert schmidt normalized gauss coefficients to unnormalized
    snorm[0] = 1;
    double flnmj;
    for (int j, n = 1; n <= maxord; n++) {
      snorm[n] = snorm[n - 1] * (2 * n - 1) / n;
      j = 2;
      for (int m = 0, d1 = 1, d2 = (n - m + d1) / d1; d2 > 0; d2--, m += d1) {
        k[m][n] = (double) (((n - 1) * (n - 1)) - (m * m)) / (double) ((2 * n - 1) * (2 * n - 3));
        if (m > 0) {
          flnmj = ((n - m + 1) * j) / (double) (n + m);
          snorm[n + m * 13] = snorm[n + (m -1) * 13] * Math.sqrt(flnmj);
          j = 1;
          c[n][m - 1] = snorm[n + m * 13] * c[n][m - 1];
          cd[n][m - 1] = snorm[n + m * 13] * cd[n][m - 1];
        }
        c[m][n] = snorm[n + m * 13] * c[m][n];
        cd[m][n] = snorm[n + m * 13] * cd[m][n];
      }
      fn[n] = (n + 1);
      fm[n] = n;
    }
    k[1][1] = 0;
    fm[0] = 0;
    otime = oalt = olat = olon = -1000;
  }

  /** Initialise the instance and calculate for given location, altitude and date.
   *	@param longitude	Longitude in decimal degrees
   *	@param latitude		Latitude in decimal degrees
   *	@param altitude		Altitude in metres (with respect to WGS-1984 ellipsoid)
   *	@param calendar		Calendar for date of calculation
   */
  Geomagnetism(double longitude, double latitude, double altitude, LocalDate date) {
    this();
    calculate(longitude, latitude, altitude, date);
  }

  /** Initialise the instance and calculate for given location, altitude and current date.
   *	@param longitude	Longitude in decimal degrees
   *	@param latitude		Latitude in decimal degrees
   *	@param altitude		Altitude in metres (with respect to WGS-1984 ellipsoid)
   */
  Geomagnetism(double longitude, double latitude, double altitude) {
    this();
    calculate(longitude, latitude, altitude);
  }

  /** Initialise the instance and calculate for given location, zero altitude and current date.
   *	@param longitude	Longitude in decimal degrees
   *	@param latitude		Latitude in decimal degrees
   */
  Geomagnetism(double longitude, double latitude) {
    this();
    calculate(longitude, latitude);
  }

  /** Calculate for given location, altitude and date.
   *	@param longitude	Longitude in decimal degrees
   *	@param latitude		Latitude in decimal degrees
   *	@param altitude		Altitude in metres (with respect to WGS-1984 ellipsoid)
   *	@param date		    Date of calculation
   */
  void calculate(double longitude, double latitude, double altitude, LocalDate date) {
    double rlon = Math.toRadians(longitude);
    double rlat = Math.toRadians(latitude);
    double altitudeKm = Double.isNaN(altitude) ? 0 : altitude / 1000;
    double srlon = Math.sin(rlon);
    double srlat = Math.sin(rlat);
    double crlon = Math.cos(rlon);
    double crlat = Math.cos(rlat);
    double srlat2 = srlat * srlat;
    double crlat2 = crlat * crlat;
    double a2 = WGS84_A * WGS84_A;
    double b2 = WGS84_B * WGS84_B;
    double c2 = a2 - b2;
    double a4 = a2 * a2;
    double b4 = b2 * b2;
    double c4 = a4 - b4;

    // Define years between now and file data
    double yearFraction = date.getYear() + ((double) date.getDayOfYear() - 1) / date.lengthOfYear();
    double dt = yearFraction - epoch;
    
    sp[1] = srlon;
    cp[1] = crlon;

    // Convert from geodetic coords. to spherical coords.
    if (altitudeKm != oalt || latitude != olat) {
      double q = Math.sqrt(a2 - c2 * srlat2),
          q1 = altitudeKm * q,
          q2 = ((q1 + a2) / (q1 + b2)) * ((q1 + a2) / (q1 + b2)),
          r2 = ((altitudeKm * altitudeKm) + 2 * q1 + (a4 - c4 * srlat2) / (q * q));
      ct = srlat / Math.sqrt(q2 * crlat2 + srlat2);
      st = Math.sqrt(1 - (ct * ct));
      r = Math.sqrt(r2);
      d = Math.sqrt(a2 * crlat2 + b2 * srlat2);
      ca = (altitudeKm + d) / r;
      sa = c2 * crlat * srlat / (r * d);
    }
    if (longitude != olon) {
      for (int m = 2; m <= maxord; m++) {
        sp[m] = sp[1] * cp[m - 1] + cp[1] * sp[m - 1];
        cp[m] = cp[1] * cp[m - 1] - sp[1] * sp[m - 1];
      }
    }
    double aor = IAU66_RADIUS / r,
        ar = aor * aor,
        br = 0, bt = 0, bp = 0, bpp = 0,
        par, parp, temp1, temp2;

    for (int n = 1; n <= maxord; n++) {
      ar = ar * aor;
      for (int m = 0, d3 = 1, d4 = (n + m + d3) / d3; d4 > 0; d4--, m += d3) {

        // Compute unnormalized associated legendre polynomials and derivatives via recursion relations
        if (altitudeKm != oalt || latitude != olat) {
          if (n == m) {
            snorm[n + m * 13] = st * snorm[n - 1 + (m - 1) * 13];				
            dp[m][n] = st * dp[m - 1][n - 1]+ ct * snorm[n - 1 + (m - 1) * 13];
          }
          if (n == 1 && m == 0) {
            snorm[n + m * 13] = ct * snorm[n - 1 + m * 13];
            dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13];
          }
          if (n > 1 && n != m) {
            if (m > n - 2) 
              snorm[n - 2 + m * 13] = 0;
            if (m > n - 2)
              dp[m][n - 2] = 0;
            snorm[n + m * 13] = ct * snorm[n - 1 + m * 13] - k[m][n] * snorm[n - 2 + m * 13];
            dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13] - k[m][n] * dp[m][n - 2];
          }
        }

        // Time adjust the gauss coefficients
        if (yearFraction != otime) {
          tc[m][n] = c[m][n] + dt * cd[m][n];

          if (m != 0)
            tc[n][m - 1] = c[n][m - 1]+ dt * cd[n][m - 1];
        }

        // Accumulate terms of the spherical harmonic expansions
        par = ar * snorm[ n + m * 13];
        if (m == 0) {
          temp1 = tc[m][n] * cp[m];
          temp2 = tc[m][n] * sp[m];
        } else {
          temp1 = tc[m][n] * cp[m] + tc[n][m - 1] * sp[m];
          temp2 = tc[m][n] * sp[m] - tc[n][m - 1] * cp[m];
        }

        bt = bt - ar * temp1 * dp[m][n];
        bp += (fm[m] * temp2 * par);
        br += (fn[n] * temp1 * par);

        // Special case: north/south geographic poles
        if (st == 0 && m == 1) {
          if (n == 1)
            pp[n] = pp[n - 1];
          else 
            pp[n] = ct * pp[n - 1] - k[m][n] * pp[n - 2];
          parp = ar * pp[n];
          bpp += (fm[m] * temp2 * parp);
        }
      }
    }

    if (st == 0)
      bp = bpp;
    else 
      bp /= st;

    // Rotate magnetic vector components from spherical to geodetic coordinates
    // bx must be the east-west field component
    // by must be the north-south field component
    // bz must be the vertical field component.
    bx = -bt * ca - br * sa;
    by = bp;
    bz = bt * sa - br * ca;

    // Compute declination (dec), inclination (dip) and total intensity (ti)
    bh = Math.sqrt((bx * bx) + (by * by));
    intensity = Math.sqrt((bh * bh) + (bz * bz));
    //	Calculate the declination.
    declination = Math.toDegrees(Math.atan2(by, bx));
    inclination = Math.toDegrees(Math.atan2(bz, bh));

    otime = yearFraction;
    oalt = altitudeKm;
    olat = latitude;
    olon = longitude;
  }

  /** Calculate for given location, altitude and current date
   *	@param longitude	Longitude in decimal degrees
   *	@param latitude		Latitude in decimal degrees
   *	@param altitude		Altitude in metres (with respect to WGS-1984 ellipsoid)
   */
  void calculate(double longitude, double latitude, double altitude) {
    calculate(longitude, latitude, altitude, LocalDate.now());
  }

  /** Calculate for given location, zero altitude and current date
   *	@param longitude	Longitude in decimal degrees
   *	@param latitude		Latitude in decimal degrees
   */
  void calculate(double longitude, double latitude) {
    calculate(longitude, latitude, 0);
  }

  /** Get declination.
   *  @return Geomagnetic declination (degrees) [opposite of variation, positive Eastward/negative Westward]
   */
  double getDeclination() {
    return declination;
  }

  /** Get inclination.
   *  @return Geomagnetic inclination/dip angle (degrees) [positive downward]
   */
  double getInclination() {
    return inclination;
  }

  /** Get intensity.
   *  @return Geomagnetic field intensity/strength (nano Teslas)
   */
  double getIntensity() {
    return intensity;
  }

  /** Get Horizontal Intensity.
   *  @return Geomagnetic horizontal field intensity/strength (nano Teslas)
   */
  double getHorizontalIntensity() {
    return bh;
  }

  /** Get Vertical Intensity.
   *  @return Geomagnetic vertical field intensity/strength (nano Teslas) [positive downward]
   */
  double getVerticalIntensity() {
    return bz;
  }

  /** Get North Intensity.
   *  @return Geomagnetic North South (northerly component) field intensity/strength (nano Tesla)
   */
  double getNorthIntensity() {
    return bx;
  }

  /** Get East Intensity.
   *  @return Geomagnetic East West (easterly component) field intensity/strength (nano Teslas)
   */
  double getEastIntensity() {
    return by;
  }

  /** Load file WMMM.COF from resources into a string array. */
  private String[] loadFile(){

    try (InputStream is = getClass().getClassLoader().getResourceAsStream("WMM.COF")) {

      if (is == null) {
        throw new IllegalStateException("File WMM.COF not found in classpath");
      }

      List<String> lines = new ArrayList<>();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty()) {
            continue;
          }
          if (line.startsWith("999999")) {
            break;
          }          
          if (!line.trim().isEmpty()) {
            lines.add(line);
          }
        }
      }

      return lines.toArray(new String[0]);

    } catch (IOException e) {
      throw new RuntimeException("Error loading file WMM.COF", e);
    }        
  }

  public static void main(String[] args) {

    Geomagnetism gm = new Geomagnetism(-47.061320, -23.166864);

    System.out.println("*************** Geomagnetism ***************");
    System.out.println("Declination: " + gm.getDeclination() + "°");
    System.out.println("Inclination: " + gm.getInclination() + "°");
    System.out.println("Total Intensity: " + gm.getIntensity() + " nT");
    System.out.println("North Component X: " + gm.getNorthIntensity() + " nT");
    System.out.println("East Component Y: " + gm.getEastIntensity() + " nT");
    System.out.println("Vertical Component Z: " + gm.getVerticalIntensity() + " nT");
    System.out.println("********************************************");
  }

}
