/*
 * AnnotationDefinition.java
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

package figtree.treeviewer.annotations;

import jebl.evolution.graphs.Node;
import jebl.util.Attributable;

import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id: AnnotationDefinition.java,v 1.3 2006/08/28 13:19:41 rambaut Exp $
 */
public class AnnotationDefinition implements Comparable<AnnotationDefinition> {

    public enum Type {
        INTEGER("Integer"),
        REAL("Real"),
        STRING("String"),
        BOOLEAN("Boolean"),
        RANGE("Range");

        Type(String name) { this.name = name; }
        public String toString() { return name; }

        private String name;
    };

    public AnnotationDefinition(String name, Type type) {
        this.name = name;
        this.code = null;
        this.type = type;
    }

    public AnnotationDefinition(String name, String code, Type type) {
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        if (code == null) {
            return name;
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public static Type guessType(final String name, final Set<Attributable> items) {
        boolean isInteger = true;
        boolean isNumber = true;
        boolean isBoolean = true;
        for (Attributable item: items) {
            Object value = item.getAttribute(name);
            if (value != null) {
                if (value instanceof Number) {
                    isBoolean = false;
                    if (value instanceof Double || value instanceof Float) {
                        isInteger = false;
                    }
                } else {
                    isInteger = false;
                    isNumber = false;
                    if (!(value instanceof Boolean)) {
                        isBoolean = false;
                    }
                }
            }
        }
        if (isInteger) {
            return Type.INTEGER;
        } else if (isNumber) {
            return Type.REAL;
        } else if (isBoolean) {
            return Type.BOOLEAN;
        }

        return Type.STRING;
    }

    public int compareTo(AnnotationDefinition o) {
        return toString().compareTo(o.toString());
    }

    private String name;
    private String code;

    private Type type;
}
