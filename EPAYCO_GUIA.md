# Epayco - Guía de integración

## Paso 1: Crear cuenta Epayco

1. Entra a **https://epayco.co/**
2. Haz clic en **"Registrarse"**
3. Completa el formulario con los datos de tu negocio
4. Verifica tu correo

---

## Paso 2: Obtener credenciales

1. Inicia sesión en **https://dashboard.epayco.co/**
2. Ve a **Integraciones** → **Propiedades de mi sitio** → **Llaves**
3. Copia la **Llave pública (P_CUST_ID_CLIENTE)** para el checkout
4. En modo prueba, usa las llaves de **"Pruebas"**

---

## Paso 3: Configurar en tu app

Edita `application.properties`:

```properties
# Epayco - Llave pública (Dashboard > Integraciones > Llaves)
epayco.public-key=TU_LLAVE_PUBLICA
epayco.test=true
```

- **epayco.test=true** → Modo pruebas
- **epayco.test=false** → Producción

---

## Paso 4: URL base

Para que Epayco redirija correctamente tras el pago, usa una URL pública:

- **Con ngrok:** `https://tu-url.ngrok-free.dev`
- Configura `app.base-url` en `application.properties`

---

## Paso 5: Probar el pago

1. Inicia la app: `.\mvnw.cmd spring-boot:run`
2. (Opcional) Inicia ngrok: `ngrok http 8080`
3. Entra a Ventas → agrega productos → Ir a pagar
4. Completa el formulario y haz clic en **"Pagar con Epayco"**
5. Se abrirá el checkout de Epayco (modal) con opciones: tarjeta, PSE, Efecty, etc.
6. En modo prueba, usa las tarjetas de prueba que Epayco indica en su panel

---

## Métodos de pago en Colombia

- Tarjeta de crédito/débito
- PSE (transferencia bancaria)
- Efecty
- Daviplata
- Nequi (vía PSE)

---

## Documentación oficial

- **Checkout:** https://docs.epayco.com/docs/integracion-personalizada
- **Dashboard:** https://dashboard.epayco.co/
