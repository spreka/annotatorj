package org.biomag.annotatorProject;

import ij.*;


public class AppImportOpSef 
{
    public static void main( String[] args )
    {
        ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);
        
        // run the plugin
     	IJ.runPlugIn(ImportOpSefWrapper.class.getName(), "");
     	
    }
}
