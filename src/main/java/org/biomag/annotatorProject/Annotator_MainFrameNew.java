package org.biomag.annotatorProject;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.frame.*;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.macro.Functions;
import ij.measure.ResultsTable;
import ij.gui.Roi;
import ij.gui.PolygonRoi;
import ij.plugin.Hotkeys;
import ij.plugin.Selection;
import ij.plugin.OverlayCommands;
import ij.plugin.RoiEnlarger;
import ij.plugin.tool.BrushTool;
import ij.plugin.tool.PlugInTool;
import ij.plugin.Colors;
import ij.plugin.Converter;
import ij.plugin.Thresholder;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.Resizer;
import ij.io.DirectoryChooser;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;
import java.awt.Checkbox;
import java.awt.Button;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;

import java.util.ArrayList;
import java.lang.Math;
import java.util.stream.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.lang.Double;
import java.lang.Integer;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import java.lang.System;
import javax.swing.JComboBox;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.nd4j.enums.ImageResizeMethod;
import org.nd4j.linalg.api.ops.impl.image.ImageResize;
import org.nd4j.linalg.api.ops.impl.image.ResizeBicubic;
import org.nd4j.linalg.api.ops.impl.image.ResizeNearestNeighbor;
import org.datavec.image.transform.ColorConversionTransform;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;

//import BrushToolCustom;
//import ij.plugin.tool.BrushToolCustom;

// for active contour
import com.mathworks.toolbox.javabuilder.*;
//import runSnake2D.*;
import runAC.*;
import com.github.emersonmoretto.*;


public class Annotator_MainFrameNew extends PlugInFrame implements ActionListener,ItemListener{ //,KeyListener

	// main plugin vars
	private Panel panel;
	private int previousID;
	private static Frame instance;

	// controls
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
	private JCheckBox chckbxContourAssist;
	private JCheckBox chckbxStepThroughContours;
	private JCheckBox chckbxShowOverlay;
	private GroupLayout gl_panel_1;
	private JLabel lblCurrentFile;	
	private	JButton buttonPrev;	
	private	JButton buttonNext;
	private JButton buttonOptions;

	// processing vars
	private boolean addAuto;
	private boolean smooth;
	private boolean showCnt;
	private boolean showLbs;
	private boolean contAssist;
	private boolean showOvl;
	private boolean stepCnt;
	private boolean editMode;

	// contour assist and options
	private boolean suggestContourOn;
	private boolean inAssisting;
	private double intensityThreshVal;
	private double intensityThreshValR;
	private double intensityThreshValG;
	private double intensityThreshValB;
	private int distanceThreshVal;
	private int correctionBrushSize;
	private int semanticBrushSize;

	// new image opening vars
	private String destNameRaw;
	private String destName;
	private String destFolder;
	private String selectedAnnotationType;
	private RoiManager manager;
	private boolean started;
	private boolean loadedROI;
	private boolean overlayedROI;
	private boolean overlayedSemantic;
	private Overlay overlaySemantic;
	private String selectedClass;
	private Window imWindow;

	//private FloatPolygon curROI;
	private Roi curROI;
	// the current original image
	private ImagePlus imp;

	// mouse event vars
	private boolean imageIsActive;
	private String prevMouseEvent;

	// overlay vars
	private RoiManager overlayManager;
	private boolean overlayAdded;
	private Color defOverlay;
	private Color currentSelectionColor;
	private String selectedAnnotationColour;
	private String selectedOverlayColour;
	private OverlayCommands overlayCommandsObj;
	// colours
	private String defAnnotCol;
	private String defOvlCol;
	// toolbar to switch current annotation tool
	private Toolbar curToolbar;
	private PlugInTool tool;

	// stepping through image list vars
	private String defDir;
	private String defFile;
	private String defImageJDir;
	private String[] curFileList;
	private int curFileIdx;
	private boolean stepping;
	private boolean finishedSaving;

	private boolean imageNameLabelIsActive;

	// contour assist model folder
	private String modelFolder;

	// time measurement
	private long startTime;
	private long pendingTime;

	// edit mode vars
	private int editROIidx;
	private boolean startedEditing;
	private Roi origEditedROI;
	private float origStrokeWidth;

	// dl4j constants
	public final static String DYNAMIC_LOAD_CLASSPATH = "ND4J_DYNAMIC_LOAD_CLASSPATH";
    public final static String DYNAMIC_LOAD_CLASSPATH_PROPERTY = "org.nd4j.backend.dynamicbackend";
    // contour assist vars (dl4j)
    private ComputationGraph trainedUNetModel;
    private ImageProcessor curPredictionImage;
    private String curPredictionImageName;
    //private ImagePlus curOrigImage;
    private ImageProcessor curOrigImage;
    private Roi invertedROI;
    private double ROIpositionX;
    private double ROIpositionY;
    private int selectedCorrMethod;

    // property config vars
    public Properties props;
    private String configFileName;

    // input args vars
    private Map<String,Object> customizableParams;
    private boolean imageFromArgs;
    private ArrayList<RoiManager> managerList;
    private boolean roisFromArgs;
    private int currentSliceIdx;
    private boolean rememberAnnotType;
    private String[] origMaskFileNames;
    private String[] origImageFileNames;
    private String exportFolderFromArgs;
    private String exportRootFolderFromArgs;
    private String exportClassFolderFromArgs;
    private JCheckBox chckbxClass;
    private boolean classMode;
    private boolean initializedRoisFromArgs;
    private int prevSliceIdx;
    private boolean isProcessingWheelEvent;
    private Timer wheelMovementTimer;
    private int wheelStepCounter;
    private boolean saveOutlines;
    private boolean enableMaskLoad;
    private boolean maskFolderInited;
    private String maskFolderInitedPath;
    private boolean autoMaskLoad;


	// options frame elements:
	private JFrame optionsFrame;
	private Panel optionsPanel;
	private JLabel lblSemancticSegmentation;
	private JLabel lblBrushSize;
	private JTextField semanticBrushSizeField;
	private JSeparator separator;
	private JLabel lblContourAssist;
	private JLabel lblMaxDistance;
	private JTextField assistDistanceField;
	private JLabel lblThresholdgray;
	private JTextField assistThreshGrayField;
	private JLabel lblThresholdrgb;
	private JTextField assistThreshRField;
	private JTextField assistThreshGField;
	private JTextField assistThreshBField;
	private JLabel lblBrushSize_1;
	private JLabel lblpixels;
	private JLabel label;
	private JLabel label_1;
	private JLabel label_2;
	private JLabel label_3;
	private JButton btnOkOptions;
	private JButton btnCancelOptions;
	private JTextField assistBrushSizeField;
	private JLabel lblMethod;
	private JLabel lblUnet;
	private JLabel lblClassic;
	private JSlider methodSlider;
	private JButton buttonQ;


	// class frame elements:
	private JFrame classesFrame;
	private Panel classPanel;
	private JLabel lblClasses;
	private DefaultListModel<String> listModelClasses;
	private JScrollPane scrollPaneClasses;
	private JList<String> classListList;
	private JRadioButton rdbtnColoursR;
	private JRadioButton rdbtnColoursG;
	private JRadioButton rdbtnColoursB;
	private JRadioButton rdbtnColoursC;
	private JRadioButton rdbtnColoursM;
	private JRadioButton rdbtnColoursY;
	private JRadioButton rdbtnColoursO;
	private JRadioButton rdbtnColoursW;
	private JRadioButton rdbtnColoursK;
	private ButtonGroup rdbtnGroup;
	private JButton btnAddClass;
	private JButton btnDeleteClass;
	private JLabel lblCurrentClass;
	private ArrayList<String> classFrameNames;
	private ArrayList<Integer> classFrameColours;
	private boolean startedClassifying;
	private int selectedClassNameNumber;
	private Color selectedClassColourIdx;
	private boolean classListSelectionHappened;
	private ArrayList<Integer> usedClassNameNumbers;
	private JComboBox comboBoxDefaultClass;
	private JLabel lblClassDefault;
	private int defaultClassNumber;
	private Color defaultClassColour;
	private int classNumberCounter;
	private Map<String,Integer> classNameLUT;


	private boolean cancelledSaving;
	//private Object waiter;
	// active contour object collector
	private ACobjectDump acObjects;

	// key event vars
	private KeyEvent lastKey;
	private boolean closeingOnPurpuse;
	//private ImageListenerNew imlisn;
	private boolean isSpaceDown;


	// annot time saving:
	ResultsTable annotTimes;
	int annotCount;
	long lastStartTime;
	private boolean saveAnnotTimes;


	// constructors
	// move most to init fcn below
	// default constructor
	public Annotator_MainFrameNew() {
		super("Annotator_MainFrameNew");
		// just init
		initAnnotatorJ();
	}

	// customizable constructor
	public Annotator_MainFrameNew(Map<String,Object> args) {
		super("Annotator_MainFrameNew");
		// init
		initAnnotatorJ();

		// customize vars
		setCustomizableParamsList();
		parseArgs(args);
	}

	// initialize the main window and config vars
	public void initAnnotatorJ(){
		
		if (instance!=null) {
			instance.toFront();
			return;
		}
		instance = this;
		addKeyListener(IJ.getInstance());

		instance.setTitle("AnnotatorJ");

		// create panel for every component
		setLayout(new FlowLayout());
		panel = new Panel();
		panel.setBackground(SystemColor.control);


		// open, load and save buttons
		btnOpen = new JButton("Open");
		btnOpen.addActionListener(this);
		btnOpen.addKeyListener(IJ.getInstance());
		btnOpen.setToolTipText("Open an image to annotate");
		add(btnOpen);

		btnLoad = new JButton("Load");
		btnLoad.addActionListener(this);
		btnLoad.addKeyListener(IJ.getInstance());
		btnLoad.setToolTipText("Load a previous annotation");
		add(btnLoad);

		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		btnSave.addKeyListener(IJ.getInstance());
		btnSave.setToolTipText("Save current annotation to file");
		add(btnSave);
		
		// load overlay
		btnOverlay = new JButton("Overlay");
		btnOverlay.addActionListener(this);
		btnOverlay.addKeyListener(IJ.getInstance());
		btnOverlay.setToolTipText("Load a different annotation's contours as overlay");
		add(btnOverlay);

		// colour choosing options
		btnColours = new JButton("Colours");
		btnColours.addActionListener(this);
		btnColours.addKeyListener(IJ.getInstance());
		btnColours.setToolTipText("Set colour for annotations or overlay");
		add(btnColours);

		// ROI options
		lblRois = new JLabel("ROIs");
		add(lblRois);


		// checkboxes
		chckbxAddAutomatically = new JCheckBox("Add automatically");
		chckbxAddAutomatically.setToolTipText("Adds contour to annotations without pressing \"t\"");
		chckbxAddAutomatically.addItemListener(this);


		chckbxSmooth = new JCheckBox("Smooth");
		chckbxSmooth.setToolTipText("Applies smoothing to contour");
		chckbxSmooth.addItemListener(this);
		chckbxShowAll = new JCheckBox("Show contours",true);
		chckbxShowAll.addItemListener(this);
		//chckbxShowLabels = new JCheckBox("Show labels");
		//chckbxShowLabels.setEnabled(false);
		chckbxContourAssist = new JCheckBox("Contour assist");
		chckbxContourAssist.addItemListener(this);
		chckbxContourAssist.setToolTipText("Helps fit contour to object boundaries. Press \"q\" to add contour after correction. Press Ctrl+\"delete\" to delete suggested contour. (You must press either before you could continue!)");
		chckbxShowOverlay = new JCheckBox("Show overlay");
		chckbxShowOverlay.setToolTipText("Shows a different annotation's contours overlayed");
		chckbxShowOverlay.addItemListener(this);
		/*
		chckbxStepThroughContours = new JCheckBox("Step through contours");
		chckbxStepThroughContours.setToolTipText("Allows switching between annotated contours with cursor keys");
		chckbxStepThroughContours.addItemListener(this);
		chckbxStepThroughContours.setEnabled(false);
		*/
		chckbxStepThroughContours = new JCheckBox("Edit mode");
		chckbxStepThroughContours.setToolTipText("Allows switching to contour edit mode.\n Select with mouse click, accept with Ctrl+\"q\".");
		chckbxStepThroughContours.addItemListener(this);


		// add class mode option too
		chckbxClass = new JCheckBox("Class mode");
		chckbxClass.setToolTipText("Allows switching to contour classification mode.\n Select with mouse click.");
		chckbxClass.addItemListener(this);

		add(chckbxAddAutomatically);
		add(chckbxSmooth);
		add(chckbxShowAll);
		add(chckbxContourAssist);
		add(chckbxShowOverlay);
		add(chckbxStepThroughContours);
		add(chckbxClass);

		// add options button for contour assist
		buttonOptions=new JButton("...");
		buttonOptions.setToolTipText("Show options");
		buttonOptions.addActionListener(this);
		buttonOptions.addKeyListener(IJ.getInstance());
		add(buttonOptions);


		// add file stepper options
		lblCurrentFile = new JLabel("Current file");
		lblCurrentFile.setText("");
		lblCurrentFile.setToolTipText("Currently opened image");
		
		buttonPrev = new JButton("<");
		buttonPrev.addActionListener(this);
		buttonPrev.addKeyListener(IJ.getInstance());
		buttonPrev.setToolTipText("Open previous image in folder");
		add(buttonPrev);
		buttonPrev.setEnabled(false);
		
		buttonNext = new JButton(">");
		buttonNext.addActionListener(this);
		buttonNext.addKeyListener(IJ.getInstance());
		buttonNext.setToolTipText("Open next image in folder");
		add(buttonNext);
		buttonNext.setEnabled(false);

		// add extra export button to open the exporter
		JButton btnExport = new JButton();//"EXPORT");
		btnExport.setText("[^]");
		btnExport.addActionListener(this);
		btnExport.addKeyListener(IJ.getInstance());
		//Image imgIcon = ImageIO.read(getClass().getResource("smalljavaexport.gif"));
		//btnExport.setIcon(new ImageIcon(imgIcon));
		btnExport.setToolTipText("Export current annotation");
		add(btnExport);


		// create grouplayout structure of elements
		gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addGap(10)
							.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
								.addGroup(gl_panel.createSequentialGroup()
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
										.addGroup(gl_panel.createSequentialGroup()
											.addComponent(lblRois)
											.addGap(390))
										.addGroup(gl_panel.createSequentialGroup()
											.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
												.addComponent(chckbxShowAll)
												.addComponent(chckbxShowOverlay)
												.addGroup(gl_panel.createSequentialGroup()
													.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
														.addGroup(gl_panel.createSequentialGroup()
															.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
																.addComponent(chckbxAddAutomatically)
																.addComponent(chckbxSmooth)
																.addComponent(lblCurrentFile))
															.addGap(29)
															.addComponent(buttonPrev))
														.addComponent(chckbxContourAssist))
													.addPreferredGap(ComponentPlacement.RELATED)
													.addComponent(buttonNext))
												.addComponent(chckbxStepThroughContours))
											.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(btnExport)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(btnSave, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnLoad, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnOpen, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
												.addComponent(btnOverlay, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnColours))))
									.addGap(10))
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(chckbxClass)
									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(buttonOptions)
									.addGap(26)))
							.addGap(0, 0, Short.MAX_VALUE))
				);
				gl_panel.setVerticalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblRois)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(chckbxAddAutomatically)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(chckbxSmooth)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(chckbxShowAll)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_panel.createSequentialGroup()
											.addGap(25)
											.addComponent(chckbxShowOverlay))
										.addComponent(chckbxContourAssist)))
								.addGroup(gl_panel.createSequentialGroup()
									.addComponent(btnOpen)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnLoad)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(btnExport, 0, 0, Short.MAX_VALUE)
										.addComponent(btnSave, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnOverlay)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxStepThroughContours)
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
									.addComponent(buttonOptions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
											.addComponent(buttonNext)
											.addComponent(btnColours))
										.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
											.addComponent(buttonPrev)
											.addComponent(lblCurrentFile)))
									.addContainerGap())
								.addGroup(gl_panel.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(chckbxClass)
									.addContainerGap())))
				);
				panel.setLayout(gl_panel);

		// add the panel after everything else is added
		add(panel);

		// prepare the GUI
		pack();
		GUI.center(this);
		setVisible(true);
		//show();

		// ------------------
		// set default values
		// ------------------

		// this will mark if annotation was started by opening an image
		started=false;
		manager=null;

		// store last space key event
		isSpaceDown=false;

		// checkbox-switchable option states
		addAuto=false;
		smooth=false;
		showCnt=true;
		showLbs=false;
		contAssist=false;
		showOvl=false;
		stepCnt=false;
		editMode=false;
		classMode=false;

		imageIsActive=false;
		loadedROI=false;
		overlayedROI=false;
		overlayedSemantic=false;
		overlaySemantic=null;

		inAssisting=false;

		overlayManager=null;
		overlayAdded=false;

		defOverlay=Color.red;
		currentSelectionColor=Color.yellow;

		selectedAnnotationColour = null;
		selectedOverlayColour = null;

		// default annot colours, can be overwritten in config file
		defAnnotCol="yellow";
		defOvlCol="red";

		Roi.setColor(currentSelectionColor);

		overlayCommandsObj=null;

		// default class vars:
		classFrameNames=null;
		classFrameColours=null;
		startedClassifying=false;
		selectedClassNameNumber=-2;
		selectedClassColourIdx=null;
		classListSelectionHappened=false;
		usedClassNameNumbers=new ArrayList<Integer>();
		defaultClassColour=null;
		defaultClassNumber=-1;
		classNumberCounter=0;
		classNameLUT=new HashMap<String,Integer>();

		// default image folder: ImageJ last image folder
		defFile=null;
		defImageJDir=IJ.getDirectory("default");
		defDir=defImageJDir;

		// empty image list
		curFileList=null; //new String[0];
		curFileIdx = -1;
		stepping=false;

		imageNameLabelIsActive=false;

		suggestContourOn=false;

		// default options for contour assist and semantic annotation
		// can be overwritten in config file
		// threshold of intensity difference for contour assisting region growing
		intensityThreshVal=0.1; //0.1
		intensityThreshValR=0.2;
		intensityThreshValG=0.4;
		intensityThreshValB=0.2;
		// threshold of distance in pixels from the existing contour in assisting region growing
		distanceThreshVal=17;
		// brush sizes
		correctionBrushSize=10;
		semanticBrushSize=50;
		
		props=null;
		
		// unet contour correction model folder:
		String defPluginsPath=IJ.getDirectory("plugins");
		boolean noPluginsPathFound=false;

		// config file default location
		if (defPluginsPath!=null) {
			configFileName=defPluginsPath+File.separator+"models"+File.separator+"AnnotatorJconfig.txt";
		} else {
			configFileName="AnnotatorJconfig.txt";
			noPluginsPathFound=true;
		}


		// read config values
		AnnotatorProperties annotProps=new AnnotatorProperties(this);
		// this sets the annot instance's props var too:
		annotProps.readProps(this,configFileName);
		IJ.log(annotProps.toString());


		// read config values and set vars
		defAnnotCol=props.getProperty("annotationColor");
		defOvlCol=props.getProperty("overlayColor");
		setSelectedColours();

		// set intensities
		intensityThreshVal=Double.parseDouble(props.getProperty("contourAssistThresholdGray"));
		intensityThreshValR=Double.parseDouble(props.getProperty("contourAssistThresholdR"));
		intensityThreshValG=Double.parseDouble(props.getProperty("contourAssistThresholdG"));
		intensityThreshValB=Double.parseDouble(props.getProperty("contourAssistThresholdB"));
		// set distance option
		distanceThreshVal=Integer.parseInt(props.getProperty("contourAssistMaxDistance"));
		// set brush sizes
		correctionBrushSize=Integer.parseInt(props.getProperty("contourAssistBrushsize"));
		semanticBrushSize=Integer.parseInt(props.getProperty("semanticBrushSize"));

		//buttonOptions.setVisible(false);
		buttonOptions.setEnabled(false);
		
		// see if the model folder was found
		// if not, pop up a dialog window
		boolean foundModelFolder=false;
		
		boolean propModelFolderPassed=true;
		String propModelFolder=props.getProperty("modelFolder");
		String propModelJson=props.getProperty("modelJsonFile");
		IJ.log("loaded config model folder: "+propModelFolder);
		if (propModelFolder.equals("") || propModelFolder==null) {
			// no model folder set yet, use the default plugins folder or fall back to open dialog if not found either
		} else {
			// model folder set in config, try to use it:
			File fm = new File(propModelFolder+File.separator+propModelJson);
			if(fm.exists() && !fm.isDirectory()){
				propModelFolderPassed=true;
				//defPluginsPath=propModelFolder;
				IJ.log("json exists");
			}
			else {
				propModelFolderPassed=false;
				IJ.log("json doesnt exist");
			}
		}

		File fm2 = new File(defPluginsPath+File.separator+"models"+File.separator+propModelJson);
		
		if(defPluginsPath==null || !propModelFolderPassed || (!(fm2.exists() && !fm2.isDirectory()) && propModelFolderPassed && propModelFolder.equals(""))) {
			// cannot find default imagej plugin path

			// open folder dialog
			JFileChooser chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File(defDir));
		    chooser.setDialogTitle("Select model folder");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);
		    int returnVal = chooser.showOpenDialog(null);

		    if(returnVal == JFileChooser.APPROVE_OPTION) {
	            defPluginsPath=chooser.getSelectedFile().getPath();

	            // check if this path contains the correct model files
	            File f = new File(defPluginsPath+File.separator+propModelJson); //"model_real.json");
				if(f.exists() && !f.isDirectory()) {
				    // model file path is correct, can continue
				    IJ.log("Opened model: "+defPluginsPath);
				    foundModelFolder=true;
				} else {
					// query again

					while (!foundModelFolder){
						chooser = new JFileChooser();
					    chooser.setCurrentDirectory(new java.io.File(defDir));
					    chooser.setDialogTitle("Select model folder");
					    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					    chooser.setAcceptAllFileFilterUsed(false);
					    returnVal = chooser.showOpenDialog(null);

					    if(returnVal == JFileChooser.APPROVE_OPTION) {
				            defPluginsPath=chooser.getSelectedFile().getPath();

				            // check if this path contains the correct model files
				            f = new File(defPluginsPath+File.separator+propModelJson);//"model_real.json");
							if(f.exists() && !f.isDirectory()) {
							    // model file path is correct, can continue
							    IJ.log("Opened model: "+defPluginsPath);
							    foundModelFolder=true;
							}
						} else if (returnVal==JFileChooser.CANCEL_OPTION) {
					    	IJ.log("canceled model folder open");
					    	return;
						} else {
					    	IJ.log("Failed to open model folder");
							MessageDialog failedFolderOpenMsg=new MessageDialog(instance,
			                 "Error",
			                 "Could not open folder");
							return;
					    }
					}

				}

				modelFolder=defPluginsPath;

				
		    } else if (returnVal==JFileChooser.CANCEL_OPTION) {
		    	IJ.log("canceled model folder open");
		    	return;
			} else {
		    	IJ.log("Failed to open model folder");
				MessageDialog failedFolderOpenMsg=new MessageDialog(instance,
                 "Error",
                 "Could not open folder");
				return;
		    }

		} else {
			if (propModelFolderPassed && !propModelFolder.equals("")) {
				//defPluginsPath=propModelFolder;
				modelFolder=propModelFolder;
			} else {
				defPluginsPath=defPluginsPath+File.separator+"models";
				modelFolder=defPluginsPath;
			}
		}
		//modelFolder=defPluginsPath;

		// TODO: this configFileName will not exist if we use the config modelFolder prop!

		if (noPluginsPathFound) {
			//configFileName=defPluginsPath+File.separator+"AnnotatorJconfig.txt";
			configFileName=modelFolder+File.separator+"AnnotatorJconfig.txt";
		}
		

		// read the props again in case the path was changed
		annotProps.readProps(this,configFileName);
		IJ.log(annotProps.toString());

		// read config values and set vars
		defAnnotCol=props.getProperty("annotationColor");
		defOvlCol=props.getProperty("overlayColor");

		setSelectedColours();

		intensityThreshVal=Double.parseDouble(props.getProperty("contourAssistThresholdGray"));
		intensityThreshValR=Double.parseDouble(props.getProperty("contourAssistThresholdR"));
		intensityThreshValG=Double.parseDouble(props.getProperty("contourAssistThresholdG"));
		intensityThreshValB=Double.parseDouble(props.getProperty("contourAssistThresholdB"));

		distanceThreshVal=Integer.parseInt(props.getProperty("contourAssistMaxDistance"));

		correctionBrushSize=Integer.parseInt(props.getProperty("contourAssistBrushsize"));
		semanticBrushSize=Integer.parseInt(props.getProperty("semanticBrushSize"));

		// set current tool to freehand roi selection tool
		curToolbar=Toolbar.getInstance();
		curToolbar.setTool(Toolbar.FREEROI);

		tool=null;

		addMouseListener(IJ.getInstance());


		// set default edit vars
		editROIidx=-1;
		startedEditing=false;
		origEditedROI=null;
		origStrokeWidth=0;

		// set default contour assist vars
		trainedUNetModel=null;
		curPredictionImage=null;
		curPredictionImageName=null;
		curOrigImage=null;
		invertedROI=null;
		ROIpositionX=0;
		ROIpositionY=0;

		selectedCorrMethod=0; // Unet

		// set correction from config method too
		String configCorrMethod=props.getProperty("contourAssistMethod");
		String[] corrMethods=new String[3];
		corrMethods[0]="unet";
		corrMethods[1]="u-net";
		corrMethods[2]="classical";

		if (Arrays.asList(corrMethods).contains(configCorrMethod.toLowerCase())) {
			for (int mi=0; mi<3; mi++) {
				if (configCorrMethod.equals(corrMethods[mi])) {
					// found it
					if (mi==0 || mi==1) {
						selectedCorrMethod=0; // Unet
					} else if (mi==2) {
						selectedCorrMethod=1; // classical
					}
					break;
				}
			}
		}

		// set annot type vars
		saveAnnotTimes=false;
		selectedAnnotationType=null;
		rememberAnnotType=false;
		saveOutlines=false;
		enableMaskLoad=false;
		maskFolderInited=false;
		maskFolderInitedPath=null;
		autoMaskLoad=false;
		String saveAnnotTimesString=props.getProperty("saveAnnotTimes");
		String defaultAnnotTypeString=props.getProperty("defaultAnnotType");
		String rememberAnnotTypeString=props.getProperty("rememberAnnotType");
		String saveOutlinesString=props.getProperty("saveOutlines");
		String enableMaskLoadString=props.getProperty("enableMaskLoad");
		String autoMaskLoadString=props.getProperty("autoMaskLoad");

		String[] booleans=new String[6];
		booleans[0]="no";
		booleans[1]="false";
		booleans[2]="0";
		booleans[3]="yes";
		booleans[4]="true";
		booleans[5]="1";

		String[] annotTypes=new String[5];
		annotTypes[0]="instance";
		annotTypes[1]="semantic";
		annotTypes[2]="bounding box";
		annotTypes[3]="boundingbox";
		annotTypes[4]="bbox";

		if (Arrays.asList(booleans).contains(saveAnnotTimesString.toLowerCase())) {
			for (int mi=0; mi<booleans.length; mi++) {
				if (saveAnnotTimesString.equals(booleans[mi])) {
					// found it
					if (mi==0 || mi==1 || mi==2) {
						saveAnnotTimes=false;
					} else if (mi==3 || mi==4 || mi==5) {
						saveAnnotTimes=true;
					}
					break;
				}
			}
		}

		if (Arrays.asList(annotTypes).contains(defaultAnnotTypeString.toLowerCase())) {
			for (int mi=0; mi<annotTypes.length; mi++) {
				if (defaultAnnotTypeString.equals(annotTypes[mi])) {
					// found it
					if (mi==0) {
						selectedAnnotationType="instance";
					} else if (mi==1) {
						selectedAnnotationType="semantic";
					} else if (mi==2 || mi==3 || mi==4) {
						selectedAnnotationType="bounding box";
					}
					break;
				}
			}
		}

		if (Arrays.asList(booleans).contains(rememberAnnotTypeString.toLowerCase())) {
			for (int mi=0; mi<booleans.length; mi++) {
				if (rememberAnnotTypeString.equals(booleans[mi])) {
					// found it
					if (mi==0 || mi==1 || mi==2) {
						rememberAnnotType=false;
					} else if (mi==3 || mi==4 || mi==5) {
						rememberAnnotType=true;
					}
					break;
				}
			}
		}

		if (rememberAnnotType && selectedAnnotationType!=null){
			// save to properties
			SaveNewProp("defaultAnnotType", selectedAnnotationType);
		}

		if (Arrays.asList(booleans).contains(saveOutlinesString.toLowerCase())) {
			for (int mi=0; mi<booleans.length; mi++) {
				if (saveOutlinesString.equals(booleans[mi])) {
					// found it
					if (mi==0 || mi==1 || mi==2) {
						saveOutlines=false;
					} else if (mi==3 || mi==4 || mi==5) {
						saveOutlines=true;
					}
					break;
				}
			}
		}
		
		if (Arrays.asList(booleans).contains(enableMaskLoadString.toLowerCase())) {
			for (int mi=0; mi<booleans.length; mi++) {
				if (enableMaskLoadString.equals(booleans[mi])) {
					// found it
					if (mi==0 || mi==1 || mi==2) {
						enableMaskLoad=false;
					} else if (mi==3 || mi==4 || mi==5) {
						enableMaskLoad=true;
					}
					break;
				}
			}
		}
		
		if (Arrays.asList(booleans).contains(autoMaskLoadString.toLowerCase())) {
			for (int mi=0; mi<booleans.length; mi++) {
				if (autoMaskLoadString.equals(booleans[mi])) {
					// found it
					if (mi==0 || mi==1 || mi==2) {
						autoMaskLoad=false;
					} else if (mi==3 || mi==4 || mi==5) {
						autoMaskLoad=true;
					}
					break;
				}
			}
		}

		



		cancelledSaving=false;
		//waiter=new Object();
		acObjects=null;

		imageFromArgs=false;
		currentSliceIdx=-1;
		prevSliceIdx=-1;
		ArrayList<RoiManager> managerList=new ArrayList<RoiManager>();
		roisFromArgs=false;
		origMaskFileNames=null;
		origImageFileNames=null;
		exportFolderFromArgs=null;
		exportRootFolderFromArgs=null;
		exportClassFolderFromArgs=null;
		initializedRoisFromArgs=false;
		isProcessingWheelEvent=false;
		wheelStepCounter=1;

		closeingOnPurpuse=false;

		// annot time log vars
		annotCount=0;
		lastStartTime=System.nanoTime();

		// log window to display various process info
		Window logWindow=WindowManager.getWindow("Log");
		if (logWindow!=null) {
			logWindow.setVisible(true);
		}


		// load model at startup to save time later
		String modelJsonFile=modelFolder+File.separator+propModelJson; //"model_real.json";
		String modelWeightsFile=modelFolder+File.separator+props.getProperty("modelWeightsFile"); //"model_real_weights.h5";
		String modelFullFile=modelFolder+File.separator+props.getProperty("modelFullFile"); //"model_real.hdf5";
		//trainedUNetModel=loadUNetModel(modelJsonFile,modelWeightsFile);

		// load the model on a new thread in the background:
		ModelLoader ModelLoaderObj=null;
		File fx = new File(modelWeightsFile);
		if(fx.exists() && !fx.isDirectory()) {
			// both json config and weight h5 files exits
			ModelLoaderObj = new ModelLoader(modelJsonFile,modelWeightsFile,this);
		} else {
			// cannot find weights file, try to use combined model file
			ModelLoaderObj = new ModelLoader(null,modelFullFile,this);
		}

        Thread t = new Thread(ModelLoaderObj);
        t.start();
        // set the trained model to this annotator instance's own trainedUnetModel var in the ModelLoaderObj function instead
        //trainedUNetModel=ModelLoaderObj.getLoadedModel();

		// for autosave
		startTime = System.currentTimeMillis();
	}


	// sets the annotation and overlay colours according to config vars
	public void setSelectedColours(){
		switch (defAnnotCol){
			case "yellow":
				currentSelectionColor=Color.yellow;
				break;
			case "black":
				currentSelectionColor=Color.black;
				break;
			case "blue":
				currentSelectionColor=Color.blue;
				break;
			case "cyan":
				currentSelectionColor=Color.cyan;
				break;
			case "green":
				currentSelectionColor=Color.green;
				break;
			case "magenta":
				currentSelectionColor=Color.magenta;
				break;
			case "orange":
				currentSelectionColor=Color.orange;
				break;
			case "red":
				currentSelectionColor=Color.red;
				break;
			case "white":
				currentSelectionColor=Color.white;
				break;
			default:
				currentSelectionColor=Color.yellow;
				break;
		}

		switch (defOvlCol){
			case "yellow":
				defOverlay=Color.yellow;
				break;
			case "black":
				defOverlay=Color.black;
				break;
			case "blue":
				defOverlay=Color.blue;
				break;
			case "cyan":
				defOverlay=Color.cyan;
				break;
			case "green":
				defOverlay=Color.green;
				break;
			case "magenta":
				defOverlay=Color.magenta;
				break;
			case "orange":
				defOverlay=Color.orange;
				break;
			case "red":
				defOverlay=Color.red;
				break;
			case "white":
				defOverlay=Color.white;
				break;
			default:
				defOverlay=Color.red;
				break;
		}

		Roi.setColor(currentSelectionColor);
	}


	// parse a string to color
	Color string2colour(String colorString){
		Color outColour=new Color(0,0,0);
		switch (colorString){
			case "yellow":
				outColour=Color.yellow;
				break;
			case "black":
				outColour=Color.black;
				break;
			case "blue":
				outColour=Color.blue;
				break;
			case "cyan":
				outColour=Color.cyan;
				break;
			case "green":
				outColour=Color.green;
				break;
			case "magenta":
				outColour=Color.magenta;
				break;
			case "orange":
				outColour=Color.orange;
				break;
			case "red":
				outColour=Color.red;
				break;
			case "white":
				outColour=Color.white;
				break;
			default:
				outColour=Color.yellow;
				break;
		}

		return outColour;
	}


	// fill customizable params list
	public void setCustomizableParamsList(){
		customizableParams=new HashMap<String,Object>();
		// checkboxes
		customizableParams.put("addAuto",false);
		customizableParams.put("smooth",false);
		customizableParams.put("showCnt",false);
		customizableParams.put("contAssist",false);
		customizableParams.put("showOvl",false);
		customizableParams.put("editMode",false);
		// some defaults we allow to be set from external functions
		customizableParams.put("curPredictionImage",null);
		customizableParams.put("curPredictionImageName",null);
		customizableParams.put("defDir",null);
		customizableParams.put("curFileList",null);
		customizableParams.put("curFileIdx",0);
		customizableParams.put("loadedROI",false);

		// some defaults not yet implemented
		// TODO: put a remember checkbox and ask only in first time use
		customizableParams.put("defaultAnnotType","instance");
		customizableParams.put("rememberAnnotType",false);


		// input image and rois
		customizableParams.put("x_inputImage2open",null);
		customizableParams.put("z_inputROIs2open",null);
		customizableParams.put("z_origMaskFileNames",null);
		customizableParams.put("exportFolderFromArgs",null);
		customizableParams.put("exportRootFolderFromArgs",null);
		customizableParams.put("exportClassFolderFromArgs",null);
		customizableParams.put("z_origImageFileNames",null);
	}

	// parse the input args to vars
	public void parseArgs(Map<String,Object> args){
		if (args.isEmpty()){
			IJ.log("No input arguments found");
			return;
		}
		Map<String,Object> argsTreeMap = new TreeMap<String,Object>(args); 

		// loop through the args
		// from https://stackoverflow.com/questions/1066589/iterate-through-a-hashmap
		Iterator it = argsTreeMap.entrySet().iterator(); // was args.
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        IJ.log(pair.getKey() + " = " + pair.getValue());
	        it.remove(); // avoids a ConcurrentModificationException

	        // find key in possible keys list
	        if (customizableParams.containsKey(pair.getKey())){
	        	// search keys in list and validate values
	        	switch ((String)pair.getKey()) {
	        		case "addAuto":
	        			if (pair.getValue() instanceof Boolean){
	        				addAuto=(boolean) pair.getValue();
	        				if (addAuto){
		        				chckbxAddAutomatically.setEnabled(true);
		        				chckbxAddAutomatically.setSelected(true);

		        				curToolbar.setTool(Toolbar.FREEROI);

					  			editMode=false;
					  			chckbxStepThroughContours.setSelected(false);
					  			chckbxStepThroughContours.setEnabled(false);

					  			chckbxContourAssist.setSelected(false);
								chckbxContourAssist.setEnabled(false);
								contAssist=false;

								chckbxClass.setSelected(false);
								chckbxClass.setEnabled(false);
								classMode=false;
					  		} else {
					  			// do nothing, this is the default
					  			chckbxAddAutomatically.setSelected(false);
					  			chckbxStepThroughContours.setEnabled(true);
  								chckbxClass.setEnabled(true);
  								chckbxContourAssist.setEnabled(true);
					  		}
	        			}
	        			else
	        				IJ.log("Invalid value parsed for variable \"addAuto\". Must be boolean.");
	        			break;

	        		case "smooth":
	        			if (pair.getValue() instanceof Boolean){
	        				smooth=(boolean) pair.getValue();
	        				if (smooth){
		        				chckbxSmooth.setEnabled(true);
		        				chckbxSmooth.setSelected(true);
		        			} else {
		        				chckbxSmooth.setSelected(false);
		        			}
		        		}
	        			else
	        				IJ.log("Invalid value parsed for variable \"smooth\". Must be boolean.");
	        			break;

	        		case "showCnt":
	        			if (pair.getValue() instanceof Boolean){
	        				showCnt=(boolean) pair.getValue();
	        				if (showCnt){
		        				chckbxShowAll.setEnabled(true);
		        				chckbxShowAll.setSelected(true);
		        			} else {
		        				chckbxShowAll.setSelected(false);
		        			}
		        		}
	        			else
	        				IJ.log("Invalid value parsed for variable \"showCnt\". Must be boolean.");
	        			break;
	        		case "contAssist":
	        			if (pair.getValue() instanceof Boolean){
	        				contAssist=(boolean) pair.getValue();
	        				if (contAssist){
	        					chckbxContourAssist.setEnabled(true);
	        					chckbxContourAssist.setSelected(true);
					  			
	        					addAuto=false;
					  			chckbxAddAutomatically.setSelected(false);
					  			chckbxAddAutomatically.setEnabled(false);

					  			curToolbar.setTool(Toolbar.FREEROI);

					  			editMode=false;
					  			chckbxStepThroughContours.setSelected(false);
					  			chckbxStepThroughContours.setEnabled(false);

					  			chckbxClass.setSelected(false);
								chckbxClass.setEnabled(false);
								classMode=false;
	        				} else {
	        					chckbxContourAssist.setSelected(false);

	        					chckbxAddAutomatically.setEnabled(true);
  								chckbxStepThroughContours.setEnabled(true);
  								chckbxClass.setEnabled(true);
	        				}
	        			}
	        			else
	        				IJ.log("Invalid value parsed for variable \"contAssist\". Must be boolean.");
	        			break;
	        		case "showOvl":
	        			if (pair.getValue() instanceof Boolean){
	        				showOvl=(boolean) pair.getValue();
	        				if (showOvl){
		        				chckbxShowOverlay.setEnabled(true);
		        				chckbxShowOverlay.setSelected(true);
		        			} else {
		        				chckbxShowOverlay.setSelected(false);
		        			}
	        			}
	        			else
	        				IJ.log("Invalid value parsed for variable \"showOvl\". Must be boolean.");
	        			break;
	        		case "editMode":
	        			if (pair.getValue() instanceof Boolean){
	        				editMode=(boolean) pair.getValue();
	        				if (editMode){
		        				chckbxStepThroughContours.setEnabled(true);
		        				chckbxStepThroughContours.setSelected(true);

		        				curToolbar.setTool(Toolbar.FREEROI);

		        				// disable automatic adding to list and contour assist while editing
  								IJ.log("Switching automatic adding and contour assist off");
					  			addAuto=false;
					  			chckbxAddAutomatically.setSelected(false);
					  			chckbxAddAutomatically.setEnabled(false);
					  			contAssist=false;
					  			chckbxContourAssist.setSelected(false);
					  			chckbxContourAssist.setEnabled(false);

					  			chckbxClass.setSelected(false);
								chckbxClass.setEnabled(false);
								classMode=false;
					  		} else {
					  			// do nothing, this is the default
					  			chckbxStepThroughContours.setSelected(false);
					  			chckbxAddAutomatically.setEnabled(true);
	  							chckbxContourAssist.setEnabled(true);
	  							chckbxClass.setEnabled(true);
					  		}
					  	}
	        			else
	        				IJ.log("Invalid value parsed for variable \"editMode\". Must be boolean.");
	        			break;


	        		// custom input image & ROI import
	        		case "x_inputImage2open":
	        			// set the input image from the param
	        			if (pair.getValue() instanceof String){
	        				// file path, open it
	        				/*
	        				File f = new File(pair.getValue());
	        				destFolder=f.getPath();
							destNameRaw=f.getName();
							defDir=destFolder;
							defFile=destNameRaw;
							curPredictionImageName=defFile;
							curPredictionImage=null;
							curOrigImage=null;

	        				Opener opener2=new Opener();
							opener2.open(destFolder+File.separator+destNameRaw);
							//destFolder=opener.getDir();
							IJ.log("Opened file: "+destNameRaw);
							*/
							File f = new File((String) pair.getValue());
	        				defDir=f.getPath();
							defFile=f.getName();
							destFolder=defDir;
							new Runner("Open", null);


	        			} else if (pair.getValue() instanceof ImagePlus) {
	        				// already read IJ image object, set vars
	        				// can contain a 2D image as ImageProcessor or a 3D image stack as ImageStack
	        				imp=(ImagePlus) pair.getValue();
	        				imageFromArgs=true;
	        				new Runner("Open", imp);
	        			} else {
	        				IJ.log("Invalid value parsed for variable \"x_inputImage2open\". Must be either String or ImagePlus.");
	        			}
	        			break;

	        		case "z_inputROIs2open":
	        			// set the input ROIs from the param
	        			if (pair.getValue() instanceof RoiManager){
	        				// only 1 manager object with ROIs, set it
	        				roisFromArgs=true;
	        				initializedRoisFromArgs=false;
	        				//manager=(RoiManager) pair.getValue();
	        				updateROImanager((RoiManager) pair.getValue(),showCnt); // also display the rois if checked
	        				// TODO: check what else to set here
	        				initializedRoisFromArgs=true;
	        			} else if (pair.getValue() instanceof ArrayList<?>){
	        				// multiple ROIs to be opened
	        				roisFromArgs=true;
	        				initializedRoisFromArgs=false;
	        				// TODO
	        				managerList=(ArrayList<RoiManager>)pair.getValue();
	        				//manager=managerList.get(0);
	        				/*
	        				for (int k=0; k<managerList.size(); k++) {
		        				//TODO!!!!
		        				//manager=managerList.get(k);
		        			}
		        			*/
		        			//debug: comment the next line out

		        			// collect class info from loaded rois
		        			classFrameNames=new ArrayList<String>();
							classFrameColours=new ArrayList<Integer>();
		        			RoiManager tmpRoiManager=new RoiManager(false);
		        			for (int k=0; k<managerList.size(); k++) {
		        				tmpRoiManager=managerList.get(k);
		        				int tmpCount=tmpRoiManager.getCount();
		        				for (int r=0; r<tmpCount; r++){
		        					int tmpGroup=tmpRoiManager.getRoi(r).getGroup();
		        					String tmpGroupName="Class_"+String.format("%02d",tmpGroup);
		        					//debug:
		        					//IJ.log(tmpGroupName);
		        					if (tmpGroup>0 && !classFrameNames.contains(tmpGroupName)){
		        						classFrameNames.add(tmpGroupName);
		        						//int tmpGroupColourIdx=getClassColourIdx(tmpRoiManager.getRoi(r).getStrokeColor());
		        						//Color tmpColour=null;
		        						//tmpColour=tmpRoiManager.getRoi(r).getFillColor();
		        						//if (tmpColour==null)
		        							Color tmpColour=tmpRoiManager.getRoi(r).getStrokeColor();
		        						//Color tmpColour2=new Color(tmpColour.getRed(),tmpColour.getGreen(),tmpColour.getBlue());
		        						int tmpGroupColourIdx=getClassColourIdx(tmpColour); //tmpColour2);
		        						if (tmpGroupColourIdx==-1)
		        							tmpGroupColourIdx=0;

		        						// assign a free colour to the new class
		        						if (!classFrameColours.contains(tmpGroupColourIdx))
		        							classFrameColours.add(tmpGroupColourIdx);
		        						else {
		        							for (int i=0;i<8;i++){
												if (!classFrameColours.contains(i)){
													// found first free colour, take it
													classFrameColours.add(i);
													break;
												}
											}
		        						}
		        						
		        						if (selectedClassNameNumber<0){
		        							selectedClassNameNumber=tmpGroup;
		        						}
		        						//debug:
		        						IJ.log(">>> import: added class '"+tmpGroupName+"' with colour '"+tmpGroupColourIdx+"'");
		        					}
		        				}
		        			}

		        			///*
		        			// colour import doesn't work, repaint all contours to the "imported" colours
		        			for (int k=0; k<managerList.size(); k++) {
		        				tmpRoiManager=managerList.get(k);
		        				int tmpCount=tmpRoiManager.getCount();
		        				for (int r=0; r<tmpCount; r++){
		        					Roi tmpROI=tmpRoiManager.getRoi(r);
		        					int tmpGroup=tmpROI.getGroup();
		        					if (tmpGroup>0){
		        						int tmpIdx=classFrameColours.get(classFrameNames.indexOf("Class_"+String.format("%02d",tmpGroup)));
		        						//IJ.log("group: "+String.valueOf(tmpGroup)+"\t|\ttmpIdx: "+String.valueOf(tmpIdx));
		        						tmpROI.setStrokeColor(getClassColour(tmpIdx));
		        						tmpRoiManager.setRoi(tmpROI,r);
		        					} else {
		        						tmpROI.setStrokeColor(currentSelectionColor);
		        					}
		        				}
		        				managerList.set(k,tmpRoiManager);
		        			}
		        			//*/

		        			if (classFrameNames.size()==0 && classFrameColours.size()==0){
		        				// no class info was found in the rois
		        				classFrameNames=null;
								classFrameColours=null;
		        			}
		        			startedClassifying=true;

		        			//debug:
		        			IJ.log("initial ROI manager from args input, count: "+String.valueOf(managerList.get(0).getCount()));
	        				updateROImanager(managerList.get(0),showCnt); // also display the rois if checked
	        				initializedRoisFromArgs=true;
	        			} else {
	        				IJ.log("Invalid value parsed for variable \"z_inputROIs2open\". Must be either RoiManager or ArrayList<RoiManager>.");
	        			}
	        			break;

	        		case "defaultAnnotType":
	        			// set the annot type
	        			if (pair.getValue() instanceof String) {
	        				String[] possibleAnnotTypes=new String[3];
	        				possibleAnnotTypes[0]="instance";
	        				possibleAnnotTypes[1]="semantic";
	        				possibleAnnotTypes[2]="bounding box";
	        				if (Arrays.asList(possibleAnnotTypes).contains((String)pair.getValue())){
	        					// valid annot type, set it
	        					selectedAnnotationType=(String)pair.getValue();
	        				} else {
	        					IJ.log("Invalid value \""+(String)pair.getValue()+"\" parsed for variable \"defaultAnnotType\". Must be one of the following Strings: {\"instance\", \"semantic\", \"bounding box\"}.");
	        				}
	        			} else {
	        				IJ.log("Invalid value parsed for variable \"defaultAnnotType\". Must be one of the following Strings: {\"instance\", \"semantic\", \"bounding box\"}.");
	        			}
	        			break;

	        		case "rememberAnnotType":
	        			// set the annot type remember value
	        			if (pair.getValue() instanceof Boolean) {
	        				rememberAnnotType=(boolean) pair.getValue();
	        			} else
	        				IJ.log("Invalid value parsed for variable \"rememberAnnotType\". Must be boolean.");
	        			break;

	        		case "z_origMaskFileNames":
	        			// set the input ROI masks original names for saving as such
	        			if (pair.getValue() instanceof String[]) {
	        				origMaskFileNames=(String[]) pair.getValue();
	        			} else
	        				IJ.log("Invalid value parsed for variable \"z_origMaskFileNames\". Must be String array.");
	        			break;

	        		case "z_origImageFileNames":
	        			// set the input original file names for saving in output text file
	        			if (pair.getValue() instanceof String[]) {
	        				origImageFileNames=(String[]) pair.getValue();
	        			} else
	        				IJ.log("Invalid value parsed for variable \"z_origImageFileNames\". Must be String array.");
	        			break;

	        		case "exportFolderFromArgs":
	        			// set the export folder in case input rois are also coming from args to save there
	        			if (pair.getValue() instanceof String) {
	        				exportFolderFromArgs=(String)pair.getValue();
	        			} else
	        				IJ.log("Invalid value parsed for variable \"exportFolderFromArgs\". Must be String.");
	        			break;

	        		case "exportRootFolderFromArgs":
	        			// set the export root folder in case other inputs are also parsed
	        			if (pair.getValue() instanceof String){
	        				exportRootFolderFromArgs=(String)pair.getValue();
	        			} else
	        				IJ.log("Invalid value parsed for variable \"exportRootFolderFromArgs\". Must be String.");
	        			break;

	        		case "exportClassFolderFromArgs":
	        			// set the export class folder
	        			if (pair.getValue() instanceof String){
	        				exportClassFolderFromArgs=(String)pair.getValue();
	        			} else
	        				IJ.log("Invalid value parsed for variable \"exportClassFolderFromArgs\". Must be String.");
	        			break;

	        		default:
	        			IJ.log("Cannot find the provided key \""+pair.getKey()+"\" in the list of valid keys:");
	        			IJ.log("addAuto");
	        			IJ.log("smooth");
	        			IJ.log("showCnt");
	        			IJ.log("contAssist");
	        			IJ.log("showOvl");
	        			IJ.log("editMode");
	        			IJ.log("curPredictionImage");
	        			IJ.log("curPredictionImageName");
	        			IJ.log("defDir");
	        			IJ.log("curFileList");
	        			IJ.log("curFileIdx");
	        			IJ.log("loadedROI");
	        			IJ.log("defaultAnnotType");
	        			IJ.log("rememberAnnotType");  
	        			IJ.log("z_inputROIs2open");
	        			IJ.log("x_inputImage2open");
	        			IJ.log("z_origMaskFileNames");
	        			IJ.log("z_origImageFileNames");
	        			IJ.log("exportFolderFromArgs");
	        			IJ.log("exportRootFolderFromArgs");
	        			IJ.log("exportClassFolderFromArgs");
	        			break;
	        	}
	        } else {
	        	IJ.log(">>>> Unrecongnized key \""+pair.getKey()+"\"");
	        	continue;
	        }
	    }
	}


	public void updateROImanager(RoiManager baseROImanager,boolean display){
		// close all instances of roi manager opened before continue
		manager=RoiManager.getInstance();
		if (manager!=null){
			manager.runCommand("Show None");
			manager.close();
			manager=RoiManager.getInstance();
			while (manager!=null){
				manager.runCommand("Show None");
				manager.close();
				manager=RoiManager.getInstance();
			}
		}

		if (baseROImanager==null){
			IJ.log("prev ROI manager is null");
			manager=new RoiManager();
			return;
		}
		int tmpRoiCount=baseROImanager.getCount();
		baseROImanager.runCommand("Show None");
		//debug:
		IJ.log("prev ROI count: "+String.valueOf(tmpRoiCount));

		//if (baseROIs==null || baseROIs.length==0 || baseROIs.length!=tmpRoiCount) {
		if (tmpRoiCount==0) {
			//debug:
			IJ.log("FAIL: could not find ROIs in the previous ROI manager");
			//return;
		}

		//Roi[] baseROIs=baseROImanager.getRoisAsArray(); 

		// first find all rois and their original names
		Roi[] baseROIs=new Roi[tmpRoiCount];
		String[] baseROInames=new String[tmpRoiCount];
		for (int c=0; c<tmpRoiCount; c++){
			if (baseROImanager.getRoi(c)==null) { //(baseROIs[c]==null) {
				IJ.log(String.valueOf(c)+". ROI is null");
				continue;
			} else {
				baseROIs[c]=baseROImanager.getRoi(c);
				baseROInames[c]=baseROImanager.getName(c);
			}
		}

		

		// close it again to be sure
		if (manager!=null)
			manager.close();
		
		if (imp==null){
			imp=WindowManager.getCurrentImage();
			if (imp==null){
				IJ.log("no image is opened, opening a dummy");
				imp=new ImagePlus();
			}
		}

		manager=new RoiManager();
		
		for (int c=0; c<tmpRoiCount; c++){
			if (baseROIs[c]==null) { //(baseROImanager.getRoi(c)==null) { //(baseROIs[c]==null) {
				IJ.log(String.valueOf(c)+". ROI is null");
				continue;
			}
			//manager.addRoi(baseROIs[c]); // hopefully this conversves the name of the roi (k)

			//Roi cROI=baseROImanager.getRoi(c);
			//cROI.setName(baseROImanager.getName(c));
			Roi cROI=baseROIs[c];
			if (cROI==null)
				IJ.log("cROI is null...");
			
			cROI.setName(baseROInames[c]);
			
			//imp.setRoi(cROI);

			// check if default class is set & this ROI is unclassified
			if (defaultClassNumber>0 && (cROI.getGroup()==-1 || cROI.getGroup()==0)){
				// assign default class & colour
				cROI.setGroup(defaultClassNumber);
				cROI.setStrokeColor(defaultClassColour);
			} else if (cROI.getGroup()>0) {
				// has a class, refresh its colour
				int cNumber=cROI.getGroup();
				String selectedClassNameVar="Class_"+String.format("%02d",cNumber);
				if (classFrameNames.indexOf(selectedClassNameVar)>=0) {
					int tmpIdx=classFrameColours.get(classFrameNames.indexOf(selectedClassNameVar));
					cROI.setStrokeColor(getClassColour(tmpIdx));
				}
			}
			
			if (manager==null)
				IJ.log("manager is null...");
			
			int tmpCountBefore=manager.getCount();
			//manager.runCommand("Add");
			manager.addRoi(cROI);
			if (manager.getCount()!=tmpCountBefore+1){
				// failed to add current roi to the manager
				IJ.log("Failed adding ROI #"+String.valueOf(c)+" to the current manager in updateROImanager() fcn");
			}
		}
		int newRoiCount=manager.getCount();
		IJ.log("new ROI count: "+String.valueOf(newRoiCount));
		if (display)
			manager.runCommand("Show All");
	}


    // ---- processMouseEvent fcn was here -------

	// listen to key presses
	///*

	// --------------------
	// key event listener
	// --------------------
	public void checkKeyEvents(KeyEvent e) {  //was KeyReleased
		//IJ.log(""+String.valueOf(e.getKeyCode()));
		IJ.log("\""+e.getKeyChar()+"\" key was released");

        if (manager!=null && started) {
        	if (e.getKeyCode() == KeyEvent.VK_Q && inAssisting){
        		// "q" was pressed
        		// also: contour assist mode is active

        		// add this roi to the list
    			if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}

        		curROI=imp.getRoi();
        		if (curROI==null) {
    				IJ.log("Empty ROI");
    				return;
    			}

        		IJ.log("Adding ROI...");

        		// check if we need to smooth the contour before adding it
        		if (smooth) {
        			curROI=smoothCurROI(imp,curROI);
        		}

        		// add current selection to the ROI list
        		// name the new roi by its number in the list:
        		int lastNumber=0;
        		int prevROIcount=manager.getCount();
        		if (prevROIcount>0) {
        			String lastName=manager.getRoi(prevROIcount-1).getName();
	        		lastNumber=Integer.parseInt(lastName);
        		} else {
        			// no rois yet, use 0
        		}
        		
        		// prefix 0-s to the name
        		String curROIname=String.format("%04d",lastNumber+1);

        		if (saveAnnotTimes) {
	        		// annot time log, can be commented out:
	        		// measure time
	        		long curTime = (System.nanoTime()-lastStartTime)/(long)1000000; //ms time
	        		annotTimes.setValue("#",annotCount,annotCount);
	        		annotTimes.setValue("label",annotCount,curROIname);
	        		annotTimes.setValue("time",annotCount,curTime);
	        		annotCount+=1;
	        	}

        		//manager.add(curROI,prevROIcount+1);
        		curROI.setName(curROIname);

        		///*
				// already been here before, add the contour
				manager.runCommand("Add");
	        	// check if it was successful
	        	int curROIcount=manager.getCount();
				IJ.log("Added ROI ("+curROIcount+".) - assist mode");

				// reset vars
				inAssisting=false;
				invertedROI=null;
				ROIpositionX=0;
				ROIpositionY=0;
				acObjects=null;
				startedEditing=false;
				origEditedROI=null;

				// reset freehand selection tool
				curToolbar.setTool(Toolbar.FREEROI);
				//*/

				if (saveAnnotTimes) {
					// annot time log:
					// TODO: delete this: -->
					lastStartTime=System.nanoTime();
				}

        	}
        	else if (e.getKeyCode() == KeyEvent.VK_DELETE && inAssisting){
        		// delete pressed in assist mode
        		// delete current suggested contour!

        		///*
        		// check if ctrl was also pressed
        		if (e.isControlDown()) {
        			IJ.log("Ctrl+del pressed - deleting suggested contour");
        		} else {
        			IJ.log("missing Ctrl --> cannot delete the suggested contour");
        			return;
        		}
        		//*/

        		//Roi emptyROI=new Roi();
        		//imp.setRoi(emptyROI);

        		imp.deleteRoi();
        		curROI=imp.getRoi();
        		if (curROI!=null) {
        			// failed to remove the current ROI
        			IJ.log("Failed to remove current suggested ROI, please do it manually.");
        			//return;
        		}

        		// reset vars
        		invertedROI=null;
				ROIpositionX=0;
				ROIpositionY=0;
        		acObjects=null;
        		inAssisting=false;
        		startedEditing=false;
        		origEditedROI=null;
        		// reset freehand selection tool
				curToolbar.setTool(Toolbar.FREEROI);

        	}
        	else if (stepCnt){
	        	if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	        		// select next contour
		            System.out.println("Right key typed");
		        }
		        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
		        	// select prev contour
		            System.out.println("Left key typed");
		        }
		    }
		    else if (e.getKeyCode() == KeyEvent.VK_U && inAssisting && selectedCorrMethod==0) {
		    	// "u" was pressed
        		// also: contour assist mode is active
        		// also: unet correction
        		//IJ.log("-- entered roi inverting");

		    	// set suggested roi to its stored inverse
		    	if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}

        		curROI=imp.getRoi();
        		if (curROI==null) {
    				IJ.log("Empty ROI");
    				return;
    			} else {
    				if (invertedROI==null) {
	    				IJ.log("No inverse ROI stored");
	    				// try to invert
						invertedROI=checkInvertedRoi(invertedROI,curROI,null);
						WindowManager.setCurrentWindow(imp.getWindow());
						if (invertedROI==null) {
							return;
						}
	    				//return;
	    			}
    				// check if we have a valid inverted roi
					if (curROI.getContainedPoints().equals(invertedROI.getContainedPoints()) || curROI.getMask().getPixelsCopy().equals(invertedROI.getMask().getPixelsCopy())) {
						IJ.log("Currently stored inverted roi is the same as the current roi");
						invertedROI=curROI.getInverse(curROI.getImage());
						//invertedROI=invertRoiForce(curROI,curROI.getImage());
						//invertedROI=checkInvertedRoi(invertedROI,curROI,null);
						if (invertedROI==null) {
							IJ.log("  null ROI on line #851");
						}
					}
					// check the inverted roi anyway
					//invertedROI=checkInvertedRoi(invertedROI,curROI,null);
					if (invertedROI==null) {
						return;
					}
					// set the inverted roi
    				//imp.deleteRoi();
    				Roi tmpROI=(Roi) curROI;
    				curROI=(Roi) invertedROI;
    				curROI=selectLargestROI(curROI);
    				imp.setRoi(curROI);
    				Rectangle invBbox=curROI.getBounds();
    				curROI.setLocation(ROIpositionX+invBbox.getX(),ROIpositionY+invBbox.getY());
    				IJ.log("Set inverted ROI...");
    				invertedROI=(Roi) tmpROI;
    				if (invertedROI==null) {
    					// debug:
						//IJ.log("  null ROI on line #1071");
					}
    			}
		    	
		    }
		    else if (e.getKeyCode() == KeyEvent.VK_G && !e.isShiftDown() && inAssisting && selectedCorrMethod==0){
		    	// "g" was pressed
        		// also: contour assist mode is active
        		// also: unet correction
        		//IJ.log("-- entered roi active contour fitting");

        		if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}

        		curROI=imp.getRoi();
        		if (curROI==null) {
    				IJ.log("Empty ROI");
    				return;
    			} else {
    				// collect active contour objects from previously stored object
    				if (acObjects==null) {
    					IJ.log("No data found for active contour fitting");
    					return;
    				}
    				Roi tmpROI=curROI;

    				// run active contour fitting using the previously stored data:
    				ImagePlus maskImage=acObjects.getMask();
    				Rectangle tmpBbox=acObjects.getBbox();
    				//Roi acRoi=acObjects.getRoi();
    				curROI=RoiEnlarger.enlarge(curROI,Math.ceil(distanceThreshVal/5));
    				/*
    				Rectangle curBbox=curROI.getBounds();
    				if (curBbox.getWidth()>maskImage.getWidth() || curBbox.getHeight()>maskImage.getHeight()) {
    					// too large selection for the mask, recrop it
    					curROI.setLocation(curBbox.getX(),curBbox.getY());
    					maskImage=new ImagePlus(maskImage.getTitle(),curPredictionImage);
    					maskImage.show();
    					maskImage.setRoi(curROI);
						Resizer resizerObj=new Resizer();
						resizerObj.run("crop");
						Roi emptyRoi=null;
						maskImage.setRoi(emptyRoi);
						ImageConverter converter=new ImageConverter(maskImage);
						converter.convertToGray8();
						(new Thresholder()).run("skip");
						curROI.setLocation(tmpBbox.getX(),tmpBbox.getY());
    				}
    				*/
    				//curROI=runActiveContourFitting(maskImage,intermediateRoi,tmpBbox,imp);

    				// run dilation simply:
    				///*
					ImageProcessor maskImageProc=maskImage.getProcessor();
					int countErode=3; // # of nearest neighbours
					int backgroundErode=0; // background intensity
					((ByteProcessor)maskImageProc).dilate(countErode, backgroundErode);
					//((ByteProcessor)maskImageProc).dilate(countErode, backgroundErode);
					maskImage.setProcessor(maskImageProc);
					//*/

    				// this was working:
    				curROI=runActiveContourFitting(maskImage,curROI,tmpBbox,acObjects.getImg());
    				/*
    				SnakeGUI_mymod snake_mymodObj=new SnakeGUI_mymod();
    				int repeatDilate=2;
    				maskImageProc=maskImage.getProcessor();
    				for (int k=0; k<repeatDilate; k++) {
    					((ByteProcessor)maskImageProc).dilate(countErode, backgroundErode);
    				}
    				maskImage.setProcessor(maskImageProc);
    				ImagePlus customMask=snake_mymodObj.runSnake_MYMOD(imp,maskImage,tmpBbox);
    				//ImagePlus customMask=snake_mymodObj.runSnake_MYMOD2(imp,maskImage,tmpBbox);
					
					customMask.show();
    				ImageConverter converter=new ImageConverter(customMask);
					converter.convertToGray8();
					(new Thresholder()).run("skip");
					curROI=ThresholdToSelection.run(customMask);
					//maskImage=customMask;
					//if (curROI!=null)
					//	curROI.setLocation(ROIpositionX+curROI.getBounds.getX(),ROIpositionY+curROI.getBounds.getY());
					*/

				    if (curROI!=null)
						IJ.log("  >> ac ROI type: "+curROI.getTypeAsString());
					

					// check if there is an output from ac as a roi
					if (curROI==null || !(curROI.getType()==Roi.FREEROI || curROI.getType()==Roi.COMPOSITE || curROI.getType()==Roi.TRACED_ROI)) {
						// failed to produce a better suggested contour with AC than we had with unet before, revert to it
						IJ.log("Failed to create new contour with active contours, showing U-Net prediction");
						//curROI=ThresholdToSelection.run(maskImage);
						//postProcessAssistedROI(curROI,tmpBbox,maskImage,true,imp,true);
						curROI=tmpROI;
						//curROI=postProcessAssistedROI(curROI,tmpBbox,maskImage,true,imp,true);
						if (maskImage!=null) {
							maskImage.changes=false;
							maskImage.close();
						}
						// also reset the inverted roi
						//invertedROI=invertRoi(intermediateRoi,maskImage);
						//invertedROI=(invertedROI instanceof ShapeRoi) ? createRoi((ShapeRoi)invertedROI) : invertedROI;
						if (curROI==invertedROI) {
							IJ.log("Failed to invert current roi (same)");
						}
						if (invertedROI==null) {
							IJ.log("  null ROI on line #3416");
						}
					}
					imp.setRoi(curROI);

					
					// roi positioning was done here, moved to its own fcn

					Window curWindow=WindowManager.getWindow("title");
					if (curWindow!=null) {
						// close image window
						maskImage.changes=false;
						maskImage.getWindow().close();
					}
					WindowManager.setCurrentWindow(imp.getWindow());
					// set main imwindow var to the original image
					//imWindow=WindowManager.getWindow(destNameRaw);
					imp.getWindow().toFront();
    			}
		    }

		    else if (e.getKeyCode() == KeyEvent.VK_T && !inAssisting){
		    	// "t" was pressed
        		// also: contour assist mode is inactive!!!

        		// the roi has already been added by the default ROIManager "t" command, rename its label
    			if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}

        		curROI=imp.getRoi();
        		if (curROI==null) {
    				IJ.log("Empty ROI");
    				return;
    			}

        		IJ.log("Renaming last ROI manually added...");

        		// name the new roi by its number in the list:
            	int lastNumber=0;
        		int prevROIcount=manager.getCount();
        		if (prevROIcount>1) {
        			String lastName=manager.getRoi(prevROIcount-2).getName();
	        		lastNumber=Integer.parseInt(lastName);
        		} else {
        			// no rois yet, use 0
        		}

        		String curROIname=String.format("%04d",lastNumber+1);

        		if (saveAnnotTimes) {
	        		// measure time
	        		long curTime = (System.nanoTime()-lastStartTime)/(long)1000000; //ms time
	        		annotTimes.setValue("#",annotCount,annotCount);
	        		annotTimes.setValue("label",annotCount,curROIname);
	        		annotTimes.setValue("time",annotCount,curTime);
	        		annotCount+=1;
	        	}


        		///*
				// already been here before, add the contour
				manager.rename(prevROIcount-1,curROIname);

				if (saveAnnotTimes) {
					// TODO: delete this: -->
					lastStartTime=System.nanoTime();
				}

		    }

		    else if (e.getKeyCode() == KeyEvent.VK_Q && startedEditing) {
		    	// "q" was pressed
        		// also: contour edit mode is active

        		// check if ctrl was also pressed
        		if (e.isControlDown()) {
        			IJ.log("Ctrl+q pressed - updating edited contour");
        		} else {
        			IJ.log("missing Ctrl --> cannot update current contour");
        			return;
        		}

        		if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}

		    	// reset the selected contour in ROI manager to this new one
		    	curROI=imp.getRoi();
		    	// check if the roi has a class
		    	int classNum=origEditedROI.getGroup();
		    	Color newColour=currentSelectionColor;
		    	if (classNum>0){
		    		String selectedClassNameVar="Class_"+String.format("%02d",classNum);
		    		int tmpIdx=classFrameColours.get(classFrameNames.indexOf(selectedClassNameVar));
		    		newColour=getClassColour(tmpIdx);
		    		curROI.setGroup(classNum);
		    		IJ.log("In edit mode set the edited classified contour colour to ("+String.valueOf(tmpIdx)+")");
		    	}
		    	curROI.setStrokeColor(newColour);
		    	//curROI.setStrokeColor(currentSelectionColor);
		    	curROI.setStrokeWidth(origStrokeWidth);
		    	manager.setRoi(curROI,editROIidx);
		    	IJ.log("Saved edited ROI");

		    	if (classNum>0)
		    		imp.setRoi((Roi)null);

		    	String curROIname=manager.getName(editROIidx)+"_editing";

		    	if (saveAnnotTimes) {
	        		// measure time
	        		long curTime = (System.nanoTime()-lastStartTime)/(long)1000000; //ms time
	        		annotTimes.setValue("#",annotCount,annotCount);
	        		annotTimes.setValue("label",annotCount,curROIname);
	        		annotTimes.setValue("time",annotCount,curTime);
	        		annotCount+=1;

	        		// TODO: delete this: -->
					lastStartTime=System.nanoTime();
				}

				// reset tool to freehand selection
				curToolbar.setTool(Toolbar.FREEROI);

				startedEditing=false;


		    }

		    else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && startedEditing) {
		    	// "esc" was pressed
        		// also: contour edit mode is active

		    	// check if the roi has a class
		    	int classNum=origEditedROI.getGroup();
		    	Color newColour=currentSelectionColor;
		    	if (classNum>0){
		    		String selectedClassNameVar="Class_"+String.format("%02d",classNum);
		    		int tmpIdx=classFrameColours.get(classFrameNames.indexOf(selectedClassNameVar));
		    		newColour=getClassColour(tmpIdx);
		    		IJ.log("In edit mode set the edited classified contour colour to ("+String.valueOf(tmpIdx)+")");
		    	}

		    	origEditedROI.setStrokeColor(newColour);
		    	//origEditedROI.setStrokeColor(currentSelectionColor);
		    	origEditedROI.setStrokeWidth(origStrokeWidth);
		    	if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}
		    	imp.setRoi(origEditedROI);
        		// reset the selected contour in ROI manager to the original version of it
		    	manager.setRoi(origEditedROI,editROIidx);
		    	IJ.log("Restored edited ROI to its original");

		    	if (classNum>0)
		    		imp.setRoi((Roi)null);

		    	curToolbar.setTool(Toolbar.FREEROI);

		    	startedEditing=false;
		    	
		    }

		    else if (e.getKeyCode() == KeyEvent.VK_DELETE && startedEditing) {
		    	// "del" was pressed
        		// also: contour edit mode is active

        		// check if ctrl was also pressed
        		if (e.isControlDown()) {
        			IJ.log("Ctrl+delete pressed - deleting current contour");
        		} else {
        			IJ.log("missing Ctrl --> cannot delete current contour");
        			return;
        		}

        		if (imp==null) {
    				//IJ.log("No image opened");
    				imp=WindowManager.getCurrentImage();
    			}

		    	// find this roi in the ROI manager
		    	//imp.setRoi(manager.getRoi(editROIidx));
		    	manager.select(editROIidx);
		    	curROI=imp.getRoi();
		    	if (curROI==null) {
        			// nothing to delete
        			IJ.log("No ROI is selected to be deleted");
        			return;
        		}
        		manager.runCommand("delete");

        		curROI=imp.getRoi();
        		if (curROI!=null) {
        			// failed to remove the current ROI
        			IJ.log("Deleting the current ROI \"manually\".");
        			imp.deleteRoi();
        			//return;
        		}


		    	curToolbar.setTool(Toolbar.FREEROI);

		    	startedEditing=false;
		    }
        }
    }
    //*/
    public void add2myROImanager(){
    	IJ.log("shortcut pressed");
    	manager.runCommand("Add");
		int curROIcount=manager.getCount();
		IJ.log("Added ROI ("+curROIcount+".)");
    }


    public Roi smoothCurROI(ImagePlus imp, Roi thisROI){
    	IJ.log("Smoothing selection...");
		// smooth the selection first
		// do smoothing by interpolation command

		// set options to interval=1.0 and smooth=true
		// call the interpolate function here:

		// from https://imagej.nih.gov/ij/source/ij/plugin/Selection.java's void interpolate() fcn:
		//public void interpolateCurRoi(){
		// ---- interpolate() fcn quote start
		double interval=1.0;
		//double interval=0.1;
		FloatPolygon poly = curROI.getInterpolatedPolygon(interval, true);
		int t = curROI.getType();
		int type = curROI.isLine()?Roi.FREELINE:Roi.FREEROI;
		if (t==Roi.POLYGON && interval>1.0)
			type = Roi.POLYGON;
		if ((t==Roi.RECTANGLE||t==Roi.OVAL||t==Roi.FREEROI) && interval>=8.0)
			type = Roi.POLYGON;
		if ((t==Roi.LINE||t==Roi.FREELINE) && interval>=8.0)
			type = Roi.POLYLINE;
		if (t==Roi.POLYLINE && interval>=8.0)
			type = Roi.POLYLINE;
		ImageCanvas ic = imp.getCanvas();
		if (poly.npoints<=150 && ic!=null && ic.getMagnification()>=12.0)
			type = curROI.isLine()?Roi.POLYLINE:Roi.POLYGON;
		Roi p = new PolygonRoi(poly,type);
		if (curROI.getStroke()!=null)
			p.setStrokeWidth(curROI.getStrokeWidth());
		p.setStrokeColor(curROI.getStrokeColor());
		p.setName(curROI.getName());
		transferProperties(curROI, p);
		imp.setRoi(p);
		curROI=p;
		// ---- interpolate() fcn quote end
		//return curROI;
		//}

		IJ.log("done");
		//IJ.log("Smoothed ROI");

		return curROI;
    }


    // currently not used fcn
    public void suggestContour(){
    	// check if suggestion mode is active
    	// create a checkbox for it
    	// TODO

    	if (suggestContourOn) {
    		// suggest contour based on previous contours added
    		int tmpRoiCount=manager.getCount();
    		if (tmpRoiCount<1) {
    			IJ.log("No annotated objects yet");
    			MessageDialog noAnnotsYetMsg=new MessageDialog(instance,
                 "Warning",
                 "No annotated objects yet\nPlease add objects to use suggestions");
    			return;
    		}

    		// can suggest based on at least 1 object

    	}
    }

    // -------------------
    // checkbox listener + radio button + comboxbox listener
    // -------------------
	public void itemStateChanged(ItemEvent ie) {
		// get source object by class
		if (ie.getItem() instanceof JCheckBox){
			// get the source checkbox of the event
		    JCheckBox cb = (JCheckBox) ie.getItem();
		    int state = ie.getStateChange();
		    boolean isSelected=false;
		    String cbText=cb.getText();
		    // log its state change
		    if (state == ItemEvent.SELECTED){
		      IJ.log(cbText + " selected");
		      isSelected=true;
		    }
		    else if (state==ItemEvent.DESELECTED){
		      IJ.log(cbText + " cleared");
		      isSelected=false;
		    }

		    // set vars according to checkboxes
		  	if (cbText.equals("Add automatically")){
	  			addAuto=isSelected;
	  			IJ.log("Add automatically: "+String.valueOf(state));
		  	}
	  		else if (cbText.equals("Smooth")){
	  			smooth=isSelected;
	  			IJ.log("Smooth: "+String.valueOf(state));
	  		}
	  		else if(cbText.equals("Show contours")){
	  			showCnt=isSelected;
	  			IJ.log("Show contours: "+String.valueOf(state));

	  			if (selectedAnnotationType.equals("instance") || selectedAnnotationType.equals("bounding box")) {
		  			if (showCnt) {
		  				// show contours
		  				manager.runCommand("Show All");
		  			} else{
		  				// hide contours
		  				manager.runCommand("Show None");
		  			}
		  		} else if (selectedAnnotationType.equals("semantic")) {
		  			imp=WindowManager.getCurrentImage();
		  			if (showCnt) {
		  				// show overlay
		  				//overlayCommandsObj.run("show");
		  				imp.setHideOverlay(false);
		  			} else{
		  				// hide overlay
		  				//overlayCommandsObj.run("hide");
		  				imp.setHideOverlay(true);
		  			}
		  		}
	  		}
	  		// this is now removed:
	  		else if(cbText.equals("Show labels")){
	  			showLbs=isSelected;
	  			IJ.log("Show labels: "+String.valueOf(state));
	  		}
	  		else if(cbText.equals("Contour assist")){
	  			contAssist=isSelected;
	  			IJ.log("Contour assist: "+String.valueOf(state));
	  			if (contAssist) {
	  				// disable automatic adding to list so the user can overview the suggested contour before adding it
	  				IJ.log("Switching automatic adding off");
		  			addAuto=false;
		  			chckbxAddAutomatically.setSelected(false);
		  			chckbxAddAutomatically.setEnabled(false);

		  			// should set boolean vars to:
		  			// first start freehand selection tool for drawing -->
		  				// on mouse release start contour correction -->
		  					// user can check it visually -->
		  						// set brush selection tool for contour modification -->
		  							// detect pressing "q" when they add the new contour -->
		  								// reset freehand selection tool

		  			curToolbar.setTool(Toolbar.FREEROI);

		  			editMode=false;
		  			chckbxStepThroughContours.setSelected(false);
		  			chckbxStepThroughContours.setEnabled(false);

		  			chckbxClass.setSelected(false);
					chckbxClass.setEnabled(false);
					classMode=false;
	  			} else {
	  				// can enable auto add again
	  				chckbxAddAutomatically.setEnabled(true);
	  				chckbxStepThroughContours.setEnabled(true);
	  				chckbxClass.setEnabled(true);
	  			}
	  			
	  		}
	  		else if(cbText.equals("Show overlay")){
	  			showOvl=isSelected;
	  			IJ.log("Show overlay: "+String.valueOf(state));

	  			if (overlayAdded){
		  			if (showOvl){

		  				if (overlayedROI && !overlayedSemantic) {
		  					// ROI overlay
			  				IJ.log("--showing overlay");
			  				Roi.setColor(defOverlay);
			  				IJ.log("set overlay color");

			  				/*
			  				try{
			  					overlayManager.runCommand("Show all");
			  					IJ.log("showed all overlay contours");
			  				} catch (Exception ex){
			  					IJ.log("Error in line 433: overlayManager.runCommand(Show all)");
			  				}
			  				*/
			  				// this doesnt actually select all:
			  				//overlayManager.runCommand("Select all");
			  				// select each roi one-by-one instead:
			  				int overlayCount=overlayManager.getCount();
			  				for (int ovi=0; ovi<overlayCount; ovi++) {
			  					overlayManager.select(ovi);
			  					overlayCommandsObj.run("add");
			  				}
		  					IJ.log("selected all overlay contours");
		  					IJ.log("added all contours as overlay on image");
		  					/*
			  				//OverlayCommands("add"); // shows the selection as overlay
			  				try{
				  				overlayCommandsObj.run("add");
				  				//overlayCommandsObj.run("from");
				  				IJ.log("added all contours as overlay on image");
				  			} catch (Exception ex) {
				  				IJ.log("Error in line 439: overlayCommandsObj.run(add)");
				  			}
				  			*/

				  			//deselect overlay contours
				  			overlayManager.runCommand("Deselect all");
		  					IJ.log("DEselected all overlay contours");

			  				// reset selection color to current
			  				try{
			  					Roi.setColor(currentSelectionColor);
			  					IJ.log("reset overlay color to current contour colour");
			  				} catch (Exception ex) {
			  					IJ.log("Error in line 446: Roi.setColor(currentSelectionColor)");
			  				}
		  				} else {
		  					// semantic overlay
		  					IJ.log("--showing overlay");

		  					String currentColorHex=ColorToHex(defOverlay);
		  					// set semi-transparent colour
							String opacityColor="#66"+currentColorHex;
							overlaySemantic.setFillColor(ij.plugin.Colors.decode(opacityColor,defOverlay));

							imp.setOverlay(overlaySemantic);
							//imp.getProcessor().drawOverlay(overlaySemantic);
		  				}


		  			} else {
		  				IJ.log("--hiding overlay");
		  				if (overlayedROI && !overlayedSemantic) {
		  					//OverlayCommands("hide"); // hides it
			  				overlayCommandsObj.run("hide");
		  				} else {
		  					imp.setOverlay(null);
		  				}
		  				
		  			}

		  			// reset show contours checkbox and set it again (for some reason to display correctly)
	  				if (showCnt) {
	  					chckbxShowAll.setSelected(false);
	  					chckbxShowAll.setSelected(true);
	  				} else {
	  					chckbxShowAll.setSelected(true);
	  					chckbxShowAll.setSelected(false);
	  				}
		  		}
	  		}
	  		else if(cbText.equals("Step through contours")){
	  			stepCnt=isSelected;
	  			IJ.log("Step through contours: "+String.valueOf(state));
	  		}
	  		else if (cbText.equals("Edit mode")) {
	  			editMode=isSelected;
	  			IJ.log("Edit mode: "+String.valueOf(state));
	  			if (editMode) {
	  				// disable automatic adding to list and contour assist while editing
	  				IJ.log("Switching automatic adding and contour assist off");
		  			addAuto=false;
		  			chckbxAddAutomatically.setSelected(false);
		  			chckbxAddAutomatically.setEnabled(false);

		  			contAssist=false;
		  			chckbxContourAssist.setSelected(false);
		  			chckbxContourAssist.setEnabled(false);
		  			chckbxClass.setSelected(false);
					chckbxClass.setEnabled(false);
					classMode=false;
	  			} else {
		  			chckbxAddAutomatically.setEnabled(true);
		  			chckbxContourAssist.setEnabled(true);
		  			chckbxClass.setEnabled(true);
	  			}
	  		}
	  		else if (cbText.equals("Class mode")) {
	  			classMode=isSelected;
	  			IJ.log("Class mode: "+String.valueOf(state));
	  			if (classMode) {
	  				// disable automatic adding to list and contour assist while editing
	  				IJ.log("Switching automatic adding and contour assist off");
		  			addAuto=false;
		  			chckbxAddAutomatically.setSelected(false);
		  			chckbxAddAutomatically.setEnabled(false);

		  			contAssist=false;
		  			chckbxContourAssist.setSelected(false);
		  			chckbxContourAssist.setEnabled(false);

		  			editMode=false;
		  			chckbxStepThroughContours.setSelected(false);
		  			chckbxStepThroughContours.setEnabled(false);

		  			// start the classes frame
		  			openClassesFrame();
		  			
	  			} else {
		  			chckbxAddAutomatically.setEnabled(true);
		  			chckbxContourAssist.setEnabled(true);
		  			chckbxStepThroughContours.setEnabled(true);
	  			}
	  		}
	  		else
	  			IJ.showStatus("Unexpected checkbox");
	  	}
	  	else if (ie.getItem() instanceof JRadioButton) {
	  		if (classListSelectionHappened){
	  			// not a real radio button change, do nothing
	  			return;
	  		}

	  		JRadioButton rb = (JRadioButton) ie.getItem();
		    int state = ie.getStateChange();
		    boolean isSelectedRb=false;
		    String rbText=rb.getText();
		    // log its state change
		    if (state == ItemEvent.SELECTED){
		      IJ.log(rbText + " selected");
		      isSelectedRb=true;
		    } else {
		    	//debug:
		    	//IJ.log("    (not a radiobutton selected event)");
		    	return;
		    }

		    // set vars according to radio buttons
		    int selectedClassColourCode=-1;
		    String curColourName=null;
		    switch (rbText){
		    	case "R":
		    		selectedClassColourCode=0;
		    		curColourName="red";
		    		selectedClassColourIdx=Color.red;
		    		break;
		    	case "G":
		    		selectedClassColourCode=1;
		    		curColourName="green";
		    		selectedClassColourIdx=Color.green;
		    		break;
		    	case "B":
		    		selectedClassColourCode=2;
		    		curColourName="blue";
		    		selectedClassColourIdx=Color.blue;
		    		break;
		    	case "C":
		    		selectedClassColourCode=3;
		    		curColourName="cyan";
		    		selectedClassColourIdx=Color.cyan;
		    		break;
		    	case "M":
		    		selectedClassColourCode=4;
		    		curColourName="magenta";
		    		selectedClassColourIdx=Color.magenta;
		    		break;
		    	case "Y":
		    		selectedClassColourCode=5;
		    		curColourName="yellow";
		    		selectedClassColourIdx=Color.yellow;
		    		break;
		    	case "O":
		    		selectedClassColourCode=6;
		    		curColourName="orange";
		    		selectedClassColourIdx=Color.orange;
		    		break;
		    	case "W":
		    		selectedClassColourCode=7;
		    		curColourName="white";
		    		selectedClassColourIdx=Color.white;
		    		break;
		    	case "K":
		    		selectedClassColourCode=8;
		    		curColourName="black";
		    		selectedClassColourIdx=Color.black;
		    		break;
		    	default:
		    		IJ.log("Unexpected radio button value");
		    		break;
		    }

		    String selectedClassName=classListList.getSelectedValue();
			// find it in the classnames list
			int selectedClassIdxList=classFrameNames.indexOf(selectedClassName);
			if (selectedClassIdxList<0){
				// didnt find it
				IJ.log("Could not find the currently selected class name in the list. Please try again.");
				return;
			}
			classFrameColours.set(selectedClassIdxList,selectedClassColourCode);
			IJ.log("Set selected class (\""+selectedClassName+"\") colour to "+rbText);
			// display currently selected class colour on the radiobuttons and label
        	lblCurrentClass.setText("<html>Current: <font color='"+curColourName+"'>"+selectedClassName+"</font></html>");

        	// set all currently assigned ROIs of this group to have the new contour colour
        	manager.selectGroup(selectedClassNameNumber);

        	Roi tmpROI=null;
        	Roi[] manyROIs=manager.getSelectedRoisAsArray();
			IJ.log("found "+String.valueOf(manyROIs.length)+" rois of class #"+String.valueOf(selectedClassNameNumber));
			for (int i=0; i<manyROIs.length; i++) {
	        	tmpROI=manyROIs[i];
	        	if (tmpROI.getGroup()!=selectedClassNameNumber){
	        		// for some reason, when no roi has a normal group (>0), all get selected --> need to skip them
	        		continue;
	        	}
	        	tmpROI.setStrokeColor(selectedClassColourIdx);
	        	//tmpROI.setStrokeWidth(1.0);
	        	/*
        		double alphav=0.15;
        		int alphaInt=(int)Math.round(alphav*255);
        		tmpROI.setFillColor(makeAlphaColor(selectedClassColourIdx,alphaInt));
        		tmpROI.setStrokeColor(selectedClassColourIdx);
        		*/

        		//manager.setRoi(tmpROI,?);
	        }

	        // deselect the current ROI so the true class colour contour can be shown
	        if (imp!=null)
    			imp.setRoi((Roi)null);

		  	return;
	  	}

	  	/*
	  	else if (ie.getItem() instanceof JComboBox) {
	  		IJ.log("combobox item state changed");
	  		JComboBox combobox = (JComboBox) ie.getItem();
		    int state = ie.getStateChange();
		    if (state == ItemEvent.SELECTED){
		    	// a default class was selected, assign all unassigned objects to this class
		    	String selectedClassName=(String) combobox.getSelectedItem();
		    	IJ.log("Selected '"+selectedClassName+"' as default class");
		    	if (selectedClassName.equals("(none)")){
		    		// set no defaults
		    		defaultClassNumber=-1;
		    	} else {
		    		// a useful class is selected
		    		defaultClassNumber=Integer.parseInt(selectedClassName.substring(selectedClassName.lastIndexOf("_")+1,selectedClassName.length()));

		    		// set all unassigned objects to this class
		    		setDefaultClass4objects();
		    	}
		    } else {
		    	// do nothing
		    	IJ.log("combobox item state changed, event: "+String.valueOf(ItemEvent.SELECTED));
		    }
	  	}
	  	*/

	}


	void addButton(String label) {
		Button b = new Button(label);
		b.addActionListener(this);
		b.addKeyListener(IJ.getInstance());
		//panel.add(b);
	}

	// this maybe useful to detect the current image and do something with it
	// from imageJ plugin demo:
	public void actionPerformed(ActionEvent e) {
		imp = WindowManager.getCurrentImage();

		String label = e.getActionCommand();
		if (label==null)
			return;
		else if (label.equals(("Open").toLowerCase()))
			closeingOnPurpuse=true;
		new Runner(label, imp);
	}

	// close windows fcn when quitting or before opening a new image
	public boolean closeActiveWindows(){
		boolean doClose=false;
		// ask for confirmation
    	int response = JOptionPane.showConfirmDialog(null, "Do you want to save current contours?\nThis will overwrite any previously\nsaved annotation for this image.", "Save before quit",
	        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
	    if (response == JOptionPane.NO_OPTION) {
	    	// just quit
	    	IJ.log("No button clicked (Save before quit)");
	    	doClose=true;
	    	return doClose;

	    } else if (response == JOptionPane.YES_OPTION) {
		    IJ.log("Yes button clicked (Save before quit)");
		    // save rois first
		    if (started) {
		    	IJ.log("  started");
		    } else {
				IJ.log("  ! started");
		    }
		    if (manager!=null) {
		    	IJ.log("  manager is not null");
		    } else {
		    	IJ.log("  manager = null");
		    }
		    

		    if (started && manager!=null) {
		    	imp=WindowManager.getCurrentImage();
		    	if (manager.getCount()!=0) {
		    		IJ.log("  >> starting save...");
		    		// save using a separate fcn instead:
		    		saveData();
		    		
		    		IJ.log("in close confirm after save finished");
		    	}
		    }
		    
		    doClose=true;
		    
	    } else if (response == JOptionPane.CANCEL_OPTION){
	    	// do nothing
	    	IJ.log("Cancel button clicked (Save before quit)");
	    	doClose=false;
	    	return doClose;

	    } else if (response == JOptionPane.CLOSED_OPTION) {
	    	// do nothing
	    	IJ.log("Closed close confirm (Save before quit)");
	    	doClose=false;
	    	return doClose;
	    }
	    return doClose;
	}

	// detect window closing event
	@Override
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
			if (!started && manager==null && WindowManager.getCurrentWindow()==null) {
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


			// add closing action warning before closing the plugin

			// check if options window is open: if so, close it
		    if (optionsFrame!=null) {
		    	optionsFrame.dispose();
		    	optionsFrame=null;
		    }

		    // check if classes window is open: if so, close it
		    if (classesFrame!=null){
		    	classesFrame.dispose();
		    	classesFrame=null;
		    }

		    boolean continueClosing=true;

		    if (started && manager!=null && manager.getCount()==0) {
		    	IJ.log("closing empty ROI manager");
		    	// delete selections from image
				manager.runCommand("Show None");
				//Selection selectionObj=new Selection();
				//selectionObj.run("none");
		    	manager.close();
		    	manager=null;

		    } else{
		    	IJ.log("popping close confirm window");
		    	continueClosing=closeActiveWindows();
		    }

		    // process confirmation answer from the user
		    // close roi manager, image window, log window
		    if (continueClosing) {
		    	IJ.log("continueClosing = true");
			    // close everything
			    if (started && manager!=null) {
			    	IJ.log("closing roi manager after close confirm window");
			    	// delete selections from image
					manager.runCommand("Show None");
					//Selection selectionObj=new Selection();
					//selectionObj.run("none");
			    	manager.close();
			    	manager=null;

			    }
			    ImageWindow curImageWindow=WindowManager.getCurrentWindow();
			    if (curImageWindow!=null) {
			    	curImageWindow.close();
			    }
			    Window logWindow=WindowManager.getWindow("Log");
			    if (logWindow!=null) {
			    	//logWindow.dispose();
			    	IJ.log("\\Clear");
			    	logWindow.setVisible(false);
			    }

			    // close the main frame too
			    dispose();
			    instance = null;
			    manager=null;
				//*/

				//instance = null;
			} else {
				IJ.log("Closing canceled");
			}
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


	// adapted from https://imagej.nih.gov/ij/developer/source/ij/plugin/tool/BrushTool.java.html
	// modified to set paint on overlay property to true
	// moved to its own class now
	/*
	public void runBrushTool(String arg) {
        //isPencil = "pencil".equals(arg);
        //widthKey = isPencil ? PENCIL_WIDTH_KEY : BRUSH_WIDTH_KEY;
        widthKey = "brush.width";
        //width = (int)Prefs.get(widthKey, isPencil ? 1 : 5);
        width = (int)Prefs.get(widthKey,5);
        //paintOnOverlay = Prefs.get(OVERLAY_KEY, false);
        paintOnOverlay = true;
        Toolbar.addPlugInTool(this);
        if (!isPencil)
            brushInstance = this;
    }
    */


    // from http://wikicode.wikidot.com/convert-color-to-hex-color-string
    public static String ColorToHex(Color color) {
        String rgb = Integer.toHexString(color.getRGB());
        IJ.log("--color before: "+rgb);
        //IJ.log("--color after: "+rgb.substring(2, rgb.length()));
        String out=null;
        if (rgb.substring(0,2).equals("FF") || rgb.substring(0,2).equals("ff")) {
        	out=rgb.substring(2, rgb.length());
        } else {
        	out=rgb;
        }
        return out;
    }

    // ---------------------
    // open a new image fcn
    // ---------------------
    public void openNew(Runner runnerInstance){
    	//boolean contClosing=false;

		// closing & saving previous annotations moved to separate fcn
		closeWindowsAndSave();

		// check contour assist setting
	    if (contAssist) {
	    	addAuto=false;
	    	chckbxAddAutomatically.setEnabled(false);
	    	IJ.log("< contour assist mode is active");
	    	editMode=false;
	    	chckbxStepThroughContours.setEnabled(false);

	    	classMode=false;
	    	chckbxClass.setEnabled(false);
	    }

	    // check edit mode setting
	    if (editMode) {
	    	addAuto=false;
	    	chckbxAddAutomatically.setEnabled(false);
	    	IJ.log("< edit mode is active");
	    	contAssist=false;
	    	chckbxContourAssist.setEnabled(false);

	    	classMode=false;
	    	chckbxClass.setEnabled(false);
	    }

	    // check class mode setting
	    if (classMode) {
	    	addAuto=false;
	    	chckbxAddAutomatically.setEnabled(false);
	    	IJ.log("< class mode is active");
	    	editMode=false;
	    	chckbxStepThroughContours.setEnabled(false);

	    	contAssist=false;
	    	chckbxContourAssist.setEnabled(false);
	    }

	    int fileListCount;

	    if (!imageFromArgs) {
			// file open dialog
			Opener opener2=new Opener();
			OpenDialog opener=null;
			
			//OpenDialog opener=new OpenDialog("Select an image",null);
			if (stepping) {
				// concatenate file path with set new prev/next image name and open it without showing the dialog
				opener=new OpenDialog("Select an image",defDir+File.separator+defFile);
				stepping=false;
			} else {
				opener=new OpenDialog("Select an image",defDir,defFile);
			}

			// check if cancel was pressed:
			String validPath=opener.getPath();
			if (validPath==null) {
				// path is null if the dialog was canceled
				IJ.log("canceled file open");
				return;
			}
			destFolder=opener.getDirectory();
			destNameRaw=opener.getFileName();
			defDir=destFolder;
			defFile=destNameRaw;
			curPredictionImageName=defFile;
			curPredictionImage=null;
			curOrigImage=null;
			opener2.open(destFolder+File.separator+destNameRaw);
			//destFolder=opener.getDir();
			IJ.log("Opened file: "+destNameRaw);


			// get a list of files in the current directory
			File folder = new File(destFolder);
			File[] listOfFiles = folder.listFiles();
			fileListCount=0;
			//String[] curFileList;

			// get number of useful files
			for (int i = 0; i < listOfFiles.length; i++) {
			  if (listOfFiles[i].isFile() && (listOfFiles[i].getName().endsWith(".png") || listOfFiles[i].getName().endsWith(".bmp") || listOfFiles[i].getName().endsWith(".jpg") || listOfFiles[i].getName().endsWith(".jpeg") || listOfFiles[i].getName().endsWith(".tif") || listOfFiles[i].getName().endsWith(".tiff"))) {
			  	fileListCount+=1;
			  }
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

			// find current file in the list
			curFileIdx = -1;
			for (int i=0;i<curFileList.length;i++) {
			    if (curFileList[i].equals(destNameRaw)) {
			        curFileIdx = i;
			        break;
			    }
			}

		} else {
			// image is parsed from args, only display it
			/* check!!!!!!!!!!!!! */
			imp.show();
			String title="-Parsed input.image-";
			imp.setTitle(title);
			curFileList=new String[1];
			curFileList[0]=title;
			curFileIdx=0;
			fileListCount=1;
			defFile=title;
			defDir="";
			destNameRaw=defFile;
			IJ.log("Opened file from input args");

			// get/set slice info (currentSlice attribute is 1-based!)
			currentSliceIdx=imp.getCurrentSlice();
			prevSliceIdx=currentSliceIdx+0;
			IJ.log("Initial slice is: "+currentSliceIdx);
			curOrigImage=imp.getStack().getProcessor(currentSliceIdx);
		}

		// update file name tag on main window to check which image we are annotating
		String displayedName=destNameRaw;
		int maxLength=13; //13
		// check how long the file name is (if it can be displayed)
		int nameLength=destNameRaw.length();
		if (nameLength>maxLength) {
			//displayedName=destNameRaw.substring(0,maxLength-(nameLength-destNameRaw.lastIndexOf(".")-3))+"..."+destNameRaw.substring(destNameRaw.lastIndexOf(".")+1,nameLength);
			displayedName=destNameRaw.substring(0,Math.min(maxLength-3,(destNameRaw.lastIndexOf("."))))+"..."+destNameRaw.substring(destNameRaw.lastIndexOf(".")+1,nameLength);
		}
		lblCurrentFile.setText(" ("+String.valueOf(curFileIdx+1)+"/"+String.valueOf(fileListCount)+"): "+displayedName);

		// MOVING FCN mods:
		lblCurrentFile.addMouseListener(runnerInstance);

		// inactivate prev/next buttons if needed
		if (curFileIdx==0) {
			// first image in folder, inactivate prev:
			buttonPrev.setEnabled(false);
		} else {
			buttonPrev.setEnabled(true);
		}

		if (curFileIdx==curFileList.length-1) {
			// last image, inactivate next:
			buttonNext.setEnabled(false);
		} else {
			buttonNext.setEnabled(true);
		}

		// initialize image vars
		imp = WindowManager.getCurrentImage();
		//ip = imp.getProcessor();
		//IJ.showStatus(command + "...");
		startTime = System.currentTimeMillis();


		// roi file name is like:
		//destName=destFolder+"/"+substring(File.name,0,indexOf(File.name, "."))+".zip";
		//File.makeDirectory(destName);

		// ask annotation type in dialog box
		String[] types=new String[3];
		types[0]="instance";
		types[1]="semantic";
		types[2]="bounding box";

		// remember option
		boolean validType=false;
		if (rememberAnnotType && (selectedAnnotationType!=null && !selectedAnnotationType.equals(""))){
			if (Arrays.asList(types).contains(selectedAnnotationType)){
				// valid annot type, can continue
				validType=true;
				IJ.showStatus("Annotation type: "+selectedAnnotationType);
				IJ.log("Fetched annotation type: "+selectedAnnotationType);
			}
		}
		if (!rememberAnnotType || !validType) {

			GenericDialog Dialog = new GenericDialog("Annotation type");

			Dialog.addChoice("Select annotation type: ", types, types[0]);
			Dialog.showDialog();
			selectedAnnotationType = Dialog.getNextChoice();
			IJ.showStatus("Annotation type: "+selectedAnnotationType);
			IJ.log("Set annotation type: "+selectedAnnotationType);
		}

		if (rememberAnnotType){
			// save selection to config file
			SaveNewProp("defaultAnnotType", selectedAnnotationType);
		}

		// instance annotation type
		if(selectedAnnotationType.equals("instance")){
			// set freehand selection tool by default
			curToolbar.setTool(Toolbar.FREEROI);
			if (!contAssist) {
				// contAssist is off
				if (!editMode) {
					// edit mode is off
					if (!classMode){
						// class mode is off
						// enable contour correction
						chckbxAddAutomatically.setEnabled(true);
						chckbxStepThroughContours.setEnabled(true);
						chckbxContourAssist.setEnabled(true);
					} else {
						// class mode is on
						// disable the others
						chckbxAddAutomatically.setSelected(false);
						chckbxAddAutomatically.setEnabled(false);
						chckbxStepThroughContours.setSelected(false);
						chckbxStepThroughContours.setEnabled(false);
						chckbxContourAssist.setSelected(false);
						chckbxContourAssist.setEnabled(false);

						chckbxClass.setEnabled(true);

						editMode=false;
						addAuto=false;
						contAssist=false;
					}
					
				} else {
					// edit mode is on
					// disable contour correction
					chckbxAddAutomatically.setSelected(false);
					chckbxAddAutomatically.setEnabled(false);
					chckbxStepThroughContours.setEnabled(true);
					chckbxContourAssist.setSelected(false);
					chckbxContourAssist.setEnabled(false);

					chckbxClass.setSelected(false);
					chckbxClass.setEnabled(false);

					addAuto=false;
					classMode=false;
				}
			} else {
				// contAssist is on
				chckbxAddAutomatically.setSelected(false);
				chckbxAddAutomatically.setEnabled(false);
				chckbxStepThroughContours.setSelected(false);
				chckbxStepThroughContours.setEnabled(false);
				chckbxContourAssist.setEnabled(true);

				chckbxClass.setSelected(false);
	    		chckbxClass.setEnabled(false);

				editMode=false;
				addAuto=false;
				classMode=false;
			}
		
		// semantic painting annotation type
		} else if(selectedAnnotationType.equals("semantic")){
			// disable contour correction
			addAuto=false;
	    	editMode=false;
	    	contAssist=false;
	    	classMode=false;
	    	chckbxStepThroughContours.setSelected(false);
			chckbxContourAssist.setSelected(false);
			chckbxAddAutomatically.setSelected(false);
			chckbxAddAutomatically.setEnabled(false);
			chckbxContourAssist.setEnabled(false);
			chckbxStepThroughContours.setEnabled(false);
			chckbxClass.setSelected(false);
    		chckbxClass.setEnabled(false);


			// set preferences to preset paint brush tool
			Prefs.set("brush.overlay",true);
			Prefs.set("brush.width",semanticBrushSize);

			// set color and opacity
			int alphaVal=40;
			String currentColorHex=ColorToHex(currentSelectionColor);
			String opacityColor="#66"+currentColorHex;
			IJ.log("--color after: "+opacityColor);

			// this was working before for overlay but not for new frames:
			curToolbar.setForegroundColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));

			IJ.log("checking set color: "+ij.plugin.Colors.colorToString(ij.plugin.Colors.decode(opacityColor,currentSelectionColor)));


			// set the custom brush selection tool:
			//tool = new ij.plugin.tool.BrushTool();
			tool=new BrushToolCustom();
			tool.run("");
			
			// set preferences again
			Prefs.set("brush.overlay",true);
			Prefs.set("brush.width",semanticBrushSize);
			// this was working before for overlay but not for new frames:
			curToolbar.setForegroundColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));

			// brush tool will paint on an overlay which can be showed/hidden
			// TODO: how to save overlay as image?
			// this is now fully implemented

		// bounding box annotation
		} else if(selectedAnnotationType.equals("bounding box")){
			// set rectangle selection tool by default
			curToolbar.setTool(Toolbar.RECTANGLE);
			// disable contour correction
			editMode=false;
	    	contAssist=false;
			chckbxAddAutomatically.setEnabled(true);
			chckbxStepThroughContours.setSelected(false);
			chckbxContourAssist.setSelected(false);
			chckbxStepThroughContours.setEnabled(false);
			chckbxContourAssist.setEnabled(false);
			chckbxClass.setEnabled(true);
		}


		// keep this window instance for key event listening
		imWindow=WindowManager.getWindow(destNameRaw);

		//imWindow.addKeyListener(this);


		// prepare annotation tools
		if (selectedAnnotationType.equals("instance") || selectedAnnotationType.equals("bounding box")){
			// instance segmentation
			if (roisFromArgs && !initializedRoisFromArgs){
				// skipt this roi manager init step
			} else {
				// open ROI manager in bg
				manager = RoiManager.getInstance();
				if (manager == null){
					// display new ROImanager in background
				    //manager = new RoiManager(false);
				    // actually display it
				    manager = new RoiManager();
				}
				else{
					if (roisFromArgs) {
						// roi list was parsed in input args, keep them unchanged
					} else {
						// delete selections from image
						manager.runCommand("Show None");
						//Selection selectionObj=new Selection();
						//selectionObj.run("none");
						manager.close();
						manager=null;
						//manager = new RoiManager(false);
						manager = new RoiManager();
					}
				}
				if (showCnt) {
					manager.runCommand("Show All");
				}
			}

			instance.toFront();
			imWindow.toFront();


			// set key bindings to add new contours by pressing "t" as in ROI manager
			//imWindow.addKeyListener(this);
			KeyListener listener = new KeyListener() {
				//@Override 
				public void keyPressed(KeyEvent event) { 
				    //IJ.log("key was pressed");
				    if (event.getKeyCode()==KeyEvent.VK_SPACE) {
				    	// space is pressed
				    	isSpaceDown=true;
				    	// debug:
				    	//IJ.log("  ---- space down ---- ");
				    }
				}
				 
				//@Override
				public void keyReleased(KeyEvent event) {
					IJ.log("key was released");
					lastKey=event;
					if (event.getKeyCode()==KeyEvent.VK_SPACE) {
				    	// space is released
				    	isSpaceDown=false;
				    	// debug:
				    	//IJ.log("  ---- space up ---- ");
				    }
				    checkKeyEvents(event);
				}
				 
				//@Override
				public void keyTyped(KeyEvent event) {
				    //IJ.log("key was typed");
				}
			};

			// add listeners
			imWindow.addKeyListener(IJ.getInstance());

			WindowManager.getCurrentImage().getCanvas().addMouseListener(runnerInstance);
			WindowManager.getCurrentImage().getCanvas().addMouseWheelListener(runnerInstance);

			WindowManager.getCurrentImage().getCanvas().addKeyListener(listener);

		}


		// add protection against accidental image closing by pressing 'w'
		ImageListenerNew imlisn=new ImageListenerNew();
		imlisn.addImageListenerNew(imp);

		// reset vars
		inAssisting=false;
		acObjects=null;
		buttonOptions.setEnabled(true);

		startedEditing=false;
		origEditedROI=null;

		overlayedROI=false;
		overlayedSemantic=false;
		overlayAdded=false;


		if (saveAnnotTimes) {
			// for annot time saving
			annotTimes=new ResultsTable(); //(100);
			annotTimes.showRowIndexes(false);
			annotTimes.showRowNumbers(false);
			annotCount=0;
			lastStartTime=System.nanoTime();
			// TODO: delete this!!!!!! ^
		}

		closeingOnPurpuse=false;


		// when open function finishes:
		started=true;
    }


    public void closeWindowsAndSave(){
    	if (started) {
			// check if there are rois added:
			if ((manager!=null && manager.getCount()>0) || (selectedAnnotationType.equals("semantic") && imp!=null && imp.getOverlay()!=null)) {
				// offer to save current roi set
				boolean contClosing=closeActiveWindows();

				if (stepping) {
					if (!finishedSaving){ //!contClosing || !finishedSaving
						// wait
						IJ.log("Not done yet");
						//return;
					}
				}

				if (contClosing) {
					// close roimanager
					if (manager!=null) {
						// delete selections from image
						manager.runCommand("Show None");
						//Selection selectionObj=new Selection();
						//selectionObj.run("none");
						manager.close();
						manager=null;
					}

					inAssisting=false;
					startedEditing=false;
					origEditedROI=null;
				}
				
			}

			// check if image was passed as input argument
			if (!imageFromArgs){
				// close image too if open
			    ImageWindow curImageWindow=WindowManager.getCurrentWindow();
			    while (curImageWindow!=null) {
			    	ImagePlus curImp=curImageWindow.getImagePlus();
			    	curImp.changes=false;
			    	curImageWindow.setImage(curImp);
			    	curImageWindow.close();
			    	curImageWindow=WindowManager.getCurrentWindow();
			    }
			    // try to get window by current image name:
				Window curWindow=WindowManager.getWindow(defFile);
				if (curWindow!=null) {
					curWindow.dispose();
				}
			}

			// clear the log window
		    Window logWindow=WindowManager.getWindow("Log");
		    if (logWindow!=null) {
		    	//logWindow.dispose();
		    	IJ.log("\\Clear");
		    	//logWindow.setVisible(false);
		    	logWindow.setVisible(true);
		    }

		}
    }


    // stepping to previous image in folder (open)
    public void prevImage(Runner runnerInstance){

    	if (!started) {
			IJ.showStatus("Use Open to select an image in a folder first");
			MessageDialog notStartedMsg=new MessageDialog(instance,
             "Warning",
             "Use Open to select an image in a folder first");
			return;
		}

		// check if there is a list of images and if we can have a previous image
		if (curFileList!=null && curFileList.length>1) {
			// more than 1 images in the list
			if (curFileIdx>0) {
				// current image is not the first, we can go back
				stepping=true;

				// save current annotation first
				// this is done in openNew() fcn

				// open previous image with Open fcn:
				// set image name:
				curFileIdx-=1;
				defFile=curFileList[curFileIdx];
				//new Runner("Open", imp);
				openNew(runnerInstance);
				imp=WindowManager.getCurrentImage();

				// check if auto mask load is enabled
				if (enableMaskLoad&&autoMaskLoad&&maskFolderInited){
					// load the mask from the selected folder automatically
					loadROIs();
				}

				return;
			}

		}

		// this should not happen due to button inactivation, but handle it anyway:
		// if we get here there is no previous image to open, show message
		IJ.showStatus("There is no previous image in the current folder");
		MessageDialog noPrevImageMsg=new MessageDialog(instance,
         "Warning",
         "No previous image in current folder");
		return;
    }


    // stepping to previous image in folder (open)
    public void nextImage(Runner runnerInstance){
    	if (!started) {
			IJ.showStatus("Use Open to select an image in a folder first");
			MessageDialog notStartedMsg=new MessageDialog(instance,
             "Warning",
             "Use Open to select an image in a folder first");
			return;
		}

		// check if there is a list of images and if we can have a previous image
		if (curFileList!=null && curFileList.length>1) {
			// more than 1 images in the list
			if (curFileIdx<curFileList.length-1) {
				// current image is not the last, we can go forward
				stepping=true;

				// save current annotation first
				// this is done in openNew() fcn

				// open next image with Open fcn:
				// set image name:
				curFileIdx+=1;
				defFile=curFileList[curFileIdx];
				//new Runner("Open", imp);
				openNew(runnerInstance);
				imp=WindowManager.getCurrentImage();

				// check if auto mask load is enabled
				if (enableMaskLoad&&autoMaskLoad&&maskFolderInited){
					// load the mask from the selected folder automatically
					loadROIs();
				}

				return;
			}

		}

		// this should not happen due to button inactivation, but handle it anyway:
		// if we get here there is no previous image to open, show message
		IJ.showStatus("There is no next image in the current folder");
		MessageDialog noNextImageMsg=new MessageDialog(instance,
         "Warning",
         "No next image in current folder");
		return;
    }


    // -------------------
    // save annotation fcn
    // -------------------
    public void saveData(){

    	// check boolean if annot times should be saved to file
    	if (saveAnnotTimes) {
	    	// save annot time in file
	    	String annotFolder=destFolder+File.separator+"annotTimes";
	    	new File(annotFolder).mkdir();
			IJ.log("Created output folder: "+annotFolder);
			String annotFileNameRaw="annotTimes.csv";
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
	    		IJ.log("Saved annotation times in file: "+annotFileName);
	    	// TODO: delete this!!!!!! ^
		}

    	finishedSaving=false;
    	boolean semanticSaving=false;

		if (!started || WindowManager.getCurrentWindow()==null) {
			IJ.showStatus("Open an image and annotate it first");
			MessageDialog notStartedMsg=new MessageDialog(instance,
             "Warning",
             "Click Open to select an image and annotate it first");
			if (stepping) {
				finishedSaving=true;
			}
			return;
		}

		// check if stepping is true and only save if the roi list is not empty
		/*
		if (manager==null || manager.getCount()==0) {
			// empty list, do not save
			IJ.log("Nothing to save yet");
			MessageDialog nothing2saveMsg=new MessageDialog(instance,
             "Info",
             "Nothing to save");
			if (stepping) {
				finishedSaving=true;
			}
			return;
		}
		*/

		IJ.log("saving...");

		if (stepping) {
			finishedSaving=false;
		}

		if (origMaskFileNames==null || !roisFromArgs || !imageFromArgs){

			if (startedClassifying){
				// see if any object was actually classified
				boolean isClassified=false;
				for (int r=0;r<manager.getCount();r++){
					if (manager.getRoi(r).getGroup()>0){
						isClassified=true;
						break;
					}
				}

				if (isClassified){
					// no need for class selection dialog box
					selectedClass="masks";

				} else {

					// ask class name in dialog box
					/*
					// ---- orig dialog box selection starts here
					String[] types2=new String[3];
					types2[0]="normal";
					types2[1]="cancerous";
					types2[2]="other";

					GenericDialog Dialog2 = new GenericDialog("Select class of objects");
					Dialog2.addChoice("class: ", types2, types2[0]);
					Dialog2.showDialog();
					//Vector<String> choices=Dialog2.getChoices();
					selectedClass = Dialog2.getNextChoice();
					//selectedClass = selectClassFcn(Dialog2.getNextChoice());
					// ---- orig dialog box selection ends here
					*/

					// create new frame for optional extra element adding manually by the user (for new custom class):
					ClassSelection classSelectionObj=new ClassSelection();
					classSelectionObj.openClassSelectionFrame();
					
					///*
					synchronized (classSelectionObj){
						try{
							//waiter.wait();
							classSelectionObj.wait();
						} catch (InterruptedException e) {
							IJ.log("Class selection was interrupted");
							e.printStackTrace();
							IJ.log("  >> Exception: "+e.getMessage());
						}
					}
				}
			} else {
				// create new frame for optional extra element adding manually by the user (for new custom class):
				ClassSelection classSelectionObj=new ClassSelection();
				classSelectionObj.openClassSelectionFrame();
				
				///*
				synchronized (classSelectionObj){
					try{
						//waiter.wait();
						classSelectionObj.wait();
					} catch (InterruptedException e) {
						IJ.log("Class selection was interrupted");
						e.printStackTrace();
						IJ.log("  >> Exception: "+e.getMessage());
					}
				}
			}

			
		} else {
			// roi stack was imported, save to mask names
			selectedClass="stack";

			// create export folder:
			String tempFolder="12_TmpRoisFromFiji";
			String exportFolder=exportRootFolderFromArgs+File.separator+tempFolder;
			File outDir=new File(exportFolder);
			if (outDir.exists() && outDir.isDirectory()) {
				// folder already exists
			} else {
				outDir.mkdir();
				IJ.log("Created output folder: "+exportFolder);
			}

			// check the number of elements in roi stacks and file names
			int maskCount=origMaskFileNames.length;
			if(managerList.size()!=maskCount){
				// number of roi sets and mask names does not match!
				IJ.log("The number of roi sets and mask names does not match!");
				return;
			}

			// find file separator in the mask file name string array elements
			String thisFileSep="/";
			int lastIdx=origMaskFileNames[0].lastIndexOf(thisFileSep);
			if (lastIdx<0) {
				thisFileSep="\\";
				lastIdx=origMaskFileNames[0].lastIndexOf(thisFileSep);
			}
			if (lastIdx<0) {
				IJ.log("Cannot find file separator character in the file path\n");
				return;
			}

			for (int mi=0; mi<maskCount; mi++){
				String outputFileName=exportFolder+File.separator+origMaskFileNames[mi].substring(origMaskFileNames[mi].lastIndexOf(thisFileSep)+1,origMaskFileNames[mi].lastIndexOf("."))+"_ROIs.zip";

				manager=managerList.get(mi);
				boolean successfullySavedRoi=manager.runCommand("Save",outputFileName);

				if (!successfullySavedRoi){
					// check if the file was actually saved
					File f=new File(outputFileName);
					if (f.exists() && !f.isDirectory()) {
						if (outputFileName.endsWith(".zip")) {
							// could check with: try and load to see if it is a valid roi .zip file
							// TODO
							successfullySavedRoi=true;
						}
					}
				}

				IJ.log("("+String.valueOf(mi+1)+"/"+String.valueOf(maskCount)+") slice:");

				if (!successfullySavedRoi){
					IJ.log("Failed to save ROI: "+outputFileName);
				}
				else
					IJ.log("Saved ROI: "+outputFileName);
			}

			IJ.log("finished saving");
			finishedSaving=true;
			instance.toFront();

			// show the latest opened roi stack again
			updateROImanager(managerList.get(currentSliceIdx-1),showCnt);

			return;
		}
		
		//*/
		
		if (cancelledSaving) {
			// abort saving
			return;
		}


		IJ.showStatus("Class: "+selectedClass);
		IJ.log("Set class: "+selectedClass);

		// create output folder with the class name
		String destMaskFolder2=destFolder+File.separator+selectedClass;
		new File(destMaskFolder2).mkdir();
		IJ.log("Created output folder: "+destMaskFolder2);

		// set output file name according to annotation type:
		if (selectedAnnotationType.equals("instance")){
			// save ROI.zip there
			destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_ROIs.zip";
		} else if (selectedAnnotationType.equals("bounding box")){
			destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_bboxes.zip";
		} else if (selectedAnnotationType.equals("semantic")){
			destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_semantic.tiff";
			semanticSaving=true;
		}
		if (!semanticSaving) {
			IJ.log("Set output ROI.zip name: "+destName);
		} else {
			IJ.log("Set output binary image name: "+destName);
		}
		

		// check if annotation already exists for this image with this class
		File f = new File(destName);
		if(f.exists() && !f.isDirectory()) {


			Object[] options = {"Yes",
			    "Rename new","Cancel"};


			// check if a saved annotation exists with this name
		    // ask if overwrite it with current
		    int response = JOptionPane.showOptionDialog(null, "Annotation file for this image and class already exists.\nDo you want to overwrite it?", "Confirm overwrite",
		        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,options,options[1]);
		    if (response == JOptionPane.NO_OPTION) {
		    	// rename new
		    	IJ.log("Rename button clicked");

		    	// add a number to the end before extension
		    	if (selectedAnnotationType.equals("instance")){
		    		destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_ROIs_1.zip";
		    	}
		    	else if (selectedAnnotationType.equals("bounding box")){
					destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_bboxes_1.zip";
				}
				else if (selectedAnnotationType.equals("semantic")){
					destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_semantic_1.tiff";
				}
		    	
		    	// loop until a file with the new number appended to the name doesn't exist
		    	int newFileNum=1;
		    	f = new File(destName);
				while(f.exists() && !f.isDirectory()){
					newFileNum+=1;

					if (selectedAnnotationType.equals("instance")){
			    		destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_ROIs_"+String.valueOf(newFileNum)+".zip";
			    	}
			    	else if (selectedAnnotationType.equals("bounding box")){
						destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_bboxes_"+String.valueOf(newFileNum)+".zip";
					}
					else if (selectedAnnotationType.equals("semantic")){
						destName=destMaskFolder2+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+"_semantic_"+String.valueOf(newFileNum)+".tiff";
					}

					
					f = new File(destName);
				}

				if (!semanticSaving) {
					IJ.log("Set output ROI.zip name: "+destName);
				} else {
					IJ.log("Set output binary image name: "+destName);
				}

		    } else if (response == JOptionPane.YES_OPTION) {
		    	// overwrite
		    	IJ.log("Overwrite button clicked");
		    	// proceed with saving
		    } else if (response == JOptionPane.CANCEL_OPTION) {
		    	// cancel, do nothing
		    	if (stepping) {
					finishedSaving=true;
				}
		    	return;
		    }

		}


		// if instance or bbox annotation type: save in roi manager
		if (!semanticSaving){
			boolean successfullySavedRoi=manager.runCommand("Save",destName);

			if (!successfullySavedRoi){
				// check if the file was actually saved
				f=new File(destName);
				if (f.exists() && !f.isDirectory()) {
					if (destName.endsWith(".zip")) {
						// could check with: try and load to see if it is a valid roi .zip file
						// TODO
						successfullySavedRoi=true;
					}
				}
			}

			if (!successfullySavedRoi){
				IJ.log("Failed to save ROI: "+destName);
			}
			else
				IJ.log("Saved ROI: "+destName);


			if (stepping) {
				finishedSaving=true;
			}

		// save a semantic annotation image
		} else {
			// prepare to save the overlayed regions as a new image

			// try to create mask image from the image overlay
			int failCount=0;
			ImagePlus maskImage=null;

			while (failCount<4 && maskImage==null){
				maskImage=startSavingSemantic();

				if (maskImage==null) {
					// failed, try again
					failCount+=1;
					IJ.log("  >> failed saving semantic annotation image "+String.valueOf(failCount)+" time(s)");
				}
			}

			// if failed too many times, show error
			if (failCount>=3) {
				MessageDialog emptySelectionMsg=new MessageDialog(instance,
                	"Error",
                	"Could not create mask image from semantic annotation. Please try again.");
				return;
			}

			// else:
			// save new mask as a tiff
			saveSemanticImage(maskImage, destName);

		}

		IJ.log("finished saving");

		instance.toFront();

		finishedSaving=true;
    }


    ImagePlus startSavingSemantic(){
    	ImagePlus maskImage=null;

    	if (imp==null) {
			imp=WindowManager.getCurrentImage();
		}

		// create a new image
		int[] dimensions=imp.getDimensions();
		int width=dimensions[0];
		int height=dimensions[1];

		long lsize = (long)width*height;
		int size = (int)lsize;
        if (size<0) {
        	IJ.log("0-sized image");
        	//return;
        	return null;
        }
        short[] pixels;

        ImageProcessor maskImageProc=null;
        //ImagePlus maskImage=null;
        pixels = new short[size];

        // it will be filled with black (0) values by default and hopefully isn't displayed in a window
        maskImageProc = new ShortProcessor(width, height, pixels, null);
        if (maskImageProc==null) {
        	IJ.log("could not create a new mask (1)");
        	//return;
        	return null;
        }
        maskImage = new ImagePlus("title", maskImageProc);
        maskImage.getProcessor().setMinAndMax(0, 255);
        //maskImage.setMinAndMax(0, 65535); // 16-bit
        if (maskImage==null) {
        	IJ.log("could not create a new mask (2)");
        	//return;
        	return null;
        }

        // get overlay
        Overlay overlay=imp.getOverlay();

        // set the overlay to the new mask image
		maskImage.setOverlay(overlay);
		//overlayCommandsObj.run("flatten");
		maskImage = maskImage.flatten();

		// process overlay
		WindowManager.setTempCurrentImage(maskImage);
		Converter converterObj=new Converter();
		converterObj.run("8-bit");
		ImageProcessor tmpProc=maskImage.getProcessor();
		tmpProc.xor(0);
		maskImage.setProcessor(tmpProc);

		// use ThresholdAdjuster to create binary image with 0/255 values
		ThresholdAdjuster thresholdAdjuster = new ThresholdAdjuster();
		WindowManager.getWindow("Threshold").setVisible(false);
		Prefs.set("threshold.dark",true); // set background to dark (black)
		Prefs.blackBackground=true;
		//thresholdAdjuster.setLutColor(1); // BLACK_AND_WHITE
		thresholdAdjuster.setMode("B&W");
		Thresholder.setMethod("Otsu");
		Thresholder.setBackground("dark");
		thresholdAdjuster.setMethod("Otsu");
		
		//thresholdAdjuster.apply(maskImage);
		maskImage.show();
		ImageWindow tmpMaskWindow=maskImage.getWindow();
		WindowManager.setCurrentWindow(tmpMaskWindow);
		//(new Thresholder()).run("mask");
		(new Thresholder()).run("skip");
		//thresholdAdjuster.run();

		maskImage.hide();
		imp=WindowManager.getCurrentImage();


		// check if the mask was created successfully
		boolean goodMask=checkEmptyMask(maskImage);

		if (goodMask) {
			return maskImage;
		}
		else {
			return null;	
		}
    }


    // checks if mask creation was successful
    boolean checkEmptyMask(ImagePlus maskImage){
    	if (maskImage==null) {
    		return false;
    	}

    	// get image stats
    	ImageStatistics stats = maskImage.getStatistics();
    	if (stats.min==stats.max) {
    		// empty image
    		return false;
    	} else {
    		return true;
    	}
    }


    boolean saveSemanticImage(ImagePlus maskImage, String outputFileName){
		// save output image to folder
		boolean successfullySaved=IJ.saveAsTiff(maskImage,outputFileName);
		if (successfullySaved) {
			IJ.log("Saved binary image: "+outputFileName);
		} else {
			IJ.log("Failed to save binary image: "+outputFileName);
		}
		return successfullySaved;
	}


	// class selection in saving
	String selectClassFcn(String origChoice){

		String outChoice=null;

		IJ.log("Set class: "+origChoice);
		// add option to create custom classes
		if (origChoice.equals("other")) {
			// new dialog for this
			GenericDialog Dialog4 = new GenericDialog("New class of objects");
			Dialog4.addStringField("new: ", "");
			outChoice = Dialog4.getNextString();
		} else {
			outChoice=origChoice;
		}
		return outChoice;
	}


	// inner class to create a class selection frame
	public class ClassSelection{

		// classSelectionFrame elements:
		private JFrame classSelectFrame;
		private JLabel lblClass;
		private JLabel lblNew;
		private JTextField newTextField;
		private JButton btnClassSelectionOk;
		private JButton btnClassSelectionCancel;
		private JLabel lblSelectClassOf;
		private JComboBox<String> classList;

		private boolean newClassActive;
		private boolean finishedSelection;
		private String prevSelectedClass;
		

		public ClassSelection(){
			boolean newClassActive=false;
			boolean finishedSelection=false;
			String prevSelectedClass=selectedClass;
			//openClassSelectionFrame();
		}

		public void openClassSelectionFrame(){

			classSelectFrame = new JFrame();
			classSelectFrame.setTitle("Select class");
			classSelectFrame.setBounds(100, 100, 230, 200);
			classSelectFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			classSelectFrame.addWindowListener(new WindowAdapter(){
			    public void windowClosing(WindowEvent e){
			        cancelClassSelection();
			    }
			});
			
			Panel classPanel = new Panel();
			classSelectFrame.getContentPane().add(classPanel, BorderLayout.CENTER);
			classPanel.setBackground(SystemColor.control);
			//IJ.log("SystemColor.control: "+Integer.toHexString(SystemColor.control.getRGB()));
			
			// add elements
			lblSelectClassOf = new JLabel("Select class of objects");
			add(lblSelectClassOf);

			lblClass = new JLabel("class:");
			lblClass.setToolTipText("Class of your annotated objects");
			add(lblClass);
			
			lblNew = new JLabel("new:");
			lblNew.setToolTipText("New custom class to create");
			add(lblNew);
			
			newTextField = new JTextField();
			newTextField.setEnabled(false);
			newTextField.setToolTipText("Create new class");
			newTextField.setColumns(10);
			newTextField.setToolTipText("Name of the new class");
			add(newTextField);

			//classList = new JComboBox<String>(new String[] {"normal", "cancerous", "other..."});
			String[] baseClassArray=new String[] {"normal", "cancerous", "other..."};
			String propsClassString=props.getProperty("classes");
			if (propsClassString.equals("normal,cancerous")) {
				// default case
				classList = new JComboBox<String>(baseClassArray);
			} else {
				// set the string array from the split string
				String[] newClassArray=propsClassString.split(",");
				// append the array with the special "other..." option too:
				String[] newClassArray2=new String[newClassArray.length+1];
				for (int ci=0; ci<newClassArray.length; ci++) {
					newClassArray2[ci]=newClassArray[ci];
				}
				newClassArray2[newClassArray2.length-1]="other...";

				classList = new JComboBox<String>(newClassArray2);
			}

			// set highlight colour
			classList.setSelectedIndex(0);
			Color baseWhite=SystemColor.text;
			classList.setBackground(baseWhite);
			Color listSelectedColour=SystemColor.textHighlight;//textHighlight; //activeCaption

			// listen if we need to activate the new class option
			classList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// listen if the options "other..." was selected
					if (classList.getSelectedIndex()==classList.getItemCount()-1) {
						// enable new class text field
						newTextField.setEnabled(true);
						newClassActive=true;
					} else {
						newTextField.setEnabled(false);
						newClassActive=false;
					}
				}
			});
			add(classList);
			
			btnClassSelectionOk = new JButton("OK");
			btnClassSelectionOk.setToolTipText("Continue to save");
			btnClassSelectionOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// collect selected class info and continue saving
					// TODO

					if (newClassActive) {
						// read from the text field
						String textVal=String.valueOf(newTextField.getText());
						if (textVal==null || textVal=="null" || textVal.length()==0) {
							// empty string, warn the user to type something
							finishedSelection=false;
							MessageDialog emptySelectionMsg=new MessageDialog(instance,
			                 "Warning",
			                 "Please enter a new class name or select one from the list to continue.");
							//return;
						} else {
							selectedClass=textVal;
							finishedSelection=true;
							cancelledSaving=false;
							SaveNewProp("classes",textVal);
							closeClassSelectionFrame();
						}
					} else {
						// get from the list
						selectedClass=String.valueOf(classList.getSelectedItem());
						finishedSelection=true;
						cancelledSaving=false;
						closeClassSelectionFrame();
					}

				}
			});
			add(btnClassSelectionOk);
			
			btnClassSelectionCancel = new JButton("Cancel");
			btnClassSelectionCancel.setToolTipText("Cancel saving");
			btnClassSelectionCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// cancel saving altogether
					// TODO

					cancelClassSelection();
				}
			});
			add(btnClassSelectionCancel);


			GroupLayout gl_ClassSelectionPanel = new GroupLayout(classPanel);
			gl_ClassSelectionPanel.setHorizontalGroup(
				gl_ClassSelectionPanel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
						.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.LEADING, false)
							.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
								.addGap(44)
								.addComponent(btnClassSelectionOk)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnClassSelectionCancel))
							.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.LEADING)
									.addComponent(lblClass)
									.addComponent(lblNew))
								.addGap(19)
								.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.LEADING)
									.addComponent(newTextField, 0, 0, Short.MAX_VALUE)
									.addComponent(classList, 0, 107, Short.MAX_VALUE)))
							.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblSelectClassOf)))
						.addContainerGap(41, Short.MAX_VALUE))
			);
			gl_ClassSelectionPanel.setVerticalGroup(
				gl_ClassSelectionPanel.createParallelGroup(Alignment.TRAILING)
					.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
						.addContainerGap(17, Short.MAX_VALUE)
						.addComponent(lblSelectClassOf)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblClass)
							.addComponent(classList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
								.addGap(53)
								.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.BASELINE)
									.addComponent(btnClassSelectionOk)
									.addComponent(btnClassSelectionCancel)))
							.addGroup(gl_ClassSelectionPanel.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_ClassSelectionPanel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblNew)
									.addComponent(newTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
						.addContainerGap())
			);
			classPanel.setLayout(gl_ClassSelectionPanel);


			pack();
			GUI.center(classSelectFrame);
			classSelectFrame.setVisible(true);
			
		}


		public void closeClassSelectionFrame(){
			// close the progress window
			if (classSelectFrame!=null) {
				
				classSelectFrame.dispose();
				classSelectFrame=null;
			
				//waiter.notify();

				synchronized(this) {
				    this.notify();
				}
			}
		}

		public void cancelClassSelection(){
			// remember that saving was cancelled here!
			cancelledSaving=true;
			IJ.log("Cancelled saving");

			selectedClass=prevSelectedClass;
			closeClassSelectionFrame();
		}

	}


	public void SaveNewProp(String propName, String newPropVal){
		if (propName.equals("classes")) {
			// concatenate the new class to the existing class list
			props.setProperty(propName,(props.getProperty(propName)+","+newPropVal));
		} else {
			// overwrite the previous value
			props.setProperty(propName,newPropVal);
		}
		
		// write props to file:
		AnnotatorProperties annotPropsObj=new AnnotatorProperties(this,props);
		boolean successfullyWrittenConfig=annotPropsObj.writeProps(configFileName);
		if (successfullyWrittenConfig) {
			IJ.log("Saved new "+propName+" to config successfully");
		} else {
			IJ.log("Could not save new "+propName+" to config");
		}
	}


	// -------------------
	// load annotation fcn
	// -------------------
	public void loadROIs(){
		if (!started || WindowManager.getCurrentWindow()==null) {
			IJ.showStatus("Open an image first");
			MessageDialog notStartedMsg=new MessageDialog(instance,
             "Warning",
             "Click Open to select an image first");
			return;
		}

		// check if we have annotations in the list before loading anything to it
		int curROInum=manager.getCount();
		IJ.log("Before loading we had "+String.valueOf(curROInum)+" contours");
		int prevROIcount=manager.getCount();
		if (loadedROI) {
			// currently the loaded rois are appended to the current roi list
			// TODO: ask if those should be deleted first
		}

		// file open dialog
		//Opener opener3=new Opener();
		
		

		// check if masks can be loaded (false by default)
		if (enableMaskLoad){
			// import ROIs from masks
			String loadedROIfolder=null;
			if (maskFolderInited){
				// no need to open the dialog again
				loadedROIfolder=maskFolderInitedPath;
			} else {
				// browse mask folder
				DirectoryChooser opener4=new DirectoryChooser("Select folder of mask files");
				loadedROIfolder=opener4.getDirectory();
			}
			
			// construct file name: [image name].tiff
			String loadedROIname=defFile.substring(0,defFile.lastIndexOf("."))+".tiff";
			// check if exists
			File fx = new File(loadedROIfolder+File.separator+loadedROIname);
		    if (!(fx.exists() && !fx.isDirectory())){
		    	// mask file doesn't exist
		    	IJ.log("Mask image "+loadedROIname+" does not exist");
		    	return;
		    }

			ImagePlus importedMask=IJ.openImage(loadedROIfolder+File.separator+loadedROIname);

			boolean successfullyImportedROIs=importROIsFromMaskImage(importedMask);
			if (!successfullyImportedROIs){
				IJ.log("Failed to import ROIs from mask file: "+loadedROIname);
				return;
			} else {
				IJ.log("Imported ROIs from mask file: "+loadedROIname);
				maskFolderInited=true;
				maskFolderInitedPath=loadedROIfolder;
			}
			WindowManager.setCurrentWindow(imp.getWindow());

			if (showCnt)
				manager.runCommand("Show All");
			else
				manager.runCommand("Show None");

		} else {
			// normal way, import ROI.zip file
			OpenDialog opener4=new OpenDialog("Select an annotation (ROI) .zip file",null);
			String loadedROIfolder=opener4.getDirectory();
			String loadedROIname=opener4.getFileName();
			//opener3.open(destFolder+File.separator+destNameRaw);
		
			boolean loadedROIsuccessfully=manager.runCommand("Open",loadedROIfolder+File.separator+loadedROIname);
			if (!loadedROIsuccessfully) {
				IJ.log("Failed to open ROI: "+loadedROIname);
				MessageDialog failed2loadROIMsg=new MessageDialog(instance,
                 "Error",
                 "Failed to open ROI .zip file");
				return;
			} else {
				IJ.log("Opened ROI: "+loadedROIname);
			}
		}

		loadedROI=true;
		curROInum=manager.getCount();
		IJ.log("After loading we have "+String.valueOf(curROInum)+" contours");


		// rename the loaded contours if there were previous contours added

		// name the new roi by its number in the list:
    	int lastNumber=0;
		if (prevROIcount>0) {
			String lastName=manager.getRoi(prevROIcount-1).getName();
    		lastNumber=Integer.parseInt(lastName);
		} else {
			// no rois yet, use 0
		}
		if (curROInum>0) {
			// new contours loaded, need to rename them
			for (int i=prevROIcount; i<curROInum; i++) {
				manager.rename(i,String.format("%04d",lastNumber+1));
				lastNumber+=1;
			}
		}

		// check if the rois have class info saved
		boolean isClassified=false;
		for (int r=prevROIcount;r<curROInum;r++){
			if (manager.getRoi(r).getGroup()>0){
				isClassified=true;
				break;
			}
		}

		if (isClassified){
			// set class vars accordingly
			classFrameNames=new ArrayList<String>();
			classFrameColours=new ArrayList<Integer>();

			for (int r=prevROIcount;r<curROInum;r++){
				int tmpGroup=manager.getRoi(r).getGroup();
				String tmpGroupName="Class_"+String.format("%02d",tmpGroup);
				//debug:
				//IJ.log(tmpGroupName);
				if (tmpGroup>0 && !classFrameNames.contains(tmpGroupName)){
					classFrameNames.add(tmpGroupName);
					//int tmpGroupColourIdx=getClassColourIdx(manager.getRoi(r).getStrokeColor());
					//Color tmpColour=null;
					//tmpColour=manager.getRoi(r).getFillColor();
					//if (tmpColour==null)
						Color tmpColour=manager.getRoi(r).getStrokeColor();
					//Color tmpColour2=new Color(tmpColour.getRed(),tmpColour.getGreen(),tmpColour.getBlue());
					int tmpGroupColourIdx=getClassColourIdx(tmpColour); //tmpColour2);
					if (tmpGroupColourIdx==-1)
						tmpGroupColourIdx=0;

					// assign a free colour to the new class
					if (!classFrameColours.contains(tmpGroupColourIdx))
						classFrameColours.add(tmpGroupColourIdx);
					else {
						for (int i=0;i<8;i++){
							if (!classFrameColours.contains(i)){
								// found first free colour, take it
								classFrameColours.add(i);
								break;
							}
						}
					}
					
					if (selectedClassNameNumber<0){
						selectedClassNameNumber=tmpGroup;
					}
					//debug:
					IJ.log(">>> import: added class '"+tmpGroupName+"' with colour '"+tmpGroupColourIdx+"'");
				}
			}

			///*
			// colour import doesn't work, repaint all contours to the "imported" colours
			for (int r=prevROIcount;r<curROInum;r++){
				Roi tmpROI=manager.getRoi(r);
				int tmpGroup=tmpROI.getGroup();
				if (tmpGroup>0){
					int tmpIdx=classFrameColours.get(classFrameNames.indexOf("Class_"+String.format("%02d",tmpGroup)));
					//IJ.log("group: "+String.valueOf(tmpGroup)+"\t|\ttmpIdx: "+String.valueOf(tmpIdx));
					tmpROI.setStrokeColor(getClassColour(tmpIdx));
					manager.setRoi(tmpROI,r);
				} else {
					tmpROI.setStrokeColor(currentSelectionColor);
				}
			}

			//*/

			if (classFrameNames.size()==0 && classFrameColours.size()==0){
				// no class info was found in the rois
				classFrameNames=null;
				classFrameColours=null;
			}
			startedClassifying=true;
		}
	}
	

	// contour assist using classical region growing
	Roi contourAssist(ImagePlus imp, Roi initROI, double intensityThresh, int distanceThresh){
		Roi assistedROI=(Roi) initROI.clone();
		// TODO

		// create image from selection
		imp=WindowManager.getCurrentImage();
		if (imp==null) {
			IJ.log("No image open, returning");
			return null;
		}

		int[] dimensions=imp.getDimensions();
		int width=dimensions[0];
		int height=dimensions[1];

		long lsize = (long)width*height;
		int size = (int)lsize;
        if (size<0) {
        	IJ.log("0-sized image");
        	return null;
        }
        byte[] pixels;

        ImageProcessor maskImageProc=null;
        ImagePlus maskImage=null;
        pixels = new byte[size];

        // it will be filled with black (0) values by default and hopefully isn't displayed in a window
        maskImageProc = new ByteProcessor(width, height, pixels, null);
        if (maskImageProc==null) {
        	IJ.log("could not create a new mask (1)");
        	return null;
        }
        maskImage = new ImagePlus("title", maskImageProc);
        maskImage.getProcessor().setMinAndMax(0, 255);
        //maskImage.setMinAndMax(0, 65535); // 16-bit
        if (maskImage==null) {
        	IJ.log("could not create a new mask (2)");
        	return null;
        }

        maskImage.getProcessor().setColor(255);
		maskImage.getProcessor().fill(initROI);

		// here we have a black image with the current selection filled in white

		
		byte[] tmpPixels=(byte[])maskImage.getProcessor().getPixelsCopy();
		ImageProcessor tmpProc=new ByteProcessor(width, height, tmpPixels, null);
		ImagePlus tmp=new ImagePlus("tmp",tmpProc);
		ImagePlus tmp2=new ImagePlus("tmp2",tmpProc);

		// erode selection mask by 2 pixels first to enable shrinking too but only a little bit
		// run erosion simply:
		maskImageProc=maskImage.getProcessor();
		int countErode=3; // # of nearest neighbours
		int backgroundErode=0; // background intensity
		((ByteProcessor)maskImageProc).erode(countErode, backgroundErode);
		((ByteProcessor)maskImageProc).erode(countErode, backgroundErode);
		maskImage.setProcessor(maskImageProc);

		// check if there are pixels left on the image
		double maxVal=maskImage.getProcessor().getMax();
		if (maxVal<1) {
			// empty image, replace with original
			maskImage=new ImagePlus("maskImage",tmpProc);
		}

		// initialize the mask as the marked region
		maskImage.setTitle("maskImage");
		maskImage.show();
		ImageWindow tmpMaskWindow=maskImage.getWindow();
		WindowManager.setCurrentWindow(tmpMaskWindow);
		//(new Thresholder()).run("mask");
		(new Thresholder()).run("skip");
		//thresholdAdjuster.run();

		maskImage.hide();


		// convert image to [0,1] range
		// instead calculate new intensityThreshVal based on the image type
		int origImageType=imp.getType();
		int maxOrigVal=0;
		boolean colourful=false;
		switch (origImageType) {
			case ImagePlus.GRAY8:
				maxOrigVal=255;
				break;
			case ImagePlus.GRAY16:
				maxOrigVal=65535;
				break;
			case ImagePlus.GRAY32:
				maxOrigVal=(int)1.0;
				MessageDialog floatImgMsg=new MessageDialog(instance,
                 "Error",
                 "Current image is of type float in range [0,1].\nType not supported in suggestion mode.");
				return null;
			case ImagePlus.COLOR_256:
				// 8-bit indexed image
				maxOrigVal=255;
				MessageDialog floatImgMsg2=new MessageDialog(instance,
                 "Error",
                 "Current image is of type indexed colour image.\nType not supported in suggestion mode.");
				return null;
				//break;
			case ImagePlus.COLOR_RGB:
				// 32-bit RGB colour image
				maxOrigVal=255;
				colourful=true;
				break;
			default:
				maxOrigVal=255;
				break;
		}

		// calculate the intensity threshold from image type
		double[] computedIntensityThreshVal=new double[3];
		if (!colourful) {
			computedIntensityThreshVal[0]=intensityThreshVal*(double)maxOrigVal;
			IJ.log("--assist: computed intensity thresh: "+String.valueOf(computedIntensityThreshVal[0]));
		} else {
			computedIntensityThreshVal[0]=intensityThreshValR*(double)maxOrigVal;
			computedIntensityThreshVal[1]=intensityThreshValG*(double)maxOrigVal;
			computedIntensityThreshVal[2]=intensityThreshValB*(double)maxOrigVal;
			IJ.log("--assist: computed intensity thresh: ("+String.valueOf(computedIntensityThreshVal[0])+","+String.valueOf(computedIntensityThreshVal[1])+","+String.valueOf(computedIntensityThreshVal[2])+")");
		}
		
		
		// collect seed region points + values
		// find number of seed points first
		int c=0;
		//if (!colourful) {
			double sumVal=0;
		//} else {
			double[] sumVals=new double[3];
		//}
		
		ArrayList<Integer> maskSeedsX=new ArrayList<Integer>();
		ArrayList<Integer> maskSeedsY=new ArrayList<Integer>();
		ArrayList<Integer> maskSeedVals=new ArrayList<Integer>();
		ImageProcessor impImageProc=imp.getProcessor();
		maskImageProc=maskImage.getProcessor();
		for (int i=0; i<width; i++){
		    for (int j=0; j<height; j++){
		    	if (maskImageProc.getPixelValue(i,j)>0) {

			    	// get current pixel value by type of image:
		       		int curPixelVal=0;
		       		int[] vals=new int[4];
		       		if (!colourful) {
	       				// grayscale image
	       				vals=imp.getPixel(i,j);
	       				curPixelVal=vals[0];

	       				sumVal+=curPixelVal;
	       			} else {
	       				// RGB image
	       				vals=imp.getPixel(i,j);
	       				// TODO !!!!!!!!!!!!
	       				// this is just taking the red channel:
	       				curPixelVal=vals[0];

	       				sumVals[0]+=vals[0];
	       				sumVals[1]+=vals[1];
	       				sumVals[2]+=vals[2];
		       		}

			        //if (curPixelVal>0){
			        	maskSeedsX.add(i);
			        	maskSeedsY.add(j);
			        	maskSeedVals.add(curPixelVal);
			            c+=1;
			            //sumVal+=curPixelVal;
			            // debug:
			            //IJ.log("--assist: adding pixel with value: "+String.valueOf(curPixelVal));
			        //}
		        }
		    }
		}

		// get mean value of already marked region
		double[] avg=new double[3];
		if (!colourful) {
			avg[0]=sumVal/c;
			IJ.log("--assist: mean pixel value of initial region: "+String.valueOf(avg)+" | # pixels in region: "+String.valueOf(maskSeedVals.size())+" | sum pixel value: "+String.valueOf(sumVal));
		} else {
			avg[0]=sumVals[0]/c;
			avg[1]=sumVals[1]/c;
			avg[2]=sumVals[2]/c;
			IJ.log("--assist: mean pixel value of initial region: ("+String.valueOf(avg[0])+","+String.valueOf(avg[1])+","+String.valueOf(avg[2])+") | # pixels in region: "+String.valueOf(c)+" | sum pixel value: ("+String.valueOf(sumVals[0])+","+String.valueOf(sumVals[1])+","+String.valueOf(sumVals[2])+")");
		}
		


		// find seed points for region growing as the contour of the initial region:

		// run erosion simply:
		ImageProcessor tmpImageProc=tmp2.getProcessor();
		((ByteProcessor)tmpImageProc).erode(countErode, backgroundErode);
		tmp2.setProcessor(tmpImageProc);

		// find their (x,y) coords:
		ArrayList<Integer> tmpi=new ArrayList<Integer>();
		ArrayList<Integer> tmpj=new ArrayList<Integer>();
	
		// calculate binary maskImage [and] ([not] tmp2) image: ring
		tmp2.setTitle("tmp2");
		//tmp2.show();
		ObjectDump objDump=combineFirstAND_NOTsecondImage(tmp2,maskImage,tmpi,tmpj);
		ImagePlus ring=objDump.getImage();
		if (ring==null) {
			return null;
		}
		tmpi=objDump.getListFirst();
		tmpj=objDump.getListSecond();
		// debug msg:
		//IJ.log("tmpi size: "+String.valueOf(tmpi.size())+" | tmpj size: "+String.valueOf(tmpj.size()));
		ring.setTitle("ring");
		//ring.show();

		// list of (x,y) points
		ArrayList<Integer> maskPointsX=castToAnything(maskSeedsX.clone());
		ArrayList<Integer> maskPointsY=castToAnything(maskSeedsY.clone());


		// while the list is not empty
		while (tmpi.size()>0 && tmpj.size()>0){
		    // get the 1st element
		    int curi=tmpi.get(0);
		    int curj=tmpj.get(0);
		    tmpi.remove(0);
		    tmpj.remove(0);

		    ArrayList<Integer> newi=new ArrayList<Integer>();
			ArrayList<Integer> newj=new ArrayList<Integer>();

		    // grow the region for this point with th threshold
		    ObjectDump objDump2=growRegion(imp,origImageType,curi,curj,computedIntensityThreshVal,distanceThreshVal,maskPointsX,maskPointsY,
		    	maskSeedsX,maskSeedsY,avg,maskImage,newi,newj);

		    // fetch output variables from object dump class obj
		    newi=objDump2.getListFirst();
		    newj=objDump2.getListSecond();
		    tmpi=objDump2.getListThird();
		    tmpj=objDump2.getListFourth();
		    maskImage=objDump2.getImage();

		    // add coords to list of points to check for growing
		    for (int i=0; i<newi.size(); i++) {
		    	tmpi.add(newi.get(i));
		    	tmpj.add(newj.get(i));
		    }
		}

		// binarize maskImage again
		// TODO

		// fill holes in mask
		maskImageProc=maskImage.getProcessor();
		ij.plugin.filter.Binary binaryObj=new ij.plugin.filter.Binary();
		binaryObj.setup("fill",maskImage);
		binaryObj.run(maskImageProc);
		maskImage.setProcessor(maskImageProc);

		//maskImage.show();

		// after everything is done: the new binary image must be converted to selection (Roi) and displayed on the image
		// create selection command, ThresholdToSelection class
		assistedROI=ThresholdToSelection.run(maskImage);

		return assistedROI;
	}


	// compute mean of values in a list
	double mean(ArrayList<Integer> list){
		Integer sum = 0;
		if(!list.isEmpty()) {
			for (Integer element : list) {
			    sum += element;
			}
			return sum.doubleValue() / list.size();
		}
		return sum;
	}


	// binary mask operation to create a ring
	ObjectDump combineFirstAND_NOTsecondImage(ImagePlus first,ImagePlus second, ArrayList<Integer> listi, ArrayList<Integer> listj){
		int[] dimensions;
		int width=0;
		int height=0;
		dimensions=first.getDimensions();
		width=dimensions[0];
		height=dimensions[1];

		int[] dimensions2=second.getDimensions();

		if (width!=dimensions2[0] || height!=dimensions2[1]) {
			IJ.log("Images are not the same size, returing");
			return null;
		}

		// create output image
		long lsize = (long)width*height;
		int size = (int)lsize;
        if (size<0) {
        	IJ.log("0-sized image");
        	return null;
        }
        byte[] pixels;

        ImageProcessor maskImageProc=null;
        ImagePlus maskImage=null;
        pixels = new byte[size];

        // it will be filled with black (0) values by default and hopefully isn't displayed in a window
        maskImageProc = new ByteProcessor(width, height, pixels, null);
        if (maskImageProc==null) {
        	IJ.log("could not create a new mask (1)");
        	return null;
        }
        maskImage = new ImagePlus("title", maskImageProc);
        maskImage.getProcessor().setMinAndMax(0, 255);
        //maskImage.setMinAndMax(0, 65535); // 16-bit
        if (maskImage==null) {
        	IJ.log("could not create a new mask (2)");
        	return null;
        }

        maskImageProc=maskImage.getProcessor();

        // for every pixel do the binary operation
		for (int i=0; i<width; i++){
			for (int j=0; j<height; j++){
				double newValue=0.0;
				if (first.getProcessor().getPixel(i,j)>0 && second.getProcessor().getPixel(i,j)==0) {
					newValue=255;
					listi.add(i);
					listj.add(j);
				}
				maskImageProc.putPixelValue(i,j,newValue);
			}
		}

		maskImage.setProcessor(maskImageProc);

		ObjectDump objDumpTmp=new ObjectDump(listi,listj,maskImage);
		return objDumpTmp;
		//return maskImage;
	}


	@SuppressWarnings("unchecked")
	public static <T> T castToAnything(Object obj) {
	    return (T) obj;
	}


	// region growing
	public ObjectDump growRegion(ImagePlus imp, int imgType, int curx, int cury, double[] computedIntensityThreshVal, int distanceThreshVal, ArrayList<Integer> maskPointsX, ArrayList<Integer> maskPointsY, ArrayList<Integer> maskSeedsX, ArrayList<Integer> maskSeedsY, double[] avg, ImagePlus outMask, ArrayList<Integer> newx, ArrayList<Integer> newy){

		// get list of 4/8 connected pixels to (x,y)
	    // 4-neighbours + 8-neighbours
	    int[][] conns=new int[][]{
	    	{-1,0}, {1,0}, {0,-1}, {0,1},
	        {-1,-1}, {-1,1}, {1,1}, {1,-1}};

	    int[] dimensions;
		int width=0;
		int height=0;
		dimensions=imp.getDimensions();
		width=dimensions[0];
		height=dimensions[1];

		// for outputting new pixels
	    //newx;
	    //newy;

		// loop through neighbours
	    for (int k=0; k<8; k++){
	        int cx=curx+conns[k][0];
	        int cy=cury+conns[k][1];

	        if (cx<1 || cy<1 || cx>width || cy>height || outMask.getProcessor().getPixel(cx,cy)>0){
            	// out of image idx or already marked on output mask
            	continue;
       		}

       		// get current pixel value by type of image:
       		double curv=0.0;
       		boolean colourful=false;
       		int[] vals=new int[4];
       		switch (imgType){
       			case ImagePlus.GRAY8:
       			case ImagePlus.GRAY16:
       				// grayscale image
       				vals=imp.getPixel(cx,cy);
       				curv=(double)vals[0];
       				break;
       			case ImagePlus.COLOR_RGB:
       				// RGB image
       				vals=imp.getPixel(cx,cy);
       				// compare the diffs for all 3 channels separately
       				colourful=true;
       				break;
       			default:
       				// dont care about type
       				curv=(double)imp.getProcessor().getPixel(cx,cy);
       				break;
       		}
       		
       		if (!colourful) {
       			if (Math.abs(curv-avg[0])>computedIntensityThreshVal[0]){
		            // pixel value diff from mean is too large, skip it
		            // debug:
		            //IJ.log("--assist: gray value too different: "+String.valueOf(curv)+" | at ("+String.valueOf(cx)+","+String.valueOf(cy)+")");
		            continue;
		        }
       		} else {
       			double curvR=(double)vals[0];
       			double curvG=(double)vals[1];
       			double curvB=(double)vals[2];
       			// for green we can be more tolerant
       			if (Math.abs(curvR-avg[0])>computedIntensityThreshVal[0] || Math.abs(curvG-avg[1])>computedIntensityThreshVal[1] || Math.abs(curvB-avg[2])>computedIntensityThreshVal[2]){
		            // pixel value diff from mean is too large, skip it
		            // debug:
		            //IJ.log("--assist: colour value too different: ("+String.valueOf(curvR)+","+String.valueOf(curvR)+","+String.valueOf(curvR)+") | at ("+String.valueOf(cx)+","+String.valueOf(cy)+")");
		            continue;
		        }
       		}
	        

	        // see min distance from any point of the mask
	        double d=minDistance(maskSeedsX,maskSeedsY,cx,cy);

	        if (Math.abs(d)>distanceThreshVal){
	            // distance is too large, skip it
	            // debug:
	            //IJ.log("--assist: pixel too far: "+String.valueOf(d)+" | at ("+String.valueOf(cx)+","+String.valueOf(cy)+")");
	            continue;
	        }

	        // else, add this point to the list
	        newx.add(cx);
	        newy.add(cy);

	        //IJ.log("--assist: growing region at ("+String.valueOf(cx)+","+String.valueOf(cy)+")");
	        outMask.getProcessor().putPixelValue(cx,cy,255);

	        maskPointsX.add(cx);
	        maskPointsY.add(cy);

	    }

	    ObjectDump objDumpTmp=new ObjectDump(newx,newy,maskPointsX,maskPointsY,outMask);
        return objDumpTmp;
	}


	// compute min distance
	public double minDistance(ArrayList<Integer> pointsX, ArrayList<Integer> pointsY, int x, int y){
		// points is a list of (x,y) coords as ints

		// Manhattan distance
		int count=pointsX.size();
		int[] distsum=new int[count];
		for (int i=0; i<pointsX.size(); i++) {
			int curx=pointsX.get(i);
			int cury=pointsY.get(i);
			
			int distx=Math.abs(curx-x);
			int disty=Math.abs(cury-y);

			distsum[i]=distx+disty;
		}
	    
	    int minv=Arrays.stream(distsum).min().getAsInt();
	    return minv;
	}


	// contour assist using U-Net
	Roi contourAssistUNet(ImagePlus imp, Roi initROI, double intensityThresh, int distanceThresh, String modelJsonFile, String modelWeightsFile) throws IOException,UnsupportedKerasConfigurationException,InvalidKerasConfigurationException{
		//Roi assistedROI=(Roi) initROI.clone();
		Roi assistedROI=null;
		invertedROI=null;
		ROIpositionX=0;
		ROIpositionY=0;
		IJ.log("  >> started assisting...");

		int[] dimensions;
		int width=0;
		int height=0;
		dimensions=imp.getDimensions();
		width=dimensions[0];
		height=dimensions[1];

		// see if the image is RGB or not
		int origImageType=imp.getType();
		int maxOrigVal=0;
		boolean colourful=false;
		switch (origImageType) {
			case ImagePlus.GRAY8:
				maxOrigVal=255;
				IJ.log("Image type: GRAY8");
				break;
			case ImagePlus.GRAY16:
				maxOrigVal=65535;
				IJ.log("Image type: GRAY16");
				break;
			case ImagePlus.GRAY32:
				maxOrigVal=(int)1.0;
				MessageDialog floatImgMsg11=new MessageDialog(instance,
                 "Error",
                 "Current image is of type float in range [0,1].\nType not supported in suggestion mode.");
				IJ.log("Image type: GRAY32");
				return null;
			case ImagePlus.COLOR_256:
				// 8-bit indexed image
				maxOrigVal=255;
				MessageDialog floatImgMsg22=new MessageDialog(instance,
                 "Error",
                 "Current image is of type indexed colour image.\nType not supported in suggestion mode.");
				IJ.log("Image type: COLOR_256");
				return null;
				//break;
			case ImagePlus.COLOR_RGB:
				// 32-bit RGB colour image
				maxOrigVal=255;
				colourful=true;
				IJ.log("Image type: COLOR_RGB");
				break;
			default:
				maxOrigVal=255;
				IJ.log("Image type: default");
				break;
		}

		

		// get the bounding box of the current roi
		Rectangle initBbox=initROI.getBounds(); //initROI.setLocation(x,y,width,height); <-- after the new selection is ready!
		// allow x pixel growth for the new suggested contour
		double doubleDistanceThresh=distanceThreshVal;
		//initROI=RoiEnlarger.enlarge(initROI,doubleDistanceThresh); // grow by distance thresh pixels
		Roi tmpROI=RoiEnlarger.enlarge(initROI,doubleDistanceThresh); // grow by distance thresh pixels
		Rectangle tmpBbox=tmpROI.getBounds();
		IJ.log("tmpROI bounds: ("+String.valueOf(tmpBbox.getX())+","+String.valueOf(tmpBbox.getY())+") "+String.valueOf(tmpBbox.getWidth())+"x"+String.valueOf(tmpBbox.getHeight()));



		// load trained unet model
		if (trainedUNetModel!=null) {
			// model already loaded
		} else {
			// load model
			
			// model loading was here, moved to its own fcn now
			trainedUNetModel=loadUNetModel(modelJsonFile,modelWeightsFile);

		}
		
		ImagePlus maskImage=null;

		// check if this image has a valid prediction
		if (!(curPredictionImage==null || curOrigImage==null)) {
			// check current image for equality too
			String[] imageTitles=WindowManager.getImageTitles();
			for (String title : imageTitles) {
				if (title.equals("title")) {
					// temp image, ignore it
					continue;
				} else if (title.equals(defFile)) {
					// current image window
					//ImagePlus curImageTmp=WindowManager.getImage(defFile);
					ImagePlus curImageTmpIP=WindowManager.getImage(defFile);
					ImageProcessor curImageTmp=curImageTmpIP.getProcessor();
					if (!imageFromArgs){
						// normal image
						//if (curImageTmp.getProcessor().equals(curOrigImage.getProcessor())) {
						if (curImageTmp.equals(curOrigImage)) {
							// it is the same, no changes applied, we can continue using the previous prediction on it
							IJ.log("  >> using previous predicted image");

							maskImage = new ImagePlus("title", curPredictionImage);
							maskImage.show();
						} else {
							IJ.log("  >> current image does not match the previous predicted original image");
						}
					}
					else {
						// stack
						if (curImageTmp.equals(curOrigImage)) {
							// it is the same, no changes applied, we can continue using the previous prediction on it
							IJ.log("  >> using previous predicted image");

							maskImage = new ImagePlus("title", curPredictionImage);
							maskImage.show();
						} else {
							IJ.log("  >> current image does not match the previous predicted original image");
							curPredictionImage=null;
							//return contourAssistUNet(imp,initROI,intensityThresh,distanceThresh,modelJsonFile,modelWeightsFile);
							return null;
						}
					}
				}
			}

		} else {
			// need to predict

			// show a dialog informing the user that prediction is being executed and wait
			// false to make in non-modal
			HTMLDialog predictionStartedDialog=new HTMLDialog("Suggesting contour, please wait...","Creating suggested contour, please wait...",false);


			//curOrigImage=WindowManager.getImage(defFile);
			ImagePlus curOrigImageIP=WindowManager.getImage(defFile);
			curOrigImage=curOrigImageIP.getProcessor();
			if (curOrigImage==null) {
				MessageDialog curImageNotfound=new MessageDialog(instance,
                 "Error",
                 "Cannot find image");
				return null;
			}


			// ------------------------------------------------------------------------------------
			// original way of preparing input image for nd4j prediction with padding
			/*
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


			// predict current image with loaded model
			// function definition: public INDArray[] output(boolean train, INDArray... input);
			INDArray[] inputs=new INDArray[1];

			// initialize image with zeros
			INDArray thisImage=null;
			
			if (need2pad)
				thisImage=Nd4j.zeros(1,3,widthx,heightx); // padded row x col
			else
				thisImage=Nd4j.zeros(1,3,width,height); // row x col

			int[] vals=new int[4];
			double curv=0.0;
			// fill image with values fetched from "imp"
			for (int i=0; i<width; i++) {
				for (int j=0; j<height; j++) {
					if (colourful) {
						// RGB image
						vals=imp.getPixel(i,j);

						for (int ch=0; ch<3; ch++) {
							int[] idxs=new int[]{0,ch,i,j};
							curv=(double)vals[ch];
							thisImage.putScalar(idxs,curv);
						}
					} else {
						// grayscale image
						vals=imp.getPixel(i,j);

	       				curv=(double)vals[0];
						for (int ch=0; ch<3; ch++) {
							int[] idxs=new int[]{0,ch,i,j};
							thisImage.putScalar(idxs,curv);
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
								thisImage.putScalar(idxs,curv);
							}
					}
				}

			}
			*/
			// ------------------------------------------------------------------------------------

			// new way of preparing input image for prediction with resizing
			// predict current image with loaded model
			// function definition: public INDArray[] output(boolean train, INDArray... input);
			INDArray[] inputs=new INDArray[1];

			// initialize image with zeros
			INDArray thisImage=null;

			String fileName=null;
			if (!imageFromArgs)
				fileName=destFolder+File.separator+destNameRaw;
			if (fileName==null){
				// how to read the file?

				String importFolder=null;
				if (exportRootFolderFromArgs!=null)
					importFolder=exportRootFolderFromArgs;
				else{
					IJ.log("Cannot find root data folder to read images from");
					return null;
				}

				// find file separator in the mask file name string array elements
				String thisFileSep="/";
				int lastIdx=origImageFileNames[currentSliceIdx-1].lastIndexOf(thisFileSep);
				if (lastIdx<0) {
					thisFileSep="\\";
					lastIdx=origImageFileNames[currentSliceIdx-1].lastIndexOf(thisFileSep);
				}
				if (lastIdx<0) {
					IJ.log("Cannot find file separator character in the file path\n");
					return null;
				}

				int startidx=0;
				if (origImageFileNames[currentSliceIdx-1].substring(0,1).equals(thisFileSep)){
					startidx=1;
				}

				// construct output file name:
				fileName=importFolder+File.separator+origImageFileNames[currentSliceIdx-1].substring(startidx,origImageFileNames[currentSliceIdx-1].lastIndexOf(thisFileSep))+File.separator+origImageFileNames[currentSliceIdx-1].substring(origImageFileNames[currentSliceIdx-1].lastIndexOf(thisFileSep)+1,origImageFileNames[currentSliceIdx-1].length());

				//IJ.log(">>> input image was parsed from arg, contour assist support not implemented yet");
				File tmpFile=new File(fileName);
				if (!tmpFile.exists()||tmpFile.isDirectory()){
					IJ.log("Cannot load current slice image for prediction");
					return null;
				}
			}
			INDArray arr2= new NativeImageLoader().asMatrix(new File(fileName),false);
            NativeImageLoader loader = new NativeImageLoader(arr2.shape()[1], arr2.shape()[2], 3, new ColorConversionTransform(COLOR_BGR2RGB));
            arr2 = loader.asMatrix(new File(fileName),false);
            thisImage=Nd4j.zeros(1,arr2.shape()[1],arr2.shape()[2],3); // row x col
            for (int i = 0; i < arr2.shape()[1]; i++) {
                for (int j = 0; j < arr2.shape()[2]; j++) {
                    for (int ch = 0; ch < 3; ch++) {
                        int[] idxs;
                        if (colourful){
                            idxs = new int[]{0,  i, j, ch};
                            thisImage.putScalar(idxs, arr2.getDouble(idxs));
                        } else {
                            idxs = new int[]{0,  i, j};
                            int[] idxs2=new int[]{0,  i, j, ch};
                            thisImage.putScalar(idxs2, arr2.getDouble(idxs));
                        }  
                    }
                }
            }


			IJ.log("  >> input image prepared...");

			// divide image by 255!!!!!!!!!!!!!!!
			thisImage.divi(255);

			// add image to "inputs" array
			// orig way:
			//inputs[0]=thisImage;
			// new way:
			INDArray size = Nd4j.createFromArray(new int[]{256,256});
            ResizeBicubic op = new ResizeBicubic(thisImage, size, false, false );
            INDArray[] res = Nd4j.exec(op);
            inputs[0]=res[0];

			// debug:
			IJ.log("  >> input image size: "+thisImage.size(0)+" x "+thisImage.size(1)+" x "+thisImage.size(2)+" x "+thisImage.size(3));
			IJ.log("  >> input array size: "+inputs.length);
			
			// expects rank 4 array with shape [miniBatchSize,layerInputDepth,inputHeight,inputWidth]
			INDArray[] predictions=trainedUNetModel.output(inputs); //(false,inputs);
			IJ.log("  >> prediction done...");
			INDArray predictedImage=predictions[0];

			// debug:
			// show output
			/*
			int h4 = (int)predictedImage.size(2);
	        int w4 = (int)predictedImage.size(3);
			BufferedImage bi = new BufferedImage(h4, w4, BufferedImage.TYPE_BYTE_GRAY);
			int[] ia = new int[1];
	        
	        for( int i=0; i<h4; i++ ){
	            for( int j=0; j<w4; j++ ){
	                int value = (int)(255 * predictedImage.getDouble(0, 0, i, j));
	                ia[0] = value;
	                bi.getRaster().setPixel(i,j,ia);
	            }
	        }
					        
	        ImagePlus debugimg=new ImagePlus("DL4J",bi);
	        debugimg.show();
	        */


			// TODO: create imageJ image from the prediction
			// this is probably a 16-bit image

	        // -----------------------------------------------------------
	        // orig way:
	        /*
			// create an empty output image
			// / *
			long lsize = (long)width*height;
			int size = (int)lsize;
	        if (size<0) {
	        	IJ.log("0-sized image");
	        	return null;
	        }
	        byte[] pixels;
	        // * /
	        short[] pixels2;
	        //float[] pixels3;

	        ImageProcessor maskImageProc=null;
	        //ImagePlus maskImage=null;
	        maskImage=null;
	        pixels = new byte[size];
	        pixels2 = new short[size];
	        //pixels3 = new float[size];

	        // it will be filled with black (0) values by default and hopefully isn't displayed in a window
	        maskImageProc = new ShortProcessor(width, height, pixels2, null);
	        //maskImageProc = new FloatProcessor(width, height, pixels3, null);
	        
	        maskImage = new ImagePlus("title", maskImageProc);
	        maskImage.getProcessor().setMinAndMax(0, 255);
	      	//maskImage.getProcessor().setMinAndMax(0, 65535); // 16-bit
	        if (maskImage==null) {
	        	IJ.log("could not create a new mask (2)");
	        	return null;
	        }

	        //maskImage.getProcessor().setColor(255);
			//maskImage.getProcessor().fill(initROI);


			// -----
			// debug:
			//ImageProcessor predIm2showProc = new FloatProcessor(width, height, new float[size], null);
			//predIm2showProc.setMinAndMax(0, 65535); // 16-bit
			//IJ.log("********debug: prediction size: "+String.valueOf(predictions.length));
			// -----


			// fill image with predicted values
			// threshold the prediction so we can convert it to roi later
			for (int i=0; i<width; i++) {
				for (int j=0; j<height; j++) {
					curv=predictedImage.getDouble(0,0,i,j);
					maskImage.getProcessor().putPixelValue(i,j,curv*255);
					// debug:
					//predIm2showProc.putPixelValue(i,j,curv);
					if (curv>255/2)
						maskImageProc.putPixelValue(i,j,255);
					//else
					//	maskImageProc.putPixelValue(i,j,0);
				}
			}
			*/
			// -----------------------------------------------------------

			// new way:
			size = Nd4j.createFromArray(new int[]{(int)thisImage.shape()[1],(int)thisImage.shape()[2]});
	        ResizeBicubic op_back = new ResizeBicubic(predictedImage, size, false, false );
	        INDArray[] out_resized = Nd4j.exec(op_back);
	        BufferedImage bi = imageFromINDArray(out_resized[0]);
	        maskImage = new ImagePlus("title", bi);


			IJ.log("  >> predicted image processed...");
			maskImage.show();

			if (predictionStartedDialog!=null) {
				predictionStartedDialog.dispose();
			}
				



	        // store prediction image until this image is closed/ new image is opened
			curPredictionImage=maskImage.getProcessor();
			curPredictionImageName=defFile;


			// -----
			// debug:
			//ImagePlus predIm2show=new ImagePlus("prediction",predIm2showProc);
			//predIm2show.show();
			//return null;
			// ----


	    }
		// -------- here we have a valid prediction image and file name

	    
	    // crop initROI + distanceTresh pixels bbox of the predmask
		///*
		maskImage.setRoi(tmpROI);
		Resizer resizerObj=new Resizer();
		resizerObj.run("crop");
		Roi emptyRoi=null;
		//maskImage.getProcessor().setColor(255);
		//maskImage.getProcessor().fill(tmpROI.getInverse(maskImage));
		maskImage.setRoi(emptyRoi);
		//*/

		ImageConverter converter=new ImageConverter(maskImage);
		converter.convertToGray8();
		(new Thresholder()).run("skip");

		// see if the mask needs to be inverted:
		if (checkIJMatrixCorners(maskImage)) {
			// need to invert it
			IJ.log("  >> need to invert mask: true");
			maskImage.setProcessor(invertImage(maskImage.getProcessor()));
			(new Thresholder()).run("skip");
		}
		



		// -------- active contour method starts here ------------
		// moved to its own fcn

		// after everything is done: the new binary image must be converted to selection (Roi) and displayed on the image
		// create selection command, ThresholdToSelection class
		Roi intermediateRoi=ThresholdToSelection.run(maskImage);
		if (intermediateRoi!=null)
			IJ.log("  >> orig ROI type: "+intermediateRoi.getTypeAsString());
		
		// run active contour fitting:
		// not here!
		/*
	   	assistedROI=runActiveContourFitting(maskImage,intermediateRoi,tmpBbox,imp);
	    if (assistedROI!=null)
			IJ.log("  >> ac ROI type: "+assistedROI.getTypeAsString());
		*/

		// store objects needed to run active contour fitting for later
		//acObjects=new ACobjectDump(maskImage,intermediateRoi,tmpBbox,imp);
		acObjects=new ACobjectDump(new ImagePlus(maskImage.getTitle(),maskImage.getProcessor().duplicate()),intermediateRoi,tmpBbox,new ImagePlus(imp.getTitle(),imp.getProcessor().duplicate()));

		// check if there is an output from ac as a roi
		if (assistedROI==null || !(assistedROI.getType()==Roi.FREEROI || assistedROI.getType()==Roi.COMPOSITE || assistedROI.getType()==Roi.TRACED_ROI)) {
			// failed to produce a better suggested contour with AC than we had with unet before, revert to it
			IJ.log("Failed to create new contour with active contours, showing U-Net prediction");
			//assistedROI=ThresholdToSelection.run(maskImage);
			//postProcessAssistedROI(assistedROI,tmpBbox,maskImage,true,imp,true);
			assistedROI=intermediateRoi;
			assistedROI=postProcessAssistedROI(assistedROI,tmpBbox,maskImage,true,imp,true);
			// also reset the inverted roi
			//invertedROI=invertRoi(intermediateRoi,maskImage);
			//invertedROI=(invertedROI instanceof ShapeRoi) ? createRoi((ShapeRoi)invertedROI) : invertedROI;
			if (assistedROI==invertedROI) {
				IJ.log("Failed to invert current roi (same)");
			}
			if (invertedROI==null) {
				IJ.log("  null ROI on line #3822");
			}
		}

		
		// roi positioning was done here, moved to its own fcn

		Window curWindow=WindowManager.getWindow("title");
		if (curWindow!=null) {
			// close image window
			maskImage.changes=false;
			maskImage.getWindow().close();
		}
		WindowManager.setCurrentWindow(imp.getWindow());
		// set main imwindow var to the original image
		//imWindow=WindowManager.getWindow(destNameRaw);
		imp.getWindow().toFront();


		return assistedROI;
	}


	private BufferedImage imageFromINDArray(INDArray array) {
        long[] shape = array.shape();

        int height = (int) shape[1];
        int width = (int) shape[2];
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int gray = (int) (255 * array.getDouble(0, y, x, 0));

                // handle out of bounds pixel values
                gray = Math.min(gray, 255);
                gray = Math.max(gray, 0);

                image.getRaster().setSample(x, y, 0, gray);
            }
        }
        return image;
    }


	// get the largest roi if multiple objects were detected on the mask
	Roi selectLargestROI(Roi ROI2check){
		if (ROI2check.getType()!=Roi.COMPOSITE) {
			// only one object remained in the roi, can continue
		} else {
			// need to select the largest from the list
			int maxSize=0;
			int maxIdx=0;
			Roi[] manyROIs = ((ShapeRoi)ROI2check).getRois();
	        for (int i=0; i<manyROIs.length; i++) {
	        	Roi thisROI=manyROIs[i];
	        	int thisPixelCount=thisROI.size();
	        	if (thisPixelCount>maxSize) {
	        		// check if the current largest object has already been added to the roi list
	        		if (isROIadded2list(thisROI)) {
	        			continue;
	        		}
	        		maxSize=thisPixelCount;
	        		maxIdx=i;
	        	}
	        }
	        // here we have the largest object index as maxIdx
	        ROI2check=manyROIs[maxIdx];
	    }
	    return ROI2check;
	}


	// avoid suggesting duplicate objects by checking the current roi list
	boolean isROIadded2list(Roi ROI2check){
		boolean isAdded=false;

		// the list is the default roi manager list
		if (manager==null) {
			IJ.log("No ROImanager found");
			manager=new RoiManager(false);
			return isAdded;
		}

		// loop through the list
		int roiCount=manager.getCount();
		if (roiCount==0) {
			// empty list
			return isAdded;
		}
		// convert the current roi in the list to ShapeRoi so we can calculate its intersection with ROI2check
		Roi[] manyROIs=manager.getRoisAsArray();
		for (int i=0; i<manyROIs.length; i++) {
        	Roi tmpROI=manyROIs[i];
        	ShapeRoi thisROI=null;
        	if (tmpROI instanceof PolygonRoi) {
        		Polygon poly=tmpROI.getPolygon();
        		thisROI=new ShapeRoi(poly);
        	} else {
        		thisROI=(ShapeRoi)tmpROI;
        	}
        	Roi intersection=null;
        	if (ROI2check instanceof PolygonRoi) {
        		//ROI2check=new ShapeRoi(ROI2check.getPolygon());
        		ShapeRoi tmpROI2check=new ShapeRoi(ROI2check.getPolygon());
        		intersection=(tmpROI2check).and(thisROI).shapeToRoi();
        	} else {
        		// check for accidental shaperoi class of object
        		// this should never happen
	        	ROI2check=(ROI2check instanceof ShapeRoi) ? createRoi((ShapeRoi)ROI2check) : ROI2check;
	        	// this fails when trying to cast a Roi to ShapeRoi:
	        	//intersection=((ShapeRoi)ROI2check).and(thisROI).shapeToRoi();
	        	intersection=(new ShapeRoi(ROI2check)).and(thisROI).shapeToRoi();
        	}

        	// check the intersection
        	if (intersection==null) {
        		// no overlap
        		return isAdded;
        	}
        	int intersectionPixelCount=intersection.size();
        	int thisPixelCount=ROI2check.size();
        	double overlap=(double)intersectionPixelCount/(double)thisPixelCount;
        	if (overlap>0.4) {
        		// >40% overlap, skip this object!
        		isAdded=true;
        		break;
        	}
        }

		return isAdded;
	}


	double[][] transposeArray(double[][] orig){
		int rows=orig.length;
		int cols=orig[0].length;
		double[][] outArray=new double[cols][rows];
		
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				outArray[j][i]=orig[i][j];
			}
		}
		return outArray;	
	}


	int[][] convert2intArray(boolean[][] orig){
		int rows=orig.length;
		int cols=orig[0].length;
		int[][] outArray=new int[rows][cols];
		
		for (int i=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				outArray[i][j]=orig[i][j]?255:0;
			}
		}
		return outArray;
	}


	// active contour fitting after contour suggestion step
	public Roi runActiveContourFitting(ImagePlus maskImage, Roi intermediateRoi, Rectangle tmpBbox, ImagePlus imp){

		Roi assistedROI=null;

		// show dialog that a calculation is running
		HTMLDialog predictionStartedDialog2=new HTMLDialog("Info","Fitting suggested contour to object...",false);


		// show the unet suggested contour on the image while processing continues
	    //Roi intermediateRoi=ThresholdToSelection.run(maskImage);
	    intermediateRoi=postProcessAssistedROI(intermediateRoi,tmpBbox,maskImage,false,imp,true);
	    imp.setRoi(intermediateRoi);
	    //maskImage.show();
	    WindowManager.setCurrentWindow(maskImage.getWindow());



		// ---- use active contour (matlab implementation) to fit the contour to the object more precisely

		Object[] result = null;
		// this is the active contour class
      	runAC_Class snakeObj = null;

		// runSnake2D is the fcn, params:
		// 1: needed
		// imageMatrix: original image (grayscale/RGB)
		// points: initial contour points <-- collect these from the previous mask image
		// method: ['basic','gvf'] --> use gvf
		// []: iterations ("" means use default: 300/400 by method)
		// []: gvf iterations ("" means use default: 600)
        //result = snakeObj.runSnake2D(1,imageMatrix,points,method,"","");

		// prepare inputs
		/*
		// original image as int matrix
		int[][] imageMatrix=imp.getProcessor().getIntArray();
		// convert to short
		short[][] shortMatrix=new short[imageMatrix.length][imageMatrix[0].length];
		for (int i=0; i<imageMatrix.length; i++) {
			for (int j=0; j<imageMatrix[0].length; j++) {
				shortMatrix[i][j]=(short)imageMatrix[i][j];
			}
		}
		*/

		

		// contour point list as 2-by-points matrix
		//double[][] pointsTemp=new double[2][5000];

		int[] dimensionsBin;
		int widthBin=0;
		int heightBin=0;
		dimensionsBin=maskImage.getDimensions();
		widthBin=dimensionsBin[0];
		heightBin=dimensionsBin[1];
		long lsize2 = (long)widthBin*heightBin;
		int size2 = (int)lsize2;
        byte[] pixels222;
        pixels222 = new byte[size2];

        ///*
        byte[] tmpPixels=(byte[])maskImage.getProcessor().getPixelsCopy();
		ByteProcessor maskBinary=new ByteProcessor(widthBin,heightBin,tmpPixels,null);
		//*/

		//short[] tmpPixels=(short[]) maskImage.getProcessor().getPixelsCopy();
		//ImageProcessor maskBinary=new ShortProcessor(widthBin,heightBin,tmpPixels,null);
		ImagePlus impBin=new ImagePlus("binarymask",maskBinary);
		
		/*
		maskBinary.invert(); // for unknown reason the image gets inverted, so we need to invert it again
		maskBinary.outline();
		maskBinary.skeletonize(); // to have truly 1-pixel-width outline
		*/

		//impBin.show();
		

		// get outline
		// ---- this is only needed for the runSnake2D fcn ----
		/*
		maskBinary=(ByteProcessor) impBin.getProcessor();
		//maskBinary=impBin.getProcessor();
		ij.plugin.filter.Binary binaryObj=new ij.plugin.filter.Binary();
		binaryObj.setup("outline",impBin);
		binaryObj.run(maskBinary);
		impBin.setProcessor(maskBinary);

		maskBinary=(ByteProcessor) impBin.getProcessor();
		//maskBinary=impBin.getProcessor();
		binaryObj=new ij.plugin.filter.Binary();
		binaryObj.setup("skel",impBin);
		binaryObj.run(maskBinary);
		impBin.setProcessor(maskBinary);
		*/
		// ---- this is only needed for the runSnake2D fcn ----


		// calculate indices for crop
		int startX=(int)tmpBbox.getX();
		int startY=(int)tmpBbox.getY();
		int endX=(int)tmpBbox.getWidth();
		int endY=(int)tmpBbox.getHeight();

		// make sure the sizes match
		IJ.log("bin:   "+String.valueOf(widthBin)+" x "+String.valueOf(heightBin));
		IJ.log("image: "+String.valueOf(endX)+" x "+String.valueOf(endY));

		// if the sizes dont match, return null
		if (widthBin!=endX || heightBin!=endY) {
			IJ.log("Size mismatch in processing, failure");
			if (predictionStartedDialog2!=null) {
				predictionStartedDialog2.dispose();
			}
			Roi emptyRoi=null;
			imp.setRoi(emptyRoi);
			return null;
		}

		int[][] imageMatrix=imp.getProcessor().getIntArray();
		// convert to short
		short[][] shortMatrix=new short[endX][endY];
		for (int i=startX; i<endX; i++) {
			for (int j=startY; j<endY; j++) {
				shortMatrix[i][j]=(short)imageMatrix[i][j];
			}
		}
		

		// new mask image as int matrix
		int[][] imageMatrix2=impBin.getProcessor().getIntArray();
		// convert to short
		short[][] shortMatrix2=new short[imageMatrix2.length][imageMatrix2[0].length];
		for (int i=0; i<imageMatrix2.length; i++) {
			for (int j=0; j<imageMatrix2[0].length; j++) {
				shortMatrix2[i][j]=(short)imageMatrix2[i][j];
			}
		}



		// find generated skel movie frames and close them
		Window curWindow=WindowManager.getWindow("Skel Movie");
		if (curWindow!=null) {
			curWindow.dispose();
		}
		

		/*
		int contourPointCount=0;
		for (short i=0; i<widthBin; i++) {
			for (short j=0; j<heightBin; j++) {
				int curPixelVal=maskBinary.get(i,j);
				if (curPixelVal>0) {
					pointsTemp[0][contourPointCount]=i;
					pointsTemp[1][contourPointCount]=j;
					contourPointCount+=1;
				}
			}
		}
		*/

		// close temp mask windows
		curWindow=WindowManager.getWindow("binarymask");
		if (curWindow!=null) {
			impBin.changes=false;
			impBin.getWindow().close();

			curWindow=WindowManager.getWindow("binarymask");
			if (curWindow!=null)
				curWindow.dispose();
		}

		// only needed for runSnake2D
		/*
		double[][] points=new double[2][contourPointCount];
		for (int k=0; k<2; k++) {
			///*
			//int[] tmpOrig=pointsTemp[k];
			//int[] tmpNew=new int[tmpOrig.length];
			//System.arrayCopy(tmpOrig,0,tmpNew,contourPointCount);
			//points[k]=tmpNew;
			//*
			System.arraycopy(pointsTemp[k],0,points[k],0,contourPointCount);
		}
		*/

		String method="GVF";
		// use default iterations
		
		IJ.log("  >> starting GVF...");
		//IJ.log("toolbar color before runAC: "+Integer.toHexString(Toolbar.getForegroundColor().getRGB()));
		
        try {
        	snakeObj = new runAC_Class();
			//result = snakeObj.runSnake2D(1,imageMatrix,points,method,"","");
			// this worked syntactically:
			//result = snakeObj.runSnake2D(1,shortMatrix,transposeArray(points),method,"","");

			// run simple ac method
			int iterations=200;
			double smoothFactor=0.5;

			result = snakeObj.runAC(1,shortMatrix,shortMatrix2,iterations,smoothFactor);
		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			IJ.log("  >> Exception: "+e.getMessage());
			return null;
		} catch (Exception e){
			e.printStackTrace();
			IJ.log("  >> Exception: "+e.getMessage());
			return null;
		}
        IJ.log("  >> done");
        //IJ.log("toolbar color after runAC: "+Integer.toHexString(Toolbar.getForegroundColor().getRGB())); 
        //System.out.println(result[0]);
        MWLogicalArray matArray=(MWLogicalArray)result[0];


		boolean[][] activeArrayBin=(boolean[][]) matArray.toArray();
		int[][] activeArray=convert2intArray(activeArrayBin);

		IJ.log("  >> preparing output image...");

		// probably result[0] contains the output binary mask as an int[][] --> create imageproc from it
        
        ByteProcessor activeBinary=new ByteProcessor(widthBin,heightBin,pixels222);
        activeBinary.setBackgroundValue(0.0);

        // check if the mask needs to be inverted
        boolean need2invertMatrix=checkMatrixCorners(activeArray);
        IJ.log("  >> need to invert matrix: "+String.valueOf(need2invertMatrix));

        for (int i=0; i<widthBin; i++) {
			for (int j=0; j<heightBin; j++) {
				int activeVal=activeArray[i][j];
				if (activeVal>0 && !need2invertMatrix)
					activeVal=255;
				else
					activeVal=0;
				activeBinary.set(i,j,activeVal);
			}
		}

		ImagePlus impBinActive=new ImagePlus("activecontour",activeBinary);
		impBinActive.show();
		WindowManager.setCurrentWindow(impBinActive.getWindow());
        
		
		IJ.log("  >> done");

		// TODO:
		// change maskImage to impBinActive a few lines below!!!!!!!!!

		// ---- active contour fitting done



        // after everything is done: the new binary image must be converted to selection (Roi) and displayed on the image
		// create selection command, ThresholdToSelection class
		//assistedROI=ThresholdToSelection.run(maskImage);

		// run it on the new active contour image instead:
		ImageConverter converter=new ImageConverter(impBinActive);
		converter.convertToGray8();
		(new Thresholder()).run("skip");

		// see if the mask needs to be inverted:
		if (checkIJMatrixCorners(impBinActive)) {
			// need to invert it
			IJ.log("  >> need to invert mask: true");
			impBinActive.setProcessor(invertImage(impBinActive.getProcessor()));
			(new Thresholder()).run("skip");
		}
		

		assistedROI=ThresholdToSelection.run(impBinActive);
		if(assistedROI!=null){
			//impBinActive.setRoi(assistedROI);
			IJ.log("  >> ac contour created");
			//assistedROI=postProcessAssistedROI(assistedROI,tmpBbox,impBinActive,true,imp,true);
			assistedROI=postProcessAssistedROI(assistedROI,tmpBbox,maskImage,true,imp,true);
		}
		else
			IJ.log("  >> ac contour is null");
		


		// close active contour windows
		///*
		curWindow=WindowManager.getWindow("activecontour");
		if (curWindow!=null) {
			impBinActive.changes=false;
			impBinActive.getWindow().close();

			curWindow=WindowManager.getWindow("activecontour");
			if (curWindow!=null)
				curWindow.dispose();
		}
		//*/

		// close the process running dialog box
		if (predictionStartedDialog2!=null) {
			predictionStartedDialog2.dispose();
		}

		return assistedROI;
	}


	public Roi postProcessAssistedROI(Roi assistedROI, Rectangle tmpBbox, ImagePlus maskImage, boolean closeMaskIm, ImagePlus imp, boolean storeRoiCoords){

		// validate current ROI and check if it needs to be inverted
		assistedROI=validateROI(assistedROI,maskImage);

		// close image window
		/*
		maskImage.changes=false;
		maskImage.getWindow().close();
		*/

		if (assistedROI==null) {
			IJ.log("  >> failed to create new contour");
			if (closeMaskIm) {
				// close image window
				maskImage.changes=false;
				if (maskImage.getWindow()!=null) {
					maskImage.getWindow().close();
				}
			}
			invertedROI=null;
			
			IJ.log("  null ROI on line #3909");
			
		} else {
			Rectangle assistedBbox=assistedROI.getBounds();
			IJ.log("assistedROI bounds: ("+String.valueOf(assistedBbox.getX())+","+String.valueOf(assistedBbox.getY())+") "+String.valueOf(assistedBbox.getWidth())+"x"+String.valueOf(assistedBbox.getHeight()));

			// store an inverted roi for later option to change
			invertedROI=assistedROI.getInverse(maskImage);
			//invertedROI=invertRoiForce(assistedROI,maskImage);
			//invertedROI=checkInvertedRoi(invertedROI,assistedROI,maskImage);
			IJ.log("Stored inverse ROI");
			if (invertedROI==null) {
				IJ.log("  null ROI on line #4309");
			} else {
				if (assistedROI.getMask().getPixelsCopy().equals(invertedROI.getMask().getPixelsCopy())) {
					IJ.log("Failed to invert current roi (same)");
				}
			}
				

			// store the coordinates of the roi's bounding box
			if (storeRoiCoords) {
				
				ROIpositionX=tmpBbox.getX();
				ROIpositionY=tmpBbox.getY();
				IJ.log("ROIposition (X,Y): "+String.valueOf(ROIpositionX)+","+String.valueOf(ROIpositionY));
			}

			//invertedROI=(invertedROI instanceof ShapeRoi) ? createRoi((ShapeRoi)invertedROI) : invertedROI;
			//invertedROI=checkInvertedRoi(invertedROI,assistedROI,maskImage);
			if (invertedROI==null) {
				IJ.log("  null ROI on line #4328");
			}
			

			if (closeMaskIm) {
				// close image window
				maskImage.changes=false;
				if (maskImage.getWindow()!=null) {
					maskImage.getWindow().close();
				}
			}


			// place new ROI on the new mask
			WindowManager.setCurrentWindow(imp.getWindow());
			//assistedROI.setLocation(initBbox.getX(),initBbox.getY()); //,initBbox.getWidth(),initBbox.getHeight()
			assistedROI.setLocation(ROIpositionX+assistedBbox.getX(),ROIpositionY+assistedBbox.getY());	
		}

		return assistedROI;
	}


	public Roi invertRoi(Roi origRoi, ImagePlus mask){
		Roi invRoi=null;
		invRoi=origRoi.getInverse(mask);
		return invRoi;
	}


	// converting a ShapeRoi to Roi
	public Roi createRoi(ShapeRoi ROI2convert){
		
		IJ.log("  >> converting ROI class");
		Roi[] tmprois=ROI2convert.getRois();
		Roi convertedROI=tmprois[0];
		/*
		// add all of them to this roi
		int i=1;
		for (i=1; i<tmprois.length; i++) {
			convertedROI.previousRoi=tmprois[i];
			convertedROI.update(true,false); // adds the current roi to the composite roi
		}
		if (i>1)
			IJ.log("  >> collected "+String.valueOf(i-1)+" rois");
		*/

		if (convertedROI==null && tmprois.length>1) {
			int i=1;
			while (convertedROI!=null && i<tmprois.length){
				convertedROI=tmprois[i];
				i++;
			}
		}
		if (convertedROI==null) {
			// still empty, do something
			IJ.log("  >> Could not convert ROI: Empty ROI");
		}
		return convertedROI;
	}


	/**
	* Checks if the inverted roi was created properly and tries to invert the 
	* original roi again if it failed (null or full image).
	* @param invRoi 	inverted ROI to check for fautly invertion
	* @param origRoi 	original ROI for invertion if needed
	* @return 			a correctly inverted ROI
	**/
	public Roi checkInvertedRoi(Roi invRoi, Roi origRoi, ImagePlus mask){

		Roi out=null;
		boolean failed=false;
		ImagePlus thisim=null;
		// check if the inverted roi is empty
		if (invRoi==null) {
			IJ.log("Failed to invert ROI: null");
			failed=true;
		} else if (invRoi.size()<1) {
			IJ.log("Failed to invert ROI: empty");
			failed=true;
		}

		// get the image correspondin to the inverted roi
		if (!failed) {
			thisim=invRoi.getImage();
		}
		if (thisim==null) {
			thisim=origRoi.getImage();
			if (thisim==null) {
				thisim=mask;
				if (thisim==null) {
					IJ.log("Cannot find image data for inverted roi");
					return out;
				}
			}
		}
		int w=thisim.getWidth();
		int h=thisim.getHeight();
		// check if the full mask was selected
		if (!failed) {
			int wxh=w*h;
			int roiPixelNum=invRoi.size();
			if (roiPixelNum==wxh) {
				IJ.log("Failed to invert ROI: full mask selected");
				failed=true;
			}
		}
		
		// check size
		if (!failed) {
			ImageProcessor tmpinvProc=invRoi.getMask();
			ImageProcessor tmporigProc=origRoi.getMask();
			int winv=tmpinvProc.getWidth();
			int hinv=tmpinvProc.getHeight();
			int worig=tmporigProc.getWidth();
			int horig=tmporigProc.getHeight();

			if (!(winv==worig && hinv==horig)){
				IJ.log("Failed to invert ROI: size doesnt match original");
				failed=true;
			} else if (invRoi.getMask().getPixel(0,0)==origRoi.getMask().getPixel(0,0)) {
				// it is not inverted
				IJ.log("Failed to invert ROI: same as original");
				// debug:
				/*
				ImagePlus tmpinv=new ImagePlus("inv",tmpinvProc);
				tmpinv.show();
				ImagePlus tmporig=new ImagePlus("orig",tmporigProc);
				tmporig.show();
				*/
				failed=true;
			}
		}
		
		if (failed) {
			// try to invert the orig roi properly
			ImagePlus tmpBin=new ImagePlus("checkingInv",new ByteProcessor(w,h));
			//tmpBin.setRoi(origRoi);
			tmpBin.getProcessor().setColor(255);
			tmpBin.getProcessor().fill(origRoi);
			ImageProcessor tmpBinProc=tmpBin.getProcessor();
			tmpBinProc.invert();
			tmpBin.setProcessor(tmpBinProc);
			// threshold2selection
			tmpBin.show();
			//WindowManager.setCurrentWindow(tmpBin.getWindow());
			ImageConverter converter=new ImageConverter(tmpBin);
			converter.convertToGray8();
			(new Thresholder()).run("skip");

			// see if the mask needs to be inverted:
			if (checkIJMatrixCorners(tmpBin)) {
				// need to invert it
				IJ.log("  >> need to invert mask: true");
				tmpBin.setProcessor(invertImage(tmpBin.getProcessor()));
				(new Thresholder()).run("skip");
			}

			// convert to selection
			out=ThresholdToSelection.run(tmpBin);
			tmpBin.changes=false;
			tmpBin.close();
			if (out==null) {
				IJ.log("***** This ROI cannot be inverted");
			} else {
				Rectangle origBbox=origRoi.getBounds();
				IJ.log("origROI bounds: ("+String.valueOf(origBbox.getX())+","+String.valueOf(origBbox.getY())+") "+String.valueOf(origBbox.getWidth())+"x"+String.valueOf(origBbox.getHeight()));
				out.setLocation(origBbox.getX(),origBbox.getY());
			}
		} else {
			out=invRoi;
		}
		return out;
	}


	// this is wrong:
	public Roi invertRoiForce(Roi origRoi, ImagePlus mask){
		Roi outRoi=invertRoi(origRoi,mask);
		if (outRoi instanceof ShapeRoi || outRoi==null) {
			// failed to invert the roi
			IJ.log("forcing invert");
			//ImageProcessor tmpImageProc=mask.getProcessor();
			ImageProcessor tmpImageProc=origRoi.getMask();
			tmpImageProc=invertImage(tmpImageProc); // "invert" the image
			mask.setProcessor(tmpImageProc);
			// set inverted image's mask to as new roi
			ij.plugin.Selection selectionObj=new ij.plugin.Selection();
			selectionObj.run("from");
		}
		return outRoi;
	}


	public ImageProcessor invertImage(ImageProcessor orig){
		int w=orig.getWidth();
		int h=orig.getHeight();
		ImageProcessor out=new ByteProcessor(w,h);

		int maxVal=(int)orig.getMax();
		int minVal=0;

		for (int i=0; i<w; i++) {
			for (int j=0; j<h; j++) {
				out.putPixel(i, j, orig.getPixel(i, j)>0 ? minVal : maxVal);
			}
		}
		return out;
	}


	public Roi validateROI(Roi assistedROI, ImagePlus maskImage){

		if (assistedROI!=null && assistedROI.getType()==Roi.COMPOSITE) {
			// select the largest found object and delete all others
			assistedROI=selectLargestROI(assistedROI);
		}

		// check if we have a valid roi now, else return null
		if (assistedROI!=null) {

			Rectangle curBbox=assistedROI.getBounds();

			// check if the corner points are included
			int cornerCount=0;
			///*
			if (assistedROI.containsPoint(0.0,0.0)) {
				// top left corner
				cornerCount+=1;
				IJ.log("     (0,0) corner");
			}
			if (assistedROI.containsPoint(0.0,curBbox.getWidth())) {
				// ? top right corner
				cornerCount+=1;
				IJ.log("     (0,+) corner");
			}
			if (assistedROI.containsPoint(curBbox.getHeight(),0.0)) {
				// ? lower left corner
				cornerCount+=1;
				IJ.log("     (+,0) corner");
			}
			if (assistedROI.containsPoint(curBbox.getHeight(),curBbox.getWidth())) {
				// ? lower right corner
				cornerCount+=1;
				IJ.log("     (+,+) corner");
			}
			//*/

			// try with int values
			/*
			if (assistedROI.contains(0,0)) {
				// top left corner
				cornerCount+=1;
				IJ.log("     (0,0) corner");
			}
			if (assistedROI.contains(0,(int) Math.ceil(curBbox.getWidth()))) {
				// ? top right corner
				cornerCount+=1;
				IJ.log("     (0,+) corner");
			}
			if (assistedROI.contains((int) Math.ceil(curBbox.getHeight()),0)) {
				// ? lower left corner
				cornerCount+=1;
				IJ.log("     (+,0) corner");
			}
			if (assistedROI.contains((int) Math.ceil(curBbox.getHeight()),(int) Math.ceil(curBbox.getWidth()))) {
				// ? lower right corner
				cornerCount+=1;
				IJ.log("     (+,+) corner");
			}
			*/

			if (cornerCount>1) {
				// at least 2 corners of the crop are included in the final roi, invert it!
				// store an inverted roi for later option to change
				//invertedROI=assistedROI;

				assistedROI=assistedROI.getInverse(maskImage);
				invertedROI=assistedROI.getInverse(maskImage);
				//assistedROI=invertRoiForce(assistedROI,maskImage);
				//assistedROI=checkInvertedRoi(assistedROI,invertedROI,maskImage);
				IJ.log("  >> inverted ROI");
				if (assistedROI.getMask().getPixelsCopy().equals(invertedROI.getMask().getPixelsCopy())) {
					IJ.log("Failed to invert current roi (same)");
				}

				if (invertedROI==null) {
					IJ.log("  null ROI on line #4137");
				}
			}


			// select the largest found object and delete all others
			assistedROI=selectLargestROI(assistedROI);

		}
		return assistedROI;
	}


	// to check if the mask needs to be inverted count the corners marked
	// it at least 2 corners are marked, the mask should be inverted
	public boolean checkMatrixCorners(int[][] matrix){
		boolean need2invert=false;
		int w=matrix.length;
		int h=matrix[0].length;

		int cornerCount=0;
		if (matrix[0][0]>0) {
			cornerCount+=1;
		}
		if (matrix[0][h-1]>0) {
			cornerCount+=1;
		}
		if (matrix[w-1][0]>0) {
			cornerCount+=1;
		}
		if (matrix[w-1][h-1]>0) {
			cornerCount+=1;
		}

		if (cornerCount>1) {
			// need to invert the image as the background is white
			need2invert=true;
		}
		return need2invert;
	}


	// do the same for an imagej matrix
	public boolean checkIJMatrixCorners(ImagePlus matrix){
		boolean need2invert=false;
		int w=matrix.getWidth();
		int h=matrix.getHeight();

		int cornerCount=0;
		if (matrix.getProcessor().getPixelValue(0,0)>0) {
			cornerCount+=1;
		}
		if (matrix.getProcessor().getPixelValue(0,h-1)>0) {
			cornerCount+=1;
		}
		if (matrix.getProcessor().getPixelValue(w-1,0)>0) {
			cornerCount+=1;
		}
		if (matrix.getProcessor().getPixelValue(w-1,h-1)>0) {
			cornerCount+=1;
		}

		if (cornerCount>1) {
			// need to invert the image as the background is white
			need2invert=true;
		}
		return need2invert;
	}


	/*
	public void selectLargestCC(ImagePlus mask){
		ByteProcessor proc=null;
		ByteProcessor out=new ByteProcessor(mask.getWidth(),mask.getHeight());
		if (mask.getProcessor() !instanceof ByteProcessor) {
			proc=(ByteProcessor)mask.getProcessor();
		} else {
			proc=mask.getProcessor();
		}

		int label=1;
		for (int i=0; i<mask.getWidth(); i++) {
			for (int j=0; j<mask.getHeight(); j++) {
				if (mask.getPixelValue(i,j)>0) {
					// check 4-neighbours
					int n=0;
					int ncol=-1;
					if (mask.getPixelValue(i-1,j)>0){
						n+=1;
						ncol=out.getPixelValue(i-1,j);
					} 
					if (mask.getPixelValue(i,j-1)>0){
						n+=1;
						ncol=out.getPixelValue(i,j-1);
					}
					if (mask.getPixelValue(i+1,j)>0){
						n+=1;
						ncol=out.getPixelValue(i+1,j);
					}
					if (mask.getPixelValue(i,j+1)>0){
						n+=1;
						ncol=out.getPixelValue(i,j+1);
					}
					if (n>1){
						// has 2+ pixel neighbours, keep it
						
						//if (out.getPixelValue(i,j)==label)
						//	label+=1;
						//
						//out.set(i,j,ncol);
						
						if (out.getPixelValue(i,j)==ncol) {
							out.set(i,j,label);
						} else {
							label+=1;
							out.set(i,j,label);
						}
					} else {
						out.set(i,j,0);
					}
				}
			}
		}
	}
	*/


	// load a unet model for the contour assist functionality
	@SuppressWarnings("all")
	public ComputationGraph loadUNetModel(String modelJsonFile, String modelWeightsFile){

		ComputationGraph trainedUNetModel=null;
		// ---- for debugging nd4j ----
		if (System.getProperties().containsKey(DYNAMIC_LOAD_CLASSPATH_PROPERTY)) {
			IJ.log("System.getProperties().containsKey(DYNAMIC_LOAD_CLASSPATH_PROPERTY)");
		} else {
			IJ.log(">> NOT --- System.getProperties().containsKey(DYNAMIC_LOAD_CLASSPATH_PROPERTY)");
		}

		if (System.getenv().containsKey(DYNAMIC_LOAD_CLASSPATH)){
			IJ.log("System.getenv().containsKey(DYNAMIC_LOAD_CLASSPATH)");
		} else {
			IJ.log(">> NOT --- System.getenv().containsKey(DYNAMIC_LOAD_CLASSPATH)");
		}
		// ----------------------------

		try{
			if (modelJsonFile==null) {
				// all saved model in a single .hdf5 file
				IJ.log("  >> importing from a single .hdf5 file...");
				trainedUNetModel=KerasModelImport.importKerasModelAndWeights(modelWeightsFile);
				IJ.log("  >> importing done...");
			} else {
				//val unet_model: ComputationGraph = KerasModelImport.importKerasModelAndWeights(modelJsonFile, modelWeightsFile);
				IJ.log("  >> importing from json config + weights .h5 files...");
				trainedUNetModel=KerasModelImport.importKerasModelAndWeights(modelJsonFile, modelWeightsFile,false);
				IJ.log("  >> importing done...");
			}
			if (trainedUNetModel!=null) {
				IJ.log("Successfully loaded pretrained U-Net model for contour correction");
			}
			IJ.log("  >> no exception in loading the model...");
		} catch(IOException e) {
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			e.printStackTrace(pw);
			IJ.log(caw.toString());
			IJ.showStatus("IOException thrown");
		} catch(InvalidKerasConfigurationException e){
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			e.printStackTrace(pw);
			IJ.log(caw.toString());
			IJ.showStatus("InvalidKerasConfigurationException thrown");
		} catch(UnsupportedKerasConfigurationException e){
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			e.printStackTrace(pw);
			IJ.log(caw.toString());
			IJ.showStatus("UnsupportedKerasConfigurationException thrown");
		}

		return trainedUNetModel;
	}


	// store the loaded model in the trainedUnetModel attribute of the plugin class
	public void setTrainedModel(ComputationGraph loadedModel){
		this.trainedUNetModel=loadedModel;
	}


	// new frame for options when clicking on "..." button in the main frame
	///*
	void openOptionsFrame(){
		// try to handle opaque overlay colour issue
		/*
		Color prevToolbarColour=curToolbar.getForegroundColor();
		String currentColorHex=ColorToHex(currentSelectionColor);
		String opacityColor="#66"+currentColorHex;
		IJ.log("--color after: "+opacityColor);
		// this was working before for overlay but not for new frames:
		curToolbar.setForegroundColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));
		*/

		optionsFrame = new JFrame("Options");
		optionsFrame.setBounds(200, 200, 450, 335);
		// this should be a separate function!!!!!!!
		//optionsFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		optionsPanel = new Panel();
		optionsFrame.getContentPane().add(optionsPanel, BorderLayout.CENTER);
		optionsPanel.setBackground(SystemColor.control);
		//optionsPanel.setForeground(SystemColor.control);
		//IJ.log("SystemColor.control: "+Integer.toHexString(SystemColor.control.getRGB()));
		//IJ.log("Prefs.FCOLOR: "+Integer.toHexString(Toolbar.getForegroundColor().getRGB())); 
		//IJ.log("Prefs.BCOLOR: "+Integer.toHexString(Toolbar.getBackgroundColor().getRGB()));

		// add elements here:
		lblSemancticSegmentation = new JLabel("Semanctic segmentation");
		lblSemancticSegmentation.setToolTipText("Only available in \"semantic\" annotation type");
		add(lblSemancticSegmentation);
		
		lblBrushSize = new JLabel("Brush size:");
		lblBrushSize.setToolTipText("Annotation brush size (diameter) in pixels");
		add(lblBrushSize);
		
		semanticBrushSizeField = new JTextField();
		semanticBrushSizeField.setColumns(10);
		semanticBrushSizeField.setToolTipText("Annotation brush size (diameter) in pixels");
		semanticBrushSizeField.setText(String.valueOf(semanticBrushSize));
		add(semanticBrushSizeField);
		
		separator = new JSeparator();
		add(separator);
		
		lblContourAssist = new JLabel("Contour assist");
		lblContourAssist.setToolTipText("Only available if \"Contour assist\" is turned on");
		add(lblContourAssist);
		
		lblMaxDistance = new JLabel("Max distance:");
		lblMaxDistance.setToolTipText("Max distance in pixels contour correction can span from the initial contour you create");
		add(lblMaxDistance);
		
		assistDistanceField = new JTextField();
		assistDistanceField.setColumns(10);
		assistDistanceField.setToolTipText("Max distance in pixels contour correction can span from the initial contour you create");
		assistDistanceField.setText(String.valueOf(distanceThreshVal));
		add(assistDistanceField);
		
		lblThresholdgray = new JLabel("Threshold (gray):");
		lblThresholdgray.setToolTipText("Intensity threshold value in the range [0,1] in which contour correction can happen");
		add(lblThresholdgray);
		
		assistThreshGrayField = new JTextField();
		assistThreshGrayField.setColumns(10);
		assistThreshGrayField.setToolTipText("Intensity threshold value for grayscale images in the range [0,1] in which contour correction can happen");
		assistThreshGrayField.setText(String.valueOf(intensityThreshVal));
		add(assistThreshGrayField);
		
		lblThresholdrgb = new JLabel("Threshold (RGB): ");
		lblThresholdrgb.setToolTipText("Intensity threshold value for RGB (colour) images in the range [0,1] in which contour correction can happen. You can set (R,G,B) values in the 3 text boxes on the right");
		add(lblThresholdrgb);
		
		assistThreshRField = new JTextField();
		assistThreshRField.setColumns(10);
		assistThreshRField.setToolTipText("Red intensity threshold value for RGB (colour) images in the range [0,1]");
		assistThreshRField.setText(String.valueOf(intensityThreshValR));
		add(assistThreshRField);
		
		assistThreshGField = new JTextField();
		assistThreshGField.setColumns(10);
		assistThreshGField.setToolTipText("Green intensity threshold value for RGB (colour) images in the range [0,1]");
		assistThreshGField.setText(String.valueOf(intensityThreshValG));
		add(assistThreshGField);
		
		assistThreshBField = new JTextField();
		assistThreshBField.setColumns(10);
		assistThreshBField.setToolTipText("Blue intensity threshold value for RGB (colour) images in the range [0,1]");
		assistThreshBField.setText(String.valueOf(intensityThreshValB));
		add(assistThreshBField);
		
		lblBrushSize_1 = new JLabel("Brush size:");
		lblBrushSize_1.setToolTipText("Correction brush size (diameter) in pixels");
		add(lblBrushSize_1);
		
		assistBrushSizeField = new JTextField();
		assistBrushSizeField.setColumns(10);
		assistBrushSizeField.setToolTipText("Correction brush size (diameter) in pixels");
		assistBrushSizeField.setText(String.valueOf(correctionBrushSize));
		add(assistBrushSizeField);
		
		lblpixels = new JLabel("(pixels)");
		label = new JLabel("(pixels)");
		label_1 = new JLabel("(pixels)");
		label_2 = new JLabel("[0-1]");
		label_3 = new JLabel("[0-1]");

		add(lblpixels);
		add(label);
		add(label_1);
		add(label_2);
		add(label_3);

		lblMethod = new JLabel("Method:");
		lblMethod.setToolTipText("Correction method");
		add(lblMethod);

		JLabel lblUnet = new JLabel("U-Net");
		lblUnet.setToolTipText("U-Net deep learning method");
		add(lblUnet);
		
		JLabel lblClassic = new JLabel("Classic");
		lblClassic.setToolTipText("Classic image processing method (region growing)");
		add(lblClassic);

		methodSlider = new JSlider();
		methodSlider.setSnapToTicks(true);
		methodSlider.setValue(selectedCorrMethod);
		methodSlider.setMaximum(1);

		JButton buttonQ = new JButton("?");
		buttonQ.setToolTipText("Info on contour assist");
		buttonQ.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// show a dialog box with a few lines of info
				MessageDialog contourOptionsInfo=new MessageDialog(instance,
                 "Info",
                 "Contour assist mode usage:\n"+
                 "1. draw an initial contour\n"+
                 "2. wait until the suggested contour is shown as selection\n"+
                 "3. edit the contour by the brush selection tool (activated automatically)\n"+
                 "4. accept or reject it with either of the keys below\n \n"+
                 "Suggested contours can be manipulated by keys:\n\"q\":\taccept and add to ROI list\n"+
                 "Ctrl+\"delete\":\treject and delete current suggested contour\n"+
                 "\"u\" (only for U-Net method):\tinverts the current suggestion around the object\n"+
                 "\"g\" (only for U-Net method):\tfits the current suggestion to the object\n");
			}
		});
		add(buttonQ);

		
		btnOkOptions = new JButton("OK");

		btnOkOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// process values entered
				// TODO

				String tmp=null;
				int endidx=0;

				// get thresh values from contour assist threshold fields:
				if (assistThreshGrayField.isEnabled()) {
					intensityThreshVal=Double.parseDouble(assistThreshGrayField.getText());
					intensityThreshValR=Double.parseDouble(assistThreshRField.getText());
					intensityThreshValG=Double.parseDouble(assistThreshGField.getText());
					intensityThreshValB=Double.parseDouble(assistThreshBField.getText());

					// handle value bounds [0-1]
					if (intensityThreshVal<0.0){
						intensityThreshVal=0.0;
						assistThreshGrayField.setText("0.0");
					}
					if (intensityThreshVal>1.0){
						intensityThreshVal=1.0;
						assistThreshGrayField.setText("1.0");
					}

					if (intensityThreshValR<0.0){
						intensityThreshValR=0.0;
						assistThreshRField.setText("0.0");
					}
					if (intensityThreshValR>1.0){
						intensityThreshValR=1.0;
						assistThreshRField.setText("1.0");
					}

					if (intensityThreshValG<0.0){
						intensityThreshValG=0.0;
						assistThreshGField.setText("0.0");
					}
					if (intensityThreshValG>1.0){
						intensityThreshValG=1.0;
						assistThreshGField.setText("1.0");
					}

					if (intensityThreshValB<0.0){
						intensityThreshValB=0.0;
						assistThreshBField.setText("0.0");
					}
					if (intensityThreshValB>1.0){
						intensityThreshValB=1.0;
						assistThreshBField.setText("1.0");
					}

					// save to properties
					SaveNewProp("contourAssistThresholdGray", String.valueOf(intensityThreshVal));
					SaveNewProp("contourAssistThresholdR", String.valueOf(intensityThreshValR));
					SaveNewProp("contourAssistThresholdG", String.valueOf(intensityThreshValG));
					SaveNewProp("contourAssistThresholdB", String.valueOf(intensityThreshValB));

				}
				// get brush size:
				if (assistBrushSizeField.isEnabled()) {
					tmp=assistBrushSizeField.getText();
					if(Math.max(tmp.lastIndexOf("."),tmp.lastIndexOf(","))>-1)
						endidx=Math.max(tmp.lastIndexOf("."),tmp.lastIndexOf(","));
					else
						endidx=tmp.length();
					correctionBrushSize=Integer.parseInt(tmp.substring(0,endidx));
					curToolbar.setBrushSize(correctionBrushSize);
					//Prefs.set("toolbar.brush.size",correctionBrushSize);

					// handle value bounds [1-1000]
					if (correctionBrushSize<1){
						correctionBrushSize=1;
						assistBrushSizeField.setText("1");
					}
					if (correctionBrushSize>1000){
						correctionBrushSize=1000;
						assistBrushSizeField.setText("1000");
					}

					SaveNewProp("contourAssistBrushsize", String.valueOf(correctionBrushSize));
				}
				// get distance:
				if (assistDistanceField.isEnabled()) {
					tmp=assistDistanceField.getText();
					if(Math.max(tmp.lastIndexOf("."),tmp.lastIndexOf(","))>-1)
						endidx=Math.max(tmp.lastIndexOf("."),tmp.lastIndexOf(","));
					else
						endidx=tmp.length();
					distanceThreshVal=Integer.parseInt(tmp.substring(0,endidx));

					// handle value bounds [1-1000]
					if (distanceThreshVal<1){
						distanceThreshVal=1;
						assistDistanceField.setText("1");
					}
					if (distanceThreshVal>1000){
						distanceThreshVal=1000;
						assistDistanceField.setText("1000");
					}

					SaveNewProp("contourAssistMaxDistance", String.valueOf(distanceThreshVal));
				}

				// get correction method:
				if (methodSlider.isEnabled()) {
					selectedCorrMethod=methodSlider.getValue();

					SaveNewProp("contourAssistMethod", selectedCorrMethod==0?"UNet":"classical");
				}

				// get semantic bursh size:
				if (semanticBrushSizeField.isEnabled()) {
					tmp=semanticBrushSizeField.getText();
					if(Math.max(tmp.lastIndexOf("."),tmp.lastIndexOf(","))>-1)
						endidx=Math.max(tmp.lastIndexOf("."),tmp.lastIndexOf(","));
					else
						endidx=tmp.length();
					semanticBrushSize=Integer.parseInt(tmp.substring(0,endidx));

					// handle value bounds [1-1000]
					if (semanticBrushSize<1){
						semanticBrushSize=1;
						semanticBrushSizeField.setText("1");
					}
					if (semanticBrushSize>1000){
						semanticBrushSize=1000;
						semanticBrushSizeField.setText("1000");
					}

					Prefs.set("brush.overlay",true);
					Prefs.set("brush.width",semanticBrushSize);

					if (selectedAnnotationType.equals("semantic")){ // && curToolbar.getToolId()==Toolbar.OVAL) {
						// in semantic mode
						tool=new BrushToolCustom();
						tool.run("");
					}
					IJ.log("set semantic brush size to "+String.valueOf(semanticBrushSize)+" px");
					IJ.log("queried brush size: "+String.valueOf((int)Prefs.get("brush.width",0))+" px");

					SaveNewProp("semanticBrushSize", String.valueOf(semanticBrushSize));
				}


				// close the progress window
				if (optionsFrame!=null) {
					
					optionsFrame.dispose();
					optionsFrame=null;
					
				}
			}
		});
		add(btnOkOptions);

		btnCancelOptions = new JButton("Cancel");

		btnCancelOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// doesn't matter if anything is changed, we don't update the values
				// close the progress window
				if (optionsFrame!=null) {
					
					optionsFrame.dispose();
					optionsFrame=null;
					
				}
			}
		});
		add(btnCancelOptions);



		GroupLayout gl_optionsPanel = new GroupLayout(optionsPanel);
		gl_optionsPanel.setHorizontalGroup(
			gl_optionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_optionsPanel.createSequentialGroup()
							.addGroup(gl_optionsPanel.createParallelGroup(Alignment.TRAILING)
								.addComponent(separator, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
								.addComponent(lblSemancticSegmentation, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
								.addGroup(Alignment.LEADING, gl_optionsPanel.createSequentialGroup()
									.addGap(10)
									.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(lblThresholdgray)
										.addComponent(lblThresholdrgb)
										.addComponent(lblMaxDistance)
										.addComponent(lblBrushSize_1)
										.addComponent(lblMethod))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(assistBrushSizeField, 188, 188, 188)
										.addComponent(assistDistanceField, 188, 188, 188)
										.addGroup(gl_optionsPanel.createSequentialGroup()
											.addComponent(assistThreshRField, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(assistThreshGField, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(assistThreshBField, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))
										.addComponent(assistThreshGrayField, 188, 188, 188)
										.addGroup(gl_optionsPanel.createSequentialGroup()
											.addComponent(lblUnet)
											.addPreferredGap(ComponentPlacement.UNRELATED)
											.addComponent(methodSlider, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.UNRELATED)
											.addComponent(lblClassic)))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
										.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
										.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
										.addComponent(label, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonQ))
									.addGap(103))
								.addGroup(gl_optionsPanel.createSequentialGroup()
									.addGap(12)
									.addComponent(lblBrushSize)
									.addGap(43)
									.addComponent(semanticBrushSizeField, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(lblpixels)
									.addGap(0, 159, Short.MAX_VALUE)))
							.addGap(59))
						.addGroup(gl_optionsPanel.createSequentialGroup()
							.addComponent(lblContourAssist)
							.addContainerGap(339, Short.MAX_VALUE))))
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addGap(148)
					.addComponent(btnCancelOptions)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnOkOptions)
					.addContainerGap(316, Short.MAX_VALUE))
		);
		gl_optionsPanel.setVerticalGroup(
			gl_optionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_optionsPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblSemancticSegmentation)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblBrushSize)
						.addComponent(semanticBrushSizeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblpixels))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblContourAssist)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMaxDistance)
						.addComponent(assistDistanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblThresholdgray)
						.addComponent(assistThreshGrayField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblThresholdrgb)
						.addComponent(assistThreshRField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(assistThreshGField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(assistThreshBField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_3))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblBrushSize_1)
						.addComponent(assistBrushSizeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblMethod)
							.addComponent(lblUnet))
						.addComponent(methodSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblClassic))
					.addPreferredGap(ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
					.addGroup(gl_optionsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOkOptions)
						.addComponent(btnCancelOptions)
						.addComponent(buttonQ))
					.addContainerGap())
		);
		optionsPanel.setLayout(gl_optionsPanel);


		//add(optionsPanel);

		
		pack();
		GUI.center(optionsFrame);
		//GUI.center(dlg);
		optionsFrame.setVisible(true);

		//btnOk.setEnabled(false);
	}
	//*/


	// new frame for classes when selecting the "Class mode" checkbox in the main frame
	void openClassesFrame(){
		// check if there is opened instance of this frame
		if (classesFrame!=null){
			// already opened
			return;
		}
		classesFrame = new JFrame("Classes");
		classesFrame.setBounds(100, 100, 350, 200);
		//classesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //EXIT_ON_CLOSE
		classesFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		        classesFrame.dispose();
		        classesFrame=null;
		    }
		});
		
		classPanel = new Panel();
		classesFrame.getContentPane().add(classPanel, BorderLayout.CENTER);
		
		lblClasses = new JLabel("Classes");
		add(lblClasses);

		lblCurrentClass = new JLabel("Current:");
		add(lblCurrentClass);

		scrollPaneClasses = new JScrollPane();
		
		listModelClasses = new DefaultListModel<String>();
		classListList = new JList<String>(listModelClasses);



    	/*
		DefaultListModel<String> listModelColours;
		listModelColours = new DefaultListModel<String>();
		listModelColours.addElement("red");		//0
		listModelColours.addElement("green");	//1
		listModelColours.addElement("blue");	//2
		listModelColours.addElement("cyan");	//3
		listModelColours.addElement("magenta");	//4
		listModelColours.addElement("yellow");	//5
		listModelColours.addElement("orange");	//6
		listModelColours.addElement("white");	//7
		listModelColours.addElement("black");	//8
		*/
		
		
		rdbtnColoursR = new JRadioButton("R");
		rdbtnColoursR.setToolTipText("red");
		rdbtnColoursR.setForeground(Color.RED);
		rdbtnColoursR.addItemListener(this);
		
		rdbtnColoursG = new JRadioButton("G");
		rdbtnColoursG.setToolTipText("green");
		rdbtnColoursG.setForeground(Color.GREEN);
		rdbtnColoursG.addItemListener(this);
		
		rdbtnColoursB = new JRadioButton("B");
		rdbtnColoursB.setToolTipText("blue");
		rdbtnColoursB.setForeground(Color.BLUE);
		rdbtnColoursB.addItemListener(this);
		
		rdbtnColoursC = new JRadioButton("C");
		rdbtnColoursC.setToolTipText("cyan");
		rdbtnColoursC.setForeground(Color.CYAN);
		rdbtnColoursC.addItemListener(this);
		
		rdbtnColoursM = new JRadioButton("M");
		rdbtnColoursM.setToolTipText("magenta");
		rdbtnColoursM.setForeground(Color.MAGENTA);
		rdbtnColoursM.addItemListener(this);
		
		rdbtnColoursY = new JRadioButton("Y");
		rdbtnColoursY.setToolTipText("yellow");
		rdbtnColoursY.setForeground(Color.YELLOW);
		rdbtnColoursY.addItemListener(this);
		
		rdbtnColoursO = new JRadioButton("O");
		rdbtnColoursO.setToolTipText("orange");
		rdbtnColoursO.setForeground(Color.ORANGE);
		rdbtnColoursO.addItemListener(this);
		
		rdbtnColoursW = new JRadioButton("W");
		rdbtnColoursW.setToolTipText("white");
		rdbtnColoursW.setForeground(Color.WHITE);
		rdbtnColoursW.addItemListener(this);
		
		rdbtnColoursK = new JRadioButton("K");
		rdbtnColoursK.setToolTipText("black");
		rdbtnColoursK.addItemListener(this);
		
		rdbtnGroup= new ButtonGroup();
		rdbtnGroup.add(rdbtnColoursR);
		rdbtnGroup.add(rdbtnColoursG);
		rdbtnGroup.add(rdbtnColoursB);
		rdbtnGroup.add(rdbtnColoursC);
		rdbtnGroup.add(rdbtnColoursM);
		rdbtnGroup.add(rdbtnColoursY);
		rdbtnGroup.add(rdbtnColoursO);
		rdbtnGroup.add(rdbtnColoursW);
		rdbtnGroup.add(rdbtnColoursK);


		// add them to the panel too
		add(rdbtnColoursR);
		add(rdbtnColoursG);
		add(rdbtnColoursB);
		add(rdbtnColoursC);
		add(rdbtnColoursM);
		add(rdbtnColoursY);
		add(rdbtnColoursO);
		add(rdbtnColoursW);
		add(rdbtnColoursK);


		// add default class option as a combo box
		lblClassDefault = new JLabel("Default:");
		add(lblClassDefault);
		
		comboBoxDefaultClass = new JComboBox();
		comboBoxDefaultClass.setToolTipText("Assign this class to all objects by default.");

		
		
		if (classFrameNames==null && classFrameColours==null){
			listModelClasses.addElement("Class_01");
			listModelClasses.addElement("Class_02");
			classFrameNames=new ArrayList<String>();
			classFrameNames.add("Class_01");
			classFrameNames.add("Class_02");
			classFrameColours=new ArrayList<Integer>();
			classFrameColours.add(0);
			classFrameColours.add(1);

			// set default colour for the default class (1)
			rdbtnColoursR.setSelected(true);
			classListList.setSelectedIndex(0);

			selectedClassNameNumber=1;
			// display currently selected class colour on the radiobuttons and label
    		lblCurrentClass.setText("<html>Current: <font color='red'>Class_01</font></html>");

    		// set default roi group
    		manager.setGroup(-1);

    		// set default class selection list
    		comboBoxDefaultClass.addItem("(none)");
    		comboBoxDefaultClass.addItem("Class_01");
    		comboBoxDefaultClass.addItem("Class_02");
    		comboBoxDefaultClass.setSelectedIndex(0);
    		defaultClassNumber=-1;

    		classNumberCounter=2;

    		classNameLUT.put("Class_01",1);
    		classNameLUT.put("Class_02",2);

		} else {
			// set default class selection list
    		comboBoxDefaultClass.addItem("(none)");

			for (int i=0; i<classFrameNames.size();i++){
				String curClassName=classFrameNames.get(i);
				listModelClasses.addElement(curClassName);
				comboBoxDefaultClass.addItem(curClassName);

				int curClassNum=Integer.parseInt(curClassName.substring(curClassName.lastIndexOf("_")+1,curClassName.length()));
				classNameLUT.put(classFrameNames.get(i),curClassNum);
			}

			// set selected colour for the previously selected class and colour
			String selectedClassNameVar="Class_"+String.format("%02d",selectedClassNameNumber);
			int selectedClassIdxList=classFrameNames.indexOf(selectedClassNameVar);
			setColourRadioButton(selectedClassNameVar,selectedClassIdxList);
			classListList.setSelectedIndex(selectedClassIdxList);

			comboBoxDefaultClass.setSelectedIndex(0);
			defaultClassNumber=-1;
		}

		//comboBoxDefaultClass.addItemListener(this);
		comboBoxDefaultClass.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent ie) {
		        IJ.log("combobox item state changed");
		        JComboBox combobox = (JComboBox)ie.getSource();
		  		//JComboBox combobox = (JComboBox) ie.getItem();
			    int state = ie.getStateChange();
			    if (state == ItemEvent.SELECTED){
			    	// a default class was selected, assign all unassigned objects to this class

			    	// first save current classes
			    	if (manager!=null && classMode && (managerList!=null && managerList.size()>0))
						managerList.set(currentSliceIdx-1,manager);

			    	String selectedClassName=(String) combobox.getSelectedItem();
			    	IJ.log("Selected '"+selectedClassName+"' as default class");
			    	if (selectedClassName.equals("(none)")){
			    		// set no defaults
			    		defaultClassNumber=-1;
			    	} else {
			    		// a useful class is selected
			    		defaultClassNumber=classNameLUT.get(selectedClassName);
			    		//defaultClassNumber=Integer.parseInt(selectedClassName.substring(selectedClassName.lastIndexOf("_")+1,selectedClassName.length()));

			    		// set all unassigned objects to this class
			    		//setDefaultClass4objects();
			    		runDefaultClassSetting4allSlices();

			    		// show the latest opened roi stack again
			    		if (managerList!=null && managerList.size()>0)
							updateROImanager(managerList.get(currentSliceIdx-1),showCnt);
			    	}
			    } else {
			    	// do nothing
			    	IJ.log("combobox item state changed, event: "+String.valueOf(ItemEvent.SELECTED));
			    }
		    }
		});


		add(comboBoxDefaultClass);

		// set default class (group) for all unclassified objects
		if (classFrameNames==null && classFrameColours==null){
			setDefaultClass4objects();
			//runDefaultClassSetting4allSlices();
		} else {
			// it should already be set
		}

		
		scrollPaneClasses.setViewportView(classListList);
		classListList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		

		// list change listener
		classListList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList sourceObj=(JList)e.getSource();
				if (e.getValueIsAdjusting()){
					// still changing, do nothing
					//debug:
					//IJ.log("    (list selecting is still in progress)");
					return;
				}
		        ListSelectionModel lsm = (ListSelectionModel)sourceObj.getSelectionModel();
				int selectedClassNameIdx=lsm.getMaxSelectionIndex();
				String selectedClassNameVar;

				classListSelectionHappened=true;

		        if(selectedClassNameIdx<0){
		        	// selection is empty
		        	selectedClassNameVar=null;
		        } else {
		        	selectedClassNameVar=listModelClasses.getElementAt(selectedClassNameIdx);
		        	IJ.log("Selected class \""+selectedClassNameVar+"\"");

		        	// store currently selected class's number for ROI grouping
		        	selectedClassNameNumber=classNameLUT.get(selectedClassNameVar);
		        	//selectedClassNameNumber=Integer.parseInt(selectedClassNameVar.substring(selectedClassNameVar.lastIndexOf("_")+1,selectedClassNameVar.length()));

		        	// find its colour
		        	
		        	// find it in the classnames list
					int selectedClassIdxList=classFrameNames.indexOf(selectedClassNameVar);
					//debug:
					IJ.log("("+String.valueOf(selectedClassIdxList+1)+"/"+String.valueOf(classFrameNames.size())+") classes");
					if (selectedClassIdxList<0){
						// didnt find it
						IJ.log("Could not find the newly selected class name in the list. Please try again.");
						return;
					}
					
					// moved radio button setting to its own fcn
					setColourRadioButton(selectedClassNameVar,selectedClassIdxList);
		        }

		        classListSelectionHappened=false;
		    }
		});

		/*
		// add right-click listener to the class list --> to rename the class
		if (roisFromArgs && managerList!=null && managerList.size()>0){
			// do not enable class renaming
		} else {
			// enable it
			classListList.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseReleased(MouseEvent e) {
	            	int btn=e.getButton();
	            	if (btn==MouseEvent.BUTTON3) {
						// right-click

						// select this item in the class list also!
						// TODO

						// popup a rename dialog
						GenericDialog DialogRename = new GenericDialog("Rename class");
						DialogRename.addStringField("class name: ", classFrameNames.get(selectedClassNameNumber-1));
						DialogRename.showDialog();

						if (DialogRename.wasCanceled())
							return;

						String newClassName = DialogRename.getNextString();
						// do something with it
						listModelClasses.remove(selectedClassNameNumber-1);
                        listModelClasses.add(selectedClassNameNumber-1, newClassName);

                        classNameLUT.put(newClassName,selectedClassNameNumber);

					}
	            }
			});
		}
		*/
		
		add(scrollPaneClasses);



		btnAddClass = new JButton("+");
		btnAddClass.setToolTipText("Add a new class");
		btnAddClass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// add new class to list
				String lastClassName=classFrameNames.get(classFrameNames.size()-1);
				int lastClassNum=-1;
				if (lastClassName==null){
					lastClassNum=classNameLUT.size();
				} else {
					lastClassNum=Integer.parseInt(lastClassName.substring(lastClassName.lastIndexOf("_")+1,lastClassName.length()));
				}

				String newClassName="Class_"+String.format("%02d",lastClassNum+1);
				classFrameNames.add(newClassName);
				listModelClasses.addElement(newClassName);
				comboBoxDefaultClass.addItem(newClassName);

				//classNameLUT.put(newClassName,classNameLUT.size()+1); // <-- if loaded and classes are not numbered from 1, this will mess the numbering up
				classNameLUT.put(newClassName,lastClassNum+1);

				// assign a free colour to the new class
				for (int i=0;i<8;i++){
					if (!classFrameColours.contains(i)){
						// found first free colour, take it
						classFrameColours.add(i);
						break;
					}
				}
			}
		});
		add(btnAddClass);
		
		btnDeleteClass = new JButton("-");
		btnDeleteClass.setToolTipText("Delete current class");
		btnDeleteClass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// see if there are classes in the list

				if (classListList.getModel().getSize()>0){
					// remove selected class from list
					// fetch selected class from the list
					String selectedClassName=classListList.getSelectedValue();
					// find it in the classnames list
					int selectedClassIdxList=classFrameNames.indexOf(selectedClassName);
					if (selectedClassIdxList<0){
						// didnt find it
						IJ.log("Could not find the currently selected class name in the list for deletion. Please try again.");
						return;
					}

					classFrameNames.set(selectedClassIdxList,null);
					classFrameColours.set(selectedClassIdxList,null);
					//listModelClasses.remove(selectedClassIdxList);
					listModelClasses.removeElement(selectedClassName);


					// reset group attribute of all ROIs in this class if any
					// set all currently assigned ROIs of this group to have the new contour colour
					// loop through all slices
					if (managerList!=null && managerList.size()>0){
						managerList.set(currentSliceIdx-1,manager);

						for (int k=0;k<managerList.size();k++){
							manager=managerList.get(k);

							unClassifyClass(classNameLUT.get(selectedClassName));
							managerList.set(k,manager);
						}

						updateROImanager(managerList.get(currentSliceIdx-1),showCnt);

					} else {
						unClassifyClass(classNameLUT.get(selectedClassName));
					}
		        	
		        	// see if the default class is being deleted
					if (defaultClassNumber==classNameLUT.get(selectedClassName)){
						// reset the default class to (none)
						IJ.log(">>>> deleting the default class --> unclassify these ROIs");
						comboBoxDefaultClass.setSelectedItem("(none)");
						//unClassifyClass(defaultClassNumber);
						defaultClassNumber=-1;
						defaultClassColour=null;
					}
					//comboBoxDefaultClass.removeItemAt(selectedClassIdxList+1); // default class selector has "(none)" as the first element
					comboBoxDefaultClass.removeItem(selectedClassName);

					IJ.log("Deleted class \""+selectedClassName+"\" from the class list");

					// select the first class as default
					if (classListList.getModel().getSize()>0){
						classListList.setSelectedIndex(0);
						String selectedClassNameVar=listModelClasses.getElementAt(0);
			        	IJ.log("Selected class \""+selectedClassNameVar+"\"");

			        	// store currently selected class's number for ROI grouping
			        	selectedClassNameNumber=Integer.parseInt(selectedClassNameVar.substring(selectedClassNameVar.lastIndexOf("_")+1,selectedClassNameVar.length()));
						selectedClassIdxList=classFrameNames.indexOf(selectedClassNameVar);

						setColourRadioButton(selectedClassNameVar,selectedClassIdxList);
					} else {
						// deleted the last class
						// allow this?

					}

				} else {
					// no classes in the list
					IJ.log("No classes left to delete.");
				}
			}
		});
		add(btnDeleteClass);
		
		
		
		GroupLayout gl_classPanel = new GroupLayout(classPanel);
		gl_classPanel.setHorizontalGroup(
			gl_classPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_classPanel.createSequentialGroup()
					.addGroup(gl_classPanel.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_classPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblClasses)
							.addGap(18)
							.addComponent(btnAddClass)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnDeleteClass))
						.addGroup(gl_classPanel.createSequentialGroup()
							.addGap(21)
							.addComponent(scrollPaneClasses, 0, 0, Short.MAX_VALUE)))
					.addGap(18)
					.addGroup(gl_classPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblCurrentClass)
						.addGroup(gl_classPanel.createSequentialGroup()
							.addGroup(gl_classPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(rdbtnColoursR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_classPanel.createSequentialGroup()
									.addGroup(gl_classPanel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(rdbtnColoursO, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(rdbtnColoursC, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.UNRELATED)))
							.addGap(2)
							.addGroup(gl_classPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(rdbtnColoursW, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(rdbtnColoursG, GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
								.addComponent(rdbtnColoursM, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_classPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(rdbtnColoursB, GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
								.addComponent(rdbtnColoursY, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(rdbtnColoursK, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(gl_classPanel.createSequentialGroup()
							.addComponent(lblClassDefault)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBoxDefaultClass, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_classPanel.setVerticalGroup(
			gl_classPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_classPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_classPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblClasses)
						.addComponent(btnAddClass)
						.addComponent(btnDeleteClass)
						.addComponent(lblCurrentClass))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_classPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPaneClasses, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_classPanel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_classPanel.createSequentialGroup()
								.addComponent(rdbtnColoursB)
								.addGap(3)
								.addComponent(rdbtnColoursY)
								.addGap(4)
								.addComponent(rdbtnColoursK))
							.addGroup(gl_classPanel.createSequentialGroup()
								.addComponent(rdbtnColoursG)
								.addGap(3)
								.addComponent(rdbtnColoursM)
								.addGap(4)
								.addComponent(rdbtnColoursW))
							.addGroup(gl_classPanel.createSequentialGroup()
								.addComponent(rdbtnColoursR)
								.addGap(3)
								.addComponent(rdbtnColoursC)
								.addGap(4)
								.addComponent(rdbtnColoursO))))
					.addContainerGap(16, Short.MAX_VALUE))
				.addGroup(gl_classPanel.createSequentialGroup()
					.addContainerGap(130, Short.MAX_VALUE)
					.addGroup(gl_classPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblClassDefault)
						.addComponent(comboBoxDefaultClass, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		classPanel.setLayout(gl_classPanel);

		pack();
		GUI.center(classesFrame);
		//GUI.center(dlg);
		classesFrame.setVisible(true);
	}


	// function to start the exporter from the annotator
	void startExporter() throws IOException{

		// export the masks here
		finishedSaving=false;

		int[] dimensions=imp.getDimensions();
		int width=dimensions[0];
		int height=dimensions[1];

		long lsize = (long)width*height;
		int size = (int)lsize;
        if (size<0) {
        	IJ.log("0-sized image");
        	return;
        }

        String exportFolder=null;
        String exportFolderClass=null;

		// see if importopsef was called initially
		if (exportRootFolderFromArgs!=null && roisFromArgs){

			// first save everything we have open and close the image
			//closeWindowsAndSave();

			// save the latest opened roi stack internally
	    	if (manager!=null && classMode && (roisFromArgs && managerList.size()>0))
				managerList.set(currentSliceIdx-1,manager);
			//updateROImanager(managerList.get(currentSliceIdx-1),showCnt);

			// from AnnotatorExportFrameNew.java startExport() fcn:

			// create export folder:
			if (exportRootFolderFromArgs!=null)
				exportFolder=exportRootFolderFromArgs+File.separator+exportFolderFromArgs;
			else{
				// TODO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				String mainExportFolder="exports";
				exportFolder=destFolder+File.separator+mainExportFolder;
			}

			File outDir=new File(exportFolder);
			if (outDir.exists() && outDir.isDirectory()) {
				// folder already exists
			} else {
				outDir.mkdir();
				IJ.log("Created output folder: "+exportFolder);
			}

			// check the number of elements in roi stacks and file names
			int maskCount=origMaskFileNames.length;
			if(managerList.size()!=maskCount){
				// number of roi sets and mask names does not match!
				IJ.log("The number of roi sets and mask names does not match!");
				return;
			}

			// find file separator in the mask file name string array elements
			String thisFileSep="/";
			int lastIdx=origMaskFileNames[0].lastIndexOf(thisFileSep);
			if (lastIdx<0) {
				thisFileSep="\\";
				lastIdx=origMaskFileNames[0].lastIndexOf(thisFileSep);
			}
			if (lastIdx<0) {
				IJ.log("Cannot find file separator character in the file path\n");
				return;
			}

			// show the exporter's progress bar window
			ExportProgressFrame exportProgressFrameObj=new ExportProgressFrame();
			exportProgressFrameObj.setCancelListener(started,finishedSaving);
			JFrame progressFrame=exportProgressFrameObj.getProgressFrame();
			Panel progressPanel=exportProgressFrameObj.getProgressPanel();
			JLabel lblExportingImages=exportProgressFrameObj.getLblExportingImages();
			JLabel lblCurrentImage=exportProgressFrameObj.getLblCurrentImage();
			JProgressBar progressBar=exportProgressFrameObj.getProgressBar();
			JButton btnOk=exportProgressFrameObj.getBtnOk();
			JButton btnCancelProgress=exportProgressFrameObj.getBtnCancelProgress();
			
			// set progressbar length
			progressBar.setMaximum(maskCount);


			// open text files to write
			try {
				String textFolder=exportRootFolderFromArgs+File.separator+"10_ImportExport";
				File fout=new File(textFolder);
				if (fout.exists() && fout.isDirectory()) {
					// folder already exists
				} else {
					fout.mkdir();
					IJ.log("Created output folder: "+textFolder);
				}
				String textFileSegs=textFolder+File.separator+"FilePairList_fromFiji_Masks.txt";
				String textFileClasses=textFolder+File.separator+"FilePairList_fromFiji_Classes.txt";
	            FileWriter writer = new FileWriter(textFileSegs, false);
	            FileWriter writer2 = new FileWriter(textFileClasses, false);
	            String curLine=null;


				// loop through all rois in stack and save them 1-by-1
				
				for (int mi=0; mi<maskCount; mi++) {
					//show progress
					// update file name tag on main window to check which image we are annotating
					String displayedName=origMaskFileNames[mi];
					int maxLength=20;
					// check how long the file name is (if it can be displayed)
					int nameLength=origMaskFileNames[mi].length();
					if (nameLength>maxLength) {
						displayedName=origMaskFileNames[mi].substring(0,maxLength-3)+"...tiff";
					}
					// display this in the progress bar too:
					// set the labels and progress
					lblExportingImages.setText("Exporting images...");
					lblCurrentImage.setText(" ("+String.valueOf(mi+1)+"/"+String.valueOf(maskCount)+"): "+displayedName);


					// get the i-th roi set
					manager=managerList.get(mi);
					int roiCount=manager.getCount();
					IJ.log("annotated objects: "+String.valueOf(roiCount));
					if (roiCount<1) {
						// this continue worked while this whole bunch of export code was in the EXPORT ------- for cycle
						continue;
						//return;
					}

					// -----------------------
        			// save segmentation masks
			        
			        // segmentation mask generation was moved to its own fcn:
			        ImagePlus maskImage=createSegmentationMask(size,width,height,roiCount);
			        if (maskImage==null)
	        			return;

					// construct output file name:
					String outputFileName=exportFolder+File.separator+origMaskFileNames[mi].substring(origMaskFileNames[mi].lastIndexOf(thisFileSep)+1,origMaskFileNames[mi].length());
					// save output image:
					boolean successfullySaved=IJ.saveAsTiff(maskImage,outputFileName);
					if (successfullySaved) {
						IJ.log("Saved exported image: "+outputFileName+"\n");
						IJ.log("---------------------");
					} else {
						IJ.log("Could not save exported image");
					}

					// write to text file
					curLine=origImageFileNames[mi]+";"+File.separator+exportFolderFromArgs+File.separator+origMaskFileNames[mi].substring(origMaskFileNames[mi].lastIndexOf(thisFileSep)+1,origMaskFileNames[mi].length());
					writer.write(curLine);
					writer.write("\r\n");   // write new line


					// -------------------------
			        // save classification masks
					// repeat for classes if the user classified the objects

					if (startedClassifying){
						//debug:
						IJ.log("++++---- classified mask for #"+String.valueOf(mi));

						// create export folder:
						if (exportRootFolderFromArgs!=null)
							exportFolderClass=exportRootFolderFromArgs+File.separator+exportClassFolderFromArgs;
						else
							exportFolderClass=exportFolder;

						outDir=new File(exportFolderClass);
						if (outDir.exists() && outDir.isDirectory()) {
							// folder already exists
						} else {
							outDir.mkdir();
							IJ.log("Created output folder: "+exportFolderClass);
						}

						// find class idxs !!!!!!!!!!!!!!!!!!
						// if we only want to save the non-empty classes as masks:
						// --> nonEmptyClassNameNumbers
						///*
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
						//*/
						// if every created class should be saved as a mask even if empty:
						// --> usedClassNameNumbers

						curLine=origImageFileNames[mi]+";";

						for (int c=0; c<nonEmptyClassNameNumbers.size(); c++){

							int curClassNum=nonEmptyClassNameNumbers.get(c);

							// maskImage2 generation moved to its own fcn:
							ImagePlus maskImage2=Annotator_MainFrameNew.createClassMask(size,width,height,roiCount,curClassNum,manager,true);
							if (maskImage2==null)
	        					return;

							// construct output file name:
							int maskNamePos=origMaskFileNames[mi].lastIndexOf("_Mask_");
							int maskNamePosShift=5;
							if (maskNamePos<0){
								// class mask was imported
								maskNamePos=origMaskFileNames[mi].lastIndexOf("_MaskClass");
								maskNamePosShift=12;

							} else {
								// segmentation mask was imported
								//outputFileName=exportFolderClass+File.separator+origMaskFileNames[mi].substring(origMaskFileNames[mi].lastIndexOf(thisFileSep)+1,maskNamePos)+"_MaskClass"+String.format("%02d",curClassNum)+origMaskFileNames[mi].substring(maskNamePos+maskNamePosShift,origMaskFileNames[mi].length());
							}

							outputFileName=exportFolderClass+File.separator+origMaskFileNames[mi].substring(origMaskFileNames[mi].lastIndexOf(thisFileSep)+1,maskNamePos)+"_MaskClass"+String.format("%02d",curClassNum)+origMaskFileNames[mi].substring(maskNamePos+maskNamePosShift,origMaskFileNames[mi].length());

							// save output image:
							successfullySaved=IJ.saveAsTiff(maskImage2,outputFileName);
							if (successfullySaved) {
								IJ.log("Saved exported image: "+outputFileName+"\n");
								IJ.log("---------------------");
							} else {
								IJ.log("Could not save exported image");
							}

							// write to text file
							curLine=curLine+File.separator+exportClassFolderFromArgs+File.separator+origMaskFileNames[mi].substring(origMaskFileNames[mi].lastIndexOf(thisFileSep)+1,maskNamePos)+"_MaskClass"+String.format("%02d",curClassNum)+origMaskFileNames[mi].substring(maskNamePos+maskNamePosShift,origMaskFileNames[mi].length())+";";

						}

						// write to text file
						writer2.write(curLine);
						writer2.write("\r\n");   // write new line
					}

					// -------------------
					// update progress bar

					progressBar.setValue(mi+1);
					if (mi==maskCount) {
						// finished every image in the folder
						btnOk.setEnabled(true);
						btnCancelProgress.setEnabled(false);
						lblExportingImages.setText("Finished exporting images");
					}

				}
				// loop end

				writer.close();
				writer2.close();

			} catch (IOException e) {
	            CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
	        }

			IJ.log("FINISHED EXPORTING ANNOTATIONS\n---------------------");

			// update export progress bar
			progressBar.setValue(origMaskFileNames.length);
			
			// finished every image in the folder
			btnOk.setEnabled(true);
			btnCancelProgress.setEnabled(false);
			lblExportingImages.setText("Finished exporting images");

			finishedSaving=true;
			//return;

			// also save current rois to the temp folder
			saveData();

			// show the latest opened roi stack again <-- done in saveData() fcn
			//updateROImanager(managerList.get(currentSliceIdx-1),showCnt);

			// should also parse the mask names for saving then the annot folder should be selected automatically like 'opsef'
		
		} else {
			// normal "AnnotatorJ" init

			// save current rois first
			saveData();

			String mainExportFolder="labelled_masks";
			exportFolder=destFolder+File.separator+selectedClass+File.separator+mainExportFolder;

			File outDir=new File(exportFolder);
			if (outDir.exists() && outDir.isDirectory()) {
				// folder already exists
			} else {
				outDir.mkdir();
				IJ.log("Created output folder: "+exportFolder);
			}

			int roiCount=manager.getCount();
			IJ.log("annotated objects: "+String.valueOf(roiCount));
			if (roiCount<1) {
				// no ROIs found
				IJ.log("No ROIs found to export");
				return;
			}

			// -----------------------
        	// save segmentation masks
	        
	        // segmentation mask generation was moved to its own fcn:
	        ImagePlus maskImage=createSegmentationMask(size,width,height,roiCount);
	        if (maskImage==null)
	        	return;

			// construct output file name:
			String outputFileName=exportFolder+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+".tiff";
			// save output image:
			boolean successfullySaved=IJ.saveAsTiff(maskImage,outputFileName);
			if (successfullySaved) {
				IJ.log("Saved exported image: "+outputFileName+"\n");
				IJ.log("---------------------");
			} else {
				IJ.log("Could not save exported image");
			}


			// -------------------------
	        // save classification masks
			// repeat for classes if the user classified the objects

			if (startedClassifying){

				// find class idxs !!!!!!!!!!!!!!!!!!
				// if we only want to save the non-empty classes as masks:
				// --> nonEmptyClassNameNumbers
				///*
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

				for (int c=0; c<nonEmptyClassNameNumbers.size(); c++){

					int curClassNum=nonEmptyClassNameNumbers.get(c);

					// create export folder:
					exportFolderClass=exportFolder+File.separator+"Class_"+String.format("%02d",curClassNum);

					outDir=new File(exportFolderClass);
					if (outDir.exists() && outDir.isDirectory()) {
						// folder already exists
					} else {
						outDir.mkdir();
						IJ.log("Created class output folder: "+exportFolderClass);
					}

					// maskImage2 generation moved to its own fcn:
					ImagePlus maskImage2=Annotator_MainFrameNew.createClassMask(size,width,height,roiCount,curClassNum,manager,true);
					if (maskImage2==null)
	        			return;

					// construct output file name:
					outputFileName=exportFolderClass+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+".tiff";

					// save output image:
					successfullySaved=IJ.saveAsTiff(maskImage2,outputFileName);
					if (successfullySaved) {
						IJ.log("Saved exported image: "+outputFileName+"\n");
						IJ.log("---------------------");
					} else {
						IJ.log("Could not save exported image");
					}
				}
					
			}

			// -------------------------
			// save outlined objects

			/*
			if (saveOutlines){
				if (selectedAnnotationType.equals("semantic")){
					MessageDialog semOutlineMsg=new MessageDialog(instance,
						"Warning",
						"Saving semantic regions as outlines is not \npermitted in semantic annotation mode.");
				} else {
					// save outlined image

					String outlineExportFolder="outlined_images";
					exportFolder=destFolder+File.separator+selectedClass+File.separator+outlineExportFolder;

					// construct output file name:
					outputFileName=exportFolder+File.separator+destNameRaw.substring(0,destNameRaw.lastIndexOf("."))+".png";

					Annotator_ExportFrameNew.saveOutlinedImage(imp,outputFileName,exportFolder);
				}
			}
			*/

			IJ.log("FINISHED EXPORTING ANNOTATIONS\n---------------------");

			finishedSaving=true;
			//return;

		}
	}


	// generate class mask
	public static ImagePlus createClassMask(int size,int width,int height,int roiCount,int curClassNum, RoiManager manager, boolean useRealValue){
		ImageProcessor maskImageProc2=null;
        ImagePlus maskImage2=null;
        short[] pixels2;
        pixels2 = new short[size];

		maskImageProc2 = new ShortProcessor(width, height, pixels2, null);
        if (maskImageProc2==null) {
        	IJ.log("could not create a new mask (1)");
        	return maskImage2;
        }
        maskImage2 = new ImagePlus("title", maskImageProc2);
        maskImage2.getProcessor().setMinAndMax(0, 65535);
        //maskImage2.setMinAndMax(0, 65535); // 16-bit
        if (maskImage2==null) {
        	IJ.log("could not create a new mask (2)");
        	return maskImage2;
        }

		// start getting the objects from the annotation file
		for (int r=0; r<roiCount; r++) {
			// get r. instance
			Roi curROI=manager.getRoi(r);
			// see if it belongs to the current class (group)
			if (curROI.getGroup()==curClassNum){
				// this class
				//debug:
				IJ.log("using ROI #"+String.valueOf(r));

				// set fill value
				int fillValue=-1;
				if (useRealValue)
					fillValue=Integer.valueOf(curROI.getName());
				else
					fillValue=1;
				maskImage2.getProcessor().setColor(fillValue);
				maskImage2.getProcessor().fill(curROI);
				manager.deselect(curROI);
			}
		}

		return maskImage2;
	}


	// generate segmentation mask
	ImagePlus createSegmentationMask(int size,int width,int height,int roiCount){
		short[] pixels;

        ImageProcessor maskImageProc=null;
        ImagePlus maskImage=null;
        pixels = new short[size];

        // it will be filled with black (0) values by default and hopefully isn't displayed in a window
        maskImageProc = new ShortProcessor(width, height, pixels, null);
        if (maskImageProc==null) {
        	IJ.log("could not create a new mask (1)");
        	return maskImage;
        }
        maskImage = new ImagePlus("title", maskImageProc);
        maskImage.getProcessor().setMinAndMax(0, 65535);
        //maskImage.setMinAndMax(0, 65535); // 16-bit
        if (maskImage==null) {
        	IJ.log("could not create a new mask (2)");
        	return maskImage;
        }

		// start getting the objects from the annotation file
		for (int r=0; r<roiCount; r++) {
			// get r. instance
			Roi curROI=manager.getRoi(r);
			// set fill value
			int fillValue=Integer.valueOf(curROI.getName());
			maskImage.getProcessor().setColor(fillValue);
			maskImage.getProcessor().fill(curROI);
			manager.deselect(curROI);
		}

		return maskImage;
	}


	// make an alpha version of the input color
	Color makeAlphaColor(Color origColor, int alpha){
		return new Color(origColor.getRed(),origColor.getGreen(),origColor.getBlue(),alpha);
	}


	// set radio button for class frame
	void setColourRadioButton(String selectedClassNameVar, int selectedClassIdxList){
		if (classesFrame==null){
			// class selection frame is not opened
			return;
		}

		int curColourIdx=classFrameColours.get(selectedClassIdxList);
		String curColourName=null;
		//debug:
		IJ.log(">>>coloridx: "+String.valueOf(curColourIdx));

		// set radio buttons
	    switch (curColourIdx){
	    	case 0:
	    		rdbtnColoursR.setSelected(true);
	    		curColourName="red";
	    		selectedClassColourIdx=Color.red;
	    		break;
	    	case 1:
	    		rdbtnColoursG.setSelected(true);
	    		curColourName="green";
	    		selectedClassColourIdx=Color.green;
	    		break;
	    	case 2:
	    		rdbtnColoursB.setSelected(true);
	    		curColourName="blue";
	    		selectedClassColourIdx=Color.blue;
	    		break;
	    	case 3:
	    		rdbtnColoursC.setSelected(true);
	    		curColourName="cyan";
	    		selectedClassColourIdx=Color.cyan;
	    		break;
	    	case 4:
	    		rdbtnColoursM.setSelected(true);
	    		curColourName="magenta";
	    		selectedClassColourIdx=Color.magenta;
	    		break;
	    	case 5:
	    		rdbtnColoursY.setSelected(true);
	    		curColourName="yellow";
	    		selectedClassColourIdx=Color.yellow;
	    		break;
	    	case 6:
	    		rdbtnColoursO.setSelected(true);
	    		curColourName="orange";
	    		selectedClassColourIdx=Color.orange;
	    		break;
	    	case 7:
	    		rdbtnColoursW.setSelected(true);
	    		curColourName="white";
	    		selectedClassColourIdx=Color.white;
	    		break;
	    	case 8:
	    		rdbtnColoursK.setSelected(true);
	    		curColourName="black";
	    		selectedClassColourIdx=Color.black;
	    		break;
	    	default:
	    		IJ.log("Unexpected radio button value");
	    		break;
	    }
		
		// display currently selected class colour on the radiobuttons and label
    	lblCurrentClass.setText("<html>Current: <font color='"+curColourName+"'>"+selectedClassNameVar+"</font></html>");

    	IJ.log("(\""+selectedClassNameVar+"\")'s colour: "+curColourName);
	}


	// fetch color from the classidxlist
	public static Color getClassColour(int curColourIdx){
		Color curColour=new Color(0,0,0);
		switch (curColourIdx){
	    	case 0:
	    		curColour=Color.red;
	    		break;
	    	case 1:
	    		curColour=Color.green;
	    		break;
	    	case 2:
	    		curColour=Color.blue;
	    		break;
	    	case 3:
	    		curColour=Color.cyan;
	    		break;
	    	case 4:
	    		curColour=Color.magenta;
	    		break;
	    	case 5:
	    		curColour=Color.yellow;
	    		break;
	    	case 6:
	    		curColour=Color.orange;
	    		break;
	    	case 7:
	    		curColour=Color.white;
	    		break;
	    	case 8:
	    		curColour=Color.black;
	    		break;
	    	default:
	    		IJ.log("Unexpected class colour index");
	    		break;
	    }

	   	return curColour;
	}


	// fetch class color idx from the classidxlist
	int getClassColourIdx(Color curColour){
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



	// sets all objects with no currently assigned class (group) to the default class
	void setDefaultClass4objects(){
		if (manager==null && manager.getCount()==0){
			IJ.log("Cannot find objects for the current image");
			return;
		} else if (defaultClassNumber<1){
			IJ.log("Cannot set the default class to '"+String.valueOf(defaultClassNumber)+"'. Must be >0.");
			return;

		} else {
        	Roi tmpROI=null;
        	/*
        	// check the objects in the unassigned "group"
			manager.selectGroup(-1);
        	Roi[] manyROIs=manager.getSelectedRoisAsArray();
			IJ.log("found "+String.valueOf(manyROIs.length)+" rois currently unclassified");
			if (manyROIs.length==0){
				// no more unclassified objects left
				IJ.log("All objects on the current are already classified");
				return;
			}
			*/
			Roi[] manyROIs=manager.getRoisAsArray();
			String selectedClassNameVar=null;
			// find its colour
        	selectedClassNameVar="Class_"+String.format("%02d",defaultClassNumber);
    		int tmpIdx=classFrameColours.get(classFrameNames.indexOf(selectedClassNameVar));
    		defaultClassColour=getClassColour(tmpIdx);
    		IJ.log("default class colour: "+tmpIdx);

			for (int i=0; i<manyROIs.length; i++) {
	        	tmpROI=manyROIs[i];
	        	if (tmpROI.getGroup()<1){
	        		// unclassified ROI
	        		//debug:
	        		//IJ.log("---- ROI '"+tmpROI.getName()+"' assigned to default class '"+selectedClassNameVar+"'");
	        		tmpROI.setGroup(defaultClassNumber);
	    			tmpROI.setStrokeColor(defaultClassColour);
	    			manager.setRoi(tmpROI,findRoi(tmpROI.getName()));
	        	}
	        }
	        IJ.log("added them to the default class: "+selectedClassNameVar);
		}
	}


	// loops through all sclies of the stack and sets the default class for all objects in all roi sets
	public void runDefaultClassSetting4allSlices(){
		// save the latest opened roi stack internally
    	if (manager!=null && classMode && (managerList!=null && managerList.size()>0))
			managerList.set(currentSliceIdx-1,manager);

		// loop through all slices
		if (managerList!=null && managerList.size()>0){
			for (int mi=0; mi<managerList.size(); mi++) {
				// get the i-th roi set
				manager=managerList.get(mi);
				// set the default class
				setDefaultClass4objects();
				managerList.set(mi,manager);
			}
		} else {
			setDefaultClass4objects();
			updateROImanager(manager,showCnt);
		}

		// show the latest opened roi stack again
		//updateROImanager(managerList.get(currentSliceIdx-1),showCnt); // do this outside of the fcn
	}


	// unclassify all instances of currently selected class
	void unClassifyClass(int classNum){
		manager.selectGroup(classNum);

    	Roi tmpROI=null;
    	Roi[] manyROIs=manager.getSelectedRoisAsArray();
		IJ.log("found "+String.valueOf(manyROIs.length)+" rois in class "+String.valueOf(classNum));
		for (int i=0; i<manyROIs.length; i++) {
        	tmpROI=manyROIs[i];
        	// selectGroup selects all ROIs if none belong the the arg class classNum --> check again
        	if (tmpROI.getGroup()!=classNum)
        		continue;

    		// --> unclassify it!
			tmpROI.setGroup(0);
			tmpROI.setFillColor(null);
			tmpROI.setStrokeColor(string2colour(defAnnotCol)); // this is the default contour colour

			manager.setRoi(tmpROI,findRoi(tmpROI.getName()));

			IJ.log("Selected '"+tmpROI.getName()+"' ROI to unclassify (0)");
        }

        // deselect the current ROI so the true class colour contour can be shown
        if (imp!=null)
			imp.setRoi((Roi)null);
	}


	// find ROI in ROImanagar by its name
	int findRoi(String roiName){
		int roiIdx=-1;
		if (manager==null || manager.getCount()<1){
			IJ.log("ROI manager is null or empty, cannot find the specified ROI");
			return roiIdx;
		}

		for (int i=0; i<manager.getCount(); i++) {
			String thisName=manager.getName(i);
			if (roiName.equals(thisName)){
				roiIdx=i;
				break;
			}
		}

		return roiIdx;
	}


	public boolean importROIsFromMaskImage(ImagePlus mask){

		boolean success=false;
		int initCount=manager.getCount();

		if (mask==null){
			// failed image read, abort
			return success;
		}
		
 		//debug:
 		//mask.show();

 		int[] maskdimensions=mask.getDimensions();
		int maskwidth=maskdimensions[0];
		int maskheight=maskdimensions[1];

		// create a ShortProcessor 16-bit image from the floating point 32-bit original mask
		ShortProcessor shortMaskProc=ImportOpSef.create16bitMask(mask.getProcessor());
		mask.setProcessor(shortMaskProc);
		//mask.setProcessor(mask.getProcessor().convertToShortProcessor());

 		// get max value to see the number of labels
 		double maxValue=mask.getProcessor().getMax();
 		int[] maskHistogram=mask.getProcessor().getHistogram();

 		//manager=new RoiManager(false);
 		mask.show();
 		WindowManager.setCurrentWindow(mask.getWindow());

 		// skip 0-values in histogram (background)
 		for (int k=1; k<maskHistogram.length;k++){
 			if (maskHistogram[k]!=0){
 				//debug:
 				//IJ.log("k="+String.valueOf(k)+"\t: "+String.valueOf(maskHistogram[k]));
				boolean foundValues=false;

 				// nextgen way ------------------------------------------
 				//mask.show();
				// threshold the mask to get an ROI
				ImageProcessor curMask = mask.getProcessor();
				curMask.setThreshold(k,k,ImageProcessor.NO_LUT_UPDATE);
				Roi curROI = new ThresholdToSelection().convert(curMask);
				//mask.draw();
				mask.setRoi(curROI);
				// nextgen way end --------------------------------------

				curROI=mask.getRoi();
		        
				if (curROI==null){
					IJ.log(" >>>> failed to create ROI from mask #"+String.valueOf(k));
				} else {
					// prefix 0-s to the name
					String curROIname=String.format("%04d",k); // this should be k
					curROI.setName(curROIname);

					manager.runCommand("Add");
				}

 			}
 		}

 		mask.changes=false;
		mask.getWindow().close();

		if (manager.getCount()<=initCount)
			success=false;
		else
			success=true;

		return success;
	}


	// inner class that listens to mouse clicks
	class Runner extends Thread implements MouseListener, MouseWheelListener{ // inner class
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
			/*
			} catch(InterruptedException e){
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
			*/
			} catch(Exception e) {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				IJ.log(caw.toString());
				IJ.showStatus("");
				if (imp!=null) imp.unlock();
			}
		}


	    public void mousePressed(MouseEvent e) { 
	        IJ.log("--Mouse pressed");
	        // check if space is pressed
	        if (isSpaceDown) {
	        	// space is held down when pressing the mouse: this is an IJ function to drag the image
	        	// remember this is not a useful mouse event for us
	        	prevMouseEvent="space";
	        	// debug:
	        	//IJ.log("    ---- mouse pressed when space is down ---- ");
	        } else {
	        	prevMouseEvent="press";
	        }
	        
        } 
        public void mouseReleased(MouseEvent e) {
        	IJ.log("--Mouse released");
        	// execute a fcn only if the previous mouse event was press
        	if (!prevMouseEvent.equals("press") || isSpaceDown) {
        		prevMouseEvent="release";
        	}
        	if (imageIsActive && prevMouseEvent.equals("press")) {
        		// check if automatic smoothing is selected:
        		if(!smooth){
            		// do nothing
            	} else {
            		if (imp==null) {
        				IJ.log("No image opened");
        				imp=WindowManager.getCurrentImage();
        			}
            		curROI=imp.getRoi();
            		if (curROI==null) {
            			// empty ROI
            			IJ.log("Empty ROI");
            			return;
            		}
            		IJ.log("Smoothing selection...");
            		// smooth the selection first
            		// TODO
            		// do smoothing by interpolation command
            		//curROI=(Roi)PolygonRoi(curROI.getInterpolatedPolygon(1.0,true),Roi.POLYGON);
            		//Selection("interpolate");

            		// set options to interval=1.0 and smooth=true
            		// call the interpolate function here:

            		// from https://imagej.nih.gov/ij/source/ij/plugin/Selection.java's void interpolate() fcn:
					//public void interpolateCurRoi(){
					// ---- interpolate() fcn quote start
					double interval=1.0;
					//double interval=0.1;
					FloatPolygon poly = curROI.getInterpolatedPolygon(interval, true);
					int t = curROI.getType();
					int type = curROI.isLine()?Roi.FREELINE:Roi.FREEROI;
					if (t==Roi.POLYGON && interval>1.0)
						type = Roi.POLYGON;
					if ((t==Roi.RECTANGLE||t==Roi.OVAL||t==Roi.FREEROI) && interval>=8.0)
						type = Roi.POLYGON;
					if ((t==Roi.LINE||t==Roi.FREELINE) && interval>=8.0)
						type = Roi.POLYLINE;
					if (t==Roi.POLYLINE && interval>=8.0)
						type = Roi.POLYLINE;
					ImageCanvas ic = imp.getCanvas();
					if (poly.npoints<=150 && ic!=null && ic.getMagnification()>=12.0)
						type = curROI.isLine()?Roi.POLYLINE:Roi.POLYGON;
					Roi p = new PolygonRoi(poly,type);
					if (curROI.getStroke()!=null)
						p.setStrokeWidth(curROI.getStrokeWidth());
					p.setStrokeColor(curROI.getStrokeColor());
					p.setName(curROI.getName());
					transferProperties(curROI, p);
					imp.setRoi(p);
					curROI=p;
					// ---- interpolate() fcn quote end
					//return curROI;
					//}

					IJ.log("done");


            		//interpolateCurRoi();
            		//curROI=imp.getRoi();
            	}

        		// check if add automatically checkbox is selected
        		if (addAuto) {
        			IJ.log("Adding selection automatically...");
        			imp=WindowManager.getCurrentImage();
        			// add this roi to the list
        			if (imp==null) {
        				IJ.log("No image opened");
        				imp=WindowManager.getCurrentImage();
        			}
        			curROI=imp.getRoi();
        			if (curROI==null) {
        				IJ.log("Empty ROI");
        			} else {
	                	// add the ROI to the list:
	                	//manager.addRoi(curROI);

	                	// name the new roi by its number in the list:
	                	int lastNumber=0;
                		int prevROIcount=manager.getCount();
                		if (prevROIcount>0) {
		        			String lastName=manager.getRoi(prevROIcount-1).getName();
			        		lastNumber=Integer.parseInt(lastName);
		        		} else {
		        			// no rois yet, use 0
		        		}

		        		String curROIname=String.format("%04d",lastNumber+1);

		        		if (saveAnnotTimes) {
			        		// measure time
			        		long curTime = (System.nanoTime()-lastStartTime)/(long)1000000; //ms time
			        		annotTimes.setValue("#",annotCount,annotCount);
			        		annotTimes.setValue("label",annotCount,curROIname);
			        		annotTimes.setValue("time",annotCount,curTime);
			        		annotCount+=1;
						}


                		//manager.add(curROI,prevROIcount+1);
                		curROI.setName(curROIname);
                		
                		//imp.setRoi(curROI);	
                		
                		// this was working before:
                		manager.runCommand("Add");

	                	// check if it was successful
	                	int curROIcount=manager.getCount();
						IJ.log("Added ROI ("+curROIcount+".)");
						//manager.rename(curROIcount,String.format("%04d", curROIcount));

						if (saveAnnotTimes) {
							// TODO: delete this: -->
							lastStartTime=System.nanoTime();
						}
					}
        		}

        		// check if contour assist checkbox is selected
        		if (contAssist) {
        			if (inAssisting) {
        				// do nothing on mouse release
    				}
    				else {

	        			IJ.log("Suggesting improved contour...");
	        			imp=WindowManager.getCurrentImage();
	        			if (imp==null) {
	        				IJ.log("No image opened");
	        				imp=WindowManager.getCurrentImage();
	        			}

	        			// get current selection as init contour
	        			curROI=imp.getRoi();
	        			if (curROI==null) {
	        				IJ.log("Empty ROI");
	        			} else {
        				
	        				// can start suggestions

	        				// first start freehand selection tool for drawing --> done
				  				// on mouse release start contour correction -->

	        				// contour correction
	        				Roi newROI=null;
	        				
	        				// setting unet model paths
	        				String modelJsonFile=modelFolder+File.separator+props.getProperty("modelJsonFile"); //"model_real.json";
	        				String modelWeightsFile=modelFolder+File.separator+props.getProperty("modelWeightsFile"); //"model_real_weights.h5";
	        				String modelFullFile=modelFolder+File.separator+props.getProperty("modelFullFile"); //"model_real.hdf5";
	        				try {
	        					if (selectedCorrMethod==0) {
	        						// unet correction
	        						// debug:
	        						//IJ.log("  >> unet correction");
	        						String jsonFileName=null;
	        						String modelFileName=null;
	        						File fy = new File(modelWeightsFile);
									if(fy.exists() && !fy.isDirectory()) {
										jsonFileName=modelJsonFile;
										modelFileName=modelWeightsFile;
									} else {
										jsonFileName=null;
										modelFileName=modelFullFile;
									}
	        						newROI=contourAssistUNet(imp,curROI,intensityThreshVal,distanceThreshVal,jsonFileName,modelFileName);
	        					} else if (selectedCorrMethod==1) {
	        						// region growing
	        						// debug:
	        						//IJ.log("  >> classical correction");
	        						newROI=contourAssist(imp,curROI,intensityThreshVal,distanceThreshVal);
	        					}
	        					
	        				} catch (Exception ex){
	        					CharArrayWriter caw = new CharArrayWriter();
								PrintWriter pw = new PrintWriter(caw);
								ex.printStackTrace(pw);
								IJ.log(caw.toString());
								invertedROI=null;
	        				}
	        				if (newROI==null) {
	        					// failed, return
	        					IJ.log("Failed suggesting a better contour");
	        					invertedROI=null;
	        				} else {
	        					// display this contour
	        					imp.setRoi(newROI);

	        					// succeeded, nothing else to do
	        					IJ.log("Showing suggested contour");

	        					int prevCount=manager.getCount();

	        					// user can check it visually -->
				  						// set brush selection tool for contour modification -->

	        					curToolbar.setBrushSize(correctionBrushSize);
	        					//Prefs.set("toolbar.brush.size",correctionBrushSize);
	        					curToolbar.setTool("brush");
	        					

	  							// detect pressing "q" when they add the new contour -->
	  							// TODO
	  							if (!inAssisting) {
	  								inAssisting=true;

	  								// wait for keypress

	  								// after key press:
	  								// moved to key listener fcn

	  							} else {
	  								// do nothing
	  							}
								
	        				}
	        			}
			  					
        			}
        		}

        		// check if edit mode checkbox is selected
        		if (editMode && !startedEditing) {
        			if (inAssisting || addAuto) {
        				// cannot edit in these modes
        				MessageDialog editModeRejectMsg=new MessageDialog(instance,
		                 "Info",
		                 "Cannot edit contours if selected:\n contour assist\n add automatically");
        			} else {
        				// start edit mode
        				startedEditing=true;
        				

        				// find current image
        				imp=WindowManager.getCurrentImage();
	        			if (imp==null) {
	        				IJ.log("No image opened");
	        				startedEditing=false;
	        				origEditedROI=null;
	        			}
	        			else {
	        				// we have an image

	        				// get clicked coordinates relative to the source component
	        				Point clickedXY=imp.getCanvas().getCursorLoc();
	        				double mouseX=clickedXY.getX();
	        				double mouseY=clickedXY.getY();

	        				IJ.log("Clicked (x,y): ("+String.valueOf(mouseX)+","+String.valueOf(mouseY)+")");


	        				// search already annotated objects in ROI manager to find which ROI contains this point
	        				boolean foundit=false;
	        				Roi tmpROI=null;
	        				Roi[] manyROIs=manager.getRoisAsArray();
	        				IJ.log("found "+String.valueOf(manyROIs.length)+" rois");
							for (int i=0; i<manyROIs.length; i++) {
					        	tmpROI=manyROIs[i];

					        	//if (tmpROI.containsPoint(mouseX,mouseY)) { // <-- this doesn't work anymore on zoomed-in-added contours for some reason
					        	if (tmpROI.contains((int)Math.round(mouseX),(int)Math.round(mouseY))) {
					        		// found it
					        		foundit=true;
					        		// select this roi on the image
					        		imp.setRoi(tmpROI);
					        		origEditedROI=tmpROI;
					        		editROIidx=i;
					        		manager.setRoi(new Roi(0.0,0.0,0.0,0.0),editROIidx);
					        		IJ.log("Selected '"+manager.getName(editROIidx)+"' ROI for editing");
					        		break;
					        	}
					        }

					        if (!foundit) {
					        	// failed to find the currently clicked point's corresponding ROI
					        	IJ.log("Could not find the ROI associated with the selected point on the image.");
					        	startedEditing=false;
					        	origEditedROI=null;
					        } else {
					        	// we have a ROI selected
					        	// invert the ROI's colour to highlight it
					        	int brightness=150;
					        	origStrokeWidth=tmpROI.getStrokeWidth();
					        	float lineWidth=origStrokeWidth+(float)1.0;
					        	IJ.log("  orig line width: "+String.valueOf(origStrokeWidth)+" | new: "+String.valueOf(lineWidth));
					        	Color invColour = new Color(Math.min(255-currentSelectionColor.getRed()+brightness,255),Math.min(255-currentSelectionColor.getGreen()+brightness,255),Math.min(255-currentSelectionColor.getBlue()+brightness,255));
					        	tmpROI.setStrokeColor(invColour);
					        	tmpROI.setStrokeWidth(lineWidth);
					        	// set current tool to selection brush so we can edit the contour
					        	curToolbar.setTool("brush");
					        }
					    }
        			}
        		}

        		if (classMode){
        			// assign a class (group) to the selected ROI
        			if (editMode || addAuto || inAssisting){
        				// cannot classify in edit mode
        				MessageDialog editModeRejectMsg=new MessageDialog(instance,
		                 "Info",
		                 "Cannot classify objects if selected:\n edit mode\n contour assist\n add automatically");
        				return;
        			}
        			// find current image
    				imp=WindowManager.getCurrentImage();
        			if (imp==null) {
        				IJ.log("No image opened");
        			}
        			else {
        				// we have an image

        				// get clicked coordinates relative to the source component
        				Point clickedXY=imp.getCanvas().getCursorLoc();
        				double mouseX=clickedXY.getX();
        				double mouseY=clickedXY.getY();

        				IJ.log("Clicked (x,y): ("+String.valueOf(mouseX)+","+String.valueOf(mouseY)+")");


        				// search already annotated objects in ROI manager to find which ROI contains this point
        				boolean foundit=false;
        				Roi tmpROI=null;
        				Roi[] manyROIs=manager.getRoisAsArray();
        				IJ.log("found "+String.valueOf(manyROIs.length)+" rois");
						for (int i=0; i<manyROIs.length; i++) {
				        	tmpROI=manyROIs[i];

				        		//if (tmpROI.containsPoint(mouseX,mouseY)) { // <-- this doesn't work anymore on zoomed-in-added contours for some reason
					        	if (tmpROI.contains((int)Math.round(mouseX),(int)Math.round(mouseY))) {
				        		// found it
				        		foundit=true;
				        		// select this roi on the image
				        		imp.setRoi(tmpROI);
				        		
				        		// fetch currently selected class info
				        		// currently selected class we used as group:
				        		int curGroup=tmpROI.getGroup();
				        		if (curGroup==selectedClassNameNumber) {
				        			// already in the target group
				        			// --> unclassify it!
				        			tmpROI.setGroup(0);
				        			tmpROI.setFillColor(null);
				        			tmpROI.setStrokeColor(string2colour(defAnnotCol)); // this is the default contour colour

				        			manager.setRoi(tmpROI,i);

				        			IJ.log("Selected '"+manager.getName(i)+"' ROI to unclassify (0)");

				        		} else {
				        			startedClassifying=true;
				        			// store the current class name idx as group for saving check
				        			if (!usedClassNameNumbers.contains(selectedClassNameNumber))
				        				usedClassNameNumbers.add(selectedClassNameNumber);

				        			//debug:
				        			//IJ.log("    (cur group: "+String.valueOf(tmpROI.getGroup())+" --> "+String.valueOf(selectedClassNameNumber)+")");
				        			//origStrokeWidth=tmpROI.getStrokeWidth();
				        			tmpROI.setGroup(selectedClassNameNumber);
					        		// its colour:
					        		tmpROI.setStrokeColor(selectedClassColourIdx);
					        		//tmpROI.setStrokeWidth(1.0);
					        		/*
					        		double alphav=0.15;
					        		int alphaInt=(int)Math.round(alphav*255);
					        		tmpROI.setFillColor(makeAlphaColor(selectedClassColourIdx,alphaInt));
					        		tmpROI.setStrokeColor(selectedClassColourIdx);
					        		tmpROI.setStrokeWidth(origStrokeWidth);
									*/

					        		manager.setRoi(tmpROI,i);
					        		IJ.log("Selected '"+manager.getName(i)+"' ROI to class "+String.valueOf(selectedClassNameNumber));

					        		//debug:
					        		Color checkColour=tmpROI.getStrokeColor();
					        		IJ.log("set stroke color to: "+String.valueOf(getClassColourIdx(checkColour)));
				        		}
				        		

				        		// deselect the current ROI so the true class colour contour can be shown
				        		imp.setRoi((Roi)null);
				        		break;
				        	}
				        }

				        if (!foundit) {
				        	// failed to find the currently clicked point's corresponding ROI
				        	IJ.log("Could not find the ROI associated with the selected point on the image.");
				        } else {
				        	// we have a ROI selected
				        	// changes are already made
				        }
				    }
        		}
        	}
        	prevMouseEvent="release";
        } 
        public void mouseExited(MouseEvent e) {
        	//IJ.log("--Mouse exited");
        	if (prevMouseEvent==null || prevMouseEvent.equals("press")) {
        		// potentially didnt finish a contour and dragged the mouse out of the image window
        		// do not reset imageIsActive to false!!!!!
        		IJ.log("> not resetting imageIsActive!");
        	} else
        		imageIsActive=false;

        	imageNameLabelIsActive=false;
        } 
        public void mouseClicked(MouseEvent e) {
        	IJ.log("--Mouse clicked");

        	if (e.getSource()==WindowManager.getCurrentImage().getCanvas()){
        		//IJ.log("  on image");
        	} else if (e.getSource()==lblCurrentFile && imageNameLabelIsActive){
        		IJ.log("  on current image name label");
        		//imageIsActive=false;
        		//imageNameLabelIsActive=true;

        		// popup current image name
        		IJ.log("current image name: "+defFile);
				MessageDialog curImageNameMsg=new MessageDialog(instance,
                 "Info",
                 "Current image name: "+defFile);
				//return;
				imageNameLabelIsActive=false;
        	} 

        	prevMouseEvent="click";
        }	
        // track when the mouse is over the image or the name label in the main window
        public void mouseEntered(MouseEvent e) {
        	//IJ.log("--Mouse entered");
        	if (e.getSource()==WindowManager.getCurrentImage().getCanvas()){
	        	imageIsActive=true;
	        	IJ.log("image is active");
	        	imageNameLabelIsActive=false;
	        } else if (e.getSource()==lblCurrentFile){
        		IJ.log("image name label is active");
        		imageIsActive=false;
        		imageNameLabelIsActive=true;
        	}
        }
        public void mouseDragged(MouseEvent e) { 
	        IJ.log("--Mouse dragged");
        }

        // new trying to get the scroll amount correctly:
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {

        	if (e.isControlDown() || !(managerList!=null && managerList.size()>0)){
        		// zooming in/out --> no need to scroll!
        		(((ImageCanvas)e.getSource()).getImage().getWindow()).mouseWheelMoved(e);
        		return;
        	}

        	if (wheelMovementTimer != null && wheelMovementTimer.isRunning()) {
                wheelMovementTimer.stop();
            }

        	int direction=e.getWheelRotation();
        	wheelStepCounter=wheelStepCounter+direction;
        	IJ.log("wheelStepCounter: "+String.valueOf(wheelStepCounter));

        	wheelMovementTimer = new Timer(100, new WheelMovementTimerActionListener()); // timer delay: 100 ms
            wheelMovementTimer.setRepeats(false);
            wheelMovementTimer.start();

        	//debug:
        	IJ.log("--wheel event end--");

        }

        // previously working method that fails on multiscroll:
        /*
        public void mouseWheelMoved(MouseWheelEvent e) {
        	int direction=e.getWheelRotation();
        	//debug:
        	//IJ.log("direction: "+String.valueOf(direction));
        	int amount=0;
        	int up=0;
        	if (direction>0){
        		amount=e.getScrollAmount();
        		IJ.log("--Mouse wheel moved down "+String.valueOf(amount)); //next
        	}
        	else {
        		amount=-e.getScrollAmount();
        		IJ.log("--Mouse wheel moved up "+String.valueOf(amount)); //prev
        		up=2;
        	}

        	// update currently opened roi manager in the manager list
        	managerList.set(currentSliceIdx-1,manager);

        	currentSliceIdx=imp.getCurrentSlice() + direction; //+ direction;
        	//int tempSlice=imp.getCurrentSlice();
        	if (imp!=null && imp.getStack().getSize()>1) {
        		//currentSliceIdx=imp.getCurrentSlice();
        		IJ.log("Set current slice to "+String.valueOf(currentSliceIdx));
        		if (currentSliceIdx>=1 && currentSliceIdx<=imp.getStack().getSize()) {
	        		// display the roi set corresponding to this slice
	        		imp.setPosition(1, currentSliceIdx, 1);
	        		updateROImanager(managerList.get(currentSliceIdx-1),showCnt); // also display the rois if checked
	        		imp.setPosition(1, currentSliceIdx-1+up, 1);
	        	} else {
	        		IJ.log("no more slices to scroll");
	        		if (currentSliceIdx<1){
	        			currentSliceIdx=1;
	        		} else if (currentSliceIdx>imp.getStack().getSize()) {
	        			currentSliceIdx=imp.getStack().getSize();
	        		}

	        	}
        	}
        }
        */
	
		// execute functions bound to buttons in the main window:
		// open, load, save, overlay, prev/next
		void runCommand(String command, ImagePlus imp) {

			ImageProcessor ip=null;
			ImageProcessor mask=null;
			Roi roi;
			long startTime=0;

			if (command.equals("Open")){
				// do this later when the image is opened
			}
			else if(!started){
				IJ.showStatus("Open an image and annotate it first");
				MessageDialog notStartedMsg=new MessageDialog(instance,
                 "Warning",
                 "Click Open to select an image and annotate it first");
				return;
			}
			else{
				if (imp!=null) {
					ip = imp.getProcessor();
				}
				
				IJ.showStatus(command + "...");
				startTime = System.currentTimeMillis();
			}



			// my functions executed
			// OPEN -----------------------------------------
			if (command.equals("Open")){
				// open file loading dialog box
				// open image
				// if successful --> create folder structure
				// ask for annotation type: instances/semantic/bbox

				closeingOnPurpuse=true;

				openNew(this);

				// check if auto mask load is enabled
				if (enableMaskLoad&&autoMaskLoad&&maskFolderInited){
					// load the mask from the selected folder automatically
					loadROIs();
				}

			}
			// SAVE ---------------------------------------
			else if (command.equals("Save")){
				// open class name selector dialog box
				// create dest folder with class name
				// save the ROI.zip there

				saveData();
			}

			// LOAD --------------------------------
			else if (command.equals("Load")){
				// loads a previous annotation to the roi list

				loadROIs();
			}

			// OVERLAY ---------------------------
			else if (command.equals("Overlay")){
				// loads a previous annotation as overlay on the current image
				if (!started || WindowManager.getCurrentWindow()==null) {
					IJ.showStatus("Open an image first");
					MessageDialog notStartedMsg2=new MessageDialog(instance,
                     "Warning",
                     "Click Open to select an image first");
					return;
				}

				// file open dialog
				Opener opener5=new Opener();
				
				OpenDialog opener6=new OpenDialog("Select an annotation (ROI) .zip file",null);
				String overlayedROIfolder=opener6.getDirectory();
				String overlayedROIname=opener6.getFileName();
				// see if this is a roi zip
				if (overlayedROIname.contains("_ROIs") || overlayedROIname.contains("_bboxes")) {
					// roi zip selected

					overlayManager=new RoiManager(false);
					boolean overlayedROIsuccessfully=overlayManager.runCommand("Open",overlayedROIfolder+File.separator+overlayedROIname);
					if (!overlayedROIsuccessfully) {
						IJ.log("Failed to open ROI: "+overlayedROIname);
						MessageDialog failed2loadROIMsg=new MessageDialog(instance,
	                     "Error",
	                     "Failed to open ROI .zip file");
						return;
					} else {
						IJ.log("Opened ROI for overlay: "+overlayedROIname);
					}

					overlayedROI=true;
					int curOverlayedROInum=overlayManager.getCount();
					IJ.log("Overlayed "+String.valueOf(curOverlayedROInum)+" contours");

					// to overlay the loaded contours on image
					overlayCommandsObj=new OverlayCommands();
					// set boolean
					overlayAdded=true;

				} else if (overlayedROIname.contains("_semantic")) {
					// semantic image selected
					if (selectedAnnotationType.equals("semantic")) {
						MessageDialog semOverlayMsg=new MessageDialog(instance,
							"Warning",
							"Overlaying semantic regions is not \npermitted in semantic annotation mode.");
						return;
					}
					// TODO!

					opener5.open(overlayedROIfolder+File.separator+overlayedROIname);
					ImagePlus overlayim=WindowManager.getImage(overlayedROIname); // load image here
					if (overlayim==null) {
						IJ.log("Failed to open overlay image");
						return;
					}

					// from mask --> selection --> overlay
					ImageConverter converter=new ImageConverter(overlayim);
					converter.convertToGray8();
					(new Thresholder()).run("skip");
					Roi semanticRegions=ThresholdToSelection.run(overlayim);

					overlayim.changes=false;
					overlayim.close();
					overlaySemantic=new Overlay(semanticRegions);

					String currentColorHex=ColorToHex(defOverlay);
					String opacityColor="#66"+currentColorHex;
					overlaySemantic.setFillColor(ij.plugin.Colors.decode(opacityColor,defOverlay));

					imp.setOverlay(overlaySemantic);
					//imp.getProcessor().drawOverlay(overlaySemantic);

					// to overlay the loaded contours on image
					overlayCommandsObj=new OverlayCommands();
					// set boolean
					overlayAdded=true;

					overlayedROI=false;
					overlayedSemantic=true;
				}

				// set show overlay checkbox
				showOvl=true;
				chckbxShowOverlay.setSelected(true);


			}


			// COLOURS ------------------------------------------------
			else if (command.equals("Colours")){

				// set default color strings
				// do it in init fcn

				// ask colours in dialog box
				String[] colours=new String[9];
				colours[0]="yellow";
				colours[1]="black";
				colours[2]="blue";
				colours[3]="cyan";
				colours[4]="green";
				colours[5]="magenta";
				colours[6]="orange";
				colours[7]="red";
				colours[8]="white";
	
				GenericDialog Dialog3 = new GenericDialog("Select contour colours");
				Dialog3.addChoice("annotation: ", colours, defAnnotCol);
				Dialog3.addChoice("overlay: ", colours, defOvlCol);
				Dialog3.showDialog();

				selectedAnnotationColour = Dialog3.getNextChoice();
				selectedOverlayColour = Dialog3.getNextChoice();
				IJ.showStatus("Annotation colour: "+selectedAnnotationColour);
				IJ.log("Set annotation colour: "+selectedAnnotationColour);
				IJ.showStatus("Overlay colour: "+selectedOverlayColour);
				IJ.log("Set overlay colour: "+selectedOverlayColour);


				// set colours to these strings
				// annotation contour colour:
				if (selectedAnnotationColour.equals("yellow")) {
					currentSelectionColor=Color.yellow;
					defAnnotCol="yellow";
				} else if (selectedAnnotationColour.equals("black")) {
					currentSelectionColor=Color.black;
					defAnnotCol="black";
				} else if (selectedAnnotationColour.equals("blue")) {
					currentSelectionColor=Color.blue;
					defAnnotCol="blue";
				} else if (selectedAnnotationColour.equals("cyan")) {
					currentSelectionColor=Color.cyan;
					defAnnotCol="cyan";
				} else if (selectedAnnotationColour.equals("green")) {
					currentSelectionColor=Color.green;
					defAnnotCol="green";
				} else if (selectedAnnotationColour.equals("magenta")) {
					currentSelectionColor=Color.magenta;
					defAnnotCol="magenta";
				} else if (selectedAnnotationColour.equals("orange")) {
					currentSelectionColor=Color.orange;
					defAnnotCol="orange";
				} else if (selectedAnnotationColour.equals("red")) {
					currentSelectionColor=Color.red;
					defAnnotCol="red";
				} else if (selectedAnnotationColour.equals("white")) {
					currentSelectionColor=Color.white;
					defAnnotCol="white";
				}
				Roi.setColor(currentSelectionColor);

				if (selectedAnnotationType.equals("semantic")) {
					String currentColorHex=ColorToHex(currentSelectionColor);
					String opacityColor="#66"+currentColorHex;


					// this was working for overlay but not for new frames:
					curToolbar.setForegroundColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));
					//if (imp==null) {
					//	imp=WindowManager.getCurrentImage();
					//}
					//int alphaVal=40;
					//Overlay semanticOverlay=imp.getOverlay();
					//if (semanticOverlay==null) {
					//	semanticOverlay=new Overlay();	
					//}
					//ImageRoi overlayRoi=(ImageRoi)imp.getRoi();
					//overlayRoi.setFillColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));
					//semanticOverlay.add(overlayRoi);
					//semanticOverlay.setFillColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));
					//imp.setOverlay(semanticOverlay);
					//imp.setColor(ij.plugin.Colors.decode(opacityColor,currentSelectionColor));
					IJ.log("checking set color: "+ij.plugin.Colors.colorToString(ij.plugin.Colors.decode(opacityColor,currentSelectionColor)));
				}

				// save selected colour to config for next time default setting:
				SaveNewProp("annotationColor", defAnnotCol);


				// overlay contour colour:
				if (selectedOverlayColour.equals("yellow")) {
					defOverlay=Color.yellow;
					defOvlCol="yellow";
				} else if (selectedOverlayColour.equals("black")) {
					defOverlay=Color.black;
					defOvlCol="black";
				} else if (selectedOverlayColour.equals("blue")) {
					defOverlay=Color.blue;
					defOvlCol="blue";
				} else if (selectedOverlayColour.equals("cyan")) {
					defOverlay=Color.cyan;
					defOvlCol="cyan";
				} else if (selectedOverlayColour.equals("green")) {
					defOverlay=Color.green;
					defOvlCol="green";
				} else if (selectedOverlayColour.equals("magenta")) {
					defOverlay=Color.magenta;
					defOvlCol="magenta";
				} else if (selectedOverlayColour.equals("orange")) {
					defOverlay=Color.orange;
					defOvlCol="orange";
				} else if (selectedOverlayColour.equals("red")) {
					defOverlay=Color.red;
					defOvlCol="red";
				} else if (selectedOverlayColour.equals("white")) {
					defOverlay=Color.white;
					defOvlCol="white";
				}

				if (selectedAnnotationColour.equals(selectedOverlayColour)) {
					MessageDialog SameColoursMsg=new MessageDialog(instance,
                     "Warning",
                     "You selected the same colour for\nannotation and overlay!");
				}

				SaveNewProp("overlayColor", defOvlCol);

				// checkboxes need to be reset to show changes
				if (showOvl) {
					chckbxShowOverlay.setSelected(false);
					chckbxShowOverlay.setSelected(true);
				} else {
					chckbxShowOverlay.setSelected(true);
					chckbxShowOverlay.setSelected(false);	
				}
				
			}

			// PREVIOUS ---------------------------------------
			else if (command.equals("<")){
				// step to prev image in folder

				closeingOnPurpuse=true;

				prevImage(this);
			}

			// NEXT ---------------------------------------
			else if (command.equals(">")){
				// step to next image in folder

				closeingOnPurpuse=true;

				nextImage(this);
			}

			// OPTIONS ------------------------------------
			else if (command.equals("...")) {
				// open annotation options dialog

				openOptionsFrame();
			}

			// EXPORT -------------------------------------
			else if (command.equals("[^]")){
				// open export plugin

				closeingOnPurpuse=true;

				try {
					startExporter();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					CharArrayWriter caw = new CharArrayWriter();
					PrintWriter pw = new PrintWriter(caw);
					e.printStackTrace(pw);
					IJ.log(caw.toString());
				}
			}
		}
	
	} // Runner inner class


	// inner class for binary mask calculations
	// inner class: ObjectDump
	public class ObjectDump {

		private ArrayList<Integer> list1;
		private ArrayList<Integer> list2;
		private ArrayList<Integer> list3;
		private ArrayList<Integer> list4;
		private ImagePlus imp;

		// constructors
		public ObjectDump(){
			this.list1=new ArrayList<Integer>();
			this.list2=new ArrayList<Integer>();
			this.list3=new ArrayList<Integer>();
			this.list4=new ArrayList<Integer>();
			this.imp=new ImagePlus();
		}

		public ObjectDump(ArrayList<Integer> first, ArrayList<Integer> second, ImagePlus im){
			this.list1=first;
			this.list2=second;
			this.imp=im;
		}

		public ObjectDump(ArrayList<Integer> first, ArrayList<Integer> second, ArrayList<Integer> third, ArrayList<Integer> fourth, ImagePlus im){
			this.list1=first;
			this.list2=second;
			this.list3=third;
			this.list4=fourth;
			this.imp=im;
		}

		// getters, setters
		public ArrayList<Integer> getListFirst(){
			return this.list1;
		}
		public ArrayList<Integer> getListSecond(){
			return this.list2;
		}
		public ArrayList<Integer> getListThird(){
			return this.list3;
		}
		public ArrayList<Integer> getListFourth(){
			return this.list4;
		}
		public ImagePlus getImage(){
			return this.imp;
		}

		public void setListFirst(ArrayList<Integer> list){
			this.list1=list;
		}
		public void setListSecond(ArrayList<Integer> list){
			this.list2=list;
		}
		public void setListThird(ArrayList<Integer> list){
			this.list3=list;
		}
		public void setListFourth(ArrayList<Integer> list){
			this.list4=list;
		}
		public void setImage(ImagePlus im){
			this.imp=im;
		}

	} // ObjectDump class


	// inner class to store active contour objects
	public class ACobjectDump{
		private ImagePlus mask;
		private ImagePlus img;
		private Roi roi;
		private Rectangle bbox;
		
		// constructors
		public ACobjectDump(){
			this.mask=new ImagePlus();
			this.img=new ImagePlus();
			Roi tmp=null;
			this.roi=tmp;
			this.bbox=new Rectangle();
		}

		public ACobjectDump(ImagePlus m, Roi r, Rectangle b, ImagePlus i){
			this.mask=m;
			this.roi=r;
			this.bbox=b;
			this.img=i;
		}

		// getters, setters
		public ImagePlus getMask(){
			return this.mask;
		}
		public ImagePlus getImg(){
			return this.img;
		}
		public Roi getRoi(){
			return this.roi;
		}
		public Rectangle getBbox(){
			return this.bbox;
		}

		public void setMask(ImagePlus m){
			this.mask=m;
		}
		public void setImg(ImagePlus im){
			this.img=im;
		}
		public void setRoi(Roi r){
			this.roi=r;
		}
		public void setBbox(Rectangle box){
			this.bbox=box;
		}

	} // ACobjectDump class


	// inner class for loading the unet model
	public class ModelLoader implements Runnable {

	    private String modelJson;
	    private String modelWeights;
	    private ComputationGraph loadedModel;
	    private Annotator_MainFrameNew annotatorJinstance;

	    public ModelLoader(String modelJson,String modelWeights,Annotator_MainFrameNew annotInst) {
	        this.modelJson=modelJson;
	        this.modelWeights=modelWeights;
	        this.loadedModel=null;
	        this.annotatorJinstance=annotInst;
	    }

	    // load the model
	    public void run() {
	        this.loadedModel=loadUNetModel(this.modelJson,this.modelWeights);
	        this.annotatorJinstance.setTrainedModel(this.loadedModel);
	    }

	    public ComputationGraph getLoadedModel(){
	    	return this.loadedModel;
	    }
	} // ModelLoader class


	// inner class for storing config vars in config file
	public class AnnotatorProperties {

		private Properties props;

		// sets the default values for each config var
		public AnnotatorProperties(Annotator_MainFrameNew annotInst){
			this.props=new Properties();
			this.props.setProperty("classes","normal,cancerous");
			this.props.setProperty("annotationColor","yellow");
			this.props.setProperty("overlayColor","red");
			this.props.setProperty("semanticBrushSize","50");
			this.props.setProperty("contourAssistMaxDistance","17");
			this.props.setProperty("contourAssistThresholdGray","0.1");
			this.props.setProperty("contourAssistThresholdR","0.2");
			this.props.setProperty("contourAssistThresholdG","0.4");
			this.props.setProperty("contourAssistThresholdB","0.2");
			this.props.setProperty("contourAssistBrushsize","10");
			this.props.setProperty("contourAssistMethod","UNet");
			this.props.setProperty("modelFolder","");
			this.props.setProperty("modelJsonFile","model_real.json");
			this.props.setProperty("modelWeightsFile","model_real_weights.h5");
			this.props.setProperty("modelFullFile","model_real.hdf5");
			// add new props
			this.props.setProperty("saveAnnotTimes","no");
			this.props.setProperty("defaultAnnotType","");
			this.props.setProperty("rememberAnnotType","no");
			//this.props.setProperty("defaultClass","");
			this.props.setProperty("saveOutlines","no"); //"no" //"yes"
			this.props.setProperty("enableMaskLoad","no");
			this.props.setProperty("autoMaskLoad","no");

			annotInst.props=this.props;
		}

		// initializes the config vars from another props var
		public AnnotatorProperties(Annotator_MainFrameNew annotInst, Properties newProps){
			newProps=setDefaultProps(newProps);

			// set them to this properties instance and also the annot instance
			this.props=newProps;
			annotInst.props=this.props;
		}

		public Properties setDefaultProps(Properties newProps){
			// go through these props and check if their values exist --> set to defult if not
			String classRegex="^[a-zA-Z_0-9]+[a-zA-Z_0-9,-.]*";
			String[] colours=new String[9];
			colours[0]="yellow";
			colours[1]="black";
			colours[2]="blue";
			colours[3]="cyan";
			colours[4]="green";
			colours[5]="magenta";
			colours[6]="orange";
			colours[7]="red";
			colours[8]="white";

			String numRegex="^[0-9]+[.]?[0-9]*";

			String[] corrMethods=new String[3];
			corrMethods[0]="unet";
			corrMethods[1]="u-net";
			corrMethods[2]="classical";

			// add boolean array
			String[] booleans=new String[6];
			booleans[0]="no";
			booleans[1]="false";
			booleans[2]="0";
			booleans[3]="yes";
			booleans[4]="true";
			booleans[5]="1";
			String[] annotTypes=new String[5];
			annotTypes[0]="instance";
			annotTypes[1]="semantic";
			annotTypes[2]="bounding box";
			annotTypes[3]="boundingbox";
			annotTypes[4]="bbox";

			String folderRegex="^[a-zA-Z_0-9]+[a-zA-Z_0-9-.:\\\\/]*"; // need to double escape "/" character
			String jsonRegex="^[a-zA-Z_0-9]+[a-zA-Z_0-9-.]*(.json){1}$";
			String h5Regex="^[a-zA-Z_0-9]+[a-zA-Z_0-9-.]*((.h){1}(df)?(5){1})$";

			if (newProps.getProperty("classes")==null || !(newProps.getProperty("classes").matches(classRegex))) {
				newProps.setProperty("classes","normal,cancerous");
			}
			if (newProps.getProperty("annotationColor")==null || !(Arrays.asList(colours).contains(newProps.getProperty("annotationColor")))) {
				newProps.setProperty("annotationColor","yellow");
			}
			if (newProps.getProperty("overlayColor")==null || !(Arrays.asList(colours).contains(newProps.getProperty("overlayColor")))) {
				newProps.setProperty("overlayColor","red");
			}
			if (newProps.getProperty("semanticBrushSize")==null || !(newProps.getProperty("semanticBrushSize").matches(numRegex))) {
				newProps.setProperty("semanticBrushSize","50");
			}
			if (newProps.getProperty("contourAssistMaxDistance")==null || !(newProps.getProperty("contourAssistMaxDistance").matches(numRegex))) {
				newProps.setProperty("contourAssistMaxDistance","17");
			}
			if (newProps.getProperty("contourAssistThresholdGray")==null || !(newProps.getProperty("contourAssistThresholdGray").matches(numRegex))) {
				newProps.setProperty("contourAssistThresholdGray","0.1");
			}
			if (newProps.getProperty("contourAssistThresholdR")==null || !(newProps.getProperty("contourAssistThresholdR").matches(numRegex))) {
				newProps.setProperty("contourAssistThresholdR","0.2");
			}
			if (newProps.getProperty("contourAssistThresholdG")==null || !(newProps.getProperty("contourAssistThresholdG").matches(numRegex))) {
				newProps.setProperty("contourAssistThresholdG","0.4");
			}
			if (newProps.getProperty("contourAssistThresholdB")==null || !(newProps.getProperty("contourAssistThresholdB").matches(numRegex))) {
				newProps.setProperty("contourAssistThresholdB","0.2");
			}
			if (newProps.getProperty("contourAssistBrushsize")==null || !(newProps.getProperty("contourAssistBrushsize").matches(numRegex))) {
				newProps.setProperty("contourAssistBrushsize","10");
			}
			if (newProps.getProperty("contourAssistMethod")==null || !(Arrays.asList(corrMethods).contains(newProps.getProperty("contourAssistMethod").toLowerCase()))) {
				newProps.setProperty("contourAssistMethod","UNet");
			}
			if (newProps.getProperty("modelFolder")==null || !(newProps.getProperty("modelFolder").matches(folderRegex))) {
				newProps.setProperty("modelFolder","");
			}
			if (newProps.getProperty("modelJsonFile")==null || !(newProps.getProperty("modelJsonFile").matches(jsonRegex))) {
				newProps.setProperty("modelJsonFile","model_real.json");
			}
			if (newProps.getProperty("modelWeightsFile")==null || !(newProps.getProperty("modelWeightsFile").matches(h5Regex))) {
				newProps.setProperty("modelWeightsFile","model_real_weights.h5");
			}
			if (newProps.getProperty("modelFullFile")==null || !(newProps.getProperty("modelFullFile").matches(h5Regex))) {
				newProps.setProperty("modelFullFile","model_real.hdf5");
			}

			// new props
			if (newProps.getProperty("saveAnnotTimes")==null || !(Arrays.asList(booleans).contains(newProps.getProperty("saveAnnotTimes")))) {
				newProps.setProperty("saveAnnotTimes","no");
			}

			if (newProps.getProperty("defaultAnnotType")==null || !(Arrays.asList(annotTypes).contains(newProps.getProperty("defaultAnnotType")))) {
				newProps.setProperty("defaultAnnotType","");
			}
			if (newProps.getProperty("rememberAnnotType")==null || !(Arrays.asList(booleans).contains(newProps.getProperty("rememberAnnotType")))) {
				newProps.setProperty("rememberAnnotType","no");
			}
			/*
			if (newProps.getProperty("defaultClass")==null || !(newProps.getProperty("defaultClass").matches(classRegex))) {
				newProps.setProperty("defaultClass","");
			}
			*/

			if (newProps.getProperty("saveOutlines")==null || !(Arrays.asList(booleans).contains(newProps.getProperty("saveOutlines")))) {
				newProps.setProperty("saveOutlines","no"); // "no" // "yes"
			}
			
			if (newProps.getProperty("enableMaskLoad")==null || !(Arrays.asList(booleans).contains(newProps.getProperty("enableMaskLoad")))) {
				newProps.setProperty("enableMaskLoad","no"); // "no" // "yes"
			}
			
			if (newProps.getProperty("autoMaskLoad")==null || !(Arrays.asList(booleans).contains(newProps.getProperty("autoMaskLoad")))) {
				newProps.setProperty("autoMaskLoad","no"); // "no" // "yes"
			}
			
			return newProps;
		}

		// get/set all properties in one go
		private Properties getProps(){
			return this.props;
		}

		private void setProps(Properties newProps){
			this.props=newProps;
		}

		// set each property
		private void setPropClasses(String classList){
			this.props.setProperty("classes",classList);
		}

		private void setPropClasses(String[] classList){
			String listString="";
			for (int i=0; i<classList.length; i++) {
				listString+=classList[i];
			}
			this.props.setProperty("classes",listString);
		}

		private void setPropAnnotationColor(String annotColor){
			this.props.setProperty("annotationColor",annotColor);
		}

		private void setPropOverlayColor(String overlayColor){
			this.props.setProperty("overlayColor",overlayColor);
		}

		// TODO: setters for the rest of them

		// read and write props to file
		private Properties readProps(String propsFileName){
			try{
				File configFile = new File(propsFileName);
				FileReader reader = new FileReader(configFile);
				this.props.load(reader);
				// check if the file actually contains all properties we need
				this.props=setDefaultProps(this.props);
				//annotInst.props=this.props;
				reader.close();
				return this.props;
			} catch (FileNotFoundException ex) {
			    // file does not exist
			    IJ.log("Properties file does not exist: "+propsFileName);
			    return null;
			} catch (IOException ex) {
			    // I/O error
			    IJ.log("An error occured while trying to read the properties file");
			    return null;
			}
		}
		private Properties readProps(Annotator_MainFrameNew annotInst,String propsFileName){
			try{
				File configFile = new File(propsFileName);
				FileReader reader = new FileReader(configFile);
				this.props.load(reader);
				// check if the file actually contains all properties we need
				this.props=setDefaultProps(this.props);
				annotInst.props=this.props;
				reader.close();
				return this.props;
			} catch (FileNotFoundException ex) {
			    // file does not exist
			    IJ.log("Properties file does not exist: "+propsFileName);
			    return null;
			} catch (IOException ex) {
			    // I/O error
			    IJ.log("An error occured while trying to read the properties file");
			    return null;
			}
		}

		private boolean writeProps(String propsFileName){
			try{
				File configFile = new File(propsFileName);
				FileWriter writer = new FileWriter(configFile);
			    this.props.store(writer,null);
			    writer.close();
			    return true;
			} catch (FileNotFoundException ex) {
			    // file does not exist
			    IJ.log("Properties file does not exist");
			    return false;
			} catch (IOException ex) {
			    // I/O error
			    IJ.log("An error occured while trying to write the properties file");
			    return false;
			}
		}
		private boolean writeProps(String propsFileName, Properties newProps){
			try{
				File configFile = new File(propsFileName);
				FileWriter writer = new FileWriter(configFile);
			    newProps.store(writer,null);
			    writer.close();
			    return true;
		    } catch (FileNotFoundException ex) {
			    // file does not exist
			    IJ.log("Properties file does not exist");
			    return false;
			} catch (IOException ex) {
			    // I/O error
			    IJ.log("An error occured while trying to write the properties file");
			    return false;
			}
		}

		@Override
		public String toString(){
			return "-------------\nProperties:\n----"
				+"\nclasses:\t\t"+this.props.getProperty("classes")
				+"\nannotationColor:\t\t"+this.props.getProperty("annotationColor")
				+"\noverlayColor:\t\t"+this.props.getProperty("overlayColor")
				+"\nsemanticBrushSize:\t\t"+this.props.getProperty("semanticBrushSize")
				+"\ncontourAssistMaxDistance:\t\t"+this.props.getProperty("contourAssistMaxDistance")
				+"\ncontourAssistThresholdGray:\t\t"+this.props.getProperty("contourAssistThresholdGray")
				+"\ncontourAssistThresholdR:\t\t"+this.props.getProperty("contourAssistThresholdR")
				+"\ncontourAssistThresholdG:\t\t"+this.props.getProperty("contourAssistThresholdG")
				+"\ncontourAssistThresholdB:\t\t"+this.props.getProperty("contourAssistThresholdB")
				+"\ncontourAssistBrushsize:\t\t"+this.props.getProperty("contourAssistBrushsize")
				+"\ncontourAssistMethod:\t\t"+this.props.getProperty("contourAssistMethod")
				+"\nmodelFolder:\t\t"+this.props.getProperty("modelFolder")
				+"\nmodelJsonFile:\t\t"+this.props.getProperty("modelJsonFile")
				+"\nmodelWeightsFile:\t\t"+this.props.getProperty("modelWeightsFile")
				+"\nmodelFullFile:\t\t"+this.props.getProperty("modelFullFile")
				// add new props
				+"\nsaveAnnotTimes:\t\t"+this.props.getProperty("saveAnnotTimes")
				+"\ndefaultAnnotType:\t\t"+this.props.getProperty("defaultAnnotType")
				+"\nrememberAnnotType:\t\t"+this.props.getProperty("rememberAnnotType")
				+"\nsaveOutlines:\t\t"+this.props.getProperty("saveOutlines")
				+"\nenableMaskLoad:\t\t"+this.props.getProperty("enableMaskLoad")
				+"\nautoMaskLoad:\t\t"+this.props.getProperty("autoMaskLoad")
				;
		}

	} // AnnotatorProperties class


	// inner class for listeing to the image close operation by pressing "w" (default imagej function)
	public class ImageListenerNew implements ImageListener {

		private KeyListener listener3;
		private KeyEvent lislastKey;

		public ImageListenerNew(){
			//this.lastKey=null;
			this.lislastKey=null;
			//imp.addKeyListener(this.listener3);
		}

		public void addImageListenerNew(ImagePlus im){
			this.listener3 = new KeyListener() {
				//@Override
				public void keyReleased(KeyEvent event) {
					IJ.log("key was released");
				    lastKey=event;
				    lislastKey=event;
				}

				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void keyPressed(KeyEvent event) {
					// TODO Auto-generated method stub
					IJ.log("key was pressed");
				    lastKey=event;
				    lislastKey=event;
				}
			};
			im.addImageListener(this);
			im.getCanvas().addKeyListener(this.listener3);
		}

		@Override
		public void imageOpened(ImagePlus impp) {
		}
		@Override
	    public void imageClosed(ImagePlus impp){
	    	//impp=imp;
	    	// debug:
	    	//IJ.log("in imageClosed fcn...");
	    	if (lastKey!=null && !closeingOnPurpuse){ // && lislastKey!=null) {
		    	IJ.log("in imageClosed fnc \""+lastKey.getKeyChar()+"\" key was released");
				// make sure that pressing 'w' doesnt close the image without warning
				if (lastKey.getKeyCode() == KeyEvent.VK_W){
					// if closed, reopen it
					Window curWindow=WindowManager.getWindow(curPredictionImageName);
					if (curWindow==null) {
						// reopen the image
						Opener opener2=new Opener();
						opener2.open(destFolder+File.separator+curPredictionImageName);

						IJ.log("Re-opened file: "+curPredictionImageName);


						// keep this window instance for key event listening
						imWindow=WindowManager.getWindow(curPredictionImageName);
						imp=WindowManager.getCurrentImage();
						//curOrigImage=WindowManager.getImage(curPredictionImageName);
						ImagePlus curOrigImageIP=WindowManager.getImage(curPredictionImageName);
						curOrigImage=curOrigImageIP.getProcessor();

						// prepare annotation tools
						if (selectedAnnotationType.equals("instance") || selectedAnnotationType.equals("bounding box")){
							// instance segmentation
							// open ROI manager in bg
							manager = RoiManager.getInstance();
							if (manager == null){
								// display new ROImanager in background
							    //manager = new RoiManager(false);
							    // actually display it
							    manager = new RoiManager();
							}
							else{
								// do not close it!
							}
							if (showCnt) {
								manager.runCommand("Show All");
							}
						}


						instance.toFront();
						imWindow.toFront();
			
						// set key bindings to add new contours by pressing "t" as in ROI manager
						KeyListener listener = new KeyListener() {
							//@Override 
							public void keyPressed(KeyEvent event) { 
							    //IJ.log("key was pressed");
							    if (event.getKeyCode()==KeyEvent.VK_SPACE) {
							    	// space is pressed
							    	isSpaceDown=true;
							    	// debug:
							    	//IJ.log("  ---- space down ---- ");
							    }
							}
							 
							//@Override
							public void keyReleased(KeyEvent event) {
								IJ.log("key was released");
								if (event.getKeyCode()==KeyEvent.VK_SPACE) {
							    	// space is pressed
							    	isSpaceDown=false;
							    	// debug:
							    	//IJ.log("  ---- space up ---- ");
							    }
							    checkKeyEvents(event);
							}
							 
							//@Override
							public void keyTyped(KeyEvent event) {
							    //IJ.log("key was typed");
							}
						};

						KeyListener[] activeKeyListeners=WindowManager.getCurrentImage().getCanvas().getKeyListeners();
						MouseListener[] activeMouseListeners=WindowManager.getCurrentImage().getCanvas().getMouseListeners();
						IJ.log("current image has "+activeKeyListeners.length+" key listeners and | "+activeMouseListeners.length+" mouse listeners");
						imWindow.addKeyListener(IJ.getInstance());
						WindowManager.getCurrentImage().getCanvas().addMouseListener(new Runner("",imp));
						WindowManager.getCurrentImage().getCanvas().addMouseWheelListener(new Runner("",imp));
						WindowManager.getCurrentImage().getCanvas().addKeyListener(listener);


						// add protection against accidental image closing by pressing 'w'
						ImageListenerNew imlisn=new ImageListenerNew();
						imlisn.addImageListenerNew(imp);

						// reset assist vars as if "ctrl"+"delete" was pressed and suggestion was rejected
						if (inAssisting) {
							imp.deleteRoi();
			        		curROI=imp.getRoi();
			        		if (curROI!=null) {
			        			// failed to remove the current ROI
			        			IJ.log("Failed to remove current suggested ROI, please do it manually.");
			        			//return;
			        		}

			        		// reset vars
			        		invertedROI=null;
							ROIpositionX=0;
							ROIpositionY=0;
			        		acObjects=null;
			        		inAssisting=false;
			        		startedEditing=false;
			        		origEditedROI=null;
			        		// reset freehand selection tool
							curToolbar.setTool(Toolbar.FREEROI);
						}
					}

					//lislastKey=null;
					//lastKey=null;
					this.listener3=null;
				/*
				} else {
					// close it anyway
					impp.changes=false;
					impp.getWindow().close();
				*/
				}
			}
	    };
	    @Override
	    public void imageUpdated(ImagePlus imp) {
		}

	} // ImageListenerNew class


	// inner class for listening to mouse wheel scrolls
	private class WheelMovementTimerActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	// stopped scrolling, can process

        	if (!(managerList!=null && managerList.size()>0)){
        		// no need to do anything
        		return;
        	}
        	// update currently opened roi manager in the manager list
			managerList.set(prevSliceIdx-1,manager);

        	int tmpCurSliceIdx=wheelStepCounter;

        	if (imp!=null && imp.getStack().getSize()>1) {       		
        		if (tmpCurSliceIdx>=1 && tmpCurSliceIdx<=imp.getStack().getSize()) {
	        		// display the roi set corresponding to this slice
	        		//debug:
	        		IJ.log("----valid----");
	        		currentSliceIdx=tmpCurSliceIdx;
	        		IJ.log("Set current slice to "+String.valueOf(currentSliceIdx)+"\t\t|\t("+String.valueOf(prevSliceIdx)+")");

	        		curPredictionImage=null;
	        		curOrigImage=imp.getStack().getProcessor(currentSliceIdx);

	        	} else {
	        		IJ.log("no more slices to scroll");
	        		if (tmpCurSliceIdx<1){
	        			currentSliceIdx=1;
	        		} else if (tmpCurSliceIdx>imp.getStack().getSize()) {
	        			currentSliceIdx=imp.getStack().getSize();
	        		}
	        		IJ.log("Adjusted slice to "+String.valueOf(currentSliceIdx));
	        	}
        	}

        	imp.setPosition(1, currentSliceIdx, 1);
    		updateROImanager(managerList.get(currentSliceIdx-1),showCnt); // also display the rois if checked
    		//imp.setPosition(1, currentSliceIdx-1+up, 1);

        	prevSliceIdx=currentSliceIdx;
        	wheelStepCounter=currentSliceIdx;
            
            //debug:
            //System.out.println("stopped\t|\t"+String.valueOf(wheelStepCounter)+"\n");
        }
    } // WheelMovementTimerActionListener class

} //Annotator_MainFrameNew class
