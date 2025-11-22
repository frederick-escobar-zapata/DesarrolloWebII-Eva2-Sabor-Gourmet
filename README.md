**Proyecto Sabor Gourmet**

**Grupo C3**
Integrantes
-José Luis Espinoza (Bludegar)
-Frederick Escobar
-Gonzalo Croft


Resumen
- **proposito**: proyecto de ejemplo para gestionar reservas de un restaurante. incluye vistas publicas para ver disponibilidad y reservar, y vistas de admin para listar y cancelar reservas.

Tecnologias
- **java**: java 21
- **spring boot**: spring boot 3.x (spring web, spring data jpa)
- **maven**: wrapper incluido (`./mvnw` para bash)
- **base de datos**: postgresql
- **thymeleaf**: plantillas server-side
- **bootstrap**: estilos y componentes UI (cdn)
- **flatpickr**: datepicker (cdn)
- **js/vanilla**: scripts en `src/main/resources/static/js`

Estructura del proyecto (resumen)
- **puntos de entrada**: la clase principal es `SaborGourmetApplication` en `src/main/java/...`.
- **controladores**: en `src/main/java/sabor_gourmet/sabor_gourmet/controladores/` — ahi se manejan rutas publicas y admin.
  - `ReservaPublicaController` / `MesasController`: flujo publico (listar disponibilidad, redirigir a reservar cliente).
  - `AdminReservasController` / `AdminDisponibilidadController`: vistas admin, listar reservas por fecha y detalle/cancelar.
- **servicios**: `servicios/` contiene la logica de negocio que orquesta repositorios y validaciones.
- **repositorios**: `repositorios/` usa Spring Data JPA para acceder a las entidades.
- **modelos**: entidades JPA en `modelos/` como `Cliente`, `Mesas`, `Reserva`, `TipoMenu`.
- **vistas**: `src/main/resources/templates/` — plantillas Thymeleaf. `layout/master.html` es el layout principal.
- **estaticos**: `src/main/resources/static/` — css y js. scripts importantes: `reservas.js` y `reservar_cliente.js`.

Como ejecutar el proyecto (desarrollo)

- prerequisitos:
  - instalar JDK 21
  - tener una base de datos PostgreSQL accesible
  - configurar credenciales en `src/main/resources/application.properties` o usar variables de entorno

- crear la base de datos (ejemplo):

```bash
sudo -u postgres psql
create database sabor_gourmet;
create user sguser with password 'tu_password';
grant all privileges on database sabor_gourmet to sguser;
\q
```

- ajustar `src/main/resources/application.properties` con la URL, usuario y password:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/sabor_gourmet
spring.datasource.username=sguser
spring.datasource.password=tu_password
```

- ejecutar en modo desarrollo (bash):

```bash
./mvnw spring-boot:run
```

- o para empacar y ejecutar el jar:

```bash
./mvnw clean package
java -jar target/*.jar
```

- tests:

```bash
./mvnw test
```

Endpoints y flujo principal
- **/** : pagina de inicio (landing) con enlaces a reservas.
- **/reservas** : vista publica con datepicker y panel de mesas. el frontend pide `/reservas/available?fecha=YYYY-MM-DD` (fetch) para obtener disponibilidad.
- **/reservar/cliente** : formulario para completar datos del cliente y confirmar la reserva.
- **/admin/reservas** : vista admin que permite seleccionar una fecha (flatpickr) y ver las reservas del dia; boton "ver" abre un modal con detalle y opcion de cancelar.

Logica y partes importantes (explicacion detallada)

- **modelo Reserva** (`Reserva`): representa una reserva con relacion a `Mesas` y `Cliente`, fecha, tipo de menu y cantidad de personas. las validaciones (no nulas, rangos) se aplican en servicios y controladores.

- **repositorios**: interfaces que extienden `JpaRepository`. proporcionan consultas CRUD basicas y consultas personalizadas (por fecha, por mesa, etc.).

- **servicio de reservas** (`ReservaServicio`): punto central de la logica. responsabilidades principales:
  - validar disponibilidad de una mesa para una fecha y tipo
  - calcular cupos y manejar concurrencia basica
  - crear, actualizar y cancelar reservas
  - transformar entidades para la vista (DTO simplificados)

- **controladores**:
  - `ReservaPublicaController` / `MesasController`:
    - sirven la pagina de disponibilidad (`/reservas`)
    - exponen `GET /reservas/available` que devuelve JSON: estructura `{ count: N, mesas: [...] }` con los tipos disponibles por mesa
    - redirigen a `GET /reservar/cliente` con query params cuando el usuario elige una mesa y tipo
  - `ReservaPublicaController` (POST): recibe formulario de `cliente`, valida campos requeridos (`nombre`, `email`, `telefono`) tanto cliente-side como server-side, guarda `Cliente` y `Reserva`, y prepara la vista de confirmacion.
  - `AdminReservasController`:
    - recibe la fecha seleccionada y devuelve la lista de reservas del dia
    - en la vista admin el boton "Ver" pasa datos via atributos `data-*` al boton y el JS rellena el modal de detalle
    - cancelacion: `POST /admin/reservas/eliminar/{id}` elimina la reserva y vuelve a la lista

- **vistas y JS**:
  - `reservas.js` (publico): inicializa `flatpickr` en el input de fecha, hace la peticion `fetch('/reservas/available')`, renderiza tarjetas de mesas y botones por cada `tipoMenu` disponible. los botones verifican que el formulario (fecha y cantidad) este completo antes de redirigir a `reservar/cliente`.
  - `reservar_cliente.js`: valida que los campos `nombre`, `apellido`, `email`, `telefono` esten completos y habilita/deshabilita el boton submit. ademas maneja el modal de confirmacion que puede mostrarse por dos mecanismos:
    - el servidor indica con un atributo o rellena los spans del modal; el JS detecta y muestra el modal usando `bootstrap.Modal` si esta disponible.
    - fallback: si bootstrap no esta disponible, el script muestra una version basica del modal y agrega un backdrop manual.
  - accesibilidad/robustez: los scripts manejan el backdrop huérfano, el foco cuando se cierra el modal, y tienen try/catch para evitar romper la UI si faltan librerias externas.

- **layout/master.html**: contiene el header/footer comun, importa `bootstrap` y `flatpickr` via CDN, y define el fragment `content` donde se insertan las plantillas especificas.

Decisiones de implementacion y justificacion
- se opto por Thymeleaf para simplificar integracion server-side y permitir renderizado inicial SEO-friendly.
- el datepicker `flatpickr` provee buenas opciones de localizacion y bloqueo de fechas pasadas; en caso de no tenerlo se aplica un fallback con `min` en el input.
- la logica de disponibilidad es server-driven: el servidor es la fuente de verdad, el cliente solo solicita y renderiza. esto evita inconsistencias por estado en el cliente.

Notas sobre el repo y cambios recientes
- algunos archivos se restauraron desde los artefactos compilados (`target/classes`) porque se realizaron pruebas de refactor y no se habia comiteado. si ves archivos modificados, revisa `git status`.

Recomendaciones y siguientes pasos
- revisa `src/main/resources/application.properties` para adaptar puertos y conexion DB.
- si vas a desplegar en produccion:
  - usar credenciales seguras (no en repo)
  - habilitar migration tool (flyway/liquibase) para manejar esquemas
  - configurar pool de conexiones y timeouts

Contacto rapido (si necesitas que haga algo mas)
- puedo: añadir un script `setup-db.sh` para crear la base, agregar `README` de despliegue en docker, o limpiar el estado git y commitear los cambios restaurados.

Fin del README.

*** NOTA: este README fue escrito para ser legible y detallado; si quieres que lo adapte a un estilo mas tecnico o lo traduzca a ingles dime y lo actualizo.
