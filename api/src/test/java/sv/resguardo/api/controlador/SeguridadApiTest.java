package sv.resguardo.api.controlador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/resguardo_test",
        "spring.jpa.hibernate.ddl-auto=create",
        "jwt.clave=clave-solo-para-las-pruebas-de-resguardo-2026"
})
@AutoConfigureMockMvc
class SeguridadApiTest {

    private static final String CONTRATO_NUEVO = """
            { "duiComprador": "02345678-9", "categoria": "ELECTRONICA", "cuentaPago": "100200300",
              "descripcion": "Prueba de seguridad", "monto": 250.00 }""";

    @Autowired
    private MockMvc mvc;

    private String tokenDe(String usuario, String clave) throws Exception {
        String json = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"%s\",\"clave\":\"%s\"}".formatted(usuario, clave)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.split("\"token\":\"")[1].split("\"")[0];
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void sinTokenDa401ConProblemDetail() throws Exception {
        mvc.perform(get("/api/contratos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void unTokenFalsoDa401() throws Exception {
        mvc.perform(get("/api/contratos").header("Authorization", bearer("esto.no.sirve")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void laClaveMalaDa401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"mlopez\",\"clave\":\"otra\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Usuario o clave incorrectos"));
    }

    @Test
    void elAsesorLeePeroNoOpera() throws Exception {
        String asesor = tokenDe("jperez", "Asesor2026");

        mvc.perform(get("/api/contratos").header("Authorization", bearer(asesor)))
                .andExpect(status().isOk());

        mvc.perform(post("/api/contratos").header("Authorization", bearer(asesor))
                        .contentType(MediaType.APPLICATION_JSON).content(CONTRATO_NUEVO))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Sin permiso"));
    }

    @Test
    void elClienteNoLeeLaBitacora() throws Exception {
        mvc.perform(get("/api/bitacora").header("Authorization", bearer(tokenDe("mlopez", "Cliente2026"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Sin permiso"));
    }

    @Test
    void elAdminNoLiberaFondos() throws Exception {
        mvc.perform(post("/api/contratos/GAR-2026-000001/liberacion")
                        .header("Authorization", bearer(tokenDe("admin", "Admin2026"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void elVendedorSaleDelTokenNoDelJson() throws Exception {
        String conOtroVendedor = """
                { "vendedor": "arivas", "duiComprador": "02345678-9", "categoria": "ELECTRONICA",
                  "cuentaPago": "100200300", "descripcion": "Prueba del token", "monto": 250.00 }""";

        mvc.perform(post("/api/contratos").header("Authorization", bearer(tokenDe("mlopez", "Cliente2026")))
                        .contentType(MediaType.APPLICATION_JSON).content(conOtroVendedor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendedor").value("Marta Lopez"));

        mvc.perform(get("/api/bitacora").header("Authorization", bearer(tokenDe("jperez", "Asesor2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuario").value("mlopez"))
                .andExpect(jsonPath("$[0].operacion").value("CREAR_CONTRATO"));
    }

    @Test
    void elCompradorLiberaYLaBitacoraLoDice() throws Exception {
        mvc.perform(post("/api/contratos/GAR-2026-000001/liberacion")
                        .header("Authorization", bearer(tokenDe("lmartinez", "Cliente2026"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comision").value(10.5))
                .andExpect(jsonPath("$.pagoVendedor").value(339.5));

        mvc.perform(get("/api/bitacora").header("Authorization", bearer(tokenDe("jperez", "Asesor2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuario").value("lmartinez"))
                .andExpect(jsonPath("$[0].operacion").value("LIBERAR_FONDOS"))
                .andExpect(jsonPath("$[0].resultado").value("EXITOSA"));
    }
}