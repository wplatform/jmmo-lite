package com.github.azeroth.world.server;

import com.github.azeroth.net.ChannelOperations;
import com.github.azeroth.net.server.TcpServer;
import com.github.azeroth.world.World;
import com.github.azeroth.world.network.NetworkOperations;
import com.github.azeroth.world.network.WorldRequest;
import com.github.azeroth.world.network.WorldResponse;
import com.github.azeroth.world.router.OpCodeRouter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;

@NoArgsConstructor(staticName = "create")
public class WorldServer extends TcpServer<WorldServer> {

    private World world;

    private final static OpCodeRouter instance = new OpCodeRouter();

    public final WorldServer route(Consumer<OpCodeRouter> routes) {
        Objects.requireNonNull(routes, "routes");
        routes.accept(instance);
        return handle((in, out) -> {
            instance.accept((WorldRequest) in, (WorldResponse) out);
        });
    }

    public final WorldServer withWorldInstance(World world) {
        this.world = world;
        return self();
    }

    @Override
    protected ChannelOperations.OnSetup onSetup() {
        return (ch, c, msg) -> new NetworkOperations(ch, c, world);
    }

    @Override
    protected WorldServer self() {
        return this;
    }


}
