/**
 * UNISOF — Burbuja de lengua de señas (MP4 en hover / panel móvil).
 *
 * Uso en HTML:
 *   <button data-sign-src="/videos/lsv/ejemplo.mp4" data-sign-label="Descripción en LSV">
 *     Texto del botón
 *   </button>
 *
 * Atributos opcionales:
 *   data-sign-position="cursor" | "element" | "element-top" | "element-right" | "element-left" — por defecto cursor
 *   element-top: preferencia arriba del disparador (p. ej. etiqueta de rol o 1.er ítem del nav); si no cabe, debajo.
 *     Si es el primer hijo de .admin-nav, sube la burbuja para no tapar .admin-sidebar-header (logo).
 *     Si el disparador está en .admin-user-info (rol junto a #user-name), no baja encima de «Cerrar sesión»: sube el bloque o clampa arriba.
 *   element-left: a la izquierda del control (p. ej. icono login en la esquina superior derecha)
 *   data-sign-size="160" — diámetro del círculo en px (hover)
 *   data-sign-gap="48" — element-right: separación horizontal (por defecto 28). element-top: separación vertical entre
 *     borde del disparador y la burbuja (por defecto 22); con hermano #user-name encima, la burbuja sube para no taparlo.
 *   data-sign-halign-with="#id" — selector CSS: para element-right, la burbuja se alinea al borde derecho de ese
 *     elemento (misma columna que otros campos), en lugar del borde del propio disparador.
 *   data-sign-valign="below" — solo con element-right: coloca la burbuja debajo del disparador (evita tapar
 *     botones/campos al centrarse en vertical con un enlace corto del pie de formulario).
 *
 * Requisitos del video: muted + sin audio para autoplay en navegadores.
 *
 * Móvil / tablet (viewport ≤768px o sin hover de puntero fino):
 *   sin hover; 1.er toque muestra la burbuja LSV, 2.o toque en el mismo control ejecuta la acción
 *   (enlace, submit, foco en input). Otro control con LSV cierra el anterior; toque fuera cierra.
 *
 * Rol en sidebar (#user-rol.admin-user-rol): ADMINISTRADOR → Administrador.mp4; VENDEDOR → vendedor.mp4;
 *   JEFE DE COMPRAS → Jefe de compras.mp4 (misma colocación element-top). Ver syncAdminUserRolLsvAttributes / window.syncUnisofAdminUserRolLsv.
 */
(function () {
    if (typeof document === 'undefined') return;

    const WRAP_CLASS = 'unisof-sign-bubble-wrap';
    const SEL = '[data-sign-src]';
    const VOLVER_INICIO_SIGN_SRC = '/videos/lsv/0417(7).mp4';

    /**
     * Mismo LSV (0417(7)) en enlaces «Volver al inicio» y «Volver al login» sin repetir data-sign-* en cada HTML.
     * — auth.back / texto «← Volver al inicio» | «← Back to home» (href="/")
     * — «← Volver al login» | «← Back to login» (href="/login.html")
     */
    function decorateVolverAlInicioAnchors() {
        document.querySelectorAll('a[href="/"]').forEach(function (a) {
            if (a.getAttribute('data-sign-src')) return;
            var key = (a.getAttribute('data-i18n-key') || '').trim();
            if (key === 'auth.back') {
                applyVolverAlInicioSignAttrs(a, 'Volver al inicio');
                return;
            }
            var t = (a.textContent || '').replace(/\s+/g, ' ').trim();
            if (t === '← Volver al inicio' || t === '← Back to home') {
                applyVolverAlInicioSignAttrs(a, 'Volver al inicio');
            }
        });
        document.querySelectorAll('a[href="/login.html"]').forEach(function (a) {
            if (a.getAttribute('data-sign-src')) return;
            var keyLogin = (a.getAttribute('data-i18n-key') || '').trim();
            if (keyLogin === 'auth.loginBack') {
                applyVolverAlInicioSignAttrs(a, 'Volver al login');
                return;
            }
            var t2 = (a.textContent || '').replace(/\s+/g, ' ').trim();
            if (t2 === '← Volver al login' || t2 === '← Back to login') {
                applyVolverAlInicioSignAttrs(a, 'Volver al login');
            }
        });
    }

    /** Borde derecho del formulario auth (inputs / botón ancho) para anclar LSV sin tapar controles. */
    function resolveAuthFormHalignSelector() {
        if (document.getElementById('contrasena')) return '#contrasena';
        if (document.getElementById('btn-enviar')) return '#btn-enviar';
        if (document.getElementById('btn-restablecer')) return '#btn-restablecer';
        if (document.getElementById('btn-verificar')) return '#btn-verificar';
        var tok = document.getElementById('token');
        if (tok && tok.getAttribute('type') !== 'hidden') return '#token';
        if (document.getElementById('correo')) return '#correo';
        if (document.getElementById('nueva-clave')) return '#nueva-clave';
        if (document.getElementById('confirmar-clave')) return '#confirmar-clave';
        return null;
    }

    function applyVolverAlInicioSignAttrs(a, signLabel) {
        var label = signLabel || 'Volver al inicio';
        var isLoginBack = label === 'Volver al login';
        a.setAttribute('data-sign-src', VOLVER_INICIO_SIGN_SRC);
        a.setAttribute('data-sign-position', 'element-right');
        a.setAttribute('data-sign-label', label);
        if (isLoginBack) {
            a.removeAttribute('data-sign-valign');
            a.setAttribute('data-sign-gap', '40');
            var hs = resolveAuthFormHalignSelector();
            if (hs) a.setAttribute('data-sign-halign-with', hs);
            else a.removeAttribute('data-sign-halign-with');
        } else {
            a.removeAttribute('data-sign-valign');
            a.removeAttribute('data-sign-gap');
            if (document.getElementById('contrasena')) {
                a.setAttribute('data-sign-halign-with', '#contrasena');
            } else {
                a.removeAttribute('data-sign-halign-with');
            }
        }
    }

    /**
     * Disparador LSV para modo táctil: si el toque es sobre (o dentro de) un <a href> sin data-sign-src,
     * no se considera el antecesor con data-sign-src — evita preventDefault() al enlace (p. ej. Ir a login).
     */
    function closestSignSrcTrigger(startEl) {
        var node = startEl;
        while (node && node.nodeType === 1) {
            if (node.matches && node.matches('a[href]') && !node.hasAttribute('data-sign-src')) {
                return null;
            }
            if (node.matches && node.matches(SEL)) {
                return node;
            }
            node = node.parentElement;
        }
        return null;
    }

    /** Tras 2.º toque LSV: navegación / envío fiable (p. ej. Safari tras 1.er preventDefault). */
    function activateSignTriggerDefault(el, e) {
        var tag = el.tagName;
        if (tag === 'A') {
            var raw = (el.getAttribute('href') || '').trim();
            if (!raw || raw === '#' || /^javascript:/i.test(raw)) {
                return false;
            }
            e.preventDefault();
            e.stopPropagation();
            var blank = el.getAttribute('target') === '_blank';
            window.setTimeout(function () {
                if (blank) {
                    window.open(el.href, '_blank', 'noopener,noreferrer');
                } else {
                    window.location.assign(el.href);
                }
            }, 0);
            return true;
        }
        if (tag === 'BUTTON' && el.type === 'submit' && el.form) {
            e.preventDefault();
            e.stopPropagation();
            window.setTimeout(function () {
                if (typeof el.form.requestSubmit === 'function') {
                    el.form.requestSubmit(el);
                } else {
                    el.form.submit();
                }
            }, 0);
            return true;
        }
        if (tag === 'INPUT' && el.form && (el.type === 'submit' || el.type === 'image')) {
            e.preventDefault();
            e.stopPropagation();
            window.setTimeout(function () {
                if (typeof el.form.requestSubmit === 'function') {
                    el.form.requestSubmit(el);
                } else {
                    el.form.submit();
                }
            }, 0);
            return true;
        }
        return false;
    }

    function prefersReducedMotion() {
        return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    }

    /**
     * Burbuja al pasar el ratón. Solo se desactiva en dispositivos sin puntero fino (móvil/tablet).
     * No usar (hover: hover): en muchos portátiles Windows con táctil Chrome reporta hover:none
     * y el LSV no aparecía en el navegador aunque sí en otros entornos.
     * prefers-reduced-motion no desactiva el LSV; el CSS ya acorta transiciones.
     */
    function useHoverBubble() {
        if (!window.matchMedia) return true;
        /* Alinear con chat.js (Nova): si el navegador reporta hover real, usar burbuja hover. */
        if (window.matchMedia('(hover: hover)').matches) return true;
        var anyFine = window.matchMedia('(any-pointer: fine)').matches;
        var fine = window.matchMedia('(pointer: fine)').matches;
        var coarse = window.matchMedia('(pointer: coarse)').matches;
        if (fine || anyFine) return true;
        if (coarse && !anyFine) return false;
        return true;
    }

    /** Escritorio “real”: hover con burbuja. Viewport ≤768px fuerza modo táctil aunque haya puntero fino. */
    function useSignHoverUi() {
        if (window.matchMedia && window.matchMedia('(max-width: 768px)').matches) return false;
        return useHoverBubble();
    }

    /** Móvil/tablet: secuencia doble toque sobre [data-sign-src], sin hover LSV. */
    function useSignTouchSequence() {
        return !useSignHoverUi();
    }

    function ensureDom() {
        let wrap = document.getElementById('unisof-sign-bubble');
        if (!wrap) {
            wrap = document.createElement('div');
            wrap.id = 'unisof-sign-bubble';
            wrap.className = WRAP_CLASS;
            wrap.setAttribute('role', 'presentation');
            wrap.innerHTML =
                '<video playsinline webkit-playsinline muted loop preload="auto" aria-hidden="true"></video>';
            document.body.appendChild(wrap);
        }

        let backdrop = document.getElementById('unisof-sign-mobile-backdrop');
        if (!backdrop) {
            backdrop = document.createElement('div');
            backdrop.id = 'unisof-sign-mobile-backdrop';
            backdrop.className = 'unisof-sign-mobile-backdrop';
            backdrop.setAttribute('aria-hidden', 'true');
            backdrop.addEventListener('click', closeMobilePanel);
            document.body.appendChild(backdrop);
        }

        let panel = document.getElementById('unisof-sign-mobile-panel');
        if (!panel) {
            panel = document.createElement('div');
            panel.id = 'unisof-sign-mobile-panel';
            panel.className = 'unisof-sign-mobile-panel';
            panel.setAttribute('role', 'dialog');
            panel.setAttribute('aria-modal', 'true');
            panel.setAttribute('aria-labelledby', 'unisof-sign-mobile-title');
            panel.innerHTML =
                '<div class="unisof-sign-mobile-panel-header">' +
                '<p class="unisof-sign-mobile-panel-title" id="unisof-sign-mobile-title">Lengua de señas</p>' +
                '<button type="button" class="unisof-sign-mobile-close" aria-label="Cerrar video de lengua de señas">×</button>' +
                '</div>' +
                '<div class="unisof-sign-mobile-video-ring">' +
                '<video playsinline webkit-playsinline muted loop controls preload="auto"></video>' +
                '</div>' +
                '<p class="unisof-sign-mobile-hint">Video sin sonido. Use los controles del reproductor si los necesita.</p>';
            panel.querySelector('.unisof-sign-mobile-close').addEventListener('click', closeMobilePanel);
            document.body.appendChild(panel);
        }

        return {
            wrap,
            video: wrap.querySelector('video'),
            backdrop,
            panel,
            panelVideo: panel.querySelector('video'),
            panelTitle: panel.querySelector('#unisof-sign-mobile-title')
        };
    }

    const dom = { initialized: false, touchSignDelegated: false };
    let currentSrc = '';
    let hideTimer = null;
    let lastMouse = { x: 0, y: 0 };
    /** En modo táctil: control cuyo LSV está visible esperando 2.º toque para la acción por defecto. */
    let touchArmedEl = null;

    function getSizePx(el) {
        const s = el.getAttribute('data-sign-size');
        const n = s ? parseInt(s, 10) : NaN;
        return Number.isFinite(n) && n > 40 && n < 400 ? n : null;
    }

    /** Separación horizontal (px) para element-right; default si no hay atributo o valor inválido. */
    function getGapPx(el, defaultGap) {
        const s = el.getAttribute('data-sign-gap');
        const n = s ? parseInt(s, 10) : NaN;
        return Number.isFinite(n) && n >= 0 && n <= 200 ? n : defaultGap;
    }

    /** Rect usado para colocar X en element-right si hay data-sign-halign-with; si no, el propio elemento. */
    function getHorizontalAnchorRect(el) {
        const sel = el.getAttribute('data-sign-halign-with');
        if (!sel) return el.getBoundingClientRect();
        const ref = document.querySelector(sel);
        return ref ? ref.getBoundingClientRect() : el.getBoundingClientRect();
    }

    function bubbleBox(wrap, size) {
        const w = wrap.offsetWidth > 16 ? wrap.offsetWidth : size;
        const h = wrap.offsetHeight > 16 ? wrap.offsetHeight : size;
        return { w: w, h: h };
    }

    function positionBubbleNearCursor(wrap, size) {
        const pad = 12;
        const box = bubbleBox(wrap, size);
        let x = lastMouse.x - box.w / 2;
        let y = lastMouse.y - box.h / 2;
        const maxX = window.innerWidth - box.w - pad;
        const maxY = window.innerHeight - box.h - pad;
        x = Math.max(pad, Math.min(x, maxX));
        y = Math.max(pad, Math.min(y, maxY));
        wrap.style.left = x + 'px';
        wrap.style.top = y + 'px';
        wrap.style.width = '';
        wrap.style.height = '';
    }

    function positionBubbleOnElement(wrap, el, size) {
        const r = el.getBoundingClientRect();
        const pad = 8;
        const box = bubbleBox(wrap, size);
        let x = r.left + r.width / 2 - box.w / 2;
        let y = r.bottom + pad;
        if (y + box.h > window.innerHeight - pad) {
            y = r.top - box.h - pad;
        }
        x = Math.max(pad, Math.min(x, window.innerWidth - box.w - pad));
        y = Math.max(pad, Math.min(y, window.innerHeight - box.h - pad));
        wrap.style.left = x + 'px';
        wrap.style.top = y + 'px';
        wrap.style.width = '';
        wrap.style.height = '';
        wrap.setAttribute('data-anchor', 'element');
    }

    /** Preferencia arriba del elemento; si no cabe en viewport, debajo (como element). */
    function positionBubbleAboveElement(wrap, el, size) {
        const r = el.getBoundingClientRect();
        const pad = 8;
        const edgeGap = getGapPx(el, 22);
        const box = bubbleBox(wrap, size);
        let x = r.left + r.width / 2 - box.w / 2;
        let y = r.top - box.h - edgeGap;
        const prev = el.previousElementSibling;
        if (prev && prev.id === 'user-name') {
            const nr = prev.getBoundingClientRect();
            const yClearName = nr.top - box.h - edgeGap;
            y = Math.min(y, yClearName);
        }
        const nav = el.closest && el.closest('.admin-nav');
        if (nav && nav.firstElementChild === el) {
            const sidebar = el.closest('.admin-sidebar');
            const header = sidebar && sidebar.querySelector('.admin-sidebar-header');
            if (header) {
                const hr = header.getBoundingClientRect();
                const yClearHeader = hr.top - box.h - edgeGap;
                y = Math.min(y, yClearHeader);
            }
        }
        if (y < pad) {
            const info = el.parentElement;
            if (info && info.classList && info.classList.contains('admin-user-info')) {
                const ir = info.getBoundingClientRect();
                const yAboveBlock = ir.top - box.h - edgeGap;
                if (yAboveBlock >= pad) {
                    y = yAboveBlock;
                } else {
                    y = pad;
                }
            } else {
                y = r.bottom + edgeGap;
            }
        }
        const infoPost = el.parentElement;
        if (infoPost && infoPost.classList && infoPost.classList.contains('admin-user-info')) {
            const logout = infoPost.nextElementSibling;
            if (logout && logout.getBoundingClientRect && (logout.classList.contains('admin-btn-logout') || logout.id === 'btn-logout')) {
                const lr = logout.getBoundingClientRect();
                const gapBtn = 6;
                if (y + box.h > lr.top - gapBtn) {
                    y = lr.top - box.h - edgeGap - gapBtn;
                    y = Math.max(pad, y);
                }
            }
        }
        x = Math.max(pad, Math.min(x, window.innerWidth - box.w - pad));
        y = Math.max(pad, Math.min(y, window.innerHeight - box.h - pad));
        wrap.style.left = x + 'px';
        wrap.style.top = y + 'px';
        wrap.style.width = '';
        wrap.style.height = '';
        wrap.setAttribute('data-anchor', 'element');
    }

    function rectsOverlap(a, b) {
        return !(a.right <= b.left || a.left >= b.right || a.bottom <= b.top || a.top >= b.bottom);
    }

    /** A la derecha del elemento (o del ancla horizontal si data-sign-halign-with). */
    function positionBubbleRightOfElement(wrap, el, size) {
        const r = el.getBoundingClientRect();
        const rX = getHorizontalAnchorRect(el);
        const pad = 12;
        const gap = getGapPx(el, 28);
        const box = bubbleBox(wrap, size);
        const w = box.w;
        const h = box.h;
        let x = rX.right + gap;
        const valign = (el.getAttribute('data-sign-valign') || '').toLowerCase();
        let y;
        if (valign === 'below' || valign === 'bottom') {
            const gapY = 10;
            y = r.bottom + gapY;
            if (y + h > window.innerHeight - pad) {
                y = r.top - h - gapY;
                if (y < pad) {
                    y = r.top + r.height / 2 - h / 2;
                }
            }
        } else {
            y = r.top + r.height / 2 - h / 2;
        }
        /* Si no cabe a la derecha, pegar al borde derecho del viewport — no colocar a la
         * izquierda del elemento: eso tapaba inputs de texto en formularios estrechos. */
        if (x + w > window.innerWidth - pad) {
            x = window.innerWidth - w - pad;
        }
        x = Math.max(pad, Math.min(x, window.innerWidth - w - pad));
        y = Math.max(pad, Math.min(y, window.innerHeight - h - pad));
        wrap.style.left = x + 'px';
        wrap.style.top = y + 'px';
        wrap.style.width = '';
        wrap.style.height = '';
        wrap.setAttribute('data-anchor', 'element');
        /* Evitar solape si el clamping movió la caja encima del botón */
        requestAnimationFrame(function () {
            if (!wrap.classList.contains('is-visible')) return;
            const br = wrap.getBoundingClientRect();
            if (rectsOverlap(br, r) && r.width > 0 && r.height > 0) {
                x = rX.right + gap;
                if (x + br.width > window.innerWidth - pad) {
                    x = window.innerWidth - br.width - pad;
                }
                x = Math.max(pad, Math.min(x, window.innerWidth - br.width - pad));
                wrap.style.left = x + 'px';
            }
        });
    }

    /** A la izquierda del elemento; si no cabe (borde izquierdo), a la derecha. Ideal para iconos en la esquina superior derecha. */
    function positionBubbleLeftOfElement(wrap, el, size) {
        const r = el.getBoundingClientRect();
        const pad = 12;
        const gap = 12;
        const box = bubbleBox(wrap, size);
        const bw = box.w;
        const bh = box.h;
        let x = r.left - bw - gap;
        let y = r.top + r.height / 2 - bh / 2;
        if (x < pad) {
            x = r.right + gap;
        }
        x = Math.max(pad, Math.min(x, window.innerWidth - bw - pad));
        y = Math.max(pad, Math.min(y, window.innerHeight - bh - pad));
        wrap.style.left = x + 'px';
        wrap.style.top = y + 'px';
        wrap.style.width = '';
        wrap.style.height = '';
        wrap.setAttribute('data-anchor', 'element');
    }

    /** Asigna URL al <video> y elimina nodos hijos (evita <source> obsoletos). */
    function applyVideoUrl(video, normalized) {
        while (video.firstChild) {
            video.removeChild(video.firstChild);
        }
        video.src = normalized;
    }

    /**
     * Codifica cada segmento del path (p. ej. 0409(2).mp4 → 0409%282%29.mp4).
     * Sin esto, algunos navegadores/servidores fallan con paréntesis en la URL.
     */
    function normalizeSignVideoSrc(src) {
        if (!src) return src;
        var s = String(src).trim();
        if (s.charAt(0) !== '/') return s;
        var qi = s.indexOf('?');
        var hi = s.indexOf('#');
        var end = s.length;
        if (qi >= 0) end = Math.min(end, qi);
        if (hi >= 0) end = Math.min(end, hi);
        var path = s.slice(0, end);
        var rest = s.slice(end);
        var parts = path.split('/').filter(function (p) {
            return p.length > 0;
        });
        if (!parts.length) return s;
        return '/' + parts.map(function (seg) {
            return encodeURIComponent(seg);
        }).join('/') + rest;
    }

    function playSignVideo(video) {
        if (!video) return;
        video.muted = true;
        video.defaultMuted = true;
        video.setAttribute('muted', '');
        video.setAttribute('playsinline', '');
        try {
            video.playsInline = true;
        } catch (e) {}
        video.loop = true;
        var tryPlay = function () {
            var p = video.play();
            if (p && typeof p.catch === 'function') {
                p.catch(function () {
                    setTimeout(function () {
                        video.play().catch(function () {});
                    }, 30);
                });
            }
        };
        tryPlay();
    }

    function loadAndPlay(video, src) {
        if (!src) return;
        var normalized = normalizeSignVideoSrc(src);
        var needLoad = video.dataset.unisofSignSrc !== normalized;
        if (needLoad) {
            video.dataset.unisofSignSrc = normalized;
            applyVideoUrl(video, normalized);
            video.load();
            video.addEventListener(
                'error',
                function onceErr() {
                    video.removeEventListener('error', onceErr);
                    console.warn(
                        '[UNISOF LSV] Error al cargar el video:',
                        normalized,
                        '— Red 404 o códec: use H.264, no HEVC. Ver /lsv-test.html y videos/lsv/README.txt'
                    );
                },
                { once: true }
            );
        }
        /* load() es asíncrono: play() inmediato suele dejar el fotograma en negro hasta que hay datos. */
        function whenReady() {
            playSignVideo(video);
        }
        if (!needLoad && video.readyState >= 2) {
            whenReady();
            return;
        }
        var token = (parseInt(video.dataset.unisofPlayGen, 10) || 0) + 1;
        video.dataset.unisofPlayGen = String(token);
        var started = false;
        function tryStart() {
            if (started) return;
            if (parseInt(video.dataset.unisofPlayGen, 10) !== token) return;
            if (video.readyState < 2) return;
            started = true;
            whenReady();
        }
        /* Edge a veces dispara loadeddata antes que canplay; usar ambos */
        video.addEventListener('canplay', tryStart, { once: true });
        video.addEventListener('loadeddata', tryStart, { once: true });
    }

    function showHoverBubble(el) {
        if (!dom.initialized) return;
        const src = el.getAttribute('data-sign-src');
        if (!src) return;
        clearTimeout(hideTimer);
        const size = getSizePx(el) || parseInt(getComputedStyle(document.documentElement).getPropertyValue('--sign-bubble-size'), 10) || 140;
        let pos = (el.getAttribute('data-sign-position') || 'cursor').toLowerCase();
        /* En táctil no hay posición de cursor fiable; anclar debajo del control. */
        if (useSignTouchSequence() && pos === 'cursor') {
            pos = 'element';
        }
        currentSrc = src;
        dom.wrap.style.setProperty('--sign-bubble-size', size + 'px');
        if (pos === 'element') {
            positionBubbleOnElement(dom.wrap, el, size);
        } else if (pos === 'element-top') {
            positionBubbleAboveElement(dom.wrap, el, size);
        } else if (pos === 'element-right') {
            positionBubbleRightOfElement(dom.wrap, el, size);
        } else if (pos === 'element-left') {
            positionBubbleLeftOfElement(dom.wrap, el, size);
        } else {
            dom.wrap.removeAttribute('data-anchor');
            positionBubbleNearCursor(dom.wrap, size);
        }
        /* Visible antes de load/play: Edge a veces no pinta ni ejecuta play() si el ancestro sigue visibility:hidden */
        dom.wrap.classList.add('is-visible');
        loadAndPlay(dom.video, src);
        function nudgePlay() {
            if (currentSrc !== src || !dom.wrap.classList.contains('is-visible')) return;
            playSignVideo(dom.video);
        }
        requestAnimationFrame(nudgePlay);
        setTimeout(nudgePlay, 50);
        setTimeout(nudgePlay, 250);

        function repositionHover() {
            if (currentSrc !== src || !dom.wrap.classList.contains('is-visible')) return;
            if (pos === 'element') positionBubbleOnElement(dom.wrap, el, size);
            else if (pos === 'element-top') positionBubbleAboveElement(dom.wrap, el, size);
            else if (pos === 'element-right') positionBubbleRightOfElement(dom.wrap, el, size);
            else if (pos === 'element-left') positionBubbleLeftOfElement(dom.wrap, el, size);
            else positionBubbleNearCursor(dom.wrap, size);
        }
        dom.video.addEventListener('loadedmetadata', repositionHover, { once: true });
        if (dom.video.readyState >= 1) {
            requestAnimationFrame(repositionHover);
        }
        requestAnimationFrame(repositionHover);
    }

    function scheduleHideBubble() {
        clearTimeout(hideTimer);
        hideTimer = setTimeout(function () {
            dom.wrap.classList.remove('is-visible');
            dom.wrap.classList.remove('unisof-sign-bubble-wrap--interactive');
            if (dom.video) {
                dom.video.pause();
            }
            currentSrc = '';
        }, 120);
    }

    function closeTouchSignPreview() {
        touchArmedEl = null;
        clearTimeout(hideTimer);
        hideTimer = null;
        if (dom.wrap) {
            dom.wrap.classList.remove('unisof-sign-bubble-wrap--interactive');
            dom.wrap.classList.remove('is-visible');
        }
        if (dom.video) {
            dom.video.pause();
        }
        currentSrc = '';
    }

    function openTouchSignPreview(el) {
        touchArmedEl = el;
        showHoverBubble(el);
        if (dom.wrap) {
            dom.wrap.classList.add('unisof-sign-bubble-wrap--interactive');
        }
    }

    function openMobilePanel(el) {
        if (!dom.initialized) return;
        const src = el.getAttribute('data-sign-src');
        if (!src) return;
        const label = el.getAttribute('data-sign-label') || el.getAttribute('aria-label') || 'Lengua de señas';
        dom.panelTitle.textContent = label;
        dom.backdrop.classList.add('is-open');
        dom.panel.classList.add('is-open');
        dom.backdrop.setAttribute('aria-hidden', 'false');
        loadAndPlay(dom.panelVideo, src);
        function nudgePanelPlay() {
            if (!dom.panel.classList.contains('is-open')) return;
            playSignVideo(dom.panelVideo);
        }
        requestAnimationFrame(nudgePanelPlay);
        setTimeout(nudgePanelPlay, 50);
        setTimeout(nudgePanelPlay, 250);
        const closeBtn = dom.panel.querySelector('.unisof-sign-mobile-close');
        if (closeBtn) closeBtn.focus();
    }

    function closeMobilePanel() {
        if (!dom.initialized) return;
        dom.backdrop.classList.remove('is-open');
        dom.panel.classList.remove('is-open');
        dom.backdrop.setAttribute('aria-hidden', 'true');
        if (dom.panelVideo) {
            dom.panelVideo.pause();
        }
    }

    function onDocumentKeydown(e) {
        if (e.key === 'Escape') {
            closeMobilePanel();
            if (touchArmedEl) {
                closeTouchSignPreview();
            }
        }
    }

    /** Clic en fase captura: modo táctil solo. */
    function onGlobalTouchSignClickCapture(e) {
        if (!dom.initialized || !useSignTouchSequence()) return;
        if (dom.wrap && (e.target === dom.wrap || dom.wrap.contains(e.target))) {
            return;
        }
        const el = closestSignSrcTrigger(e.target);
        if (el) {
            if (touchArmedEl === el && dom.wrap && dom.wrap.classList.contains('is-visible')) {
                closeTouchSignPreview();
                activateSignTriggerDefault(el, e);
                return;
            }
            e.preventDefault();
            e.stopPropagation();
            openTouchSignPreview(el);
            return;
        }
        if (touchArmedEl && dom.wrap && dom.wrap.classList.contains('is-visible')) {
            closeTouchSignPreview();
        }
    }

    /** Toque fuera del disparador y de la burbuja: cerrar LSV táctil. */
    function onGlobalTouchSignPointerDownCapture(e) {
        if (!dom.initialized || !useSignTouchSequence()) return;
        if (!touchArmedEl || !dom.wrap || !dom.wrap.classList.contains('is-visible')) return;
        if (dom.wrap.contains(e.target)) return;
        const onTrigger = closestSignSrcTrigger(e.target);
        if (onTrigger === touchArmedEl) return;
        closeTouchSignPreview();
    }

    function bindTouchSignDelegation() {
        if (dom.touchSignDelegated) return;
        dom.touchSignDelegated = true;
        document.addEventListener('click', onGlobalTouchSignClickCapture, true);
        document.addEventListener('pointerdown', onGlobalTouchSignPointerDownCapture, true);
    }

    function bindElement(el) {
        if (el.dataset.signBound === '1') return;
        el.dataset.signBound = '1';

        /* Escritorio: hover con puntero no táctil (comportamiento histórico). */
        el.addEventListener('pointerenter', function (e) {
            if (!useSignHoverUi()) return;
            if (e.pointerType === 'touch') return;
            lastMouse.x = e.clientX;
            lastMouse.y = e.clientY;
            showHoverBubble(el);
        });
        el.addEventListener('pointerleave', function (e) {
            if (!useSignHoverUi()) return;
            if (e.pointerType === 'touch') return;
            scheduleHideBubble();
        });
        el.addEventListener('pointermove', function (e) {
            if (!useSignHoverUi()) return;
            if (e.pointerType === 'touch') return;
            lastMouse.x = e.clientX;
            lastMouse.y = e.clientY;
            if (dom.wrap && dom.wrap.classList.contains('is-visible')) {
                const pos = (el.getAttribute('data-sign-position') || 'cursor').toLowerCase();
                if (pos !== 'element' && pos !== 'element-top' && pos !== 'element-right' && pos !== 'element-left') {
                    const size = getSizePx(el) || 140;
                    positionBubbleNearCursor(dom.wrap, size);
                }
            }
        });
    }

    const ADMIN_ROL_LSV_SRC = '/videos/lsv/Administrador.mp4';
    const VENDEDOR_ROL_LSV_SRC = '/videos/lsv/vendedor.mp4';
    const JEFE_COMPRAS_ROL_LSV_SRC = '/videos/lsv/Jefe de compras.mp4';

    /** LSV del rol en #user-rol.admin-user-rol: administrador, vendedor o jefe de compras (misma colocación element-top). */
    function syncAdminUserRolLsvAttributes() {
        const el = document.getElementById('user-rol');
        if (!el || !el.classList || !el.classList.contains('admin-user-rol')) return;
        const t = (el.textContent || '').replace(/\s+/g, ' ').trim().toUpperCase();
        if (t === 'ADMINISTRADOR') {
            el.setAttribute('data-sign-src', ADMIN_ROL_LSV_SRC);
            el.setAttribute('data-sign-position', 'element-top');
            el.setAttribute('data-sign-gap', '28');
            el.setAttribute('data-sign-size', '140');
            el.setAttribute('data-sign-label', 'Rol administrador');
            if (el.dataset.signBound !== '1') {
                bindElement(el);
            }
        } else if (t === 'VENDEDOR') {
            el.setAttribute('data-sign-src', VENDEDOR_ROL_LSV_SRC);
            el.setAttribute('data-sign-position', 'element-top');
            el.setAttribute('data-sign-gap', '28');
            el.setAttribute('data-sign-size', '140');
            el.setAttribute('data-sign-label', 'Rol vendedor');
            if (el.dataset.signBound !== '1') {
                bindElement(el);
            }
        } else if (t === 'JEFE DE COMPRAS') {
            el.setAttribute('data-sign-src', JEFE_COMPRAS_ROL_LSV_SRC);
            el.setAttribute('data-sign-position', 'element-top');
            el.setAttribute('data-sign-gap', '28');
            el.setAttribute('data-sign-size', '140');
            el.setAttribute('data-sign-label', 'Rol jefe de compras');
            if (el.dataset.signBound !== '1') {
                bindElement(el);
            }
        } else {
            el.removeAttribute('data-sign-src');
            el.removeAttribute('data-sign-position');
            el.removeAttribute('data-sign-gap');
            el.removeAttribute('data-sign-size');
            el.removeAttribute('data-sign-label');
        }
    }

    function observeAdminUserRolLsv() {
        const el = document.getElementById('user-rol');
        if (!el || el.dataset.unisofRolLsvObserved === '1') return;
        el.dataset.unisofRolLsvObserved = '1';
        const obs = new MutationObserver(function () {
            syncAdminUserRolLsvAttributes();
        });
        obs.observe(el, { characterData: true, subtree: true, childList: true });
    }

    function init() {
        decorateVolverAlInicioAnchors();
        syncAdminUserRolLsvAttributes();
        observeAdminUserRolLsv();
        const nodes = document.querySelectorAll(SEL);
        if (!nodes.length) return;

        Object.assign(dom, ensureDom());
        dom.initialized = true;
        if (dom.video && !dom.video.hasAttribute('webkit-playsinline')) {
            dom.video.setAttribute('webkit-playsinline', '');
        }
        if (dom.panelVideo && !dom.panelVideo.hasAttribute('webkit-playsinline')) {
            dom.panelVideo.setAttribute('webkit-playsinline', '');
        }

        nodes.forEach(function (el) {
            bindElement(el);
        });
        bindTouchSignDelegation();

        document.addEventListener('keydown', onDocumentKeydown);

        window.addEventListener(
            'resize',
            function () {
                if (!dom.initialized) return;
                if (!useSignTouchSequence()) {
                    if (touchArmedEl) closeTouchSignPreview();
                    return;
                }
                if (touchArmedEl && dom.wrap && dom.wrap.classList.contains('is-visible')) {
                    showHoverBubble(touchArmedEl);
                    dom.wrap.classList.add('unisof-sign-bubble-wrap--interactive');
                }
            },
            { passive: true }
        );

        if (window.matchMedia) {
            window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', function () {
                if (prefersReducedMotion()) {
                    scheduleHideBubble();
                    closeMobilePanel();
                    closeTouchSignPreview();
                }
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    window.initUnisofSignLanguageBubble = init;
    window.syncUnisofAdminUserRolLsv = syncAdminUserRolLsvAttributes;
})();
