// Widget de accesibilidad global UNISOF
(function () {
    if (typeof document === 'undefined') return;

    const STORAGE_KEY = 'unisof_a11y_prefs';

    const defaultPrefs = {
        contrast: 'normal',
        fontSize: 'normal',
        fontFamily: 'default',
        theme: 'dark',
        textColor: 'default',
        lang: 'es',
        contentScale: 100,
        fontScale: 100,
        lineHeight: 100,
        letterSpacing: 100
    };

    function loadPrefs() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return { ...defaultPrefs };
            return { ...defaultPrefs, ...JSON.parse(raw) };
        } catch {
            return { ...defaultPrefs };
        }
    }

    function savePrefs(prefs) {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs));
        } catch (_) {}
    }

    const translations = {
        es: {
            'nav.dashboard': 'Dashboard',
            'nav.personal': 'Personal',
            'nav.orders': 'Consultar órdenes de compra',
            'nav.purchases': 'Consultar compras insumos',
            'nav.inventory': 'Consultar inventario',
            'nav.suppliers': 'Proveedores',
            'nav.sales': 'Ventas',
            'nav.clients': 'Clientes',
            'nav.requests': 'Pedidos',
            'title.personal': 'Gestión de personal',
            'title.sales': 'Ventas',
            'title.clients': 'Consultar clientes',
            'title.orders': 'Consultar pedidos',
            'common.logout': 'Cerrar sesión',
            'auth.title': 'Iniciar Sesión',
            'auth.subtitle': 'Ingresa tus credenciales para acceder',
            'auth.user': 'Usuario',
            'auth.password': 'Contraseña',
            'auth.forgot': '¿Olvidaste la contraseña?',
            'auth.submit': 'Iniciar Sesión',
            'auth.back': '← Volver al inicio',
            'auth.loginBack': '← Volver al login',
            'auth.userPlaceholder': 'Ej: admin',
            'auth.passPlaceholder': 'Ej: tu contraseña',
            'home.badge': '★ Nueva Colección 2026',
            'home.catalog': 'Ver Catálogo',
            'home.manual': 'Manual de Usuario',
            'home.manualTitle': 'Abrir manual de usuario (PDF en nueva pestaña)',
            'home.woman': 'Mujer',
            'home.man': 'Hombre',
            'home.catalogTitle': 'Caballeros',
            'home.catalogSubtitle': 'Explora nuestra colección para hombre.',
            'home.footer': '© 2026 UNISOF. Todos los derechos reservados.',
            'home.loginTitle': 'Iniciar Sesión',
            'clients.search': 'Buscar por cédula',
            'clients.searchBtn': 'Buscar',
            'clients.create': 'Crear cliente',
            'clients.listAll': 'Listar todos',
            'clients.name': 'Nombre',
            'clients.cedula': 'Cédula',
            'clients.email': 'Correo',
            'clients.phone': 'Teléfono',
            'clients.actions': 'Acciones',
            'clients.edit': 'Editar',
            'clients.empty': 'No hay clientes. Haz clic en "Crear cliente" para agregar uno.',
            'clients.modalCreate': 'Crear cliente',
            'clients.modalEdit': 'Editar cliente',
            'clients.fullName': 'Nombre completo *',
            'clients.cedulaLabel': 'Cédula *',
            'clients.emailLabel': 'Correo electrónico *',
            'clients.phoneLabel': 'Teléfono',
            'clients.address': 'Dirección',
            'clients.cancel': 'Cancelar',
            'clients.save': 'Guardar',
            'clients.clearFields': 'Limpiar campos',
            'clients.noSelection': 'Ningún cliente seleccionado.',
            'orders.orderNo': 'Nº orden',
            'orders.cedula': 'Cédula',
            'orders.date': 'Fecha',
            'orders.month': 'Mes',
            'orders.searchByNo': 'Buscar',
            'orders.byCedula': 'Por cédula',
            'orders.byDate': 'Por fecha',
            'orders.byMonth': 'Por mes',
            'orders.listAll': 'Listar todas',
            'orders.empty': 'No hay pedidos. Los pedidos se crean desde el carrito en Ventas.',
            'orders.client': 'Cliente',
            'orders.total': 'Total',
            'orders.status': 'Estado',
            'orders.paid': 'Pagado',
            'orders.consultTitle': 'Consultar pedidos',
            'orders.idPlaceholder': 'Número de identificación',
            'personal.register': 'Registrar',
            'personal.manage': 'Gestionar',
            'personal.fullName': 'Nombre completo',
            'personal.userLogin': 'Usuario (para inicio de sesión)',
            'personal.idNumber': 'Número de identificación',
            'personal.email': 'Correo electrónico',
            'personal.cell': 'Celular',
            'personal.password': 'Clave de acceso',
            'personal.confirmPassword': 'Confirmar clave',
            'personal.role': 'Rol',
            'personal.terms': 'Acepto la Política de privacidad y los Términos y condiciones.',
            'personal.clear': 'Limpiar',
            'personal.searchType': 'Tipo de búsqueda',
            'personal.searchByName': 'Por nombre completo',
            'personal.searchById': 'Por número de identificación',
            'personal.searchData': 'Dato a buscar',
            'personal.searchBtn': 'Buscar',
            'personal.listAll': 'Listar todos',
            'personal.updateUser': 'Actualizar usuario',
            'personal.deleteUser': 'Eliminar usuario',
            'personal.noUserSelected': 'Ningún usuario seleccionado.',
            'personal.selectRole': 'Seleccione un rol',
            'personal.vendedor': 'Vendedor',
            'personal.jefeCompras': 'Jefe de compras',
            'personal.administrador': 'Administrador',
            'a11y.title': 'Accesibilidad UNISOF',
            'a11y.toolsTitle': 'Herramientas de Accesibilidad',
            'a11y.contrast': 'Contraste',
            'a11y.contrastNormal': 'Normal',
            'a11y.contrastHigh': 'Alto contraste',
            'a11y.fontSize': 'Tamaño de texto',
            'a11y.fontSizeNormal': 'Normal',
            'a11y.fontSizeLarge': 'Grande',
            'a11y.fontSizeXLarge': 'Muy grande',
            'a11y.fontFamily': 'Tipo de letra',
            'a11y.fontDefault': 'Predeterminada',
            'a11y.fontSans': 'Sin serif (pantalla)',
            'a11y.fontSerif': 'Con serif (lectura)',
            'a11y.theme': 'Fondo de pantalla',
            'a11y.themeDark': 'Oscuro',
            'a11y.themeLight': 'Blanco',
            'a11y.textColor': 'Color del texto',
            'a11y.textDefault': 'Por defecto',
            'a11y.textBlack': 'Negro',
            'a11y.textWhite': 'Blanco',
            'a11y.textBlue': 'Azul oscuro',
            'a11y.lang': 'Idioma',
            'a11y.langEs': 'Español',
            'a11y.langEn': 'English',
            'a11y.visualSection': 'Ajustes visuales',
            'a11y.textSection': 'Texto',
            'a11y.contentScale': 'Escalamiento de contenido',
            'a11y.fontSizeSlider': 'Tamaño de fuente',
            'a11y.lineHeight': 'Altura de línea',
            'a11y.letterSpacing': 'Espaciado de letras'
        },
        en: {
            'nav.dashboard': 'Dashboard',
            'nav.personal': 'Staff',
            'nav.orders': 'View purchase orders',
            'nav.purchases': 'View supply purchases',
            'nav.inventory': 'View inventory',
            'nav.suppliers': 'Suppliers',
            'nav.sales': 'Sales',
            'nav.clients': 'Clients',
            'nav.requests': 'Orders',
            'title.personal': 'Staff management',
            'title.sales': 'Sales',
            'title.clients': 'View clients',
            'title.suppliers': 'Supply suppliers',
            'title.orders': 'View orders',
            'common.logout': 'Log out',
            'auth.title': 'Log in',
            'auth.subtitle': 'Enter your credentials to access',
            'auth.user': 'Username',
            'auth.password': 'Password',
            'auth.forgot': 'Forgot password?',
            'auth.submit': 'Log in',
            'auth.back': '← Back to home',
            'auth.loginBack': '← Back to login',
            'auth.userPlaceholder': 'e.g. admin',
            'auth.passPlaceholder': 'e.g. your password',
            'home.badge': '★ New Collection 2026',
            'home.catalog': 'View Catalog',
            'home.manual': 'User manual',
            'home.manualTitle': 'Open user manual (PDF in new tab)',
            'home.woman': 'Women',
            'home.man': 'Men',
            'home.catalogTitle': 'Men',
            'home.catalogSubtitle': 'Explore our collection for men.',
            'home.footer': '© 2026 UNISOF. All rights reserved.',
            'home.loginTitle': 'Log in',
            'clients.search': 'Search by ID',
            'clients.searchBtn': 'Search',
            'clients.create': 'Create client',
            'clients.listAll': 'List all',
            'clients.name': 'Name',
            'clients.cedula': 'ID',
            'clients.email': 'Email',
            'clients.phone': 'Phone',
            'clients.actions': 'Actions',
            'clients.edit': 'Edit',
            'clients.empty': 'No clients. Click "Create client" to add one.',
            'clients.modalCreate': 'Create client',
            'clients.modalEdit': 'Edit client',
            'clients.fullName': 'Full name *',
            'clients.cedulaLabel': 'ID *',
            'clients.emailLabel': 'Email *',
            'clients.phoneLabel': 'Phone',
            'clients.address': 'Address',
            'clients.cancel': 'Cancel',
            'clients.save': 'Save',
            'clients.clearFields': 'Clear fields',
            'clients.noSelection': 'No client selected.',
            'orders.orderNo': 'Order no.',
            'orders.cedula': 'ID',
            'orders.date': 'Date',
            'orders.month': 'Month',
            'orders.searchByNo': 'Search',
            'orders.byCedula': 'By ID',
            'orders.byDate': 'By date',
            'orders.byMonth': 'By month',
            'orders.listAll': 'List all',
            'orders.empty': 'No orders. Orders are created from the cart in Sales.',
            'orders.client': 'Client',
            'orders.total': 'Total',
            'orders.status': 'Status',
            'orders.paid': 'Paid',
            'orders.consultTitle': 'View orders',
            'orders.idPlaceholder': 'ID number',
            'personal.register': 'Register',
            'personal.manage': 'Manage',
            'personal.fullName': 'Full name',
            'personal.userLogin': 'Username (for login)',
            'personal.idNumber': 'ID number',
            'personal.email': 'Email',
            'personal.cell': 'Phone',
            'personal.password': 'Password',
            'personal.confirmPassword': 'Confirm password',
            'personal.role': 'Role',
            'personal.terms': 'I accept the Privacy Policy and Terms and conditions.',
            'personal.clear': 'Clear',
            'personal.searchType': 'Search type',
            'personal.searchByName': 'By full name',
            'personal.searchById': 'By ID number',
            'personal.searchData': 'Data to search',
            'personal.searchBtn': 'Search',
            'personal.listAll': 'List all',
            'personal.updateUser': 'Update user',
            'personal.deleteUser': 'Delete user',
            'personal.noUserSelected': 'No user selected.',
            'personal.selectRole': 'Select a role',
            'personal.vendedor': 'Seller',
            'personal.jefeCompras': 'Purchasing manager',
            'personal.administrador': 'Administrator',
            'a11y.title': 'UNISOF Accessibility',
            'a11y.toolsTitle': 'Accessibility Tools',
            'a11y.contrast': 'Contrast',
            'a11y.contrastNormal': 'Normal',
            'a11y.contrastHigh': 'High contrast',
            'a11y.fontSize': 'Text size',
            'a11y.fontSizeNormal': 'Normal',
            'a11y.fontSizeLarge': 'Large',
            'a11y.fontSizeXLarge': 'X-Large',
            'a11y.fontFamily': 'Font type',
            'a11y.fontDefault': 'Default',
            'a11y.fontSans': 'Sans serif (screen)',
            'a11y.fontSerif': 'Serif (reading)',
            'a11y.theme': 'Screen background',
            'a11y.themeDark': 'Dark',
            'a11y.themeLight': 'White',
            'a11y.textColor': 'Text color',
            'a11y.textDefault': 'Default',
            'a11y.textBlack': 'Black',
            'a11y.textWhite': 'White',
            'a11y.textBlue': 'Dark blue',
            'a11y.lang': 'Language',
            'a11y.langEs': 'Español',
            'a11y.langEn': 'English',
            'a11y.visualSection': 'Visual settings',
            'a11y.textSection': 'Text',
            'a11y.contentScale': 'Content scaling',
            'a11y.fontSizeSlider': 'Font size',
            'a11y.lineHeight': 'Line height',
            'a11y.letterSpacing': 'Letter spacing'
        }
    };

    function applyLanguage(lang) {
        const dict = translations[lang] || translations.es;
        document.querySelectorAll('[data-i18n-key]').forEach(el => {
            const key = el.getAttribute('data-i18n-key');
            if (key && dict[key] != null) el.textContent = dict[key];
        });
        document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
            const key = el.getAttribute('data-i18n-placeholder');
            if (key && dict[key] != null) el.placeholder = dict[key];
        });
        document.querySelectorAll('[data-i18n-title]').forEach(el => {
            const key = el.getAttribute('data-i18n-title');
            if (key && dict[key] != null) el.title = dict[key];
        });
    }

    function applyPrefs(prefs) {
        const body = document.body;
        const html = document.documentElement;
        if (!body || !html) return;

        body.removeAttribute('data-a11y-contrast');
        body.removeAttribute('data-a11y-theme');
        body.removeAttribute('data-a11y-text-color');
        html.removeAttribute('data-a11y-font-family');
        body.removeAttribute('data-a11y-font-family');

        if (prefs.contrast === 'high') body.setAttribute('data-a11y-contrast', 'high');
        if (prefs.theme === 'light') body.setAttribute('data-a11y-theme', 'light');
        if (prefs.textColor && prefs.textColor !== 'default') body.setAttribute('data-a11y-text-color', prefs.textColor);
        if (prefs.fontFamily === 'sans' || prefs.fontFamily === 'serif') html.setAttribute('data-a11y-font-family', prefs.fontFamily);

        const scale = (prefs.contentScale != null ? prefs.contentScale : 100) / 100;
        const fontScale = (prefs.fontScale != null ? prefs.fontScale : 100) / 100;
        const lineH = (prefs.lineHeight != null ? prefs.lineHeight : 100) / 100;
        const letterS = prefs.letterSpacing != null ? (prefs.letterSpacing - 100) * 0.002 : 0;
        html.style.setProperty('--a11y-content-scale', String(scale));
        html.style.setProperty('--a11y-font-scale', String(fontScale));
        html.style.setProperty('--a11y-line-height', String(lineH));
        html.style.setProperty('--a11y-letter-spacing', letterS + 'em');

        applyLanguage(prefs.lang || 'es');
    }

    function createWidget(prefs) {
        const existing = document.querySelector('.a11y-widget-toggle');
        if (existing) return;

        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className = 'a11y-widget-toggle';
        toggle.setAttribute('aria-label', 'Opciones de accesibilidad');
        toggle.innerHTML = `
            <span class="a11y-toggle-icon" aria-hidden="true">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="30" height="30" fill="none" role="img" aria-label="">
                    <circle cx="32" cy="32" r="30" fill="#007AFF"/>
                    <circle cx="32" cy="21" r="5" fill="#ffffff"/>
                    <path d="M20 30 L44 30" stroke="#ffffff" stroke-width="4" stroke-linecap="round" />
                    <path d="M32 28 L32 44" stroke="#ffffff" stroke-width="4" stroke-linecap="round" />
                    <path d="M32 44 L24 54" stroke="#ffffff" stroke-width="4" stroke-linecap="round" />
                    <path d="M32 44 L40 54" stroke="#ffffff" stroke-width="4" stroke-linecap="round" />
                </svg>
            </span>
            <span class="sr-only">Accesibilidad</span>
        `;

        const panel = document.createElement('div');
        panel.className = 'a11y-widget-panel';
        panel.setAttribute('role', 'dialog');
        panel.setAttribute('aria-label', 'Centro de accesibilidad');
        panel.style.display = 'none';

        panel.innerHTML = `
            <div class="a11y-widget-panel-header">
                <h2 class="a11y-widget-title" data-i18n-key="a11y.toolsTitle">Herramientas de Accesibilidad</h2>
                <div class="a11y-widget-header-actions">
                    <button type="button" class="a11y-widget-reset" aria-label="Restaurar valores por defecto" title="Restaurar valores por defecto">↺</button>
                    <button type="button" class="a11y-widget-close" aria-label="Cerrar">✕</button>
                </div>
            </div>
            <div class="a11y-panel-section">
                <h3 class="a11y-panel-section-title" data-i18n-key="a11y.visualSection">Ajustes visuales</h3>
                <div class="a11y-toggle-grid">
                    <div class="a11y-toggle-group">
                        <span class="a11y-toggle-icon-small" aria-hidden="true">◐</span>
                        <label class="a11y-widget-label" data-i18n-key="a11y.contrast">Contraste</label>
                        <div class="a11y-widget-row" data-a11y-group="contrast">
                            <button type="button" data-value="normal" class="a11y-chip" data-i18n-key="a11y.contrastNormal">Normal</button>
                            <button type="button" data-value="high" class="a11y-chip" data-i18n-key="a11y.contrastHigh">Alto</button>
                        </div>
                    </div>
                    <div class="a11y-toggle-group">
                        <span class="a11y-toggle-icon-small" aria-hidden="true">☀</span>
                        <label class="a11y-widget-label" data-i18n-key="a11y.theme">Fondo</label>
                        <div class="a11y-widget-row" data-a11y-group="theme">
                            <button type="button" data-value="dark" class="a11y-chip" data-i18n-key="a11y.themeDark">Oscuro</button>
                            <button type="button" data-value="light" class="a11y-chip" data-i18n-key="a11y.themeLight">Blanco</button>
                        </div>
                    </div>
                </div>
                <div class="a11y-toggle-group a11y-toggle-group--full">
                    <span class="a11y-toggle-icon-small" aria-hidden="true">A</span>
                    <label class="a11y-widget-label" data-i18n-key="a11y.textColor">Color del texto</label>
                    <div class="a11y-widget-row" data-a11y-group="textColor">
                        <button type="button" data-value="default" class="a11y-chip" data-i18n-key="a11y.textDefault">Por defecto</button>
                        <button type="button" data-value="black" class="a11y-chip" data-i18n-key="a11y.textBlack">Negro</button>
                        <button type="button" data-value="white" class="a11y-chip" data-i18n-key="a11y.textWhite">Blanco</button>
                        <button type="button" data-value="blue" class="a11y-chip" data-i18n-key="a11y.textBlue">Azul</button>
                    </div>
                </div>
            </div>
            <div class="a11y-panel-section">
                <h3 class="a11y-panel-section-title" data-i18n-key="a11y.textSection">Texto</h3>
                <div class="a11y-widget-section">
                    <label class="a11y-widget-label" data-i18n-key="a11y.fontFamily">Tipo de letra</label>
                    <select class="a11y-widget-select" data-a11y-control="fontFamily">
                        <option value="default" data-i18n-key="a11y.fontDefault">Predeterminada</option>
                        <option value="sans" data-i18n-key="a11y.fontSans">Sin serif</option>
                        <option value="serif" data-i18n-key="a11y.fontSerif">Con serif</option>
                    </select>
                </div>
            </div>
            <div class="a11y-widget-section">
                <label class="a11y-widget-label" data-i18n-key="a11y.lang">Idioma</label>
                <select class="a11y-widget-select" data-a11y-control="lang">
                    <option value="es" data-i18n-key="a11y.langEs">Español</option>
                    <option value="en" data-i18n-key="a11y.langEn">English</option>
                </select>
            </div>
        `;

        document.body.appendChild(toggle);
        document.body.appendChild(panel);

        const closeBtn = panel.querySelector('.a11y-widget-close');
        const resetBtn = panel.querySelector('.a11y-widget-reset');
        function openPanel() { panel.style.display = 'block'; applyLanguage(prefs.lang || 'es'); }
        function closePanel() { panel.style.display = 'none'; }
        toggle.addEventListener('click', () => {
            if (panel.style.display === 'block') closePanel();
            else openPanel();
        });
        closeBtn.addEventListener('click', closePanel);
        resetBtn.addEventListener('click', () => {
            const def = { ...defaultPrefs };
            Object.keys(def).forEach(k => { prefs[k] = def[k]; });
            savePrefs(prefs);
            applyPrefs(prefs);
            panel.querySelectorAll('.a11y-chip--active').forEach(c => c.classList.remove('a11y-chip--active'));
            panel.querySelectorAll('[data-a11y-group]').forEach(row => {
                const val = prefs[row.getAttribute('data-a11y-group')];
                row.querySelectorAll('.a11y-chip').forEach(chip => {
                    if (chip.getAttribute('data-value') === val) chip.classList.add('a11y-chip--active');
                });
            });
            panel.querySelectorAll('[data-a11y-control]').forEach(sel => {
                const key = sel.getAttribute('data-a11y-control');
                if (prefs[key]) sel.value = prefs[key];
            });
            ['contentScale', 'fontScale', 'lineHeight', 'letterSpacing'].forEach(key => {
                const range = panel.querySelector(`.a11y-slider[data-a11y-slider="${key}"]`);
                const valueEl = panel.querySelector(`.a11y-slider-value[data-a11y-value="${key}"]`);
                if (range && valueEl) {
                    const v = prefs[key] != null ? prefs[key] : 100;
                    range.value = v;
                    valueEl.textContent = v + '%';
                }
            });
            applyLanguage(prefs.lang || 'es');
        });

        ['contrast', 'theme', 'textColor'].forEach(groupName => {
            const row = panel.querySelector(`[data-a11y-group="${groupName}"]`);
            if (!row) return;
            row.querySelectorAll('.a11y-chip').forEach(chip => {
                const value = chip.getAttribute('data-value');
                if (prefs[groupName] === value) chip.classList.add('a11y-chip--active');
                chip.addEventListener('click', () => {
                    prefs[groupName] = value;
                    savePrefs(prefs);
                    applyPrefs(prefs);
                    row.querySelectorAll('.a11y-chip').forEach(c => c.classList.remove('a11y-chip--active'));
                    chip.classList.add('a11y-chip--active');
                    applyLanguage(prefs.lang || 'es');
                });
            });
        });

        panel.querySelectorAll('[data-a11y-control]').forEach(sel => {
            const key = sel.getAttribute('data-a11y-control');
            if (prefs[key]) sel.value = prefs[key];
            sel.addEventListener('change', () => {
                prefs[key] = sel.value;
                savePrefs(prefs);
                applyPrefs(prefs);
                if (key === 'lang') applyLanguage(prefs.lang);
            });
        });

        const sliderKeys = ['contentScale', 'fontScale', 'lineHeight', 'letterSpacing'];
        sliderKeys.forEach(key => {
            const range = panel.querySelector(`.a11y-slider[data-a11y-slider="${key}"]`);
            const valueEl = panel.querySelector(`.a11y-slider-value[data-a11y-value="${key}"]`);
            if (!range || !valueEl) return;
            const min = parseInt(range.min, 10);
            const max = parseInt(range.max, 10);
            const step = parseInt(range.step, 10) || 5;
            const update = () => {
                const v = parseInt(range.value, 10);
                prefs[key] = v;
                valueEl.textContent = v + '%';
                savePrefs(prefs);
                applyPrefs(prefs);
            };
            if (prefs[key] != null) {
                const v = Math.max(min, Math.min(max, prefs[key]));
                range.value = v;
                valueEl.textContent = v + '%';
            }
            range.addEventListener('input', update);
            panel.querySelectorAll(`.a11y-slider-btn[data-a11y-slider="${key}"]`).forEach(btn => {
                btn.addEventListener('click', () => {
                    const delta = parseInt(btn.getAttribute('data-delta'), 10);
                    let v = parseInt(range.value, 10) + delta;
                    v = Math.max(min, Math.min(max, v));
                    range.value = v;
                    update();
                });
            });
        });

        applyLanguage(prefs.lang || 'es');
    }

    function init() {
        const prefs = loadPrefs();
        applyPrefs(prefs);
        createWidget(prefs);
    }

    window.__a11yTranslate = function (key) {
        const p = loadPrefs();
        const dict = translations[p.lang] || translations.es;
        return dict[key] != null ? dict[key] : key;
    };

    window.__a11yReapplyLanguage = function () {
        const prefs = loadPrefs();
        applyLanguage(prefs.lang || 'es');
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
