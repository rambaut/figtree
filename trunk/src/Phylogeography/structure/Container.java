package phylogeography.structure;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class Container implements Item {
    protected Container(final String name, final String description, final boolean visible) {
        this.name = name;
        this.description = description;
        isVisible = visible;
    }

    public boolean isContainer() {
        return true;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void addItems(Collection<Item> items) {
        items.addAll(items);
    }

    public List<Item> getItems() {
        return new ArrayList<Item>(items);
    }

    private final List<Item> items = new ArrayList<Item>();
    private final String name;
    private final String description;
    private final boolean isVisible;
}