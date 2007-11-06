package figtree.treeviewer.annotations;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: AnnotationDefinition.java,v 1.3 2006/08/28 13:19:41 rambaut Exp $
 */
public class AnnotationDefinition {
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	private String name;
	private Type type;
}
