# Pruebas en Postman - Login y 2FA

## Requisito previo
- Aplicación ejecutándose en **http://localhost:8080**
- PostgreSQL con la base de datos `unisof_db` activa

---

## Paso 1: Login (envía token por correo)

| Campo | Valor |
|-------|-------|
| **Método** | `POST` |
| **URL** | `http://localhost:8080/api/auth/login` |
| **Headers** | `Content-Type: application/json` |
| **Body** | raw → JSON |

**Body (JSON):**
```json
{
    "usuario": "admin",
    "contrasena": "admin123"
}
```

**Respuesta esperada (200 OK):**
```json
{
    "valido": true,
    "mensaje": "Token de verificacion enviado a tu correo electronico",
    "usuario": {
        "id": 1,
        "usuario": "admin",
        "nombre": "Administrador",
        "correo": "tu-correo@ejemplo.com",
        "rol": "ADMINISTRADOR"
    },
    "requiereVerificacionDosPasos": true
}
```

**Siguiente acción:** Revisa el correo configurado en la BD (`usuarios.correo`) y copia el token de 6 dígitos.

---

## Paso 2: Verificar token 2FA

| Campo | Valor |
|-------|-------|
| **Método** | `POST` |
| **URL** | `http://localhost:8080/api/auth/verify-token` |
| **Headers** | `Content-Type: application/json` |
| **Body** | raw → JSON |

**Body (JSON):**
```json
{
    "usuario": "admin",
    "token": "123456"
}
```
> Reemplaza `"123456"` por el código de 6 dígitos que recibiste por correo.

**Respuesta esperada (200 OK):**
```json
{
    "valido": true,
    "mensaje": "Verificación exitosa. Redirigiendo al panel principal",
    "usuario": {
        "id": 1,
        "usuario": "admin",
        "nombre": "Administrador",
        "correo": "tu-correo@ejemplo.com",
        "rol": "ADMINISTRADOR"
    },
    "requiereVerificacionDosPasos": false
}
```

---

## Paso 3: SCRUM-36 - Verificar sesion y timeout

Tras verify-token, Postman guarda la cookie JSESSIONID automaticamente. Esa sesion expira tras **1 minuto de inactividad**.

### 3.1 Obtener usuario actual (GET /api/auth/me)

| Campo | Valor |
|-------|-------|
| **Metodo** | `GET` |
| **URL** | `http://localhost:8080/api/auth/me` |
| **Nota** | Debe tener sesion activa (haber llamado verify-token antes) |

**Respuesta 200:** Datos del usuario. **Respuesta 401:** Sesion expirada (esperar 1 min sin hacer requests).

### 3.2 Cerrar sesion (POST /api/auth/logout)

| Campo | Valor |
|-------|-------|
| **Metodo** | `POST` |
| **URL** | `http://localhost:8080/api/auth/logout` |

Invalida la sesion. Tras esto, GET /me retornara 401.

---

## Resumen rapido

| # | Endpoint | Metodo | Body |
|---|----------|--------|------|
| 1 | `/api/auth/login` | POST | `{"usuario":"admin","contrasena":"admin123"}` |
| 2 | `/api/auth/verify-token` | POST | `{"usuario":"admin","token":"CODIGO_6_DIGITOS"}` |
| 3 | `/api/auth/me` | GET | (ninguno, usa cookie de sesion) |
| 4 | `/api/auth/logout` | POST | (ninguno) |

---

## Errores comunes

| Respuesta | Causa |
|-----------|-------|
| **Credenciales inválidas** | Usuario o contraseña incorrectos |
| **Error al enviar token** | Problema con SMTP (revisar configuración de correo) |
| **Token inválido o expirado** | Código incorrecto o ya usado/expirado |
| **Connection refused** | La aplicación no está corriendo o no está en el puerto 8080 |
