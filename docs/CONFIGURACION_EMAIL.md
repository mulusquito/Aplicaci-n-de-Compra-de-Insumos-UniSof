# Configuracion de correo - SCRUM-35

## Diferencia entre los correos

| Uso | Propiedad | Descripcion |
|-----|-----------|-------------|
| **DESDE** (quien envia) | `spring.mail.username` | Cuenta SMTP que envia el token. Necesita contrasena de aplicacion |
| **HACIA** (quien recibe) | Tabla `usuarios.correo` | Correo de cada usuario en la BD. El token se envia a este correo |
| **Admin inicial** | `app.admin.email` | Solo para cuando se crea el primer usuario (BD vacia) |

## Tu configuracion

- **Correo que envía:** unisofmelissa@gmail.com (en application.properties)
- **Correo que recibe:** El que esta en la BD (usuarios.correo) - ya lo modificaste

## Si usas el MISMO correo para enviar y recibir

Puedes usar alfonso.ocampom@uqvirtual.edu.co para ambos:
- **Enviar:** Necesitas SMTP. Si uqvirtual usa Gmail/Google Workspace, usa smtp.gmail.com
- **Recibir:** El admin en la BD debe tener ese correo (ya lo tienes)

## Contrasena de aplicacion

1. Gmail: Cuenta Google > Seguridad > Verificacion en 2 pasos > Contrasenas de aplicaciones
2. Crea una contrasena y ponla en `spring.mail.password`

## Si el correo de la empresa es diferente

- `spring.mail.username` = correo@empresa.com (el que envia)
- `usuarios.correo` en BD = correo de cada usuario (donde reciben)
- Pueden ser distintos: la empresa envia, los usuarios reciben en su correo personal
