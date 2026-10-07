\# Arquitectura del Sistema - La Ramadita



\## 1. Objetivo



Este documento resume la arquitectura técnica oficial del Sistema Integral de Gestión del Restaurante y Pollería La Ramadita.



Debe utilizarse como referencia para el desarrollo del frontend, backend, base de datos y comunicación entre dispositivos.



Las reglas generales del proyecto se encuentran también en el archivo `AGENTS.md`.



\---



\## 2. Tipo de arquitectura



El sistema utilizará una arquitectura de:



\*\*Monolito modular.\*\*



No se utilizarán microservicios en esta etapa.



El backend será una sola aplicación Spring Boot organizada internamente por módulos de negocio.



Ejemplos:



\- seguridad;

\- usuarios;

\- unidades de negocio;

\- turnos;

\- compras;

\- inventario;

\- productos;

\- mesas;

\- pedidos;

\- cocina;

\- caja;

\- ventas;

\- reportes;

\- auditoría.



\---



\## 3. Arquitectura general



El flujo principal será:



Frontend Angular  

↓  

API REST / WebSocket  

↓  

Backend Spring Boot  

↓  

SQL Server



El frontend nunca debe conectarse directamente a la base de datos.



Toda operación importante debe pasar por el backend.



\---



\## 4. Tecnologías oficiales



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

\- Spring Data JPA

\- Spring Security

\- JWT

\- Jakarta Validation

\- WebSocket



\### Base de datos



\- Microsoft SQL Server 2025 Express

\- Flyway para migraciones



\### Futuro



La aplicación podrá posteriormente utilizar Azure SQL u otra infraestructura en nube sin cambiar la arquitectura principal del sistema.



\---



\## 5. Funcionamiento local



La primera versión será \*\*local-first\*\*.



Las funciones esenciales deben funcionar dentro de la red local aunque no exista Internet.



Esto incluye:



\- inicio de sesión;

\- mesas;

\- pedidos;

\- cocina;

\- caja;

\- pagos;

\- inventario.



Todos los dispositivos autorizados se conectarán mediante la red LAN o Wi-Fi del establecimiento.



\---



\## 6. Distribución física inicial



\### PC 1 - Caja / Administración



Inicialmente podrá ejecutar:



\- backend Spring Boot;

\- SQL Server;

\- caja;

\- administración;

\- productos;

\- inventario;

\- ventas;

\- reportes;

\- Business Intelligence.



Esta PC funcionará inicialmente como servidor local.



\### PC 2 - Cocina



Accederá al sistema mediante navegador.



Permitirá principalmente:



\- visualizar pedidos;

\- visualizar modificaciones;

\- marcar productos preparados;

\- consultar pedidos pendientes;

\- recibir actualizaciones en tiempo real.



\### Tablets - Meseros



Se utilizarán principalmente para:



\- consultar mesas;

\- iniciar atención;

\- registrar pedidos;

\- agregar productos;

\- consultar cuenta;

\- recibir avisos de cocina.



No ejecutarán directamente lógica de negocio sensible.



\---



\## 7. Comunicación entre dispositivos



La comunicación normal utilizará:



\### HTTP / REST



Para operaciones como:



\- inicio de sesión;

\- consultas;

\- creación;

\- modificación;

\- eliminación lógica;

\- configuración;

\- reportes.



\### WebSocket



Para operaciones que deben actualizarse prácticamente en tiempo real.



Ejemplos:



\- nuevo pedido enviado a cocina;

\- pedido modificado;

\- pedido cancelado;

\- producto preparado;

\- aviso al mesero;

\- cambio de estado de mesa;

\- actualización de pedidos pendientes de cobro.



No utilizar recarga manual de pantalla como solución definitiva para estos procesos.



\---



\## 8. Unidades de negocio



El sistema utilizará una sola aplicación y una sola base de datos.



Existirán dos unidades de negocio:



\- Restaurante

\- Pollería



Las operaciones deberán identificar la unidad correspondiente cuando aplique.



Ejemplos:



\- turnos;

\- pedidos;

\- ventas;

\- caja;

\- pagos;

\- consumos;

\- reportes.



El sistema también debe permitir consultar información consolidada.



\---



\## 9. Seguridad



Cada trabajador debe utilizar un usuario individual.



Roles principales:



\- ADMINISTRADOR

\- SUPERVISOR

\- MESERO

\- COCINA

\- CAJA



Los permisos deben verificarse en backend mediante Spring Security.



Ocultar una opción en Angular no reemplaza la validación de permisos en backend.



Cada usuario tiene un único rol permanente en esta primera versión. Solo ADMINISTRADOR crea usuarios y modifica roles permanentes.



ADMINISTRADOR puede acceder a los paneles de los demás roles con su misma sesión; los otros roles solo acceden a su propio panel. Esta regla se aplica en los guards del frontend y debe respetarse también en los permisos del backend.



Las contraseñas deberán almacenarse mediante hash seguro.



Los secretos y credenciales deben utilizar variables de entorno.



\---



\## 10. Base de datos



La base de datos oficial es:



\*\*Microsoft SQL Server 2025\*\*



La estructura del esquema se controla mediante Flyway.



Ruta oficial de migraciones:



`backend/src/main/resources/db/migration/`



La migración inicial es:



`V1\_\_estructura\_inicial.sql`



Las migraciones ejecutadas no deben modificarse.



Cualquier cambio posterior deberá realizarse mediante una nueva migración:



\- `V2\_\_descripcion.sql`

\- `V3\_\_descripcion.sql`

\- etc.



Hibernate se utilizará para trabajar con entidades y validar el esquema, no para modificar automáticamente la estructura.



Configuración esperada:



`spring.jpa.hibernate.ddl-auto=validate`



\---



\## 11. Configuración de red



El sistema no debe asumir que todos los dispositivos utilizan `localhost`.



Ejemplo:



Si el backend se ejecuta en la PC servidor:



`localhost:8080`



solo funciona desde esa misma PC.



Una tablet deberá acceder mediante la dirección de red de la PC servidor.



Por esta razón, la URL del backend debe configurarse según el entorno y no escribirse directamente dentro de los componentes Angular.



\---



\## 12. Organización del frontend



El frontend debe dividirse en componentes y servicios.



No concentrar toda la aplicación en `AppComponent`.



Ejemplo conceptual:



frontend

├── core

├── shared

├── auth

├── usuarios

├── productos

├── mesas

├── pedidos

├── cocina

├── caja

├── inventario

└── reportes



Las llamadas HTTP deben centralizarse en servicios.



\---



\## 13. Organización del backend



El backend debe mantenerse modular.



Ejemplo conceptual:



backend

├── seguridad

├── usuarios

├── unidadesnegocio

├── turnos

├── compras

├── inventario

├── productos

├── mesas

├── pedidos

├── cocina

├── caja

├── ventas

├── reportes

└── auditoria



Cada módulo debe mantener responsabilidades claras.



No crear dependencias innecesarias entre módulos.



\---



\## 14. Impresión



El sistema contempla posteriormente impresión de:



\- comandas de cocina;

\- comandas de barra;

\- precuentas;

\- tickets;

\- comprobantes simulados;

\- carta diaria.



La impresión debe mantenerse desacoplada de la lógica principal del pedido.



Una falla de impresión no debe provocar pérdida del pedido registrado.



\---



\## 15. Evolución futura



La arquitectura debe permitir que posteriormente:



\- el backend sea desplegado en Internet;

\- SQL Server pueda migrarse a Azure SQL;

\- el propietario tenga acceso remoto;

\- se utilice HTTPS;

\- existan respaldos externos;

\- se mantenga un mecanismo local para impresión.



No se implementará por ahora una sincronización compleja entre base de datos local y nube.



\---



\## 16. Decisiones que reemplazan documentos anteriores



Algunos documentos iniciales mencionaron PostgreSQL o MySQL como posibles bases de datos.



Esa decisión fue reemplazada.



La tecnología oficial actual es:



\*\*Microsoft SQL Server 2025 Express.\*\*



También quedan establecidas como tecnologías oficiales:



\- Angular 22;

\- Spring Boot 4.1.1;

\- Java 21;

\- Flyway;

\- WebSockets.



Estas decisiones prevalecen sobre propuestas técnicas anteriores.



\---



\## 17. Principio de arquitectura



La arquitectura debe mantenerse:



\- simple;

\- modular;

\- local-first;

\- segura;

\- mantenible;

\- preparada para crecimiento futuro.



No agregar complejidad técnica que no sea necesaria para los requisitos actuales del proyecto.

