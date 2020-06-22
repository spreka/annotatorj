package org.biomag.annotatorProject;

import org.biomag.annotatorProject.Annotator_MainFrameNew;

import ij.*;


public class TestLocalDev 
{
    public static void main( String[] args )
    {
        ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);
        
        // set the model folder
        String defPluginsPath=IJ.getDirectory("plugins");
		if(defPluginsPath==null) {
			// cannot find default imagej plugin path
			// cannot set plugins path... :(
		}
        
        // run the plugin
     	IJ.runPlugIn(Annotator_MainFrameNew.class.getName(), "");
        //Annotator_MainFrameNew obj=new Annotator_MainFrameNew();
     	
    }
}