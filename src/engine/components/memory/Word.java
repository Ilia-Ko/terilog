package engine.components.memory;

import gui.control.ControlMain;
import org.w3c.dom.Element;

public class Word extends Linear {

    public Word(ControlMain control) {
        super(control, 12);
    }
    public Word(ControlMain control, Element data) {
        super(control, data, 12);
    }

}
