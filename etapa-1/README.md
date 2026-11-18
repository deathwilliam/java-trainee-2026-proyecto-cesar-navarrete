# Resguardo

Proyecto integrador, etapa 1.

Nombre: César Eduardo Navarrete Chavarria

## 1. Nombre y de qué se trata

**Resguardo** es una API REST de pagos en garantía (*escrow*) para personas que compran y venden en línea.

Elegí este tema porque en El Salvador es muy común comprar y vender por redes sociales, y en esos tratos siempre aparece el mismo problema: alguien tiene que confiar primero. Si el comprador paga antes, se arriesga a que el producto nunca llegue. Si el vendedor envía antes, se arriesga a que nunca le paguen.

Resguardo se pone en medio. El comprador deposita el dinero, la plataforma lo guarda en custodia y solo se lo paga al vendedor cuando el comprador confirma que recibió lo que compró. Si algo sale mal, un asesor revisa el caso y decide si el dinero va para el vendedor o regresa al comprador.

## 2. Quién lo usa

| Rol | Quién es | Qué puede hacer |
|---|---|---|
| ADMIN | El administrador de la plataforma | Configura las categorías y el catálogo de bancos, crea los usuarios de los asesores y consulta todos los contratos y la bitácora. |
| ASESOR | El personal de operaciones | Verifica que las transferencias hayan llegado, registra los depósitos y resuelve las disputas. |
| CLIENTE | La persona que compra o vende | Registra sus cuentas bancarias. Como vendedor crea contratos y registra el envío; como comprador confirma que recibió el producto. También puede cancelar un contrato CREADO o abrir una disputa, y solo ve los contratos en los que participa. |

Cada cliente y cada asesor está ligado a su usuario por medio de `cliente.usuario_id` y `asesor.usuario_id`. Gracias a eso el servicio sabe qué contratos le pertenecen a cada cliente: los que tiene como comprador o como vendedor. Y decidí que las dos partes salieran de la misma tabla `cliente` porque una misma persona puede comprar en un contrato y vender en otro.

## 3. Las entidades

El diseño tiene 14 tablas, que agrupé en cinco módulos. Cada entidad de Java corresponde a una tabla con el mismo nombre en snake_case; por ejemplo, ContratoGarantia es `contrato_garantia`. Las columnas y relaciones completas están en el diagrama entidad-relación y en el diagrama de clases, que hice en draw.io: el primero con Entity Relation y la notación que me enseñaron como pata de gallo, y el segundo con notación UML.

**El negocio**

- **ContratoGarantia**: el trato entre vendedor y comprador. Guarda qué se vende, por cuánto, la comisión pactada y en qué estado va.
- **Deposito**: la transferencia del comprador, ya verificada por un asesor. Registra desde qué cuenta se hizo y con qué referencia.
- **Liquidacion**: la salida del dinero de la custodia, con su número de comprobante. Puede ser una LIBERACION para el vendedor o un REEMBOLSO para el comprador.
- **DetalleLiquidacion**: el desglose de cada liquidación, es decir, cuánto recibe el vendedor y cuánto queda de comisión, o el monto que se devuelve.
- **Disputa**: el reclamo de una de las partes cuando algo sale mal, con su motivo y la decisión del asesor.

**Las personas**

- **Cliente**: la persona que compra o vende.
- **CuentaBancaria**: las cuentas de cada cliente, a donde le llega el dinero cuando vende y desde donde paga cuando compra.
- **Asesor**: el empleado que verifica los depósitos y resuelve las disputas.

**Los catálogos**

- **Categoria**: el tipo de artículo, por ejemplo ELECTRONICA, con su monto mínimo, su monto máximo y su porcentaje de comisión.
- **Banco**: los bancos donde los clientes tienen sus cuentas.

**La bitácora**

- **BitacoraOperacion**: el registro de cada operación que se intentó, con quién la hizo, sobre qué contrato, cuándo y si terminó EXITOSA o RECHAZADA.

**La seguridad**

- **Usuario**: la cuenta de acceso a la plataforma.
- **Rol**: ADMIN, ASESOR o CLIENTE.
- **UsuarioRol**: la relación entre usuarios y roles, ya que un usuario puede tener más de un rol.

Las categorías, los bancos y los roles son tablas de catálogo porque el ADMIN los da de alta sin tocar el código. Los estados y los motivos, en cambio, son enums, porque solo cambian si cambia el programa.

**El recorrido de un contrato.** Todo contrato nace CREADO. Pasa a FONDEADO cuando el asesor registra el depósito, a ENVIADO cuando el vendedor registra el envío, y termina LIBERADO cuando el comprador confirma la recepción. Si algo sale mal mientras está FONDEADO o ENVIADO, cualquiera de las dos partes puede abrir una disputa, y el asesor la cierra con LIBERADO o REEMBOLSADO. Mientras siga CREADO, el contrato todavía se puede cancelar.

## 4. La operación principal: liberar los fondos

Es el momento en que el comprador confirma que recibió el producto y la plataforma le paga al vendedor. Toca cuatro tablas y tiene que ser todo o nada: un contrato LIBERADO sin su liquidación dejaría al vendedor sin su pago, y una liquidación con el contrato todavía ENVIADO permitiría abrir una disputa por un dinero que ya salió.

**Entrada:** el `numero_contrato` y el usuario que confirma la recepción.

**Validaciones previas, antes de tocar nada:** que el contrato exista, R4 (está ENVIADO), R6 (quien confirma es el comprador) y R7 (la cuenta de pago del vendedor sigue activa).

**Los cambios, en orden:**

1. Se inserta la `liquidacion` de tipo LIBERACION con su `numero_comprobante`. Por R9, un contrato solo puede tener una.
2. Se insertan los dos `detalle_liquidacion`, COMISION y PAGO_VENDEDOR, y se comprueba que sumen el `monto` del contrato (R3). Por ejemplo, en un contrato de $350.00 con una comisión de 0.0300, la COMISION es de $10.50 y el PAGO_VENDEDOR de $339.50.
3. El `estado` del `contrato_garantia` cambia a LIBERADO.
4. La operación LIBERAR_FONDOS queda registrada como EXITOSA en `bitacora_operacion`.

Cuando es el asesor quien resuelve una disputa, se siguen estos mismos pasos y además se cierra la `disputa`. Si decide a favor del comprador, la liquidación es un REEMBOLSO por el monto completo hacia la cuenta de origen del depósito, y el contrato termina REEMBOLSADO.

**Si falla a la mitad:** se deshace todo. Por ejemplo, si algo falla después de insertar la liquidación, no queda ninguna liquidación ni detalle, el contrato sigue ENVIADO y el dinero sigue en custodia.

**Salida:** el comprobante, con su número, la fecha y hora, el monto del contrato, la comisión y lo que recibe el vendedor.

## 5. Las reglas de negocio

Estas son las reglas que el sistema tiene que cumplir siempre. Las que se cumplen en la tabla las protege la propia base de datos; las del servicio son las que voy a programar y probar en la etapa 2.

| # | La regla, con los nombres del diagrama | Dónde se cumple | Si no se cumple |
|---|---|---|---|
| R1 | El `monto` del `contrato_garantia` está entre el `monto_minimo` y el `monto_maximo` de su `categoria`. | Servicio, al crear el contrato | Se rechaza: "El monto está fuera del rango de la categoría ELECTRONICA". |
| R2 | El asesor solo registra el `deposito` si la transferencia es por el `monto` exacto del contrato. No se aceptan depósitos parciales. | Servicio, al registrar el depósito | Se rechaza: "La transferencia no coincide con el monto del contrato". |
| R3 | La COMISION es `monto` × `porcentaje_comision`, redondeada a dos decimales, y el PAGO_VENDEDOR es `monto` − COMISION, así que los dos `detalle_liquidacion` suman el `monto` del contrato. Un REEMBOLSO tiene un solo detalle, por el `monto` completo. | Servicio, en la operación principal | Se deshace toda la operación. |
| R4 | Solo se liberan los fondos de un contrato ENVIADO, o EN_DISPUTA cuando lo decide el asesor. | Servicio, al liberar | Se rechaza: "El contrato GAR-2026-000123 está FONDEADO: todavía no se puede liberar". |
| R5 | Solo se abre una `disputa` en un contrato FONDEADO o ENVIADO, y cada contrato tiene como máximo una. | Servicio + tabla (UNIQUE) | Se rechaza: "El contrato ya tiene una disputa". |
| R6 | Solo el comprador confirma la recepción y solo el vendedor registra el envío. | Servicio, al liberar y al registrar el envío | Se rechaza: "Solo el comprador puede liberar los fondos". |
| R7 | La `cuenta_pago_id` del contrato es una `cuenta_bancaria` activa del vendedor, y la `cuenta_origen_id` del depósito es una `cuenta_bancaria` del comprador. | Servicio, al crear, al depositar y al liberar | Se rechaza: "La cuenta no pertenece al vendedor o está inactiva". |
| R8 | El `dui` tiene el formato 00000000-0, y ni el `dui` ni el `correo` del cliente se repiten. | Tabla (UNIQUE) + servicio (formato) | Se rechaza: "Ya existe un cliente con ese DUI". |
| R9 | Un contrato tiene una sola `liquidacion`, y el `numero_contrato`, el `numero_comprobante` y la `referencia` del depósito no se repiten. | Tabla (UNIQUE) | La base rechaza el INSERT y se deshace la operación: un contrato no se puede pagar dos veces. |
| R10 | Los montos son mayores que cero, el `monto_maximo` es mayor o igual que el `monto_minimo` y el `porcentaje_comision` es mayor que 0 y menor que 1. | Tabla (CHECK) | La base rechaza el INSERT. |

**Sobre los datos repetidos.** No guardo totales calculados: el total de una liquidación sale de sumar su detalle, y el `deposito` no repite el monto porque R2 ya obliga a que sea el del contrato. El único dato que copio a propósito es `contrato_garantia.porcentaje_comision`, que congela la comisión de la categoría el día que se crea el contrato. Así, si el ADMIN la cambia después, los contratos que ya existen conservan la suya.

## 6. Lo que no hace

- No mueve dinero real ni se conecta con bancos o pasarelas de pago. El asesor registra los depósitos que ya verificó, y cada liquidación deja el comprobante de lo que se le debe pagar a cada parte.
- No libera los fondos de forma automática con el paso del tiempo. Si el comprador nunca confirma, el vendedor puede abrir una disputa con el motivo SIN_CONFIRMACION.
- No maneja pagos parciales ni contratos con varios artículos. Cada contrato es una sola venta, con un solo depósito y una sola liquidación.
