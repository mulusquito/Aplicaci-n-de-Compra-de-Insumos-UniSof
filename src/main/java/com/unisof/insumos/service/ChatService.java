package com.unisof.insumos.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Chatbot por reglas (FAQ) para UniSof. Responde según palabras clave. */
@Service
public class ChatService {

    private static final String DEFAULT_RESPONSE = "No encontré información sobre eso. Puedes preguntar por: "
            + "carrito, pedidos, clientes, inventario, insumos, contraseña, inicio de sesión, código de verificación, roles, catálogo, pagos o sesión.";

    private final List<FaqRegla> reglas = new ArrayList<>();

    public ChatService() {
        reglas.add(new FaqRegla("carrito, agregar, añadir, producto, comprar, catálogo",
                "En la página principal el catálogo es solo informativo. Para comprar debes iniciar sesión y usar el módulo de ventas/compras. El carrito se muestra en la barra superior."));
        reglas.add(new FaqRegla("total, precio, pago, pagar, stripe, mercadopago",
                "Al finalizar el pedido puedes pagar con la pasarela configurada (Stripe o Mercado Pago). El total se muestra en el resumen antes de confirmar."));
        reglas.add(new FaqRegla("pedido, pedidos, orden, órdenes, estado, seguimiento",
                "Consulta tus pedidos en el módulo 'Órdenes de compra'. Ahí verás el listado y 'Ver pedido' con el detalle completo."));
        reglas.add(new FaqRegla("cancelar, eliminar pedido",
                "Para cancelar o modificar un pedido contacta al administrador o al vendedor que atendió la orden."));
        reglas.add(new FaqRegla("contraseña, clave, restablecer, olvidé, recuperar",
                "En inicio de sesión usa '¿Olvidaste tu contraseña?' o ve a recuperar-contrasena. Ingresa tu correo y te enviaremos un enlace para crear una nueva clave."));
        reglas.add(new FaqRegla(
                "login, iniciar sesion, iniciar sesión, ingresar, entrar, inicio de sesion, inicio de sesión, "
                        + "como funciona el login, funcionamiento login, acceder al sistema",
                "Ve a Iniciar sesión e ingresa usuario y contraseña. Recibirás un código de verificación por correo (2FA). "
                        + "Introdúcelo en la siguiente pantalla para acceder."));
        reglas.add(new FaqRegla("código, token, verificación, 2FA, no llega",
                "El código es de 6 dígitos y se envía al correo del usuario. Revisa bandeja de entrada y spam. Expira en 10 minutos."));
        reglas.add(new FaqRegla("usuario, nombre usuario, olvidé usuario",
                "El usuario lo define el administrador al registrarte. Revisa el correo de bienvenida o pide al administrador que te lo indique."));
        reglas.add(new FaqRegla("rol, roles, administrador, vendedor, jefe",
                "Hay tres roles: Administrador, Vendedor y Jefe de compras. El administrador asigna el rol en Gestión de personal."));
        reglas.add(new FaqRegla("registrar usuario, nuevo usuario, gestión personal, personal",
                "Solo el administrador puede registrar usuarios. Panel admin > Gestión de personal > Registrar. Completa datos y acepta términos. Se envía correo de bienvenida."));
        reglas.add(new FaqRegla("dashboard, reportes, ventas, estadísticas",
                "El dashboard está en el panel de administrador. Muestra usuarios, pedidos y ventas. Puedes filtrar por fechas y exportar PDF."));
        reglas.add(new FaqRegla("sesión, inactividad, cerrar sesión, expira, tiempo",
                "Tras 2 minutos sin usar la app verás un aviso; si pasan 2 minutos más sin actividad, la sesión se cierra. Mover el ratón o pulsar teclas cuenta como actividad; «Seguir conectado» también renueva la sesión."));
        reglas.add(new FaqRegla("cerrar sesión, salir",
                "Usa el menú de usuario (icono en la barra) y elige 'Cerrar sesión'."));
        reglas.add(new FaqRegla("catálogo, productos, mujer, hombre, camisa",
                "En la página principal ves el catálogo por categorías. Es solo visual; para comprar usa el módulo de ventas/compras con sesión iniciada."));
        reglas.add(new FaqRegla("hola, ayuda, ayuda unisof, qué puedes",
                "Hola, soy el asistente de UNISOF. Puedo ayudarte con: carrito, pedidos, contraseña, inicio de sesión, código, roles, dashboard, sesión. Escribe una pregunta o palabra clave."));
        reglas.add(new FaqRegla("cliente, clientes, buscar cliente, buscar un cliente, listado clientes",
                "Los clientes los gestiona el vendedor o administrador en el módulo **Clientes** (con sesión iniciada). "
                        + "Allí puedes buscar por nombre o documento, ver el listado y editar datos. "
                        + "Si eres jefe de compras, tu módulo es **Compras** e **Inventario de insumos**."));
        reglas.add(new FaqRegla("insumo, insumos, inventario, stock, telas, categoría insumo, jefe de compras, compras y produccion",
                "El **Jefe de compras** entra a **Compras** (panel con resumen) y a **Inventario de insumos** para listar, buscar y dar de alta insumos (telas, hilos, etc.). "
                        + "Usa categoría, búsqueda por nombre o código, y «Listar todos» si hace falta."));
    }

    public String responder(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return "Escribe tu pregunta o una palabra clave (por ejemplo: pedidos, contraseña, carrito).";
        }
        String normalizado = normalizarTexto(mensaje);
        for (FaqRegla r : reglas) {
            for (String keyword : r.keywords) {
                if (normalizado.contains(normalizarTexto(keyword))) {
                    return r.respuesta;
                }
            }
        }
        return DEFAULT_RESPONSE;
    }

    /** Misma normalización para mensaje del usuario y para cada palabra clave (tildes, etc.). */
    private static String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[áàä]", "a").replaceAll("[éèë]", "e").replaceAll("[íìï]", "i")
                .replaceAll("[óòö]", "o").replaceAll("[úùü]", "u");
    }

    private static class FaqRegla {
        final String[] keywords;
        final String respuesta;

        FaqRegla(String keywordsStr, String respuesta) {
            this.keywords = keywordsStr.toLowerCase(Locale.ROOT).split("\\s*,\\s*");
            this.respuesta = respuesta;
        }
    }
}
