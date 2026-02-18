# Por qué la contraseña en la BD es tan larga

Se usa **BCrypt** (de Spring Security), un algoritmo de cifrado:

- **No se guarda** la contraseña en texto plano.
- Se guarda un **hash** (valor irreversible) de la contraseña.
- El hash de BCrypt siempre tiene ~60 caracteres, por eso se ve larga.

Ejemplo:
- Contraseña real: `admin123`
- Hash en BD: `$2a$10$xxxxxxx...` (60 caracteres)

Al hacer login, Spring compara el texto que ingresaste con el hash guardado. Si coinciden, el acceso es válido.
