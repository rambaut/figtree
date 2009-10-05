package figtree.treeviewer.painters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id: AbstractPainter.java 373 2006-07-01 15:18:27Z rambaut $
 */
public abstract class AbstractPainter<T> implements Painter<T> {
    public void addPainterListener(PainterListener listener) {
        listeners.add(listener);
    }

    public void removePainterListener(PainterListener listener) {
        listeners.remove(listener);
    }

    public void firePainterChanged() {
        for (PainterListener listener : listeners) {
            listener.painterChanged();
        }
    }

    public void firePainterSettingsChanged() {
        for (PainterListener listener : listeners) {
            listener.painterSettingsChanged();
        }
    }

    public void fireAttributesChanged() {
        for (PainterListener listener : listeners) {
            listener.attributesChanged();
        }
    }
    
    private final List<PainterListener> listeners = new ArrayList<PainterListener>();
}
