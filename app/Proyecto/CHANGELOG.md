# Historial de cambios — Repuestos Anelisa

## V0.1
- Agregar pantalla de inicio de sesión temporal.
- Agregar gestión de productos con almacenamiento y lectura mediante XML.
- Implementar funciones para agregar, editar y eliminar productos.
- Conectar el mgit statusodelo del inventario con el servicio de persistencia XML.

## V0.2
- Agregar interfaces JavaFX inicio de sesión, panel principal, inventario, formulario de producto, ventas, compras, reportes y usuarios.
- Agregar control de acceso por rol.
- Implementar registro de ventas con descuento de stock y comprobante en PDF.
- Implementar registro de compras a proveedor con aumento de stock y comprobante en PDF.
- Implementar reportes PDF de inventario, ventas y compras, con filtro por tipo, rango de fechas y categoría.
- Implementar consulta rápida de existencias.
- Ampliar el modelo de producto con categoría, proveedor, descripción y foto.
- Corregir la carga de `usuarios.xml` de versiones anterior.

## V1.0
- Reemplazar la persistencia en XML por MySQL ahora se guardan en la base de datos `repuestos_anelisa`.
- Agregar la clase `Db`  y la configuración de conexión en `Config`.
- Proteger el stock contra ventas simultáneas descontando solo si todavía hay existencias.
- Agregar el driver `mysql-connector-j` al `pom.xml`.

## V1.1
- Agregar la pantalla Proveedores (Se nos olvido) con botón en el menú: crear, editar nombre y contacto, y activar o desactivar.
- Agregar el módulo `PROVEEDORES` a la matriz de permisos.

## V1.2
- Rediseño visual con la paleta de los prototipos a color: gris oscuro #3E434A, índigo #302C9B, amarillo #ECAB0F, fondo #F3F4F5.
- Logo minimalista de Auto Repuestos Anelisa en el inicio de sesión, el menú lateral y el icono de la ventana (carpeta `resources/.../images`).
- Nuevo `styles.css`: menú lateral oscuro con botones de borde amarillo, tarjetas con borde superior, tablas con encabezado gris y filas alternadas lavanda, alertas de poco stock en tono crema, botones primarios índigo y botón "Salir" amarillo.
- Sin cambios de lógica ni de base de datos.

