package gui.control;

import engine.Circuit;
import engine.Component;
import engine.TerilogIO;
import engine.Wire;
import engine.lumped.Diode;
import engine.lumped.Indicator;
import engine.lumped.Reconciliator;
import engine.lumped.Voltage;
import engine.transistors.HardN;
import engine.transistors.HardP;
import engine.transistors.SoftN;
import engine.transistors.SoftP;
import gui.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class ControlMain {

    // dimensions
    private static final double GRID_PERIOD = 0.01; // relative to screen width
    public  static final double GRID_POINT_RADIUS = 1.0 / 18.0; // in periods
    private static final double GRID_HOVER_RADIUS = 0.5; // in periods
    public  static final double LINE_WIDTH = 1.0 / 12.0; // in periods
    private static final double GRID_PANE_GAP = 0.01;
    // opacity
    private static final double OPACITY_NORMAL = 1.0;
    private static final double OPACITY_FLYING = 0.5;
    // input bounds
    private static final double MIN_GRID_PERIOD = 1.0 / 6.0; // relative to period
    private static final double MAX_GRID_PERIOD = 1.0 * 144.0; // relative to period
    // colours
    private static final Color COL_CLEAR = Color.rgb(219, 255, 244);
    private static final Color COL_GRID  = Color.BLACK;
    private static final Color COL_HOVER = Color.DARKRED;

    // some gui
    private Stage stage;
    @FXML private MenuItem menuPlay;
    @FXML private MenuItem menuZoomIn;
    @FXML private MenuItem menuZoomOut;
    @FXML private ScrollPane scroll;
    @FXML private StackPane stack;
    @FXML private Canvas field; // field for circuit
    @FXML private Canvas point; // mouse hovering
    private String defFont;

    // dimensions
    private double screenW;
    private double p; // the period of the field grid in pixels
    private double minPeriod, maxPeriod; // in pixels
    private int w, h; // field width and height in periods

    // circuit logic
    private Circuit circuit;
    private TerilogIO ioSystem;
    private File lastSave;

    // mouse logic
    private int mouseX, mouseY;
    private boolean holdingWire, holdingComp;
    private Wire flyWire;
    private Component flyComp;

    // initialization
    public void initialSetup(Stage stage, String defFont, double screenWidth, double screenHeight) {
        this.stage = stage;
        this.defFont = defFont;
        screenW = screenWidth;

        // dimensions
        p = screenWidth * GRID_PERIOD;
        w = rnd(screenWidth / p);
        h = rnd(screenHeight / p);
        minPeriod = p * MIN_GRID_PERIOD; if (minPeriod < 1.0) minPeriod = 1.0;
        maxPeriod = p * MAX_GRID_PERIOD;

        // circuit logic
        circuit = new Circuit(this);
        try {
            ioSystem = new TerilogIO(circuit);
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
            ioSystem = null;
        }
        lastSave = null;

        // mouse logic
        mouseX = 0;
        mouseY = 0;
        holdingComp = false;
        holdingWire = false;
        flyComp = null;
        flyWire = null;

        // handle some events
        scroll.addEventFilter(MouseEvent.ANY, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                fieldMouseClicked(event);
                event.consume();
            }
        });
        field.setOnMouseEntered(event -> {
            field.requestFocus();
            point.setVisible(true);
        });
        field.setOnMouseExited(event -> point.setVisible(false));
        field.setCursor(Cursor.CROSSHAIR);

        // prepare field
        updateGridParameters();
        renderField();
        renderPoint();
    }
    public ContextMenu makeContextMenuFor(Component comp) {
        // move
        MenuItem itemMove = new MenuItem("Move");
        itemMove.setAccelerator(KeyCombination.valueOf("M"));
        itemMove.setOnAction(event -> moveComp(comp));

        // delete
        MenuItem itemDel = new MenuItem("Remove");
        itemDel.setAccelerator(KeyCombination.valueOf("Delete"));
        itemDel.setOnAction(event -> deleteComp(comp));

        // rotate CW
        MenuItem itemRotCW = new MenuItem("Rotate right (CW)");
        itemRotCW.setAccelerator(KeyCombination.valueOf("R"));
        itemRotCW.setOnAction(event -> comp.rotateCW());

        // rotate CCW
        MenuItem itemRotCCW = new MenuItem("Rotate left (CCW)");
        itemRotCCW.setAccelerator(KeyCombination.valueOf("L"));
        itemRotCCW.setOnAction(event -> comp.rotateCCW());

        // mirror horizontally
        MenuItem itemMirrorH = new MenuItem("Mirror horizontally");
        itemMirrorH.setAccelerator(KeyCombination.valueOf("X"));
        itemMirrorH.setOnAction(event -> comp.mirrorHorizontal());

        // mirror vertically
        MenuItem itemMirrorV = new MenuItem("Mirror vertically");
        itemMirrorV.setAccelerator(KeyCombination.valueOf("Y"));
        itemMirrorV.setOnAction(event -> comp.mirrorVertical());

        return new ContextMenu(itemMove, itemDel, itemRotCW, itemRotCCW, itemMirrorH, itemMirrorV);
    }

    // some actions
    @FXML private void fieldMouseMoved(MouseEvent mouse) {
        // get snapped point
        int x = snapCoordinateToGrid(mouse.getX());
        int y = snapCoordinateToGrid(mouse.getY());
        mouseX = rnd(x / p);
        mouseY = rnd(y / p);

        // update snapped point
        point.setVisible(true);
        point.setTranslateX((mouseX - GRID_HOVER_RADIUS) * p);
        point.setTranslateY((mouseY - GRID_HOVER_RADIUS) * p);

        if (holdingComp) // move flying canvas with a component
            flyComp.setPos(mouseX, mouseY);
        else if (holdingWire) // lay out flying wire
            flyWire.layoutAgain(mouseX, mouseY);
    }
    @FXML private void fieldMouseClicked(MouseEvent mouse) {
        if (mouse.getButton() == MouseButton.PRIMARY) {
            if (holdingWire) // add flying wire to circuit
                finishWireInsertion();
            else if (holdingComp) // add flying component to circuit
                finishCompInsertion();
        }
    }
    @FXML private void fieldKeyPressed(KeyEvent key) {
        KeyCode code = key.getCode();
        if (code == KeyCode.ESCAPE) { // stop insertion
            if (holdingComp || holdingWire) breakInsertion();
        } else if (code == KeyCode.SPACE && holdingWire) { // flip flying wire
            flyWire.flip();
            key.consume();
        }

        // actions with flying component
        if (holdingComp) componentKeyPressed(flyComp, code);
    }
    public void componentKeyPressed(Component comp, KeyCode code) {
        switch (code) {
            case M:
                moveComp(comp);
                break;
            case DELETE:
                deleteComp(comp);
                break;
            case R:
                comp.rotateCW();
                break;
            case L:
                comp.rotateCCW();
                break;
            case X:
                comp.mirrorHorizontal();
                break;
            case Y:
                comp.mirrorVertical();
                break;
        }
    }
    private void moveComp(Component comp) {
        deleteComp(comp);
        flyComp = comp.newCompOfTheSameClass();
        beginCompInsertion();
    }
    private void deleteComp(Component comp) {
        stack.getChildren().remove(comp.getBasis());
        circuit.del(comp);
    }

    // insertion
    private void beginCompInsertion() {
        // setup basis
        Canvas basis = flyComp.getBasis();
        stack.getChildren().add(basis);
        StackPane.setAlignment(basis, Pos.TOP_LEFT);
        basis.setMouseTransparent(true);
        basis.setCursor(Cursor.CLOSED_HAND);

        // initialize component
        flyComp.setGlobalAlpha(OPACITY_FLYING);
        flyComp.setGridPeriod(p);
        flyComp.setPos(mouseX, mouseY);
        flyComp.render();
        holdingComp = true;
    }
    private void beginWireInsertion() {
        // setup basis
        Canvas basis = flyWire.getBasis();
        stack.getChildren().add(basis);
        StackPane.setAlignment(basis, Pos.TOP_LEFT);
        basis.setMouseTransparent(true);

        // initialize wire
        flyWire.setGlobalAlpha(OPACITY_FLYING); // make it 'transparent'
        flyWire.setGridPeriod(p);
        flyWire.setPos(mouseX, mouseY);
        flyWire.render();
        holdingWire = true;
    }
    private void finishCompInsertion() {
        flyComp.setGlobalAlpha(OPACITY_NORMAL);
        flyComp.render();
        flyComp.initEvents(this);
        circuit.add(flyComp);
        holdingComp = false;
    }
    private void finishWireInsertion() {
        flyWire.setGlobalAlpha(OPACITY_NORMAL);
        flyWire.render();
        circuit.add(flyWire);
        holdingWire = false;
    }
    private void breakInsertion() {
        // remove basis
             if (holdingComp) stack.getChildren().remove(flyComp.getBasis());
        else if (holdingWire) stack.getChildren().remove(flyWire.getBasis());

        // update flags
        holdingComp = false;
        holdingWire = false;
        flyComp = null;
        flyWire = null;
    }

    // rendering
    private void updateGridParameters() {
        // update field
        field.setWidth(p * w);
        field.setHeight(p * h);

        // update stack
        stack.setPrefWidth(p * w);
        stack.setPrefHeight(p * h);

        // update circuit
        circuit.updateGridPeriod(p);
        circuit.renderAll();

        // update flying renderable
        if (holdingComp) {
            flyComp.setGridPeriod(p);
            flyComp.render();
        } else if (holdingWire) {
            flyWire.setGridPeriod(p);
            flyWire.render();
        }
    }
    private void renderField() {
        // configure gc
        GraphicsContext gc = field.getGraphicsContext2D();
        gc.save();
        gc.scale(p, p);
        gc.setLineWidth(LINE_WIDTH);

        // clear background
        gc.setFill(COL_CLEAR);
        gc.fillRect(0.0, 0.0, w, h);

        // draw grid points
        double a = GRID_POINT_RADIUS;
        gc.setFill(COL_GRID);
        for (int i = 0; i <= w; i++)
            for (int j = 0; j <= h; j++)
                gc.fillOval(i - a, j - a, 2 * a, 2 * a);

        // finish rendering
        gc.restore();
    }
    private void renderPoint() {
        double b = LINE_WIDTH * p;
        double a = GRID_HOVER_RADIUS * p - b / 2;
        double c = GRID_HOVER_RADIUS * p;

        // setup point
        point.setWidth(2 * c);
        point.setHeight(2 * c);
        point.toFront();

        // prepare gc
        GraphicsContext gc = point.getGraphicsContext2D();
        gc.setStroke(COL_HOVER);
        gc.setLineWidth(b);
        gc.clearRect(0, 0, 2 * c, 2 * c);

        // stroke point
        gc.strokeOval(b / 2.0, b / 2.0, a * 2, a * 2);
    }

    // menu.file
    @FXML private void menuOpen() {
        // configure file chooser
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open TLG file");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TLG files", "*.tlg"));

        // get file
        File tlg = chooser.showOpenDialog(stage);

        // load it
        try {
            ioSystem.loadTLG(tlg);
        } catch (IOException e) {
            e.printStackTrace();
            // show IO alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("IO Error:");
            alert.setContentText("Failed to open " + tlg.getAbsolutePath());
            alert.showAndWait();
        } catch (SAXException e) {
            e.printStackTrace();
            // show XML alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("XML error:");
            alert.setContentText("Failed to parse " + tlg.getAbsolutePath());
            alert.showAndWait();
        }
    }
    @FXML private void menuSave() {
        if (lastSave == null)
            menuSaveAs();
        else
            updateSavedFile();
    }
    @FXML private void menuSaveAs() {
        // configure file chooser
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save TLG file");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TLG files", "*.tlg"));

        // get file
        lastSave = chooser.showSaveDialog(stage);

        // save it
        updateSavedFile();
    }
    @FXML private void menuToggle() {
        stage.setFullScreen(!stage.isFullScreen());
    }
    @FXML private void menuQuit() {
        Platform.exit();
        System.exit(0);
    }

    // menu.add
    @FXML private void menuHardN() {
        breakInsertion();
        flyComp = new HardN();
        beginCompInsertion();
    }
    @FXML private void menuHardP() {
        breakInsertion();
        flyComp = new HardP();
        beginCompInsertion();
    }
    @FXML private void menuSoftN() {
        breakInsertion();
        flyComp = new SoftN();
        beginCompInsertion();
    }
    @FXML private void menuSoftP() {
        breakInsertion();
        flyComp = new SoftP();
        beginCompInsertion();
    }
    @FXML private void menuDiode() {
        breakInsertion();
        flyComp = new Diode();
        beginCompInsertion();
    }
    @FXML private void menuReconciliator() {
        breakInsertion();
        flyComp = new Reconciliator();
        beginCompInsertion();
    }
    @FXML private void menuVoltage() {
        breakInsertion();
        flyComp = new Voltage();
        beginCompInsertion();
    }
    @FXML private void menuIndicator() {
        breakInsertion();
        flyComp = new Indicator();
        beginCompInsertion();
    }
    @FXML private void menuWire() {
        if (!holdingWire && field.isHover()) {
            if (holdingComp) breakInsertion();
            flyWire = new Wire();
            beginWireInsertion();
        }
    }

    // menu.simulate
    @FXML private void menuPlay() {
        if (circuit.isSimulationRunning()) {
            circuit.stopSimulation();
            menuPlay.setText("Start");
        } else {
            circuit.startSimulation();
            menuPlay.setText("Stop");
        }
    }
    @FXML private void menuSettings() {}

    // menu.grid
    @FXML private void menuGrid() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialog-grid.fxml"));
        try {
            // init gui
            GridPane root = loader.load();
            root.setStyle(defFont);
            root.setHgap(screenW * GRID_PANE_GAP);
            root.setVgap(screenW * GRID_PANE_GAP);
            Scene scene = new Scene(root);
            Stage dialog = new Stage(StageStyle.UNDECORATED);
            dialog.setScene(scene);

            // init controller
            ControlDlgGrid control = loader.getController();
            control.initialSetup(dialog, this, w, h);

            // show
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to show Grid Size dialog.");
        }
    }
    @FXML private void menuZoomIn() {
        double newPeriod = p * 1.12;
        if (newPeriod <= maxPeriod) {
            p = newPeriod;
            updateGridParameters();
            renderField();
            renderPoint();
            point.setVisible(false); // because mouseX and mouseY became incorrect
            menuZoomOut.setDisable(false);
        } else {
            menuZoomIn.setDisable(true);
        }
    }
    @FXML private void menuZoomOut() {
        double newPeriod = p * 0.88;
        if (newPeriod >= minPeriod) {
            p = newPeriod;
            updateGridParameters();
            renderField();
            renderPoint();
            point.setVisible(false); // because mouseX and mouseY became incorrect
            menuZoomIn.setDisable(false);
        } else {
            menuZoomOut.setDisable(true);
        }
    }

    // menu.help
    @FXML private void menuAbout() {}
    @FXML private void menuCredits() {}

    // informative
    public int getGridWidth() {
        return w;
    }
    public int getGridHeight() {
        return h;
    }
    public void setGridDimensions(int width, int height) {
        w = width;
        h = height;
        updateGridParameters();
        renderField();
    }

    // utilities
    private static int rnd(double val) {
        return (int) Math.round(val);
    }
    private int snapCoordinateToGrid(double c) {
        return rnd(Math.round(c / p) * p);
    }
    private void updateSavedFile() {
        try {
            ioSystem.saveTLG(lastSave);
        } catch (TransformerException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("IO error:");
            alert.setContentText("Failed to save " + lastSave.getAbsolutePath());
            alert.showAndWait();
            lastSave = null;
        }
    }

}
