package figtree.treeviewer;

import figtree.treeviewer.decorators.Decorator;
import figtree.treeviewer.decorators.HSBContinuousColorDecorator;
import figtree.treeviewer.decorators.HSBDiscreteColorDecorator;
import jam.controlpalettes.AbstractController;

import javax.swing.*;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class AttributeColourController extends AbstractController {

    public static final String CONTROLLER_KEY = "colour";

    public AttributeColourController() {
    }

    public Decorator getDecoratorForAttribute(String attribute) {
        return attributeDecoratorMap.get(attribute);
    }

    public void setDecoratorForAttribute(String attribute, Decorator decorator) {
        attributeDecoratorMap.put(attribute, decorator);
    }

    @Override
    public JComponent getTitleComponent() {
        return null;
    }

    @Override
    public JPanel getPanel() {
        return null;
    }

    @Override
    public boolean isInitiallyVisible() {
        return false;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void getSettings(Map<String, Object> settings) {
        for (String key : settings.keySet()) {
            if (key.startsWith(CONTROLLER_KEY + ".")) {
                Decorator decorator = null;

                String attribute = key.substring(CONTROLLER_KEY.length() + 1);
                String colourSettings = (String)settings.get(key);
                if (colourSettings.startsWith("HSBDiscrete")) {
                    decorator = new HSBDiscreteColorDecorator(colourSettings.substring("HSBDiscrete".length()));
                } else if (colourSettings.startsWith("HSBContinuous")) {
                    decorator = new HSBContinuousColorDecorator(colourSettings.substring("HSBContinuous".length()));
                } else {
                    throw new IllegalArgumentException("Unrecognized colour decorator type");
                }

                attributeDecoratorMap.put(attribute, decorator);
            }
        }
    }

    @Override
    public void setSettings(Map<String, Object> settings) {
        for (String attribute : attributeDecoratorMap.keySet()) {
            Decorator decorator = attributeDecoratorMap.get(attribute);
            String colourSettings = decorator.toString();
            settings.put(CONTROLLER_KEY + "." + attribute, colourSettings);
        }
    }

    private Map<String, Decorator> attributeDecoratorMap = new HashMap<String, Decorator>();

}
