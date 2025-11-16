package sabor_gourmet.sabor_gourmet.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sabor_gourmet.sabor_gourmet.repositorios.MesasRepository;
import sabor_gourmet.sabor_gourmet.modelos.Mesas;

import java.util.List;
import java.util.Optional;

@Service        
public class MesaServicio {

        @Autowired
        private MesasRepository mesasRepository; // repo para acceder a la tabla mesas

        // aca devolvemos todas las mesas sin orden especial (usa findAll() del repo)
        public List<Mesas> obtenerTodasLasMesas() {
            return mesasRepository.findAll();
        }
        // aca devolvemos solo las mesas disponibles (disponible = true)
        public Mesas guardarMesa(Mesas mesa) {
            if (mesa == null) {
                throw new IllegalArgumentException("La mesa no puede ser nula.");
            }

            // aca agregamos un log simple para depurar: muestra que id llega desde el formulario/controlador
            System.out.println("ReservaServicio.guardarMesa -> id recibido: " + mesa.getId());

            // Si el id viene con un valor negativo o 0 desde el formulario, lo tratamos como nuevo
            // (esto es un truco defensivo por si alguien manda id=0 en vez de null)
            if (mesa.getId() != null && mesa.getId() <= 0) {
                mesa.setId(null);
            }

            
            return mesasRepository.save(mesa);
        }
        // aca devolvemos una mesa por id, o Optional vacío si no existe
        public Optional<Mesas> obtenerMesaPorId(Long id) {
            if (id == null) {
                return Optional.empty();
            }
            // aca devolvemos el Optional directamente. El que llama al método decide qué hacer si no existe.
            return mesasRepository.findById(id);
        }
        // aca eliminamos una mesa por id
        public void eliminarMesa(Long id) {
            if (id == null) {
                throw new IllegalArgumentException("El ID de la mesa no puede ser nulo.");
            }
            mesasRepository.deleteById(id);
        }       
}

