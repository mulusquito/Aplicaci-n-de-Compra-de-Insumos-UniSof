/**
 * UNISOF — Burbuja de lengua de señas (MP4 en hover / panel móvil).
 *
 * Uso en HTML:
 *   <button data-sign-src="/videos/lsv/ejemplo.mp4" data-sign-label="Descripción en LSV">
 *     Texto del botón
 *   </button>
 *
 * Atributos opcionales:
 *   data-sign-position="cursor" | "element" | "element-right" — por defecto cursor
 *   data-sign-size="160" — diámetro del círculo en px (hover)
 *
 * Requisitos del video: muted + sin audio para autoplay en navegadores.
 */
(function () {
    if (typeof document === 'undefined') return;

    const WRAP_CLASS = 'unisof-sign-bubble-wrap';
    const SEL = '[data-sign-src]';

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
        var anyFine = window.matchMedia('(any-pointer: fine)').matches;
        var fine = window.matchMedia('(pointer: fine)').matches;
        var coarse = window.matchMedia('(pointer: coarse)').matches;
        if (fine || anyFine) return true;
        if (coarse && !anyFine) return false;
        return true;
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

    const dom = { initialized: false };
    let currentSrc = '';
    let hideTimer = null;
    let lastMouse = { x: 0, y: 0 };

    function getSizePx(el) {
        const s = el.getAttribute('data-sign-size');
        const n = s ? parseInt(s, 10) : NaN;
        return Number.isFinite(n) && n > 40 && n < 400 ? n : null;
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

    function rectsOverlap(a, b) {
        return !(a.right <= b.left || a.left >= b.right || a.bottom <= b.top || a.top >= b.bottom);
    }

    /** A la derecha del elemento; tamaño según el vídeo. Si no cabe, a la izquierda. */
    function positionBubbleRightOfElement(wrap, el, size) {
        const r = el.getBoundingClientRect();
        const pad = 12;
        const gap = 28;
        const box = bubbleBox(wrap, size);
        const w = box.w;
        const h = box.h;
        let x = r.right + gap;
        let y = r.top + r.height / 2 - h / 2;
        if (x + w > window.innerWidth - pad) {
            x = r.left - w - gap;
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
                x = r.right + gap;
                if (x + br.width > window.innerWidth - pad) {
                    x = r.left - br.width - gap;
                }
                x = Math.max(pad, Math.min(x, window.innerWidth - br.width - pad));
                wrap.style.left = x + 'px';
            }
        });
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
        const pos = (el.getAttribute('data-sign-position') || 'cursor').toLowerCase();
        currentSrc = src;
        dom.wrap.style.setProperty('--sign-bubble-size', size + 'px');
        if (pos === 'element') {
            positionBubbleOnElement(dom.wrap, el, size);
        } else if (pos === 'element-right') {
            positionBubbleRightOfElement(dom.wrap, el, size);
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
            else if (pos === 'element-right') positionBubbleRightOfElement(dom.wrap, el, size);
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
            if (dom.video) {
                dom.video.pause();
            }
            currentSrc = '';
        }, 120);
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
        }
    }

    function bindElement(el, hoverMode) {
        if (el.dataset.signBound === '1') return;
        el.dataset.signBound = '1';

        if (hoverMode) {
            /* pointer*: mejor soporte en Edge con pantallas táctiles / Pen que solo mouseenter */
            el.addEventListener('pointerenter', function (e) {
                if (e.pointerType === 'touch') return;
                showHoverBubble(el);
            });
            el.addEventListener('pointerleave', function (e) {
                if (e.pointerType === 'touch') return;
                scheduleHideBubble();
            });
            el.addEventListener('pointermove', function (e) {
                if (e.pointerType === 'touch') return;
                lastMouse.x = e.clientX;
                lastMouse.y = e.clientY;
                if (dom.wrap && dom.wrap.classList.contains('is-visible')) {
                    const pos = (el.getAttribute('data-sign-position') || 'cursor').toLowerCase();
                    if (pos !== 'element' && pos !== 'element-right') {
                        const size = getSizePx(el) || 140;
                        positionBubbleNearCursor(dom.wrap, size);
                    }
                }
            });
        } else {
            el.setAttribute('role', el.getAttribute('role') || 'button');
            if (!el.hasAttribute('tabindex')) {
                el.setAttribute('tabindex', '0');
            }
            const open = function (e) {
                if (e.type === 'keydown' && e.key !== 'Enter' && e.key !== ' ') return;
                if (e.type === 'keydown' && e.key === ' ') e.preventDefault();
                openMobilePanel(el);
            };
            el.addEventListener('click', open);
            el.addEventListener('keydown', open);
        }
    }

    function init() {
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

        const hoverMode = useHoverBubble();
        nodes.forEach(function (el) {
            bindElement(el, hoverMode);
        });

        document.addEventListener('keydown', onDocumentKeydown);

        if (window.matchMedia) {
            window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', function () {
                if (prefersReducedMotion()) {
                    scheduleHideBubble();
                    closeMobilePanel();
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
})();
