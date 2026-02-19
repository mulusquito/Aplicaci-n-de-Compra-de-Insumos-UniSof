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

## Importante para Postman

- **Cookies:** En Settings de Postman, "Cookies" debe estar activo para que guarde JSESSIONID.
- **Orden:** Siempre haz login → verify-token antes de /me o /logout (para tener sesion).
