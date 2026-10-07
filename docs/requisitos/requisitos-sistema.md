\# Requisitos del Sistema - La Ramadita



\## 1. Objetivo



Este documento resume los requisitos que deben guiar el desarrollo del Sistema Integral de Gestión para Restaurante y Pollería La Ramadita.



No reemplaza los documentos originales ubicados en:



`docs/fuentes/`



Para detalles completos de requisitos, historias de usuario y criterios de aceptación consultar:



`docs/fuentes/Informe\_Base\_Proyecto\_La\_Ramadita\_v01.docx`



También deben respetarse:



\- `AGENTS.md`

\- `docs/arquitectura/arquitectura-sistema.md`



\---



\## 2. Alcance funcional



La versión inicial contempla:



\- usuarios y roles;

\- Restaurante y Pollería como unidades de negocio;

\- turnos;

\- compras y recepción;

\- inventario;

\- recetas;

\- productos;

\- carta diaria;

\- mesas;

\- atención en salón;

\- pedidos para llevar;

\- pedidos;

\- cocina;

\- pagos;

\- división de cuentas;

\- caja;

\- comprobantes simulados;

\- historial de ventas;

\- reportes;

\- Business Intelligence básico.



No forma parte del alcance inicial:



\- facturación electrónica real con SUNAT;

\- delivery por Internet;

\- contabilidad completa;

\- planillas;

\- operación multi-sede;

\- sincronización compleja local-nube;

\- IA avanzada.



\---



\# 3. M01 - Usuarios, roles y acceso



\## Objetivo



Controlar autenticación, usuarios y permisos.



\## Requisitos principales



El sistema debe:



\- permitir inicio de sesión mediante usuario y contraseña;

\- manejar los roles:

&#x20; - ADMINISTRADOR;

&#x20; - SUPERVISOR;

&#x20; - MESERO;

&#x20; - COCINA;

&#x20; - CAJA;

\- permitir que cada trabajador tenga su propio usuario;

\- mostrar funciones según el rol;

\- impedir en backend acciones no autorizadas;

\- permitir crear, editar, activar y desactivar usuarios a los roles autorizados;

\- evitar eliminar físicamente usuarios que posean historial.



Las contraseñas no deben almacenarse en texto plano.



\## Decisiones del equipo (roles y accesos)



El rol DUEÑO de los documentos originales equivale oficialmente a ADMINISTRADOR.



\*\*Empleado y usuario\*\*



\- Empleado: cualquier trabajador de La Ramadita, tenga o no acceso al sistema. Se registran sus datos básicos (nombres, apellidos, DNI, teléfono, correo personal, cargo y estado).

\- Usuario: cuenta de acceso al sistema vinculada a un empleado existente.

\- No todos los empleados tienen usuario.

\- El username es el identificador para iniciar sesión. Es distinto del correo personal del empleado.



\*\*Cargo y rol\*\*



\- Cargo: función laboral del empleado. Cargos iniciales: Cocinero, Ayudante de cocina, Mesero, Cajero, Supervisor, Seguridad, Almacenero, Otro.

\- Rol: permisos dentro del sistema (ADMINISTRADOR, SUPERVISOR, MESERO, COCINA, CAJA).

\- Un cargo no otorga por sí solo un rol, ni un rol implica un cargo.



\*\*Rol permanente\*\*



\- En esta primera versión cada usuario tiene un único rol permanente.

\- Solo ADMINISTRADOR crea usuarios, activa o inactiva cuentas y modifica roles permanentes.

\- SUPERVISOR no crea usuarios ni modifica roles permanentes.



\*\*Estado activo / inactivo\*\*



\- Empleado activo: trabaja actualmente. Empleado inactivo: no trabaja actualmente.

\- Usuario activo: puede iniciar sesión. Usuario inactivo: no puede iniciar sesión.

\- El estado del empleado y el del usuario son independientes.



\*\*Asignaciones temporales (previstas, no implementadas)\*\*



\- Se contemplan asignaciones operativas temporales dentro de un turno para MESERO, COCINA y CAJA.

\- El SUPERVISOR podrá realizarlas cuando se implementen.



\*\*Alcance del ADMINISTRADOR\*\*



\- Control general del sistema.

\- Gestión de empleados, usuarios y accesos permanentes.

\- Consulta de Dashboard, Reportes / BI (incluye ventas e históricos), control de caja e información global.

\- Acceso a los paneles operativos (supervisión, mesero, cocina y caja) con su misma identidad, sin cambiar de usuario ni de rol.



\---



\# 4. M02 - Unidades de negocio y turnos



\## Objetivo



Diferenciar las operaciones del Restaurante y la Pollería.



\## Requisitos principales



El sistema debe gestionar:



\- Restaurante;

\- Pollería.



Debe permitir:



\- abrir un turno;

\- indicar la unidad de negocio;

\- cerrar el turno;

\- conservar las operaciones realizadas;

\- consultar información de Restaurante;

\- consultar información de Pollería;

\- consultar información consolidada.



Cuando corresponda, deben identificarse por unidad de negocio:



\- pedidos;

\- ventas;

\- pagos;

\- turnos;

\- consumos;

\- caja.



Una operación iniciada como Restaurante o Pollería conserva esa unidad hasta finalizar.



\---



\# 5. M03 - Compras y recepción de insumos



\## Objetivo



Registrar compras y controlar cuándo la mercadería realmente ingresa al inventario.



\## Requisitos principales



El sistema debe permitir:



\- registrar proveedores;

\- registrar compras;

\- indicar insumos;

\- cantidades;

\- unidad de medida;

\- proveedor;

\- fecha;

\- costo.



Al registrar una compra:



`Estado = Pendiente de recepción`



Registrar la compra NO debe incrementar el inventario.



Cuando la mercadería llegue:



\- se selecciona la compra pendiente;

\- se registra la cantidad realmente recibida;

\- se confirma la recepción;

\- el inventario aumenta;

\- la compra cambia a `Recibida`.



\---



\# 6. M04 - Inventario y recetas



\## Objetivo



Controlar los insumos y su consumo.



\## Requisitos principales



Cada insumo debe poder manejar:



\- nombre;

\- unidad de medida;

\- stock actual;

\- stock mínimo;

\- costo cuando corresponda;

\- estado.



Estados principales:



\- Stock normal;

\- Stock bajo;

\- Sin stock.



El sistema debe alertar cuando un insumo alcance o disminuya de su stock mínimo.



\## Recetas



Debe ser posible relacionar:



`Producto → Insumos → Cantidades`



Ejemplo conceptual:



`Pollo a la brasa → pollo + papa + ensalada`



Cuando corresponda, el sistema debe registrar el consumo de insumos relacionado con los productos preparados.



El consumo debe poder identificarse como:



\- Restaurante;

\- Pollería.



\---



\# 7. M05 - Productos y carta diaria



\## Objetivo



Gestionar los productos que se ofrecen al cliente.



\## Requisitos principales



El sistema debe permitir registrar:



\- platos;

\- bebidas;

\- otros productos;

\- nombre;

\- precio;

\- categoría;

\- unidad de negocio;

\- disponibilidad.



Un producto podrá pertenecer a:



\- Restaurante;

\- Pollería;

\- ambas unidades.



\## Carta diaria



Debe permitirse:



\- crear la carta del día;

\- modificarla;

\- activar productos;

\- desactivar productos;

\- marcar productos como agotados;

\- generar una versión preparada para impresión.



Estados:



\- Disponible;

\- Agotado.



Un producto `Agotado`:



\- debe mostrarse visualmente diferente;

\- no puede agregarse a nuevos pedidos.



\---



\# 8. M06 - Mesas y atención



\## Objetivo



Gestionar la atención presencial y para llevar.



\## Requisitos principales



El sistema debe:



\- mostrar las mesas registradas;

\- asociarlas a piso o zona;

\- mostrar su estado;

\- identificar al mesero responsable;

\- permitir iniciar una atención;

\- permitir pedidos para llevar;

\- mostrar el consumo acumulado.



Estados principales de mesa:



\- Disponible;

\- Ocupada;

\- Pendiente de pago.



Una mesa Disponible cambia a Ocupada cuando se inicia una atención.



Una mesa ocupada no puede iniciar otra atención.



Una mesa vuelve a Disponible únicamente después de completar el pago.



\---



\# 9. M07 - Pedidos



\## Objetivo



Registrar y enviar los pedidos realizados por los clientes.



\## Requisitos principales



El Mesero debe poder:



\- seleccionar mesa o pedido para llevar;

\- consultar productos disponibles;

\- agregar productos;

\- indicar cantidades;

\- agregar observaciones opcionales;

\- consultar subtotal;

\- consultar total;

\- confirmar el pedido;

\- enviarlo a Cocina.



\## Pedido en borrador



Mientras no haya sido enviado a Cocina se puede:



\- agregar;

\- quitar;

\- modificar productos;

\- modificar cantidades.



\## Pedido enviado



Cuando se confirma:



\- debe guardarse;

\- debe aparecer en Cocina;

\- debe conservar mesa o identificación;

\- debe conservar cantidades;

\- debe conservar observaciones.



Se pueden agregar nuevos productos posteriormente a una atención todavía abierta.



Si un pedido enviado se modifica o cancela, Cocina debe identificar claramente el cambio.



\---



\# 10. M08 - Cocina



\## Objetivo



Permitir a Cocina visualizar y procesar pedidos.



\## Requisitos principales



Los pedidos enviados deben aparecer automáticamente.



Cada pedido debe mostrar como mínimo:



\- mesa o identificación;

\- piso o zona;

\- productos;

\- cantidades;

\- observaciones.



Estados principales de productos en Cocina:



\- Pendiente;

\- Preparado.



No es obligatorio utilizar el estado intermedio `En preparación`.



El Cocinero debe poder marcar productos individualmente como Preparados.



Cuando un producto sea marcado como Preparado:



\- debe registrarse el cambio;

\- debe notificarse al mesero correspondiente.



Los pedidos modificados o cancelados deben distinguirse claramente.



Se podrá disponer de alertas sonoras configurables.



También se contempla impresión de comandas.



\---



\# 11. M09 - Caja, pagos y división de cuentas



\## Objetivo



Gestionar cuentas pendientes y pagos.



\## Requisitos principales



Caja debe visualizar:



\- mesas pendientes de pago;

\- pedidos para llevar pendientes;

\- detalle de consumo;

\- monto total;

\- saldo pendiente.



\## División de cuenta



Debe permitirse dividir una cuenta:



\- seleccionando productos;

\- utilizando montos parciales.



\## Pagos



Una cuenta puede recibir varios pagos.



Cada pago puede usar un método diferente.



Métodos mínimos:



\- Efectivo;

\- Yape;

\- Plin;

\- Tarjeta;

\- Transferencia.



Después de cada pago se debe recalcular el saldo.



Mientras:



`saldo > 0`



la cuenta permanece:



`Pendiente`



Cuando:



`saldo = 0`



la cuenta cambia a:



`Pagado`



Al completar el pago:



\- se cierra la atención;

\- se libera la mesa.



El Mesero puede consultar o ayudar a dividir la cuenta, pero la confirmación del pago corresponde al rol autorizado de Caja o equivalente autorizado.



\---



\# 12. M10 - Comprobantes simulados y control de caja



\## Objetivo



Gestionar comprobantes y movimientos de caja.



\## Comprobantes



Después del pago se podrá generar:



\- Ticket;

\- Boleta simulada;

\- Factura simulada.



En esta versión:



\*\*NO existe integración real con SUNAT.\*\*



Los comprobantes no tienen validez tributaria real.



\## Caja



Debe permitirse:



\- apertura;

\- registro de ingresos;

\- registro de egresos;

\- acumulación de pagos por método;

\- cierre.



Al cerrar caja se debe mostrar:



\- efectivo esperado;

\- efectivo contado;

\- diferencia;

\- observación cuando corresponda.



\---



\# 13. M11 - Ventas e historial



\## Objetivo



Conservar información histórica de las operaciones.



El sistema debe conservar las ventas y permitir consultar por:



\- día;

\- semana;

\- mes;

\- rango de fechas;

\- Restaurante;

\- Pollería;

\- consolidado.



Una venta histórica debe conservar como mínimo la información necesaria para reconstruir:



\- fecha;

\- productos;

\- cantidades;

\- total;

\- métodos de pago;

\- unidad de negocio.



La información histórica no debe desaparecer al cambiar posteriormente precios o productos.



\---



\# 14. M12 - Reportes y Business Intelligence



\## Objetivo



Proporcionar información para apoyar decisiones del negocio.



Debe ser posible consultar, entre otros:



\- ventas del día;

\- ventas por periodo;

\- platos más vendidos;

\- horarios con mayor venta;

\- Restaurante vs Pollería;

\- resultado consolidado;

\- ventas por método de pago;

\- alertas de inventario;

\- consumo de insumos;

\- evolución de ventas.



Cuando existan costos suficientes, también podrá calcularse:



\- costo estimado por producto;

\- resultado operativo aproximado;

\- rentabilidad estimada.



Los indicadores financieros deben construirse con información válida del sistema.



\---



\# 15. Estados principales



\## Mesa



\- Disponible

\- Ocupada

\- Pendiente de pago



\## Pedido



\- Borrador

\- Enviado a Cocina

\- Cerrado



\## Producto en Cocina



\- Pendiente

\- Preparado



\## Producto de carta



\- Disponible

\- Agotado



\## Insumo



\- Stock normal

\- Stock bajo

\- Sin stock



\## Compra



\- Pendiente de recepción

\- Recibida



\## Cuenta



\- Pendiente

\- Pagado



\## Caja



\- Abierta

\- Cerrada



\## Turno



\- Abierto

\- Cerrado



\---



\# 16. Requisitos no funcionales



\## Disponibilidad



El sistema debe funcionar dentro de la red local del establecimiento.



Una caída de Internet no debe impedir las funciones esenciales:



\- pedidos;

\- Cocina;

\- mesas;

\- pagos;

\- caja;

\- inventario.



\---



\## Usabilidad



Las interfaces deben ser:



\- sencillas;

\- intuitivas;

\- adecuadas para usuarios no técnicos;

\- apropiadas para pantallas táctiles.



Cada rol debe visualizar únicamente las funciones necesarias para su trabajo.



Las operaciones frecuentes deben requerir pocos pasos.



\---



\## Compatibilidad



El sistema debe adaptarse a:



\- computadora;

\- laptop;

\- tablet;

\- teléfono móvil cuando corresponda.



Debe funcionar en navegadores web modernos.



No se requiere desarrollar una aplicación distinta para cada dispositivo.



\---



\## Rendimiento



Las operaciones comunes deben responder aproximadamente en:



`3 segundos o menos`



bajo condiciones normales de la red local.



Un pedido confirmado debe aparecer en Cocina prácticamente en tiempo real.



Una notificación de producto Preparado debe aparecer al Mesero sin necesidad de actualizar manualmente la pantalla.



\---



\## Seguridad



Cada trabajador debe utilizar credenciales individuales.



Debe existir:



\- control por roles;

\- protección de endpoints;

\- almacenamiento seguro de contraseñas;

\- validación de permisos en backend.



Un usuario no debe poder ejecutar acciones para las cuales no tenga permiso.



\---



\## Integridad



El sistema debe evitar situaciones inconsistentes como:



\- dos atenciones activas sobre una misma mesa;

\- cuenta cerrada con saldo pendiente;

\- venta de productos agotados;

\- pagos con saldos incorrectos.



\---



\## Confiabilidad



La información debe conservarse aunque:



\- se cierre sesión;

\- se cierre el navegador;

\- se actualice una pantalla;

\- se apague un dispositivo cliente.



Los pedidos enviados a Cocina deben permanecer registrados hasta ser procesados correctamente.



\---



\## Respaldo



Debe existir capacidad de respaldo de la base de datos.



Para el proyecto se propone como mínimo:



`respaldo diario`



\---



\## Mantenibilidad



El código debe:



\- mantenerse modular;

\- utilizar Git/GitHub;

\- permitir futuras modificaciones;

\- permitir nuevas funcionalidades sin reconstruir completamente el sistema.



\---



\## Escalabilidad



La arquitectura debe permitir evolucionar en el futuro desde una operación local hacia acceso remoto sin reconstruir completamente la aplicación.



No significa que la sincronización local-nube deba implementarse en esta etapa.



\---



\# 17. Criterio general de aceptación



Una funcionalidad no se considera terminada únicamente porque compile.



Debe:



1\. cumplir el requisito correspondiente;

2\. respetar el rol autorizado;

3\. cumplir las reglas de negocio;

4\. funcionar correctamente;

5\. ser probada;

6\. no impedir completar el proceso principal;

7\. integrarse coherentemente con frontend, backend y base de datos.



\---



\# 18. Regla para desarrollo con IA



Antes de implementar una funcionalidad, Cursor/OpenCode/Codex debe identificar:



\- módulo;

\- requisito relacionado;

\- rol involucrado;

\- tablas involucradas;

\- endpoints necesarios;

\- componentes frontend involucrados;

\- reglas de negocio afectadas.



Si existe una contradicción entre este resumen y los documentos originales, revisar:



`AGENTS.md`



y las fuentes en:



`docs/fuentes/`



antes de continuar.



No inventar requisitos para completar vacíos.

