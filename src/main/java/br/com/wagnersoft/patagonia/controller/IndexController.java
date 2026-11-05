package br.com.wagnersoft.patagonia.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.wagnersoft.patagonia.service.MunicipioService;
import br.com.wagnersoft.patagonia.service.PopulacaoService;
import jakarta.servlet.http.HttpSession;

/** 
 * IndexController.
 * 
 * @since 0.0.1
 * @version 0.0.1
 * @author Wagner Lopes
 */
@Controller
public class IndexController {

  private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

  @Value("${app.usr}")
  private String usr;

  @Autowired
  private MunicipioService munSvc;

  @Autowired
  private PopulacaoService popSvc;
  
  @ModelAttribute("allMunicipios")
  public Map<Long, String> listBeneficiarios() {
    return munSvc.mapAll();
  }

  @ModelAttribute("usr")
  public String logUsr() {
    return usr;
  }
  
  @GetMapping("/")
  public String index(HttpSession session, Model model) throws IOException {
    logger.info("+++ Index +++");
    session.setMaxInactiveInterval(0);
    session.setAttribute("usr", usr);
    model.addAttribute("menu", "index");
    return "index";
  }

  @GetMapping("/municipio")
  public String municipio(Long id, Model model) {
    model.addAttribute("municipio", munSvc.findById(id));
    model.addAttribute("populacao", popSvc.obterPopulacao(id));
    return "index";
  }
  
}
