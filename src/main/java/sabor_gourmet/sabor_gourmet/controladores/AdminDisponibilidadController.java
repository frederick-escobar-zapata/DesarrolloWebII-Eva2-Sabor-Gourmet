package sabor_gourmet.sabor_gourmet.controladores; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;   
import org.springframework.ui.Model;          
import org.springframework.web.bind.annotation.*;
import sabor_gourmet.sabor_gourmet.modelos.Mesas;
import sabor_gourmet.sabor_gourmet.modelos.TipoMenu;
import sabor_gourmet.sabor_gourmet.servicios.ReservaServicio;
import java.time.LocalDate;
import java.util.Map;

@Controller                                       
@RequestMapping("/admin/disponibilidad")         // prefijo para todas las rutas de este controlador
public class AdminDisponibilidadController {

    @Autowired                                   
    private ReservaServicio reservaServicio;

    @GetMapping   // este getmapping es para mostrar el formulario de disponibilidad en GET /admin/disponibilidad
    public String mostrarFormulario(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,                    

            @RequestParam(required = false)
            String tipoMenu,                    // parametro opcional 'tipoMenu' (DESAYUNO,ALMUERZO, ONCE, CENA)

            Model model) {                      // Model para pasar datos a la vista Thymeleaf

        // aca guardamos en el modelo la fecha y el tipoMenu que vinieron, para rellenar el formulario
        model.addAttribute("fecha", fecha);
        model.addAttribute("tipoMenu", tipoMenu);

        // aca si el usuario ya selecciono fecha y tipo de menu (no vacio), calculamos la disponibilidad
        if (fecha != null && tipoMenu != null && !tipoMenu.isBlank()) {
            // aca convertimos el texto (desayuno, almuerzo, once, cena) a enum TipoMenu (DESAYUNO, ALMUERZO, etc)
            TipoMenu tipoMenuEnum = TipoMenu.valueOf(tipoMenu.toUpperCase());

            // aca pedimos al servicio un mapa Mesa -> Boolean (true si ocupada, false si disponible)
            Map<Mesas, Boolean> disponibilidad =
                    reservaServicio.obtenerDisponibilidadPorFechaYTipo(fecha, tipoMenuEnum);

            // aca metemos el mapa en el modelo para que la vista lo pinte
            model.addAttribute("disponibilidad", disponibilidad);
        }

        // aca devolvemos el nombre de la plantilla Thymeleaf a usar:
        // src/main/resources/templates/admin_disponibilidad.html
        return "admin_disponibilidad";
    }
}
