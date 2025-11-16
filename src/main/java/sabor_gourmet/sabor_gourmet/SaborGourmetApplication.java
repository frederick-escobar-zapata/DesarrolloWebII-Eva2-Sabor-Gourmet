package sabor_gourmet.sabor_gourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// Escanea las entidades en el paquete 'modelos'
@EntityScan(basePackages = "sabor_gourmet.sabor_gourmet.modelos")
// Escanea los repositorios en el paquete 'repositorios'
@EnableJpaRepositories(basePackages = "sabor_gourmet.sabor_gourmet.repositorios")
public class SaborGourmetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaborGourmetApplication.class, args);
    }

}
