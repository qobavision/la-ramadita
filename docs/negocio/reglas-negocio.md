\# Reglas de Negocio - La Ramadita



\## 1. Objetivo



Este documento resume las reglas de negocio que deben respetarse durante el desarrollo del Sistema Integral de Gestión para Restaurante y Pollería La Ramadita.



Estas reglas provienen del análisis funcional y del levantamiento realizado al stakeholder.



También deben consultarse:



\- `AGENTS.md`

\- `docs/requisitos/requisitos-sistema.md`

\- `docs/arquitectura/arquitectura-sistema.md`

\- `docs/fuentes/`



\---



\# 2. Mesas y atención



\## RN-01 - Mesa ocupada



Una mesa no puede iniciar una nueva atención si ya se encuentra ocupada.



La mesa debe estar Disponible antes de iniciar una atención nueva.



\---



\## RN-02 - Liberación de mesa



Una mesa vuelve a estado Disponible únicamente después de que la cuenta correspondiente haya sido pagada completamente.



\---



\## RN-03 - Atención para llevar



El sistema debe permitir registrar pedidos sin mesa mediante la modalidad:



`Para llevar`



Estos pedidos deben continuar teniendo identificación propia y seguir el flujo de pedido, cocina y pago.



\---



\# 3. Pedidos



\## RN-04 - Pedido en borrador



Mientras el pedido se encuentre en estado Borrador, el Mesero puede:



\- agregar productos;

\- quitar productos;

\- modificar cantidades;

\- modificar observaciones.



\---



\## RN-05 - Envío a Cocina



Al confirmar un pedido:



\- deja de ser un borrador;

\- debe quedar registrado;

\- debe enviarse a Cocina;

\- debe conservar la información de la atención.



\---



\## RN-06 - Modificaciones después del envío



Si un pedido ya fue enviado a Cocina y posteriormente se modifica o cancela algún producto, el cambio debe identificarse claramente.



Cocina debe poder distinguir:



\- pedido normal;

\- pedido modificado;

\- producto cancelado.



El objetivo es evitar que se prepare información desactualizada.



\---



\## RN-07 - Nuevos consumos



Mientras una atención permanezca abierta, el cliente puede solicitar productos adicionales.



Estos productos deben agregarse a la misma atención y cuenta existente.



\---



\## RN-08 - Observaciones



Las observaciones son opcionales.



Cuando existan, deben asociarse al producto correspondiente.



Ejemplos:



\- sin cebolla;

\- sin hielo;

\- poco picante.



\---



\# 4. Productos y carta



\## RN-09 - Producto agotado



Un producto marcado como Agotado no puede agregarse a nuevos pedidos.



Debe distinguirse visualmente para que el Mesero sepa que no está disponible.



\---



\## RN-10 - Carta diaria



La carta puede cambiar diariamente.



El usuario autorizado debe poder modificar la disponibilidad de productos sin modificar código del sistema.



\---



\# 5. Cocina



\## RN-11 - Visualización de pedidos



Los pedidos enviados deben aparecer en Cocina con información suficiente para ser preparados.



Como mínimo:



\- mesa o identificación;

\- piso o zona cuando corresponda;

\- productos;

\- cantidades;

\- observaciones.



\---



\## RN-12 - Estados de Cocina



Los estados principales son:



\- Pendiente;

\- Preparado.



El sistema no obliga a utilizar el estado intermedio `En preparación`.



\---



\## RN-13 - Producto preparado



Cuando Cocina marque un producto como Preparado, el sistema debe notificar al Mesero correspondiente.



La notificación debe producirse sin exigir una actualización manual de pantalla.



\---



\# 6. Pagos y cuentas



\## RN-14 - Pagos parciales



Una cuenta puede pagarse en varias partes.



Mientras exista saldo pendiente, la cuenta permanece en estado:



`Pendiente`



\---



\## RN-15 - Métodos de pago



Una misma cuenta puede registrar diferentes métodos de pago.



Métodos contemplados:



\- Efectivo;

\- Yape;

\- Plin;

\- Tarjeta;

\- Transferencia.



Cada pago debe conservar el método utilizado.



\---



\## RN-16 - Cuenta pagada



Una cuenta cambia a estado:



`Pagado`



únicamente cuando su saldo pendiente llegue a cero.



No debe cerrarse una cuenta con saldo pendiente.



\---



\## RN-17 - División de cuenta



El sistema debe permitir dividir una cuenta.



La división puede realizarse:



\- seleccionando productos;

\- utilizando montos parciales.



El Mesero puede consultar o ayudar a calcular la división.



La confirmación del pago corresponde a Caja o a un usuario expresamente autorizado según las reglas vigentes del sistema.



\---



\# 7. Compras e inventario



\## RN-18 - Registro de compra



Registrar una compra no aumenta automáticamente el inventario.



La compra debe quedar inicialmente como:



`Pendiente de recepción`



\---



\## RN-19 - Recepción de compra



El inventario aumenta cuando se confirma que la mercadería fue realmente recibida.



Después de confirmar la recepción:



\- se registra la cantidad recibida;

\- aumenta el inventario;

\- la compra cambia a `Recibida`.



\---



\## RN-20 - Stock mínimo



Cuando un insumo alcance o disminuya de su nivel mínimo, el sistema debe generar una alerta de:



`Stock bajo`



\---



\## RN-21 - Consumo por unidad de negocio



Cuando corresponda, el consumo de insumos debe poder identificarse como perteneciente a:



\- Restaurante;

\- Pollería.



Esto permite comparar cuánto consume cada unidad.



\---



\# 8. Restaurante y Pollería



\## RN-22 - Unidades de negocio



Restaurante y Pollería funcionan dentro de una sola aplicación y una sola base de datos.



Sin embargo, sus operaciones deben poder diferenciarse.



\---



\## RN-23 - Conservación de unidad



Cada operación conserva la unidad de negocio con la que fue iniciada.



Por ejemplo:



Si un pedido fue creado durante el turno Restaurante y permanece abierto al comenzar el horario de Pollería, continúa perteneciendo al Restaurante hasta finalizar.



\---



\## RN-24 - Información separada y consolidada



El sistema debe permitir consultar:



\- Restaurante;

\- Pollería;

\- resultado consolidado.



Esto aplica cuando corresponda a:



\- ventas;

\- caja;

\- pedidos;

\- consumos;

\- reportes;

\- indicadores.



\---



\# 9. Caja



\## RN-25 - Apertura y cierre



La caja debe manejar estados:



\- Abierta;

\- Cerrada.



Las operaciones del turno deben quedar asociadas a la caja y unidad de negocio correspondiente cuando aplique.



\---



\## RN-26 - Efectivo esperado y real



Al cerrar caja, el sistema debe permitir comparar:



\- efectivo esperado según el sistema;

\- efectivo contado físicamente.



Debe calcularse la diferencia.



Cuando corresponda puede registrarse una observación.



\---



\## RN-27 - Movimientos de caja



El sistema debe poder registrar movimientos adicionales como:



\- ingresos;

\- egresos.



Estos movimientos deben conservar monto, motivo y fecha cuando corresponda.



\---



\# 10. Historial



\## RN-28 - Conservación de ventas



Las ventas cerradas deben conservarse para consultas históricas.



No deben desaparecer al terminar el día o cerrar un turno.



\---



\## RN-29 - Datos históricos



Los cambios posteriores en productos o precios no deben alterar incorrectamente la información de ventas ya realizadas.



El historial debe permitir conocer qué ocurrió en el momento de la venta.



\---



\# 11. Comprobantes



\## RN-30 - Comprobantes simulados



En la versión inicial se podrán generar:



\- Ticket;

\- Boleta simulada;

\- Factura simulada.



Estos comprobantes:



\- son funcionales dentro del proyecto;

\- no tienen validez tributaria real;

\- no se conectan con SUNAT.



\---



\# 12. Seguridad y permisos



\## RN-31 - Usuario individual



Cada trabajador debe utilizar su propio usuario.



Varios trabajadores pueden compartir el mismo rol, pero no deben compartir necesariamente la misma cuenta.



\---



\## RN-32 - Permisos



Las acciones deben validarse según el rol del usuario.



El backend debe impedir operaciones no autorizadas.



No basta con ocultar una opción en el frontend.



\---



\## RN-33 - Eliminación de usuarios



Los usuarios que posean historial no deben eliminarse físicamente.



Cuando corresponda se deben desactivar.



\---



\## RN-36 - Empleado y usuario



Empleado es cualquier trabajador de La Ramadita, tenga o no acceso al sistema.



Usuario es la cuenta de acceso al sistema vinculada a un empleado existente.



No todos los empleados tienen usuario. Un empleado tiene como máximo una cuenta; esa unicidad se garantiza en base de datos.



El username es el identificador para iniciar sesión y es distinto del correo personal del empleado.



\---



\## RN-37 - Cargo y rol



El cargo es la función laboral del empleado. Cargos iniciales: Cocinero, Ayudante de cocina, Mesero, Cajero, Supervisor, Seguridad, Almacenero, Otro.



El rol define los permisos dentro del sistema.



Los roles del sistema son: ADMINISTRADOR, SUPERVISOR, MESERO, COCINA y CAJA.



El rol DUEÑO de los documentos originales equivale a ADMINISTRADOR.



\---



\## RN-38 - Rol permanente único



En esta primera versión cada usuario tiene un único rol permanente.



\---



\## RN-39 - Administración de usuarios y roles



Solo ADMINISTRADOR crea usuarios, activa o inactiva cuentas y modifica roles permanentes.



SUPERVISOR no crea usuarios ni modifica roles permanentes.



ADMINISTRADOR tiene control general del sistema y puede acceder a los paneles operativos con su misma identidad, sin cambiar de usuario ni de rol.



\---



\## RN-40 - Asignaciones temporales (previsto)



Se contemplan asignaciones operativas temporales dentro de un turno para los roles MESERO, COCINA y CAJA.



El SUPERVISOR podrá realizarlas cuando se implementen.



Esta regla todavía no está implementada.



\---



\## RN-41 - Estado de empleado y de usuario



Empleado activo significa que trabaja actualmente. Empleado inactivo significa que no trabaja actualmente.



Usuario activo significa que puede iniciar sesión. Usuario inactivo significa que no puede iniciar sesión.



Son estados independientes, con estas reglas:



\- No se puede crear un usuario para un empleado inactivo o archivado.

\- Al inactivar un empleado, su usuario queda inactivo en la misma operación.

\- Al reactivar un empleado, su usuario permanece inactivo hasta que el ADMINISTRADOR lo reactive.

\- Un usuario puede estar inactivo mientras el empleado continúa activo.

\- Un usuario inactivo no puede iniciar sesión y puede reactivarse desde Editar.

\- Archivado (acción Eliminar) es una baja lógica: no hay eliminación física. El registro conserva ID, relaciones e historial, queda fuera de la operación normal y no aparece en Todos ni en Activos. Se consulta al final del filtro Inactivos. Al archivar se registra la fecha y hora de la baja.

\- Al archivar un empleado, su usuario también queda archivado y no puede iniciar sesión.

\- Un registro archivado no se reactiva desde Editar.

\- Último acceso es la fecha y hora del último inicio de sesión exitoso del usuario. No se actualiza en intentos fallidos ni al consultar la sesión.



\---



\# 13. Trazabilidad



\## RN-34 - Operaciones importantes



Las operaciones críticas deben conservar suficiente información para permitir trazabilidad.



Entre ellas pueden encontrarse:



\- cambios de pedidos;

\- pagos;

\- movimientos de caja;

\- compras;

\- recepción;

\- cambios de productos;

\- acciones administrativas.



\---



\# 14. Funcionamiento sin Internet



\## RN-35 - Operación local



Las funciones esenciales deben continuar funcionando dentro de la red local aunque no exista conexión a Internet.



Esto incluye principalmente:



\- mesas;

\- pedidos;

\- Cocina;

\- pagos;

\- caja;

\- inventario.



\---



\# 15. Necesidades operativas confirmadas por el stakeholder



Durante el levantamiento se identificaron las siguientes necesidades:



\- Restaurante y Pollería funcionan en el mismo establecimiento pero requieren control diferenciado.

\- La carta cambia con frecuencia y necesita actualizarse rápidamente.

\- Los productos agotados deben identificarse claramente.

\- Los clientes pueden agregar nuevos productos durante una atención.

\- Los clientes pueden solicitar cuentas divididas.

\- Cocina necesita recibir claramente pedidos nuevos y cambios.

\- El Mesero debe recibir aviso cuando Cocina termine una preparación.

\- El negocio utiliza Efectivo, Yape, Plin, Tarjeta y Transferencia.

\- Se requiere registrar ingresos y egresos de caja.

\- Se necesita comparar efectivo esperado con efectivo real.

\- Se necesita conservar información histórica.

\- Se requieren alertas de stock.

\- Es importante que el sistema funcione aunque falle Internet.



\---



\# 16. Regla para implementación



Antes de programar una funcionalidad, verificar:



1\. qué regla de negocio afecta;

2\. qué rol puede ejecutarla;

3\. qué estado tiene la entidad antes de la operación;

4\. qué estado debe tener después;

5\. qué información debe conservarse;

6\. si afecta inventario, caja, pedidos o historial;

7\. si requiere notificación en tiempo real.



Si una implementación contradice estas reglas, no continuar automáticamente.



Revisar primero los documentos oficiales y solicitar confirmación cuando sea necesario.

