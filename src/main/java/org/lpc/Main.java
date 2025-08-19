// Main.java
package org.lpc;

import javafx.application.Application;
import javafx.stage.Stage;
import org.lpc.utils.Logger;

public final class Main extends Application {
    public static final String EXECUTED_FILE = "/test/test_console.tc";

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

    // TODO:
    //  maybe make
    //      var x: Struct* = malloc(strideOf(Struct))
    //      x[0]
    //  return a struct* instead of auto dereferencing it?

    public static void main(String[] args) {
        Logger.log("Launching...");
        launch(args);
    }
}