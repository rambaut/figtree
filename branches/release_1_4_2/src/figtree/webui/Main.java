package figtree.webui;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class Main extends WtServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7955893733032239397L;

	@Override
	public WApplication createApplication(WEnvironment env) {
		return new FigTreeWebApplication(env);
	}

}
