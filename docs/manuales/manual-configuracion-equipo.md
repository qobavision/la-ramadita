# Manual sencillo para trabajar en equipo
## Sistema Integral de Gestión - Restaurante y Pollería La Ramadita

Este manual explica **solo lo necesario** para que cualquier integrante pueda preparar su computadora, abrir el proyecto y trabajar sin dañar el código de los demás.

> **Idea principal:** nadie trabaja directamente en `main` ni en `develop`. Cada tarea se hace en una rama propia y luego se integra con un Pull Request.

---

# 1. Cómo está organizado el proyecto

El repositorio es:

`https://github.com/qobavision/la-ramadita.git`

La estructura principal es:

```text
la-ramadita/
|
|-- frontend/      -> Angular: pantallas y parte visual
|-- backend/       -> Spring Boot: lógica, API y conexión a la BD
|-- database/      -> documentación y archivos auxiliares de BD
|-- docs/          -> manuales y documentación del proyecto
|-- .gitignore
`-- README.md
```

La carpeta más importante para los cambios de base de datos es:

```text
backend/src/main/resources/db/migration/
```

Ahí están las migraciones de Flyway. Actualmente la base inicial se crea con:

```text
V1__estructura_inicial.sql
```

## Ramas principales

- `main`: versión estable del proyecto.
- `develop`: versión donde se integran los cambios del equipo.
- `feature/...`: ramas para desarrollar funcionalidades nuevas.
- `chore/...`: ramas para tareas técnicas o de mantenimiento.
- `docs/...`: ramas para documentación.

`main` y `develop` están protegidas. Los cambios deben entrar mediante **Pull Request**. No se necesita aprobación obligatoria de otro integrante, pero sí se debe usar el Pull Request.

---

# 2. Programas que debe tener cada integrante

Instalar una sola vez:

1. **Git**
2. **Node.js 24** (incluye npm)
3. **Java JDK 21 LTS**
4. **SQL Server 2025 Express**
5. **SQL Server Management Studio (SSMS)**
6. Un editor: **Cursor**, **VS Code** u otro de preferencia

No es necesario instalar Maven aparte porque el proyecto incluye **Maven Wrapper** (`mvnw.cmd`).

Comprobaciones rápidas en CMD:

```bat
git --version
node -v
npm -v
java -version
javac -version
```

Las versiones esperadas son Git instalado, Node 24 y Java 21.

---

# 3. Configurar Git por primera vez

Cada integrante debe usar su propio nombre y el correo asociado a su cuenta de GitHub.

```bat
git config --global user.name "TU NOMBRE"
git config --global user.email "TU CORREO DE GITHUB"
git config --global init.defaultbranch main
```

Qué hace cada comando:

- `user.name`: coloca tu nombre en los commits.
- `user.email`: coloca tu correo en los commits.
- `init.defaultbranch main`: usa `main` como nombre predeterminado para ramas iniciales.

---

# 4. Entrar al repositorio y clonarlo

Antes de clonar, el propietario del repositorio debe agregarte como colaborador y tú debes **aceptar la invitación en GitHub**.

Luego abre CMD en la carpeta donde quieras guardar el proyecto y ejecuta:

```bat
git clone -b develop https://github.com/qobavision/la-ramadita.git
cd la-ramadita
```

Comprueba que estés en `develop`:

```bat
git branch
git status
```

Debe aparecer `develop` como rama activa y el repositorio debe estar limpio.

---

# 5. Preparar SQL Server en una computadora nueva

Esta parte se hace **una sola vez por computadora**.

## 5.1 Activar autenticación mixta

En SSMS entra con **Windows Authentication**.

Luego:

```text
Servidor -> clic derecho -> Properties -> Security
```

Selecciona:

```text
SQL Server and Windows Authentication mode
```

Guarda los cambios y reinicia el servicio de SQL Server.

## 5.2 Habilitar TCP/IP y puerto 1433

Abre **SQL Server Configuration Manager**.

Para SQL Server 2025 normalmente puede abrirse con:

```text
SQLServerManager17.msc
```

Entra a:

```text
SQL Server Network Configuration
-> Protocols for SQLEXPRESS01
-> TCP/IP
```

Configura:

```text
Enabled: Yes
IPAll -> TCP Dynamic Ports: vacío
IPAll -> TCP Port: 1433
```

Después reinicia:

```text
SQL Server (SQLEXPRESS01)
```

Si tu instancia tiene otro nombre, usa el nombre que aparezca en tu instalación.

Para comprobar el puerto, abre PowerShell:

```powershell
Test-NetConnection localhost -Port 1433
```

Debe salir:

```text
TcpTestSucceeded : True
```

---

# 6. Crear la base local y el usuario de la aplicación

En SSMS, conectado con **Windows Authentication**, abre una nueva consulta y ejecuta lo siguiente.

Cambia `TU_PASSWORD_LOCAL_SEGURA` por una contraseña propia. **No compartas esa contraseña y no la subas a GitHub.**

```sql
USE master;
GO

CREATE LOGIN laramadita_app
WITH PASSWORD = 'TU_PASSWORD_LOCAL_SEGURA';
GO

CREATE DATABASE LaRamadita;
GO

USE LaRamadita;
GO

CREATE USER laramadita_app FOR LOGIN laramadita_app;
GO

ALTER ROLE db_owner ADD MEMBER laramadita_app;
GO
```

En este momento la base puede estar vacía. **No hay que crear las tablas manualmente.** Flyway las creará al iniciar el backend.

---

# 7. Configurar las variables de conexión

En CMD ejecuta:

```bat
setx DB_URL "jdbc:sqlserver://localhost:1433;databaseName=LaRamadita;encrypt=true;trustServerCertificate=true"
setx DB_USERNAME "laramadita_app"
setx DB_PASSWORD "TU_PASSWORD_LOCAL_SEGURA"
```

Después de usar `setx`, cierra CMD y abre una ventana nueva.

Comprueba la configuración:

```bat
echo %DB_URL%
echo %DB_USERNAME%
if defined DB_PASSWORD (echo DB_PASSWORD configurada) else (echo DB_PASSWORD NO configurada)
```

El último comando verifica que exista la contraseña **sin mostrarla**.

Nunca escribas una contraseña real dentro de `application.properties` ni la subas a GitHub.

## 7.1 Variables de seguridad (módulo M01)

El backend también necesita estas variables. Igual que las de conexión, se configuran una vez con `setx` y **nunca se escriben en archivos del repositorio**.

| Variable | Obligatoria | Para qué sirve |
|---|---|---|
| `JWT_SECRET` | Sí | Clave con la que se firman los tokens de sesión. Debe tener **al menos 32 caracteres**. Si falta, el backend no arranca. |
| `JWT_EXPIRATION_MINUTES` | No | Duración del token en minutos. Si no se define, se usan 480 (8 horas). |
| `APP_ADMIN_USERNAME` | Solo la primera vez | Usuario del ADMINISTRADOR inicial. |
| `APP_ADMIN_PASSWORD` | Solo la primera vez | Contraseña del ADMINISTRADOR inicial (mínimo 8 caracteres). |
| `CORS_ALLOWED_ORIGINS` | No | Dirección desde la que se sirve el frontend. Si no se define, se usa `http://localhost:4200`. En red local, poner la IP de la PC que sirve Angular. |

Ejemplo en CMD (reemplaza los valores por los tuyos; no uses estos):

```bat
setx JWT_SECRET "UNA_CLAVE_LARGA_Y_ALEATORIA_DE_32_O_MAS_CARACTERES"
setx APP_ADMIN_USERNAME "TU_USUARIO_ADMIN"
setx APP_ADMIN_PASSWORD "TU_PASSWORD_ADMIN_SEGURA"
```

Cómo funciona el ADMINISTRADOR inicial:

- El backend **solo** crea ese usuario cuando la tabla `usuarios` está vacía y las dos variables existen.
- Si la tabla está vacía y faltan las variables, el backend arranca igual y muestra una advertencia en consola.
- Cuando ya existe al menos un usuario, las variables se ignoran y puedes quitarlas de tu computadora.
- No existe ninguna contraseña por defecto en el código.

Después de usar `setx`, cierra CMD y abre una ventana nueva.

---

# 8. Levantar el proyecto por primera vez

Se recomienda usar **dos terminales**: una para Angular y otra para Spring Boot.

## Terminal 1 - Frontend

Desde la raíz del proyecto:

```bat
cd frontend
npm install
npm start
```

Qué hace:

- `npm install`: instala las dependencias del frontend.
- `npm start`: inicia Angular.

El frontend normalmente queda en:

```text
http://localhost:4200/
```

Para detenerlo: `Ctrl + C`.

## Terminal 2 - Backend

Desde la raíz del proyecto:

```bat
cd backend
mvnw.cmd spring-boot:run
```

Qué hace:

- compila el backend;
- se conecta a SQL Server;
- Flyway revisa la base de datos;
- ejecuta las migraciones que falten;
- inicia Spring Boot.

El backend normalmente queda en:

```text
http://localhost:8080/
```

La primera vez, si la base está vacía, Flyway ejecutará `V1__estructura_inicial.sql` y creará las tablas automáticamente.

Para detenerlo: `Ctrl + C`.

---

# 9. Qué hacer cada vez que vas a comenzar una tarea

Este es el flujo normal del equipo.

## Paso 1 - Ir a develop

```bat
git checkout develop
```

Cambia a la rama de integración.

## Paso 2 - Descargar los cambios más recientes

```bat
git pull origin develop
```

Trae lo último que subieron los demás.

## Paso 3 - Crear tu propia rama

Ejemplo para trabajar en mesas:

```bat
git checkout -b feature/gestion-mesas
```

Otros ejemplos:

```text
feature/login
feature/usuarios
feature/pedidos
feature/caja
feature/inventario
```

**Nunca comiences a programar directamente en `develop` o `main`.**

## Paso 4 - Trabajar normalmente

Edita tus archivos desde Cursor, VS Code u otro editor.

Revisa qué cambiaste:

```bat
git status
```

## Paso 5 - Guardar los cambios en Git

```bat
git add .
git commit -m "feat: implementar gestion de mesas"
```

- `git add .`: prepara los archivos modificados.
- `git commit`: guarda un punto de avance dentro de tu rama.

## Paso 6 - Subir tu rama a GitHub

```bat
git push -u origin feature/gestion-mesas
```

La primera vez se usa `-u`. Después, para esa misma rama, normalmente basta con:

```bat
git push
```

---

# 10. Crear el Pull Request

Después de subir la rama, entra al repositorio en GitHub y crea un Pull Request.

Debe quedar así:

```text
base: develop
compare: feature/tu-rama
```

Ejemplo:

```text
base: develop
compare: feature/gestion-mesas
```

Revisa los archivos cambiados y luego realiza el merge.

Actualmente **no se necesita aprobación obligatoria de otro compañero**, por lo que un integrante con permisos puede fusionar su propio Pull Request.

Después del merge, elimina la rama remota con **Delete branch**.

En tu PC vuelve a actualizar `develop`:

```bat
git checkout develop
git pull origin develop
git branch -d feature/gestion-mesas
```

El último comando elimina la rama local que ya terminó.

---

# 11. Regla especial para cambios en la base de datos

La base se versiona con **Flyway**.

La migración actual es:

```text
V1__estructura_inicial.sql
```

## Nunca modificar una migración ya utilizada

No se debe editar `V1__estructura_inicial.sql` después de haber sido integrada y ejecutada.

Si se necesita cambiar la base, se crea una migración nueva, por ejemplo:

```text
V2__agregar_telefono_cliente.sql
V3__crear_tabla_reservas.sql
```

Antes de crear una nueva migración:

```bat
git checkout develop
git pull origin develop
```

Luego revisa cuál es el último número existente en:

```text
backend/src/main/resources/db/migration/
```

Esto evita que dos compañeros creen por accidente dos archivos con el mismo número de versión.

Cuando otro integrante agregue una migración y tú hagas `git pull`, Flyway la ejecutará automáticamente la próxima vez que levantes Spring Boot.

---

# 12. Si necesitas traer cambios nuevos de develop a tu rama

Si ya estás trabajando en una rama y otro compañero integró cambios importantes a `develop`, puedes hacer:

```bat
git checkout develop
git pull origin develop
git checkout feature/tu-rama
git merge develop
```

Esto trae los cambios recientes de `develop` a tu rama.

Si Git informa un conflicto, **no borres archivos al azar**. Revisa el conflicto y coordina con el compañero que modificó esa parte si es necesario.

---

# 13. Reglas simples del equipo

1. No trabajar directamente en `main`.
2. No trabajar directamente en `develop`.
3. Antes de una tarea: `checkout develop` y `pull`.
4. Crear una rama para cada tarea o funcionalidad.
5. Subir cambios con commits claros y frecuentes.
6. Integrar cambios mediante Pull Request hacia `develop`.
7. No subir contraseñas, tokens ni credenciales.
8. No modificar migraciones de Flyway que ya fueron utilizadas.
9. Para cambios de BD, crear una migración nueva.
10. Si tienes dudas sobre un conflicto o una migración, coordinar antes de hacer merge.

---

# 14. Resumen rápido del trabajo diario

```text
1. git checkout develop
2. git pull origin develop
3. git checkout -b feature/nombre-de-la-tarea
4. trabajar en el código
5. git status
6. git add .
7. git commit -m "feat: descripcion corta"
8. git push -u origin feature/nombre-de-la-tarea
9. crear Pull Request: feature -> develop
10. hacer Merge y Delete branch
11. git checkout develop
12. git pull origin develop
13. git branch -d feature/nombre-de-la-tarea
```

---

# 15. Si algo deja de funcionar

Antes de cambiar configuraciones, revisa estas tres cosas:

```bat
git status
git branch
git pull origin develop
```

Para problemas de SQL Server, comprueba:

```powershell
Test-NetConnection localhost -Port 1433
```

Para confirmar que las variables existen:

```bat
echo %DB_URL%
echo %DB_USERNAME%
if defined DB_PASSWORD (echo DB_PASSWORD configurada) else (echo DB_PASSWORD NO configurada)
```

Si sigue fallando, comparte con el equipo **el mensaje de error**, pero nunca publiques tu contraseña.

---

## En una frase

**Actualiza `develop` -> crea tu rama -> trabaja -> commit -> push -> Pull Request a `develop` -> merge -> vuelve a actualizar `develop`.**
