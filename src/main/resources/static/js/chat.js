/**
 * Widget Nova / artful-robot adaptado a UNISOF.
 * Conecta a POST /api/chat (FAQ por reglas).
 */
(function () {
    'use strict';
    if (typeof document === 'undefined' || !document.body) return;

    var API_CHAT = '/api/chat';
    var AVATAR_SRC = '/images/robot-avatar.png';
    /**
     * LSV del chatbot Nova: clip principal 0409(16).mp4; respaldos si falla la carga.
     */
    var CHAT_LAUNCHER_LSV_CANDIDATES = [
        '/videos/lsv/0409(16).mp4',
        '/videos/lsv/0409-16.mp4',
        '/videos/lsv/0409.mp4',
        '/videos/lsv/0409-2.mp4'
    ];

    var LSV_ROW_HOVER_CLASS = 'chat-widget-launcher-row--lsv-hover';

    /**
     * Codifica cada segmento del path (p. ej. 0409(16).mp4 → 0409%2816%29.mp4).
     * Misma lógica que sign-language-bubble.js para paréntesis en la URL.
     */
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

    function getTime() {
        return new Date().toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    }

    var welcomeMsg =
        '¡Hola! Soy Nova, tu asistente UNISOF. Escribe una pregunta o palabra clave (por ejemplo: pedidos, contraseña, carrito). ✨';

    var widgetHtml =
        '<div class="chat-widget" id="chat-widget">' +
        '  <div class="chat-widget-launcher-row" id="chat-widget-launcher-row">' +
        '  <div class="chat-widget-launcher-lsv-stack" id="chat-widget-launcher-lsv-stack" aria-hidden="true">' +
        '    <div class="chat-widget-launcher-lsv-pop">' +
        '      <video id="chat-widget-launcher-lsv-video" class="chat-widget-launcher-lsv-video" playsinline webkit-playsinline muted loop preload="metadata" tabindex="-1"></video>' +
        '    </div>' +
        '  </div>' +
        '  <button type="button" class="chat-widget-launcher" id="chat-widget-btn" aria-label="Abrir asistente Nova">' +
        '    <span class="chat-widget-launcher-cloud">' +
        '      <img src="' +
        AVATAR_SRC +
        '" alt="" class="chat-widget-launcher-avatar" width="52" height="52" loading="lazy" />' +
        '    </span>' +
        '  </button>' +
        '  </div>' +
        '  <div class="chat-widget-panel" id="chat-widget-panel" role="dialog" aria-label="Asistente Nova">' +
        '    <header class="chat-nova-header">' +
        '      <div class="chat-nova-avatar-wrap">' +
        '        <img src="' +
        AVATAR_SRC +
        '" alt="Nova AI" class="chat-nova-avatar" width="44" height="44" loading="lazy" />' +
        '        <span class="chat-nova-online" aria-hidden="true"></span>' +
        '      </div>' +
        '      <div class="chat-nova-title-block">' +
        '        <div class="chat-nova-title-row">' +
        '          <h2 class="chat-nova-title">Nova AI</h2>' +
        '          <span class="chat-nova-sparkle" aria-hidden="true">✦</span>' +
        '        </div>' +
        '        <p class="chat-nova-subtitle">Siempre en línea • Asistente UNISOF</p>' +
        '      </div>' +
        '      <div class="chat-nova-badge" aria-hidden="true">' +
        '        <span class="chat-nova-badge-icon">🤖</span>' +
        '        <span class="chat-nova-badge-ver">v2.0</span>' +
        '      </div>' +
        '      <button type="button" class="chat-widget-close" id="chat-widget-close" aria-label="Cerrar">×</button>' +
        '    </header>' +
        '    <div class="chat-nova-messages scrollbar-thin" id="chat-widget-messages"></div>' +
        '    <footer class="chat-nova-footer">' +
        '      <form class="chat-nova-form" id="chat-widget-form">' +
        '        <div class="chat-nova-input-shell gradient-border-nova">' +
        '          <input type="text" id="chat-widget-input" class="chat-nova-input" placeholder="Escribe tu mensaje..." autocomplete="off" />' +
        '          <button type="submit" class="chat-nova-send" id="chat-widget-send" aria-label="Enviar">' +
        '            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m22 2-7 20-4-9-9-4Z"/><path d="M22 2 11 13"/></svg>' +
        '          </button>' +
        '        </div>' +
        '      </form>' +
        '      <p class="chat-nova-disclaimer">Nova UNISOF • Con IA (OpenAI) si está configurada; si no, guía FAQ</p>' +
        '    </footer>' +
        '  </div>' +
        '</div>';

    function appendUserMessage(container, text) {
        var row = document.createElement('div');
        row.className = 'chat-msg-row chat-msg-row--user';
        row.innerHTML =
            '<div class="chat-msg-col">' +
            '  <div class="chat-msg-bubble chat-msg-bubble--user">' +
            escapeHtml(text) +
            '</div>' +
            '  <p class="chat-msg-time">' +
            escapeHtml(getTime()) +
            '</p>' +
            '</div>' +
            '<div class="chat-msg-user-badge" aria-hidden="true">Tú</div>';
        container.appendChild(row);
        scrollToBottom(container);
    }

    function appendBotMessage(container, text) {
        var row = document.createElement('div');
        row.className = 'chat-msg-row chat-msg-row--bot';
        row.innerHTML =
            '<div class="chat-msg-avatar-wrap"><img src="' +
            AVATAR_SRC +
            '" alt="" width="34" height="34" loading="lazy" /></div>' +
            '<div class="chat-msg-col">' +
            '  <div class="chat-msg-bubble chat-msg-bubble--bot gradient-border-nova">' +
            escapeHtml(text) +
            '</div>' +
            '  <p class="chat-msg-time">' +
            escapeHtml(getTime()) +
            '</p>' +
            '</div>';
        container.appendChild(row);
        scrollToBottom(container);
    }

    function escapeHtml(s) {
        if (!s) return '';
        var div = document.createElement('div');
        div.textContent = s;
        return div.innerHTML;
    }

    function showTyping(container) {
        hideTyping(container);
        var row = document.createElement('div');
        row.className = 'chat-typing-row';
        row.id = 'chat-typing-row';
        row.innerHTML =
            '<div class="chat-msg-avatar-wrap"><img src="' +
            AVATAR_SRC +
            '" alt="" width="34" height="34" loading="lazy" /></div>' +
            '<div class="chat-typing-bubble gradient-border-nova">' +
            '  <div class="chat-typing-dots" aria-hidden="true">' +
            '    <span></span><span></span><span></span>' +
            '  </div>' +
            '</div>';
        container.appendChild(row);
        scrollToBottom(container);
    }

    function hideTyping(container) {
        var el = document.getElementById('chat-typing-row');
        if (el && el.parentNode === container) {
            container.removeChild(el);
        }
    }

    function scrollToBottom(el) {
        el.scrollTop = el.scrollHeight;
    }

    function updateSendState(input, sendBtn) {
        var ok = input && input.value.trim().length > 0;
        if (sendBtn) sendBtn.disabled = !ok;
    }

    function sendMessage(messages, input, sendBtn, form) {
        var text = input && input.value ? input.value.trim() : '';
        if (!text) return;

        input.value = '';
        updateSendState(input, sendBtn);
        appendUserMessage(messages, text);
        showTyping(messages);
        if (sendBtn) sendBtn.disabled = true;
        if (input) input.disabled = true;

        fetch(API_CHAT, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensaje: text }),
            credentials: 'include'
        })
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                hideTyping(messages);
                appendBotMessage(messages, data.respuesta || 'No pude obtener una respuesta.');
            })
            .catch(function () {
                hideTyping(messages);
                appendBotMessage(messages, 'Error de conexión. Intenta de nuevo.');
            })
            .finally(function () {
                if (input) input.disabled = false;
                updateSendState(input, sendBtn);
                if (input) input.focus();
            });
    }

    function init() {
        if (document.getElementById('chat-widget')) return;
        document.body.insertAdjacentHTML('beforeend', widgetHtml);

        var btn = document.getElementById('chat-widget-btn');
        var launcherRow = document.getElementById('chat-widget-launcher-row');
        var launcherLsvVideo = document.getElementById('chat-widget-launcher-lsv-video');
        var panel = document.getElementById('chat-widget-panel');
        var closeBtn = document.getElementById('chat-widget-close');
        var form = document.getElementById('chat-widget-form');
        var input = document.getElementById('chat-widget-input');
        var sendBtn = document.getElementById('chat-widget-send');
        var messages = document.getElementById('chat-widget-messages');

        function wireChatLsvVideo(video) {
            if (!video) return;
            video.setAttribute('muted', '');
            video.setAttribute('playsinline', '');
            video.setAttribute('webkit-playsinline', '');
            video.muted = true;
            var list = CHAT_LAUNCHER_LSV_CANDIDATES;
            function tryAt(index) {
                if (index >= list.length) {
                    return;
                }
                var logical = list[index];
                var path = normalizeLsvVideoSrc(logical);
                var loadDone = false;
                function cleanup() {
                    video.removeEventListener('error', onErr);
                    video.removeEventListener('loadeddata', onOk);
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
                video.addEventListener('canplay', onOk, { once: true });
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

        wireChatLsvVideo(launcherLsvVideo);

        function pauseLauncherHoverPreview() {
            if (!launcherLsvVideo) return;
            launcherLsvVideo.pause();
            try {
                launcherLsvVideo.currentTime = 0;
            } catch (e) {
                /* ignore */
            }
        }

        function playLauncherHoverPreview() {
            if (!launcherLsvVideo) return;
            if (panel && panel.classList.contains('open')) return;
            launcherLsvVideo.muted = true;
            try {
                launcherLsvVideo.playsInline = true;
            } catch (e) {
                /* ignore */
            }
            function doPlay() {
                var p = launcherLsvVideo.play();
                if (p && typeof p.then === 'function') {
                    p.catch(function () {
                        /* autoplay / política del navegador */
                    });
                }
            }
            doPlay();
            requestAnimationFrame(doPlay);
            setTimeout(doPlay, 60);
            setTimeout(doPlay, 220);
            if (launcherLsvVideo.readyState < 2) {
                var onReady = function () {
                    launcherLsvVideo.removeEventListener('canplay', onReady);
                    launcherLsvVideo.removeEventListener('loadeddata', onReady);
                    doPlay();
                };
                launcherLsvVideo.addEventListener('canplay', onReady, { once: true });
                launcherLsvVideo.addEventListener('loadeddata', onReady, { once: true });
            }
        }

        appendBotMessage(messages, welcomeMsg);
        updateSendState(input, sendBtn);

        function openPanel() {
            if (launcherRow) launcherRow.classList.remove(LSV_ROW_HOVER_CLASS);
            pauseLauncherHoverPreview();
            if (panel) panel.classList.add('open');
            var w = document.getElementById('chat-widget');
            if (w) w.classList.add('chat-panel-open');
            if (input) setTimeout(function () { input.focus(); }, 120);
        }
        function closePanel() {
            if (panel) panel.classList.remove('open');
            var w = document.getElementById('chat-widget');
            if (w) w.classList.remove('chat-panel-open');
        }
        function togglePanel() {
            if (panel && panel.classList.contains('open')) closePanel();
            else openPanel();
        }

        if (launcherRow && launcherLsvVideo) {
            launcherRow.addEventListener('pointerenter', function () {
                launcherRow.classList.add(LSV_ROW_HOVER_CLASS);
                playLauncherHoverPreview();
            });
            launcherRow.addEventListener('pointerleave', function () {
                launcherRow.classList.remove(LSV_ROW_HOVER_CLASS);
                pauseLauncherHoverPreview();
            });
        }

        if (btn) btn.addEventListener('click', togglePanel);
        if (closeBtn) closeBtn.addEventListener('click', closePanel);
        if (input) {
            input.addEventListener('input', function () {
                updateSendState(input, sendBtn);
            });
        }
        if (form) {
            form.addEventListener('submit', function (e) {
                e.preventDefault();
                sendMessage(messages, input, sendBtn, form);
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
