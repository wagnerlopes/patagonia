package br.com.wagnersoft.patagonia;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class GeoController {

  private static final Logger logger = LoggerFactory.getLogger(GeoController.class);

  @Value("${app.usr}")
  private String usr;

  @GetMapping("/")
  public String index(HttpSession session, Model model) throws IOException {
    logger.info("+++ Index +++");
    session.setMaxInactiveInterval(0);
    session.setAttribute("usr", usr);
    return "index";
  }

  @GetMapping("/geo")
  public String showBanner(Model model) {
    model.addAttribute("geo", "xxx");
    return "geo";
  }

}
