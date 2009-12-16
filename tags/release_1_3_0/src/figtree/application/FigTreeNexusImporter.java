package figtree.application;

import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: FigTreeNexusImporter.java,v 1.2 2006/08/27 15:17:36 rambaut Exp $
 */
public class FigTreeNexusImporter extends NexusImporter {
	public FigTreeNexusImporter(Reader reader) {
		super(reader);
	}

	/**
	 * Parses a 'TREEDRAW' block.
	 */
	public void parseFigTreeBlock(Map<String, Object> settings) throws ImportException, IOException
	{
		readFigTreeBlock(settings);
	}

	/**
	 * Reads a 'TREEDRAW' block.
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
