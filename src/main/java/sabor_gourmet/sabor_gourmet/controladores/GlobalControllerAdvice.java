package sabor_gourmet.sabor_gourmet.controladores;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalControllerAdvice {

    // añade la ruta actual al modelo para condicionales en las plantillas
    @ModelAttribute
    public void addRequestUri(HttpServletRequest request, Model model) {
        if (request != null) {
            model.addAttribute("requestUri", request.getRequestURI());
        }
    }
}
