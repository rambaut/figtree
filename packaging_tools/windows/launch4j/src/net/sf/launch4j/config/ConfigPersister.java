/*
	Launch4j (http://launch4j.sourceforge.net/)
	Cross-platform Java application wrapper for creating Windows native executables.

	Copyright (c) 2004, 2015 Grzegorz Kowal
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification,
	are permitted provided that the following conditions are met:
	
	1. Redistributions of source code must retain the above copyright notice,
	   this list of conditions and the following disclaimer.
	
	2. Redistributions in binary form must reproduce the above copyright notice,
	   this list of conditions and the following disclaimer in the documentation
	   and/or other materials provided with the distribution.
	
	3. Neither the name of the copyright holder nor the names of its contributors
	   may be used to endorse or promote products derived from this software without
	   specific prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
	THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
	ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
	AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
	OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/*
 * Created on Apr 22, 2005
 */
package net.sf.launch4j.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.*;

import net.sf.launch4j.binding.Validator;

/**
 * @author Copyright (C) 2014 Grzegorz Kowal
 */
public class ConfigPersister {

	private static final ConfigPersister _instance = new ConfigPersister();

	private final XStream _xstream;
	private Config _config;
	private File _configPath;

	private ConfigPersister() {
		_xstream = new XStream(new DomDriver());
		
		_xstream.addPermission(NoTypePermission.NONE);
		_xstream.addPermission(NullPermission.NULL);
		_xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
		_xstream.allowTypeHierarchy(Collection.class);
		_xstream.allowTypesByWildcard(new String[] { "net.sf.launch4j.config.*" });
		
    	_xstream.alias("launch4jConfig", Config.class);
    	_xstream.alias("classPath", ClassPath.class);
    	_xstream.alias("jre", Jre.class);
    	_xstream.alias("splash", Splash.class);
    	_xstream.alias("versionInfo", VersionInfo.class);

    	_xstream.addImplicitCollection(Config.class, "headerObjects", "obj",
    			String.class);
    	_xstream.addImplicitCollection(Config.class, "libs", "lib", String.class);
    	_xstream.addImplicitCollection(Config.class, "variables", "var", String.class);
    	_xstream.addImplicitCollection(ClassPath.class, "paths", "cp", String.class);
    	_xstream.addImplicitCollection(Jre.class, "options", "opt", String.class);
	}

	public static ConfigPersister getInstance() {
		return _instance;
	}
	
	public Config getConfig() {
		return _config;
	}

	public File getConfigPath() {
		return _configPath;
	}
	
	public File getOutputPath() throws IOException {
		if (_config.getOutfile().isAbsolute()) {
			return _config.getOutfile().getParentFile();
		}
		File parent = _config.getOutfile().getParentFile();
		return (parent != null) ? new File(_configPath, parent.getPath()) : _configPath;
	}
	
	public File getOutputFile() throws IOException {
		return _config.getOutfile().isAbsolute()
			? _config.getOutfile()
			: new File(getOutputPath(), _config.getOutfile().getName());
	}

	public void createBlank() {
		_config = new Config();
		_config.setJre(new Jre());
		_configPath = null;
	}

	public void setAntConfig(Config c, File basedir) {
		_config = c;
		_configPath = basedir;
	}

	public void load(File f) throws ConfigPersisterException {
	    try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);

			DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
		    LSSerializer lsSerializer = domImplementation.createLSSerializer();
		    String configString = lsSerializer.writeToString(doc);

	    	_config = convertToCurrent(configString);
	    	setConfigPath(f);
		} catch (Exception e) {
			throw new ConfigPersisterException(e);
		}
	}

	
	public void save(File f) throws ConfigPersisterException {
		try {
			BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
			w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	    	_xstream.toXML(_config, w);
	    	w.close();
	    	setConfigPath(f);
		} catch (Exception e) {
			throw new ConfigPersisterException(e);
		}
	}
	
	/**
	 * Converts 2.x config to current format.
	 */
	private Config convertToCurrent(String configString) {
		boolean requires64Bit = configString.contains("<bundledJre64Bit>true</bundledJre64Bit>")
				|| configString.contains("<runtimeBits>64</runtimeBits>");

    	String updatedConfigString = configString
    			.replaceAll("<headerType>0<", "<headerType>gui<")
    			.replaceAll("<headerType>1<", "<headerType>console<")
    			.replaceAll("jarArgs>", "cmdLine>")
    			.replaceAll("<jarArgs[ ]*/>", "<cmdLine/>")
    			.replaceAll("args>", "opt>")
    			.replaceAll("<args[ ]*/>", "<opt/>")
    			.replaceAll("<jdkPreference>jdkOnly</jdkPreference>", "<requiresJdk>true</requiresJdk>")
    			.replaceAll("<initialHeapSize>0</initialHeapSize>", "")
    			.replaceAll("<maxHeapSize>0</maxHeapSize>", "")
    			.replaceAll("<customProcName>.*</customProcName>", "")
    			.replaceAll("<bundledJre64Bit>.*</bundledJre64Bit>", "")
    			.replaceAll("<bundledJreAsFallback>.*</bundledJreAsFallback>", "")
    			.replaceAll("<jdkPreference>.*</jdkPreference>", "")
    			.replaceAll("<runtimeBits>.*</runtimeBits>", "");
    	
    	Config config = (Config) _xstream.fromXML(updatedConfigString);
    
    	if (Validator.isEmpty(config.getJre().getPath())) {
    		config.getJre().setPath(Jre.DEFAULT_PATH);
    	}
    	
    	if (requires64Bit) {
    		config.getJre().setRequires64Bit(true);
    	}
    	
    	return config;
	}

	private void setConfigPath(File configFile) {
		_configPath = configFile.getAbsoluteFile().getParentFile();
	}
}
