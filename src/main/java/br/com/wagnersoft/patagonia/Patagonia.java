package br.com.wagnersoft.patagonia;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Patagonia starting application. 
 * 
 * @since 1.0
 * @version 1.0
 * @author Wagner Lopes
 */
@Controller
@SpringBootApplication
public class Patagonia {

  private static final Logger logger = LoggerFactory.getLogger(Patagonia.class);

  @Value("${app.usr}")
  private String usr;
  
  @GetMapping("/")
  public String index(HttpSession session, Model model) throws IOException {
    logger.info("+++ Index +++");
    session.setMaxInactiveInterval(0);
    session.setAttribute("usr", usr);
    return "index";
  }

  
  public static void main(String[] args) {
    SpringApplication.run(Patagonia.class, args);
  }

}
