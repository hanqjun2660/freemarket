package com.api.freemarket;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(servers = {
		@Server(url = "https://freeapi.devsj.site", description = "Default Server URL"),
		@Server(url = "http://localhost:4544", description = "Local")
})
@SpringBootApplication
public class FreemarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreemarketApplication.class, args);
	}

}
