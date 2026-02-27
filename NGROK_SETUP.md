# Configurar ngrok para Mercado Pago

ngrok ya está instalado. Solo falta configurar tu cuenta (gratis) y ejecutarlo.

## Paso 1: Crear cuenta y obtener authtoken

1. Entra a **https://dashboard.ngrok.com/signup** y crea una cuenta (gratis).
2. Ve a **https://dashboard.ngrok.com/get-started/your-authtoken**
3. Copia tu **authtoken**.

## Paso 2: Configurar ngrok

En una terminal (PowerShell o CMD), ejecuta:

```powershell
ngrok config add-authtoken TU_AUTHTOKEN_AQUI
```

Reemplaza `TU_AUTHTOKEN_AQUI` con el token que copiaste.

## Paso 3: Ejecutar ngrok

1. **Asegúrate de que tu aplicación Spring Boot esté corriendo** en el puerto 8080.

2. En una **nueva terminal**, ejecuta:

```powershell
ngrok http 8080
```

3. Verás algo como:

```
Forwarding    https://abc1-23-45-67-89.ngrok-free.app -> http://localhost:8080
```

4. **Copia la URL https** (ej: `https://abc1-23-45-67-89.ngrok-free.app`).

## Paso 4: Actualizar application.properties

Abre `src/main/resources/application.properties` y cambia:

```properties
app.base-url=https://TU-URL-DE-NGROK-AQUI
```

Por ejemplo:
```properties
app.base-url=https://abc1-23-45-67-89.ngrok-free.app
```

## Paso 5: Reiniciar la aplicación

Detén la aplicación (Ctrl+C) y vuelve a ejecutarla:

```powershell
cd insumos
.\mvnw.cmd spring-boot:run
```

## Paso 6: Usar la app

**Accede a la aplicación usando la URL de ngrok**, no localhost:

- `https://abc1-23-45-67-89.ngrok-free.app` (tu URL)
- Login, ventas, carrito, checkout, Mercado Pago

---

**Nota:** La URL de ngrok cambia cada vez que reinicias ngrok (en la versión gratuita). Si reinicias ngrok, debes actualizar `app.base-url` y reiniciar la app.
