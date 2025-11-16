package sabor_gourmet.sabor_gourmet.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sabor_gourmet.sabor_gourmet.modelos.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
