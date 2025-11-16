package sabor_gourmet.sabor_gourmet.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity                      
public class Mesas {

    @Id                      // esta es mi llave primaria de la mesa
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // este es mi id autoincremental, se usa como "numero interno" de la mesa
    private Long id;   
    private int numero;
    // aca tengo la cantidad de personas que caben en esta mesa
    private int capacidad;
    // este boleaano indica si la mesa está en servicio (true) o fuera de servicio (false)
    private boolean disponible;

    // Relación inversa: Una mesa puede tener muchas reservas asociadas.
    @OneToMany(mappedBy = "mesa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // evita loops infinitos si serializas la mesa a JSON (reserva->mesa->reserva->mesa...)
    private List<Reserva> reservas;

    // aca tengo un Constructor vacío requerido por JPA
    public Mesas() {}

    // ----- Getters y Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
}
