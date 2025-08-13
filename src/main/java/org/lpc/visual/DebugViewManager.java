// DebugViewManager.java
package org.lpc.visual;

import javafx.stage.Stage;
import org.lpc.VirtualMachine;
import org.lpc.utils.Logger;

public class DebugViewManager {
    private final VirtualMachine vm;

    public DebugViewManager(VirtualMachine vm) {
        this.vm = vm;
    }

    public void launchDebugViews() {
        createTextModeViewer();
        createMemoryViewer();
        createCpuViewer();
        Logger.log("Debug views launched successfully");
    }

    private void createTextModeViewer() {
        Stage stage = new Stage();
        TextModeViewer viewer = new TextModeViewer(vm.getCpu());
        viewer.start(stage);
        vm.getKeyboardDevice().setScene(stage.getScene());
        stage.requestFocus();
    }

    private void createMemoryViewer() {
        new MemoryViewer(vm.getCpu()).start(new Stage());
    }

    private void createCpuViewer() {
        new CpuViewer(vm.getCpu()).start(new Stage());
    }
}