package cn.wolfcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootApplication
public class WebsocketServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketServerApplication.class, args);
    }
}
