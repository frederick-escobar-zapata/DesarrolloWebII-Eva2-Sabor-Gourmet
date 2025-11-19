package sabor_gourmet.sabor_gourmet.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sabor_gourmet.sabor_gourmet.modelos.Reserva;
import sabor_gourmet.sabor_gourmet.modelos.TipoMenu;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    // acqa tenemos la disponibilidad por fecha + tipo menú (lo usaremos para mostrar disponibilidad en /admin/disponibilidad)
    List<Reserva> findByFechaAndTipoMenu(LocalDate fecha, TipoMenu tipoMenu);
    // aca devolvemos todas las reservas asociadas a una mesa (sin filtros de fecha ni tipo)
    List<Reserva> findByMesaId(Long mesaId);
    // devuelve todas las reservas para una fecha concreta (sin filtrar por tipo)
    List<Reserva> findByFecha(java.time.LocalDate fecha);

    // Comprueba si ya existe una reserva para una mesa en una fecha y tipo específicos
    boolean existsByMesaIdAndFechaAndTipoMenu(Long mesaId, java.time.LocalDate fecha, TipoMenu tipoMenu);
}
