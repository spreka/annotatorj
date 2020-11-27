package org.biomag.annotatorProject;

import ij.*;
import ij.gui.MessageDialog;
import java.io.File;


public class App 
{
    public static void main( String[] args ) throws Exception
    {
        try{
            ImageJ ij=IJ.getInstance();

            // check if plugins directory is set correctly
            String defPluginsPath=IJ.getDirectory("plugins");
            if (defPluginsPath==null || defPluginsPath.equals("null")) {
                String curOS=System.getProperty("os.name");
                if (curOS.equals("Linux")) {
                    // cannot find plugins dir on ubuntu for some reason
                    String jarPath=new File(Annotator_MainFrameNew.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                    String fileSep="/";
                    int fileSepPos=jarPath.lastIndexOf(fileSep);
                    if(fileSepPos<0){
                        fileSep="\\";
                        fileSepPos=jarPath.lastIndexOf(fileSep);
                        if (fileSepPos<0) {
                            // this should never happen
                            IJ.log("---- Failed to find currently executing jar's path.");
                            //MessageDialog failedFolderOpenMsg=new MessageDialog(ij,
                            //     "Msg",
                            //     "Failed to find currently executing jar's path.");
                            // TODO: what to do?
                            fileSepPos=jarPath.length();
                        }
                    }
                    jarPath=jarPath.substring(0,fileSepPos);
                    IJ.log("---- Currently executing jar's path: "+jarPath);
                    //MessageDialog failedFolderOpenMsg=new MessageDialog(ij,
                    //             "Msg",
                    //             jarPath);
                    System.getProperties().setProperty("plugins.dir", jarPath);
                    if (ij!=null) {
                        // an instance of imageJ is running
                        ij.dispose();
                        return;
                    }
                }
            }
            
            // orig way
            if(ij==null)
            	ij=new ImageJ(ImageJ.EMBEDDED);
            
            ij.exitWhenQuitting(true);
            
            // run the plugin
         	IJ.runPlugIn(Annotator_MainFrameNew.class.getName(), "");
            //Annotator_MainFrameNew obj=new Annotator_MainFrameNew();

     	} catch(Exception e){
            IJ.log("AnnotatorJ init was interrupted");
            e.printStackTrace();
            IJ.log("  >> Exception: "+e.getMessage());
        }
    }
}
