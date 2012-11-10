/*
 * FigTreeNexusImporter.java
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

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id: FigTreeNexusImporter.java,v 1.2 2006/08/27 15:17:36 rambaut Exp $
 */
public class FigTreeNexusImporter extends NexusImporter {
	public FigTreeNexusImporter(Reader reader) {
		super(reader);
	}

	/**
	 * Parses a 'FigTree' block.
	 */
	public void parseFigTreeBlock(Map<String, Object> settings) throws ImportException, IOException
	{
		readFigTreeBlock(settings);
	}

	/**
	 * Reads a 'FigTree' block.
	 */
	private void readFigTreeBlock(Map<String, Object> settings) throws ImportException, IOException
	{

		String command = helper.readToken(";");
		while (!command.equalsIgnoreCase("END")) {

			if (command.equalsIgnoreCase("SET")) {
				while (helper.getLastDelimiter() != ';') {
					String key = helper.readToken("=;");

					if (helper.getLastDelimiter() != '=') {
						throw new ImportException("Subcommand, " + key + ", is missing a value in command, " + command + ", in FIGTREE block");
					}

					String value = helper.readToken(";");

					settings.put(key, parseValue(value));
				}
			} else {
				throw new ImportException("Unknown command, " + command + ", in FIGTREE block");
			}

			command = helper.readToken(";");
		}

		findEndBlock();
	}


	private Object parseValue(String value) {
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return new Boolean(value);
		}

		if (value.startsWith("#")) {
			try {
				return Color.decode(value.substring(1));
			} catch (NumberFormatException nfe) {
			}
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
		}

		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe) {
		}

		// Simply return it as a string...
		return value;
	}
}
