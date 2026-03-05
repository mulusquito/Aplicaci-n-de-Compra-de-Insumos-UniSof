# Guía paso a paso: Desplegar UNISOF en Microsoft Azure

Esta guía te lleva desde cero hasta tener tu aplicación Spring Boot corriendo en Azure con base de datos PostgreSQL.

---

## Parte 1: Prerrequisitos

Antes de empezar, necesitas:

- [ ] **Cuenta de Microsoft** (Outlook, Hotmail, etc.)
- [ ] **Cuenta de Azure** (puedes crear una gratuita en https://azure.microsoft.com/free/)
- [ ] **Repositorio en GitHub** con tu código (ya lo tienes)
- [ ] **Las credenciales** que usabas en Render (Stripe, correo, etc.)

---

## Parte 2: Crear cuenta y recursos en Azure

### Paso 1: Entrar al portal de Azure

1. Ve a **https://portal.azure.com**
2. Inicia sesión con tu cuenta de Microsoft
3. Si es tu primera vez, Azure puede ofrecerte créditos gratuitos (12 meses) o una suscripción de prueba

---

### Paso 2: Crear un grupo de recursos

1. En el menú lateral (o en la barra de búsqueda), busca **"Grupos de recursos"**
2. Haz clic en **"+ Crear"**
3. Completa:
   - **Suscripción:** Elige la que tengas (por ejemplo, "Suscripción gratuita")
   - **Grupo de recursos:** `unisof-rg` (o el nombre que prefieras)
   - **Región:** `Este de EE. UU.` o `Centro de EE. UU.` (elige una cercana)
4. Haz clic en **"Revisar + crear"** → **"Crear"**

---

### Paso 3: Crear la base de datos PostgreSQL

1. En la barra de búsqueda, escribe **"Azure Database for PostgreSQL"**
2. Haz clic en **"Azure Database for PostgreSQL flexible servers"**
3. Haz clic en **"+ Crear"**
4. En **"Conceptos básicos"**:
   - **Suscripción:** La misma de antes
   - **Grupo de recursos:** `unisof-rg`
   - **Nombre del servidor:** `unisof-db` (debe ser único globalmente; si no está disponible, prueba `unisof-db-tuNombre`)
   - **Región:** La misma del grupo de recursos
   - **Versión de PostgreSQL:** 15 o 16
   - **Tipo de carga de trabajo:** Desarrollo (más barato) o Producción
   - **Tamaño de proceso:** Burstable B1ms (el más económico para desarrollo)
   - **Almacenamiento:** 32 GB (mínimo)
5. En **"Autenticación"**:
   - **Método de autenticación:** Contraseña
   - **Nombre de usuario del administrador:** `unisofadmin` (o el que prefieras)
   - **Contraseña:** Crea una contraseña segura y **guárdala** (la necesitarás después)
6. Haz clic en **"Revisar + crear"** → **"Crear"**
7. Espera 5–10 minutos a que se cree el servidor

---

### Paso 4: Configurar el firewall de PostgreSQL

1. Cuando termine la creación, entra al recurso **"unisof-db"**
2. En el menú lateral, ve a **"Redes"** (o "Networking")
3. En **"Configuración del firewall"**:
   - Activa **"Permitir acceso público desde cualquier servicio de Azure a este servidor"**
   - O bien, añade la IP de tu máquina para pruebas (opcional)
4. Haz clic en **"Guardar"**

---

### Paso 5: Crear la base de datos `unisof_db`

1. En el recurso del servidor PostgreSQL, ve a **"Bases de datos"**
2. Haz clic en **"+ Agregar base de datos"**
3. Nombre: `unisof_db`
4. Haz clic en **"Guardar"**

---

### Paso 6: Obtener la cadena de conexión

1. En el servidor PostgreSQL, ve a **"Cadenas de conexión"** o **"Configuración del servidor"**
2. Anota:
   - **Nombre del host:** algo como `unisof-db.postgres.database.azure.com`
   - **Puerto:** 5432
   - **Usuario:** `unisofadmin`
   - **Contraseña:** la que creaste
   - **Base de datos:** `unisof_db`

La URL de conexión JDBC será:
```
jdbc:postgresql://unisof-db.postgres.database.azure.com:5432/unisof_db?sslmode=require
```

---

## Parte 3: Crear la aplicación web (App Service)

### Paso 7: Crear el App Service

1. En la barra de búsqueda, escribe **"App Services"**
2. Haz clic en **"+ Crear"** → **"Aplicación web"**
3. En **"Conceptos básicos"**:
   - **Suscripción:** La misma
   - **Grupo de recursos:** `unisof-rg`
   - **Nombre:** `unisof-app` (o `unisof-tuNombre` si no está disponible)
   - **Publicar:** Código
   - **Pila en tiempo de ejecución:** Java 17
   - **Sistema operativo:** Linux (recomendado) o Windows
   - **Región:** La misma
   - **Plan de App Service:** Crear nuevo → nombre `unisof-plan`
   - **Precio:** F1 (gratis) o B1 (básico, ~13 USD/mes, sin apagado)
4. Haz clic en **"Revisar + crear"** → **"Crear"**
5. Espera a que se cree (2–3 minutos)

---

### Paso 8: Conectar GitHub al App Service

1. Entra al recurso **"unisof-app"** (tu App Service)
2. En el menú lateral, ve a **"Centro de implementación"** (Deployment Center)
3. En **"Origen"**:
   - Elige **GitHub**
   - Autoriza Azure con GitHub si te lo pide
   - **Organización:** tu usuario de GitHub
   - **Repositorio:** `Aplicaci-n-de-Compra-de-Insumos-UniSof` (o el nombre real de tu repo)
   - **Rama:** `desarrollo` (o `despliegue`, la que uses para producción)
4. En **"Compilador"**:
   - **Tipo de compilación:** Maven (si despliegas código) o Docker (si usas Dockerfile)
   - Si usas Maven: deja la configuración por defecto
   - Si usas Docker: elige Dockerfile y la ruta `/Dockerfile` o la que corresponda
5. Haz clic en **"Guardar"**
6. Azure empezará a hacer el primer despliegue (puede tardar 5–10 minutos)

---

### Paso 9: Configurar variables de entorno

1. En tu App Service, ve a **"Configuración"** → **"Configuración de la aplicación"** (Application settings)
2. Haz clic en **"+ Nueva configuración de la aplicación"**
3. Añade **cada una** de estas variables (nombre = clave, valor = el valor correspondiente):

| Nombre | Valor |
|--------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://TU_HOST:5432/unisof_db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | `unisofadmin` (o tu usuario) |
| `SPRING_DATASOURCE_PASSWORD` | La contraseña de PostgreSQL |
| `SPRING_PROFILES_ACTIVE` | `azure` |
| `APP_BASE_URL` | `https://unisof-app.azurewebsites.net` (reemplaza con tu URL real) |
| `SPRING_MAIL_PASSWORD` | Tu contraseña de aplicación Gmail |
| `STRIPE_SECRET_KEY` | `sk_test_...` (tu clave de Stripe) |
| `APP_PAYMENT_PROVIDER` | `stripe` |
| `RESEND_API_KEY` | Tu clave de Resend (si la usas) |
| `APP_RESEND_FROM` | `UNISOF <noreply@unisof.shop>` |
| `MERCADOPAGO_ACCESS_TOKEN` | (opcional, si usas Mercado Pago) |

4. Haz clic en **"Guardar"** y luego en **"Continuar"** si te pregunta por reiniciar

---

## Parte 4: Archivo de configuración para Azure

### Paso 10: Crear `application-azure.properties`

En tu proyecto, crea el archivo `src/main/resources/application-azure.properties` con algo como:

```properties
# Perfil Azure - usa variables de entorno
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
app.base-url=${APP_BASE_URL}
spring.mail.password=${SPRING_MAIL_PASSWORD}
stripe.secret-key=${STRIPE_SECRET_KEY}
app.payment-provider=${APP_PAYMENT_PROVIDER:stripe}
```

Las variables se leen desde la configuración del App Service.

---

## Parte 5: Ajustes para el despliegue

### Paso 11: Verificar el puerto

Azure App Service usa el puerto que expone la variable `PORT` o `WEBSITES_PORT`. Spring Boot suele detectarlo. Si no, en `application.properties` puedes añadir:

```properties
server.port=${PORT:8080}
```

### Paso 12: Hacer commit y push

1. Haz commit de los cambios (incluido `application-azure.properties` si lo creaste)
2. Haz push a la rama `desarrollo` (o la que configuraste en el Centro de implementación)
3. Azure detectará el push y volverá a desplegar automáticamente

---

## Parte 6: Verificar el despliegue

### Paso 13: Revisar logs y estado

1. En el App Service, ve a **"Registros de implementación"** para ver el progreso
2. Ve a **"Registros de flujo"** o **"Log stream"** para ver los logs en tiempo real
3. Si hay errores, revisa que las variables de entorno estén bien y que la BD sea accesible

### Paso 14: Abrir la aplicación

1. En el App Service, haz clic en **"URL"** o **"Examinar"**
2. La URL será algo como: `https://unisof-app.azurewebsites.net`
3. Prueba que cargue la página principal y que el login funcione

---

## Resumen de costos (orientativo)

| Recurso | Plan gratuito / bajo costo | Costo aproximado |
|---------|----------------------------|------------------|
| App Service | F1 (gratis) | $0 (con limitaciones) |
| App Service | B1 (básico) | ~13 USD/mes |
| PostgreSQL Flexible | Burstable B1ms | ~12–15 USD/mes |
| **Total (desarrollo)** | | ~25–30 USD/mes |

El plan F1 de App Service es gratuito pero tiene limitaciones (se puede apagar por inactividad, similar a Render). El B1 mantiene la app siempre encendida.

---

## Solución de problemas frecuentes

### La app no arranca
- Revisa los logs en "Log stream"
- Comprueba que `SPRING_DATASOURCE_URL` tenga el formato correcto con `?sslmode=require`
- Verifica que el firewall de PostgreSQL permita conexiones desde Azure

### Error de conexión a la base de datos
- En PostgreSQL → Redes: activa "Permitir acceso desde servicios de Azure"
- Comprueba usuario, contraseña y nombre de la base de datos

### Stripe o correo no funcionan
- Revisa que las variables `STRIPE_SECRET_KEY` y `SPRING_MAIL_PASSWORD` estén bien configuradas en "Configuración de la aplicación"
- No uses espacios extra al pegar los valores

---

## Siguiente paso

Cuando tengas la cuenta de Azure creada y el grupo de recursos, sigue los pasos en orden. Si en algún paso te atascas, indica en qué paso estás y qué mensaje de error ves.
