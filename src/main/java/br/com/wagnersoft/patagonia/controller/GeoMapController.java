package br.com.wagnersoft.patagonia.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.wagnersoft.patagonia.service.PibService;
import br.com.wagnersoft.patagonia.service.PopulacaoService;

/** 
 * GeoController.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Controller
@RequestMapping("/geomap")
public class GeoMapController {

  private static final Logger logger = LoggerFactory.getLogger(GeoMapController.class);

  public record MunInfoDTO(Long pop, Double pib) { };

  @Autowired
  private PopulacaoService popSvc;
  
  @Autowired
  private PibService pibSvc;
  
  @GetMapping
  public String geoMap(Model model) {
    logger.info("+++ GeoMap +++");
    model.addAttribute("menu", "geomap");
    return "geomap";
  }

  @GetMapping("/muninfo")
  public ResponseEntity<MunInfoDTO> munInfo(@RequestParam Long id, Model model) {
    Long pop = popSvc.obterPopulacao(id);
    Double pib = pibSvc.obterPib(id);
    MunInfoDTO info = new MunInfoDTO(pop, pib);
    logger.debug("{}", info);
    return ResponseEntity.ok(info);
  }
  
}
