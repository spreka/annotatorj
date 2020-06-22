package org.biomag.annotatorProject;

import ij.IJ;
import ij.ImageJ;

public class AppExport {

	public static void main( String[] args )
    {
        ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);
        
        // run the plugin
     	IJ.runPlugIn(Annotator_ExportFrameNew.class.getName(), "");
     	
    }

}
