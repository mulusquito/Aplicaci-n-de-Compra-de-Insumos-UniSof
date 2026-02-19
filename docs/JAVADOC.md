# Documentacion JavaDoc

El codigo esta documentado con JavaDoc. Para generar y exportar la documentacion HTML:

## Generar documentacion

```bash
cd insumos
.\mvnw.cmd javadoc:javadoc
```

La documentacion se genera en: `target/site/apidocs/`

## Exportar / Compartir

1. **Abrir localmente:** Abre `target/site/apidocs/index.html` en el navegador.
2. **Carpeta completa:** La carpeta `target/site/apidocs/` contiene todos los archivos HTML. Puedes:
   - Copiarla y compartirla (ZIP)
   - Subirla a un servidor web
   - Incluirla en documentacion del proyecto

## Estructura generada

- `index.html` - Pagina principal con listado de paquetes
- `com/unisof/insumos/` - Documentacion por paquete (model, dto, service, etc.)

## En el IDE

Al pasar el cursor sobre clases y metodos documentados, el IDE muestra el JavaDoc automaticamente.
