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

\- permitir crear, editar, activar y desactivar empleados y usuarios solo al ADMINISTRADOR;

\- impedir que SUPERVISOR administre cuentas o asigne roles permanentes;

\- evitar eliminar físicamente empleados o usuarios que posean historial.



Las contraseñas no deben almacenarse en texto plano.



\## Decisiones del equipo (roles y accesos)



El rol DUEÑO de los documentos originales equivale oficialmente a ADMINISTRADOR.



\*\*Empleado y usuario\*\*



\- Empleado: cualquier trabajador de La Ramadita, tenga o no acceso al sistema. Se registran sus datos básicos (nombres, apellidos, DNI, teléfono, correo personal, cargo y estado).

\- Usuario: cuenta de acceso al sistema vinculada a un empleado existente.

\- No todos los empleados tienen usuario.

\- El username es el identificador para iniciar sesión. Es distinto del correo personal del empleado.



\*\*Cargo y rol\*\*



\- Cargo: función laboral del empleado. Cargos: Supervisor, Cajero, Ayudante de cajero, Cocinero, Ayudante de cocina, Mesero, Motorizado, Almacenero, Seguridad, Otro. Administrador no es un cargo: es un rol del sistema.

\- Rol: permisos dentro del sistema (ADMINISTRADOR, SUPERVISOR, MESERO, COCINA, CAJA).

\- Un cargo no otorga por sí solo un rol, ni un rol implica un cargo.



\*\*Rol permanente\*\*



\- En esta primera versión cada usuario tiene un único rol permanente.

\- Solo ADMINISTRADOR crea usuarios, activa o inactiva cuentas y modifica roles permanentes.

\- SUPERVISOR no crea usuarios ni modifica roles permanentes.



\*\*Estado activo / inactivo\*\*



\- Empleado activo: trabaja actualmente. Empleado inactivo: no trabaja actualmente.

\- Usuario activo: puede iniciar sesión. Usuario inactivo: no puede iniciar sesión.

\- El estado del empleado y el del usuario son independientes, con estas reglas: no se crea un usuario para un empleado inactivo o archivado; inactivar un empleado inactiva su usuario en la misma operación; reactivar un empleado no reactiva su usuario.

\- Activo, Inactivo y Archivado son distintos. Inactivo es temporal y sigue en el listado operativo. Archivado es la baja lógica de Eliminar: no hay DELETE físico; el registro se conserva para historial, deja de participar en la operación normal y guarda la fecha de esa baja.

\- Un usuario inactivo puede reactivarse. Un registro archivado no vuelve a Activo desde Editar.

\- Último acceso es el último inicio de sesión exitoso del usuario.

\- Relación: un empleado tiene como máximo una cuenta. Un usuario siempre pertenece a un empleado.

\- DNI obligatorio (8 dígitos, único) para empleados nuevos. Los empleados creados al migrar usuarios existentes pueden no tener DNI.



\*\*Asignaciones temporales (implementadas)\*\*



\- Dentro de un turno `EN_CURSO`, ADMINISTRADOR o SUPERVISOR pueden asignar una función temporal a una persona del personal: MESERO, COCINA o CAJA.

\- Solo ADMINISTRADOR puede asignar SUPERVISOR como función temporal.

\- SUPERVISOR no puede asignar SUPERVISOR ni gestionar (asignar o expulsar) a un ADMINISTRADOR.

\- La función temporal no cambia el rol permanente ni el JWT. `/auth/me` expone `funcionActual`. Esa función otorga el panel operativo correspondiente, además del panel del rol permanente.

\- ADMINISTRADOR conserva acceso a todos los paneles aunque no tenga función temporal.

\- Expulsar quita a la persona del personal del turno (borra la fila de asistencia). Un nuevo inicio de sesión la vuelve a registrar si el turno sigue abierto.



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



\## Horario base y apertura de turno



Horarios base (zona `America/Lima`, editables por ADMINISTRADOR):



\- Restaurante: 07:00 a 16:00;

\- Pollería: 16:00 a 01:00.



El turno no se abre al primer inicio de sesión del día.



Se abre automáticamente al llegar la hora de inicio de la unidad, o al arrancar el sistema si todavía está dentro de la ventana.



El proceso programado (cada minuto) no reabre un turno si ya existió uno ese día y esa unidad, aunque esté cerrado. Así, Finalizar no vuelve a abrir el mismo turno de inmediato.



Al arrancar el sistema sí puede abrirse el turno de la ventana vigente, aunque ese día ya se haya cerrado uno (por ejemplo, si la PC se apagó y se volvió a encender dentro del horario).



Pollería solo se abre automáticamente si no hay otro turno `EN_CURSO`.



La apertura automática no asigna un supervisor: el responsable se muestra como `Apertura automática`. `usuario_apertura_id` puede ser nulo.



ADMINISTRADOR o SUPERVISOR pueden iniciar o finalizar el turno de forma manual. El cierre registra quién lo cerró (`usuario_cierre_id`).



El inicio de sesión nunca se bloquea por no haber turno abierto.



\## Personal del turno



El personal del turno son quienes inician sesión cuando ya hay un turno `EN_CURSO` (o quien abre el turno de forma manual).



Reingresar en el mismo turno no duplica la fila: se conserva la primera `hora_ingreso`.



El responsable mostrado en Inicio y Turnos es quien abrió el turno, no quien está viendo la pantalla.



\## Dashboard de supervisión (Inicio)



Indicadores que ya usan datos reales:



\- Personal activo (mismo origen que Turnos), con desglose por función actual;

\- Compras por recibir (Pendiente y Parcial);

\- Alertas de stock (bajo y agotado);

\- Mesas registradas (nombre y zona). El filtro de mesas es Todas (por defecto), Libres y Ocupadas.



Indicadores que permanecen vacíos hasta que existan sus módulos:



\- Pedidos en curso y pedidos atendidos hoy;

\- Ventas del día;

\- Ocupación real de mesa, mesero, pedido, tiempo y total;

\- Tablas de cajas y cocina.



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

\- importe de cada línea.



Eliminar un proveedor es archivado (baja lógica, migración V11). No hay eliminación física ni un interruptor de activo en el listado operativo.



En la compra, la línea se registra con `Importe` (total de la línea). El precio unitario se calcula internamente (`importe / cantidad`, cuatro decimales) para valorizar stock; no se pide al usuario.



Al registrar una compra:



`Estado = Pendiente`



Registrar la compra NO debe incrementar el inventario.



Cuando la mercadería llegue:



\- se selecciona la compra pendiente;

\- se registra la cantidad realmente recibida;

\- se confirma la recepción;

\- el inventario aumenta;

\- se genera un movimiento `ENTRADA` con origen `Recepción COMP-xxx`;

\- la compra cambia a `Recibida` o `Parcial` si aún falta mercadería.



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



Los combos y recetas se arman en Inventario: un producto compuesto por componentes (insumos u otros productos) con cantidades.



Cuando corresponda, el sistema debe registrar el consumo de insumos relacionado con los productos preparados. El movimiento `CONSUMO` se registrará cuando exista el módulo de pedidos.



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



Los combos se ofrecen en carta con nombre pensado para el mesero, sin prefijo `Combo`. El plato ejecutivo comparte precio entre unidades cuando corresponde.



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



Un producto no debe duplicarse en la misma sección de una carta. Si en consulta aparece más de una vez el mismo producto en la misma sección, prevalece el último.



La consulta de carta devuelve detalles únicos. La carta de Pollería y la sección de marinos se muestran en tablas compactas para lectura operativa.



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



\---



\# 19. Estado de implementación documentado



Este apartado resume lo ya construido en el repositorio oficial para el informe del proyecto. No sustituye los requisitos de cada módulo. Lo que aún depende de pedidos, cocina, caja o ventas permanece pendiente a propósito.



\## M01 Usuarios, roles y acceso



Implementado: empleados, cargos, usuarios, rol permanente, activo/inactivo, archivado, último acceso, login local con JWT. ADMINISTRADOR accede a todos los paneles con la misma identidad.



Implementado: función temporal del turno (MESERO, COCINA, CAJA; SUPERVISOR solo si lo asigna un ADMINISTRADOR), expulsar del personal, y acceso al panel según `funcionActual` sin cambiar el rol permanente.



\## M02 Unidades, turnos e Inicio



Implementado: horarios base por unidad (`America/Lima`), apertura automática, apertura al arrancar si la ventana está vigente, inicio y cierre manual, responsable `Apertura automática` o quien abrió, quién cerró, personal por inicios de sesión posteriores a la apertura, reingreso único.



Dashboard Inicio: personal activo, compras por recibir y alertas de stock son reales. Pedidos, ventas, ocupación de mesa, cajas y cocina esperan sus módulos.



\## M03 Compras y recepción



Implementado: proveedores (semilla inicial), archivado de proveedor, registro de compra con `Importe` de línea, estados Pendiente / Parcial / Recibida, recepción que sí incrementa stock y genera movimiento `ENTRADA`.



\## M04 Inventario y recetas



Implementado: insumos, stock mínimo, alertas, movimientos de entrada por recepción, armado de recetas y combos en Inventario. Pendiente: consumo automático al preparar pedidos.



\## M05 Productos y carta diaria



Implementado: productos, categorías, carta por unidad, combos sin prefijo `Combo` en el nombre de carta, precio compartido del ejecutivo, consulta de carta con detalles únicos, tablas compactas en Pollería y marinos.



\## M06 a M12



Mesas registradas y zonas existen. Atención, pedidos, cocina, caja, comprobantes, ventas y reportes operativos aún no alimentan Inicio ni el flujo de servicio.

