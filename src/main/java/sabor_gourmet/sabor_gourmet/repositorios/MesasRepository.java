package sabor_gourmet.sabor_gourmet.repositorios;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Query;        
import org.springframework.stereotype.Repository;
import sabor_gourmet.sabor_gourmet.modelos.Mesas;
import java.util.List;

@Repository 
public interface MesasRepository extends JpaRepository<Mesas, Long> {
    // aca vusco todas las mesas  ordenadas por id (número de mesa interno)
    @Query("SELECT m FROM Mesas m ORDER BY m.id ASC")
    // esta consulta JPQL le dice a Hibernate:
    //  "traeme todas las filas de la tabla mesas, ordendas por el campo id de menor a mayor"
    List<Mesas> obtenerTodasLasMesas();

    // este metodo usa "query derivada" de Spring Data:
    // por el nombre findByDisponibleTrue, Spring arma una consulta tipo:
    //  SELECT * FROM mesas WHERE disponible = true
    //
    // trae solo las mesas que estan marcadas como disponibles (campo boolean disponible = true)
    List<Mesas> findByDisponibleTrue();
}
