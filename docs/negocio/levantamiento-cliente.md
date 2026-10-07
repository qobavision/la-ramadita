\# Levantamiento del Cliente - La Ramadita



\## 1. Objetivo



Este documento resume las necesidades expresadas por el stakeholder del Restaurante y Pollería La Ramadita durante el levantamiento realizado al negocio.



La fuente original se encuentra en:



`docs/fuentes/Cuestionario\_Stakeholder\_La\_Ramadita.docx`



Este documento sirve como contexto para comprender el funcionamiento real del negocio.



No reemplaza:



\- `AGENTS.md`

\- `docs/arquitectura/arquitectura-sistema.md`

\- `docs/requisitos/requisitos-sistema.md`

\- `docs/negocio/reglas-negocio.md`



\---



\# 2. Funcionamiento actual



\## Atención en mesa



Actualmente el proceso es principalmente manual.



Flujo aproximado:



1\. El cliente elige una mesa.

2\. Consulta la carta o la pizarra.

3\. El Mesero anota el pedido en un ticket.

4\. El ticket se entrega a Cocina.

5\. Cocina prepara el pedido.

6\. Cuando el cliente termina, solicita la cuenta.

7\. El personal revisa o recuerda lo consumido para calcular el cobro.

8\. Se realiza el pago.



Se identificó como problema que en ocasiones existen productos anunciados en la pizarra que ya no están disponibles.



\---



\## Pedidos para llevar



El proceso es similar.



El cliente:



\- solicita la carta o indica directamente su pedido;

\- el Mesero lo registra en un ticket;

\- Cocina prepara;

\- se entrega el pedido;

\- se realiza el cobro.



\---



\# 3. Problemas administrativos actuales



El control administrativo se realiza principalmente mediante cuadernos.



El negocio revisa información semanalmente para conocer:



\- cuánto se vendió por día;

\- gastos de insumos;

\- pagos al personal;

\- diferencias entre ingresos y gastos.



Existe dificultad para consultar información histórica de varios meses atrás.



El stakeholder desea tener mayor control y acceso a información histórica.



\---



\# 4. Restaurante y Pollería



Ambos funcionan en el mismo establecimiento.



Según las respuestas del stakeholder:



\- Restaurante: aproximadamente de 7:00 a. m. a 5:00 p. m.

\- Pollería: aproximadamente de 6:00 p. m. a 11:00 p. m.



Se desea controlar ambas operaciones de manera diferenciada.



Entre la información que interesa separar se encuentra:



\- ingresos;

\- gastos;

\- carta;

\- resultados;

\- consumo.



También resulta útil poder consultar el resultado general del negocio.



\---



\# 5. Compras



La Supervisora es quien normalmente realiza o controla las compras.



Se indicó que algunas compras pueden realizarse para periodos amplios, pero también existen compras adicionales imprevistas que deben registrarse durante el día.



La Supervisora también revisa el estado de los productos recibidos.



\---



\# 6. Inventario



El stakeholder considera importante controlar todos los insumos posibles.



Entre los mencionados se encuentran:



\- pollo;

\- papa;

\- aceite;

\- leche;

\- azúcar;

\- verduras;

\- gaseosas;

\- agua.



Se desea conocer cuánto consume:



\- Restaurante;

\- Pollería.



También se considera útil recibir avisos cuando un insumo esté cerca de agotarse.



\---



\# 7. Carta diaria



La carta puede cambiar según el día.



Por esta razón, el encargado o la Supervisora necesita poder actualizarla con facilidad.



Cuando un producto ya no está disponible, el stakeholder propone que:



\- aparezca en gris;

\- indique `Agotado`;

\- no permita agregarlo a nuevos pedidos.



También existe una necesidad importante relacionada con la impresión de la carta.



Actualmente la carta puede prepararse manualmente, lo que consume tiempo.



El negocio cuenta con impresora y desea que el sistema facilite la generación e impresión de la carta diaria.



\---



\# 8. Registro de pedidos



El stakeholder desea que el Mesero pueda registrar:



\- mesa;

\- opción Para llevar;

\- platos;

\- bebidas;

\- cantidades;

\- observaciones.



También debe ser posible agregar nuevos productos posteriormente a la misma atención.



Ejemplo:



Un cliente pide inicialmente comida y posteriormente agrega bebidas.



Los nuevos productos deben mantenerse dentro de la misma cuenta.



\---



\# 9. División de cuentas



Es frecuente que grupos de clientes soliciten pagar por separado.



El stakeholder considera importante que el sistema permita dividir una cuenta después de haber registrado todos los consumos.



La división puede necesitar realizarse entre diferentes integrantes de una misma mesa.



\---



\# 10. Cocina



El establecimiento cuenta con dos pisos.



Existe una cocina en cada piso.



Según el stakeholder:



\- la cocina del primer piso tiene menor capacidad;

\- la cocina principal se encuentra en el segundo piso;

\- existe una cocinera en cada piso.



Las cocineras se encargan de las operaciones relacionadas con Cocina.



\---



\# 11. Estados necesarios en Cocina



El stakeholder considera principalmente necesario:



\- saber claramente cuándo llega un pedido nuevo;

\- visualizar cuándo un pedido está preparado.



No considera obligatorio utilizar un estado intermedio:



`En preparación`



También existe una necesidad importante relacionada con cambios de pedido.



Puede ocurrir que:



1\. el Mesero envíe un pedido;

2\. Cocina lo vea;

3\. el cliente solicite un cambio pocos segundos después.



El sistema debe permitir distinguir claramente esos cambios.



El stakeholder también mencionó la posibilidad de utilizar sonidos diferentes para:



\- pedidos nuevos;

\- pedidos modificados;

\- pedidos cancelados.



\---



\# 12. Aviso al Mesero



Cuando Cocina termine una preparación, el Mesero debe recibir un aviso.



Esto permitirá que pueda recoger el pedido y entregarlo al cliente.



\---



\# 13. Comandas



Además de la pantalla digital de Cocina, el stakeholder considera útil disponer de la opción de imprimir comandas.



La impresión funciona como apoyo al proceso operativo.



\---



\# 14. Cobro actual



Actualmente los Meseros realizan los cobros.



En efectivo, el dinero se deja en Caja.



Los otros métodos se gestionan mediante los medios disponibles del negocio.



Esta descripción corresponde al funcionamiento actual del establecimiento.



Los permisos finales del sistema deben consultarse en los documentos oficiales de requisitos y reglas de negocio.



\---



\# 15. Métodos de pago



El negocio utiliza:



\- Efectivo;

\- Yape;

\- Plin;

\- Tarjeta;

\- Transferencia.



El stakeholder desea poder diferenciar cuánto dinero se recibió mediante cada método.



\---



\# 16. Comprobantes actuales



Actualmente no todos los clientes solicitan boleta o factura.



Cuando se solicita un comprobante electrónico, este es gestionado mediante la contadora.



Para documentos físicos puede utilizarse una nota de pedido en talonario.



El stakeholder considera que esta área puede mejorarse mediante el sistema.



La versión inicial del proyecto contempla comprobantes simulados, no integración tributaria real.



\---



\# 17. Caja



El stakeholder considera necesario registrar movimientos adicionales que puedan producirse durante el turno.



Entre ellos pueden encontrarse:



\- ingresos;

\- egresos.



También desea comparar al cierre:



\- cuánto efectivo debería existir según el sistema;

\- cuánto efectivo existe realmente.



\---



\# 18. Información y reportes



Entre la información solicitada se encuentra:



\- ventas del día;

\- platos más vendidos;

\- rango horario con mayor cantidad de ventas.



También se desea comparar:



\- Restaurante;

\- Pollería;

\- resultado general.



\---



\# 19. Información histórica



El stakeholder considera importantes distintos periodos de consulta.



La revisión detallada suele realizarse semanalmente.



Sin embargo, actualmente existe dificultad para consultar información de hace varios meses porque los datos se encuentran en cuadernos físicos que van siendo reemplazados.



El sistema debe permitir conservar históricos.



\---



\# 20. Usuarios del sistema



Los tipos de trabajadores identificados son:



\- Dueño;

\- Supervisor;

\- Meseros;

\- Cocinero;

\- Caja.



Estos roles sirven como base para definir los permisos del sistema.



Nota: en el sistema, el rol del dueño se denomina ADMINISTRADOR.



Nota: los tipos de trabajador listados son cargos laborales. En el sistema, el cargo (función laboral) es distinto del rol (permisos). No todos los trabajadores tendrán usuario; las decisiones vigentes están en `docs/requisitos/requisitos-sistema.md` (M01) y `docs/negocio/reglas-negocio.md` (RN-36 a RN-40).



\---



\# 21. Tecnología disponible



Durante el levantamiento se indicó que el establecimiento todavía posee infraestructura tecnológica limitada.



También utilizan autoradios como medio importante de comunicación interna debido a la distancia entre trabajadores dentro del establecimiento.



El proyecto contempla incorporar progresivamente:



\- computadoras;

\- tablets;

\- impresoras;

\- red local.



\---



\# 22. Funcionamiento sin Internet



Esta necesidad fue considerada especialmente importante por el stakeholder.



El negocio ha experimentado interrupciones de Internet durante el año.



Por esta razón, se desea que las funciones principales puedan continuar trabajando aunque no exista conexión a Internet.



La solución propuesta utilizará la red local del establecimiento para las operaciones esenciales.



\---



\# 23. Necesidades principales resumidas



A partir del levantamiento se identifican como necesidades prioritarias:



1\. Digitalizar los pedidos.

2\. Evitar depender de tickets de papel.

3\. Mejorar la comunicación entre Meseros y Cocina.

4\. Registrar cambios y cancelaciones de pedidos de forma clara.

5\. Mantener actualizada la carta diaria.

6\. Identificar productos agotados.

7\. Controlar inventario.

8\. Recibir alertas de stock.

9\. Diferenciar Restaurante y Pollería.

10\. Registrar varios métodos de pago.

11\. Permitir división de cuentas.

12\. Controlar Caja.

13\. Conservar información histórica.

14\. Obtener reportes de ventas.

15\. Identificar productos y horarios con mayor demanda.

16\. Facilitar la impresión de la carta.

17\. Continuar operando aunque falle Internet.



\---



\# 24. Uso de este documento durante el desarrollo



Antes de implementar una funcionalidad relacionada con la operación del negocio, este documento puede utilizarse para comprender el motivo detrás del requisito.



Sin embargo:



\- este documento describe principalmente necesidades del stakeholder;

\- no define por sí solo la arquitectura técnica;

\- no reemplaza los requisitos oficiales;

\- no reemplaza las reglas de negocio;

\- no reemplaza las decisiones establecidas en `AGENTS.md`.



Si una necesidad expresada aquí contradice una decisión posterior del proyecto, debe revisarse la documentación oficial antes de implementar.

