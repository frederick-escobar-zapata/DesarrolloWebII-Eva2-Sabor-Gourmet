// reservar_cliente
// reservar_cliente: validación mínima y manejo del modal de confirmación
(function(){
    const inputs = ['nombre','apellido','email','telefono']
        .map(id => document.getElementById(id))
        .filter(Boolean);
    const submitBtn = document.getElementById('submitBtn');
    function allFilled(){
        return inputs.length > 0 && inputs.every(i => String(i.value || '').trim().length > 0);
    }
    function update(){ if (submitBtn) submitBtn.disabled = !allFilled(); }

    inputs.forEach(i => i.addEventListener('input', update));

    if (window.MutationObserver) {
        const obs = new MutationObserver(update);
        inputs.forEach(i => {
            try { obs.observe(i, { attributes: true, attributeFilter: ['value'] }); }
            catch(e) { }
        });
    }

    const form = document.getElementById('clienteForm');
    function setupModal() {
        if (!form) return;
        // obtener elementos del modal y comprobación de Bootstrap
        const confirmModalEl = document.getElementById('confirmModal');
        const bsModal = (confirmModalEl && window.bootstrap) ? new bootstrap.Modal(confirmModalEl) : null;
        const acceptBtn = document.getElementById('confirmAccept');
        let shownByServer = false;

        function fillModal() {
            const mesaEl = document.querySelector('input[name="mesaId"]');
            const fechaEl = document.querySelector('input[name="fecha"]');
            const tipoEl = document.querySelector('input[name="tipoMenu"]');
            const cantidadEl = document.querySelector('input[name="cantidadPersonas"]');
            const mesa = mesaEl ? mesaEl.value : '';
            const fecha = fechaEl ? fechaEl.value : '';
            const tipo = tipoEl ? tipoEl.value : '';
            const cantidad = cantidadEl ? cantidadEl.value : '';
            const nombre = document.getElementById('nombre') ? document.getElementById('nombre').value : '';
            const email = document.getElementById('email') ? document.getElementById('email').value : '';
            const telefono = document.getElementById('telefono') ? document.getElementById('telefono').value : '';

            const setText = (id, value) => { const el = document.getElementById(id); if (el) el.textContent = value; };
            setText('confMesa', mesa);
            setText('confFecha', fecha);
            setText('confTipo', tipo);
            setText('confCantidad', cantidad);
            setText('confNombre', nombre);
            setText('confEmail', email);
            setText('confTelefono', telefono);
        }
        try {
            // si el servidor indicó mostrar el modal tras guardar, mostrarlo
            const serverShow = confirmModalEl && confirmModalEl.getAttribute('data-server-show');
            if (serverShow === 'true') {
                shownByServer = true;
                if (bsModal) {
                    bsModal.show();
                } else if (confirmModalEl) {
                    confirmModalEl.classList.add('show');
                    confirmModalEl.style.display = 'block';
                    document.body.classList.add('modal-open');
                    let backdrop = document.createElement('div');
                    backdrop.className = 'modal-backdrop fade show';
                    backdrop.id = '__fallback_backdrop';
                    // si el usuario hace click sobre el backdrop en el fallback, redirigir
                    backdrop.addEventListener('click', function(){ if (shownByServer) window.location.href = '/'; });
                    document.body.appendChild(backdrop);
                }
            }

            // fallback: si el servidor renderizó datos en los spans del modal, mostrarlo
            const confNameEl = document.getElementById('confNombre');
            const confMesaEl = document.getElementById('confMesa');
            const hasServerData = (confNameEl && confNameEl.textContent && confNameEl.textContent.trim().length > 0)
                || (confMesaEl && confMesaEl.textContent && confMesaEl.textContent.trim().length > 0);
            if (hasServerData) {
                shownByServer = true;
                if (bsModal) {
                    bsModal.show();
                } else if (confirmModalEl) {
                    confirmModalEl.classList.add('show');
                    confirmModalEl.style.display = 'block';
                    document.body.classList.add('modal-open');
                    let backdrop = document.createElement('div');
                    backdrop.className = 'modal-backdrop fade show';
                    backdrop.id = '__fallback_backdrop';
                    backdrop.addEventListener('click', function(){ if (shownByServer) window.location.href = '/'; });
                    document.body.appendChild(backdrop);
                }
            }
        } catch (e) {  }

        if (acceptBtn) {
            acceptBtn.addEventListener('click', function(){
                const serverShow = confirmModalEl && confirmModalEl.getAttribute('data-server-show');
                if (serverShow === 'true') {
                    window.location.href = '/';
                    return;
                }
                if (bsModal) bsModal.hide();
                if (confirmModalEl) {
                    confirmModalEl.classList.remove('show');
                    confirmModalEl.style.display = 'none';
                    document.body.classList.remove('modal-open');
                }
                const bd = document.getElementById('__fallback_backdrop');
                if (bd) bd.remove();
                form.submit();
            });
        }

        // Si el modal se cierra (evento de Bootstrap), redirigir a / cuando fue mostrado por el servidor
        try {
            if (confirmModalEl && bsModal) {
                confirmModalEl.addEventListener('hidden.bs.modal', function(){ if (shownByServer) window.location.href = '/'; });
            } else if (confirmModalEl) {
                // fallback: si existen elementos con data-bs-dismiss (Cerrar/Cancelar), enlazarlos
                const dismissEls = confirmModalEl.querySelectorAll('[data-bs-dismiss]');
                dismissEls.forEach(function(el){ el.addEventListener('click', function(){ if (shownByServer) window.location.href = '/'; }); });
            }
        } catch(e) { }
    }


    update();
    setupModal();
})();