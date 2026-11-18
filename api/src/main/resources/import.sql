-- Usuarios de prueba (usuario / clave / rol)
-- admin / Admin2026 (ADMIN)
-- jperez / Asesor2026 (ASESOR)
-- mlopez / Cliente2026 (CLIENTE)
-- lmartinez / Cliente2026 (CLIENTE)
-- arivas / Cliente2026 (CLIENTE)

-- 1. Catalogos
INSERT INTO banco (codigo, nombre, activo, creado_en) VALUES ('CUSCATLAN', 'Banco Cuscatlan', true, now());
INSERT INTO banco (codigo, nombre, activo, creado_en) VALUES ('AGRICOLA', 'Banco Agricola', true, now());
INSERT INTO banco (codigo, nombre, activo, creado_en) VALUES ('BAC', 'BAC Credomatic', true, now());

INSERT INTO categoria (codigo, nombre, monto_minimo, monto_maximo, porcentaje_comision, activo, creado_en) VALUES ('ELECTRONICA', 'Electronica', 50.00, 5000.00, 0.0300, true, now());
INSERT INTO categoria (codigo, nombre, monto_minimo, monto_maximo, porcentaje_comision, activo, creado_en) VALUES ('ROPA', 'Ropa y calzado', 10.00, 1000.00, 0.0500, true, now());
INSERT INTO categoria (codigo, nombre, monto_minimo, monto_maximo, porcentaje_comision, activo, creado_en) VALUES ('MUEBLES', 'Muebles', 25.00, 3000.00, 0.0400, true, now());

INSERT INTO rol (nombre, descripcion, activo, creado_en) VALUES ('ADMIN', 'Administrador de la plataforma', true, now());
INSERT INTO rol (nombre, descripcion, activo, creado_en) VALUES ('ASESOR', 'Personal de operaciones', true, now());
INSERT INTO rol (nombre, descripcion, activo, creado_en) VALUES ('CLIENTE', 'Persona que compra o vende', true, now());

-- 2. Usuarios y roles
INSERT INTO usuario (nombre_usuario, clave_hash, activo, creado_en) VALUES ('admin', '$2a$10$nAj8JBpH8CHceMI2P4q9a.xX/HMiaV9OUgKHk5kaL4hP9UdZ2x69a', true, now());
INSERT INTO usuario (nombre_usuario, clave_hash, activo, creado_en) VALUES ('jperez', '$2a$10$X6LFmuNB4ms2XukPOJSRp.80r2uQPNZrQokixuKggoL./mxA5I9oe', true, now());
INSERT INTO usuario (nombre_usuario, clave_hash, activo, creado_en) VALUES ('mlopez', '$2a$10$Pdn.Aogn4Zmd074OUUA7DemNn2Nym7/kC0DPe4zkFK4pRwnXAU9RG', true, now());
INSERT INTO usuario (nombre_usuario, clave_hash, activo, creado_en) VALUES ('lmartinez', '$2a$10$Pdn.Aogn4Zmd074OUUA7DemNn2Nym7/kC0DPe4zkFK4pRwnXAU9RG', true, now());
INSERT INTO usuario (nombre_usuario, clave_hash, activo, creado_en) VALUES ('arivas', '$2a$10$Pdn.Aogn4Zmd074OUUA7DemNn2Nym7/kC0DPe4zkFK4pRwnXAU9RG', true, now());

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (1, 1);
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (2, 2);
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (3, 3);
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (4, 3);
INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (5, 3);

-- 3. Personas
INSERT INTO cliente (dui, nombres, apellidos, correo, telefono, usuario_id, activo, creado_en) VALUES ('01234567-8', 'Marta', 'Lopez', 'marta.lopez@correo.com', '7123-4567', 3, true, now());
INSERT INTO cliente (dui, nombres, apellidos, correo, telefono, usuario_id, activo, creado_en) VALUES ('02345678-9', 'Luis', 'Martinez', 'luis.martinez@correo.com', '7234-5678', 4, true, now());
INSERT INTO cliente (dui, nombres, apellidos, correo, telefono, usuario_id, activo, creado_en) VALUES ('03456789-0', 'Ana', 'Rivas', 'ana.rivas@correo.com', '7345-6789', 5, true, now());

INSERT INTO asesor (codigo_empleado, nombres, apellidos, usuario_id, activo, creado_en) VALUES ('AS-001', 'Juan', 'Perez', 2, true, now());

-- 4. Cuentas bancarias
INSERT INTO cuenta_bancaria (cliente_id, banco_id, numero_cuenta, tipo_cuenta, activo, creado_en) VALUES (1, 1, '100200300', 'AHORRO', true, now());
INSERT INTO cuenta_bancaria (cliente_id, banco_id, numero_cuenta, tipo_cuenta, activo, creado_en) VALUES (2, 2, '400500600', 'AHORRO', true, now());
INSERT INTO cuenta_bancaria (cliente_id, banco_id, numero_cuenta, tipo_cuenta, activo, creado_en) VALUES (3, 3, '700800900', 'CORRIENTE', true, now());
INSERT INTO cuenta_bancaria (cliente_id, banco_id, numero_cuenta, tipo_cuenta, activo, creado_en) VALUES (1, 3, '111222333', 'CORRIENTE', false, now());

-- 5. Contratos
-- 1: ENVIADO, listo para liberar (ejemplo del README: 350.00 al 0.0300)
INSERT INTO contrato_garantia (numero_contrato, vendedor_id, comprador_id, categoria_id, cuenta_pago_id, descripcion, monto, porcentaje_comision, estado, empresa_envio, numero_guia, fecha_envio, creado_en) VALUES ('GAR-2026-000001', 1, 2, 1, 1, 'Laptop Lenovo IdeaPad usada', 350.00, 0.0300, 'ENVIADO', 'C807 Xpress', 'C807-55871', now(), now());
-- 2: FONDEADO, todavia no se puede liberar (R4)
INSERT INTO contrato_garantia (numero_contrato, vendedor_id, comprador_id, categoria_id, cuenta_pago_id, descripcion, monto, porcentaje_comision, estado, creado_en) VALUES ('GAR-2026-000002', 1, 2, 2, 1, 'Chaqueta de cuero talla M', 80.00, 0.0500, 'FONDEADO', now());
-- 3: CREADO, sin deposito
INSERT INTO contrato_garantia (numero_contrato, vendedor_id, comprador_id, categoria_id, cuenta_pago_id, descripcion, monto, porcentaje_comision, estado, creado_en) VALUES ('GAR-2026-000003', 3, 2, 3, 3, 'Escritorio de madera', 150.00, 0.0400, 'CREADO', now());
-- 4: ENVIADO, pero la cuenta de pago del vendedor esta inactiva (R7)
INSERT INTO contrato_garantia (numero_contrato, vendedor_id, comprador_id, categoria_id, cuenta_pago_id, descripcion, monto, porcentaje_comision, estado, empresa_envio, numero_guia, fecha_envio, creado_en) VALUES ('GAR-2026-000004', 1, 3, 1, 4, 'Audifonos inalambricos', 120.00, 0.0300, 'ENVIADO', 'C807 Xpress', 'C807-55902', now(), now());

-- 6. Depositos de los contratos ya fondeados
INSERT INTO deposito (contrato_id, cuenta_origen_id, asesor_id, referencia, fecha_hora) VALUES (1, 2, 1, 'TRF-AGR-0001', now());
INSERT INTO deposito (contrato_id, cuenta_origen_id, asesor_id, referencia, fecha_hora) VALUES (2, 2, 1, 'TRF-AGR-0002', now());
INSERT INTO deposito (contrato_id, cuenta_origen_id, asesor_id, referencia, fecha_hora) VALUES (4, 3, 1, 'TRF-BAC-0003', now());

-- Prueba ROLLBACK
INSERT INTO contrato_garantia (numero_contrato, vendedor_id, comprador_id, categoria_id, cuenta_pago_id, descripcion, monto, porcentaje_comision, estado, empresa_envio, numero_guia, fecha_envio, creado_en) VALUES ('GAR-2026-000005', 3, 2, 3, 3, 'Silla de oficina', 200.00, 0.0400, 'ENVIADO', 'C807 Xpress', 'C807-56010', now(), now());
INSERT INTO deposito (contrato_id, cuenta_origen_id, asesor_id, referencia, fecha_hora) VALUES (5, 2, 1, 'TRF-AGR-0004', now());