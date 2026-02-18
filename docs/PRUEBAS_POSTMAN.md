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

## Resumen rápido

| # | Endpoint | Método | Body |
|---|----------|--------|------|
| 1 | `/api/auth/login` | POST | `{"usuario":"admin","contrasena":"admin123"}` |
| 2 | `/api/auth/verify-token` | POST | `{"usuario":"admin","token":"CODIGO_6_DIGITOS"}` |

---

## Errores comunes

| Respuesta | Causa |
|-----------|-------|
| **Credenciales inválidas** | Usuario o contraseña incorrectos |
| **Error al enviar token** | Problema con SMTP (revisar configuración de correo) |
| **Token inválido o expirado** | Código incorrecto o ya usado/expirado |
| **Connection refused** | La aplicación no está corriendo o no está en el puerto 8080 |
