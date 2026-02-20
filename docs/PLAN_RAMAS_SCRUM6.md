# Plan: Separar frontend en rama SCRUM-6-frontend

## Situación actual
- **SCRUM-36** (en GitHub): Solo backend (login, 2FA, sesión)
- **SCRUM-36** (local): Igual + cambios del frontend **sin commitear** (static/, SecurityConfig)

## Objetivo
1. Hacer merge de SCRUM-36 → desarrollo (para que desarrollo tenga el backend)
2. Crear rama SCRUM-6-frontend con todo el frontend

## Pasos en orden

### 1. Guardar los cambios del frontend (stash)
```bash
cd insumos
git add .
git stash push -m "Frontend SCRUM-6 - interfaces, login, panel"
```

### 2. Cambiar a desarrollo y hacer merge de SCRUM-36
```bash
git checkout desarrollo
git pull origin desarrollo
git merge SCRUM-36 -m "Merge SCRUM-36: backend auth, 2FA, sesión"
```

### 3. Subir desarrollo a GitHub
```bash
git push origin desarrollo
```

### 4. Crear la rama SCRUM-6-frontend
```bash
git checkout -b SCRUM-6-frontend
```

### 5. Recuperar el frontend del stash
```bash
git stash pop
```

### 6. Hacer commit y subir la rama
```bash
git add .
git commit -m "SCRUM-6: Interfaz frontend - login, 2FA, panel por roles, recuperar contraseña"
git push -u origin SCRUM-6-frontend
```

## Resultado final
- **desarrollo**: Backend completo (SCRUM-7, 35, 36)
- **SCRUM-6-frontend**: desarrollo + frontend (interfaces, login, panel admin, etc.)
