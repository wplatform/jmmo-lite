package com.github.azeroth.bootstrap;

import com.rainbowland.portal.PortalRestfulServer;
import com.rainbowland.portal.PortalRpcServer;
import com.rainbowland.portal.boot.PortalServerConfiguration;
import com.rainbowland.service.boot.AuthServiceConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@RequiredArgsConstructor
@SpringBootApplication
@Import({AuthServiceConfiguration.class, PortalServerConfiguration.class})
public class PortalApplication implements CommandLineRunner {

    private final PortalRestfulServer restfulServer;
    private final PortalRpcServer portalRpcServer;


    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(PortalApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }


    @Override
    public void run(String... args) {
        portalRpcServer.start();
        restfulServer.start();
    }

}
