package engine.components.lumped;

import engine.components.Component;
import engine.components.Pin;
import gui.control.ControlMain;
import javafx.scene.layout.Pane;
import org.w3c.dom.Element;

public class Diode extends Component {

    private Pin anode, cathode;

    // initialization
    public Diode(ControlMain control) {
        super(control);
    }
    public Diode(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pane loadContent() {
        Pane pane = super.loadContent();
        anode = new Pin(pane, "anode", 0, 1);
        cathode = new Pin(pane, "cathode", 4, 1);
        return pane;
    }

    // simulation
    @Override public void simulate() {
        // TODO: implement simulation logic
    }

    // xml info
    @Override protected String getAttrClass() {
        return "diode";
    }

}