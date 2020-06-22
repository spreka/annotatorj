package org.biomag.annotatorProject;

import java.awt.Color;
import java.util.ArrayList;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

public class TestRoiFcn {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);

        Opener openerStack=new Opener();
        ImagePlus image=openerStack.openImage("s:\\SZBK\\annotator\\annotatorj-opsef__build\\build\\sample_data\\\\toSend_inputonly\\Processed_999\\00_InputRaw\\8bitSum_Test_20200304_080807.tif");
        image.show();

        String roiPath="s:\\SZBK\\annotator\\annotatorj-opsef__build\\build\\sample_data\\\\toSend_inputonly\\Processed_999\\12_TmpRoisFromFiji\\000_CP_Mask_0.4_RGBInput_000_Test_20200304_080807_ROIs.zip";
        //String roiPath="s:\\SZBK\\annotator\\annotatorj-opsef__build\\build\\sample_data\\toSend_inputonly\\Processed_999\\tmp_roifolder\\drawn_classified_748_RoiSet.zip";
        
        RoiManager manager=new RoiManager();
		boolean loadedROIsuccessfully=manager.runCommand("Open",roiPath);
		if (!loadedROIsuccessfully) {
			IJ.log("Failed to open ROI: "+roiPath);
		} else {
			IJ.log("Opened ROI: "+roiPath);
		}


		// collect class info from loaded rois
		ArrayList<String> classFrameNames=new ArrayList<String>();
		ArrayList<Integer> classFrameColours=new ArrayList<Integer>();

		int tmpCount=manager.getCount();
		for (int r=0; r<tmpCount; r++){
			int tmpGroup=manager.getRoi(r).getGroup();
			IJ.log("ROI '"+manager.getRoi(r).getName()+"'\t|\tgroup: "+String.valueOf(tmpGroup));
			String tmpGroupName="Class_"+String.format("%02d",tmpGroup);
			if (tmpGroup>0 && !classFrameNames.contains(tmpGroupName)){
				classFrameNames.add(tmpGroupName);
				//int tmpGroupColourIdx=getClassColourIdxTest(manager.getRoi(r).getStrokeColor());
				Color tmpColour=null;
				//tmpColour=manager.getRoi(r).getFillColor();
				//if (tmpColour==null)
					tmpColour=manager.getRoi(r).getStrokeColor();
				//Color tmpColour2=new Color(tmpColour.getRed(),tmpColour.getGreen(),tmpColour.getBlue());
				int tmpGroupColourIdx=getClassColourIdxTest(tmpColour); //tmpColour2);
				classFrameColours.add(tmpGroupColourIdx);

				//debug:
				IJ.log(">>> import: added class '"+tmpGroupName+"' with colour '"+tmpGroupColourIdx+"'");
			}
		}

		manager.runCommand("Show all");

	}

	// fetch class color idx from the classidxlist
	public static int getClassColourIdxTest(Color curColour){
		int curColourIdx=-1;

		if (curColour.equals(Color.red)){
			curColourIdx=0;
	    } else if (curColour.equals(Color.green)){
	    	curColourIdx=1;
	    } else if (curColour.equals(Color.blue)){
	    	curColourIdx=2;
	    } else if (curColour.equals(Color.cyan)){
	    	curColourIdx=3;
	    } else if (curColour.equals(Color.magenta)){
	    	curColourIdx=4;
	    } else if (curColour.equals(Color.yellow)){
	    	curColourIdx=5;
	    } else if (curColour.equals(Color.orange)){
	    	curColourIdx=6;
	    } else if (curColour.equals(Color.white)){
	    	curColourIdx=7;
	    } else if (curColour.equals(Color.black)){
	    	curColourIdx=8;
	    } else {
	    	IJ.log("Unexpected Color, no matching class colour index");
	    }

	   	return curColourIdx;
	}

}
