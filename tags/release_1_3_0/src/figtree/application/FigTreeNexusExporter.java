package figtree.application;

import jebl.evolution.io.NexusExporter;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: FigTreeNexusExporter.java,v 1.2 2006/08/27 15:17:36 rambaut Exp $
 */
public class FigTreeNexusExporter extends NexusExporter {

	public FigTreeNexusExporter(Writer writer, boolean writeMetaComments) {
		super(writer, writeMetaComments);
	}

	/**
	 * Writes a 'TREEDRAW' block.
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
		if (value instanceof Color) {
			return "#" + ((Color)value).getRGB();
		}
		if (value instanceof String) {
			return "\"" + value + "\"";
		}

		return value.toString();
	}
}
