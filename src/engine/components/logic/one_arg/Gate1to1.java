package engine.components.logic.one_arg;

import engine.LogicLevel;
import engine.components.BusComponent;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

abstract class Gate1to1 extends BusComponent {

    private Pin in, out;

    // initialization
    Gate1to1(ControlMain control) {
        super(control, false);
        capacity.setValue(6);
    }
    Gate1to1(ControlMain control, Element data) {
        super(control, false, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/one_arg/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        in = new Pin(this, true, 1, 0, 1);
        out = new Pin(this, false, 1, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(out);
        return pins;
    }
    @Override protected HashSet<Pin> getDependentPins() {
        return getPins();
    }

    // simulation
    @Override public void simulate() {
        LogicLevel[] inp = in.get();
        LogicLevel[] out = new LogicLevel[capacity.get()];

        for (int i = 0; i < capacity.get(); i++) {
            if (inp[i].isUnstable()) out[i] = inp[i];
            else out[i] = function(inp[i]);
        }
        this.out.put(out);
    }
    abstract LogicLevel function(LogicLevel a);

}
