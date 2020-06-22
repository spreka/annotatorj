package org.biomag.annotatorProject;

import ij.*;
import ij.plugin.PlugIn;

public class ImportOpSefWrapper implements PlugIn {

	public void run(String arg) {
		// runs the importer plugin
		ImportOpSef.run();
	}
	
}