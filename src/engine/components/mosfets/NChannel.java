package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import static engine.LogicLevel.*;

abstract class NChannel extends MOSFET {

    private final int vgsth;

    NChannel(ControlMain control, int vgsth) {
        super(control);
        this.vgsth = vgsth;
    }
    NChannel(ControlMain control, Element data, int vgsth) {
        super(control, data);
        this.vgsth = vgsth;
    }

    @Override public void simulate() {
        LogicLevel g = gate.get();
        LogicLevel s = source.get();
        LogicLevel d = drain.get();
        boolean opened = g.volts() - s.volts() >= vgsth;

        if (s == ZZZ && d == ZZZ || s == ERR || d == ERR) {
            // do nothing
        } else if (g == ERR || (g == ZZZ && (s.isStable() || d.isStable()))) {
            source.put(ERR);
            drain.put(ERR);
        } else if (s.isStable() && (opened || s.volts() > NIL.volts())) {
            drain.put(s);
            source.put(ZZZ);
//        } else if (d.isStable() && s.isUnstable() && d.volts() < s.volts()) {
//            source.put(d);
//            drain.put(ZZZ);
        } else if (opened && s.conflicts(d)) {
            source.put(ERR);
            drain.put(ERR);
        } else if (!opened) {
            source.put(ZZZ);
            drain.put(ZZZ);
        }
    }

}
