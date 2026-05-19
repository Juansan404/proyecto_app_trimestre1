# TattooAge — Especificaciones App Escritorio JavaFX
## Documento para Claude Code

---

## 1. CONTEXTO DEL PROYECTO

**TattooAge** es una plataforma de gestión para estudios de tatuajes. Este documento describe la aplicación de escritorio (módulo de Interfaces/DI), que actúa como panel de administración conectado a la API REST ya desplegada.

La app de escritorio **ya existía parcialmente** con las siguientes tablas implementadas:
- Clientes
- Tatuadores
- Citas

El objetivo de este documento es **ampliar y refactorizar** esa aplicación para cubrir **todas las tablas de la base de datos**, conectándose a la API REST real mediante HTTP.

---

## 2. TECNOLOGÍAS DE LA APP DE ESCRITORIO

| Elemento | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework UI | JavaFX (con FXML + SceneBuilder) |
| Arquitectura | MVC |
| Comunicación con API | Java `HttpClient` (java.net.http) |
| Serialización JSON | Jackson (`com.fasterxml.jackson`) |
| Gestión de dependencias | Maven |
| IDE recomendado | IntelliJ IDEA |

---

## 3. API REST — CONEXIÓN

**URL base de producción:**
```
https://tattooage-backend-293514144387.europe-west1.run.app
```

**Autenticación:** JWT (Bearer Token)
- El token se obtiene en `POST /api/auth/login`
- Se envía en cada petición como header: `Authorization: Bearer <token>`
- El token debe almacenarse en memoria durante la sesión (variable estática o Singleton de sesión)

**Stack del backend (referencia):**
- Java 17 + Spring Boot 3.2.3
- Spring Security + JWT (jjwt 0.12.5)
- PostgreSQL en Supabase
- Swagger UI disponible en `/swagger-ui.html`

---

## 4. BASE DE DATOS — TABLAS Y ENTIDADES

La base de datos tiene las siguientes tablas. La app de escritorio debe gestionar **todas** ellas:

### 4.1 `usuarios`
```
id_usuario      (PK)
nombre
apellidos
email
password_hash
rol             (CLIENTE, ARTISTA, ADMIN)
foto_perfil
bio
activo
creado_en
actualizado_en
```

### 4.2 `estudios`
```
id_estudio      (PK)
nombre
direccion
ciudad
telefono
email
descripcion
foto
activo
creado_en
```

### 4.3 `perfiles_artista`
```
id_perfil       (PK)
id_usuario      (FK → usuarios)
id_estudio      (FK → estudios)
especialidades
anos_experiencia
instagram
precio_hora
activo
```

### 4.4 `publicaciones`
```
id_publicacion  (PK)
id_usuario      (FK → usuarios)
imagen_url
descripcion
estilo
likes_count
comentarios_count
creado_en
```

### 4.5 `likes`
```
id_usuario      (FK → usuarios)  PK compuesto
id_publicacion  (FK → publicaciones)  PK compuesto
creado_en
```

### 4.6 `comentarios`
```
id_comentario   (PK)
id_publicacion  (FK → publicaciones)
id_usuario      (FK → usuarios)
contenido
creado_en
```

### 4.7 `seguidores`
```
id_seguidor     (FK → usuarios)  PK compuesto
id_seguido      (FK → usuarios)  PK compuesto
creado_en
```

### 4.8 `solicitudes_cita`
```
id_solicitud    (PK)
id_cliente      (FK → usuarios)
id_artista      (FK → usuarios)
descripcion
zona_cuerpo
tamano          (Pequeño, Mediano, Grande)
presupuesto_aprox
fecha_preferida
foto_referencia
estado          (Pendiente, Aceptada, Rechazada, Completada)
notas_artista
creado_en
actualizado_en
```

### 4.9 `mensajes`
```
id_mensaje      (PK)
id_solicitud    (FK → solicitudes_cita)
id_remitente    (FK → usuarios)
contenido
leido
creado_en
```

### 4.10 `citas`
```
id_cita         (PK)
id_cliente      (FK → usuarios)
id_artista      (FK → usuarios)
id_solicitud    (FK → solicitudes_cita)
fecha_cita
hora_inicio
duracion_aproximada  (en minutos)
precio
estado          (Pendiente, Confirmada, Cancelada, Completada)
sala
foto_diseno
notas
creado_en
actualizado_en
```

---

## 5. ENDPOINTS DE LA API (referencia)

Los siguientes endpoints ya existen o deben existir en el backend:

```
POST   /api/auth/login                          → Login, devuelve JWT
POST   /api/auth/register                       → Registro de usuario

GET    /api/usuarios                            → Listar todos
GET    /api/usuarios/{id}                       → Ver uno
PUT    /api/usuarios/{id}                       → Editar
DELETE /api/usuarios/{id}                       → Eliminar

GET    /api/estudios                            → Listar todos
POST   /api/estudios                            → Crear
PUT    /api/estudios/{id}                       → Editar
DELETE /api/estudios/{id}                       → Eliminar

GET    /api/artistas                            → Listar perfiles artista
GET    /api/artistas/{id}                       → Ver perfil artista

GET    /api/publicaciones                       → Listar todas
DELETE /api/publicaciones/{id}                  → Eliminar (moderación)

GET    /api/citas                               → Listar todas
GET    /api/citas/{id}                          → Ver una
POST   /api/citas                               → Crear
PUT    /api/citas/{id}                          → Editar
DELETE /api/citas/{id}                          → Eliminar
PUT    /api/citas/{id}/estado                   → Cambiar estado

GET    /api/solicitudes                         → Listar solicitudes de cita
PUT    /api/solicitudes/{id}/estado             → Aceptar/Rechazar

GET    /api/mensajes/solicitud/{id}             → Ver mensajes de una solicitud
```

---

## 6. ARQUITECTURA DE LA APLICACIÓN

### Patrón MVC estricto:

```
src/main/java/com/tattooage/desktop/
├── Main.java                          ← Arranque JavaFX
├── util/
│   ├── ApiClient.java                 ← HttpClient singleton, gestión JWT
│   ├── JsonUtil.java                  ← Wrapper Jackson
│   └── SessionManager.java            ← Almacena token y usuario activo
├── model/                             ← POJOs que representan entidades
│   ├── Usuario.java
│   ├── Estudio.java
│   ├── PerfilArtista.java
│   ├── Publicacion.java
│   ├── Cita.java
│   ├── SolicitudCita.java
│   ├── Mensaje.java
│   ├── Like.java
│   ├── Comentario.java
│   └── Seguidor.java
├── service/                           ← Llamadas HTTP a la API
│   ├── AuthService.java
│   ├── UsuarioService.java
│   ├── EstudioService.java
│   ├── ArtistaService.java
│   ├── PublicacionService.java
│   ├── CitaService.java
│   ├── SolicitudService.java
│   └── MensajeService.java
└── controller/                        ← Controladores de cada vista FXML
    ├── LoginController.java
    ├── MainController.java            ← Contenedor principal con sidebar
    ├── UsuariosController.java
    ├── EstudiosController.java
    ├── ArtistasController.java
    ├── PublicacionesController.java
    ├── CitasController.java
    └── SolicitudesController.java

src/main/resources/
├── fxml/
│   ├── Login.fxml
│   ├── Main.fxml
│   ├── Usuarios.fxml
│   ├── Estudios.fxml
│   ├── Artistas.fxml
│   ├── Publicaciones.fxml
│   ├── Citas.fxml
│   └── Solicitudes.fxml
└── css/
    └── styles.css
```

---

## 7. PANTALLAS Y FUNCIONALIDADES

### 7.1 Pantalla de Login
- Campos: email + contraseña
- Llama a `POST /api/auth/login`
- Si login OK → guarda JWT en `SessionManager` → navega a Main
- Si error → muestra alerta con mensaje

### 7.2 Pantalla Principal (Main)
- Sidebar izquierdo con navegación a cada sección
- Área central de contenido que carga los FXML de cada sección
- Botón de cerrar sesión en la parte superior

### 7.3 Usuarios
- **TableView** con columnas: ID, Nombre, Apellidos, Email, Rol, Activo
- Botones: Buscar, Ver detalle, Editar rol, Desactivar/Activar, Eliminar
- Formulario de edición en panel lateral o diálogo modal

### 7.4 Estudios
- **TableView** con columnas: ID, Nombre, Ciudad, Teléfono, Activo
- CRUD completo: Crear, Ver, Editar, Eliminar
- Formulario en diálogo modal

### 7.5 Artistas (Perfiles)
- **TableView** con columnas: ID, Nombre (de usuario), Estudio, Especialidades, Años experiencia
- Ver detalle: incluye publicaciones y citas del artista
- Solo lectura + posibilidad de desactivar perfil

### 7.6 Publicaciones (Moderación)
- **TableView** con columnas: ID, Usuario, Estilo, Descripción (truncada), Likes, Fecha
- Solo permite **eliminar** publicaciones (moderación de contenido)
- Doble clic en fila → ver imagen en ventana emergente

### 7.7 Citas
- **TableView** con columnas: ID, Cliente, Artista, Fecha, Estado, Precio
- CRUD completo
- Filtro por estado (Pendiente / Confirmada / Cancelada / Completada)
- Botón rápido de cambiar estado directamente en la tabla

### 7.8 Solicitudes de Cita
- **TableView** con columnas: ID, Cliente, Artista, Zona cuerpo, Tamaño, Estado, Fecha
- Botones: Aceptar solicitud, Rechazar solicitud, Ver mensajes
- Panel de mensajes al seleccionar una solicitud (lista de mensajes del hilo)

---

## 8. CLASE ApiClient — REFERENCIA DE IMPLEMENTACIÓN

```java
public class ApiClient {
    private static final String BASE_URL = "https://tattooage-backend-293514144387.europe-west1.run.app";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static ApiClient instance;

    public static ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    public HttpResponse<String> get(String endpoint) throws Exception {
        String token = SessionManager.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .GET()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String endpoint, String jsonBody) throws Exception {
        String token = SessionManager.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> put(String endpoint, String jsonBody) throws Exception {
        String token = SessionManager.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> delete(String endpoint) throws Exception {
        String token = SessionManager.getInstance().getToken();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + endpoint))
            .header("Authorization", "Bearer " + token)
            .DELETE()
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
```

---

## 9. DEPENDENCIAS MAVEN (pom.xml)

```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>

    <!-- Jackson para JSON -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.16.1</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>2.16.1</version>
    </dependency>
</dependencies>
```

---

## 10. INSTRUCCIONES PARA CLAUDE CODE

1. **Leer este documento completo** antes de generar cualquier código.
2. **Crear la estructura de carpetas** exactamente como se define en la sección 6.
3. **Empezar por**: `ApiClient.java` → `SessionManager.java` → `LoginController.java` + `Login.fxml` → `MainController.java` + `Main.fxml` (con sidebar).
4. **Continuar con cada módulo** en este orden: Usuarios → Citas → Solicitudes → Estudios → Artistas → Publicaciones.
5. **Para cada módulo** generar: Model (POJO) + Service (llamadas HTTP) + Controller (JavaFX) + FXML (vista).
6. **Todos los services** deben usar `ApiClient.getInstance()` para las peticiones.
7. **Todos los controllers** deben manejar errores HTTP (mostrar Alert en caso de fallo).
8. **Las TableView** deben usar `ObservableList` y `PropertyValueFactory` o lambdas para los valores.
9. **El token JWT** nunca se hardcodea — siempre se lee de `SessionManager`.
10. **Adaptar el código existente** de las pantallas de Clientes, Tatuadores y Citas si ya existen en el proyecto, migrando al nuevo sistema de servicios HTTP.
