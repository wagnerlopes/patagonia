package br.com.wagnersoft.patagonia;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeoController {

    @GetMapping("/geo")
    public String showBanner(Model model) {
        model.addAttribute("geo", "xxx");
        return "geo";
    }

}
