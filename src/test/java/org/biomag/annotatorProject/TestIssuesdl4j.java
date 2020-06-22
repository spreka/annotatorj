package org.biomag.annotatorProject;


import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
//import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import ij.*;
import ij.io.Opener;

public class TestIssuesdl4j {

    //@Test
    public static void main( String[] args ) throws Exception {

        //File f = new File("d:\\ImageJ\\ImageJ\\plugins\\models\\model_real.hdf5");
        //File f = new File("D:\\SZBK\\tmp\\cytoplasm_unet_out\\cyto_model_next4.hdf5");
        File f = new File("D:\\SZBK\\tmp\\cityscapes_unet\\cityscapes_model2.hdf5");
        ComputationGraph model = KerasModelImport.importKerasModelAndWeights(f.getAbsolutePath(), false);
        
        //String fileName="D:\\tmp\\imagej_plugin_testing\\experimenting\\cyto\\MCF7_HPA054177.png";
        //String fileName="D:\\tmp\\imagej_plugin_testing\\experimenting\\cyto\\resized\\images\\B15-32768_3_1_x40_z0_i33j069_S03.png";
        //String fileName="D:\\SZBK\\tmp\\cityscapes_unet\\train\\resized\\images\\aachen_000003_000019.png";
        String fileName="D:\\\\SZBK\\\\tmp\\\\cityscapes_unet\\\\train\\\\resized\\\\images\\berlin_000001_000019_leftImg8bit.png";
        boolean colourful=true;

        // issue suggested method to load an input image
        /*
        NativeImageLoader nil = new NativeImageLoader(256, 256, 3);
        //File imgF = new File("D:\\tmp\\imagej_plugin_testing\\r01c01f01p01-ch1sk1fk1fl1_crop.png");
        File imgF = new File("D:\\tmp\\imagej_plugin_testing\\experimenting\\cyto\\180309_HT1080-f05p01_S03.png");
        INDArray arr = nil.asMatrix(imgF);
        arr.divi(255);
        
        INDArray out = model.output(arr)[0];
        */
        
        // try with imagej method
        ImageJ ij=IJ.getInstance();
        if(ij==null)
        	ij=new ImageJ(ImageJ.EMBEDDED);
        
        ij.exitWhenQuitting(true);
        
        
        Opener opener2=new Opener();
        opener2.open(fileName);
        ImagePlus imp=WindowManager.getCurrentImage();
        
        int[] dimensions;
		int width=0;
		int height=0;
		dimensions=imp.getDimensions();
		width=dimensions[0];
		height=dimensions[1];
		
		INDArray[] inputs=new INDArray[1];
		
		// image size must be multiplyable by 64 to avoid "illegal concatenation" error in nd4j
					double wx=(double)width/(double)64;
					double hx=(double)height/(double)64;
					int widthx=((int) Math.ceil(wx))*64;
					int heightx=((int) Math.ceil(hx))*64;
					boolean need2pad=false;
					if (widthx!=width || heightx!=height) {
						// pad image to this size
						need2pad=true;
					}
					
					INDArray arr=null;
					
					if (need2pad)
						arr=Nd4j.zeros(1,3,widthx,heightx); // padded row x col
					else
						arr=Nd4j.zeros(1,3,width,height); // row x col
					
					int[] vals=new int[4];
					double curv=0.0;
					// fill image with values fetched from "imp"
					for (int i=0; i<width; i++) {
						for (int j=0; j<height; j++) {
							if (colourful) {
								// RGB image
								vals=imp.getPixel(i,j);
								//vals=copyImp.getPixel(i,j); // crop image processing fails
								for (int ch=0; ch<3; ch++) {
									int[] idxs=new int[]{0,ch,i,j};
									curv=(double)vals[ch];
									arr.putScalar(idxs,curv);
								}
							} else {
								// grayscale image
								vals=imp.getPixel(i,j);
								//vals=copyImp.getPixel(i,j); // crop image processing fails
			       				curv=(double)vals[0];
								for (int ch=0; ch<3; ch++) {
									int[] idxs=new int[]{0,ch,i,j};
									arr.putScalar(idxs,curv);
								}
							}
							
						}
					}
					// image values filled
					if (need2pad) {
						// fill remaining rows and cols with zeros
						curv=0.0;
						for (int i=width; i<widthx; i++) {
							for (int j=height; j<heightx; j++) {
									// RGB image
									// grayscale image
									for (int ch=0; ch<3; ch++) {
										int[] idxs=new int[]{0,ch,i,j};
										arr.putScalar(idxs,curv);
									}
							}
						}

					}
		
					
        //float[][] floatMatrix = imp.getProcessor().getFloatArray();
        //INDArray arr = Nd4j.create(floatMatrix);
        
        
        
		arr.divi(255);
		
        			inputs[0]=arr;
        			
        			
        INDArray out = model.output(inputs)[0];

        
        

        // original issue method to show output
        BufferedImage bi = toBI(out);

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(bi)));
        frame.pack();
        frame.setVisible(true);
        frame.setTitle("DL4J");
        
        
        // imagej method
        int h4 = (int)out.size(2);
        int w4 = (int)out.size(3);
		BufferedImage bi1 = new BufferedImage(h4, w4, BufferedImage.TYPE_BYTE_GRAY);
		int[] ia = new int[1];
        
				        for( int i=0; i<h4; i++ ){
				            for( int j=0; j<w4; j++ ){
				                int value = (int)(255 * out.getDouble(0, 0, i, j));
				                ia[0] = value;
				                bi1.getRaster().setPixel(i,j,ia);
				            }
				        }
				        
        ImagePlus debugimg=new ImagePlus("DL4J",bi1);
        //debugimg.getProcessor().setFloatArray(floatPred);
        //debugimg.getProcessor().setFloatArray(predictedImage.toFloatMatrix());
        debugimg.show();


        // to load a python keras predicted image saved with numpy
        /*
        INDArray kerasPredict = Nd4j.createFromNpyFile(new File("C:/Temp/Issue8298/prediction.npy"));
        kerasPredict = kerasPredict.permute(0, 3, 1, 2);    //NHWC to NCHW
        System.out.println(kerasPredict.shapeInfoToString());
        BufferedImage biKeras = toBI(kerasPredict);
        JFrame frame2 = new JFrame();
        frame2.getContentPane().setLayout(new FlowLayout());
        frame2.getContentPane().add(new JLabel(new ImageIcon(biKeras)));
        frame2.pack();
        frame2.setVisible(true);
        frame2.setTitle("Keras");

        INDArray absDiff = Transforms.abs(out.sub(kerasPredict));
        System.out.println("Min diff: " + absDiff.minNumber());
        System.out.println("Max diff: " + absDiff.maxNumber());
        System.out.println("Avg diff: " + absDiff.meanNumber());
        */



        Thread.sleep(100000);
    }

    private static BufferedImage toBI(INDArray arr){
        int h = (int)arr.size(2);
        int w = (int)arr.size(3);
        BufferedImage bi = new BufferedImage(h, w, BufferedImage.TYPE_BYTE_GRAY);
        int[] ia = new int[1];
        for( int i=0; i<h; i++ ){
            for( int j=0; j<w; j++ ){
                int value = (int)(255 * arr.getDouble(0, 0, i, j));
                ia[0] = value;
                bi.getRaster().setPixel(i,j,ia);
            }
        }

        return bi;
    }
}