package engine.components.logic.inverters;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

abstract class Inverter extends Component {

    private Pin in, out;

    // initialization
    Inverter(ControlMain control) {
        super(control);
    }
    Inverter(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/logic/inverters/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        in = new Pin(this, true, false, 0, 1);
        out = new Pin(this, false, true, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(in);
        pins.add(out);
        return pins;
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        boolean changed;
        LogicLevel i = in.querySigFromNode();

        // simulate
        if (i.isUnstable()) changed = out.update(LogicLevel.ERR);
        else changed = out.update(invert(i));

        if (changed) affected.add(out.getNode());
        return affected;
    }
    protected abstract LogicLevel invert(LogicLevel a);

}
