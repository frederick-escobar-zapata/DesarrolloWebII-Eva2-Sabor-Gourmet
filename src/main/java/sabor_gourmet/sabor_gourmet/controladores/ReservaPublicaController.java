package sabor_gourmet.sabor_gourmet.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sabor_gourmet.sabor_gourmet.modelos.Mesas;
import sabor_gourmet.sabor_gourmet.modelos.Reserva;
import sabor_gourmet.sabor_gourmet.modelos.TipoMenu;
import sabor_gourmet.sabor_gourmet.servicios.ReservaServicio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller // aca tenemos el controlador para el flujo publico (pagina index)
public class ReservaPublicaController {

    @Autowired
    private ReservaServicio reservaServicio; // aca tenemos el servicio que hace toda la pega de logica de negocio
        @Autowired
        private sabor_gourmet.sabor_gourmet.repositorios.MesasRepository mesasRepository;
        @Autowired
        private sabor_gourmet.sabor_gourmet.repositorios.ReservaRepository reservaRepository;

    @GetMapping("/reservar")
    // aca si alguien entra a /reservar por GET, lo mandamos de vuelta al home
    public String redirigirAIndex() {
        return "redirect:/";
    }

    @PostMapping("/reservar")
    // aqui llega el formulario de index para buscar disponibilidad
    public String buscarDisponibilidad(
            @RequestParam("fechaReserva")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaReserva,           // fecha que escogio el usuario

            @RequestParam("cantidadPersonas")
            int cantidadPersonas,             // cuantas personas vienen

            @RequestParam("tipoMenu")
            String tipoMenu,                  // tipo de comida (desayuno/almuerzo/once/cena en minuscula)

            Model model) {
        //aca agregamos un debug en la consola para chequear los parametros que llegan
        System.out.println("[POST /reservar] fechaReserva=" + fechaReserva +
                ", cantidadPersonas=" + cantidadPersonas +
                ", tipoMenu=" + tipoMenu);

        // aca pasamos de string en minuscula a enum en mayuscula
        TipoMenu tipoMenuEnum = TipoMenu.valueOf(tipoMenu.toUpperCase());

        // aca le pedimos al servicio las mesas candidatas (capacidad suficiente y no reservadas ese dia/tipo)
        List<Mesas> mesasCandidatas =
                reservaServicio.buscarMesasCandidatas(fechaReserva, tipoMenuEnum, cantidadPersonas);
        //aca agregamos un debug en la consola para chequear las mesas candidatas encontradas, por que tenia problemas para guardar cliente
        System.out.println("[POST /reservar] mesasCandidatas size=" + mesasCandidatas.size());

        // aca agrego mensaje segun haya mesas o no
        if (mesasCandidatas.isEmpty()) {
            model.addAttribute("mensaje",
                    "No hay mesas disponibles para esa fecha, tipo de comida y cantidad de personas.");
        } else {
            model.addAttribute("mensaje",
                    "Mesas disponibles encontradas. Seleccione una mesa y continúe con los datos del cliente.");
        }

        // aca guardamos los datos ingresados para que se mantengan en la vista
        model.addAttribute("fechaSeleccionada", fechaReserva);
        model.addAttribute("cantidadSeleccionada", cantidadPersonas);
        model.addAttribute("tipoMenuSeleccionado", tipoMenu);

        // aca guardamos la lista de mesas que se mostraran en tarjetas
        model.addAttribute("mesasCandidatas", mesasCandidatas);

        // volver a la vista de clientes con resultados
        return "clientes";
    }

        @GetMapping("/reservas/available")
        @ResponseBody
        public java.util.Map<String, Object> disponibilidadPorFecha(
                        @RequestParam("fecha")
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                        java.time.LocalDate fecha) {

                java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
                if (fecha == null) {
                        respuesta.put("count", 0);
                        respuesta.put("mesas", java.util.List.of());
                        return respuesta;
                }

                // traer mesas activas
                java.util.List<sabor_gourmet.sabor_gourmet.modelos.Mesas> mesas = mesasRepository.findByDisponibleTrue();

                // traer reservas para la fecha
                java.util.List<sabor_gourmet.sabor_gourmet.modelos.Reserva> reservasEnFecha = reservaRepository.findByFecha(fecha);

                // map mesaId -> set de tipos de menu reservados
                java.util.Map<Long, java.util.Set<sabor_gourmet.sabor_gourmet.modelos.TipoMenu>> reservadosPorMesa = new java.util.HashMap<>();
                for (sabor_gourmet.sabor_gourmet.modelos.Reserva r : reservasEnFecha) {
                        Long mid = r.getMesa().getId();
                        reservadosPorMesa.computeIfAbsent(mid, k -> new java.util.HashSet<>()).add(r.getTipoMenu());
                }

                java.util.List<java.util.Map<String, Object>> mesasResp = new java.util.ArrayList<>();
                for (sabor_gourmet.sabor_gourmet.modelos.Mesas m : mesas) {
                        java.util.Map<String, Object> mm = new java.util.HashMap<>();
                        mm.put("id", m.getId());
                        // si el campo 'numero' no está seteado (0), mostramos el id como fallback
                        mm.put("numero", m.getNumero() != 0 ? m.getNumero() : m.getId());
                        mm.put("capacidad", m.getCapacidad());

                        java.util.List<String> disponibles = new java.util.ArrayList<>();
                        for (sabor_gourmet.sabor_gourmet.modelos.TipoMenu tm : sabor_gourmet.sabor_gourmet.modelos.TipoMenu.values()) {
                                java.util.Set<sabor_gourmet.sabor_gourmet.modelos.TipoMenu> reservados = reservadosPorMesa.getOrDefault(m.getId(), java.util.Set.of());
                                if (!reservados.contains(tm)) {
                                        disponibles.add(tm.name().toLowerCase());
                                }
                        }
                        mm.put("availableTipos", disponibles);
                        mesasResp.add(mm);
                }

                // count mesas that have at least one available tipo
                long count = mesas.stream().filter(m -> {
                        java.util.Set<sabor_gourmet.sabor_gourmet.modelos.TipoMenu> reservados = reservadosPorMesa.getOrDefault(m.getId(), java.util.Set.of());
                        return reservados.size() < sabor_gourmet.sabor_gourmet.modelos.TipoMenu.values().length;
                }).count();

                respuesta.put("count", count);
                respuesta.put("mesas", mesasResp);
                return respuesta;
        }

    @GetMapping("/reservar/cliente")
    // luego aca mostramos el formulario con datos de cliente para la mesa seleccionada
    public String mostrarFormularioCliente(
            @RequestParam("mesaId") Long mesaId,      // aca agregamos id de la mesa que el usuario eligio
            @RequestParam("fecha")  // aca agregamos el request param fecha
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)//aca agregamos el formato de fecha
            LocalDate fecha,
            @RequestParam("cantidadPersonas") int cantidadPersonas, // aca agregamos cantidad de personas
            @RequestParam("tipoMenu") String tipoMenu,// aca agregamos tipo de menu
            Model model) {
        //aca agregamos un debug en la consola para chequear los parametros que llegan
        System.out.println("[GET /reservar/cliente] mesaId=" + mesaId +
                ", fecha=" + fecha +
                ", cantidadPersonas=" + cantidadPersonas +
                ", tipoMenu=" + tipoMenu);

        // aca buscamos la mesa en la BD
        Optional<Mesas> mesaOpt = reservaServicio.obtenerMesaPorId(mesaId);
        if (mesaOpt.isEmpty()) {
            // si no existe, mostrar mensaje en clientes
            System.out.println("[GET /reservar/cliente] mesa no encontrada");
            model.addAttribute("mensaje", "La mesa seleccionada no existe.");
            return "clientes";
        }
        // si existe, seguimos adelante
        Mesas mesa = mesaOpt.get();
        System.out.println("[GET /reservar/cliente] mesa encontrada id=" + mesa.getId());

        // aca pasamos a la vista toda la info de la reserva
        model.addAttribute("mesa", mesa);
        model.addAttribute("fecha", fecha);
        model.addAttribute("cantidadPersonas", cantidadPersonas);
        model.addAttribute("tipoMenu", tipoMenu);

        // aca agregamos un cliente vacio para que el formulario lo rellene
        model.addAttribute("cliente", new sabor_gourmet.sabor_gourmet.modelos.Cliente());

        // aca mostramos la vista reservar_cliente.html
        return "reservar_cliente";
    }

    @PostMapping("/reservar/cliente")
    // si todo lo anteior esta correcto aca guardamos el cliente + reserva
    public String guardarReservaConCliente(
            @RequestParam("mesaId") Long mesaId,
            @RequestParam("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam("cantidadPersonas") int cantidadPersonas,
            @RequestParam("tipoMenu") String tipoMenu,
            @ModelAttribute("cliente")
            sabor_gourmet.sabor_gourmet.modelos.Cliente cliente,
            Model model) {
        //aca agregamos un debug en la consola para chequear los parametros que llegan
        System.out.println("[POST /reservar/cliente] mesaId=" + mesaId +
                ", fecha=" + fecha +
                ", cantidadPersonas=" + cantidadPersonas +
                ", tipoMenu=" + tipoMenu +
                ", cliente.nombre=" + cliente.getNombre());

        // aca pasamos tipoMenu string a enum
        TipoMenu tipoMenuEnum = TipoMenu.valueOf(tipoMenu.toUpperCase());

        // aca pedimos al servicio que cree la reserva con el cliente
        Optional<Reserva> reservaOpt = reservaServicio.crearReservaConCliente(
                fecha, tipoMenuEnum, cantidadPersonas, mesaId, cliente);

        if (reservaOpt.isEmpty()) {
            // si algo sale mal, volver a clientes con mensaje
            System.out.println("[POST /reservar/cliente] crearReservaConCliente devolvió empty");
            model.addAttribute("mensaje", "No fue posible crear la reserva. Intente nuevamente.");
            return "clientes";
        }

        // aca si todo OK, devolver la misma vista de confirmación con la reserva creada
        Reserva reserva = reservaOpt.get();
        // pasar la reserva creada a la vista para mostrar el modal
        model.addAttribute("reservaConfirmada", reserva);
        model.addAttribute("showConfirmModal", true);

        // también rellenar los datos necesarios en el modelo para la vista
        model.addAttribute("mesa", reserva.getMesa());
        model.addAttribute("fecha", reserva.getFecha());
        model.addAttribute("cantidadPersonas", reserva.getCantidadPersonas());
        model.addAttribute("tipoMenu", reserva.getTipoMenu().name().toLowerCase());
        model.addAttribute("cliente", reserva.getCliente());

        // regresar a la vista de confirmar cliente (la misma página) para mostrar modal
        return "reservar_cliente";
    }
}
