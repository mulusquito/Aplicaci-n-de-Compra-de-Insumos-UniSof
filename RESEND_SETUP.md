# Configuración de Resend para envío de correos

Resend permite enviar correos desde Render (y otros cloud) porque usa HTTP API en lugar de SMTP, que suele estar bloqueado.

## Paso 1: Crear cuenta en Resend

1. Entra a [resend.com](https://resend.com)
2. Regístrate (gratis)
3. Plan gratuito: 3.000 correos/mes

## Paso 2: Obtener API Key

1. En el dashboard de Resend → **API Keys**
2. Clic en **Create API Key**
3. Nombre: `unisof-render`
4. Copia la clave (empieza con `re_`)

## Paso 3: Agregar en Render

1. Render → tu Web Service → **Environment**
2. Clic en **Edit** → **Add** → **New variable**
3. **KEY:** `RESEND_API_KEY`
4. **VALUE:** `re_xxxxxxxxxxxx` (tu API key)
5. **Save, rebuild, and deploy**

## Correo remitente

Por defecto se usa `UNISOF <onboarding@resend.dev>`. Para usar tu dominio (ej. `noreply@tudominio.com`):

1. Resend → **Domains** → verifica tu dominio
2. En Render, agrega variable: `APP_RESEND_FROM` = `UNISOF <noreply@tudominio.com>`
