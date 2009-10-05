package figtree.treeviewer.painters;

/**
 * @author Andrew Rambaut
 * @version $Id: PainterListener.java 308 2006-05-01 21:15:41Z rambaut $
 */
public interface PainterListener {

    void painterChanged();

    void painterSettingsChanged();

    void attributesChanged();
}
