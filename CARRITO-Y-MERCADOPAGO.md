# Carrito y Mercado Pago - Guía de uso

## Resumen del flujo

1. **Agregar al carrito**: El vendedor hace clic en el botón **+** de cualquier producto del catálogo.
2. **Ver carrito**: Clic en el icono del carrito (nav) para abrir el sidebar.
3. **Modificar cantidades**: Usar los botones +/− en cada ítem, o × para quitar.
4. **Ir a pagar**: Clic en "Ir a pagar" → se abre el formulario de datos del cliente.
5. **Datos del cliente**: Nombre, email, teléfono y dirección (requeridos para el pago).
6. **Pagar con Mercado Pago**: El cliente es redirigido a la página de pago de Mercado Pago.
7. **Retorno**: Tras pagar, Mercado Pago redirige de vuelta a ventas.html con `?payment=success`, `failure` o `pending`.

---

## Cómo funciona el carrito

- **Almacenamiento**: Los ítems se guardan en `localStorage` del navegador (clave `unisof_cart`).
- **Persistencia**: El carrito se mantiene al recargar la página o cerrar el navegador.
- **Identificación de productos**: Cada producto se identifica por `nombre + precio` (ej: `blaizer-clásico-429000`).
- **Cantidades**: Si agregas el mismo producto varias veces, se suma la cantidad.
- **Limpieza**: El carrito se vacía automáticamente cuando el pago es exitoso (`payment=success`).

---

## Cómo funciona Mercado Pago

### ¿Qué es Mercado Pago?

Mercado Pago es un procesador de pagos que permite cobrar con tarjeta, PSE, efectivo, etc. sin manejar datos bancarios directamente.

### Flujo técnico

1. **Tu backend** crea una "preferencia" en la API de Mercado Pago con:
   - Los productos (nombre, cantidad, precio en COP)
   - Los datos del comprador (nombre, email, teléfono)
   - Las URLs de retorno (éxito, fallo, pendiente)

2. **Mercado Pago** devuelve una URL (`init_point` o `sandbox_init_point`).

3. **Tu frontend** redirige al cliente a esa URL.

4. **El cliente** paga en la página de Mercado Pago (tarjeta, PSE, etc.).

5. **Mercado Pago** redirige al cliente de vuelta a tu sitio con el resultado del pago.

### Configuración necesaria

1. **Crear cuenta en Mercado Pago Developers**  
   https://www.mercadopago.com.co/developers

2. **Crear una aplicación** en el panel de desarrolladores.

3. **Obtener el Access Token**:
   - **Pruebas**: Credenciales de prueba → Access Token de prueba.
   - **Producción**: Credenciales de producción → Access Token de producción.

4. **Configurar en `application.properties`**:
   ```properties
   mercadopago.access-token=APP_USR-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
   app.base-url=http://localhost:8080
   ```
   En producción, usa la URL real de tu sitio en `app.base-url`.

### Modo pruebas vs producción

- **Pruebas**: Usa el Access Token de prueba. Mercado Pago devuelve `sandbox_init_point` y puedes pagar con tarjetas de prueba.
- **Producción**: Usa el Access Token de producción. Los pagos son reales.

### Tarjetas de prueba (modo sandbox)

En modo pruebas puedes usar:
- **Aprobada**: `5031 7557 3453 0604`
- **Rechazada**: `5031 4332 1540 6351`
- **Pendiente**: otras tarjetas de prueba

CVV: cualquier 3 dígitos. Fecha: cualquier fecha futura.

---

## Archivos involucrados

| Archivo | Función |
|---------|---------|
| `js/cart.js` | Lógica del carrito (agregar, quitar, totales) |
| `ventas.html` | UI del carrito, formulario checkout, integración |
| `CheckoutController.java` | Endpoint que recibe items + cliente y crea preferencia |
| `MercadoPagoService.java` | Llama a la API de Mercado Pago |
| `application.properties` | `mercadopago.access-token`, `app.base-url` |
