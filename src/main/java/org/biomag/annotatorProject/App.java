package org.biomag.annotatorProject;

import ij.*;


public class App 
{
    public static void main( String[] args )
    {
        ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);
        
        // run the plugin
     	IJ.runPlugIn(Annotator_MainFrameNew.class.getName(), "");
        //Annotator_MainFrameNew obj=new Annotator_MainFrameNew();
     	
    }
}
