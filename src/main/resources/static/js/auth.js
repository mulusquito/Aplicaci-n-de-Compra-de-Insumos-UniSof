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

// Inactividad: aviso a los 2 min; cierre a los 2 min más sin actividad.
// Spring solo alarga la sesión con peticiones HTTP: ping ligero al interactuar (cursor, teclas, scroll).
(function initInactivityWarning() {
    if (typeof window === 'undefined') return;

    if (!sessionStorage.getItem('hadSession')) return;

    const INACTIVITY_MS = 120 * 1000;
    const WARNING_DURATION_MS = 120 * 1000;
    const PING_MIN_INTERVAL_MS = 45 * 1000;

    let inactiveUntilWarningTimer = null;
    let forceLogoutAfterWarningTimer = null;
    let countdownInterval = null;
    let lastSessionPing = 0;
    let lastIdleSchedule = 0;
    const IDLE_SCHEDULE_THROTTLE_MS = 500;

    function clearCountdown() {
        if (countdownInterval) {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
    }

    function clearIdleTimers() {
        if (inactiveUntilWarningTimer) {
            clearTimeout(inactiveUntilWarningTimer);
            inactiveUntilWarningTimer = null;
        }
        if (forceLogoutAfterWarningTimer) {
            clearTimeout(forceLogoutAfterWarningTimer);
            forceLogoutAfterWarningTimer = null;
        }
        clearCountdown();
    }

    function closeWarningModal() {
        clearCountdown();
        const modal = document.getElementById('modal-aviso-inactividad');
        if (modal) {
            modal.classList.remove('visible');
            setTimeout(() => modal.remove(), 250);
        }
    }

    function pingSessionRenew() {
        const now = Date.now();
        if (now - lastSessionPing < PING_MIN_INTERVAL_MS) return;
        lastSessionPing = now;
        fetch('/api/auth/me', { credentials: 'include' })
            .then((res) => {
                if (res.status === 401 && sessionStorage.getItem('hadSession')) {
                    clearIdleTimers();
                    closeWarningModal();
                    authApi.mostrarSesionExpirada();
                }
            })
            .catch(() => {});
    }

    function scheduleInactiveChain() {
        clearIdleTimers();
        inactiveUntilWarningTimer = setTimeout(() => {
            inactiveUntilWarningTimer = null;
            showWarningModal();
            forceLogoutAfterWarningTimer = setTimeout(forceLogout, WARNING_DURATION_MS);
        }, INACTIVITY_MS);
    }

    function showWarningModal() {
        if (document.getElementById('modal-aviso-inactividad')) return;

        const secs = Math.round(WARNING_DURATION_MS / 1000);
        const overlay = document.createElement('div');
        overlay.id = 'modal-aviso-inactividad';
        overlay.className = 'modal-sesion-expirada';
        overlay.innerHTML = `
            <div class="modal-sesion-contenido">
                <span class="modal-sesion-icono">⏱</span>
                <h3>Sesión a punto de cerrarse</h3>
                <p>Por seguridad, tu sesión se cerrará por inactividad en
                    <strong id="inactividad-countdown">${secs}</strong> segundos.
                    Mueva el cursor, pulse una tecla o use «Seguir conectado» para continuar.</p>
                <button type="button" class="btn-primary" id="btn-seguir-activo">
                    Seguir conectado
                </button>
            </div>
        `;
        document.body.appendChild(overlay);
        setTimeout(() => overlay.classList.add('visible'), 10);

        const countdownEl = document.getElementById('inactividad-countdown');
        let remaining = secs;
        countdownInterval = setInterval(() => {
            remaining -= 1;
            if (remaining <= 0) {
                clearCountdown();
            }
            if (countdownEl) countdownEl.textContent = String(Math.max(remaining, 0));
        }, 1000);

        const btnSeguir = document.getElementById('btn-seguir-activo');
        if (btnSeguir) {
            btnSeguir.addEventListener('click', () => {
                closeWarningModal();
                clearIdleTimers();
                lastSessionPing = 0;
                pingSessionRenew();
                scheduleInactiveChain();
            });
        }
    }

    async function forceLogout() {
        closeWarningModal();
        clearIdleTimers();
        await authApi.logout();
        authApi.mostrarSesionExpirada();
    }

    function registerActivity() {
        if (document.getElementById('modal-aviso-inactividad')) {
            closeWarningModal();
            if (forceLogoutAfterWarningTimer) {
                clearTimeout(forceLogoutAfterWarningTimer);
                forceLogoutAfterWarningTimer = null;
            }
            lastSessionPing = 0;
            pingSessionRenew();
            lastIdleSchedule = Date.now();
            scheduleInactiveChain();
            return;
        }
        pingSessionRenew();
        const now = Date.now();
        if (now - lastIdleSchedule < IDLE_SCHEDULE_THROTTLE_MS) return;
        lastIdleSchedule = now;
        scheduleInactiveChain();
    }

    const activityEvents = ['click', 'keydown', 'mousemove', 'touchstart', 'scroll', 'wheel'];
    activityEvents.forEach((evt) =>
        window.addEventListener(evt, registerActivity, { passive: true })
    );

    lastSessionPing = 0;
    pingSessionRenew();
    scheduleInactiveChain();
})();
