package org.biomag.annotatorProject;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ImageProcessor;

public class TestImageFcn {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);

        Opener openerStack=new Opener();
        ImagePlus mask=openerStack.openImage("s:\\SZBK\\annotator\\annotatorj-opsef\\sample_data\\Processed_999\\02_SegMasks\\000_CP_Mask_0.4_RGBInput_000_Test_20200304_080730.tif");
        mask.show();
		
		// threshold the mask to get an ROI
		ImageProcessor imageProcessor = mask.getProcessor();
		imageProcessor.setThreshold(9,9,ImageProcessor.NO_LUT_UPDATE);
		Roi roi = new ThresholdToSelection().convert(imageProcessor);
		mask.draw();
		mask.setRoi(roi);

	}

}
