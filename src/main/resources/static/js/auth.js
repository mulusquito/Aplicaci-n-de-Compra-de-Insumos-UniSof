/**
 * API de autenticación UNISOF
 * Conecta el frontend con el backend Spring Boot (SCRUM-6, SCRUM-7, SCRUM-35, SCRUM-36)
 */

const authApi = {
    baseUrl: '',  // mismo origen (localhost:8080)

    /** Marca que el usuario tuvo sesión (para detectar expiración por inactividad) */
    setHadSession() { sessionStorage.setItem('hadSession', '1'); },

    /** Limpia la marca de sesión */
    clearHadSession() { sessionStorage.removeItem('hadSession'); },

    /** Muestra modal de sesión expirada y redirige al login */
    mostrarSesionExpirada() {
        if (document.getElementById('modal-sesion-expirada')) return;
        const overlay = document.createElement('div');
        overlay.id = 'modal-sesion-expirada';
        overlay.className = 'modal-sesion-expirada';
        overlay.innerHTML = `
            <div class="modal-sesion-contenido">
                <span class="modal-sesion-icono">⏱</span>
                <h3>Sesión cerrada por inactividad</h3>
                <p>Sesión expirada o no autenticado. Inicie sesión nuevamente.</p>
                <a href="/login.html" class="btn-primary">Iniciar Sesión</a>
            </div>
        `;
        document.body.appendChild(overlay);
        setTimeout(() => overlay.classList.add('visible'), 10);
        sessionStorage.removeItem('hadSession');
    },

    async login(usuario, contrasena) {
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usuario, contrasena }),
                credentials: 'include'
            });
            const data = await res.json();
            if (res.ok && data.valido) {
                return { ok: true, mensaje: data.mensaje, data };
            }
            return { ok: false, mensaje: data.mensaje || 'Error al iniciar sesión' };
        } catch (err) {
            return { ok: false, mensaje: 'Error de conexión con el servidor' };
        }
    },

    async verifyToken(usuario, token) {
        try {
            const res = await fetch('/api/auth/verify-token', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usuario, token }),
                credentials: 'include'
            });
            const data = await res.json();
            if (res.ok && data.valido) {
                authApi.setHadSession();
                return { ok: true, mensaje: data.mensaje, data };
            }
            return { ok: false, mensaje: data.mensaje || 'Token inválido o expirado' };
        } catch (err) {
            return { ok: false, mensaje: 'Error de conexión con el servidor' };
        }
    },

    async getMe() {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (res.ok) {
                return { ok: true, usuario: await res.json() };
            }
            const data = res.status === 401 ? (await res.json().catch(() => ({}))) : {};
            const mensaje = data.mensaje || 'Sesión expirada';
            if (res.status === 401 && sessionStorage.getItem('hadSession')) {
                authApi.mostrarSesionExpirada();
            }
            return { ok: false, mensaje };
        } catch (err) {
            return { ok: false, mensaje: 'Error de conexión' };
        }
    },

    async logout() {
        try {
            await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
        } catch (_) {}
        authApi.clearHadSession();
    }
};
