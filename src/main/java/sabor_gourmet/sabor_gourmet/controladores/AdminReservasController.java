package sabor_gourmet.sabor_gourmet.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sabor_gourmet.sabor_gourmet.modelos.Reserva;
import sabor_gourmet.sabor_gourmet.repositorios.ReservaRepository;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/admin/reservas")
public class AdminReservasController {

    @Autowired
    private ReservaRepository reservaRepository;

    // GET /admin/reservas?fecha=yyyy-MM-dd  -> lista reservas por fecha (por defecto hoy)
    @GetMapping
    public String listarPorFecha(@RequestParam(value = "fecha", required = false) String fechaStr, Model model) {
        java.time.LocalDate fecha;
        try {
            fecha = (fechaStr == null || fechaStr.isBlank()) ? java.time.LocalDate.now() : java.time.LocalDate.parse(fechaStr);
        } catch (Exception e) {
            fecha = java.time.LocalDate.now();
        }

        java.util.List<Reserva> reservas = reservaRepository.findByFecha(fecha);
        model.addAttribute("reservas", reservas);
        model.addAttribute("fechaSeleccionada", fecha);
        return "admin_reservas";
    }

    // GET /admin/reservas/editar/{id}  -> muestra formulario para editar la reserva
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("mensaje", "La reserva no existe.");
            return "redirect:/admin/mesas";
        }
        Reserva reserva = opt.get();
        if (reserva.getFecha() != null && reserva.getFecha().isBefore(LocalDate.now())) {
            reserva.setFecha(LocalDate.now());
        }


        model.addAttribute("reserva", reserva);
        return "admin_reserva_editar";
    }

    // POST /admin/reservas/editar/{id}  -> guarda cambios
    @PostMapping("/editar/{id}")
    public String guardarEdicion(@PathVariable("id") Long id,
                                 @ModelAttribute("reserva") Reserva reservaForm,
                                 Model model) {

        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("mensaje", "La reserva no existe.");
            return "redirect:/admin/mesas";
        }

        // validacion: no permitir guardar fecha anterior a hoy
        if (reservaForm.getFecha() != null && reservaForm.getFecha().isBefore(LocalDate.now())) {
            model.addAttribute("reserva", opt.get());
            model.addAttribute("mensajeError", "La fecha no puede ser anterior a hoy.");
            return "admin_reserva_editar";
        }

        Reserva reservaOriginal = opt.get();
        reservaOriginal.setFecha(reservaForm.getFecha());
        reservaOriginal.setTipoMenu(reservaForm.getTipoMenu());
        reservaOriginal.setCantidadPersonas(reservaForm.getCantidadPersonas());
        // mesa y cliente no se tocan aqui

        reservaRepository.save(reservaOriginal);

        Long mesaId = reservaOriginal.getMesa().getId();
        return "redirect:/admin/mesas/eliminarReserva/" + mesaId;
    }

    // POST /admin/reservas/eliminar/{id}  -> elimina la reserva y vuelve a la lista de reservas de esa mesa
    @PostMapping("/eliminar/{id}")
    public String eliminarReserva(@PathVariable("id") Long id, Model model) {
        Optional<Reserva> opt = reservaRepository.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("mensaje", "La reserva no existe.");
            return "redirect:/admin/mesas";
        }

        Reserva reserva = opt.get();
        Long mesaId = reserva.getMesa().getId();

        reservaRepository.deleteById(id);

        return "redirect:/admin/mesas/eliminarReserva/" + mesaId;
    }
}

