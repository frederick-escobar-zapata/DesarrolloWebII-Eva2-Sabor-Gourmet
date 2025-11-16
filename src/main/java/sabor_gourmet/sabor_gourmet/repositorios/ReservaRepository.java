package sabor_gourmet.sabor_gourmet.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sabor_gourmet.sabor_gourmet.modelos.Reserva;
import sabor_gourmet.sabor_gourmet.modelos.TipoMenu;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    // Para disponibilidad por fecha + tipo menú (lo usaremos para mostrar disponibilidad en /admin/disponibilidad)
    List<Reserva> findByFechaAndTipoMenu(LocalDate fecha, TipoMenu tipoMenu);
}
