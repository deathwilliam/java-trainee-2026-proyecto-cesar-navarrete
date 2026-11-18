# Resguardo

**Video:** https://youtu.be/FXDwQCGjxkQ

API REST de pagos en garantía (*escrow*) para compras y ventas en línea. El comprador deposita, la plataforma guarda el dinero en custodia y solo se lo paga al vendedor cuando el comprador confirma que recibió el producto.

César Eduardo Navarrete Chavarria · Programa Digital Trainee 2026

**Tecnologías:** Java 21 · Spring Boot 4.1 · Spring Data JPA · Spring Security con JWT · PostgreSQL 17 · Swagger · JUnit 5, Mockito y MockMvc · Docker Compose.

El diseño completo (diagramas y reglas) está en `proyecto/etapa-1/`.

---

## Levantarlo desde cero

Solo se necesita Docker.

**1. Crear el archivo `.env`** en esta carpeta, junto a `docker-compose.yml`, con una clave de 32 caracteres o más. Se puede partir de `.env.ejemplo`. Conviene crearlo desde el editor y no desde PowerShell.

```
JWT_CLAVE=una-clave-larga-de-32-caracteres-o-mas
```

**2. Levantar la API y la base:**

```
docker compose up --build -d
docker compose logs -f api
```

La primera vez tarda unos minutos. Está lista cuando aparece `Started ApiApplication`.

**3. Abrir Swagger:** http://localhost:8080/swagger-ui.html

**4. Para apagarla:**

```
docker compose down      # apaga y conserva el volumen
docker compose down -v   # apaga y borra también el volumen
```

Cada vez que la API arranca, la base vuelve a los datos de `import.sql`, porque `ddl-auto` está en `create`.

---

## Usuarios de prueba

| Usuario | Clave | Rol | Qué puede hacer |
| :--- | :--- | :--- | :--- |
| `mlopez` | `Cliente2026` | CLIENTE | Vende en los contratos 1, 2 y 4 |
| `lmartinez` | `Cliente2026` | CLIENTE | Compra en los contratos 1, 2, 3 y 5 |
| `arivas` | `Cliente2026` | CLIENTE | Vende en los contratos 3 y 5, compra en el 4 |
| `jperez` | `Asesor2026` | ASESOR | Consulta los contratos y lee la bitácora |
| `admin` | `Admin2026` | ADMIN | Consulta, lee la bitácora y cancela contratos |

Los clientes crean, editan, cancelan y liberan contratos. El asesor y el admin no compran ni venden.

**Para probar en Swagger:** `POST /api/auth/login`, copiar el `token`, clic en **Authorize** y pegarlo sin la palabra `Bearer`.

---

## La operación principal: liberar los fondos

`POST /api/contratos/{numero}/liberacion` no lleva cuerpo. El comprador confirma que recibió el producto y la plataforma le paga al vendedor. Es **todo o nada** (`@Transactional`): se crea la liquidación, se crean sus dos detalles (comisión y pago al vendedor), el contrato pasa a LIBERADO y queda la fila EXITOSA en la bitácora. Quien libera **sale del token**, no del JSON.

Ejemplo con `GAR-2026-000001`: $350.00 al 3 % da $10.50 de comisión y $339.50 para el vendedor.

Si una regla dice que no, no cambia nada, pero el intento queda en la bitácora como RECHAZADA. Esa fila se guarda en una transacción aparte (`REQUIRES_NEW`), para que el rollback no se la lleve.

### Las reglas

| Regla | Qué dice | Dónde se cumple | Si no se cumple |
| :--- | :--- | :--- | :--- |
| R1 | El monto está entre el mínimo y el máximo de la categoría | Servicio, al crear | 400 |
| R3 | Comisión = monto × porcentaje (2 decimales) y pago al vendedor = monto − comisión; los dos suman el monto | Servicio, al liberar | Se deshace todo |
| R4 | Solo se liberan los fondos de un contrato ENVIADO | Servicio, al liberar | 400 |
| R6 | Solo el comprador libera los fondos | Servicio, al liberar (el usuario sale del token) | 400 |
| R7 | La cuenta de pago es una cuenta activa del vendedor | Servicio, al crear y al liberar | 400 |
| R9 | Un contrato se liquida una sola vez; número de contrato y de comprobante únicos | Servicio y `UNIQUE` en la tabla | 400 · 409 |
| R10 | Los montos son mayores que cero | DTO (`@Positive`) | 400 |
| — | Un contrato solo se edita o se cancela mientras está CREADO | Servicio | 400 · 409 |

---

## Los endpoints

Sin token, todo responde **401**, salvo el login y Swagger. Con un rol sin permiso, **403**. Todos los errores responden con `ProblemDetail`.

| Método y ruta | Qué hace | Quién | Responde |
| :--- | :--- | :--- | :--- |
| `POST /api/auth/login` | Entrega el token | Abierto | 200 · 401 |
| `GET /api/auth/yo` | Quién soy, según el token | Con token | 200 |
| `GET /api/contratos` | Lista los contratos | Con token | 200 |
| `GET /api/contratos/{numero}` | Un contrato | Con token | 200 · 404 |
| `POST /api/contratos` | Crea un contrato (el vendedor sale del token) | CLIENTE | 201 · 400 |
| `PATCH /api/contratos/{numero}` | Edita la descripción | CLIENTE | 200 · 400 · 404 |
| `DELETE /api/contratos/{numero}` | Cancela el contrato | CLIENTE, ADMIN | 204 · 404 · 409 |
| `POST /api/contratos/{numero}/liberacion` | Libera los fondos | CLIENTE (el comprador) | 201 · 400 · 404 |
| `GET /api/contratos/{numero}/liquidacion` | La liquidación de un contrato | Con token | 200 · 404 |
| `GET /api/bitacora` | Las 20 últimas operaciones | ASESOR, ADMIN | 200 |
| `GET /api/categorias` | Categorías con sus límites y comisión | Con token | 200 |
| `GET /api/bancos` | Bancos aceptados | Con token | 200 |

---

## Las pruebas

Se corren en la máquina y necesitan PostgreSQL con la base `resguardo_test`. La primera vez:

```
docker run --name banco-db -e POSTGRES_USER=banco -e POSTGRES_PASSWORD=banco123 -p 5432:5432 -d postgres:17-alpine
docker exec banco-db psql -U banco -d banco -c "CREATE DATABASE resguardo;"
docker exec banco-db psql -U banco -d banco -c "CREATE DATABASE resguardo_test;"
```

Después, en esta carpeta:

```
.\mvnw.cmd test
```

No necesitan `JWT_CLAVE`: traen su propia clave de prueba. Tienen que salir **18 de 18**.

| Clase | Qué comprueba | Necesita la base |
| :--- | :--- | :---: |
| `LiberacionReglasTest` | Cada regla de la liberación, con Mockito | No |
| `LiberacionServicioTest` | Que una caída a la mitad hace rollback y que el intento rechazado queda en la bitácora | Sí |
| `SeguridadApiTest` | 401, 403, cada rol y que el usuario sale del token, con MockMvc | Sí |
| `ApiApplicationTests` | Que la aplicación arranca entera | Sí |

---

## Correrla desde IntelliJ

Con `banco-db` encendido, agregar la variable `JWT_CLAVE` en *Run → Edit Configurations → Environment variables* y arrancar `ApiApplication`. Se conecta a la base `resguardo` en el puerto 5432. Antes de usar Docker Compose hay que apagarla, porque las dos usan el puerto 8080.

---

## Cómo está organizado

```
src/main/java/sv/resguardo/api/
├── controlador/   recibe HTTP, llama al servicio y responde con DTOs; ManejadorErrores atiende todos los errores
├── dto/           lo que entra y sale por la API, con record
├── servicio/      las reglas y las transacciones
├── repositorio/   el acceso a la base
├── entidad/       las 14 tablas del diseño
├── excepcion/     las reglas que dicen que no
└── seguridad/     la cadena de filtros, los roles y el token
```

Un controlador nunca usa un repositorio, y ninguna entidad entra ni sale por la API. La clave de firma nunca está en el repositorio: va en `JWT_CLAVE`.

---

## Lo que todavía no hace

El diseño de la etapa 1 incluye el depósito, el envío y las disputas (R2, R5 y el reembolso). Las tablas ya existen, pero en esta versión la API cubre la creación de contratos y su operación principal, liberar los fondos.
