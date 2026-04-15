/**
 * LSV del icono #btn-login en index.html.
 * La burbuja es position:fixed en document.body y se coloca con getBoundingClientRect
 * (evita que quede fuera de vista por layout estrecho del header).
 */
(function () {
    'use strict';

    var DEFAULT_LOGIN_LSV = '/videos/lsv/0409.mp4';
    var FALLBACK_LOGIN_LSV = '/videos/lsv/0409-2.mp4';
    var VISIBLE_CLASS = 'nav-login-lsv-float--visible';

    /** Misma raíz que la página (evita fallos con <base> o proxies). */
    function toAbsoluteMediaUrl(path) {
        if (!path) return path;
        var s = String(path).trim();
        if (/^https?:\/\//i.test(s)) return s;
        if (s.charAt(0) !== '/') s = '/' + s;
        try {
            return new URL(s, window.location.origin).href;
        } catch (e) {
            return s;
        }
    }

    function normalizeLsvVideoSrc(src) {
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
        return (
            '/' +
            parts
                .map(function (seg) {
                    return encodeURIComponent(seg);
                })
                .join('/') +
            rest
        );
    }

    function wireVideo(video, candidateList) {
        if (!video) return;
        video.setAttribute('muted', '');
        video.setAttribute('playsinline', '');
        video.setAttribute('webkit-playsinline', '');
        video.muted = true;
        try {
            video.defaultMuted = true;
        } catch (e) {
            /* ignore */
        }
        var list = candidateList && candidateList.length ? candidateList : [DEFAULT_LOGIN_LSV, FALLBACK_LOGIN_LSV];
        function tryAt(index) {
            if (index >= list.length) return;
            var logical = list[index];
            var path = toAbsoluteMediaUrl(normalizeLsvVideoSrc(logical));
            var loadDone = false;
            function cleanup() {
                video.removeEventListener('error', onErr);
                video.removeEventListener('loadeddata', onOk);
                video.removeEventListener('loadedmetadata', onOk);
                video.removeEventListener('canplay', onOk);
            }
            function onErr() {
                cleanup();
                tryAt(index + 1);
            }
            function onOk() {
                if (loadDone) return;
                loadDone = true;
                cleanup();
            }
            video.addEventListener('error', onErr);
            video.addEventListener('loadeddata', onOk, { once: true });
            video.addEventListener('loadedmetadata', onOk, { once: true });
            video.addEventListener('canplay', onOk, { once: true });
            video.setAttribute('data-unisof-lsv-logical', logical);
            video.src = path;
            try {
                video.load();
            } catch (e) {
                cleanup();
                tryAt(index + 1);
            }
        }
        tryAt(0);
    }

    function ensureFloatEl() {
        var el = document.getElementById('nav-login-lsv-float');
        if (el) return el;
        el = document.createElement('div');
        el.id = 'nav-login-lsv-float';
        el.className = 'nav-login-lsv-float';
        el.setAttribute('aria-hidden', 'true');
        el.innerHTML =
            '<div class="nav-login-lsv-float-ring">' +
            '<video id="nav-login-lsv-video" class="nav-login-lsv-float-video" playsinline webkit-playsinline muted loop preload="auto" tabindex="-1"></video>' +
            '</div>';
        document.body.appendChild(el);
        return el;
    }

    function positionFloat(btn, floatEl) {
        var r = btn.getBoundingClientRect();
        var maxSize = Math.min(140, window.innerWidth - 24, window.innerHeight - 24);
        var size = Math.max(72, maxSize);
        var gap = 12;
        var x = r.left - size - gap;
        var y = r.top + r.height / 2 - size / 2;
        if (x < 8) {
            x = r.right + gap;
        }
        if (x + size > window.innerWidth - 8) {
            x = Math.max(8, window.innerWidth - size - 8);
        }
        y = Math.max(8, Math.min(y, window.innerHeight - size - 8));
        floatEl.style.left = x + 'px';
        floatEl.style.top = y + 'px';
        floatEl.style.width = size + 'px';
        floatEl.style.height = size + 'px';
    }

    function init() {
        var btn = document.getElementById('btn-login');
        if (!btn) return;

        var floatEl = ensureFloatEl();
        var video = document.getElementById('nav-login-lsv-video');
        if (!video) return;

        var fromHtml = (btn.getAttribute('data-nav-lsv-src') || '').trim();
        var primary = fromHtml || DEFAULT_LOGIN_LSV;
        var candidates = primary === FALLBACK_LOGIN_LSV ? [primary] : [primary, FALLBACK_LOGIN_LSV];
        wireVideo(video, candidates);

        function isBtnHidden() {
            if (!btn.parentNode) return true;
            if (btn.style.display === 'none') return true;
            try {
                return window.getComputedStyle(btn).display === 'none';
            } catch (e) {
                return false;
            }
        }

        /** Salta fotogramas negros al inicio del clip (mismo síntoma que “burbuja negra”). */
        function seekPastBlackLeader() {
            try {
                if (video.readyState < 1) return;
                var d = video.duration;
                if (!isFinite(d) || d <= 0.25) return;
                var t = Math.min(0.22, Math.max(0.1, d * 0.05));
                if (video.currentTime < 0.08) {
                    video.currentTime = t;
                }
            } catch (e) {
                /* ignore */
            }
        }

        function playPreview() {
            video.muted = true;
            try {
                video.defaultMuted = true;
            } catch (e) {
                /* ignore */
            }
            try {
                video.playsInline = true;
            } catch (e) {
                /* ignore */
            }
            function kick() {
                seekPastBlackLeader();
                var p = video.play();
                if (p && typeof p.then === 'function') {
                    p.catch(function () {});
                }
            }
            kick();
            requestAnimationFrame(kick);
            setTimeout(kick, 40);
            setTimeout(kick, 160);
            if (video.readyState < 2) {
                var onReady = function () {
                    video.removeEventListener('canplay', onReady);
                    video.removeEventListener('loadeddata', onReady);
                    kick();
                };
                video.addEventListener('canplay', onReady, { once: true });
                video.addEventListener('loadeddata', onReady, { once: true });
            }
        }

        function pausePreview() {
            video.pause();
            try {
                var d = video.duration;
                if (isFinite(d) && d > 0.25) {
                    video.currentTime = Math.min(0.22, Math.max(0.1, d * 0.05));
                } else {
                    video.currentTime = 0;
                }
            } catch (e) {
                /* ignore */
            }
        }

        var hideTimer = null;
        var HIDE_DELAY_MS = 220;

        function cancelScheduledHide() {
            if (hideTimer) {
                clearTimeout(hideTimer);
                hideTimer = null;
            }
        }

        function scheduleHide() {
            cancelScheduledHide();
            hideTimer = setTimeout(function () {
                hideTimer = null;
                hide();
            }, HIDE_DELAY_MS);
        }

        function leaveBtnTowardBubble(e) {
            var rt = e.relatedTarget;
            return rt && floatEl.contains(rt);
        }

        function show() {
            if (isBtnHidden()) return;
            cancelScheduledHide();
            positionFloat(btn, floatEl);
            floatEl.classList.add(VISIBLE_CLASS);
            floatEl.setAttribute('aria-hidden', 'false');
            if (floatEl.style.display === 'none') floatEl.style.display = '';
            /* Dos frames: el vídeo a veces no pinta hasta que el padre ya es visible (opacity/visibility). */
            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    positionFloat(btn, floatEl);
                    playPreview();
                });
            });
        }

        function hide() {
            cancelScheduledHide();
            floatEl.classList.remove(VISIBLE_CLASS);
            floatEl.setAttribute('aria-hidden', 'true');
            pausePreview();
        }

        function onEnterBtn() {
            if (isBtnHidden()) return;
            cancelScheduledHide();
            show();
        }

        function onLeaveBtn(e) {
            if (e.pointerType === 'touch') return;
            if (leaveBtnTowardBubble(e)) return;
            scheduleHide();
        }

        btn.addEventListener('mouseenter', onEnterBtn);
        btn.addEventListener('mouseleave', onLeaveBtn);
        btn.addEventListener('pointerenter', function (e) {
            if (e.pointerType === 'touch') return;
            onEnterBtn();
        });
        btn.addEventListener('pointerleave', onLeaveBtn);

        floatEl.addEventListener('mouseenter', function () {
            cancelScheduledHide();
        });
        floatEl.addEventListener('mouseleave', function (e) {
            var rt = e.relatedTarget;
            if (rt && (btn.contains(rt) || btn === rt)) return;
            scheduleHide();
        });

        function reflowIfVisible() {
            if (floatEl.classList.contains(VISIBLE_CLASS) && !isBtnHidden()) {
                positionFloat(btn, floatEl);
            }
        }
        window.addEventListener('scroll', reflowIfVisible, true);
        window.addEventListener('resize', reflowIfVisible);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
