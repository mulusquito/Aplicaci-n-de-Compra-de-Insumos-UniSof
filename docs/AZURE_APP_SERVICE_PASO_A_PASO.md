# Guía detallada: Crear App Service en Azure para UNISOF

Ya tienes PostgreSQL creado. Ahora crearemos la aplicación web (App Service) y la conectaremos a GitHub.

---

## Parte 1: Crear el App Service

### Paso 1.1: Buscar App Service

1. En la **barra de búsqueda** de Azure (arriba), escribe: **`App Services`**
2. Haz clic en **"App Services"** (icono de nube con flecha)
3. En la página de App Services, haz clic en **"+ Crear"** (botón azul)

---

### Paso 1.2: Seleccionar tipo de aplicación

1. Verás opciones: **Aplicación web**, **API web**, **Aplicación web para contenedores**, etc.
2. Haz clic en **"Aplicación web"** (o deja la opción por defecto si ya está seleccionada)

---

### Paso 1.3: Pestaña "Datos básicos" – Detalles del proyecto

| Campo | Valor |
|-------|--------|
| **Suscripción** | Azure subscription 1 |
| **Grupo de recursos** | **unisof-rg** (el que creaste) |

---

### Paso 1.4: Detalles de la instancia

| Campo | Valor |
|-------|--------|
| **Nombre** | `unisof-app` (o `unisof-app-alfonso` si no está disponible) |
| **Publicar** | **Código** |
| **Pila en tiempo de ejecución** | **Java 17** |
| **Sistema operativo** | **Linux** |
| **Región** | **Central US** (la misma que PostgreSQL) |

---

### Paso 1.5: Plan de App Service

| Campo | Valor |
|-------|--------|
| **Plan de Linux** | Haz clic en **"Crear nuevo"** |
| **Nombre del plan** | `unisof-plan` |
| **Plan de tarifa** | **F1 (Gratis)** o **B1** (~13 USD/mes, sin apagado) |

> **Nota:** F1 es gratis pero puede apagarse por inactividad (como Render). B1 mantiene la app siempre encendida.

---

### Paso 1.6: Revisar y crear

1. Haz clic en **"Revisar + crear"**
2. Revisa el resumen
3. Haz clic en **"Crear"**
4. Espera 2–3 minutos hasta que aparezca "Se completó la implementación"

---

## Parte 2: Conectar GitHub (Centro de implementación)

### Paso 2.1: Ir al Centro de implementación

1. Cuando termine la creación, haz clic en **"Ir al recurso"**
2. En el menú lateral izquierdo del App Service, busca **"Centro de implementación"** (Deployment Center)
3. Haz clic en **"Centro de implementación"**

---

### Paso 2.2: Origen del código

1. En **"Origen"**, selecciona **"GitHub"**
2. Si te pide autorizar Azure con GitHub:
   - Haz clic en **"Autorizar"**
   - Inicia sesión en GitHub si hace falta
   - Acepta los permisos que Azure solicite
3. Completa:
   - **Organización:** tu usuario de GitHub (ej: `alfonsoocampo999` o `mulusquito`)
   - **Repositorio:** `Aplicaci-n-de-Compra-de-Insumos-UniSof` (o el nombre exacto de tu repo)
   - **Rama:** `desarrollo` (o `despliegue` si esa es tu rama de producción)

---

### Paso 2.3: Configuración del compilador

1. En **"Compilador"** o **"Build Provider"**:
   - Si aparece **"Tipo de compilación"**: elige **"Maven"** (para código Java)
   - O **"Docker"** si prefieres usar el Dockerfile
2. Si usas **Maven**:
   - **Proyecto:** deja vacío o pon `/` si el `pom.xml` está en la raíz
   - Si tu `pom.xml` está en una subcarpeta (ej: `insumos/`), pon esa ruta
3. Haz clic en **"Guardar"** (o "Save")
4. Azure empezará el primer despliegue (puede tardar 5–15 minutos)

---

## Parte 3: Configurar variables de entorno

**Importante:** Haz esto **antes** o **justo después** del primer despliegue. Sin estas variables, la app no podrá conectarse a la base de datos.

### Paso 3.1: Ir a Configuración

1. En el menú lateral del App Service, ve a **"Configuración"**
2. Haz clic en **"Configuración de la aplicación"** (Application settings)

---

### Paso 3.2: Añadir variables

Haz clic en **"+ Nueva configuración de la aplicación"** y añade **cada una** de estas variables:

| Nombre | Valor |
|--------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://unisof-db.postgres.database.azure.com:5432/unisof_db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `unisofadmin` |
| `SPRING_DATASOURCE_PASSWORD` | *(tu contraseña de PostgreSQL)* |
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `APP_BASE_URL` | `https://unisof-app.azurewebsites.net` *(cambia si tu app tiene otro nombre)* |
| `SPRING_MAIL_PASSWORD` | *(tu contraseña de aplicación Gmail)* |
| `STRIPE_SECRET_KEY` | *(tu clave secreta de Stripe, ej: sk_test_...)* |
| `APP_PAYMENT_PROVIDER` | `stripe` |
| `RESEND_API_KEY` | *(tu API key de Resend, si usas correo por Resend)* |
| `APP_RESEND_FROM` | `UNISOF <noreply@unisof.shop>` *(o tu dominio verificado)* |

---

### Paso 3.3: Guardar y reiniciar

1. Haz clic en **"Guardar"** (arriba)
2. Si pregunta si quieres reiniciar, haz clic en **"Continuar"**

---

## Parte 4: Obtener la URL de tu app

1. En el App Service, en la parte superior verás la **URL** (ej: `https://unisof-app.azurewebsites.net`)
2. Si tu nombre es distinto, la URL será `https://[tu-nombre].azurewebsites.net`
3. Actualiza `APP_BASE_URL` en Configuración si usaste otro nombre

---

## Parte 5: Verificar el despliegue

### Paso 5.1: Ver logs de implementación

1. En **Centro de implementación**, ve a la pestaña **"Registros"** o **"Logs"**
2. Revisa que el despliegue termine sin errores

### Paso 5.2: Ver logs de la aplicación

1. En el menú lateral, ve a **"Registro de flujo"** (Log stream) o **"Registros"**
2. Activa el registro si está desactivado
3. Recarga la URL de tu app y revisa si aparecen errores en los logs

### Paso 5.3: Abrir la aplicación

1. Haz clic en **"Examinar"** o abre la URL en el navegador
2. Deberías ver la página de inicio de UNISOF

---

## Solución de problemas

### La app no arranca
- Revisa los logs en "Registro de flujo"
- Comprueba que `SPRING_DATASOURCE_URL` tenga `?sslmode=require`
- Verifica que la contraseña de PostgreSQL no tenga caracteres especiales que requieran escape

### Error 503 o página en blanco
- Espera 2–3 minutos tras el primer despliegue
- Comprueba que el despliegue haya terminado correctamente en el Centro de implementación

### No se conecta a la base de datos
- En PostgreSQL → Redes: confirma que "Permitir acceso desde servicios de Azure" esté activado
- Revisa usuario, contraseña y nombre de la base de datos en las variables

---

## Resumen de pasos

1. Crear App Service (Aplicación web, Java 17, Linux, Central US)
2. Centro de implementación → Conectar GitHub → Rama desarrollo
3. Configuración → Añadir todas las variables de entorno
4. Guardar y esperar a que la app arranque
5. Abrir la URL y probar
