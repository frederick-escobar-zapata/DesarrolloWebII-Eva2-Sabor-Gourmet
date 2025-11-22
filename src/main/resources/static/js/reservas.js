// reservas.js - manejo de disponibilidad, datepicker y navegación a reservar/cliente

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('reservaForm');
    const fechaInput = document.getElementById('fechaReserva');
    const fechaError = document.getElementById('fechaError');
    const availCountEl = document.getElementById('availCount');
    const availListEl = document.getElementById('availList');

    if (!form || !fechaInput) return; // no estamos en la página correcta

    const today = new Date();
    const fechaMinima = today.toISOString().slice(0,10);

    // iniciar flatpickr si está disponible
    if (typeof flatpickr !== 'undefined') {
        try { flatpickr.localize(flatpickr.l10ns.es); } catch (e) { }
        flatpickr(fechaInput, {
            dateFormat: 'Y-m-d',
            minDate: fechaMinima,
            locale: 'es',
            defaultDate: fechaInput.value || new Date(),
            onChange(selectedDates, dateStr) {
                fechaInput.value = dateStr;
                triggerFetch();
                updateTipoButtons();
            }
        });
    } else {
        fechaInput.setAttribute('min', fechaMinima);
    }

    form.addEventListener('submit', (e) => {
        if (!fechaInput.value) {
            e.preventDefault();
            if (fechaError) fechaError.textContent = 'Debe seleccionar una fecha.';
            return;
        }
        if (fechaInput.value < fechaMinima) {
            e.preventDefault();
            if (fechaError) fechaError.textContent = `Fecha inválida. Fecha mínima: ${fechaMinima}`;
            return;
        }
        if (fechaError) fechaError.textContent = '';
    });

    async function fetchAvailability(fecha, tipoMenu) {
        if (!fecha) {
            if (availCountEl) availCountEl.textContent = '-';
            if (availListEl) availListEl.innerHTML = '';
            return;
        }
        try {
            const params = new URLSearchParams();
            params.set('fecha', fecha);
            if (tipoMenu) params.set('tipoMenu', tipoMenu);
            const resp = await fetch(`/reservas/available?${params.toString()}`);
            if (!resp.ok) throw new Error('network');
            const data = await resp.json();

            if (availCountEl) availCountEl.textContent = String(data.count || 0);

            if (availListEl) {
                availListEl.innerHTML = '';
                const mesas = data.mesas || [];
                if (mesas.length === 0) {
                    availListEl.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay mesas disponibles para esa fecha.</div></div>';
                    return;
                }

                mesas.forEach(m => {
                    const col = document.createElement('div');
                    col.className = 'col-6 mb-2';
                    const ALL_TIPOS = ['desayuno','almuerzo','once','cena'];
                    let badges = '';
                    const available = Array.isArray(m.availableTipos) ? m.availableTipos : [];
                    ALL_TIPOS.forEach(t => {
                        const isAvailable = available.includes(t);
                        badges += `<button type="button" class="btn btn-sm tipo-badge ${isAvailable ? 'btn-outline-success' : 'btn-outline-secondary disabled'} me-1" data-tipo="${t}" ${isAvailable ? '' : 'disabled'}>${t.toUpperCase()}</button>`;
                    });

                    col.innerHTML = `
                        <div class="card p-2 h-100" data-mesa-id="${m.id}">
                            <div class="card-body p-2">
                                <div class="d-flex align-items-center mb-2">
                                    <div class="w-50">
                                        <h6 class="mb-0">Mesa N° ${m.numero}</h6>
                                    </div>
                                    <div class="w-50 text-end">
                                        <p class="mb-0 small">Capacidad: <strong>${m.capacidad}</strong></p>
                                    </div>
                                </div>
                                <div class="mesa-tipos mt-2">${badges}</div>
                            </div>
                        </div>`;

                    if (tipoMenu && Array.isArray(m.availableTipos) && !m.availableTipos.includes(tipoMenu)) {
                        // no agregar si no coincide el tipo solicitado
                    } else {
                        availListEl.appendChild(col);
                    }
                });
                updateTipoButtons();
            }
        } catch (err) {
            console.error('Error fetching availability', err);
            if (availCountEl) availCountEl.textContent = '-';
            if (availListEl) availListEl.innerHTML = '<div class="col-12"><div class="alert alert-danger">Error al obtener disponibilidad.</div></div>';
        }
    }

    function triggerFetch() {
        const fecha = fechaInput.value;
        fetchAvailability(fecha, null);
    }

    fechaInput.addEventListener('change', () => {
        triggerFetch();
        updateTipoButtons();
    });

    function isFormValid() {
        const fecha = fechaInput.value;
        if (!fecha) return false;
        if (fecha < fechaMinima) return false;
        const cantidadRaw = document.getElementById('cantidadPersonas') ? document.getElementById('cantidadPersonas').value : null;
        const cantidad = parseInt(cantidadRaw, 10);
        if (isNaN(cantidad) || cantidad <= 0) return false;
        return true;
    }

    function updateTipoButtons() {
        const formOk = isFormValid();
        const buttons = document.querySelectorAll('.tipo-badge');
        buttons.forEach(btn => {
            const originallyDisabled = btn.classList.contains('disabled') && btn.hasAttribute('disabled');
            if (originallyDisabled) {
                btn.setAttribute('disabled', 'disabled');
                btn.classList.add('disabled');
                btn.setAttribute('title', 'No disponible');
                return;
            }
            if (!formOk) {
                btn.setAttribute('disabled', 'disabled');
                btn.classList.add('disabled');
                btn.setAttribute('title', 'Complete fecha y cantidad');
            } else {
                btn.removeAttribute('disabled');
                btn.classList.remove('disabled');
                btn.removeAttribute('title');
            }
        });
    }

    const cantidadSelect = document.getElementById('cantidadPersonas');
    if (cantidadSelect) {
        cantidadSelect.addEventListener('change', () => {
            updateTipoButtons();
            triggerFetch();
        });
    }

    if (availListEl) {
        availListEl.addEventListener('click', (ev) => {
            const btn = ev.target.closest('.tipo-badge');
            if (!btn) return;
            const tipo = btn.getAttribute('data-tipo');

            const fecha = fechaInput.value;
            const cantidadRaw = document.getElementById('cantidadPersonas') ? document.getElementById('cantidadPersonas').value : null;
            const cantidad = parseInt(cantidadRaw, 10);
            if (!fecha) {
                if (fechaError) fechaError.textContent = 'Seleccione una fecha antes de reservar.';
                return;
            }
            if (isNaN(cantidad) || cantidad <= 0) {
                alert('Seleccione la cantidad de personas antes de reservar.');
                return;
            }

            const card = btn.closest('.card');
            const mesaId = card ? card.getAttribute('data-mesa-id') : null;
            if (mesaId) {
                const params = new URLSearchParams();
                params.set('mesaId', mesaId);
                params.set('fecha', fecha);
                params.set('cantidadPersonas', String(cantidad));
                params.set('tipoMenu', tipo);
                window.location.href = `/reservar/cliente?${params.toString()}`;
                return;
            }
            form.submit();
        });
    }

    if (fechaInput.value) triggerFetch();
    updateTipoButtons();
});

// Valida fecha y controla la UI de reservas

document.addEventListener('DOMContentLoaded', () => {
    // Ejecutar tras carga del DOM

    const form = document.getElementById('reservaForm');      // formulario principal
    const fechaInput = document.getElementById('fechaReserva'); // input fecha
    const fechaError = document.getElementById('fechaError'); // contenedor error fecha

    // salir si no estamos en la página
    if (!form || !fechaInput) {
        return;
    }

    // Inicializar Flatpickr (si está)
    try {
        flatpickr.localize(flatpickr.l10ns.es);
    } catch (e) {
        // si falta Flatpickr, usar fallback
    }
    const today = new Date();
    const fechaMinima = today.toISOString().slice(0,10);

    if (typeof flatpickr !== 'undefined') {
        // Al elegir fecha actualizar disponibilidad y botones
        flatpickr(fechaInput, {
            dateFormat: 'Y-m-d',
            minDate: fechaMinima,
            locale: 'es',
            onChange: function(selectedDates, dateStr) {
                // actualizar input y UI
                fechaInput.value = dateStr;
                triggerFetch();
                updateTipoButtons();
            }
        });
    } else {
        // fallback nativo
        fechaInput.setAttribute('min', fechaMinima);
    }

    // validar al enviar formulario
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

        // limpiar mensaje si pasa validación
        if (fechaError) {
            fechaError.textContent = '';
        }

        
    });

    // El tipo se elige desde las tarjetas

    // Obtener disponibilidad por fecha
    const availCountEl = document.getElementById('availCount');
    const availListEl = document.getElementById('availList');
    async function fetchAvailability(fecha, tipoMenu) {
        if (!fecha) {
            if (availCountEl) availCountEl.textContent = '-';
            if (availListEl) availListEl.innerHTML = '';
            return;
        }
        try {
            const params = new URLSearchParams();
            params.set('fecha', fecha);
            if (tipoMenu) params.set('tipoMenu', tipoMenu);
            const resp = await fetch(`/reservas/available?${params.toString()}`);
            if (!resp.ok) throw new Error('Network response was not ok');
            const data = await resp.json();

            if (availCountEl) availCountEl.textContent = String(data.count || 0);

            if (availListEl) {
                availListEl.innerHTML = '';
                const mesas = data.mesas || [];
                if (mesas.length === 0) {
                    availListEl.innerHTML = '<div class="col-12"><div class="alert alert-info">No hay mesas disponibles para esa fecha.</div></div>';
                } else {
                    mesas.forEach(m => {
                        const col = document.createElement('div');
                        col.className = 'col-6 mb-2';
                        const ALL_TIPOS = ['desayuno','almuerzo','once','cena'];
                        let badges = '';
                        const available = Array.isArray(m.availableTipos) ? m.availableTipos : [];
                        ALL_TIPOS.forEach(t => {
                            const isAvailable = available.includes(t);
                            badges += `
                                <button type="button" class="btn btn-sm tipo-badge ${isAvailable ? 'btn-outline-success' : 'btn-outline-secondary disabled'} me-1" data-tipo="${t}" ${isAvailable ? '' : 'disabled'}>${t.toUpperCase()}</button>
                            `;
                        });
                        col.innerHTML = `
                            <div class="card p-2 h-100" data-mesa-id="${m.id}">
                                <div class="card-body p-2">
                                    <div class="d-flex align-items-center mb-2">
                                        <div class="w-50">
                                            <h6 class="mb-0">Mesa N° ${m.numero}</h6>
                                        </div>
                                        <div class="w-50 text-end">
                                            <p class="mb-0 small">Capacidad: <strong>${m.capacidad}</strong></p>
                                        </div>
                                    </div>
                                    <div class="mesa-tipos mt-2">${badges}</div>
                                </div>
                            </div>`;
                        if (tipoMenu && Array.isArray(m.availableTipos) && !m.availableTipos.includes(tipoMenu)) {
                        } else {
                            availListEl.appendChild(col);
                        }
                    });
                    updateTipoButtons();
                }
            }

        } catch (err) {
            console.error('Error fetching availability', err);
            if (availCountEl) availCountEl.textContent = '-';
            if (availListEl) availListEl.innerHTML = '<div class="col-12"><div class="alert alert-danger">Error al obtener disponibilidad.</div></div>';
        }
    }

    function triggerFetch() {
        const fecha = fechaInput.value;
        fetchAvailability(fecha, null);
    }

    fechaInput.addEventListener('change', () => {
        triggerFetch();
        updateTipoButtons();
    });

    function isFormValid() {
        const fecha = fechaInput.value;
        if (!fecha) return false;
        if (fecha < fechaMinima) return false;
        const cantidadRaw = document.getElementById('cantidadPersonas') ? document.getElementById('cantidadPersonas').value : null;
        const cantidad = parseInt(cantidadRaw, 10);
        if (isNaN(cantidad) || cantidad <= 0) return false;
        return true;
    }

    function updateTipoButtons() {
        const formOk = isFormValid();
        const buttons = document.querySelectorAll('.tipo-badge');
        buttons.forEach(btn => {
            const originallyDisabled = btn.classList.contains('disabled') && btn.hasAttribute('disabled');
            if (originallyDisabled) {
                btn.setAttribute('disabled', 'disabled');
                btn.classList.add('disabled');
                btn.setAttribute('title', 'No disponible');
                return;
            }
            if (!formOk) {
                btn.setAttribute('disabled', 'disabled');
                btn.classList.add('disabled');
                btn.setAttribute('title', 'Complete fecha y cantidad');
            } else {
                btn.removeAttribute('disabled');
                btn.classList.remove('disabled');
                btn.removeAttribute('title');
            }
        });
    }

    const cantidadSelect = document.getElementById('cantidadPersonas');
    if (cantidadSelect) {
        // Al cambiar cantidad, actualizar botones y disponibilidad
        cantidadSelect.addEventListener('change', () => {
            updateTipoButtons();
            triggerFetch();
        });
    }

    if (availListEl) {
        availListEl.addEventListener('click', (ev) => {
        const btn = ev.target.closest('.tipo-badge');
        if (!btn) return;
        const tipo = btn.getAttribute('data-tipo');

        // tipo desde botón

        // validar fecha y cantidad
        const fecha = fechaInput.value;
        const cantidadRaw = document.getElementById('cantidadPersonas') ? document.getElementById('cantidadPersonas').value : null;
        const cantidad = parseInt(cantidadRaw, 10);
        if (!fecha) {
            if (fechaError) fechaError.textContent = 'Seleccione una fecha antes de reservar.';
            return;
        }
        if (isNaN(cantidad) || cantidad <= 0) {
            alert('Seleccione la cantidad de personas antes de reservar.');
            return;
        }

        // Obtener id de mesa y redirigir a reservar/cliente
        const card = btn.closest('.card');
        const mesaId = card ? card.getAttribute('data-mesa-id') : null;
        if (mesaId) {
            // redirigir con params
            const params = new URLSearchParams();
            params.set('mesaId', mesaId);
            params.set('fecha', fecha);
            params.set('cantidadPersonas', String(cantidad));
            params.set('tipoMenu', tipo);
            window.location.href = `/reservar/cliente?${params.toString()}`;
            return;
        }
        // fallback: enviar formulario
        form.submit();
    });
    }

    // Si la página carga con fecha preseleccionada, obtener disponibilidad
    if (fechaInput.value) {
        triggerFetch();
    }
    // actualizar botones según estado inicial
    updateTipoButtons();
});
