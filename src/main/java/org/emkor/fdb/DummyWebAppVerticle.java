package org.emkor.fdb;

import io.vertx.core.AbstractVerticle;

public class DummyWebAppVerticle extends AbstractVerticle {

    @Override
    public void start() {
        String name = config().getString("name", "World");
        Integer port = config().getInteger("http_port", 8090);
        vertx.createHttpServer().requestHandler(req -> req.response().end("Hello " + name + "!")).listen(port);
    }
}