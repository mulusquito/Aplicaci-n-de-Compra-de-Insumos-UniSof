# PayU - Guía paso a paso

## Paso 1: Crear cuenta PayU

1. Entra a **https://www.payu.com.co/**
2. Haz clic en **"Registrarse"** o **"Crear cuenta"**
3. Completa el formulario con los datos de tu negocio
4. PayU te enviará un correo con tu **merchantId** y datos de acceso

---

## Paso 2: Obtener credenciales

1. Inicia sesión en **https://www.payu.com.co/**
2. Ve al **Módulo PayU** (panel de administración)
3. Busca la sección **"Integraciones"** o **"Credenciales"**
4. Obtén:
   - **merchantId** – ID único de tu comercio
   - **accountId** – ID de cuenta (para Colombia: usa el que te asignen)
   - **apiKey** – Clave API para firmar transacciones

> **Sandbox (pruebas):** PayU tiene credenciales de prueba. Si no las ves, contacta a PayU para activar el ambiente de pruebas.

---

## Paso 3: Configurar en tu app

Edita `application.properties` y agrega:

```properties
# PayU - Credenciales (usa las de PRUEBA para sandbox)
payu.merchant-id=TU_MERCHANT_ID
payu.account-id=TU_ACCOUNT_ID
payu.api-key=TU_API_KEY
payu.test=1
```

- **payu.test=1** → Modo pruebas (sandbox)
- **payu.test=0** → Producción (pagos reales)

---

## Paso 4: URLs de respuesta

PayU redirige al cliente a tu sitio después del pago. Necesitas una URL pública:

- **Con ngrok:** `https://tu-url.ngrok-free.dev`
- **Producción:** `https://tudominio.com`

La app usa `app.base-url` para construir:
- **responseUrl** – Donde PayU redirige al cliente (éxito/fallo)
- **confirmationUrl** – Donde PayU envía la confirmación (webhook)

---

## Paso 5: Probar el pago

1. Inicia la app: `.\mvnw.cmd spring-boot:run`
2. (Opcional) Inicia ngrok: `ngrok http 8080`
3. Entra a Ventas → agrega productos → Ir a pagar
4. Completa el formulario y haz clic en **"Pagar con PayU"**
5. Serás redirigido al checkout de PayU
6. Usa tarjetas de prueba (PayU te las indica en el panel de pruebas)

---

## Métodos de pago en Colombia

| Método | Parámetro |
|--------|-----------|
| Visa | VISA |
| Mastercard | MASTERCARD |
| PSE | PSE |
| Nequi | NEQUI |
| Efecty | EFECTY |

---

## Documentación oficial

- **WebCheckout:** https://developers.payulatam.com/latam/es/docs/integrations/webcheckout-integration.html
- **Formulario de pago:** https://developers.payulatam.com/latam/es/docs/integrations/webcheckout-integration/payment-form.html
