package org.biomag.annotatorProject;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.frame.*;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.macro.Functions;
import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ij.plugin.Hotkeys;
import ij.plugin.Selection;
import ij.plugin.Thresholder;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.OverlayCommands;
import ij.plugin.Converter;
import ij.ImageStack;
import ij.measure.ResultsTable;

import java.awt.event.*;
import java.io.*;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.Color;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.nio.file.Files;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JDialog;


public class Annotator_ExportFrameNew extends PlugInFrame implements ActionListener,ItemListener{ //,KeyListener

	private Panel panel;
	private int previousID;
	private static Frame instance;

	// export elements
	private JButton btnBrowseOrig;
	private JButton btnBrowseROI;
	private JTextField textFieldOrig;
	private JTextField textFieldROI;
	private JLabel lblNewLabel;
	private JCheckBox chckbxMultiLabel;
	private JCheckBox chckbxMultiLayer;
	private JCheckBox chckbxSemantic;
	private JCheckBox chckbxCoordinates;
	private JCheckBox chckbxOverlay;
	private JButton btnExportMasks;
	private JButton btnCancel;

	// object type elements
	private JRadioButton rdbtnRoi;
	private JRadioButton rdbtnSemantic;
	private JRadioButton rdbtnBoundingBox;
	private ButtonGroup group;
	private JLabel lblObjectToExport;

	private JFrame progressFrame;
	private Panel progressPanel;
	private JLabel lblExportingImages;
	private JLabel lblCurrentImage;
	private JProgressBar progressBar;
	private JButton btnOk;
	private JButton btnCancelProgress;
	//final JDialog dlg;
	private JDialog dlg;

	// vars
	private boolean multiLabel;
	private boolean multiLayer;
	private boolean semantic;
	private boolean coordinates;
	private boolean overlay;
	private boolean exportDone;

	private String originalFolder;
	private String annotationFolder;


	private String[] curROIList;

	private boolean startedOrig;
	private boolean startedROI;

	private boolean finished;



	// --------------------------

	//
	private JButton btnOpen;
	private JButton btnLoad;
	private JButton btnSave;
	private JButton btnOverlay;
	private JButton btnColours;
	private GroupLayout gl_panel;
	private Panel panel_1;
	private JLabel lblRois;
	private JCheckBox chckbxAddAutomatically;
	private JCheckBox chckbxSmooth;
	private JCheckBox chckbxShowAll;
	private JCheckBox chckbxShowLabels;
	private JCheckBox chckbxStepThroughContours;
	private JCheckBox chckbxShowOverlay;
	private GroupLayout gl_panel_1;
	private JLabel lblCurrentFile;	
	private	JButton buttonPrev;	
	private	JButton buttonNext;


	// processing vars
	private boolean addAuto;
	private boolean smooth;
	private boolean showCnt;
	private boolean showLbs;
	private boolean showOvl;
	private boolean stepCnt;

	private String destNameRaw;
	private String destName;
	private String destFolder;
	private String selectedAnnotationType;
	private RoiManager manager;
	private boolean started;
	private boolean loadedROI;
	private boolean overlayedROI;
	private String selectedClass;
	private Window imWindow;

	//private FloatPolygon curROI;
	private Roi curROI;
	private ImagePlus imp;

	private boolean imageIsActive;
	private String prevMouseEvent;

	private RoiManager overlayManager;
	private boolean overlayAdded;
	private Color defOverlay;
	private Color currentSelectionColor;
	private String selectedAnnotationColour;
	private String selectedOverlayColour;
	private OverlayCommands overlayCommandsObj;

	private String defAnnotCol;
	private String defOvlCol;

	private Toolbar curToolbar;

	private String defDir;
	private String defFile;
	private String defImageJDir;
	private String[] curFileList;
	private int curFileIdx;
	private boolean stepping;

	private boolean imageNameLabelIsActive;

	private String selectedObjectType;

	private long startTime;
	private long pendingTime;


	// annot time saving:
	ResultsTable annotTimes;
	int annotCount;
	long lastStartTime;


	public Annotator_ExportFrameNew() {
		super("Annotator_ExportFrameNew");

		if (instance!=null) {
			instance.toFront();
			return;
		}
		instance = this;
		addKeyListener(IJ.getInstance());

		instance.setTitle("AnnotatorJExporter");

		// create panel for every component
		setLayout(new FlowLayout());
		panel = new Panel();
		panel.setBackground(SystemColor.control);



		// browse buttons
		JButton btnBrowseOrig = new JButton("Browse original ...");
		btnBrowseOrig.addActionListener(this);
		btnBrowseOrig.addKeyListener(IJ.getInstance());
		btnBrowseOrig.setToolTipText("Browse folder of original images");
		add(btnBrowseOrig);
		
		textFieldOrig = new JTextField();
		textFieldOrig.setToolTipText("original images folder");
		textFieldOrig.setColumns(10);
		//textFieldOrig.addActionListener(this);
		textFieldOrig.addKeyListener(IJ.getInstance());
		textFieldOrig.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        textFieldActionPerformed(e,textFieldOrig);
	      }
	    });
		add(textFieldOrig);
		
		JButton btnBrowseROI = new JButton("Browse annot ...");
		btnBrowseROI.addActionListener(this);
		btnBrowseROI.addKeyListener(IJ.getInstance());
		btnBrowseROI.setToolTipText("Browse folder of annotation zip files");
		add(btnBrowseROI);
		
		textFieldROI = new JTextField();
		textFieldROI.setToolTipText("annotation zips folder");
		textFieldROI.setColumns(10);
		//textFieldROI.addActionListener(this);
		textFieldROI.addKeyListener(IJ.getInstance());
		textFieldROI.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        textFieldActionPerformed(e,textFieldROI);
	      }
	    });
		add(textFieldROI);


		// export options
		lblNewLabel = new JLabel("Export options");
		add(lblNewLabel);

		chckbxMultiLabel = new JCheckBox("Multi-label (instances)",true); // this will be the default
		chckbxMultiLabel.setToolTipText("Single 1-channel image with increasing labels for instances");
		chckbxMultiLabel.addItemListener(this);
		add(chckbxMultiLabel);
		
		chckbxMultiLayer = new JCheckBox("Multi-layer (stack)");
		chckbxMultiLayer.setToolTipText("Single image (stack) with separate layers for instances");
		chckbxMultiLayer.addItemListener(this);
		add(chckbxMultiLayer);
		
		chckbxSemantic = new JCheckBox("Semantic (binary)");
		chckbxSemantic.setToolTipText("Single binary image (foreground-background)");
		chckbxSemantic.addItemListener(this);
		add(chckbxSemantic);

		chckbxCoordinates = new JCheckBox("Coordinates");
		chckbxCoordinates.setToolTipText("Bounding box coordinates");
		chckbxCoordinates.addItemListener(this);
		add(chckbxCoordinates);

		JCheckBox chckbxOverlay = new JCheckBox("Overlay");
		chckbxOverlay.setToolTipText("Annotations overlayed as outlines on the original image");
		chckbxOverlay.addItemListener(this);
		add(chckbxOverlay);


		// object type selector radio buttons
		rdbtnRoi = new JRadioButton("ROI");
		rdbtnRoi.setSelected(true);
		rdbtnRoi.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent event) {
		        int state = event.getStateChange();
		        if (state == ItemEvent.SELECTED) {
		        	// ROI object type selected, default
		        	// set var for object
		        	selectedObjectType="ROI";
		        	IJ.log("Selected annotation object type: "+selectedObjectType);

		        	// enable all checkboxes
		        	chckbxMultiLabel.setEnabled(true);
		        	chckbxMultiLayer.setEnabled(true);
		        	chckbxSemantic.setEnabled(true);
		        	chckbxCoordinates.setEnabled(true);
		        	chckbxOverlay.setEnabled(true);

		        	initializeOrigFolderOpening(textFieldOrig.getText());
		        	initializeROIFolderOpening(textFieldROI.getText());
		 
		        } else if (state == ItemEvent.DESELECTED) {
		 			// handle in next button's selected option
		        }
		    }
		});
		add(rdbtnRoi);
		
		rdbtnSemantic = new JRadioButton("semantic");
		//rdbtnSemantic.addItemListener(this);
		rdbtnSemantic.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent event) {
		        int state = event.getStateChange();
		        if (state == ItemEvent.SELECTED) {
		        	// semantic selected, set everything to semantic instead
		        	// set var for object
		        	selectedObjectType="semantic";
		        	IJ.log("Selected annotation object type: "+selectedObjectType);

		        	// enable all checkboxes
		        	chckbxMultiLabel.setEnabled(true);
		        	chckbxMultiLayer.setEnabled(true);
		        	chckbxSemantic.setEnabled(true);
		        	chckbxCoordinates.setEnabled(true);
		        	chckbxOverlay.setEnabled(true);

		        	initializeOrigFolderOpening(textFieldOrig.getText());
		        	initializeROIFolderOpening(textFieldROI.getText());

		        	MessageDialog semanticObjtTypeMsg=new MessageDialog(instance,
	                 "Warning",
	                 "Semantic annotation type selected.\nExported images (instance, stack) might\ncontain multiple touching objects as one!");
		 
		        } else if (state == ItemEvent.DESELECTED) {
		 			// handle in next button's selected option
		        }
		    }
		});
		add(rdbtnSemantic);

		rdbtnBoundingBox = new JRadioButton("bounding box");
		rdbtnBoundingBox.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent event) {
		        int state = event.getStateChange();
		        if (state == ItemEvent.SELECTED) {
		        	// bbox selected, set everything to bbox instead
		        	// set var for object
		        	selectedObjectType="bbox";
		        	IJ.log("Selected annotation object type: "+selectedObjectType);

		        	// enable only coordinates checkbox
		        	chckbxMultiLabel.setEnabled(false);
		        	chckbxMultiLayer.setEnabled(false);
		        	chckbxSemantic.setEnabled(false);
		        	chckbxCoordinates.setEnabled(true);
		        	chckbxOverlay.setEnabled(true);
		        	// also reset the others to false
		        	chckbxMultiLabel.setSelected(false);
		        	chckbxMultiLayer.setSelected(false);
		        	chckbxSemantic.setSelected(false);

		        	initializeOrigFolderOpening(textFieldOrig.getText());
		        	initializeROIFolderOpening(textFieldROI.getText());
		 
		        } else if (state == ItemEvent.DESELECTED) {
		 			// handle in next button's selected option
		        }
		    }
		});
		add(rdbtnBoundingBox);
		
		//Group the radio buttons.
	    group = new ButtonGroup();
	    group.add(rdbtnRoi);
	    group.add(rdbtnSemantic);
	    group.add(rdbtnBoundingBox);
		
		lblObjectToExport = new JLabel("Object to export");
		add(lblObjectToExport);


		// export and cancel buttons
		btnExportMasks = new JButton("Export masks");
		btnExportMasks.addActionListener(this);
		btnExportMasks.addKeyListener(IJ.getInstance());
		btnExportMasks.setToolTipText("Start exporting mask images");
		add(btnExportMasks);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		btnCancel.addKeyListener(IJ.getInstance());
		add(btnCancel);


		// create grouplayout structure of elements
		gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addComponent(textFieldROI, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
								.addComponent(textFieldOrig, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
								.addComponent(btnCancel))
							.addPreferredGap(ComponentPlacement.RELATED))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(4)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(chckbxMultiLayer)
								.addComponent(chckbxMultiLabel)
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(chckbxSemantic)
										.addGap(164))
									.addComponent(chckbxCoordinates, Alignment.LEADING)
									.addComponent(chckbxOverlay, Alignment.LEADING))
								.addComponent(lblNewLabel))
							.addGap(16)))
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
							.addComponent(btnBrowseROI, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnBrowseOrig, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(rdbtnRoi)
							.addComponent(rdbtnSemantic))
						.addGroup(gl_panel.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnExportMasks))
						.addComponent(rdbtnBoundingBox)
						.addGroup(gl_panel.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblObjectToExport)))
					.addGap(12))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(112)
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNewLabel)
								.addComponent(lblObjectToExport)))
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(26)
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(textFieldOrig, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnBrowseOrig))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnBrowseROI)
								.addComponent(textFieldROI, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxMultiLabel)
						.addComponent(rdbtnRoi))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxMultiLayer)
						.addComponent(rdbtnSemantic))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxSemantic)
						.addComponent(rdbtnBoundingBox))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxCoordinates)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxOverlay)
					.addGap(4)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnExportMasks)
						.addComponent(btnCancel))
					.addContainerGap())
		);
		panel.setLayout(gl_panel);

		// add the panel after everything else is added
		
		add(panel);



		
		pack();
		GUI.center(this);
		setVisible(true);
		//show();

		// set default values

		// this will mark if annotation was started by opening an image
		started=false;
		manager=null;


		multiLabel=true;
		multiLayer=false;
		semantic=false;
		coordinates=false;
		overlay=false;

		exportDone=false;

		originalFolder=null;
		annotationFolder=null;

		curROIList=null;

		startedOrig=false;
		startedROI=false;

		finished=true;

		// --------------------

		overlayCommandsObj=null;

		defFile=null;
		defImageJDir=IJ.getDirectory("default");
		defDir=defImageJDir;

		textFieldOrig.setText(defDir);
		textFieldROI.setText(defDir);

		curFileList=null; //new String[0];
		curFileIdx = -1;
		stepping=false;

		selectedObjectType="ROI";


		addMouseListener(IJ.getInstance());


		Window logWindow=WindowManager.getWindow("Log");
		if (logWindow!=null) {
			logWindow.setVisible(true);
		}

		// for autosave
		startTime = System.currentTimeMillis();
	}


    // ---- processMouseEvent fcn was here -------


	public void itemStateChanged(ItemEvent ie) {
	    JCheckBox cb = (JCheckBox) ie.getItem();
	    int state = ie.getStateChange();
	    boolean isSelected=false;
	    String cbText=cb.getText();
	    if (state == ItemEvent.SELECTED){
	      IJ.log(cbText + " selected");
	      isSelected=true;
	    }
	    else if (state==ItemEvent.DESELECTED){
	      IJ.log(cbText + " cleared");
	      isSelected=false;
	    }

	  	if (cbText.equals("Multi-label (instances)")){
			multiLabel=isSelected;
  			IJ.log("Multi-label (instances): "+String.valueOf(state));
	  	}
  		else if (cbText.equals("Multi-layer (stack)")){
  			multiLayer=isSelected;
  			IJ.log("Multi-layer (stack): "+String.valueOf(state));
  		}
  		else if(cbText.equals("Semantic (binary)")){
  			semantic=isSelected;
  			IJ.log("Semantic (binary): "+String.valueOf(state));

  		}
  		else if(cbText.equals("Coordinates")){
  			coordinates=isSelected;
  			IJ.log("Coordinates: "+String.valueOf(state));

  		}
  		else if(cbText.equals("Overlay")){
  			overlay=isSelected;
  			IJ.log("Overlay: "+String.valueOf(state));
  		}

  		else
  			IJ.showStatus("Unexpected checkbox");
	  }


	void addButton(String label) {
		Button b = new Button(label);
		b.addActionListener(this);
		b.addKeyListener(IJ.getInstance());
		//panel.add(b);
	}

	// this maybe useful to detect the current image and do something with it
	public void actionPerformed(ActionEvent e) {
		//imp = WindowManager.getCurrentImage();
		/*
		if (imp==null) {
			IJ.beep();
			IJ.showStatus("No image");
			previousID = 0;
			return;
		}
		if (!imp.lock())
			{previousID = 0; return;}
		int id = imp.getID();
		if (id!=previousID)
			imp.getProcessor().snapshot();
		previousID = id;
		*/
		String label = e.getActionCommand();
		if (label==null)
			return;
		new Runner(label, imp);
	}

	public boolean closeActiveWindows(int curFileIdx, int curFileListLength){
		boolean doClose=true;
		// ask for confirmation
    	int response = JOptionPane.showConfirmDialog(null, "Export is in progress: "+String.valueOf(curFileIdx+1)+"/"+String.valueOf(curFileListLength)+"\nAbort exporting?", "Abort export",
	        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	    if (response == JOptionPane.NO_OPTION) {
	    	// do nothing
	    	IJ.log("No button clicked");
	    	doClose=false;
	    	return doClose;

	    } else if (response == JOptionPane.YES_OPTION) {
		    IJ.log("Yes button clicked");
		    // stop process and quit
		    // TODO: stop process
		    doClose=true;
		    
	    } else if (response == JOptionPane.CANCEL_OPTION){
	    	// do nothing
	    	IJ.log("Cancel button clicked");
	    	doClose=false;
	    	return doClose;

	    } else if (response == JOptionPane.CLOSED_OPTION) {
	    	// do nothing
	    	IJ.log("Closed close confirm");
	    	doClose=false;
	    	return doClose;
	    }
	    return doClose;
	}

	public void processWindowEvent(WindowEvent e) {
		//super.processWindowEvent(e);
		if (e.getID()==WindowEvent.WINDOW_CLOSING) {

			// are you sure confirm shortly:
			int sure = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit confirm",
		        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    if (sure == JOptionPane.NO_OPTION) {
		    	// do nothing
		    	IJ.log("No button clicked");
		    	return;

		    } else if (sure == JOptionPane.YES_OPTION) {
			    IJ.log("Yes button clicked");
			    // continue
			    
		    } else if (sure == JOptionPane.CLOSED_OPTION) {
		    	// do nothing
		    	IJ.log("Closed sure close confirm");
		    	return;
		    }

			// check if there is anything open
			if (!started) {
				Window logWindow=WindowManager.getWindow("Log");
				if (logWindow!=null) {
			    	//logWindow.dispose();
			    	IJ.log("\\Clear");
			    	logWindow.setVisible(false);
			    }
			    // close the main frame too
			    dispose();
			    instance = null;
			    return;
			}


		    boolean continueClosing=true;

		    if (started && !finished) {
		    	// export in progress
		    	//IJ.log("export is in progress: "+String.valueOf(curFileIdx+1)+"/"+String.valueOf(curFileList.length)+" | please wait");
		    	IJ.log("export is in progress: "+String.valueOf(curFileIdx+1)+"/"+String.valueOf(curROIList.length)+" | please wait");
				// offer to stop it -->
				//continueClosing=closeActiveWindows(curFileIdx,curFileList.length);
				continueClosing=closeActiveWindows(curFileIdx,curROIList.length);
		    }
	    	

		    if (continueClosing) {
			    // close everything
			    Window logWindow=WindowManager.getWindow("Log");
			    if (logWindow!=null) {
			    	//logWindow.dispose();
			    	IJ.log("\\Clear");
			    	logWindow.setVisible(false);
			    }

			    // close progress frame:
			    if (progressFrame!=null) {
			    	progressFrame.dispose();
			    	progressFrame=null;
			    }

			    // close the main frame too
			    dispose();
			    instance = null;

			} else {
				IJ.log("Closing canceled");
			}
		}
	}


	public void openExportProgressFrame(){
		///*
		progressFrame = new JFrame();
		progressFrame.setBounds(200, 200, 300, 170);
		// this should be a separate function!!!!!!!
		progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//*/


		/*
		dlg = new JDialog(instance, "Export progress", true);
		//final JDialog dlg = new JDialog(progressFrame, "Export progress", true);
		dlg.setSize(300, 170);
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		//dlg.setLocationRelativeTo(instance);
		//dlg.setLocationRelativeTo(progressFrame);
		*/
		
		progressPanel = new Panel();
		progressFrame.getContentPane().add(progressPanel, BorderLayout.CENTER);
		//dlg.add(progressPanel, BorderLayout.CENTER);
		
		lblExportingImages = new JLabel("Exporting images...");
		add(lblExportingImages);
		
		lblCurrentImage = new JLabel("Current image");
		add(lblCurrentImage);
		
		progressBar = new JProgressBar();
		add(progressBar);
		
		btnOk = new JButton("OK");
		//btnOk.addActionListener(this);
		btnOk.addKeyListener(IJ.getInstance());
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// close the progress window
				if (progressFrame!=null) {
					
					progressFrame.dispose();
					progressFrame=null;
					
					//dlg.dispose();
					//dlg=null;
					
				}
			}
		});
		add(btnOk);
		
		btnCancelProgress = new JButton("Cancel");
		btnCancelProgress.addKeyListener(IJ.getInstance());
		btnCancelProgress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// check if there is a process running --> if so, confirm stopping and stop it
				if (started && !finished) {
					// process is running
					// confirm
					// stop it, close
					// TODO
				} else {
					// close the progress window
					if (progressFrame!=null) {
						
						progressFrame.dispose();
						progressFrame=null;
						
						//dlg.dispose();
						//dlg=null;
						
					}
				}
			}
		});
		add(btnCancelProgress);


		GroupLayout gl_panelProgress = new GroupLayout(progressPanel);
		gl_panelProgress.setHorizontalGroup(
			gl_panelProgress.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelProgress.createSequentialGroup()
					.addGroup(gl_panelProgress.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelProgress.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panelProgress.createParallelGroup(Alignment.LEADING)
								.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
								.addComponent(lblExportingImages)
								.addComponent(lblCurrentImage)))
						.addGroup(gl_panelProgress.createSequentialGroup()
							.addGap(59)
							.addComponent(btnCancelProgress)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnOk, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panelProgress.setVerticalGroup(
			gl_panelProgress.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelProgress.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblExportingImages)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblCurrentImage)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelProgress.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCancelProgress)
						.addComponent(btnOk))
					.addContainerGap(22, Short.MAX_VALUE))
		);
		progressPanel.setLayout(gl_panelProgress);


		//add(progressPanel);

		
		pack();
		//GUI.center(progressFrame);
		//GUI.center(dlg);
		//dlg.setVisible(true);
		progressFrame.setVisible(true);

		btnOk.setEnabled(false);
	}


	void textFieldActionPerformed(ActionEvent e, JTextField textField){
		if (textField==textFieldOrig) {
			String newOrigText=textFieldOrig.getText();
			if (newOrigText.equals(originalFolder)) {
				// no new folder typed to open, do nothing
				return;
			} else {
				originalFolder=newOrigText;
				// new folder, open it without the dialog
				initializeOrigFolderOpening(originalFolder);
			}

		} else if (textField==textFieldROI) {
			String newROIText=textFieldROI.getText();
			if (newROIText.equals(annotationFolder)) {
				// no new folder typed to open, do nothing
				return;
			} else {
				annotationFolder=newROIText;
				// new folder, open it without the dialog
				initializeROIFolderOpening(annotationFolder);
			}
			
		}
	}


	void initializeOrigFolderOpening(String originalFolder){

		defDir=originalFolder;


		// get a list of files in the current directory
		File folder = new File(originalFolder);
		File[] listOfFiles = folder.listFiles();
		int fileListCount=0;
		//String[] curFileList;

		// get number of useful files
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile() && (listOfFiles[i].getName().endsWith(".png") || listOfFiles[i].getName().endsWith(".bmp") || listOfFiles[i].getName().endsWith(".jpg") || listOfFiles[i].getName().endsWith(".jpeg") || listOfFiles[i].getName().endsWith(".tif") || listOfFiles[i].getName().endsWith(".tiff"))) {
		  	fileListCount+=1;
		  }
		}

		// check if there are correct files in the selected folder
		if (fileListCount<1) {
			IJ.log("No original image files found in current folder");
			MessageDialog noFilesMsg=new MessageDialog(instance,
             "Error",
             "Could not find original image files in selected folder");
			started=false;
			return;
		}

		// update file list array
		curFileList=new String[fileListCount];
		IJ.log("Found "+String.valueOf(fileListCount)+" images in current folder");
		fileListCount=0;
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile() && (listOfFiles[i].getName().endsWith(".png") || listOfFiles[i].getName().endsWith(".bmp") || listOfFiles[i].getName().endsWith(".jpg") || listOfFiles[i].getName().endsWith(".jpeg") || listOfFiles[i].getName().endsWith(".tif") || listOfFiles[i].getName().endsWith(".tiff"))) {
		  	curFileList[fileListCount]=listOfFiles[i].getName();
		  	fileListCount+=1;
		  }
		}

		startedOrig=true;
		if (startedROI) {
			started=true;
		}
	}


	void initializeROIFolderOpening(String annotationFolder){

		// get a list of files in the current directory
		File folder2 = new File(annotationFolder);
		File[] listOfROIs = folder2.listFiles();
		int ROIListCount=0;
		//String[] curFileList;

		// get number of useful files
		// see which object type is selected
		String annotNameReg=null;
		String annotExt=null;
		switch (selectedObjectType){
			case "ROI":
				//
				annotNameReg="_ROIs";
				annotExt=".zip";
				break;
			case "semantic":
				//
				annotNameReg="_semantic";
				annotExt=".tiff";
				break;
			case "bbox":
				//
				annotNameReg="_bboxes";
				annotExt=".zip";
				break;
			default:
				//
				break;
		}
		for (int i = 0; i < listOfROIs.length; i++) {
			// old, only for all .zip files
			/*
			if (listOfROIs[i].isFile() && (listOfROIs[i].getName().endsWith(".zip"))) {
				ROIListCount+=1;
			}
			*/

			// new, for any type of object we support
			if (listOfROIs[i].isFile()) {
				String curFileName=listOfROIs[i].getName();
				if (curFileName.endsWith(annotExt) && curFileName.contains(annotNameReg)) {
					ROIListCount+=1;
				}
			}
		}


		// check if there are correct files in the selected folder
		if (ROIListCount<1) {
			IJ.log("No annotation files found in current folder");
			MessageDialog noROIsMsg=new MessageDialog(instance,
             "Error",
             "Could not find annotation files in selected folder");
			started=false;
			return;
		}

		// update file list array
		curROIList=new String[ROIListCount];
		IJ.log("Found "+String.valueOf(ROIListCount)+" annotation files in current folder");
		ROIListCount=0;
		for (int i = 0; i < listOfROIs.length; i++) {
			// old, only for all .zip files
			/*
			if (listOfROIs[i].isFile() && (listOfROIs[i].getName().endsWith(".zip"))) {
				curROIList[ROIListCount]=listOfROIs[i].getName();
				ROIListCount+=1;
			}
			*/

		  	// new, for any type of object we support
			if (listOfROIs[i].isFile()) {
				String curFileName=listOfROIs[i].getName();
				if (curFileName.endsWith(annotExt) && curFileName.contains(annotNameReg)) {
					curROIList[ROIListCount]=curFileName;
					ROIListCount+=1;
				}
			}
		}

		instance.toFront();

		startedROI=true;
		if (startedOrig) {
			started=true;
		}
	}


	// from https://imagej.nih.gov/ij/source/ij/plugin/Selection.java:
	//private static void transferProperties(Roi roi1, Roi roi2) {
	public void transferProperties(Roi roi1, Roi roi2) {
		if (roi1==null || roi2==null)
			return;
		roi2.setStrokeColor(roi1.getStrokeColor());
		if (roi1.getStroke()!=null)
			roi2.setStroke(roi1.getStroke());
		roi2.setDrawOffset(roi1.getDrawOffset());
	}


	class Runner extends Thread{ // inner class
		private String command;
		private ImagePlus imp;
	
		Runner(String command, ImagePlus imp) {
			super(command);
			this.command = command;
			this.imp = imp;
			setPriority(Math.max(getPriority()-2, MIN_PRIORITY));
			start();
		}
	
		public void run() {
			try {
				runCommand(command, imp);
			} catch(OutOfMemoryError e) {
				IJ.outOfMemory(command);
				if (imp!=null) imp.unlock();
			} catch(Exception e) {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
				IJ.showStatus("");
				if (imp!=null) imp.unlock();
			}
		}

	
		void runCommand(String command, ImagePlus imp) {

			ImageProcessor ip=null;
			ImageProcessor mask=null;
			Roi roi;
			long startTime=0;

			if (command.equals("Browse original ...") || command.equals("Browse annot ...")){
				// do this later when the image is opened
			}
			else if(!started){
				// collect folder names from the text fields
				originalFolder=textFieldOrig.getText();
				initializeOrigFolderOpening(originalFolder);
				annotationFolder=textFieldROI.getText();
				initializeROIFolderOpening(annotationFolder);

				// old warning:
				/*
				IJ.showStatus("Open an original image and annotation folder first");
				MessageDialog notStartedMsg=new MessageDialog(instance,
                 "Warning",
                 "Click Browse buttons to select an image and annotation folder first");
				return;
				*/
			}
			else{
				// do nothing?
			}



			// my functions executed
			// BROWSE ORIGINAL -----------------------------------------
			if (command.equals("Browse original ...")){
				// open folder loading dialog box

				if (started) {
					// check if there are rois added:
					if (!exportDone) {
						// check how far the current export has progressed
						closeActiveWindows(curFileIdx,curFileList.length);

					}

				    Window logWindow=WindowManager.getWindow("Log");
				    if (logWindow!=null) {
				    	//logWindow.dispose();
				    	IJ.log("\\Clear");
				    	//logWindow.setVisible(false);
				    	logWindow.setVisible(true);
				    }

				}


				// open folder dialog
				JFileChooser chooser = new JFileChooser();
			    chooser.setCurrentDirectory(new java.io.File(defDir));
			    chooser.setDialogTitle("Select original image folder");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);
			    int returnVal = chooser.showOpenDialog(null);

			    if(returnVal == JFileChooser.APPROVE_OPTION) {
		            originalFolder=chooser.getSelectedFile().getPath();
					IJ.log("Opened original image folder: "+originalFolder);
					textFieldOrig.setText(originalFolder);
			    } else if (returnVal==JFileChooser.CANCEL_OPTION) {
			    	IJ.log("canceled original folder open");
			    	return;
				} else {
			    	IJ.log("Failed to open original image folder");
					MessageDialog failedFolderOpenMsg=new MessageDialog(instance,
	                 "Error",
	                 "Could not open folder");
					return;
			    }

			    initializeOrigFolderOpening(originalFolder);

			}
			// BROWSE ROI ---------------------------------------
			else if (command.equals("Browse annot ...")){
				if (started) {
					// check if there are rois added:
					if (!exportDone) {
						// check how far the current export has progressed
						closeActiveWindows(curFileIdx,curFileList.length);

					}

				    Window logWindow=WindowManager.getWindow("Log");
				    if (logWindow!=null) {
				    	//logWindow.dispose();
				    	IJ.log("\\Clear");
				    	//logWindow.setVisible(false);
				    	logWindow.setVisible(true);
				    }

				}


				// open folder dialog
			    JFileChooser chooser2 = new JFileChooser();
			    chooser2.setCurrentDirectory(new java.io.File(defDir));
			    chooser2.setDialogTitle("Select annotation folder");
			    chooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser2.setAcceptAllFileFilterUsed(false);
			    int returnVal2 = chooser2.showOpenDialog(null);

			    if(returnVal2 == JFileChooser.APPROVE_OPTION) {
		            annotationFolder=chooser2.getSelectedFile().getPath();
					IJ.log("Opened annotation image folder: "+annotationFolder);
					textFieldROI.setText(annotationFolder);
			    } else if (returnVal2==JFileChooser.CANCEL_OPTION) {
			    	IJ.log("canceled annotation folder open");
			    	return;
				} else {
			    	IJ.log("Failed to open annotation image folder");
					MessageDialog failedFolderOpenMsg2=new MessageDialog(instance,
	                 "Error",
	                 "Could not open folder");
					return;
			    }


				initializeROIFolderOpening(annotationFolder);
			}

			// EXPORT --------------------------------
			else if (command.equals("Export masks")){
				if (!started) {
					IJ.showStatus("Open an image and annotation folder first");
					MessageDialog notStartedMsg=new MessageDialog(instance,
                     "Warning",
                     "Click Browse buttons to initialize folders");
					return;
				}


				// check that at least one export option is selected:
				if (!multiLabel && !multiLayer && !semantic && !coordinates && !overlay) {
					IJ.showStatus("Select export option");
					IJ.log("No export option is selected");
					MessageDialog noExportOptionMsg=new MessageDialog(instance,
                     "Error",
                     "Select at least one export option");
					return;
				}

				finished=false;

				// this doesnt work yet:
				openExportProgressFrame();

				// check if the folders have the same number of files
				// check if every annotation file has a corresponding original image file
				int origFileCount=curFileList.length;
				int annotFileCount=curROIList.length;
				ArrayList<String> skipFileList = new ArrayList<String>();
				String curAnnotType=null;
				if (origFileCount!=annotFileCount) {
					IJ.log("Different number of files in folders");
				} else {
					IJ.log("Same number of files in folders");
				}

				// set progressbar length
				//progressBar.setMaximum(annotFileCount-1);
				progressBar.setMaximum(annotFileCount);

				// for annot time saving
				annotTimes=new ResultsTable(); //(100);
				annotTimes.showRowIndexes(false);
				annotTimes.showRowNumbers(false);
				annotCount=0;
				lastStartTime=System.nanoTime();
				// TODO: delete this!!!!!! ^

				IJ.log("---- starting export ----");
				// check for annot file correspondance
				for (int i=0; i<annotFileCount; i++) {
					String curAnnotFileName=curROIList[i];
					IJ.log("("+String.valueOf(i+1)+"/"+String.valueOf(annotFileCount)+"): "+curAnnotFileName);

					// check if this file is in the skip list by being a multiple-annotated file
					if (skipFileList.contains(curAnnotFileName)) {
						// should check if the skipped annot file matches another original file name better !!!!!!!!
						// TODO

						// it is, skip it
						IJ.log("skipping annotation file "+curAnnotFileName);
						continue;
					}

					String curAnnotFileRaw=null;
					// find annotation type by name:
					if (curAnnotFileName.contains("_ROIs")) {
						// roi file
						curAnnotFileRaw=curAnnotFileName.substring(0,curAnnotFileName.lastIndexOf("_ROIs"));
						curAnnotType="ROI";
					} else if (curAnnotFileName.contains("_bboxes")) {
						// bbox file
						curAnnotFileRaw=curAnnotFileName.substring(0,curAnnotFileName.lastIndexOf("_bboxes"));
						curAnnotType="bbox";
					} else if (curAnnotFileName.contains("_semantic")) {
						// semantic file
						curAnnotFileRaw=curAnnotFileName.substring(0,curAnnotFileName.lastIndexOf("_semantic"));
						curAnnotType="binary";
					} else {
						IJ.log("could not determine type of annotation file: "+curAnnotFileName);
						//curAnnotType=null;
						//return;
						// use default ROI in this case
						IJ.log("using default annotation type: ROI");
						curAnnotType="ROI";
						curAnnotFileRaw=curAnnotFileName.substring(0,curAnnotFileName.lastIndexOf("."));
					}


					// check if multiple annotation files exist for this image/annot file:
					boolean multipleAnnots=false;
					ArrayList<Integer> multipleList = new ArrayList<Integer>();
					for (int e=0; e<annotFileCount; e++) {
						if (e==i) {
							// current annot file
							continue;
						} else {
							String tmpAnnotFileName=curROIList[e];
							int tmpIdx=tmpAnnotFileName.indexOf(curAnnotFileRaw);
							if (tmpIdx==-1) {
								// not found, continue
							} else {
								// another annot file for this image found!
								// store this name or idx
								// TODO
								multipleList.add(e);
								multipleAnnots=true;
							}
						}
					}

					if (multipleAnnots) {
						// show dialog to choose which annotation file they want for the image
						// ask annotation type in dialog box
						int multiNum=multipleList.size();
						String[] annotNames=new String[multiNum+1];
						annotNames[0]=curAnnotFileName;
						for (int e=0; e<multiNum; e++) {
							annotNames[e+1]=curROIList[multipleList.get(e)];
						}

						GenericDialog Dialog = new GenericDialog("Multiple instances found");

						//Dialog.create("Annotation type");
						Dialog.addChoice("Select which annotation file to use for "+curAnnotFileRaw+" :", annotNames, annotNames[0]);
						Dialog.showDialog();
						if (Dialog.wasCanceled()) {
							// selection was cancelled --> abort
							finished=true;
							exportDone=true;
							lblExportingImages.setText("Exporting was cancelled");
							btnOk.setEnabled(true);
							btnCancelProgress.setEnabled(false);
							return;
							// do not continue if cancel was pressed:
							//IJ.log("Using default (1st match) annoation file: "+curAnnotFileName);
							//continue;
						} else {
							int choiceIdx=Dialog.getNextChoiceIndex();

							// add all other options to skipFileList
							for (int c=0; c<multiNum+1; c++) {
								if (c==0) {
									// first item was the original name
									skipFileList.add(curAnnotFileName);
								/*
								} else if (c==choiceIdx) {
									// this item was selected, don't add it to the skiplist
									//continue;
								*/
								} else {
									// any other item from the list
									skipFileList.add(curROIList[multipleList.get(c-1)]);
								}
							}

							// can set selected file name now:
							//curAnnotFileName = Dialog.getNextChoice();
							curAnnotFileName = annotNames[choiceIdx];
							IJ.log("Selected annotation file: "+curAnnotFileName);
							
						}
					}


					// find if its original image exists
					//Arrays.asList(curFileList).contains(curAnnotFileRaw);
					boolean foundIt=false;
					String curOrigFileName=null;
					for (int j=0; j<origFileCount; j++) {
						curOrigFileName=curFileList[j];
						String curOrigFileNameRaw=curOrigFileName.substring(0,curOrigFileName.lastIndexOf("."));
						if (curAnnotFileRaw.equals(curOrigFileNameRaw)) {
							// found it
							foundIt=true;
							break;
						} else {
							// continue searching
						}
					}

					if (foundIt) {
						// check annotation type:
						// so far only instance segmentation is supported
						if (!curAnnotType.equals("ROI")) {
							// implemented now
							/*
							IJ.log("export option not implemented yet");
							MessageDialog notImplementedExportOptionMsg=new MessageDialog(instance,
		                     "Error",
		                     "Export option "+curAnnotType+" is not implemented yet");
							return;
							*/
						}



						// ---------------------
						// call export function:
						// ---------------------
						startExport(curAnnotFileName,curAnnotFileRaw,curOrigFileName,annotFileCount,i,curAnnotType);

					} else {
						// no original image for it --> skip or throw error?
						// cannot generate mask for sure as we need the image dims to create a mask of the same size!
						IJ.log("No original image found for annotation file \""+curAnnotFileName+"\" --> skipping it");
						//IJ.log("orig: "+curOrigFileName+"| annot: "+curAnnotFileRaw);
						IJ.log("---------------------");
						continue;
					}
				}
				IJ.log("---- finished export ----");
				
				//progressBar.setValue(annotFileCount-1);
				progressBar.setValue(annotFileCount);
				
				// finished every image in the folder
				btnOk.setEnabled(true);
				btnCancelProgress.setEnabled(false);
				lblExportingImages.setText("Finished exporting images");
				
				//}


				// save annot time in file
		    	String annotFolder=annotationFolder+File.separator+"exportTimes";
		    	new File(annotFolder).mkdir();
				IJ.log("Created output folder: "+annotFolder);
				String annotFileNameRaw="exportTimes.csv";
		    	String annotFileName=annotFolder+File.separator+annotFileNameRaw;
		    	File f2 = new File(annotFileName);
		    	if (f2.exists() && !f2.isDirectory()){
			    	annotFileName=annotFolder+File.separator+annotFileNameRaw.substring(0,annotFileNameRaw.lastIndexOf("."))+"_1.csv";
			    	int newFileNum2=1;
			    	f2 = new File(annotFileName);
					while(f2.exists() && !f2.isDirectory()){
						newFileNum2+=1;
						annotFileName=annotFolder+File.separator+annotFileNameRaw.substring(0,annotFileNameRaw.lastIndexOf("."))+"_"+String.valueOf(newFileNum2)+".csv";
						f2 = new File(annotFileName);
					}
				}
		    	boolean successfullySaved=annotTimes.save(annotFileName);
		    	if (successfullySaved)
		    		IJ.log("Saved export times in file: "+annotFileName);
		    	// TODO: delete this!!!!!! ^


				// when open functions finish:
				started=true;

				// check export options
				// call export function
				//startExport();

				finished=true;
				exportDone=true;

			}

			// OVERLAY ---------------------------
			else if (command.equals("Cancel")){

				// if this window was opened from the main annotator frame just close it
				// otherwise use it to stop processing

				if (!started) {
					IJ.showStatus("Nothing to stop");
					IJ.log("Nothing to stop");
					MessageDialog notStartedMsg2=new MessageDialog(instance,
                     "Info",
                     "No export process to stop");
					return;
				} else {
					// process is running
					// try to stop it
					// TODO

					// abort
					finished=true;
					exportDone=true;
					lblExportingImages.setText("Exporting was cancelled");
					btnOk.setEnabled(true);
					btnCancelProgress.setEnabled(false);
					return;
				}

			}

			/*
			// OK ------------------------------
			else if (command.equals("OK")) {
				
				// close the progress window
				if (progressFrame!=null) {
					
					//progressFrame.dispose();
					//progressFrame=null;
					
					
					dlg.dispose();
					dlg=null;
					
				}
			}
			*/


		}


		void startExport(String curAnnotFileName, String curAnnotFileRaw, String curOrigFileName, int annotFileCount, int i, String curAnnotType){
			// start a progress bar for logging

			// ------------ for logging progress: -----------------
			// (i+1/annotFileCount) <-- to log

			// update file name tag on main window to check which image we are annotating
			String displayedName=curAnnotFileRaw;
			int maxLength=20;
			// check how long the file name is (if it can be displayed)
			int nameLength=curAnnotFileRaw.length();
			if (nameLength>maxLength) {
				displayedName=curAnnotFileRaw.substring(0,maxLength-3)+"...tiff";
			}
			// display this in the progress bar too:
			// not yet
			///*
			// set the labels and progress
			lblExportingImages.setText("Exporting images...");
			lblCurrentImage.setText(" ("+String.valueOf(i+1)+"/"+String.valueOf(annotFileCount)+"): "+displayedName);
			//*/
			
			//lblCurrentFile.setText(" ("+String.valueOf(curFileIdx+1)+"/"+String.valueOf(curFileList.length)+"): "+displayedName);

			// ------------- logging end -------------------------
			
			

			// export
			// ----------------------------------------------------------------
			// instance segmentation

			// create masks folder in current annotation folder
			// create output folder with the class name
			

			int[] dimensions;
			int width=0;
			int height=0;

			// this annotation file has a corresponding image
			// read original image to get the dimensions of it:
			ImagePlus origImage=IJ.openImage(originalFolder+File.separator+curOrigFileName);
			if (origImage!=null) {
				// read it successfully
				// if it is opened by default, close the window after getting the size info from it
				// dimensions is an array of (width, height, nChannels, nSlices, nFrames)
				dimensions=origImage.getDimensions();
				width=dimensions[0];
				height=dimensions[1];
				// close the window:
				origImage.hide();
			} else {
				IJ.log("Could not open original image: "+curOrigFileName);
				MessageDialog failed2openOrigImageMsg=new MessageDialog(instance,
                 "Error",
                 "Could not open original image: "+curOrigFileName);
				// allow to continue the processing?
				// if not, close progressbar too

				return;
			}

			// open annotation file
			RoiManager manager=new RoiManager(false);

			// check annot type first
			if (curAnnotType.equals("ROI") || curAnnotType.equals("bbox")) {
				// only need to load the .zip file
				boolean loadedROIsuccessfully=manager.runCommand("Open",annotationFolder+File.separator+curAnnotFileName);
				if (!loadedROIsuccessfully) {
					IJ.log("Failed to open ROI: "+curAnnotFileName);
					MessageDialog failed2loadROIMsg=new MessageDialog(instance,
	                 "Error",
	                 "Failed to open ROI .zip file");
					return;
				} else {
					IJ.log("Opened ROI: "+curAnnotFileName);
				}

			}
			else if (curAnnotType.equals("binary")) {
				// need to convert the semantic binary image to rois
				int[] semdimensions;
				int semwidth=0;
				int semheight=0;
				ImagePlus semanticImage=IJ.openImage(annotationFolder+File.separator+curAnnotFileName);
				if (semanticImage!=null) {
					// read it successfully
					// if it is opened by default, close the window after getting the size info from it
					// dimensions is an array of (width, height, nChannels, nSlices, nFrames)
					semdimensions=semanticImage.getDimensions();
					semwidth=semdimensions[0];
					semheight=semdimensions[1];
					if (semwidth!=width || semheight!=height) {
						IJ.log("Inconsistent size of semantic annotation image: "+curAnnotFileName+", skipping it");
						MessageDialog failed2openOrigImageMsg=new MessageDialog(instance,
		                 "Error",
		                 "Could not verify annotation image: "+curAnnotFileName);
						return;
					}
					// create rois from it
					semanticImage.show();
					Converter converterObj=new Converter();
					converterObj.run("8-bit");
					Prefs.set("threshold.dark",true); // set background to dark (black)
					Prefs.blackBackground=true;
					(new Thresholder()).run("skip");
					Roi semanticROI=ThresholdToSelection.run(semanticImage);
					if (semanticROI.getType()==Roi.COMPOSITE) {
						// multiple rois in the roi --> split them!
						Roi[] manyROIs = ((ShapeRoi)semanticROI).getRois();
				        for (int sroi=0; sroi<manyROIs.length; sroi++) {
				        	Roi thisROI=manyROIs[sroi];
				        	manager.add(thisROI,sroi);
				        }
					} else {
						manager.add(semanticROI,1);
					}

					// close the window:
					semanticImage.hide();
				} else {
					IJ.log("Could not open original image: "+curOrigFileName);
					MessageDialog failed2openOrigImageMsg=new MessageDialog(instance,
	                 "Error",
	                 "Could not open original image: "+curOrigFileName);
					// allow to continue the processing?
					// if not, close progressbar too

					return;
				}
				
			}
			// check if there are annotated objects in the file:
			int roiCount=manager.getCount();
			IJ.log("annotated objects: "+String.valueOf(roiCount));
			if (roiCount<1) {
				// this continue worked while this whole bunch of export code was in the EXPORT ------- for cycle
				//continue;
				return;
			}

			// create mask image

			//ImagePlus mask = createImage("", width, height, 1, 16, options);
			// based on https://imagej.nih.gov/ij/developer/source/ij/gui/NewImage.java.html createShortImage fcn:
			long lsize = (long)width*height;
			int size = (int)lsize;
	        if (size<0) {
	        	IJ.log("0-sized image");
	        	return;
	        }
	        short[] pixels;

	        ImageProcessor maskImageProc=null;
	        ImagePlus maskImage=null;
	        pixels = new short[size];

	        // it will be filled with black (0) values by default and hopefully isn't displayed in a window
	        maskImageProc = new ShortProcessor(width, height, pixels, null);
	        if (maskImageProc==null) {
	        	IJ.log("could not create a new mask (1)");
	        	return;
	        }
	        maskImage = new ImagePlus("title", maskImageProc);
	        maskImage.getProcessor().setMinAndMax(0, 65535);
	        //maskImage.setMinAndMax(0, 65535); // 16-bit
	        if (maskImage==null) {
	        	IJ.log("could not create a new mask (2)");
	        	return;
	        }

	        /*
			ImageProcessor maskImage=new ImageProcessor();
			// fill it with 0 values:
			double bgValue=0.0;
			maskImage.set(bgValue); // <-- this sets double values
			// this sets by pixel to int --> probably not worth it:
			for (int n=1; n<width; n++) {
				for (int m=1; m<height; m++) {
					maskImage.set(n,m,0);
				}
			}
			// convert image to 16-bit grayscale:
			new StackConverter(maskImage).convertToGray16();
			*/

			// fill output mask image according to export type selected:
			String outputFileName=null;
			String annotationFolder2=null;
			String exportFolder=null;
			boolean refreshMask=false;


			// collect class info from the ROIs if any
			ArrayList<Integer> nonEmptyClassNameNumbers=new ArrayList<Integer>();
			for (int r=0; r<roiCount; r++) {
				// get r. instance
				Roi curROI=manager.getRoi(r);
				int curROIgroup=curROI.getGroup();
				if (curROIgroup>0 && !nonEmptyClassNameNumbers.contains(curROIgroup)){
					nonEmptyClassNameNumbers.add(curROIgroup);
					//debug:
					IJ.log("Nonempty class idx+= "+curROIgroup);
				}
				manager.deselect(curROI);
			}
			
			String exportFolderClass=null;
			File outDir=null;


			// check export type selected
			if (multiLabel) {
				// labelled masks on single layer of mask

				exportFolder="labelled_masks";

				// start getting the objects from the annotation file
				for (int r=0; r<roiCount; r++) {
					// get r. instance
					Roi curROI=manager.getRoi(r);
					// set fill value
					maskImage.getProcessor().setColor(r+1);
					maskImage.getProcessor().fill(curROI);
					manager.deselect(curROI);
				}

				// create export folder:
				annotationFolder2=createExportFolder(exportFolder);
				// construct output file name:
				outputFileName=annotationFolder2+File.separator+curAnnotFileRaw+".tiff";
				// save output image:
				saveExportedImage(maskImage, outputFileName);

				// also save class mask images if the ROIs have class info saved
				for (int c=0; c<nonEmptyClassNameNumbers.size(); c++){

					int curClassNum=nonEmptyClassNameNumbers.get(c);

					// create export folder:
					exportFolderClass=annotationFolder2+File.separator+"Class_"+String.format("%02d",curClassNum);

					outDir=new File(exportFolderClass);
					if (outDir.exists() && outDir.isDirectory()) {
						// folder already exists
					} else {
						outDir.mkdir();
						IJ.log("Created class output folder: "+exportFolderClass);
					}

					ImagePlus maskImage2=Annotator_MainFrameNew.createClassMask(size,width,height,roiCount,curClassNum,manager,true);
					if (maskImage2==null)
	        			return;

					// construct output file name:
					outputFileName=exportFolderClass+File.separator+curAnnotFileRaw+".tiff";

					saveExportedImage(maskImage2, outputFileName);
				}

				refreshMask=true;

			}

			if (multiLayer) {
				// multi-layer stack mask

				exportFolder="layered_masks";
				boolean ok=false;

				if (refreshMask) {
					// create new empty mask
					pixels = new short[size];
					maskImageProc = new ShortProcessor(width, height, pixels, null);
			        if (maskImageProc==null) {
			        	IJ.log("could not create a new mask (1)");
			        	return;
			        }
			        maskImage = new ImagePlus("title", maskImageProc);
			        maskImage.getProcessor().setMinAndMax(0, 65535);
			        //maskImage.setMinAndMax(0, 65535); // 16-bit
			        if (maskImage==null) {
			        	IJ.log("could not create a new mask (2)");
			        	return;
			        }
				}

				// create stack image:
				if (roiCount>1) {
					manager.runCommand("Deselect all");

					// based on https://imagej.nih.gov/ij/developer/source/ij/gui/NewImage.java.html createStack fcn:
		            //boolean ok = createStack(maskImage, maskImageProc, roiCount, GRAY16, options);
		            // imp = maskImage
		            // ip = maskImageProc
		            ImageStack stack = maskImage.createEmptyStack();
		            try {
		            	//stack.addSlice(null, maskImageProc);
		            	int color1=1;
		            	// start getting the objects from the annotation file
			            for (int s=0; s<roiCount; s++) {
			            	//Object pixels2 = new short[width*height];
			            	//stack.addSlice(null, pixels2);

			            	// could already set the slices here instead of creating empty layers
			            	// it needs an "ImageProcessor ip"
			            	pixels = new short[size];
			            	ImageProcessor tmpSlice = new ShortProcessor(width, height, pixels, null);
					        if (tmpSlice==null) {
					        	IJ.log("could not create layer "+String.valueOf(s));
					        	return;
					        }

					        // get s. instance
							Roi curROI=manager.getRoi(s);
							// set fill value
							tmpSlice.setColor(color1);
							tmpSlice.fill(curROI);
							manager.deselect(curROI);

			            	stack.addSlice(null, tmpSlice);
			            }
		            }
		            catch(OutOfMemoryError e) {
			            IJ.outOfMemory(maskImage.getTitle());
			            IJ.log("Out-of-memory error");
			            stack.trim();
			            ok=false;
			        }
			        if (stack.getSize()>1)
			            maskImage.setStack(null, stack);
			        ok=true;

		            if (!ok){
		            	maskImage = null;
		            	IJ.log("Error creating stack image");
		            	return;
		            }
		        }

		        // create export folder:
				annotationFolder2=createExportFolder(exportFolder);
				// construct output file name:
				outputFileName=annotationFolder2+File.separator+curAnnotFileRaw+".tiff";
				// save output image:
				saveExportedImage(maskImage, outputFileName);

				// also save class mask images if the ROIs have class info saved
				for (int c=0; c<nonEmptyClassNameNumbers.size(); c++){

					int curClassNum=nonEmptyClassNameNumbers.get(c);

					// create export folder:
					exportFolderClass=annotationFolder2+File.separator+"Class_"+String.format("%02d",curClassNum);

					outDir=new File(exportFolderClass);
					if (outDir.exists() && outDir.isDirectory()) {
						// folder already exists
					} else {
						outDir.mkdir();
						IJ.log("Created class output folder: "+exportFolderClass);
					}

					ImagePlus maskImage2=Annotator_ExportFrameNew.createClassMaskStack(size,width,height,roiCount,curClassNum,manager);
					if (maskImage2==null)
	        			return;

					// construct output file name:
					outputFileName=exportFolderClass+File.separator+curAnnotFileRaw+".tiff";

					saveExportedImage(maskImage2, outputFileName);
				}

				refreshMask=true;

			}

			if (semantic) {
				// binary semantic segmentation image

				exportFolder="binary_masks";

				if (refreshMask) {
					// create new empty mask
					pixels = new short[size];
					maskImageProc = new ShortProcessor(width, height, pixels, null);
			        if (maskImageProc==null) {
			        	IJ.log("could not create a new mask (1)");
			        	return;
			        }
			        maskImage = new ImagePlus("title", maskImageProc);
			        maskImage.getProcessor().setMinAndMax(0, 65535);
			        //maskImage.setMinAndMax(0, 65535); // 16-bit
			        if (maskImage==null) {
			        	IJ.log("could not create a new mask (2)");
			        	return;
			        }
				}

				// start getting the objects from the annotation file
				for (int r=0; r<roiCount; r++) {
					// get r. instance
					Roi curROI=manager.getRoi(r);
					// set fill value
					maskImage.getProcessor().setColor(1);
					maskImage.getProcessor().fill(curROI);
					manager.deselect(curROI);
				}

				// create export folder:
				annotationFolder2=createExportFolder(exportFolder);
				// construct output file name:
				outputFileName=annotationFolder2+File.separator+curAnnotFileRaw+".tiff";
				// save output image:
				saveExportedImage(maskImage, outputFileName);

				// also save class mask images if the ROIs have class info saved
				for (int c=0; c<nonEmptyClassNameNumbers.size(); c++){

					int curClassNum=nonEmptyClassNameNumbers.get(c);

					// create export folder:
					exportFolderClass=annotationFolder2+File.separator+"Class_"+String.format("%02d",curClassNum);

					outDir=new File(exportFolderClass);
					if (outDir.exists() && outDir.isDirectory()) {
						// folder already exists
					} else {
						outDir.mkdir();
						IJ.log("Created class output folder: "+exportFolderClass);
					}

					ImagePlus maskImage2=Annotator_MainFrameNew.createClassMask(size,width,height,roiCount,curClassNum,manager,false);
					if (maskImage2==null)
	        			return;

					// construct output file name:
					outputFileName=exportFolderClass+File.separator+curAnnotFileRaw+".tiff";

					saveExportedImage(maskImage2, outputFileName);
				}

				refreshMask=true;
			}

			if (coordinates) {
				// bounding box coordinates of objects

				exportFolder="bounding_box_coordinates";

				// prepare a bbox array for export
				ResultsTable bboxList=new ResultsTable(roiCount);
				bboxList.showRowIndexes(false);
				bboxList.showRowNumbers(false);
				// start getting the objects from the annotation file
				for (int r=0; r<roiCount; r++) {
					// get r. instance
					Roi curROI=manager.getRoi(r);
					// get coordinates
					Rectangle bbox=curROI.getBounds();
					bboxList.setValue("x",r,bbox.getX());
					bboxList.setValue("y",r,bbox.getY());
					bboxList.setValue("width",r,bbox.getWidth());
					bboxList.setValue("height",r,bbox.getHeight());
					manager.deselect(curROI);
				}

				// create export folder:
				annotationFolder2=createExportFolder(exportFolder);
				// construct output file name:
				outputFileName=annotationFolder2+File.separator+curAnnotFileRaw+".csv";
				// save output csv:
				// TODO: create fcn for this
				saveExportedCSV(bboxList,outputFileName);
				//saveExportedImage(maskImage, outputFileName);

				// also save class mask images if the ROIs have class info saved
				for (int c=0; c<nonEmptyClassNameNumbers.size(); c++){

					int curClassNum=nonEmptyClassNameNumbers.get(c);

					// create export folder:
					exportFolderClass=annotationFolder2+File.separator+"Class_"+String.format("%02d",curClassNum);

					outDir=new File(exportFolderClass);
					if (outDir.exists() && outDir.isDirectory()) {
						// folder already exists
					} else {
						outDir.mkdir();
						IJ.log("Created class output folder: "+exportFolderClass);
					}

					ResultsTable bboxList2=Annotator_ExportFrameNew.createClassCSV(roiCount,curClassNum,manager);
					if (bboxList2==null)
	        			return;

					// construct output file name:
					outputFileName=exportFolderClass+File.separator+curAnnotFileRaw+".csv";

					saveExportedCSV(bboxList2,outputFileName);
				}

				refreshMask=true;
			}

			if(overlay){
				// outlines overlayed on original image

				exportFolder="outlined_images";

				// create export folder:
				annotationFolder2=createExportFolder(exportFolder);
				// construct output file name:
				outputFileName=annotationFolder2+File.separator+curAnnotFileRaw+".tiff";
				// save output image:
				origImage.show();
				manager.runCommand("Show All");
				Annotator_ExportFrameNew.saveOutlinedImage(origImage,outputFileName,annotationFolder2);
				manager.runCommand("Show None");
				origImage.hide();
			}

			
			// measure time
    		long curTime = (System.nanoTime()-lastStartTime)/(long)1000000; //ms time
    		annotTimes.setValue("#",annotCount,annotCount);
    		annotTimes.setValue("image",annotCount,curAnnotFileRaw);
    		annotTimes.setValue("time",annotCount,curTime);
    		annotCount+=1;


			// finished this image
			// not yet:
			///*
			//progressBar.setValue(i);
			progressBar.setValue(i+1);
			//if (i==annotFileCount-1) {
			if (i==annotFileCount) {
				// finished every image in the folder
				btnOk.setEnabled(true);
				btnCancelProgress.setEnabled(false);
				lblExportingImages.setText("Finished exporting images");
			}
			//*/


    		// TODO: delete this: -->
			lastStartTime=System.nanoTime();

		}

		String createExportFolder(String exportFolder){
			String annotationFolder2=annotationFolder+File.separator+exportFolder;
			File outDir=new File(annotationFolder2);
			if (outDir.exists() && outDir.isDirectory()) {
				// folder already exists
			} else {
				outDir.mkdir();
				IJ.log("Created output folder: "+annotationFolder2);
			}
			return annotationFolder2;
		}

		boolean saveExportedImage(ImagePlus maskImage, String outputFileName){
			// save output image to folder
			boolean successfullySaved=IJ.saveAsTiff(maskImage,outputFileName);
			if (successfullySaved) {
				IJ.log("Saved exported image: "+outputFileName+"\n");
				IJ.log("---------------------");
			} else {
				IJ.log("Could not save exported image");
			}
			return successfullySaved;
		}

		boolean saveExportedCSV(ResultsTable list, String outputFileName){
			// save coordinates to .csv in output folder
			boolean successfullySaved=list.save(outputFileName);
			if (successfullySaved) {
				IJ.log("Saved exported coordinates for image: "+outputFileName+"\n");
				IJ.log("---------------------");
			} else {
				IJ.log("Could not save coordinates of image");
			}
			return successfullySaved;
		}
	
	} // Runner inner class


	// save current annotations as outlines on the original image
	public static void saveOutlinedImage(ImagePlus image, String outputFileName, String exportFolder){
		OverlayCommands overlayCommandsObj=new OverlayCommands();
		overlayCommandsObj.run("flatten");
		image.hide();
		ImagePlus outlineImp=WindowManager.getCurrentImage();
		image.show();
		if (outlineImp==null || outlineImp.equals(image)){
			// failed to create new flattened image
			IJ.log("Failed to create outlined image");
		} else {

			outlineImp.hide();

			File outDir=new File(exportFolder);
			if (outDir.exists() && outDir.isDirectory()) {
				// folder already exists
			} else {
				outDir.mkdir();
				IJ.log("Created output folder: "+exportFolder);
			}

			
			//IJ.saveAs("png",outputFileName); // current image must not be hidden for this!
			boolean successfullySaved2=IJ.saveAsTiff(outlineImp,outputFileName);
			if (successfullySaved2) {
				IJ.log("Saved outline image: "+outputFileName);
			} else {
				IJ.log("Failed to save outline image: "+outputFileName);
			}

			
			outlineImp.changes=false;
			outlineImp.close();
		}
	}


	// generate class mask as a stack image
		public static ImagePlus createClassMaskStack(int size,int width,int height,int roiCount,int curClassNum, RoiManager manager){

			ImagePlus maskImage2=null;
	        short[] pixels2;
	        pixels2 = new short[size];
	        boolean ok=false;

			manager.runCommand("Deselect all");

			ImageProcessor maskImageProc = new ShortProcessor(width, height, pixels2, null);
	        if (maskImageProc==null) {
	        	IJ.log("could not create a new mask (1)");
	        	return null;
	        }
	        maskImage2 = new ImagePlus("title", maskImageProc);

            ImageStack stack = maskImage2.createEmptyStack();
            try {
            	
            	int color1=1;
            	// start getting the objects from the annotation file
	            for (int s=0; s<roiCount; s++) {
	            	// could already set the slices here instead of creating empty layers
	            	// it needs an "ImageProcessor ip"
	            	pixels2 = new short[size];
	            	ImageProcessor tmpSlice = new ShortProcessor(width, height, pixels2, null);
			        if (tmpSlice==null) {
			        	IJ.log("could not create layer "+String.valueOf(s));
			        	return null;
			        }

			        // get s. instance
					Roi curROI=manager.getRoi(s);
					// see if it belongs to the current class (group)
					if (curROI.getGroup()==curClassNum){
						// set fill value
						tmpSlice.setColor(color1);
						tmpSlice.fill(curROI);
						manager.deselect(curROI);
					}

	            	stack.addSlice(null, tmpSlice);
	            }
            }
            catch(OutOfMemoryError e) {
	            IJ.outOfMemory(maskImage2.getTitle());
	            IJ.log("Out-of-memory error");
	            stack.trim();
	            ok=false;
	        }
	        if (stack.getSize()>1)
	            maskImage2.setStack(null, stack);
	        ok=true;

            if (!ok){
            	maskImage2 = null;
            	IJ.log("Error creating stack image");
            	return null;
            }

			return maskImage2;
		}

		public static ResultsTable createClassCSV(int roiCount,int curClassNum, RoiManager manager){
			// count the number of objects in the current class first --> create the bboxList of this size
			int thisClassCount=0;
			int lineNum=0;
			for (int r=0; r<roiCount; r++) {
				// get r. instance
				Roi curROI=manager.getRoi(r);
				// see if it belongs to the current class (group)
				if (curROI.getGroup()==curClassNum){
					thisClassCount+=1;
				}
			}
			if (thisClassCount==0){
				IJ.log("Class "+String.valueOf(curClassNum)+" contains 0 objects");
				return null;
			}

			// prepare a bbox array for export
			ResultsTable bboxList=new ResultsTable(thisClassCount);
			bboxList.showRowIndexes(false);
			bboxList.showRowNumbers(false);
			// start getting the objects from the annotation file
			for (int r=0; r<roiCount; r++) {
				// get r. instance
				Roi curROI=manager.getRoi(r);
				// see if it belongs to the current class (group)
				if (curROI.getGroup()==curClassNum){
					// get coordinates
					Rectangle bbox=curROI.getBounds();
					bboxList.setValue("x",lineNum,bbox.getX());
					bboxList.setValue("y",lineNum,bbox.getY());
					bboxList.setValue("width",lineNum,bbox.getWidth());
					bboxList.setValue("height",lineNum,bbox.getHeight());
					manager.deselect(curROI);
					lineNum+=1;
				}
			}

			return bboxList;
		}

} //Annotator_ExportFrameNew class
