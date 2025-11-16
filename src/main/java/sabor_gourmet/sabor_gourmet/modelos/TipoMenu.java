package sabor_gourmet.sabor_gourmet.modelos; 

// Estos valores se guardan en la BD como texto (por @Enumerated(EnumType.STRING) en Reserva.tipoMenu)
public enum TipoMenu {
    DESAYUNO,  // reservas para la mañana
    ALMUERZO,  // reservas para el almuerzo (mediodia)
    ONCE,      // reservas para la "once" (tipo merienda / tarde)
    CENA       // reservas para la noche
}
