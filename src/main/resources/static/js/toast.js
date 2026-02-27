/**
 * Toast de feedback para la aplicación UNISOF.
 * Muestra mensajes temporales (éxito, error, info).
 */
const Toast = {
    /** Muestra un toast por unos segundos */
    show(mensaje, tipo = 'success', duracion = 3000) {
        let el = document.getElementById('toast-feedback');
        if (!el) {
            el = document.createElement('div');
            el.id = 'toast-feedback';
            el.className = 'toast-feedback';
            document.body.appendChild(el);
        }
        el.textContent = mensaje;
        el.className = 'toast-feedback ' + tipo;
        el.classList.add('visible');
        clearTimeout(el._timeout);
        el._timeout = setTimeout(() => {
            el.classList.remove('visible');
        }, duracion);
    },
    success(mensaje, duracion) { this.show(mensaje, 'success', duracion); },
    error(mensaje, duracion) { this.show(mensaje, 'error', duracion || 4000); },
    info(mensaje, duracion) { this.show(mensaje, 'info', duracion); }
};
