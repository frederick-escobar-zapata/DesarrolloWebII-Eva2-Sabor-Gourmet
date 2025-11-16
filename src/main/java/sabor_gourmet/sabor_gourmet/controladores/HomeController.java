package sabor_gourmet.sabor_gourmet.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import sabor_gourmet.sabor_gourmet.repositorios.MesasRepository;

@Controller
public class HomeController {

    @Autowired
    private MesasRepository mesasRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("mesas", mesasRepository.findAll());
        return "index";
    }

    @GetMapping("/reservas")
    public String reservas(Model model) {
        model.addAttribute("mesas", mesasRepository.findAll());
        return "index"; 
    }
}
