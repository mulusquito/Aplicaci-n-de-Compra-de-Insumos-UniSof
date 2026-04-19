package com.unisof.insumos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InsumosApplicationTests {

	@Test
	@DisplayName("La aplicación Spring Boot carga el contexto correctamente")
	void contextLoads() {
	}

}
