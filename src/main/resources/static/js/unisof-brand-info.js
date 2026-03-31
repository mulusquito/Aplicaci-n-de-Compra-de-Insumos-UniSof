/**
 * Clic en el logo UNISOF (sidebar admin o barra principal): modal Quiénes somos / Misión / Visión.
 */
(function () {
    if (typeof document === 'undefined') return;

    var SELECTOR = 'a.admin-logo, header.navbar > a.logo, a.logo.auth-logo';

    function ensureModal() {
        if (document.getElementById('unisof-brand-overlay')) return;

        var overlay = document.createElement('div');
        overlay.id = 'unisof-brand-overlay';
        overlay.className = 'unisof-brand-overlay is-hidden';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-modal', 'true');
        overlay.setAttribute('aria-labelledby', 'unisof-brand-dialog-title');

        var chev =
            '<svg class="chevron" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">' +
            '<path d="m6 9 6 6 6-6"></path></svg>';

        overlay.innerHTML =
            '<div class="unisof-brand-dialog" role="document">' +
            '<button type="button" class="unisof-brand-close" aria-label="Cerrar">&times;</button>' +
            '<div class="unisof-brand-dialog-header">' +
            '<span class="unisof-brand-dialog-badge" aria-hidden="true">U</span>' +
            '<h2 id="unisof-brand-dialog-title" tabindex="-1">UNISOF</h2>' +
            '</div>' +
            '<div class="unisof-brand-dialog-body">' +
            '<p class="unisof-brand-dialog-hint">Pulse cada opción para desplegar u ocultar su contenido. Cada sección se abre y cierra por separado.</p>' +
            '<div class="unisof-brand-dropdown-stack">' +
            '<div class="dropdown-catalogo unisof-brand-dd">' +
            '<button type="button" class="btn-secondary btn-catalogo" aria-expanded="false" aria-controls="unisof-dd-quienes" id="unisof-dd-btn-quienes">' +
            'Quiénes somos' +
            chev +
            '</button>' +
            '<div class="dropdown-catalogo-menu unisof-brand-dd-menu" id="unisof-dd-quienes" role="region" aria-labelledby="unisof-dd-btn-quienes">' +
            '<div class="unisof-brand-dd-text"><p>UNISOF es una compañía del sector textil y confección orientada a la gestión integral de insumos, compras y control de inventario. Unimos en una sola plataforma las necesidades de equipos administrativos, de ventas y de compras para que la información fluya con claridad y seguridad.</p></div>' +
            '</div>' +
            '</div>' +
            '<div class="dropdown-catalogo unisof-brand-dd">' +
            '<button type="button" class="btn-secondary btn-catalogo" aria-expanded="false" aria-controls="unisof-dd-mision" id="unisof-dd-btn-mision">' +
            'Misión' +
            chev +
            '</button>' +
            '<div class="dropdown-catalogo-menu unisof-brand-dd-menu" id="unisof-dd-mision" role="region" aria-labelledby="unisof-dd-btn-mision">' +
            '<div class="unisof-brand-dd-text"><p>Facilitar el día a día de nuestros usuarios con herramientas simples y confiables para consultar pedidos, administrar personal, compras e inventario, reduciendo errores operativos y acortando los tiempos de respuesta frente al cliente y a la producción.</p></div>' +
            '</div>' +
            '</div>' +
            '<div class="dropdown-catalogo unisof-brand-dd">' +
            '<button type="button" class="btn-secondary btn-catalogo" aria-expanded="false" aria-controls="unisof-dd-vision" id="unisof-dd-btn-vision">' +
            'Visión' +
            chev +
            '</button>' +
            '<div class="dropdown-catalogo-menu unisof-brand-dd-menu" id="unisof-dd-vision" role="region" aria-labelledby="unisof-dd-btn-vision">' +
            '<div class="unisof-brand-dd-text"><p>Ser un aliado tecnológico de referencia para empresas que buscan orden, trazabilidad y crecimiento sostenible en su cadena de suministro textil, consolidando procesos digitales que impulsen la competitividad del sector.</p></div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '<div class="unisof-brand-dialog-footer">' +
            '<a href="/" class="unisof-brand-home-link">Ir al inicio</a>' +
            '</div>' +
            '</div>';

        document.body.appendChild(overlay);

        var dialog = overlay.querySelector('.unisof-brand-dialog');
        overlay.querySelector('.unisof-brand-close').addEventListener('click', closeModal);
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) closeModal();
        });
        if (dialog) {
            dialog.addEventListener('click', function (e) {
                e.stopPropagation();
            });
        }

        overlay.querySelectorAll('.unisof-brand-dialog .dropdown-catalogo.unisof-brand-dd').forEach(function (wrap) {
            var btn = wrap.querySelector('.btn-catalogo');
            if (!btn) return;
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                e.stopPropagation();
                var open = wrap.classList.toggle('open');
                btn.setAttribute('aria-expanded', open ? 'true' : 'false');
            });
        });
    }

    function openModal() {
        ensureModal();
        var overlay = document.getElementById('unisof-brand-overlay');
        if (!overlay) return;
        overlay.querySelectorAll('.unisof-brand-dialog .dropdown-catalogo.unisof-brand-dd').forEach(function (wrap) {
            wrap.classList.remove('open');
            var b = wrap.querySelector('.btn-catalogo');
            if (b) b.setAttribute('aria-expanded', 'false');
        });
        overlay.classList.remove('is-hidden');
        document.body.classList.add('unisof-brand-modal-open');
        var title = document.getElementById('unisof-brand-dialog-title');
        if (title) title.focus();
    }

    function closeModal() {
        var overlay = document.getElementById('unisof-brand-overlay');
        if (!overlay) return;
        overlay.classList.add('is-hidden');
        document.body.classList.remove('unisof-brand-modal-open');
    }

    document.addEventListener('click', function (e) {
        if (e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;
        var a = e.target.closest(SELECTOR);
        if (!a || !document.body.contains(a)) return;
        e.preventDefault();
        e.stopPropagation();
        openModal();
    });

    document.addEventListener('keydown', function (e) {
        if (e.key !== 'Escape') return;
        var overlay = document.getElementById('unisof-brand-overlay');
        if (overlay && !overlay.classList.contains('is-hidden')) {
            closeModal();
        }
    });
})();
