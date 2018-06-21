package org.emkor.fdb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class MainApp extends AbstractVerticle {

    @Override
    public void start() {
        JsonObject globalConfig = config();
        System.out.println("Global config: " + globalConfig.encode());
        DeploymentOptions options = new DeploymentOptions().setConfig(globalConfig.getJsonObject("web_app"));
        System.out.println("Deploying HelloWorldVerticle...");
        vertx.deployVerticle(new HelloWorldVerticle(), options);
    }

    @Override
    public void stop() {
        System.out.println("Stopped MainApp!");
    }
}