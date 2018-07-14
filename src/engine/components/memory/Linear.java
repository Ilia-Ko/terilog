package engine.components.memory;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import gui.control.ControlMemSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public abstract class Linear extends Component {

    private int digits;
    private Pin control, clock;
    private MemCell[] cells;
    // initialization
    Linear(ControlMain control, int digits) {
        super(control);
        this.digits = digits;
        cells = new MemCell[digits];
        for (int i = 0; i < digits; i++) cells[i] = new MemCell(this, i);
    }
    Linear(ControlMain control, Element data, int digits) {
        this(control, digits);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        HashSet<Pin> pins = new HashSet<>();

        // managing pins
        control = new Pin(this, true, 0, 1);
        clock = new Pin(this, true, 0, 2);
        pins.add(control);
        pins.add(clock);

        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        MenuItem itemSet = new MenuItem("Set value");
        itemSet.setOnAction(action -> {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialogs/memset.fxml"));
                Stage dialog = getControl().initDialog(loader, false);
                ((ControlMemSet) loader.getController()).initialSetup(dialog, this);
                dialog.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to show Memory Set dialog.");
            }
        });

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, itemSet);
        return menu;
    }

    // simulate
    @Override public void simulate() {
        LogicLevel ctrl = control.get();
        LogicLevel clck = clock.get();

        for (MemCell cell : cells) cell.simulate(ctrl, clck);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        // triggers' surrounding
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 2);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 2);

        // triggers themselves (everything x3)
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, digits * 3 * 5);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, digits * 3 * 5);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, digits * 3 * 3);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, digits * 3 * 2);
        summary.addResistor(digits * 3 * 3);
        summary.addDiode(digits * 3);
        summary.addInput(LogicLevel.POS, digits * 3);
        summary.addInput(LogicLevel.NIL, digits * 3);
        summary.addInput(LogicLevel.NEG, digits * 3);
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element l = super.writeXML(doc);
        l.setAttribute("mem", memToString());
        return l;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        parseString(comp.getAttribute("mem"));
    }

    // utils
    private String memToString() {
        StringBuilder builder = new StringBuilder();
        int c = 0;
        for (MemCell cell : cells) {
            builder.append(cell.memProperty().get().getDigitCharacter());
            if (c++ == 2) {
                builder.append('\'');
                c = 0;
            }
        }
        return builder.deleteCharAt(builder.length() - 1).toString();
    }
    private void parseString(String str) {
        int c = digits;
        for (char d : str.toCharArray()) {
            if (d == '\'') continue;
            LogicLevel sig = LogicLevel.parseDigit(d);
            if (sig == null)
                System.out.printf("WARNING: unknown digit '%c'. Using default Z value.\n", d);
            else
                cells[--c].memProperty().setValue(sig);
        }
    }
    public MemCell[] getCells() {
        return cells;
    }

}
