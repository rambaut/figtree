package figtree.panel;

import jam.controlpalettes.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id: BasicControlPalette.java 856 2007-12-13 23:36:02Z rambaut $
 */
public class SimpleControlPalette extends JPanel implements ControlPalette {

    public SimpleControlPalette() {
        BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
        setLayout(layout);
        setOpaque(true);
    }


    public JPanel getPanel() {
        return this;
    }

    private ControllerListener controllerListener = new ControllerListener() {
        public void controlsChanged() {
            layoutControls();
        }
    };

    public void addController(Controller controller) {
        controllers.add(controller);
        controller.addControllerListener(controllerListener);
        setupControls();
    }

    public void addController(int position, Controller controller) {
        controllers.add(position, controller);
        controller.addControllerListener(controllerListener);
        setupControls();
    }

    public void removeController(Controller controller) {
        controller.removeControllerListener(controllerListener);
        controllers.remove(controller);
        setupControls();
    }

    public int getControllerCount() {
        return controllers.size();
    }

    public void fireControlsChanged() {
        for (ControlPaletteListener listener : listeners) {
            listener.controlsChanged();
        }
    }

    public void addControlPaletteListener(ControlPaletteListener listener) {
        listeners.add(listener);
    }

    public void removeControlPaletteListener(ControlPaletteListener listener) {
        listeners.remove(listener);
    }

    private final List<ControlPaletteListener> listeners = new ArrayList<ControlPaletteListener>();

    private void setupControls() {
        removeAll();

        for (Controller controller : controllers) {
            add(controller.getPanel());
        }
    }

    public void layoutControls() {
        validate();
    }

    public void initialize() {
        for (Controller controller : controllers) {
            controller.initialize();
        }
    }

    public void getSettings(Map<String,Object> settings) {
        for (Controller controller : controllers) {
            controller.getSettings(settings);
        }
    }

    public void setSettings(Map<String,Object> settings) {
        for (Controller controller : controllers) {
            controller.setSettings(settings);
        }
    }

    private List<Controller> controllers = new ArrayList<Controller>();

}