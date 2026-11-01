package br.com.wagnersoft.patagonia.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.wagnersoft.patagonia.geoinfo.Azimute;
import br.com.wagnersoft.patagonia.geoinfo.CoordenadaGeografica;

/** DistanciaController.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@RestController
@RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
public class DistanciaController {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DistanciaController.class);

  public record DistanciaDTO(Double distancia, Double azv, Double azm) { };

	@GetMapping("/distancia")
	public ResponseEntity<DistanciaDTO> obterDistancia(@RequestParam Double lat1, @RequestParam Double lon1, @RequestParam Double alt1, @RequestParam Double lat2, @RequestParam Double lon2, @RequestParam Double alt2) {

    LOGGER.info("Lat1: {} Lon1: {} Lat2: {} Lon2: {}", lat1, lon1, lat2, lon2);
		//Double result = new Coordenada(lat1, lon1, alt1).haversineDistance(new Coordenada(lat2, lon2, alt2));
		Double distancia = new CoordenadaGeografica(lat1, lon1, alt1).vincentyDistance(new CoordenadaGeografica(lat2, lon2, alt2));
    Azimute az = new Azimute(lat1, lon1, lat2, lon2);
    Double azv = az.getAzimuteVerdadeiro();
    Double azm = az.getAzimuteMagnetico();
		DistanciaDTO result = new DistanciaDTO(distancia, azv, azm);
		LOGGER.debug("{}", result);
		return ResponseEntity.ok(result);
	}

  @GetMapping("/azv")
  public ResponseEntity<Double> obterAzimuteV(@RequestParam Double lat1, @RequestParam Double lon1, @RequestParam Double alt1, @RequestParam Double lat2, @RequestParam Double lon2, @RequestParam Double alt2) {

    Double result = new Azimute(lat1, lon1, lat2, lon2).getAzimuteVerdadeiro();

    LOGGER.debug("{}", result);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/azm")
  public ResponseEntity<Double> obterAzimuteM(@RequestParam Double lat1, @RequestParam Double lon1, @RequestParam Double alt1, @RequestParam Double lat2, @RequestParam Double lon2, @RequestParam Double alt2) {

    Double result = new Azimute(lat1, lon1, lat2, lon2).getAzimuteMagnetico();

    LOGGER.debug("{}", result);

    return ResponseEntity.ok(result);
  }
  
}
