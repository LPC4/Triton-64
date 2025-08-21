// Main.java
package org.lpc;

import javafx.application.Application;
import javafx.stage.Stage;
import org.lpc.utils.Logger;

public final class Main extends Application {
    public static final String EXECUTED_FILE = "/kernel/shell.tc";

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
    //  we can't do anything with a Struct type if it's not a pointer,
    //  so arrays won't work on structs right now unless you make a
    //  struct pointer array which is not very convenient
    //
    //TODO:
    //  some simplicity in string syntax like
    //      var str = "Hello, World!"
    //  turns into
    //      var str: byte* = malloc(strideOf("Hello, World!"))
    //      @str = "Hello, World!"
    //  but it's difficult because malloc is from runtime, rn just pasting
    //  the stdlibs as string before the rest of the program
    //  maybe I should add a linker but that will be a huge task

    public static void main(String[] args) {
        Logger.log("Launching...");
        launch(args);
    }
}