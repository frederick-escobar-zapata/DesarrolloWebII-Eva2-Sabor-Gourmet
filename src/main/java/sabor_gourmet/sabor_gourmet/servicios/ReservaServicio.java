package sabor_gourmet.sabor_gourmet.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sabor_gourmet.sabor_gourmet.modelos.Cliente;
import sabor_gourmet.sabor_gourmet.modelos.Mesas;
import sabor_gourmet.sabor_gourmet.modelos.Reserva;
import sabor_gourmet.sabor_gourmet.modelos.TipoMenu;
import sabor_gourmet.sabor_gourmet.repositorios.ClienteRepository;
import sabor_gourmet.sabor_gourmet.repositorios.MesasRepository;
import sabor_gourmet.sabor_gourmet.repositorios.ReservaRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service 
public class ReservaServicio {

    @Autowired
    private MesasRepository mesasRepository;    // repo para hablar con la tabla mesas

    @Autowired
    private ReservaRepository reservaRepository; // repo para hablar con la tabla reserva

    @Autowired
    private ClienteRepository clienteRepository; // repo para hablar con la tabla clientes

   

    public List<Mesas> obtenerTodasLasMesas() {
        // aca devolvemos todas las mesas ordenadas por id (consulta custom del repo)
        return mesasRepository.obtenerTodasLasMesas();
    }

    public List<Mesas> obtenerMesasDisponibles() {
        // aca devolvemos solo mesas con disponible = true (en servicio)
        return mesasRepository.findByDisponibleTrue();
    }
    // aca guardamos una mesa (nueva o editada)
    public Mesas guardarMesa(Mesas mesa) {
        // aca validamos y  evitamos guardar null 
        if (mesa == null) {
            throw new IllegalArgumentException("La mesa no puede ser nula.");
        }
        // save inserta o actualiza segun si id es null o no
        return mesasRepository.save(mesa);
    }

    public Optional<Mesas> obtenerMesaPorId(Long id) {
        // aca si el id viene null devolvemos Optional vacío
        if (id == null) {
            return Optional.empty();
        }
        // aca devolvemos el Optional tal cual viene del repo
        return mesasRepository.findById(id);
    }

    public void eliminarMesa(Long id) {
        // aca validamos que el id no sea null
        if (id == null) {
            throw new IllegalArgumentException("El ID de la mesa no puede ser nulo.");
        }
        // aca borramos por id (si no existe lanza excepcion de JPA)
        mesasRepository.deleteById(id);
    }

    // --- DISPONIBILIDAD (ADMIN /admin/disponibilidad) ---

    /**
        * Devuelve un mapa Mesa -> true/false indicando si la mesa está disponible
     * Esto lo usamos en el panel admin para ver la ocupacion general por día y tipo de comida.
     */
    public Map<Mesas, Boolean> obtenerDisponibilidadPorFechaYTipo(LocalDate fecha, TipoMenu tipoMenu) {
        // primero  traemos todas las mesas ordenadas
        List<Mesas> todasLasMesas = mesasRepository.obtenerTodasLasMesas();
        // despues. traemos todas las reservas exactas para esa fecha + tipo de menu
        List<Reserva> reservas = reservaRepository.findByFechaAndTipoMenu(fecha, tipoMenu);

        // despues . armamos un set con los ids de las mesas que estan reservadas
        Set<Long> idsMesasOcupadas = reservas.stream()
                .map(reserva -> reserva.getMesa().getId()) // mapeamos Reserva -> id de la mesa
                .collect(Collectors.toSet());              // lo metemos en un Set para busqueda rapida

        // finalemnte . construimos el mapa Mesa -> Boolean manteniendo el orden original de la lista
        Map<Mesas, Boolean> disponibilidad = new LinkedHashMap<>();
        for (Mesas mesa : todasLasMesas) {
            // si el id de la mesa está en el set, la marcamos como ocupada=true
            boolean ocupada = idsMesasOcupadas.contains(mesa.getId());
            disponibilidad.put(mesa, ocupada);
        }

        return disponibilidad;
    }

    // --- DISPONIBILIDAD PÚBLICA (Paso 1 /reservar) ---

    /**
     * Paso 1: buscar mesas que cumplan capacidad, estén marcadas como disponibles
     * y NO tengan una reserva para esa fecha + tipo de menú.
     *
     * Esto es lo que usas en la página pública (index) para mostrar las mesas
     * candidatas para la reserva, antes de pedir datos del cliente.
     */
    public List<Mesas> buscarMesasCandidatas(LocalDate fecha, TipoMenu tipoMenu, int cantidadPersonas) {
        System.out.println("[Servicio] buscarMesasCandidatas -> fecha=" + fecha +
                ", tipoMenu=" + tipoMenu + ", cantidadPersonas=" + cantidadPersonas);

        // primero . traemos  las mesas con disponible = true (activas en el sistema)
        List<Mesas> mesasDisponibles = mesasRepository.findByDisponibleTrue();

        // despues. reservas existentes para esa fecha y ese tipo de comida
        List<Reserva> reservasEnEsaFechaYTipo =
                reservaRepository.findByFechaAndTipoMenu(fecha, tipoMenu);

        // despues . ids de mesas que ya estan ocupadas para esa combinacion de fecha+tipo
        Set<Long> idsMesasOcupadas = reservasEnEsaFechaYTipo.stream()
                .map(reserva -> reserva.getMesa().getId())  // tomamos el id de la mesa de cada reserva
                .collect(Collectors.toSet());

        System.out.println("[Servicio] mesasDisponibles=" + mesasDisponibles.size() +
                ", mesasOcupadas=" + idsMesasOcupadas.size());

        // despues. filtramos:
        //    - mesas disponibles (boolean disponible = true)
        //    - que no estén en el set de ocupadas
        //    - que tengan capacidad suficiente para 'cantidadPersonas'
        //    - ordenadas por id para que salgan bonitas y predecibles
        return mesasDisponibles.stream()
                .filter(m -> !idsMesasOcupadas.contains(m.getId()))  // excluimos mesas ya reservadas
                .filter(m -> m.getCapacidad() >= cantidadPersonas)   // solo las que soportan la cantidad
                .sorted(Comparator.comparingLong(Mesas::getId))      // orden por id ascendente
                .collect(Collectors.toList());
    }

    /**
     * Devuelve la lista de mesas (activas) que no están reservadas en una fecha dada.
     * No se filtra por tipo de menú ni por capacidad: es una vista general de disponibilidad por fecha.
     */
    public List<Mesas> obtenerMesasDisponiblesParaFecha(LocalDate fecha) {
        if (fecha == null) {
            return List.of();
        }
        // mesas activas en el sistema
        List<Mesas> mesasDisponibles = mesasRepository.findByDisponibleTrue();

        // reservas para esa fecha (cualquiera sea el tipo)
        List<Reserva> reservasEnFecha = reservaRepository.findByFecha(fecha);

        Set<Long> idsMesasOcupadas = reservasEnFecha.stream()
                .map(r -> r.getMesa().getId())
                .collect(Collectors.toSet());

        return mesasDisponibles.stream()
                .filter(m -> !idsMesasOcupadas.contains(m.getId()))
                .sorted(Comparator.comparingLong(Mesas::getId))
                .collect(Collectors.toList());
    }

    /**
     * Cuenta cuántas mesas activas están disponibles para una fecha específica.
     */
    public int contarMesasDisponiblesPorFecha(LocalDate fecha) {
        return obtenerMesasDisponiblesParaFecha(fecha).size();
    }

    // --- CREAR RESERVA + CLIENTE (Paso 2 /reservar/cliente) ---

    /**
     * Crea una reserva asociando un cliente nuevo a una mesa concreta.
     * Se usa cuando ya elegiste la mesa y el usuario metió sus datos en reservar_cliente.html.
     */
    public Optional<Reserva> crearReservaConCliente(
            LocalDate fecha, TipoMenu tipoMenu, int cantidadPersonas, Long mesaId, Cliente cliente) {

        System.out.println("[Servicio] crearReservaConCliente -> fecha=" + fecha +
                ", tipoMenu=" + tipoMenu +
                ", cantidadPersonas=" + cantidadPersonas +
                ", mesaId=" + mesaId +
                ", cliente.nombre=" + (cliente != null ? cliente.getNombre() : "null"));

        // aca validamos los datos  basico
        if (mesaId == null || fecha == null || tipoMenu == null || cliente == null) {
            System.out.println("[Servicio] Parámetros inválidos, retornando empty");
            return Optional.empty();
        }

        // aca buscamos la mesa por id en la BD
        Optional<Mesas> mesaOpt = mesasRepository.findById(mesaId);
        if (mesaOpt.isEmpty()) {
            System.out.println("[Servicio] Mesa no encontrada, retornando empty");
            return Optional.empty();
        }

        // Verificar que no exista ya una reserva para la misma mesa, fecha y tipo
        boolean yaReservada = reservaRepository.existsByMesaIdAndFechaAndTipoMenu(mesaId, fecha, tipoMenu);
        if (yaReservada) {
            System.out.println("[Servicio] La mesa ya está reservada para esa fecha y tipo, retornando empty");
            return Optional.empty();
        }

        //aca guardardo cliente nuevo en la tabla clientes
        Cliente clienteGuardado = clienteRepository.save(cliente);
        System.out.println("[Servicio] Cliente guardado con id=" + clienteGuardado.getId());

        // aca armamos la entidad Reserva con todos los datos
        Reserva reserva = new Reserva();
        reserva.setFecha(fecha);
        reserva.setTipoMenu(tipoMenu);
        reserva.setCantidadPersonas(cantidadPersonas);
        reserva.setMesa(mesaOpt.get());            // asignamos la mesa encontrada
        reserva.setCliente(clienteGuardado);       // asignamos el cliente recién guardado

        // aca guardamos la reserva en la tabla reserva
        Reserva guardada = reservaRepository.save(reserva);
        System.out.println("[Servicio] Reserva guardada con id=" + guardada.getId());

        // aca devolvemos la reserva envuelta en Optional 
        return Optional.of(guardada);
    }


    // aca devolvemos todas las reservas de una mesa sin filtros (todas las fechas y tipos)
    public List<Reserva> obtenerReservasPorMesa(Long mesaId) {
        if (mesaId == null) {
            return List.of(); // lista vacia si viene null
        }
        return reservaRepository.findByMesaId(mesaId);
    }

    
}
