# SCRUM-35: Flujo de verificación en dos pasos

## Cómo funciona

1. **Login** → El usuario envía `usuario` y `contrasena`
2. **Validación** → Se busca en la tabla `usuarios` quien coincida con usuario y contraseña
3. **Obtener correo** → Al encontrar el usuario, se toma su campo `correo` de la base de datos
4. **Generar token** → Se genera un token numérico de 6 dígitos
5. **Guardar token** → Se guarda en la tabla `tokens_verificacion` (usuario_id, token, fecha_expiracion)
6. **Enviar por correo** → Se envía el token al `correo` del usuario
7. **Usuario recibe** → El correo llega al email real o se muestra en la consola (modo desarrollo)
8. **Verificación** → El usuario ingresa el token en la aplicación
9. **Validar** → Se compara: ¿El token ingresado es igual al que se guardó para ese usuario?
   - **Sí y no expiró** → Éxito, redirigir al panel principal
   - **No** → Mensaje de error: "Token inválido o expirado"
