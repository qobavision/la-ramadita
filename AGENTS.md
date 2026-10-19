\# AGENTS.md



\# Proyecto La Ramadita



Este repositorio contiene el proyecto oficial del:



Sistema Integral de Gestión para Restaurante y Pollería La Ramadita.



Este archivo define las reglas que deben seguir Cursor, OpenCode, Codex y cualquier otro agente de IA que trabaje sobre este repositorio.



Antes de modificar código, leer este archivo completo.



\---



\## 1. Regla principal



No asumir requisitos ni inventar funcionalidades.



Antes de realizar cambios importantes:



1\. Revisar este archivo.

2\. Revisar la documentación disponible en `docs/`.

3\. Revisar el código existente.

4\. Explicar brevemente qué se modificará.

5\. Recién después implementar.



No reemplazar estructuras existentes sin justificarlo.



\---



\## 2. Proyecto oficial



El proyecto oficial es este repositorio:



`la-ramadita`



La estructura principal es:



\- `frontend/` → aplicación Angular.

\- `backend/` → API Spring Boot.

\- `database/` → documentación, semillas y diagramas relacionados con base de datos.

\- `docs/` → documentación funcional, técnica y fuentes del proyecto.



El código externo de otros integrantes debe considerarse material de referencia hasta que sea revisado y adaptado.



Nunca reemplazar automáticamente el proyecto oficial con otro proyecto o ZIP.



\---



\## 3. Tecnologías oficiales



Las tecnologías acordadas para este proyecto son:



\### Frontend



\- Angular 22

\- TypeScript

\- SCSS

\- Bootstrap 5



\### Backend



\- Java 21 LTS

\- Spring Boot 4.1.1

\- Maven

\- Spring Web

\- Spring Data JPA / Hibernate

\- Spring Security

\- JWT para autenticación

\- Jakarta Validation

\- WebSocket



\### Base de datos



\- Microsoft SQL Server 2025 Express para desarrollo local.

\- Azure SQL podrá evaluarse posteriormente para despliegue en nube.

\- Flyway para control de cambios de estructura.



\### Herramientas



\- Git

\- GitHub

\- Cursor / OpenCode / Codex

\- Postman

\- SQL Server Management Studio



No cambiar estas tecnologías sin una decisión explícita del equipo.



\---



\## 4. Arquitectura oficial



El sistema utiliza una arquitectura de:



\*\*Monolito modular.\*\*



No utilizar microservicios en esta etapa.



El sistema debe funcionar inicialmente dentro de la red local del establecimiento.



Arquitectura general:



Frontend Angular

&#x20;       ↓

API REST / WebSocket

&#x20;       ↓

Spring Boot

&#x20;       ↓

SQL Server



El frontend nunca debe conectarse directamente a SQL Server.



Toda validación importante de negocio debe realizarse también en el backend.



\---



\## 5. Funcionamiento local-first



Las funciones esenciales deben poder trabajar dentro de la red local aunque no exista conexión a Internet.



Esto incluye principalmente:



\- inicio de sesión local;

\- mesas;

\- pedidos;

\- cocina;

\- pagos;

\- caja;

\- inventario.



Inicialmente una PC podrá ejecutar backend y base de datos.



Las demás computadoras y tablets accederán al sistema mediante la red LAN/Wi-Fi.



No diseñar funcionalidades esenciales que dependan obligatoriamente de Internet.



\---



\## 6. Unidades de negocio



El sistema administra dos unidades dentro de una sola aplicación y una sola base de datos:



\- Restaurante

\- Pollería



Las operaciones deben poder identificarse por su unidad de negocio cuando corresponda.



Esto aplica principalmente a:



\- turnos;

\- pedidos;

\- ventas;

\- pagos;

\- caja;

\- inventario;

\- reportes.



También debe ser posible consultar información consolidada.



\---



\## 7. Roles principales



Los roles del sistema son:



\- ADMINISTRADOR (rol con control total; en la documentación original aparecía como DUEÑO y fue renombrado por decisión del equipo mediante la migración V2)

\- SUPERVISOR

\- MESERO

\- COCINA

\- CAJA



Los permisos deben validarse en backend.



No confiar únicamente en ocultar botones en frontend.



El frontend puede ocultar opciones según el rol, pero el backend debe impedir cualquier acción no autorizada.



\### Decisiones del equipo sobre roles y accesos



\- DUEÑO equivale oficialmente a ADMINISTRADOR en toda la documentación, el código y la base de datos.

\- ADMINISTRADOR tiene control general del sistema: gestiona empleados, usuarios y accesos permanentes; consulta Dashboard, Reportes / BI, ventas históricas, control de caja e información global; y puede entrar a los paneles operativos (supervisión, mesero, cocina y caja) con su misma identidad, sin cambiar de usuario ni de rol.

\- SUPERVISOR no crea usuarios, no modifica cuentas ni asigna roles permanentes. Dentro de un turno abierto puede asignar función temporal MESERO, COCINA o CAJA y expulsar a esa persona. No puede asignar SUPERVISOR ni gestionar a un ADMINISTRADOR.

\- EMPLEADO y USUARIO son entidades distintas: empleado es cualquier trabajador de La Ramadita; usuario es una cuenta de acceso al sistema vinculada a un empleado existente. No todos los empleados tienen usuario.

\- El username es el identificador para iniciar sesión. Es distinto del correo personal del empleado.

\- CARGO y ROL son conceptos distintos. El cargo es la función laboral. Cargos: Supervisor, Cajero, Ayudante de cajero, Cocinero, Ayudante de cocina, Mesero, Motorizado, Almacenero, Seguridad, Otro. Administrador no es un cargo: es el rol ADMINISTRADOR. El rol define los permisos dentro del sistema (ADMINISTRADOR, SUPERVISOR, MESERO, COCINA, CAJA).

\- En esta primera versión cada usuario tiene un único rol permanente. Solo ADMINISTRADOR crea usuarios, activa o inactiva cuentas y cambia el rol permanente. SUPERVISOR no gestiona usuarios ni roles permanentes.

\- Activo/inactivo del empleado significa si trabaja o no trabaja actualmente. Activo/inactivo del usuario significa si puede o no puede iniciar sesión. Son estados independientes.

\- Archivado es la baja lógica de Eliminar: no hay eliminación física. El registro conserva historial, sale del listado operativo y no se reactiva desde Editar. Al archivar se guarda la fecha y hora de esa baja.

\- Inactivar un empleado inactiva su usuario en la misma operación. Archivar un empleado archiva también su usuario. Reactivar el empleado no reactiva la cuenta: el ADMINISTRADOR lo hace aparte. No se crea usuario para un empleado inactivo o archivado.

\- Último acceso del usuario es el último inicio de sesión exitoso.

\- Las asignaciones temporales del turno (MESERO, COCINA, CAJA; SUPERVISOR solo si lo asigna un ADMINISTRADOR) están implementadas. No cambian el rol permanente. `/auth/me` expone `funcionActual` y esa función otorga el panel operativo correspondiente. Expulsar borra la asistencia; un nuevo login la vuelve a registrar si el turno sigue abierto.



\---



\## 8. Módulos funcionales



El proyecto contempla los siguientes módulos:



\- M01 Usuarios, roles y acceso

\- M02 Unidades de negocio y turnos

\- M03 Compras y recepción de insumos

\- M04 Inventario y recetas

\- M05 Productos y carta diaria

\- M06 Mesas y atención

\- M07 Pedidos

\- M08 Cocina

\- M09 Caja, pagos y división de cuentas

\- M10 Comprobantes simulados y control de caja

\- M11 Ventas e historial

\- M12 Reportes y Business Intelligence



Antes de implementar una funcionalidad identificar a qué módulo pertenece.



\---



\## 9. Reglas importantes del negocio



No modificar estas reglas sin revisar primero los requisitos del proyecto.



Una mesa ocupada no puede iniciar otra atención.



Mientras un pedido esté en borrador puede modificarse libremente.



Después de enviar un pedido a cocina, los cambios deben identificarse claramente.



Un producto agotado no puede agregarse a nuevos pedidos.



Una cuenta puede recibir varios pagos y diferentes métodos de pago.



Una cuenta solo queda pagada cuando su saldo llega a cero.



Una mesa vuelve a estar disponible después de completar el pago.



Registrar una compra no debe aumentar automáticamente el inventario.



El inventario aumenta cuando se confirma la recepción de la compra.



El turno no se abre al primer inicio de sesión. Se abre por horario (`America/Lima`) o al arrancar el sistema si la ventana está vigente. La apertura automática no asigna supervisor (`Apertura automática`).



El personal del turno son quienes inician sesión con el turno ya abierto. Reingresar no duplica la asistencia.



La función temporal no cambia el rol permanente; otorga el panel de esa función durante el turno.



Las operaciones importantes deben conservar trazabilidad e historial.



\---



\## 10. Tiempo real



Las operaciones que requieran actualización inmediata deben utilizar WebSockets cuando corresponda.



Ejemplos:



\- pedido enviado por mesero → aparece en cocina;

\- cocina marca producto preparado → aviso al mesero;

\- actualización del estado de una mesa;

\- pedidos pendientes de cobro;

\- cambios relevantes en operación.



No implementar actualización mediante recarga manual de página como solución definitiva.



\---



\## 11. Base de datos y Flyway



La fuente oficial de migraciones es:



`backend/src/main/resources/db/migration/`



Actualmente existen:



`V1\\\_\\\_estructura\\\_inicial.sql`

`V2\\\_\\\_renombrar\\\_rol\\\_dueno\\\_a\\\_administrador.sql`

`V3\\\_\\\_empleados\\\_cargos\\\_y\\\_vinculo\\\_usuario.sql`

`V4\\\_\\\_archivado\\\_y\\\_ultimo\\\_acceso.sql`

`V5\\\_\\\_fecha\\\_archivado.sql`

`V6\\\_\\\_cargos\\\_ayudante\\\_cajero\\\_y\\\_motorizado.sql`

`V7\\\_\\\_compras\\\_recepcion\\\_y\\\_trazabilidad.sql`

`V8\\\_\\\_productos\\\_cartas\\\_componentes.sql`

`V9\\\_\\\_zonas\\\_capacidad\\\_y\\\_turnos.sql`

`V10\\\_\\\_categorias\\\_plato\\\_especificas.sql`

`V11\\\_\\\_proveedores\\\_archivado.sql`

`V12\\\_\\\_turno\\\_apertura\\\_automatica\\\_y\\\_asistencia.sql`

`V13\\\_\\\_turno\\\_asistencia\\\_funcion\\\_temporal.sql`

`V14\\\_\\\_turno\\\_funcion\\\_temporal\\\_supervisor.sql`



\### Regla crítica



Una migración que ya fue integrada o ejecutada NO debe modificarse.



Si se necesita cambiar la base de datos, crear una migración nueva:



`V2\\\_\\\_descripcion.sql`



`V3\\\_\\\_descripcion.sql`



etc.



Antes de elegir el siguiente número de migración actualizar primero `develop` para evitar conflictos con migraciones creadas por otros integrantes.



No usar `spring.jpa.hibernate.ddl-auto=create` ni `update` para modificar automáticamente el esquema.



La configuración oficial utiliza:



`spring.jpa.hibernate.ddl-auto=validate`



Flyway controla la estructura de la base de datos.



\---



\## 12. Credenciales y secretos



Nunca guardar en Git:



\- contraseñas;

\- tokens;

\- claves privadas;

\- credenciales SQL Server;

\- secretos JWT;

\- archivos `.env` con valores reales.



Utilizar variables de entorno.



Nunca escribir una contraseña real dentro de:



\- `application.properties`;

\- código Java;

\- código Angular;

\- documentación versionada.



\---



\## 13. Configuración del frontend



No escribir direcciones como:



`http://localhost:8080`



directamente en componentes o servicios de negocio.



Recordar que una tablet interpreta `localhost` como la propia tablet.



La dirección del backend debe manejarse mediante configuración adecuada para cada entorno.



\---



\## 14. Organización del backend



Mantener el backend organizado por módulos o dominios del negocio.



Evitar crear un único paquete gigante con todo el sistema.



Los módulos deben mantener responsabilidades claras.



Ejemplos de dominios:



\- seguridad

\- usuarios

\- unidadesnegocio

\- turnos

\- compras

\- inventario

\- productos

\- mesas

\- pedidos

\- cocina

\- caja

\- ventas

\- reportes

\- auditoria



No crear dependencias innecesarias entre módulos.



\---



\## 15. Organización del frontend



Evitar concentrar toda la aplicación en `AppComponent`.



Crear componentes y servicios según responsabilidad.



Ejemplos:



\- login;

\- layout;

\- mesas;

\- pedidos;

\- cocina;

\- caja;

\- productos;

\- inventario;

\- reportes.



Los componentes no deben contener grandes cantidades de datos simulados cuando ya exista backend disponible.



Centralizar las llamadas HTTP en servicios.



\---



\## 16. Código de otros integrantes



Puede existir código desarrollado previamente por otros integrantes del equipo.



Ese código debe considerarse referencia hasta ser revisado.



Antes de reutilizarlo:



1\. Identificar qué funcionalidad implementa realmente.

2\. Compararlo con los requisitos.

3\. Revisar compatibilidad con las tecnologías oficiales.

4\. Revisar compatibilidad con nuestra base de datos.

5\. Adaptarlo a nuestra estructura.

6\. Probarlo.

7\. Integrarlo mediante Git.



No copiar automáticamente:



\- `pom.xml`;

\- `package.json`;

\- configuraciones completas;

\- migraciones Flyway;

\- archivos de entorno;

\- estructuras completas de otro proyecto.



Se puede reutilizar lógica, componentes, diseños y funcionalidades útiles después de adaptarlos.



El objetivo es conservar el avance válido sin romper la arquitectura oficial.



\---



\## 17. Fuentes originales del proyecto



Los documentos originales se encuentran en:



`docs/fuentes/`



Actualmente:



\- `Informe\\\_Base\\\_Proyecto\\\_La\\\_Ramadita\\\_v01.docx`

\- `Cuestionario\\\_Stakeholder\\\_La\\\_Ramadita.docx`

\- `Arquitectura\\\_final.pdf`



Estos documentos son fuentes de contexto y evidencia.



No todas sus decisiones técnicas tienen prioridad sobre las decisiones posteriores del equipo.



Ejemplo:



La arquitectura original menciona PostgreSQL o MySQL como posibilidad.



La decisión oficial posterior del equipo es utilizar Microsoft SQL Server.



Por lo tanto se utiliza SQL Server.



\---



\## 18. Prioridad cuando exista una contradicción



Si diferentes fuentes se contradicen, utilizar este orden:



1\. Decisiones actuales aprobadas y documentadas en `AGENTS.md`.

2\. Documentación técnica actual del repositorio.

3\. Requisitos y reglas de negocio validadas.

4\. Cuestionario del stakeholder.

5\. Documentos originales en `docs/fuentes/`.

6\. Código antiguo o código externo de otros integrantes.



Si la contradicción afecta una regla importante del negocio, detener la implementación y solicitar confirmación.



\---



\## 19. Git



No trabajar directamente sobre:



\- `main`

\- `develop`



Crear una rama para cada cambio.



Ejemplos:



`feature/login`



`feature/productos`



`feature/mesas`



`fix/...`



`docs/...`



`chore/...`



Flujo normal:



`develop`

→ crear rama

→ desarrollar

→ probar

→ commit

→ push

→ Pull Request hacia `develop`



`main` representa versiones estables.



No hacer force push sobre `main` o `develop`.



\---



\## 20. Uso de agentes de IA



Los agentes pueden:



\- analizar código;

\- crear o modificar archivos;

\- ejecutar pruebas;

\- ejecutar compilaciones;

\- revisar errores;

\- preparar commits;

\- proponer mejoras.



Antes de cambios grandes deben explicar qué archivos modificarán.



No realizar automáticamente:



\- merge a `develop`;

\- merge a `main`;

\- cambios destructivos en la base de datos;

\- eliminación masiva de código;

\- reemplazo completo de arquitectura;

\- modificación de migraciones Flyway ya ejecutadas.



El desarrollador debe revisar los cambios antes de integrarlos.



\---



\## 21. Antes de considerar una tarea terminada



Verificar como mínimo:



\- el proyecto compila;

\- no existen errores evidentes;

\- la funcionalidad cumple el requisito correspondiente;

\- frontend y backend utilizan contratos coherentes;

\- no se agregaron credenciales;

\- no se modificaron migraciones antiguas;

\- los cambios pertenecen a la rama correcta.



\---



\## 22. Principio general



Priorizar:



\- simplicidad;

\- claridad;

\- mantenibilidad;

\- coherencia con requisitos;

\- reutilización responsable del código existente.



No agregar complejidad únicamente porque una herramienta o IA pueda hacerlo.



El objetivo es construir un sistema funcional, comprensible y mantenible por el equipo del proyecto.

