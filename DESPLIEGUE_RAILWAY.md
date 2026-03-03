# Guía de despliegue en Railway

Despliegue paso a paso del proyecto UNISOF en Railway (la opción más sencilla).

---

## Paso 1: Crear cuenta en Railway

1. Entra a **https://railway.app**
2. Haz clic en **"Login"** o **"Start a New Project"**
3. Elige **"Login with GitHub"**
4. Autoriza Railway para acceder a tu cuenta de GitHub

---

## Paso 2: Crear proyecto y conectar GitHub

1. Haz clic en **"New Project"**
2. Elige **"Deploy from GitHub repo"**
3. Si no ves tus repos, haz clic en **"Configure GitHub App"** y da permisos a Railway
4. Busca y selecciona el repositorio: **Aplicaci-n-de-Compra-de-Insumos-UniSof**
   - Usa el repo de **alfonsoocampo999** (o el de tu compañera si tienes acceso)
5. En **"Branch"**, selecciona **`despliegue`**
6. Haz clic en **"Deploy"**

---

## Paso 3: Configurar directorio raíz (si es necesario)

Si Railway no detecta el proyecto (no encuentra `pom.xml`):

1. Entra a tu servicio en Railway
2. Ve a **Settings** (engranaje)
3. En **"Root Directory"**, escribe: `insumos` (si tu repo tiene la estructura con carpeta insumos dentro)
4. Si el `pom.xml` está en la raíz del repo, déjalo vacío

---

## Paso 4: Añadir PostgreSQL

1. En tu proyecto Railway, haz clic en **"+ New"**
2. Elige **"Database"** → **"Add PostgreSQL"**
3. Espera a que se cree la base de datos
4. Haz clic en el servicio **PostgreSQL**
5. Ve a la pestaña **"Variables"** y copia o anota:
   - `PGHOST`
   - `PGPORT`
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`
   - O la variable `DATABASE_URL` completa

---

## Paso 5: Conectar la app con la base de datos

1. Haz clic en tu **servicio de la aplicación** (el que tiene el icono de engranaje)
2. Ve a **"Variables"**
3. Haz clic en **"+ New Variable"** o **"Add Variable Reference"**
4. Añade estas variables (usa las referencias de Railway si están disponibles):

| Variable | Valor |
|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `railway` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `APP_BASE_URL` | `https://tu-app.up.railway.app` *(la pondrás en el paso 7)* |
| `MERCADOPAGO_ACCESS_TOKEN` | `APP_USR-3066688050255775-030220-...` *(tu token de MP)* |
| `APP_ADMIN_EMAIL` | `alfonso.ocampom@uqvirtual.edu.co` |

**Si Railway no permite referencias `${{Postgres.XXX}}`**, copia los valores manualmente desde el servicio PostgreSQL.

---

## Paso 6: Configurar correo (opcional)

Si quieres que funcione el envío de correos:

| Variable | Valor |
|----------|-------|
| `SPRING_MAIL_HOST` | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | `465` |
| `SPRING_MAIL_USERNAME` | `unisofmelissa@gmail.com` |
| `SPRING_MAIL_PASSWORD` | `cnkamdzczrgaeygg` |
| `APP_MAIL_ENABLED` | `true` |

---

## Paso 7: Generar URL pública

1. En tu **servicio de la aplicación**, ve a **"Settings"**
2. En **"Networking"**, haz clic en **"Generate Domain"**
3. Copia la URL que te dan (ej: `insumos-production.up.railway.app`)
4. Vuelve a **Variables** y actualiza `APP_BASE_URL` con esa URL
5. En Mercado Pago, configura el webhook: `https://tu-url.up.railway.app/api/webhooks/mercadopago`

---

## Paso 8: Redesplegar

1. Ve a **"Deployments"**
2. Haz clic en los tres puntos del último deployment
3. Elige **"Redeploy"**

O haz un nuevo `git push` a la rama `despliegue`.

---

## Resumen de variables mínimas

```
SPRING_PROFILES_ACTIVE=railway
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tu_password
APP_BASE_URL=https://tu-dominio.up.railway.app
MERCADOPAGO_ACCESS_TOKEN=APP_USR-...
APP_ADMIN_EMAIL=tu@correo.com
```

---

## Solución de problemas

| Problema | Solución |
|----------|----------|
| "Build failed" | Revisa que el directorio raíz sea correcto y que exista `pom.xml` |
| "Application failed to start" | Revisa las variables de la base de datos |
| "Port already in use" | El perfil `railway` usa `PORT`; no debería pasar |
| Mercado Pago no recibe webhooks | Verifica que `APP_BASE_URL` sea la URL pública de Railway |
