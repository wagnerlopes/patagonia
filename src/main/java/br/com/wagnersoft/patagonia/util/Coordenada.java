package br.com.wagnersoft.patagonia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public class Coordenada {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Coordenada.class);

	// Parâmetros do elipsóide WGS84
	private static final double ACHATAMENTO = 1.0 / 298.257223563;
	
	private static final double RAIO_TERRA = 6378137.0; // semi-eixo equatorial 
	
	private static final double RAIO_POLAR = (1.0 - ACHATAMENTO) * RAIO_TERRA;  // semi-eixo polar

	private static final double TO_RADIANS = Math.PI / 180.0;
	
	@DecimalMin(value = "-90.0", message = "A latitude deve ser maior ou igual a -90.0")
	@DecimalMax(value = "90.0", message = "A latitude deve ser menor ou igual a 90.0")
	private double latitude;

	@DecimalMin(value = "-180.0", message = "A longitude deve ser maior ou igual a -180.0")
	@DecimalMax(value = "180.0", message = "A longitude deve ser menor ou igual a 180.0")
	private double longitude;

	private double altitude;   // metros

	public Coordenada(double latitude, double Longitude, double altitude) {
		this.latitude = latitude;
		this.longitude = Longitude;
		this.altitude = altitude;
	}

	@Override
	public String toString() {
		return String.format("Lat: %.6f, Lon: %.6f, Alt: %.2f m", latitude, longitude, altitude);
	}

	/**
	 * Calcula a distância entre esta coordenada e outra usando a fórmula de Haversine.
	 * @param outra Coordenada destino
	 * @return distância em metros
	 */
	public double haversineDistance(Coordenada outra) {

		double lat1 = this.latitude * TO_RADIANS;
		double lon1 = this.longitude * TO_RADIANS;
		double lat2 = outra.latitude * TO_RADIANS;
		double lon2 = outra.longitude * TO_RADIANS;
		
		double dLat = lat2 - lat1;
		double dLon = lon2 - lon1;

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
  		           Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double distanciaSuperficie = RAIO_TERRA * c;

		// Considera diferença de altitude
		double dAlt = outra.altitude - this.altitude;

		double distancia = Math.sqrt(distanciaSuperficie * distanciaSuperficie + dAlt * dAlt);

		LOGGER.info("Distância (Haversie): {}", distancia);
		return distancia;
	}

	/**
	 * Calcula a distância entre esta coordenada e outra usando a fórmula de Vincenty.
	 * @param outra Coordenada destino
	 * @return distância em metros
	 */
	public double vincentyDistance(Coordenada outra) {

		double lat1 = this.latitude * TO_RADIANS;
		double lon1 = this.longitude * TO_RADIANS;
		double lat2 = outra.latitude * TO_RADIANS;
		double lon2 = outra.longitude * TO_RADIANS;

		LOGGER.info("Lat1 {}  Lon1 {} -  Lat2 {} Lon2 {}", lat1, lon1, lat2, lon2);

		double L = lon2 - lon1;
		
		double U1 = Math.atan((1 - ACHATAMENTO) * Math.tan(lat1));
		double U2 = Math.atan((1 - ACHATAMENTO) * Math.tan(lat2));

		double sinU1 = Math.sin(U1);
		double cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2);
		double cosU2 = Math.cos(U2);

		double lambda = L, lambdaPrev;
		double sinSigma, cosSigma, sigma, cosSqAlpha, cos2SigmaM;
		int iterLimit = 100;

		do {
			double sinLambda = Math.sin(lambda);
			double cosLambda = Math.cos(lambda);
			
			sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
					(cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));

			if (sinSigma == 0) return 0; // pontos coincidentes

			cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha * sinAlpha;
			cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
			
			if (Double.isNaN(cos2SigmaM)) cos2SigmaM = 0; // equador

			double C = ACHATAMENTO / 16 * cosSqAlpha * (4 + ACHATAMENTO * (4 - 3 * cosSqAlpha));

			lambdaPrev = lambda;
			lambda = L + (1 - C) * ACHATAMENTO * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));

		} while (Math.abs(lambda - lambdaPrev) > 1e-12 && --iterLimit > 0);

		double uSq = cosSqAlpha * (RAIO_TERRA * RAIO_TERRA - RAIO_POLAR * RAIO_POLAR) / (RAIO_POLAR * RAIO_POLAR);
		double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
		double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
		double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 *
				(cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

		double distancia = RAIO_POLAR * A * (sigma - deltaSigma);
		
		LOGGER.info("Distância (Vicenty): {}", distancia);
		return distancia;
	}

}
