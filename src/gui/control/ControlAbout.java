package gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ControlAbout {

    // gui
    @FXML private TreeView<Label> content;
    @FXML private WebView text;

    // callback
    private Stage dialog;

    // initialization
    void initialSetup(Stage dialog) {
        this.dialog = dialog;
        loadTree();
        onItemClick("roots/about.html");
    }
    private void loadTree() {
        // roots
        TreeItem<Label> rootGlob = newItem("About", "roots/about.html");
        TreeItem<Label> rootMenu = newItem("Menu", "roots/menu.html");
        TreeItem<Label> rootComp = newItem("Components", "roots/components.html");
        rootGlob.getChildren().addAll(rootMenu, rootComp);
        content.setRoot(rootGlob);

        // about: menu.add
        TreeItem<Label> menuAdd = newItem("Add", "menu/add/add.html");
        TreeItem<Label> menuAddComp = newItem("Component", "menu/add/component.html");
        TreeItem<Label> menuAddWire = newItem("Wire", "menu/add/wire.html");
        menuAdd.getChildren().addAll(menuAddComp, menuAddWire);
        rootMenu.getChildren().add(menuAdd);

        // about: menu.circuit
        TreeItem<Label> menuCir = newItem("Circuit", "menu/cir/circuit.html");
        TreeItem<Label> menuCirParse = newItem("Parse", "menu/cir/parse.html");
        TreeItem<Label> menuCirClear = newItem("Clear", "menu/cir/clear.html");
        TreeItem<Label> menuCirStepInto = newItem("Step into", "menu/cir/step-into.html");
        TreeItem<Label> menuCirStepOver = newItem("Step over", "menu/cir/step-over.html");
        TreeItem<Label> menuCirRun = newItem("Run", "menu/cir/run.html");
        TreeItem<Label> menuCirSettings = newItem("Settings", "menu/cir/settings.html");
        menuCir.getChildren().addAll(menuCirParse, menuCirClear, menuCirStepInto, menuCirStepOver, menuCirRun, menuCirSettings);
        rootMenu.getChildren().add(menuCir);

        // about: menu.grid
        TreeItem<Label> menuGrid = newItem("Grid", "menu/grid.html");
        rootMenu.getChildren().add(menuGrid);

        // about: comp.MOSFET
        TreeItem<Label> compMOSFET = newItem("MOSFET", "comp/mosfet/mosfet.html");
        TreeItem<Label> compMOSFETHardP = newItem("Hard P", "comp/mosfet/hard-p.html");
        TreeItem<Label> compMOSFETHardN = newItem("Hard N", "comp/mosfet/hard-n.html");
        TreeItem<Label> compMOSFETSoftP = newItem("Soft P", "comp/mosfet/soft-p.html");
        TreeItem<Label> compMOSFETSoftN = newItem("Soft N", "comp/mosfet/soft-n.html");
        compMOSFET.getChildren().addAll(compMOSFETHardP, compMOSFETHardN, compMOSFETSoftP, compMOSFETSoftN);
        rootComp.getChildren().add(compMOSFET);

        // about: comp.lumped
        TreeItem<Label> compLumped = newItem("Lumped", "comp/lumped/lumped.html");
        TreeItem<Label> compLumpedDiode = newItem("Diode", "comp/lumped/diode.html");
        TreeItem<Label> compLumpedReconciliator = newItem("Reconciliator", "comp/lumped/reconciliator.html");
        TreeItem<Label> compLumpedIndicator = newItem("Indicator", "comp/lumped/indicator.html");
        TreeItem<Label> compLumpedVoltage = newItem("Voltage", "comp/lumped/voltage.html");
        TreeItem<Label> compLumpedClock = newItem("Clock", "comp/lumped/clock.html");
        compLumped.getChildren().addAll(compLumpedDiode, compLumpedIndicator, compLumpedReconciliator, compLumpedVoltage, compLumpedClock);

        // about: comp.logic 1-arg
        TreeItem<Label> compLog1 = newItem("Logic 1-arg", "comp/log1/log-1-arg.html");
        TreeItem<Label> compLog1Inv = newItem("Inverters", "comp/log1/inv/inverters.html");
        TreeItem<Label> compLog1InvNTI = newItem("NTI", "comp/log1/inv/nti.html");
        TreeItem<Label> compLog1InvSTI = newItem("STI", "comp/log1/inv/sti.html");
        TreeItem<Label> compLog1InvPTI = newItem("PTI", "comp/log1/inv/pti.html");
        compLog1Inv.getChildren().addAll(compLog1InvNTI, compLog1InvSTI, compLog1InvPTI);
        compLog1.getChildren().add(compLog1Inv);
        rootComp.getChildren().add(compLog1);

        // about: comp.logic 2-arg
        TreeItem<Label> compLog2 = newItem("Logic 2-arg", "comp/log2/log-2-arg.html");
        TreeItem<Label> compLog2Gate = newItem("Gates", "comp/log2/gate/gates.html");
        TreeItem<Label> compLog2GateNAND = newItem("NAND", "comp/log2/gates/nand.html");
        TreeItem<Label> compLog2GateNOR = newItem("NOR", "comp/log2/gates/nor.html");
        TreeItem<Label> compLog2GateNCON = newItem("NCON", "comp/log2/gates/ncon.html");
        TreeItem<Label> compLog2GateNANY = newItem("NANY", "comp/log2/gates/nany.html");
        TreeItem<Label> compLog2GateMUL = newItem("MUL", "comp/log2/gates/mul.html");
        TreeItem<Label> compLog2Key = newItem("Keys", "comp/log2/keys/keys.html");
        TreeItem<Label> compLog2KeyOKEY = newItem("OKEY", "comp/log2/keys/okey.html");
        TreeItem<Label> compLog2KeyCKEY = newItem("CKEY", "comp/log2/keys/ckey.html");
        compLog2Gate.getChildren().addAll(compLog2GateNAND, compLog2GateNOR, compLog2GateNCON, compLog2GateNANY, compLog2GateMUL);
        compLog2Key.getChildren().addAll(compLog2KeyOKEY, compLog2KeyCKEY);
        compLog2.getChildren().addAll(compLog2Gate, compLog2Key);
        rootComp.getChildren().add(compLog2);

        // about: comp.arithmetic
        TreeItem<Label> compArith = newItem("Arithmetic", "comp/arith/arithmetic.html");
        TreeItem<Label> compArithHalfAdder = newItem("Half Adder", "comp/arith/half-adder.html");
        TreeItem<Label> compArithFullAdder = newItem("Full Adder", "comp/arith/full-adder.html");
        TreeItem<Label> compArithTryteAdder = newItem("Tryte Adder", "comp/arith/tryte-adder.html");
        TreeItem<Label> compArithTryteMult = newItem("Tryte Multiplier", "comp/arith/tryte-multiplier.html");
        TreeItem<Label> compArithCounter = newItem("Counter", "comp/arith/counter.html");
        compArith.getChildren().addAll(compArithHalfAdder, compArithFullAdder, compArithTryteAdder, compArithTryteMult, compArithCounter);
        rootComp.getChildren().add(compArith);

        // about: comp.memory
        TreeItem<Label> compMem = newItem("Memory", "comp/memory/memory.html");
        TreeItem<Label> compMemTrigger = newItem("Trigger", "comp/memory/trigger.html");
        TreeItem<Label> compMemLin = newItem("Linear", "comp/memory/linear/linear.html");
        TreeItem<Label> compMemLinTriplet = newItem("Triplet", "comp/memory/linear/triplet.html");
        TreeItem<Label> compMemLinTryte = newItem("Tryte", "comp/memory/linear/tryte.html");
        TreeItem<Label> compMemLinWord = newItem("Word", "comp/memory/linear/word.html");
        TreeItem<Label> compMemLinDword = newItem("Dword", "comp/memory/linear/dword.html");
        TreeItem<Label> compMemFlat = newItem("Flat", "comp/memory/flat/flat.html");
        TreeItem<Label> compMemFlatRAM_6_6 = newItem("RAM 6x6", "comp/memory/flat/ram_6_6.html");
        compMemLin.getChildren().addAll(compMemLinTriplet, compMemLinTryte, compMemLinWord, compMemLinDword);
        compMemFlat.getChildren().add(compMemFlatRAM_6_6);
        compMem.getChildren().addAll(compMemTrigger, compMemLin, compMemFlat);
        rootComp.getChildren().add(compMem);

        // setup tree
        rootGlob.setExpanded(true);
        rootMenu.setExpanded(true);
        rootComp.setExpanded(true);
    }
    private TreeItem<Label> newItem(String name, String manual) {
        Label label = new Label(name);
        label.setOnMouseClicked(event -> onItemClick(manual));
        return new TreeItem<>(label);
    }

    // events
    private void onItemClick(String manual) {
        String path = getClass().getResource("manuals/" + manual).toExternalForm();
        text.getEngine().load(path);
    }
    @FXML private void btnCloseClicked() {
        dialog.close();
    }
    @FXML private void btnZoomIn() {
        text.setZoom(text.getZoom() * 1.12);
    }
    @FXML private void btnZoomOut() {
        text.setZoom(text.getZoom() * 0.88);
    }

}
