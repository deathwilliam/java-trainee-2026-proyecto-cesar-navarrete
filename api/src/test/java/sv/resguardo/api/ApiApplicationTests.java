package sv.resguardo.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:postgresql://localhost:5432/resguardo_test",
		"jwt.clave=clave-solo-para-las-pruebas-de-resguardo-2026"
})
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}