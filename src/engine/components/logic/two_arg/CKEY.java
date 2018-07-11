package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Pin;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class CKEY extends Gate2to1 {

    // initialization
    public CKEY(ControlMain control) {
        super(control);
    }
    public CKEY(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected HashSet<Pin> initPins() {
        inA = new Pin(this, Pin.IN, 0, 1);
        inB = new Pin(this, Pin.IN, 2, 2);
        out = new Pin(this, Pin.OUT, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(inA);
        pins.add(inB);
        pins.add(out);
        return pins;
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 1);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 1);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        if (b == LogicLevel.POS) return a;
        else return LogicLevel.ZZZ;
    }

}
