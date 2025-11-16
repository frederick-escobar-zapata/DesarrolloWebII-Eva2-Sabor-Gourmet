package sabor_gourmet.sabor_gourmet.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sabor_gourmet.sabor_gourmet.servicios.ReservaServicio;
import sabor_gourmet.sabor_gourmet.modelos.Mesas;
import sabor_gourmet.sabor_gourmet.modelos.Reserva;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/mesas")
public class MesasController {

    @Autowired
    private ReservaServicio reservaservicio;
    


    @GetMapping //este getmapping es para listar todas las mesas, admin/mesas
    public String listarTodasLasMesas(Model model) {
        model.addAttribute("mesas", reservaservicio.obtenerTodasLasMesas());
        return "admin_mesas";
    }

    @GetMapping("/nueva") //este getmapping es para mostrar el formulario de nueva mesa
    public String mostrarFormularioNuevaMesa(Model model) {        
        Mesas mesa = new Mesas();
        mesa.setDisponible(true);         
        model.addAttribute("mesa", mesa);
        return "admin_mesas_form";
    }

    @PostMapping("/guardar") //este postmapping es para guardar la nueva mesa o la mesa editada
    public String guardarMesa(@ModelAttribute Mesas mesa) {       
        reservaservicio.guardarMesa(mesa);
        return "redirect:/admin/mesas";
    }

    @GetMapping("/editar/{id}")//este getmapping es para mostrar el formulario de editar mesa obteniendo la mesa por id
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Mesas mesa = reservaservicio.obtenerMesaPorId(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
        model.addAttribute("mesa", mesa);
        return "admin_mesas_form";
    }

    @PostMapping("/cambiar-estado/{id}")//este postmapping es para cambiar el estado de la mesa (disponible/no disponible)
    public String cambiarEstado(@PathVariable Long id) {
        reservaservicio.obtenerMesaPorId(id).ifPresent(mesa -> {
            mesa.setDisponible(!mesa.isDisponible());
            reservaservicio.guardarMesa(mesa);
        });
        return "redirect:/admin/mesas";
    }

    @PostMapping("/eliminar/{id}")//este postmapping es para eliminar una mesa por id
    public String eliminarMesa(@PathVariable Long id) {
        reservaservicio.eliminarMesa(id);
        return "redirect:/admin/mesas";
    }

    // aca cuando presiono el boton "Reservas" o cuando redirijo desde eliminar/editar reserva,
    // muestro todas las reservas de esa mesa
    @GetMapping("/eliminarReserva/{id}")
    public String verReservasDeMesa(@PathVariable("id") Long mesaId, Model model) {
        Optional<Mesas> mesaOpt = reservaservicio.obtenerMesaPorId(mesaId);
        if (mesaOpt.isEmpty()) {
            model.addAttribute("mensaje", "La mesa seleccionada no existe.");
            return "admin_mesas";
        }

        List<Reserva> reservas = reservaservicio.obtenerReservasPorMesa(mesaId);

        model.addAttribute("mesa", mesaOpt.get());
        model.addAttribute("reservas", reservas);

        return "admin_mesa_reservas";
    }
}
