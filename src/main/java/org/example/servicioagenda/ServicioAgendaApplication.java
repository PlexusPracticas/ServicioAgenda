package org.example.servicioagenda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicioAgendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioAgendaApplication.class, args);
    }

}
