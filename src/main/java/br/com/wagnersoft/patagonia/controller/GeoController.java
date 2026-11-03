package br.com.wagnersoft.patagonia.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** 
 * GeoController.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Controller
public class GeoController {

  private static final Logger logger = LoggerFactory.getLogger(GeoController.class);

  @GetMapping("/geo")
  public String geoMap(Model model) {
    logger.info("+++ Geo +++");
    model.addAttribute("menu", "geo");
    return "geo";
  }

}
