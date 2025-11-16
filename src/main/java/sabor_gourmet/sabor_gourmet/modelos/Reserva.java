package sabor_gourmet.sabor_gourmet.modelos;   

import jakarta.persistence.*;                 
import java.time.LocalDate;                  

@Entity                                      
public class Reserva {

    @Id        // esta es mi llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)    
    private Long id;// id autoincremental (IDENTITY en Postgres)

    
    private LocalDate fecha;// esta es la fecha de la reserva (dia que viene el cliente)

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_menu")    
    private TipoMenu tipoMenu;// este es el tipo de comida (enum) que se guarda como texto en la columna tipo_menu

   
    private int cantidadPersonas; // cuantas personas vienen con esta reserva

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id", nullable = false)
    // esta es mi relacion ya que muchas reservas pueden apuntar a la misma mesa
    // en la tabla reserva hay una columna mesa_id (no puede ser null)
    private Mesas mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    // esta es mi relacion ya que muchas reservas pueden ser del mismo cliente
    
    private Cliente cliente;

    // ----- Getters y Setters -----
    public Long getId() {                    // devuelve el id
        return id;
    }

    public void setId(Long id) {             // setea el id (normalmente lo hace Hibernate)
        this.id = id;
    }

    public LocalDate getFecha() {            // devuelve la fecha
        return fecha;
    }

    public void setFecha(LocalDate fecha) {  // asigna la fecha de la reserva
        this.fecha = fecha;
    }

    public TipoMenu getTipoMenu() {          // devuelve el enum de tipo de menu
        return tipoMenu;
    }

    public void setTipoMenu(TipoMenu tipoMenu) { // asigna el tipo de comida
        this.tipoMenu = tipoMenu;
    }

    public int getCantidadPersonas() {       // devuelve el numero de personas
        return cantidadPersonas;
    }

    public void setCantidadPersonas(int cantidadPersonas) {
        // asigna cuantas personas vienen
        this.cantidadPersonas = cantidadPersonas;
    }

    public Mesas getMesa() {                 // devuelve la mesa asociada
        return mesa;
    }

    public void setMesa(Mesas mesa) {        // asigna la mesa a esta reserva
        this.mesa = mesa;
    }

    public Cliente getCliente() {            // devuelve el cliente asociado
        return cliente;
    }

    public void setCliente(Cliente cliente) {// asigna el cliente a la reserva
        this.cliente = cliente;
    }
}