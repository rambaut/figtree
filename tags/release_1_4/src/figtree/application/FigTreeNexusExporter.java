/*
 * FigTreeNexusExporter.java
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

package figtree.application;

import jebl.evolution.io.NexusExporter;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: FigTreeNexusExporter.java,v 1.2 2006/08/27 15:17:36 rambaut Exp $
 */
public class FigTreeNexusExporter extends NexusExporter {

	public FigTreeNexusExporter(Writer writer, boolean writeMetaComments) {
		super(writer, writeMetaComments);
	}

	/**
	 * Writes a 'FigTree' block.
	 */
	public void writeFigTreeBlock(Map<String, Object> settings) throws IOException {
		writer.println("\nbegin figtree;");
		for (String key : settings.keySet()) {
			Object value = settings.get(key);
			writer.println("\tset " + key + "=" + createString(value) + ";");
		}
		writer.println("end;\n");
	}

	private String createString(Object value) {
        if (value == null) {
            return "null";
        }
		if (value instanceof Color) {
			return "#" + ((Color)value).getRGB();
		}
		if (value instanceof String) {
			return "\"" + value + "\"";
		}

		return value.toString();
	}
}
