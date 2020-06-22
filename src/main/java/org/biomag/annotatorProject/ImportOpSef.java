package org.biomag.annotatorProject;

import ij.*;
import ij.process.*;
import ij.plugin.frame.*;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.filter.ThresholdToSelection;
import java.io.*;
import java.util.ArrayList;
import java.lang.Math;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.lang.System;

public class ImportOpSef{

	private String importPath;
	private String importFileName;
	private String rootOpSefDataPath;
	private Annotator_MainFrameNew annotatorJinstance;
	private String opsefPath;
	public boolean initialized;
	private String[] imageList;
	private String[] maskList;
	private ArrayList<String[]> classMaskLists;
	private ImageJ ijInstance;

	public ImportOpSef() throws FileNotFoundException{

		opsefPath=null;
		initialized=false;

		// get default imageJ folder
		importPath=IJ.getDirectory("imagej");
		if (importPath==null || importPath.equals("null") || importPath.equals("null"+File.separator))
			importPath=System.getProperty("user.dir");

		// concat path from the base AnnotatorJ folder + OpSef data folder
		String concatPath=importPath+File.separator+"sample_data";
		IJ.log("OpSef data folder: "+concatPath);
		rootOpSefDataPath=concatPath;

		// file open dialog
		OpenDialog opener=null;
		
		opener=new OpenDialog("Select the FilePairList text file",concatPath,"FilePairList_yourdataset.txt");

		// check if cancel was pressed:
		String validPath=opener.getPath();
		File tmpValidFile=new File(validPath);
		if (validPath==null || !(tmpValidFile.exists() && !tmpValidFile.isDirectory())) {
			// path is null if the dialog was canceled
			IJ.log("canceled file open");
			return;
		}
		importPath=opener.getDirectory();
		importFileName=opener.getFileName();

		// set the root opsef path here by removing the last folder name "10_ForFiji"
		String thisFileSep="/";
		int lastIdx=importPath.lastIndexOf(thisFileSep);
		if (lastIdx<0) {
			thisFileSep="\\";
			lastIdx=importPath.lastIndexOf(thisFileSep);
		}
		if (lastIdx<0) {
			System.out.println("Cannot find file separator character in the file path\n");
			//return;
		}
		
		if (importPath.substring(importPath.length()-1).equals(thisFileSep))
			lastIdx=importPath.length()-1;
		else
			lastIdx=importPath.length();
		
		rootOpSefDataPath=importPath.substring(0,importPath.substring(0,lastIdx).lastIndexOf(thisFileSep));

		
		// read the file
		try {
			File listFile = new File(importPath,importFileName);

			// first find the number of lines in the files to init the fileList arrays
			Scanner scanner = new Scanner(listFile);
			int lineNum=0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lineNum+=1;
			}
			imageList=new String[lineNum]; //[lineNum-1];
			maskList=new String[lineNum]; //[lineNum-1];
			classMaskLists=null;

			// then read the lines
			scanner = new Scanner(listFile);
			int lineNum2=0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.startsWith("root;")){
					// 1st line with root folder
					opsefPath=line.substring(5);
					// debug:
					IJ.log("root OpSef path: "+opsefPath);
					File tmpOpsefPath = new File(opsefPath);
					if (!tmpOpsefPath.exists()){
						IJ.log("Cannot find the root OpSef path");
						return;
					} else {
						// path exists, can continue
						IJ.log("OpSef path: "+opsefPath);
					}
				} else {
					String[] curPaths=line.split(";");
					// debug:
					//for (int s=0;s<curPaths.length;s++)
					//	IJ.log("line "+String.valueOf(s)+": "+curPaths[s]);
					imageList[lineNum2]=curPaths[0];
					maskList[lineNum2]=curPaths[1];

					// check if multiple file names can be extracted
					if (curPaths.length>2){
						if (lineNum2==0) {
							classMaskLists=new ArrayList<String[]>();
							for (int s=2; s<curPaths.length; s++){
								String[] maskListExtra=new String[lineNum];
								classMaskLists.add(maskListExtra);
							}
						}
						for (int s=2; s<curPaths.length; s++){
							String[] maskListExtra=classMaskLists.get(s-2);
							IJ.log("extra file name in mask list: "+curPaths[s]);
							maskListExtra[lineNum2]=curPaths[s];
							classMaskLists.set(s-2,maskListExtra);
						}
					}

					lineNum2+=1;
				}
				// debug:
				IJ.log(line);
			}
			scanner.close();
	    } catch (FileNotFoundException e) {
			IJ.log("Cannot find the FilePairList file");
			IJ.log(e.toString());
	    } catch (Exception e){
	    	IJ.log("An exception occurred");
	    	IJ.log(e.toString());
	    	e.printStackTrace();
	    }
	    initialized=true;
		IJ.log("Opened file: "+importFileName+" in folder "+importPath);
	}

	public void runNewAJ(){

		if (!initialized) {
			return;
		}
        
        // run the plugin
     	//this.annotatorJinstance=(Annotator_MainFrameNew) IJ.runPlugIn(Annotator_MainFrameNew.class.getName(), "");

     	// set init params
     	Map<String,Object> initParams=new HashMap<String,Object>();
     	initParams.put("editMode",true);
     	initParams.put("defaultAnnotType","instance");
     	initParams.put("rememberAnnotType",true);
     	initParams.put("exportFolderFromArgs","11_SegMasksFromFiji");
     	initParams.put("exportClassFolderFromArgs","13_ClassifiedSegMasksFromFiji");

     	// concat export root folder path:
     	if (imageList.length>0){

     		initParams.put("exportRootFolderFromArgs",rootOpSefDataPath);

     	}

     	// check temp save folder to see if rois have been saved previously
     	String tmpFolder=rootOpSefDataPath+File.separator+"12_TmpRoisFromFiji";
     	boolean tmpExists=false;
     	// list files in folder
     	// get a list of files in the current directory
		File folder2 = new File(tmpFolder);
		int ROIListCount=0;
		if (folder2.exists() && folder2.isDirectory()){
			File[] listOfROIs = folder2.listFiles();

			// get number of useful files
			// see which object type is selected
			String annotNameReg="_ROIs";
			String annotExt=".zip";
			for (int i = 0; i < listOfROIs.length; i++) {
				// new, for any type of object we support
				if (listOfROIs[i].isFile()) {
					String curFileName=listOfROIs[i].getName();
					if (curFileName.endsWith(annotExt) && curFileName.contains(annotNameReg)) {
						ROIListCount+=1;
					}
				}
			}
		}

		// check if there are correct files in the selected folder
		if (ROIListCount<1) {
			IJ.log("No annotation files found in current temp folder "+tmpFolder);
		} else {
			tmpExists=true;
			IJ.log("Found "+String.valueOf(ROIListCount)+" annotation files found in current temp folder "+tmpFolder);
		}
		// find file separator in the mask file name string array elements
		String thisFileSep="/";
		int lastIdx=maskList[0].lastIndexOf(thisFileSep);
		if (lastIdx<0) {
			thisFileSep="\\";
			lastIdx=maskList[0].lastIndexOf(thisFileSep);
		}
		if (lastIdx<0) {
			IJ.log("Cannot find file separator character in the file path\n");
			return;
		}



     	if (imageList.length!=maskList.length){
     		IJ.log(" >>>> #images and #masks do not match!");
     		// TODO: can we proceed?
     	}

     	// init ROI managers for them
     	int stackNum=imageList.length;
     	RoiManager manager=null;
     	ArrayList<RoiManager> managers=new ArrayList<RoiManager>();

     	// construct stack from single images in the folder
     	ImagePlus imParam=new ImagePlus();
     	ImageStack imStack=new ImageStack();
     	Opener openerStack=new Opener();
     	for (int i=0; i<imageList.length; i++){
     		//debug:
     		IJ.log(rootOpSefDataPath+File.separator+imageList[i]);
     		imStack.addSlice(openerStack.openImage(rootOpSefDataPath+File.separator+imageList[i]).getProcessor());

     		// also open mask for this image + create ROIs from it

     		// check if temp annotation file exists --> load it if so
     		String curAnnotChunk=maskList[i].substring(maskList[i].lastIndexOf(thisFileSep)+1,maskList[i].lastIndexOf("."))+"_ROIs.zip";
     		String tmpAnnotFileName=tmpFolder+File.separator+curAnnotChunk;
     		File tmpAnnotFile=new File(tmpAnnotFileName);
     		if (tmpAnnotFile.exists() && !tmpAnnotFile.isDirectory()){
     			// load it
     			manager=new RoiManager(false);
     			boolean loadedROIsuccessfully=manager.runCommand("Open",tmpAnnotFileName);
				if (!loadedROIsuccessfully) {
					IJ.log("Failed to open ROI: "+curAnnotChunk);
					// load the rois from the mask below
				} else {
					IJ.log("Opened ROI: "+curAnnotChunk);
					managers.add(manager);
					continue;
				}
				
     		}


     		// this is the "else" branch:
     		// fetch all possible class mask file names
     		ArrayList<String> curMaskList=new ArrayList<String>();
     		ArrayList<Integer> curMaskClasses=null;
     		if (classMaskLists==null){
     			curMaskList.add(maskList[i]);
     		} else {
     			curMaskClasses=new ArrayList<Integer>();
     			curMaskList.add(maskList[i]);
     			int maskNamePos=maskList[i].lastIndexOf("_MaskClass");
     			int classNum=Integer.parseInt(maskList[i].substring(maskNamePos+10,maskNamePos+12));
     			curMaskClasses.add(classNum);
     			for (int s=0; s<classMaskLists.size(); s++){
     				String curMaskName=classMaskLists.get(s)[i];
					curMaskList.add(curMaskName);
					maskNamePos=curMaskName.lastIndexOf("_MaskClass");
	     			classNum=Integer.parseInt(curMaskName.substring(maskNamePos+10,maskNamePos+12));
	     			curMaskClasses.add(classNum);
				}
     		}

     		// loop through all masks to import
     		manager=new RoiManager(false);
     		
     		for (int x=0; x<curMaskList.size();x++){
     			// orig:
	     		//ImagePlus mask=openerStack.openImage(rootOpSefDataPath+File.separator+maskList[i]);
	     		ImagePlus mask=openerStack.openImage(rootOpSefDataPath+File.separator+curMaskList.get(x));

	     		//debug:
	     		//mask.show();

	     		int[] maskdimensions=mask.getDimensions();
				int maskwidth=maskdimensions[0];
				int maskheight=maskdimensions[1];

				// create a ShortProcessor 16-bit image from the floating point 32-bit original mask
				ShortProcessor shortMaskProc=create16bitMask(mask.getProcessor());
				mask.setProcessor(shortMaskProc);
				//mask.setProcessor(mask.getProcessor().convertToShortProcessor());

	     		// get max value to see the number of labels
	     		double maxValue=mask.getProcessor().getMax();
	     		int[] maskHistogram=mask.getProcessor().getHistogram();

	     		//manager=new RoiManager(false);


	     		// skip 0-values in histogram (background)
	     		for (int k=1; k<maskHistogram.length;k++){
	     			if (maskHistogram[k]!=0){
	     				//debug:
	     				//IJ.log("k="+String.valueOf(k)+"\t: "+String.valueOf(maskHistogram[k]));
						boolean foundValues=false;

	     				// nextgen way ------------------------------------------
	     				mask.show();
						// threshold the mask to get an ROI
						ImageProcessor curMask = mask.getProcessor();
						curMask.setThreshold(k,k,ImageProcessor.NO_LUT_UPDATE);
						Roi curROI = new ThresholdToSelection().convert(curMask);
						//mask.draw();
						mask.setRoi(curROI);
						// nextgen way end --------------------------------------

				        // prefix 0-s to the name
						String curROIname=String.format("%04d",k); // this should be k
						if (curROI==null){
							IJ.log(" >>>> failed to create ROI from mask #"+String.valueOf(k));
						}
						curROI.setName(curROIname);

						// check class
						if (curMaskClasses!=null && curMaskClasses.get(x)>0){
							int curClassNum=curMaskClasses.get(x);
							curROI.setGroup(curClassNum);
							//curROI.setFillColor(Annotator_MainFrameNew.getClassColour(curClassNum));
							curROI.setStrokeColor(Annotator_MainFrameNew.getClassColour(curClassNum));
						}

						manager.runCommand("Add");

	     			}
	     		}

	     		mask.changes=false;
				mask.getWindow().close();

			}

     		managers.add(manager);


     	}
     	imParam.setStack(imStack);

     	initParams.put("x_inputImage2open",imParam);
     	initParams.put("z_inputROIs2open",managers);
     	// also store the original mask names for reference in the saving fcn
     	initParams.put("z_origMaskFileNames",maskList);
     	initParams.put("z_origImageFileNames",imageList);
     	this.annotatorJinstance=new Annotator_MainFrameNew(initParams);
     	//this.annotatorJinstance.parseArgs(initParams);
	}


	public ShortProcessor create16bitMask(ImageProcessor orig){

		int width=orig.getWidth();
		int height=orig.getHeight();
		ShortProcessor res=new ShortProcessor(width,height);

		for (int i=0; i<width; i++){
			for (int j=0; j<height; j++) {
				res.putPixel(i,j,Math.round(orig.getPixelValue(i,j)));
				//debug:
				//IJ.log("\t"+String.valueOf(Math.round(orig.getPixelValue(i,j)))+"\t-->\t"+res.getPixel(i,j));
			}
		}

		return res;
	}

	//public static void main( String[] args ){
	public static void run() {
		ImportOpSef importOpSefObj;
		try {
			importOpSefObj = new ImportOpSef();
			if (importOpSefObj.initialized) {
				importOpSefObj.ijInstance=IJ.getInstance();
		        if(importOpSefObj.ijInstance==null)
		        	importOpSefObj.ijInstance=new ImageJ(ImageJ.EMBEDDED);
		        
		        importOpSefObj.ijInstance.exitWhenQuitting(true);
				importOpSefObj.runNewAJ();
			} else {
				IJ.log("Failed to initialize OpSef paths, please restart.");
				return;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}