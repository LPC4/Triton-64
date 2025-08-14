// Main.java
package org.lpc;

import javafx.application.Application;
import javafx.stage.Stage;
import org.lpc.utils.Logger;

public final class Main extends Application {
    public static final String EXECUTED_FILE = "/test/test_typing.tc";

    private final PipelineExecutor pipelineExecutor = new PipelineExecutor();

    @Override
    public void init() {
        Logger.log("Initializing virtual machine components...");
        pipelineExecutor.initializeVM();
    }

    @Override
    public void start(Stage primaryStage) {
        pipelineExecutor.executePipeline();
    }

    @Override
    public void stop() {
        pipelineExecutor.shutdown();
    }

    public static void main(String[] args) {
        Logger.log("Launching...");
        launch(args);
    }
}