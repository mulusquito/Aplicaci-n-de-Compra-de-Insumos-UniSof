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

// Aviso de inactividad: muestra feedback antes de cerrar sesión por timeout del servidor
(function initInactivityWarning() {
    if (typeof window === 'undefined') return;

    // Solo tiene sentido si alguna vez hubo sesión
    if (!sessionStorage.getItem('hadSession')) return;

    const INACTIVITY_MS = 120 * 1000;      // 2 min sin actividad (alineado con sesión 2m30s)
    const WARNING_DURATION_MS = 30 * 1000; // 30 s de aviso antes de cerrar

    let inactivityTimeout = null;
    let forceLogoutTimeout = null;
    let countdownInterval = null;

    function clearWarningTimers() {
        if (inactivityTimeout) {
            clearTimeout(inactivityTimeout);
            inactivityTimeout = null;
        }
        if (forceLogoutTimeout) {
            clearTimeout(forceLogoutTimeout);
            forceLogoutTimeout = null;
        }
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
    }

    function closeWarningModal() {
        const modal = document.getElementById('modal-aviso-inactividad');
        if (modal) {
            modal.classList.remove('visible');
            setTimeout(() => modal.remove(), 250);
        }
    }

    function showWarningModal() {
        if (document.getElementById('modal-aviso-inactividad')) return;

        const overlay = document.createElement('div');
        overlay.id = 'modal-aviso-inactividad';
        overlay.className = 'modal-sesion-expirada';
        overlay.innerHTML = `
            <div class="modal-sesion-contenido">
                <span class="modal-sesion-icono">⏱</span>
                <h3>Sesión a punto de cerrarse</h3>
                <p>Por seguridad, tu sesión se cerrará por inactividad en
                    <strong id="inactividad-countdown">30</strong> segundos.</p>
                <button type="button" class="btn-primary" id="btn-seguir-activo">
                    Seguir conectado
                </button>
            </div>
        `;
        document.body.appendChild(overlay);
        setTimeout(() => overlay.classList.add('visible'), 10);

        const countdownEl = document.getElementById('inactividad-countdown');
        let remaining = WARNING_DURATION_MS / 1000;
        countdownInterval = setInterval(() => {
            remaining -= 1;
            if (remaining <= 0) {
                clearInterval(countdownInterval);
                countdownInterval = null;
            }
            if (countdownEl) countdownEl.textContent = String(Math.max(remaining, 0));
        }, 1000);

        const btnSeguir = document.getElementById('btn-seguir-activo');
        if (btnSeguir) {
            btnSeguir.addEventListener('click', () => {
                closeWarningModal();
                clearWarningTimers();
                startInactivityTimer(); // reinicia el conteo
            });
        }
    }

    async function forceLogout() {
        closeWarningModal();
        clearWarningTimers();
        await authApi.logout();
        authApi.mostrarSesionExpirada();
    }

    function startInactivityTimer() {
        clearWarningTimers();
        inactivityTimeout = setTimeout(() => {
            showWarningModal();
            forceLogoutTimeout = setTimeout(forceLogout, WARNING_DURATION_MS);
        }, INACTIVITY_MS);
    }

    function registerActivity() {
        // Si el usuario realiza cualquier acción, reiniciamos el contador
        startInactivityTimer();
    }

    ['click', 'keydown', 'mousemove', 'touchstart'].forEach(evt =>
        window.addEventListener(evt, registerActivity, { passive: true })
    );

    startInactivityTimer();
})();
