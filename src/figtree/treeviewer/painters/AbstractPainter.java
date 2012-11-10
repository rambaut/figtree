/*
 * AbstractPainter.java
 *
 * Copyright (C) 2012 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
