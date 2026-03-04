# Variables de entorno para Render

Configura estas variables en **Render** → tu servicio → **Environment** → **Environment Variables**.

## Correo (Gmail SMTP)

Para que funcione el envío de tokens y recibos por correo:

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `SPRING_MAIL_PASSWORD` | *(tu contraseña de aplicación)* | Contraseña de aplicación de Gmail (generada en [Google App Passwords](https://myaccount.google.com/apppasswords)) |
| `SPRING_MAIL_USERNAME` | `unisofmelissa@gmail.com` | Cuenta Gmail que envía |
| `SPRING_MAIL_HOST` | `smtp.gmail.com` | Servidor SMTP |
| `SPRING_MAIL_PORT` | `465` | Puerto SSL |

**Importante:** Usa la **contraseña de aplicación** de Gmail, no la contraseña normal. Si recuperaste la cuenta o cambiaste la contraseña, genera una nueva en [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords).

### Desarrollo local

Para probar en local, define la variable de entorno `SPRING_MAIL_PASSWORD` con tu contraseña de aplicación (en la configuración de ejecución de tu IDE o en la terminal antes de ejecutar).

## Otras variables típicas

| Variable | Descripción |
|----------|-------------|
| `APP_BASE_URL` | URL pública de tu app (ej: `https://insumos.onrender.com`) |
| `SPRING_DATASOURCE_URL` | URL de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario de BD |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de BD |
| `STRIPE_SECRET_KEY` | Clave secreta de Stripe (sk_test_...) |
| `MERCADOPAGO_ACCESS_TOKEN` | Token de Mercado Pago (si usas MP) |
| `APP_COPIAS_EMAILS` | Correos que reciben copia de recibos (separados por coma) |
