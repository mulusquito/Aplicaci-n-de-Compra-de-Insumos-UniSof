# ngrok - Guía paso a paso para Mercado Pago

> **Nota:** ngrok ya está instalado en tu PC. Si no lo tienes, ejecuta: `winget install ngrok.ngrok`

---

## Paso 1: Configurar tu authtoken

1. Entra a **https://dashboard.ngrok.com/get-started/your-authtoken**
2. Inicia sesión si hace falta
3. **Copia** tu authtoken (es una cadena larga como `3AE1WG2AyDSjJVN1HuKP55XPuSa_...`)

4. En PowerShell ejecuta (reemplaza con TU token):

```powershell
ngrok config add-authtoken TU_TOKEN_AQUI
```

Ejemplo:
```powershell
ngrok config add-authtoken 3AE1WG2AyDSjJVN1HuKP55XPuSa_WzXo86y45yQv1WpT76KW
```

---

## Paso 2: Iniciar tu aplicación

En una terminal, ejecuta tu app Spring Boot:

```powershell
cd "c:\Users\alfon\Desktop\SEMESTRE 8\Software 3\insumos\insumos"
.\mvnw.cmd spring-boot:run
```

Espera a que aparezca `Started InsumosApplication`. Déjala corriendo.

---

## Paso 3: Iniciar ngrok

Abre **otra terminal** (PowerShell) y ejecuta:

```powershell
ngrok http 8080
```

Verás algo como:

```
Forwarding    https://abc1-23-45.ngrok-free.app -> http://localhost:8080
```

**Copia la URL https** (ej: `https://abc1-23-45.ngrok-free.app`).

---

## Paso 4: Actualizar application.properties

1. Abre `insumos/src/main/resources/application.properties`
2. Busca la línea `app.base-url=`
3. Cámbiala por tu URL de ngrok:

```properties
app.base-url=https://abc1-23-45.ngrok-free.app
```

(Usa la URL que copiaste en el paso 3)

---

## Paso 5: Reiniciar la aplicación

1. En la terminal donde corre la app, presiona **Ctrl+C** para detenerla
2. Vuelve a ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## Paso 6: Usar la app con Mercado Pago

1. Abre el navegador
2. Entra a la **URL de ngrok** (no uses localhost):
   - `https://abc1-23-45.ngrok-free.app`
3. Inicia sesión, ve a Ventas, agrega productos al carrito
4. Haz clic en "Ir a pagar" y completa el formulario
5. Haz clic en "Pagar con Mercado Pago"
6. Usa las tarjetas de prueba (ver MERCADOPAGO_SANDBOX.md)

---

## Resumen de terminales

| Terminal 1 | Terminal 2 |
|------------|------------|
| `.\mvnw.cmd spring-boot:run` | `ngrok http 8080` |
| App corriendo | ngrok exponiendo el puerto 8080 |

Ambas deben estar abiertas al mismo tiempo.
