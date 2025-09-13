package com.github.azeroth.bootstrap;

import com.rainbowland.portal.PortalRestfulServer;
import com.rainbowland.portal.PortalRpcServer;
import com.rainbowland.portal.boot.PortalServerConfiguration;
import com.rainbowland.service.boot.AuthServiceConfiguration;
import com.rainbowland.service.boot.CharacterServiceConfiguration;
import com.rainbowland.worldserver.boot.WorldServerConfiguration;
import com.rainbowland.worldserver.server.WorldServer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@RequiredArgsConstructor
@SpringBootApplication
@Import({AuthServiceConfiguration.class,
        CharacterServiceConfiguration.class,
        PortalServerConfiguration.class,
        WorldServerConfiguration.class})
public class AllInOneApplication implements CommandLineRunner {

    private final PortalRestfulServer restfulServer;
    private final PortalRpcServer portalRpcServer;
    private final WorldServer worldServer;


    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AllInOneApplication.class);
        application.run(args);
    }


    @Override
    public void run(String... args) {
        portalRpcServer.start();
        restfulServer.start();
        worldServer.start();
    }
}
