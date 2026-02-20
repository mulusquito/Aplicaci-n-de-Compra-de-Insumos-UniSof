# Pruebas en Postman - Flujo completo (Login, 2FA, Sesion SCRUM-36)

**Base URL:** `http://localhost:8080`

**Headers en todas las peticiones:** `Content-Type: application/json` (solo donde haya Body)

---

## 1. Login (envia token por correo)

| Campo | Valor |
|-------|-------|
| **Metodo** | `POST` |
| **URL** | `http://localhost:8080/api/auth/login` |
| **Headers** | `Content-Type: application/json` |
| **Body** | raw → JSON |

**Body:**
```json
{
    "usuario": "admin",
    "contrasena": "admin123"
}
```

**Respuesta:** 200 con `requiereVerificacionDosPasos: true`. Revisa el correo y copia el codigo de 6 digitos.

---

## 2. Verificar token 2FA (crea la sesion)

| Campo | Valor |
|-------|-------|
| **Metodo** | `POST` |
| **URL** | `http://localhost:8080/api/auth/verify-token` |
| **Headers** | `Content-Type: application/json` |
| **Body** | raw → JSON |

**Body:** (reemplaza 123456 por el codigo del correo)
```json
{
    "usuario": "admin",
    "token": "123456"
}
```

**Respuesta:** 200 con datos del usuario. **Postman guarda automaticamente la cookie JSESSIONID** (sesion activa).

---

## 3. Obtener usuario actual (SCRUM-36)

| Campo | Valor |
|-------|-------|
| **Metodo** | `GET` |
| **URL** | `http://localhost:8080/api/auth/me` |
| **Body** | Ninguno |
| **Cookies** | Usar cookies (Postman las envia solas si viene de verify-token) |

**Respuesta 200:** Datos del usuario. **Respuesta 401:** Sesion expirada (espera 1 min sin hacer requests).

---

## 4. Cerrar sesion (SCRUM-36)

| Campo | Valor |
|-------|-------|
| **Metodo** | `POST` |
| **URL** | `http://localhost:8080/api/auth/logout` |
| **Body** | Ninguno |

**Respuesta:** 200. La sesion se invalida. Tras esto, GET /me dara 401.

---

## 5. Probar timeout de 1 minuto (SCRUM-36)

1. Haz **verify-token** (paso 2) para crear sesion.
2. Haz **GET /api/auth/me** → debe dar 200.
3. **Espera 1 minuto** sin hacer ninguna peticion.
4. Haz **GET /api/auth/me** de nuevo → debe dar **401** (sesion expirada por inactividad).

---

## Tabla rapida

| # | URL | Metodo | Body |
|---|-----|--------|------|
| 1 | `/api/auth/login` | POST | `{"usuario":"admin","contrasena":"admin123"}` |
| 2 | `/api/auth/verify-token` | POST | `{"usuario":"admin","token":"CODIGO_6_DIGITOS"}` |
| 3 | `/api/auth/me` | GET | (ninguno) |
| 4 | `/api/auth/logout` | POST | (ninguno) |

---

## Como funciona en un frontend real (React, Angular, etc.)

**Importante:** Ningún endpoint se ejecuta "solo" ni "automáticamente". El frontend decide cuándo llamar a cada uno.

| Situación | Qué hace el frontend |
|-----------|----------------------|
| Usuario hace login | Llama `POST /login` → muestra pantalla de código 2FA |
| Usuario ingresa código 2FA | Llama `POST /verify-token` → si OK, guarda cookie y redirige al panel |
| Usuario navega en la app | Cada petición (datos, etc.) va con la cookie; el servidor valida la sesión |
| **1 min de inactividad** | El servidor invalida la sesión internamente. La próxima petición que haga el usuario devolverá **401** |
| Usuario recibe 401 | El frontend intercepta el 401, lee el JSON `{"mensaje":"Sesion expirada..."}`, muestra un modal o alerta con ese mensaje y redirige al login |
| Usuario hace clic en "Cerrar sesión" | Llama `POST /logout` (con cookie) para invalidar la sesión y luego redirige al login |

**No hay redirección automática a /logout.** El flujo es: cualquier API devuelve 401 → el frontend muestra el mensaje y redirige al login. El mensaje `"Sesion expirada o no autenticado..."` ya viene en el JSON de la respuesta 401; el frontend debe mostrarlo en pantalla.

---

## Importante para Postman (cookies)

**Las cookies son obligatorias.** Sin ellas, `/me` y `/logout` devolverán 401 aunque hayas hecho verify-token.

1. **Settings de Postman:** `Settings → General` → asegúrate de que **"Send no cookies"** esté **desactivado** (unchecked).
2. **Dominio único:** Todas las peticiones deben ir a `http://localhost:8080` (mismo dominio para que se compartan cookies).
3. **Orden estricto:** Login → **verify-token** → luego /me o /logout. Si llamas /me sin haber hecho verify-token, obtendrás 401.

### Si /me da 401 antes del minuto

- Verifica que **verify-token** haya devuelto 200.
- En la respuesta de verify-token, abre la pestaña **Cookies** (debajo del body) y comprueba que haya una cookie `JSESSIONID` para `localhost:8080`.
- Si no hay cookie, la sesión no se creó. Reinicia la app y repite login → verify-token → /me en ese orden.
