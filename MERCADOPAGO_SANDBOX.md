# Mercado Pago - Pruebas en Sandbox

## Problema: "No pudimos procesar tu pago"

### 1. Paga como INVITADO con tarjeta (no con cuenta Mercado Pago)

En el checkout de Mercado Pago, **elige "Pagar con tarjeta"** (o "Otro medio de pago" → Tarjeta).  
**No** uses "Pagar con Mercado Pago" si estás logueado con tu cuenta real: en sandbox eso suele fallar.

### 2. Usa tarjetas de prueba exactas

| Tarjeta | Número | CVV | Vencimiento |
|---------|--------|-----|-------------|
| Mastercard | 5254 1336 7440 3564 | 123 | 11/30 |
| Visa | 4013 5406 8274 6260 | 123 | 11/30 |
| Visa (alternativa) | 4509 9535 6623 3704 | 123 | 11/30 |
| Mastercard (alternativa) | 5031 7557 3453 0604 | 123 | 11/30 |

**Para que el pago sea APROBADO:**
- **Nombre del titular:** `APRO` (exacto, sin apellido)
- **Documento:** `123456789` o `12345678`

### 3. No uses PSE ni otros medios offline

En sandbox, usa **solo tarjeta de crédito/débito**. PSE y medios offline pueden dar error.

### 4. Cuenta comprador de prueba (si pagas con MP)

Si quieres pagar con "Cuenta Mercado Pago":

1. Ve a [Tus integraciones](https://www.mercadopago.com.co/developers/panel/app)
2. Menú lateral → **Cuentas de prueba**
3. Crea un **Comprador** de prueba
4. Usa ese usuario/contraseña para pagar en el checkout

### 5. back_urls con localhost

Mercado Pago **no acepta** `http://localhost:8080`. Usa ngrok:

```bash
ngrok http 8080
```

En `application.properties`:

```properties
app.base-url=https://tu-url.ngrok-free.dev
```

---

## Checklist rápido

- [ ] Pagar como **invitado** con tarjeta (no con cuenta MP)
- [ ] Tarjeta: `5254 1336 7440 3564` o `4509 9535 6623 3704`
- [ ] CVV: `123`, vencimiento: `11/30`
- [ ] Nombre: `APRO`
- [ ] Documento: `123456789`
- [ ] ngrok corriendo y `app.base-url` actualizado
