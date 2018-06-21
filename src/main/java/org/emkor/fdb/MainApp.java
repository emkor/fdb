package org.emkor.fdb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class MainApp extends AbstractVerticle {

    @Override
    public void start() {
        JsonObject globalConfig = loadConfig();
        deployWebApp(globalConfig);
        vertx.deployVerticle(new FileWatcherVerticle(), new DeploymentOptions().setWorker(true));
    }

    private JsonObject loadConfig() {
        JsonObject globalConfig = config();
        System.out.println("Global config: " + globalConfig.encode());
        return globalConfig;
    }

    private void deployWebApp(JsonObject globalConfig) {
        System.out.println("Deploying HelloWorldVerticle...");
        DeploymentOptions webAppOptions = new DeploymentOptions().setConfig(globalConfig.getJsonObject("web_app"));
        vertx.deployVerticle(new HelloWorldVerticle(), webAppOptions);
    }

    @Override
    public void stop() {
        System.out.println("Stopped MainApp!");
    }
}