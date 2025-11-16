
// Principalmente valida la fecha y evita que el usuario mande cosas raras (fecha vacia o pasada)

document.addEventListener('DOMContentLoaded', () => {
    // en sta función se ejecuta cuando el HTML ya terminó de cargarse

    const form = document.getElementById('reservaForm');      // el formulario principal de reserva
    const fechaInput = document.getElementById('fechaReserva'); // input type="date" donde el user elige el dia
    const fechaError = document.getElementById('fechaError'); // div donde mostramos el error de fecha

    // Si por algun motivo no estamos en la pagina que tiene estos elementos, salimos
    if (!form || !fechaInput) {
        return;
    }

    // aca sacamos la fecha actual (hoy) desde JS
    const hoy = new Date();
    const año = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0'); // getMonth es 0-11, por eso +1
    const dia = String(hoy.getDate()).padStart(2, '0');

    // armamos la fecha minima en formato yyyy-MM-dd que usa el input date
    const fechaMinima = `${año}-${mes}-${dia}`;

    // le decimos al input que no acepte fechas menores a hoy (minima)
    fechaInput.setAttribute('min', fechaMinima);

    // aca escuchamos el evento submit del formulario
    form.addEventListener('submit', (e) => {
        // Validamos que la fecha no este vacia
        if (!fechaInput.value) {
            e.preventDefault(); // cancelamos el submit 
            if (fechaError) {
                fechaError.textContent = 'Debe seleccionar una fecha.'; // mensaje para el user
            }
            return;
        }

        // Validamos que la fecha no sea anterior a hoy
        if (fechaInput.value < fechaMinima) {
            e.preventDefault(); // bloqueamos el submit
            if (fechaError) {
                fechaError.textContent = `Fecha inválida. Fecha mínima: ${fechaMinima}`;
            }
            return;
        }

        // Si la fecha es valida, limpiamos el mensaje de error (si es que habia)
        if (fechaError) {
            fechaError.textContent = '';
        }

        
    });
});
