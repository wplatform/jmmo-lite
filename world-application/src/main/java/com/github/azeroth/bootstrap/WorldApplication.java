package com.github.azeroth.bootstrap;

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
        WorldServerConfiguration.class})
public class WorldApplication implements CommandLineRunner {

    private final WorldServer worldServer;

    public static void main(String[] args) {
        SpringApplication.run(WorldApplication.class, args);
    }

    @Override
    public void run(String... args) {
        worldServer.start();
    }
}
