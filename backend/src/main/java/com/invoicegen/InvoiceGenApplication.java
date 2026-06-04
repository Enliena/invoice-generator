package com.invoicegen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Point d'entrée Spring Boot de l'API. */
@SpringBootApplication
public class InvoiceGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceGenApplication.class, args);
    }
}
