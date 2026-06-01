# SuperComp Backend

Este backend contiene la lógica de la API y el modelo de datos para la aplicación de comparación de productos.

## Qué incluye
- Servidor Express con rutas para productos, wishlist, listas de compra, comentarios y autenticación.
- Esquema de Mongoose para `Product` con campos `name`, `supermarket`, `price`, `category`, `imageUrl` y `source`.
- Los datos de producto se guardan en MongoDB y se devuelven desde la API.

## Sobre las imágenes de producto
- `imageUrl` está guardado como parte de cada producto en la base de datos.
- El backend actual no genera ni descarga imágenes en tiempo de ejecución.
- Los enlaces de imagen se cargaron antes, durante un seed/import de datos, por eso no hay un script de imágenes en esta carpeta.

## Nota importante
- El código que ves aquí solo sirve datos ya guardados.
- Si tienes imágenes de Unsplash u otro origen, fueron añadidas previamente al dataset, no por el servidor en este momento.
