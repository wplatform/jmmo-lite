package com.github.azeroth.net.router;

import com.github.azeroth.net.NettyInbound;
import com.github.azeroth.net.NettyOutbound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class Router<C extends Router<C, IN, OUT>, IN extends NettyInbound, OUT extends NettyOutbound> implements BiConsumer<IN, OUT> {

    private static final Logger log = LoggerFactory.getLogger(Router.class);

    private final List<RouteHandler<IN, OUT>> handlers = new ArrayList<>();

    public C route(Predicate<? super IN> condition, BiConsumer<? super IN, ? super OUT> handler) {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(handler, "handler");
        RouteHandler<IN, OUT> httpRouteHandler = new RouteHandler<>(condition, handler);
        handlers.add(httpRouteHandler);
        @SuppressWarnings("unchecked") C c = (C) this;
        return c;
    }


    @Override
    public void accept(IN request, OUT response) {
        // find I/0 handler to process this request
        try {
            BiConsumer<IN, OUT> cursor = identity(request);
            if (cursor != null) {
                cursor.accept(request, response);
            }

            for (RouteHandler<IN, OUT> handler : handlers) {
                if (handler.test(request)) {
                    handler.accept(request, response);
                }
            }

        } catch (Throwable e) {
            log.error("I/0 handler to process request {} error.", request, e);
        }


    }

    protected BiConsumer<IN, OUT> identity(IN request) {
        return null;
    }

    record RouteHandler<IN extends NettyInbound, OUT extends NettyOutbound>(
            Predicate<? super IN> condition, BiConsumer<? super IN, ? super OUT> handler) implements BiConsumer<IN, OUT>, Predicate<IN> {

        RouteHandler(Predicate<? super IN> condition, BiConsumer<? super IN, ? super OUT> handler) {
            this.condition = Objects.requireNonNull(condition, "condition");
            this.handler = Objects.requireNonNull(handler, "handler");
        }

        @Override
        public void accept(IN request, OUT response) {
            handler.accept(request, response);
        }

        @Override
        public boolean test(IN o) {
            return condition.test(o);
        }

    }

}
