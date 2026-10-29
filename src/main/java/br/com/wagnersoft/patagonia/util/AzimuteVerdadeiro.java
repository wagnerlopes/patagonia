package br.com.wagnersoft.patagonia.util;

public class AzimuteVerdadeiro {

	public static double calcularAzimute(double lat1, double lon1, double lat2, double lon2) {

		// Converter para radianos
		double phi1 = Math.toRadians(lat1);
		double phi2 = Math.toRadians(lat2);
		double deltaLon = Math.toRadians(lon2 - lon1);

		double y = Math.sin(deltaLon) * Math.cos(phi2);
		double x = Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLon);
		double azimute = Math.toDegrees(Math.atan2(y, x));

		// Normaliza para 0-360°
		azimute = (azimute + 360.0) % 360.0;

		return azimute;
	}

	public static void main(String[] args) {

		double lat1 = -23.166864;
		double lon1 = -47.061320;

		double lat2 = -23.2310352;
		double lon2 = -46.9986411;

		double azimute = calcularAzimute(lat1, lon1, lat2, lon2);

		System.out.printf("Azimute verdadeiro: %.3f°%n", azimute);
	}

}
