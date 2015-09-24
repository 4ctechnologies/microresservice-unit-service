package be.foreseegroup.micro.resourceservice.unit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Kaj on 24/09/15.
 */

@SpringBootApplication
@EnableDiscoveryClient
public class UnitServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnitServiceApplication.class, args);
    }
}
