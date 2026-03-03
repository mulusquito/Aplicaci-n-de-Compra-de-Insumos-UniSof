# Guía de configuración de Stripe

## 1. Obtener las claves API

1. Entra a [Stripe Dashboard](https://dashboard.stripe.com)
2. Asegúrate de estar en **Entorno de prueba** (modo test)
3. Ve a **Desarrolladores** → **Claves API**
4. Copia:
   - **Clave secreta** (`sk_test_...`) → para el backend

## 2. Configurar la aplicación

### Opción A: application.properties (desarrollo local)

```properties
# Usar Stripe en lugar de Mercado Pago
app.payment-provider=stripe

# Clave secreta de Stripe (modo prueba)
stripe.secret-key=TU_CLAVE_SECRETA_AQUI
```

### Opción B: Variables de entorno (Render)

En Render Dashboard → Environment:

| Variable | Valor |
|----------|-------|
| `APP_PAYMENT_PROVIDER` | `stripe` |
| `STRIPE_SECRET_KEY` | `sk_test_...` (tu clave) |
| `APP_BASE_URL` | `https://tu-app.onrender.com` |

## 3. Tarjetas de prueba (Stripe)

| Resultado | Número | CVV | Fecha |
|-----------|--------|-----|-------|
| **Aprobado** | 4242 4242 4242 4242 | Cualquiera 3 dígitos | Cualquier fecha futura |
| Rechazado | 4000 0000 0000 0002 | 123 | 12/34 |
| Fondos insuficientes | 4000 0000 0000 9995 | 123 | 12/34 |

- **Nombre:** Cualquier nombre
- **Correo:** Cualquier correo válido

## 4. Moneda (COP vs USD)

Por defecto la app usa **COP** (pesos colombianos). Stripe soporta COP.

Si prefieres **USD** para pruebas, modifica el frontend para enviar `currency_id: 'usd'` en los items del checkout.

## 5. Webhook (opcional)

Para recibir notificaciones cuando un pago se complete:

1. Stripe Dashboard → **Desarrolladores** → **Webhooks**
2. **Añadir endpoint**: `https://tu-app.onrender.com/api/webhooks/stripe`
3. Eventos sugeridos: `checkout.session.completed`

## 6. Cambiar entre Stripe y Mercado Pago

- `app.payment-provider=stripe` → usa Stripe
- `app.payment-provider=mercadopago` → usa Mercado Pago (por defecto)
