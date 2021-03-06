package org.emkor.fdb;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import org.emkor.fdb.verticle.DummyWebApp;
import org.emkor.fdb.verticle.FileUpdateHandler;
import org.emkor.fdb.verticle.FileWatcher;

public class MainApp extends AbstractVerticle {

    @Override
    public void start() {
        JsonObject globalConfig = loadConfig();
        deployWebApp(globalConfig);
        deployFileUpdateHandler(globalConfig);
        deployFileWatcher(globalConfig);
    }

    private void deployFileUpdateHandler(JsonObject globalConfig) {
        vertx.deployVerticle(
                new FileUpdateHandler(),
                new DeploymentOptions().setConfig(globalConfig.getJsonObject("inotify_watcher")));
    }

    private void deployFileWatcher(JsonObject globalConfig) {
        DeploymentOptions fileWatcherConfig = new DeploymentOptions()
                .setConfig(globalConfig.getJsonObject("inotify_watcher"))
                .setWorker(true);
        vertx.deployVerticle(new FileWatcher(), fileWatcherConfig);
    }

    private JsonObject loadConfig() {
        JsonObject globalConfig = config();
        System.out.println("Global config: " + globalConfig.encode());
        return globalConfig;
    }

    private void deployWebApp(JsonObject globalConfig) {
        System.out.println("Deploying DummyWebApp...");
        DeploymentOptions webAppOptions = new DeploymentOptions().setConfig(globalConfig.getJsonObject("web_app"));
        vertx.deployVerticle(new DummyWebApp(), webAppOptions);
    }

    @Override
    public void stop() {
        System.out.println("Stopped MainApp!");
    }
}