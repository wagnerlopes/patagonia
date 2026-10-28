package br.com.wagnersoft.patagonia.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.wagnersoft.patagonia.util.Coordenada;

@RestController
@RequestMapping(value = "/distancia", produces = MediaType.APPLICATION_JSON_VALUE)
public class DistanciaController {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DistanciaController.class);

	@GetMapping
	public ResponseEntity<Double> obterDistancia(@RequestParam Double lat1, @RequestParam Double lon1, @RequestParam Double alt1, @RequestParam Double lat2, @RequestParam Double lon2, @RequestParam Double alt2) {

		//Double result = new Coordenada(lat1, lon1, alt1).haversineDistance(new Coordenada(lat2, lon2, alt2));
		
		Double result = new Coordenada(lat1, lon1, alt1).vincentyDistance(new Coordenada(lat2, lon2, alt2));

		LOGGER.debug("{}", result);

		return ResponseEntity.ok(result);
	}

}
