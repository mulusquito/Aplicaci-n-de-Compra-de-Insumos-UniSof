# CLAUDE.md — UNISOF Insumos

Contexto del proyecto para Claude Code. Actualizar cada vez que se agregue un módulo, entidad o cambio arquitectónico relevante.

---

## Descripción general

Sistema web de gestión para la empresa de confección **UNISOF**. Permite administrar insumos (telas, hilos, etc.), proveedores, clientes, ventas (recibos), usuarios con roles, pagos en línea y un chatbot IA interno llamado **Nova**.

- **Rama principal:** `main`
- **Rama de desarrollo activa:** `avance`
- **Git user:** `alfonsoocampo999`
- **Repositorio remoto:** `https://github.com/mulusquito/Aplicaci-n-de-Compra-de-Insumos-UniSof.git`

---

## Flujo de trabajo con Jira

El proyecto usa Jira para gestión de tareas (historias de usuario) con integración automática a GitHub.

**Convención de ramas:** cada tarea de Jira tiene un código `SCRUM-XXX`. La rama debe nombrarse con ese código para que Jira detecte los commits/PRs automáticamente y actualice el estado de la tarea.

```
# Patrón de nombre de rama
SCRUM-XXX-descripcion-corta

# Ejemplo
git checkout -b SCRUM-75-agregar-orden-de-compra
```

**Flujo:**
1. Tomar tarea en Jira → anotar el código `SCRUM-XXX`
2. Desde `avance` actualizado: `git pull origin avance`
3. Crear rama: `git checkout -b SCRUM-XXX-descripcion`
4. Desarrollar y hacer commits (mencionar el código en el mensaje: `SCRUM-XXX: descripción`)
5. Push: `git push origin SCRUM-XXX-descripcion`
6. Jira detecta la rama/commits y los vincula automáticamente a la tarea

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.3.5 / Java 17 |
| Base de datos | PostgreSQL (`unisof_db`) — Hibernate `ddl-auto=update` |
| Seguridad | Spring Security (sesiones HTTP + BCrypt + 2FA por correo) |
| Frontend | HTML / CSS / JS vanilla (sin framework frontend) |
| Pagos | Stripe + Mercado Pago |
| IA / Chatbot | OpenAI GPT-4o-mini (chat "Nova") |
| Observabilidad | Spring Actuator + Prometheus + Micrometer |
| Build | Maven |
| Email | SMTP Gmail (puerto 465 SSL) |

---

## Estructura de paquetes

```
com.unisof.insumos
├── config/        → SecurityConfig, DataInitializers (seed de BD al arrancar)
├── model/         → Entidades JPA (8 entidades)
├── repository/    → 1 repositorio por entidad (Spring Data JPA)
├── service/       → Lógica de negocio
├── controller/    → Endpoints REST + servicio de vistas HTML
└── dto/           → Objetos de request/response
```

---

## Entidades JPA y tablas en PostgreSQL

### Dominio: Insumos / Compras

| Entidad | Tabla | Notas |
|---|---|---|
| `CategoriaInsumo` | `categorias_insumo` | Catálogo de categorías (telas, hilos, etc.) |
| `Insumo` | `insumos` | `@ManyToOne` → `CategoriaInsumo`. Tiene stock, código único, unidad de medida |
| `Proveedor` | `proveedores` | `@ManyToMany` ↔ `CategoriaInsumo` (pivot: `proveedor_categorias_insumo`) |

### Dominio: Ventas / Clientes

| Entidad | Tabla | Notas |
|---|---|---|
| `Cliente` | `clientes` | Identificado por cédula (único) |
| `Recibo` | `recibos` | `@ManyToOne` → `Cliente` y `Usuario` (vendedor). Items guardados como JSON en columna TEXT |

### Dominio: Usuarios / Seguridad

| Entidad | Tabla | Notas |
|---|---|---|
| `Usuario` | `usuarios` | Roles: `ADMINISTRADOR`, `VENDEDOR`, `JEFE DE VENTAS`, `JEFE DE COMPRAS` |
| `TokenVerificacion` | `tokens_verificacion` | 2FA — código 6 dígitos, expira en 10 min |
| `TokenRecuperacionContrasena` | `tokens_recuperacion_contrasena` | Token UUID para restablecer contraseña, expira en 1 hora |

### Relaciones clave

```
categorias_insumo ──< insumos
categorias_insumo >──< proveedores  (pivot: proveedor_categorias_insumo)
clientes ──< recibos >── usuarios
usuarios ──< tokens_verificacion
usuarios ──< tokens_recuperacion_contrasena
```

---

## Módulos funcionales

| Módulo | Controller | Ruta base | Estado |
|---|---|---|---|
| Autenticación / 2FA | `AuthController` | `/api/auth` | Completo |
| Usuarios / Roles | `UsuarioController` | `/api/usuarios` | Completo |
| Clientes | `ClienteController` | `/api/clientes` | Completo |
| Recibos / Ventas | `ReciboController` | `/api/recibos` | Completo |
| Insumos / Inventario | `ComprasInsumoController` | `/api/compras/insumos` | Completo |
| Proveedores | `ProveedorController` | `/api/proveedores` | Completo |
| Dashboard General | `DashboardController` | `/api/dashboard` | Completo |
| Dashboard Compras | `ComprasDashboardController` | `/api/compras/dashboard` | Completo |
| Pagos | `CheckoutController`, `WebhookController` | `/api/checkout`, `/api/webhooks` | Integrado |
| Chat IA (Nova) | `ChatController` | `/api/chat` | Integrado |

---

## Roles y permisos (Spring Security)

| Rol | Acceso |
|---|---|
| `ADMINISTRADOR` | Todo: usuarios, dashboard, proveedores, compras |
| `JEFE DE COMPRAS` / `JEFE DE VENTAS` | `/api/compras/**` |
| Autenticado (cualquier rol) | Clientes, recibos, checkout |
| Público | Login, 2FA, recuperar contraseña, chat, webhooks, actuator/health |

---

## Vistas HTML (frontend)

```
login.html, verificar-token.html, recuperar-contrasena.html, restablecer-contrasena.html
index.html (catálogo público)
ventas.html, clientes.html, recibos
compras.html, inventario-insumos.html, proveedores.html
ordenes.html, panel-admin.html, personal.html
terminos-y-condiciones.html
```

---

## Pruebas unitarias existentes

Ubicadas en `src/test/java/com/unisof/insumos/`:

- `service/AuthServiceTest`
- `service/RegistroUsuarioServiceTest`
- `service/UsuarioAdminServiceTest`
- `service/RecuperarContrasenaServiceTest`
- `controller/ClienteControllerTest`
- `controller/ReciboControllerTest`
- `controller/DashboardControllerTest`
- `controller/CheckoutControllerTest`
- `dto/LoginRequestTest`
- `InsumosApplicationTests`

**Sin cobertura aún:** módulo de insumos, proveedores y compras.

---

## Sistema de Auditoría (SCRUM-64)

Implementado en la rama `SCRUM-64`. Registra **todas las acciones relevantes** en la tabla `auditoria_logs`.

### Entidad y capas

| Archivo | Descripción |
|---|---|
| `model/AuditoriaLog.java` | Entidad JPA con: id, fechaHora, usuarioNombre, usuarioRol, accion, modulo, descripcion, ipCliente, resultado, datosAdicionales |
| `repository/AuditoriaLogRepository.java` | Repositorio JPA con filtros por módulo, usuario, fecha, resultado y acción |
| `service/AuditoriaService.java` | Servicio central con constantes de módulos/acciones/resultados, método `registrar()` y helpers `obtenerUsuarioInfo()` / `obtenerIp()` |
| `config/LogoutAuditoriaHandler.java` | Handler de Spring Security para registrar LOGOUT antes de invalidar la sesión |
| `controller/AuditoriaController.java` | `GET /api/auditoria` — solo ADMINISTRADOR, con filtros opcionales |

### Módulos y acciones instrumentadas

| Módulo | Acciones registradas |
|---|---|
| AUTENTICACION | LOGIN (éxito/fallo), VERIFICACION_2FA (éxito/fallo), RECUPERAR_CONTRASENA, RESTABLECER_CONTRASENA, LOGOUT |
| USUARIOS | CREAR, EDITAR, ELIMINAR (éxito/fallo) |
| CLIENTES | CREAR, EDITAR, ELIMINAR, CONSULTAR por cédula (éxito/fallo) |
| INSUMOS | CREAR, EDITAR, ELIMINAR, CONSULTAR inventario (éxito/fallo) |
| PROVEEDORES | CREAR, EDITAR, ELIMINAR (éxito/fallo) |
| VENTAS | CREAR recibo, EDITAR recibo, ELIMINAR recibo, CREAR_PAGO (checkout) |
| COMPRAS | CONSULTAR dashboard de compras |
| DASHBOARD | CONSULTAR estadísticas del panel de administración |
| CHATBOT | CONSULTA_NOVA (indica si usó OpenAI o FAQ-reglas) |

### Endpoint de consulta

```
GET /api/auditoria                    → últimos 100 registros (paginado)
GET /api/auditoria?modulo=VENTAS
GET /api/auditoria?usuario=jperez
GET /api/auditoria?resultado=FALLIDO
GET /api/auditoria?accion=LOGIN
GET /api/auditoria?desde=2026-04-01&hasta=2026-04-30
GET /api/auditoria/{id}
```

---

## Gaps / Pendientes conocidos

- No existe entidad **OrdenDeCompra** — no hay trazabilidad de qué proveedor surtió qué insumo, en qué cantidad y a qué precio.
- No existe **historial de movimientos de stock** (entradas/salidas de insumos).
- Falta cobertura de tests para los controllers/services de insumos y proveedores.

---

## Configuración local

- PostgreSQL en `localhost:5432`, base de datos `unisof_db`, usuario `postgres`, contraseña `123456`
- Variables de entorno necesarias: `SPRING_MAIL_PASSWORD`, `OPENAI_API_KEY`, `STRIPE_SECRET_KEY`, `MERCADOPAGO_ACCESS_TOKEN`
- Arrancar: `./mvnw spring-boot:run`
- URL local: `http://localhost:8080`

---

*Última actualización: 2026-04-02*

---

## Documentación del Proyecto

> Extraído de los documentos oficiales en `Documentacion/`: Definición del Proyecto, Arquitectura C4, Plan de Calidad, Manual de Usuario y Reglas de Negocio.

---

### Propósito del sistema

UNISOF automatiza el proceso de **gestión de compra de insumos** para una empresa de confección de uniformes bajo pedido. El problema central era el cálculo manual de materiales, que generaba desperdicios, faltantes de inventario y retrasos en entregas. El sistema cubre el ciclo completo: captura de pedidos → análisis de insumos → generación de órdenes de compra a proveedores.

---

### Actores y responsabilidades

| Actor | Responsabilidades principales |
|---|---|
| **Administrador** | Registrar/gestionar empleados y roles, ver dashboard global (usuarios activos, órdenes del mes, ventas mensuales), consultar todas las órdenes de compra |
| **Vendedor** | Registrar clientes, crear pedidos de uniformes, ver resumen de ventas diarias, acceder al catálogo de productos |
| **Jefe de Compras** | Analizar pedidos vs inventario, identificar insumos faltantes, generar reportes de carencias, emitir órdenes de compra a proveedores |

---

### Procesos de negocio principales

1. **Autenticación y control de acceso** — Login + 2FA por email (token 6 dígitos). Salida: sesión activa con rol asignado.
2. **Gestión de pedidos de clientes** — Captura formal del pedido (prenda, cantidad, talla). Salida: pedido confirmado.
3. **Análisis de producción e inventario** — El sistema consulta "Fichas Técnicas" de cada prenda, calcula insumos necesarios y los compara contra stock. Salida: reporte de faltantes.
4. **Compra de insumos** — Toma el reporte de faltantes, genera órdenes de compra dirigidas a proveedores específicos. Solo adquiere lo necesario para cumplir los pedidos.

---

### Reglas de negocio

| Código | Regla |
|---|---|
| RN-01 | Solo usuarios registrados pueden acceder (usuario + contraseña) |
| RN-02 | Todo usuario debe tener un rol asignado (Administrador, Jefe de Compras, Vendedor) |
| RN-03 | Recuperación de contraseña solo mediante enlace enviado al email registrado |
| RN-04 | Inicio de sesión requiere verificación en dos pasos (token por email) |
| RN-05 | Todo pedido debe estar registrado en el sistema antes de iniciar producción |
| RN-06 | El sistema requiere confirmación de pago antes de aprobar un pedido |
| RN-07 | Todo pedido tiene fecha de entrega estándar de 15 días |
| RN-08 | Todo pedido genera una factura con nombre del vendedor responsable |
| RN-09 | El cliente debe cancelar el 100% del pedido (no pagos parciales) |

---

### Arquitectura — Modelo C4

**Nivel 1 (Contexto):**
- Sistema central: Aplicación UNISOF
- Actores: Vendedor, Administrador, Jefe de Compras
- Sistemas externos: Pasarela de pagos (Stripe), Servicio de notificaciones (Gmail SMTP), Proveedores

**Nivel 2 (Contenedores):**
- Frontend: Web App HTML5/CSS3/JS vanilla
- Backend: API REST Spring Boot
- Base de datos: PostgreSQL
- Integraciones: Stripe, Gmail SMTP

**Nivel 3 (Componentes del backend):**
- Componente de Inicio de Sesión
- Componente de Seguridad (Spring Security + BCrypt + 2FA)
- Componente de Historial de Clientes
- Componente de Pedidos
- Componente de Notificaciones
- Componente Generador de Reportes
- Componente de Inventario y Órdenes de Compra

**Comunicación:**
- Frontend → Backend: HTTPS / JSON (REST)
- Backend → BD: SQL / JPA Hibernate
- Backend → Stripe: HTTPS / API pagos
- Backend → Gmail: SMTP SSL puerto 465

---

### Decisiones arquitectónicas (ADRs)

| ADR | Decisión | Justificación |
|---|---|---|
| ADR-01 | Web app con Java, HTML5, CSS3, JS | Rendimiento, seguridad, integración, consistencia |
| ADR-02 | Backend API REST centralizado en Spring Boot | Facilita comunicación, frontend/backend en mismo repositorio |
| ADR-03 | Java + Spring Boot | Ecosistema estable para APIs empresariales |
| ADR-04 | PostgreSQL | Mejor manejo de tipos de datos para inventarios textiles |
| ADR-05 | Spring Data JPA | Mejora mantenibilidad, pruebas y consistencia |
| ADR-06 | Stripe con patrón Adapter | Facilita cambio de pasarela sin modificar el sistema |
| ADR-07 | Gmail SMTP externalizado | Permite migrar a SendGrid/SES sin tocar el core |
| ADR-08 | Recibos/órdenes en PostgreSQL | Escalabilidad, orden y seguridad |
| ADR-09 | Autenticación centralizada con Spring Security | Reduce riesgos, centraliza control de acceso |

---

### Flujos de usuario principales

**Vendedor — Crear pedido:**
1. Login → 2FA → Dashboard de ventas
2. Catálogo → seleccionar prenda y talla → carrito
3. Buscar cliente existente (por cédula) o crear nuevo
4. Pago con Stripe → confirmación → recibo generado → email enviado

**Administrador:**
1. Dashboard: usuarios activos, órdenes del mes, ventas (gráficas)
2. Gestión de personal: registrar, asignar rol, actualizar, eliminar
3. Consulta de todas las órdenes de compra

**Jefe de Compras:**
1. Consultar inventario actual
2. Ver reporte de insumos faltantes
3. Generar orden de compra → email a proveedor → actualizar stock

---

### Requisitos no funcionales y métricas medidas

| RNF | Métrica | Resultado |
|---|---|---|
| Tiempo de autenticación | < 5 segundos | Cumple |
| Tasa de autenticación exitosa | ≥ 95% | Cumple (95%+) |
| Recuperación de contraseña efectiva | ≥ 95% | Cumple (100%) |
| Envío de token de verificación | < 60 segundos | Cumple |
| Precisión en control de roles | ≥ 99% | Cumple (100%) |
| Mensajes de error claros al usuario | ≤ 5% sin mensaje | **No cumple (12.5%)** |
| Tiempo de pago | < 15 minutos | Cumple (3.10 min promedio) |
| Integridad de pedidos registrados | 0% incompletos | Cumple |
| Performance (GTmetrix) | Calificación A | 100% |
| Structure (GTmetrix) | — | 92% |

**Estándar de calidad aplicado:** ISO/IEC 25010 (Adecuación Funcional, Fiabilidad, Seguridad, Eficiencia de Desempeño, Usabilidad, Mantenibilidad, Interoperabilidad)

**Pendiente de mejora:** Los mensajes de error al usuario no cumplen el umbral del 5% — requiere revisión de validaciones en frontend.
