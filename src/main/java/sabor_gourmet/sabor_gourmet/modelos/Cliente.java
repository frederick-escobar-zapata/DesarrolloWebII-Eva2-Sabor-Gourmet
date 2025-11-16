package sabor_gourmet.sabor_gourmet.modelos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import java.util.List;

@Entity                         // entidad JPA
@Table(name = "clientes")      // mapeada a la tabla "clientes" (ojo con el nombre en la BD)
public class Cliente {

    @Id                         // llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // es un id autoincremental generado por la BD
    private Long id;

    // aca tengo los datos basicos del cliente que hace la reserva
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;

    // aca tenemos las realaciones con tabla reservas
    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "cliente",           // aca esta el nombre del atributo en Reserva que apunta a Cliente
        cascade = {CascadeType.ALL}     // aca si borras un cliente, borra todas sus reservas (ojo!)
    )
    private List<Reserva> reserva;      // aca tenemso la lista de reservas asociadas a este cliente

    // ----- Getters y Setters -----
    public Long getId() {              // aca devolvemos  el id
        return id;
    }

    public void setId(Long id) {       // aca asignamos el id (normalmente lo hace Hibernate)
        this.id = id;
    }

    public String getNombre() {        // aca devolvemos el nombre del cliente
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {      // aca devolvemos el apellido del cliente
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {         //   aca devolvemos el correo de contacto
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {      // aca devolvemos el telefono de contacto
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Reserva> getReservas() { // aca devolvemos la lista de reservas del cliente
        return reserva;
    }

    public void setReservas(List<Reserva> reservas) {
        // aca asignamos la lista de reservas al cliente
        this.reserva = reservas;
    }
}


