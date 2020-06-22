package com.github.emersonmoretto;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.process.ByteProcessor;
import ij.plugin.Resizer;
import ij.gui.Roi;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * @author Emerson Moretto
 *
 */
public class SnakeGUI_mymod {
	
	
	public static int SNAKE = 1;
	
	
	// --- MODEL -----------------------------------------------------------

	// the true snake object
	private Snake snakeinstance = null;

	// ---- IMAGE DATA -----------------------------------------------------

	private BufferedImage image = null;
	private BufferedImage imageOri = null;
	private BufferedImage imageanimation = null;
	private int[][] channel_gradient = null;
	private double[][] channel_flow = null;
	
	private double[][] gvf_v = null;
	private double[][] gvf_u = null;

	// ------------ MY MODS START HERE -------------
	public ImagePlus dispim=null;
	private ImagePlus impRes;
	private ImagePlus imp;
	private ImagePlus mask;
	private Rectangle box;
	// ------------ MY MODS END HERE ---------------

	// --- SWING COMPONENTS ------------------------------------------------

	private JLabel label0 = new JLabel("Gradient Vector Flow");
	private JLabel label1 = new JLabel("Snake");
	private JSlider slideMaxiter = new JSlider(50, 800, 400);

	private JSlider slideThreshold = new JSlider(1, 200, 100);
	private JTextField txtAlpha = new JTextField("1.0", 3);
	private JTextField txtBeta = new JTextField("1.0", 3);
	private JTextField txtGamma = new JTextField("1.0", 3);
	private JTextField txtDelta = new JTextField("1.0", 3);

	private JTextField txtStep = new JTextField("10", 3);
	private JTextField txtMinlen = new JTextField("6", 6);
	private JTextField txtMaxlen = new JTextField("9", 9);

	// --- Create the GUI ---------------------------------------------------

	public SnakeGUI_mymod(int bla) {
		label0.setVerticalTextPosition(JLabel.BOTTOM);
		label0.setHorizontalTextPosition(JLabel.CENTER);
		label1.setVerticalTextPosition(JLabel.BOTTOM);
		label1.setHorizontalTextPosition(JLabel.CENTER);

		final JFrame frame = new JFrame("Snake GVF - emoretto at usp br");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		final JPanel panneau = new JPanel();
		panneau.add(label0);
		panneau.add(label1);
		final JScrollPane scrollPane = new JScrollPane(panneau);

		JButton buttonLoad = new JButton("Load Image");
		JButton buttonRun = new JButton("Run");
		JButton buttonPreproc = new JButton("Pre-process");
		
		final JCheckBox snakeType = new JCheckBox("Snake GVF?");
		snakeType.setSelected(true);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonLoad);
		buttonPanel.add(buttonRun);
		buttonPanel.add(snakeType);
		
		//buttonPanel.add(cbShowAnim);
		buttonPanel.add(new JLabel("Iterations (100-800):"));
		buttonPanel.add(slideMaxiter);
		buttonPanel.add(new JLabel("GVF iteractions (0-200):"));
		buttonPanel.add(slideThreshold);
		//cbShowAnim.setSelected(true);

		final JPanel coefPanel = new JPanel();
		
		coefPanel.add(buttonPreproc);
		coefPanel.add(new JLabel("alpha:"));
		coefPanel.add(txtAlpha);
		coefPanel.add(new JLabel("beta:"));
		coefPanel.add(txtBeta);
		coefPanel.add(new JLabel("gamma:"));
		coefPanel.add(txtGamma);
		coefPanel.add(new JLabel("delta:"));
		coefPanel.add(txtDelta);

		final JPanel adpatPanel = new JPanel();
		//adpatPanel.add(cbAutoadapt);
		//coefPanel.add(new JLabel("adapt iterations:"));
		//coefPanel.add(txtStep);
		coefPanel.add(new JSeparator(0));
		coefPanel.add(new JLabel("min seg:"));
		coefPanel.add(txtMinlen);
		coefPanel.add(new JLabel("max seg:"));
		coefPanel.add(txtMaxlen);
		//cbAutoadapt.setSelected(true);

		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(buttonPanel);
		mainPanel.add(coefPanel);
		mainPanel.add(adpatPanel);

		// frame
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frame.getContentPane().add(mainPanel, BorderLayout.PAGE_END);
		frame.setSize(900, 600);
		frame.setVisible(true);

		// ActionListener "LOAD"
		buttonLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileDialog filedialog = new FileDialog(frame, "Choose an image file");
				filedialog.setVisible(true);
				String filename = filedialog.getFile();
				String directory = filedialog.getDirectory();
				if (filename == null)
					return;
				File file = new File(directory + File.separator + filename);
				mainPanel.setVisible(false);
				try {
					loadimage(file);
					computegflow();
				} catch (Exception ex) {
					error(ex.getMessage(), ex);
				}
				mainPanel.setVisible(true);
			}
		});

		// ActionListener "SLIDE"
		slideThreshold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (slideThreshold.getValueIsAdjusting())
					return;
				mainPanel.setVisible(false);
				try {
					computegflow();
				} catch (Exception ex) {
					error(ex.getMessage(), ex);
				}
				mainPanel.setVisible(true);
			}
		});

		// Snake Runnable
		final Runnable snakerunner = new Runnable() {
			public void run() {
				try {
					startsnake();
				} catch (Exception ex) {
					error(ex.getMessage(), ex);
				}
			}
		};

		// ActionListener "RUN"
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(snakerunner).start();
			}
		});
		
		buttonPreproc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				ImagePlus imp = new ImagePlus("Pre process", image);
				
				IJ.run(imp, "Find Edges", "");
				IJ.run(imp, "Invert", "");
				Prefs.blackBackground = false;
				IJ.run(imp, "Make Binary", "");
				
				image = imp.getBufferedImage();
			}
		});
		
		snakeType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(snakeType.isSelected()){
					SNAKE = Snake.SNAKE_GVF;
					label0.setText("Gradient Vector Flow");
				}else{
					SNAKE = Snake.SNAKE_KASS;
					label0.setText("Energia Externa - Kass");
				}
				//recalc
				computegflow();
			}
		});

		/*
		try {
			mainPanel.setVisible(false);
			loadimage(new File("trefle.png"));
			computegflow();
			mainPanel.setVisible(true);
		} catch (Exception ex) {
			error(ex.getMessage(), ex);
		}*/
	}

	// ----------- MY MODS START HERE --------------
	public SnakeGUI_mymod() {
		
	}
	// ----------- MY MODS END HERE ----------------

	// error (exception) display
	private static void error(String text, Exception ex) {
		if (ex != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			String s = sw.toString();
			s = s.substring(0, Math.min(512, s.length()));
			text = text + "\n\n" + s + " (...)";
		}
		JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	// ---------------------------------------------------------------------
	//                        DRAWING PRIMITIVES
	// ---------------------------------------------------------------------

	public void display() { /* callback from snakeinstance */
		Graphics2D gc = imageanimation.createGraphics();
		
		// draw background image
        gc.drawImage(imageOri,0,0,null);

		// draw snake lines
		gc.setColor( Color.BLUE );
		gc.setStroke(new BasicStroke(2.0f));
		List<Point> snakepoints = snakeinstance.snake;
		for (int i = 0; i < snakepoints.size(); i++) {
			int j = (i + 1) % snakepoints.size();
			Point p1 = snakepoints.get(i);
			Point p2 = snakepoints.get(j);
			gc.drawLine(p1.x, p1.y, p2.x, p2.y);
		}

		// draw snake points
		gc.setColor( Color.GREEN );
		for (int i = 0; i < snakepoints.size(); i++) {
			Point p = snakepoints.get(i);
			gc.fillRect(p.x-2, p.y-2, 5, 5);
		}

		// swing display
		label1.setIcon(new ImageIcon(imageanimation));
	}


	// -------------- MY MODS START HERE ------------
	public void display_mymod(ImagePlus dispim) { /* callback from snakeinstance */
		Graphics2D gc = imageanimation.createGraphics();
		
		// draw background image
        gc.drawImage(imageOri,0,0,null);

		// draw snake lines
		gc.setColor( Color.BLUE );
		gc.setStroke(new BasicStroke(2.0f));
		List<Point> snakepoints = snakeinstance.snake;
		for (int i = 0; i < snakepoints.size(); i++) {
			int j = (i + 1) % snakepoints.size();
			Point p1 = snakepoints.get(i);
			Point p2 = snakepoints.get(j);
			gc.drawLine(p1.x, p1.y, p2.x, p2.y);
		}

		// draw snake points
		gc.setColor( Color.GREEN );
		for (int i = 0; i < snakepoints.size(); i++) {
			Point p = snakepoints.get(i);
			gc.fillRect(p.x-2, p.y-2, 5, 5);
		}

		// swing display
		//label1.setIcon(new ImageIcon(imageanimation));
		dispim=new ImagePlus(dispim.getTitle(),imageanimation);
		dispim.draw();

	}
	// -------------- MY MODS END HERE --------------

	// ---------------------------------------------------------------------
	//                       IMAGE LOADING/COMPUTATION
	// ---------------------------------------------------------------------

	private void loadimage(File file) throws IOException {
		image = null;
		image = ImageIO.read( file );
		imageOri = ImageIO.read( file );
		imageanimation = new BufferedImage(image.getWidth(),image.getHeight(),ColorSpace.TYPE_RGB);

		// swing display
		label1.setIcon(new ImageIcon(image));
	}

	 
	/**
	 * @param f : image normalized in [0,1] 
	 * @param w : width of image
	 * @param h : height of image
	 * @param ITER : number of iterations
	 * @param mu : iteration step
	 * @return u[x,y] and v[x,y] arrays
	 */
	public double[][][] gvf(double[][] f, int w, int h, int ITER, double mu) {
	 
		// create empty arrays
		double[][] u = new double[w][h];
		double[][] v = new double[w][h];
		double[][] fx = new double[w][h];
		double[][] fy = new double[w][h];
		double[][] Lu = new double[w][h];
		double[][] Lv = new double[w][h];
	 
		// precompute edge-map (gradient)
		for (int y=1;y<(h-1);y++) {
			for (int x=1;x<(w-1);x++) {
				fx[x][y] = (f[x+1][y]-f[x-1][y])/2;
				fy[x][y] = (f[x][y+1]-f[x][y-1])/2;
				u[x][y] = fx[x][y];
				v[x][y] = fy[x][y];
			}
		}
	 
		// iterative diffusion
		for(int loop=0;loop<ITER;loop++) {
	 
			// compute laplacian of U and V
			for (int x=1 ; x < w-1 ; x++){
				for (int y=1 ; y < h-1 ; y++) {
					
					if(x > 0 && y > 0 && x < w-1 && y < h-1){
						Lu[x][y] = ((u[x-1][y]+u[x+1][y] + u[x][y-1] + u[x][y+1]) - 4 * u[x][y]) / 4; 
						Lv[x][y] = ((v[x-1][y]+v[x+1][y] + v[x][y-1] + v[x][y+1]) - 4 * v[x][y]) / 4;
					}				
				}
			}
			
			
			// Laplace
			del2(w, h, u, v, Lu, Lv);
			
					
			
			// update U and V
			for (int y=0;y<h;y++) {
				for (int x=0;x<w;x++) {
					
					double gnorm2 = fx[x][y]*fx[x][y] + fy[x][y]*fy[x][y];
	 
					u[x][y] += mu*4*Lu[x][y] - gnorm2 * (u[x][y]-fx[x][y]);
					v[x][y] += mu*4*Lv[x][y] - gnorm2 * (v[x][y]-fy[x][y]);
					
					// GVF chanel flow
					double mag = Math.sqrt(u[x][y]*u[x][y] + v[x][y]*v[x][y]);
					channel_flow[x][y] = 1 - (u[x][y] / (mag + 1e-10));
					
					/*
					GVF Norm
					mag = sqrt(u.*u+v.*v);
					px = u./(mag+1e-10); 
					py = v./(mag+1e-10);
					*/
					gvf_u[x][y] =  -1 * (u[x][y] / (mag + 1e-10));
					gvf_v[x][y] =  -1 * (v[x][y] / (mag + 1e-10));
					
				}
			}
		}
	 
		// return U and V arrays
		return new double[][][]{u,v};
	}

	/**
	 * Discrete Laplacian Operator - same del2 function from Matlab 
	 *
	 * @param w weight
	 * @param h height
	 * @param u gradient x
	 * @param v gradient y
	 * @param Lu Laplace over u
	 * @param Lv Laplace over v
	 */
	private void del2(int w, int h, double[][] u, double[][] v, double[][] Lu, double[][] Lv) {
		
		
		for (int x=0 ; x < w ; x++) {
			for (int y=0 ; y < h ; y++) {

				if(x > 0 && y > 0 && x < w-1 && y < h-1){
				}else{
					if(x==0 && y ==0){
					}
					else if(y == 0 && x < w-1){
						Lu[x][y] = (-5 * u[x][y+1] + 4 * u[x][y+2] - u[x][y+3] + 2 * u[x][y] + u[x+1][y] + u[x-1][y] - 2 * u[x][y]) / 4;
						Lv[x][y] = (-5 * v[x][y+1] + 4 * v[x][y+2] - v[x][y+3] + 2 * v[x][y] + v[x+1][y] + v[x-1][y] - 2 * v[x][y]) / 4;
					}
					
					else if(x == 0  && y < h-1){
						Lu[x][y] = (-5 * u[x+1][y] + 4 * u[x+2][y] - u[x+3][y] + 2 * u[x][y] + u[x][y+1] + u[x][y-1] - 2 * u[x][y]) / 4;
						Lv[x][y] = (-5 * v[x+1][y] + 4 * v[x+2][y] - v[x+3][y] + 2 * v[x][y] + v[x][y+1] + v[x][y-1] - 2 * v[x][y]) / 4;
					}
					
					else if(y == h-1 && x > 0 && x < w-1){
						Lu[x][y] = (-5 * u[x][y-1] + 4 * u[x][y-2] - u[x][y-3] + 2 * u[x][y] + u[x+1][y] + u[x-1][y] - 2 * u[x][y]) / 4;
						Lv[x][y] = (-5 * v[x][y-1] + 4 * v[x][y-2] - v[x][y-3] + 2 * v[x][y] + v[x+1][y] + v[x-1][y] - 2 * v[x][y]) / 4;
					}
					
					else if(x == w-1 && y > 0 && y < h-1){
						Lu[x][y] = (-5 * u[x-1][y] + 4 * u[x-2][y] - u[x-3][y] + 2 * u[x][y] + u[x][y+1] + u[x][y-1] - 2 * u[x][y]) / 4;
						Lv[x][y] = (-5 * v[x-1][y] + 4 * v[x-2][y] - v[x-3][y] + 2 * v[x][y] + v[x][y+1] + v[x][y-1] - 2 * v[x][y]) / 4;
					}
				}
			}
		}
		
		//ul
		int x = 0;
		int y = 0;
		Lu[x][y] = (-5 * u[x][y+1] + 4 * u[x][y+2] - u[x][y+3] + 2 * u[x][y] - 5 * u[x+1][y] + 4 * u[x+2][y] - u[x+3][y] + 2 * u[x][y]) / 4;
		Lv[x][y] = (-5 * v[x][y+1] + 4 * v[x][y+2] - v[x][y+3] + 2 * v[x][y] - 5 * v[x+1][y] + 4 * v[x+2][y] - v[x+3][y] + 2 * v[x][y]) / 4;
		
		//br
		x = w-1;
		y = h-1;
		Lu[x][y] = (-5 * u[x][y-1] + 4 * u[x][y-2] - u[x][y-3] + 2 * u[x][y] - 5 * u[x-1][y] + 4 * u[x-2][y] - u[x-3][y] + 2 * u[x][y]) / 4;
		Lv[x][y] = (-5 * v[x][y-1] + 4 * v[x][y-2] - v[x][y-3] + 2 * v[x][y] - 5 * v[x-1][y] + 4 * v[x-2][y] - v[x-3][y] + 2 * v[x][y]) / 4;
			
		//bl
		x = 0;
		y = h-1;
		Lu[x][y] = (-5 * u[x][y-1] + 4 * u[x][y-2] - u[x][y-3] + 2 * u[x][y] - 5 * u[x+1][y] + 4 * u[x+2][y] - u[x+3][y] + 2 * u[x][y]) / 4;
		Lv[x][y] = (-5 * v[x][y-1] + 4 * v[x][y-2] - v[x][y-3] + 2 * v[x][y] - 5 * v[x+1][y] + 4 * v[x+2][y] - v[x+3][y] + 2 * v[x][y]) / 4;
		
		//ur
		x = w-1;
		y = 0;
		Lu[x][y] = (-5 * u[x][y+1] + 4 * u[x][y+2] - u[x][y+3] + 2 * u[x][y] - 5 * u[x-1][y] + 4 * u[x-2][y] - u[x-3][y] + 2 * u[x][y]) / 4;
		Lv[x][y] = (-5 * v[x][y+1] + 4 * v[x][y+2] - v[x][y+3] + 2 * v[x][y] - 5 * v[x-1][y] + 4 * v[x-2][y] - v[x-3][y] + 2 * v[x][y]) / 4;
	}
	
	public static  String debug(double[][] mtx, int x, int y) {
	 
		DecimalFormat df = new DecimalFormat("#.####");
		StringBuilder sb = new StringBuilder();
		for(int i=0 ; i < mtx.length ; i++){
			for(int j=0 ; j < mtx[i].length ; j++){
				
				if(i == x && y == j)
					System.out.print("["+df.format(mtx[i][j]).replaceAll(",",".")+ "]\t");
				else
					System.out.print(df.format(mtx[i][j]).replaceAll(",",".")+ "\t");
				
				sb.append(df.format(mtx[i][j]).replaceAll(",",".")+ "\t");
			}
			sb.append(",\n");
			System.out.println(",");
		}
		System.out.println("");
		return sb.toString();	
			
	}
	
	
	double map(double x, double in_min, double in_max, double out_min, double out_max){
	  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}	

	private void computegflow() {
		
		int W = image.getWidth();
		int H = image.getHeight();

		//int THRESHOLD = slideThreshold.getValue();
		int THRESHOLD = 100;

		// GrayLevelScale (Luminance)
		int[][] clum = new int[W][H];
		for (int y = 0; y < H; y++)
			for (int x = 0; x < W; x++) {
				int rgb=image.getRGB(x,y);
				int r = (rgb >>16 ) & 0xFF;
				int g = (rgb >> 8 ) & 0xFF;
				int b = rgb & 0xFF;
				clum[x][y] = (int)(0.299*r + 0.587*g + 0.114*b);  
			}
				
		/**
		 * to gray and normalize
		 */
		double[][] f = new double[W][H];
		
		for(int i=0 ; i < image.getWidth() ; i++)
			for(int j=0 ; j < image.getHeight() ; j++){
				
				 int rgbP = image.getRGB(i, j);
			     int r = (rgbP >> 16) & 0xFF;
			     int g = (rgbP >> 8) & 0xFF;
			     int b = (rgbP & 0xFF);

			     int grayLevel = (r + g + b) / 3;
			        
			     //norm
			     f[i][j] = grayLevel / 255;
			}
		
		// Gradient (sobel)
		this.channel_gradient = new int[W][H]; 
		int maxgradient=0;
		for (int y = 0; y < H-2; y++)
			for (int x = 0; x < W-2; x++) {
				int p00 = clum[x+0][y+0]; int p10 = clum[x+1][y+0]; int p20 = clum[x+2][y+0];
				int p01 = clum[x+0][y+1]; /*-------------------- */ int p21 = clum[x+2][y+1];
				int p02 = clum[x+0][y+2]; int p12 = clum[x+1][y+2]; int p22 = clum[x+2][y+2];
				int sx = (p20+2*p21+p22)-(p00+2*p01+p02);
				int sy = (p02+2*p12+p22)-(p00+2*p10+p10);
				int snorm = (int)Math.sqrt(sx*sx+sy*sy);
				channel_gradient[x+1][y+1]=snorm;
				maxgradient=Math.max(maxgradient, snorm);
			}

		// distance map to binarized gradient
		channel_flow = new double[W][H];
			
		
		
		// thresholding
		boolean[][] binarygradient = new boolean[W][H];
		for (int y = 0; y < H; y++)
			for (int x = 0; x < W; x++)
				if (channel_gradient[x][y] > THRESHOLD*maxgradient/100) {
					binarygradient[x][y]=true;
				} else {
					channel_gradient[x][y]=0;
				}
		
		
		if(SNAKE == Snake.SNAKE_GVF){
		
			// THE FUCKING GVF!!!
			gvf_v = new double[W][H];
			gvf_u = new double[W][H];
			gvf(f, W, H, slideThreshold.getValue(), 0.2);
			
			// Map
			for (int y = 0; y < H; y++)
				for (int x = 0; x < W; x++)
					channel_flow[x][y] = map(channel_flow[x][y], 0, 1, 0, 255);
			
		}else{
		
			// Snake ORI
			double[][] cdist = new ChamferDistance(ChamferDistance.chamfer5).compute(binarygradient, W,H);
			for (int y = 0; y < H; y++)
				for (int x = 0; x < W; x++)
					channel_flow[x][y]=(int)(5*cdist[x][y]);
		}		
		
		//debug(gvf_v,-1,-1);
		//debug(gvf_u,-1,-1);
		
		
		// show flow + gradient
		int[] rgb = new int[3];
		BufferedImage imgflow = new BufferedImage(W, H, ColorSpace.TYPE_RGB);
		
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				int vflow = (int) ((channel_flow[x][y]/2)+0.5);
				int vgrad = binarygradient[x][y]?255:0;

				if (vgrad > 0) {
					rgb[0] = 0;
					rgb[1] = vgrad;
					rgb[2] = 0;
				} else {
					rgb[0] = 0;//Math.max(0, 255 - vflow);
					rgb[1] = 0;
					rgb[2]  = Math.max(0, 255 - vflow);
				}
				int irgb = (0xFF<<24)+(rgb[0]<<16)+(rgb[1]<<8)+rgb[2];
				imgflow.setRGB(x, y, irgb);
			}
		}
		
		// swing display
		label0.setIcon(new ImageIcon(imgflow));
	}


	// --------- MY MODS START HERE -------------
	private void computegflow_mymod(int gvfIters) {
		
		int W = image.getWidth();
		int H = image.getHeight();

		//int THRESHOLD = slideThreshold.getValue();
		//int THRESHOLD = 100;
		int THRESHOLD=gvfIters;

		// GrayLevelScale (Luminance)
		int[][] clum = new int[W][H];
		for (int y = 0; y < H; y++)
			for (int x = 0; x < W; x++) {
				int rgb=image.getRGB(x,y);
				int r = (rgb >>16 ) & 0xFF;
				int g = (rgb >> 8 ) & 0xFF;
				int b = rgb & 0xFF;
				clum[x][y] = (int)(0.299*r + 0.587*g + 0.114*b);  
			}
				
		/**
		 * to gray and normalize
		 */
		double[][] f = new double[W][H];
		
		for(int i=0 ; i < image.getWidth() ; i++)
			for(int j=0 ; j < image.getHeight() ; j++){
				
				 int rgbP = image.getRGB(i, j);
			     int r = (rgbP >> 16) & 0xFF;
			     int g = (rgbP >> 8) & 0xFF;
			     int b = (rgbP & 0xFF);

			     int grayLevel = (r + g + b) / 3;
			        
			     //norm
			     f[i][j] = grayLevel / 255;
			}
		
		// Gradient (sobel)
		this.channel_gradient = new int[W][H]; 
		int maxgradient=0;
		for (int y = 0; y < H-2; y++)
			for (int x = 0; x < W-2; x++) {
				int p00 = clum[x+0][y+0]; int p10 = clum[x+1][y+0]; int p20 = clum[x+2][y+0];
				int p01 = clum[x+0][y+1]; /*-------------------- */ int p21 = clum[x+2][y+1];
				int p02 = clum[x+0][y+2]; int p12 = clum[x+1][y+2]; int p22 = clum[x+2][y+2];
				int sx = (p20+2*p21+p22)-(p00+2*p01+p02);
				int sy = (p02+2*p12+p22)-(p00+2*p10+p10);
				int snorm = (int)Math.sqrt(sx*sx+sy*sy);
				channel_gradient[x+1][y+1]=snorm;
				maxgradient=Math.max(maxgradient, snorm);
			}

		// distance map to binarized gradient
		channel_flow = new double[W][H];
			
		
		
		// thresholding
		boolean[][] binarygradient = new boolean[W][H];
		for (int y = 0; y < H; y++)
			for (int x = 0; x < W; x++)
				if (channel_gradient[x][y] > THRESHOLD*maxgradient/100) {
					binarygradient[x][y]=true;
				} else {
					channel_gradient[x][y]=0;
				}
		
		
		if(SNAKE == Snake.SNAKE_GVF){
		
			// THE FUCKING GVF!!!
			gvf_v = new double[W][H];
			gvf_u = new double[W][H];
			//gvf(f, W, H, slideThreshold.getValue(), 0.2);
			gvf(f, W, H, gvfIters, 0.2);
			
			// Map
			for (int y = 0; y < H; y++)
				for (int x = 0; x < W; x++)
					channel_flow[x][y] = map(channel_flow[x][y], 0, 1, 0, 255);
			
		}else{
		
			// Snake ORI
			double[][] cdist = new ChamferDistance(ChamferDistance.chamfer5).compute(binarygradient, W,H);
			for (int y = 0; y < H; y++)
				for (int x = 0; x < W; x++)
					channel_flow[x][y]=(int)(5*cdist[x][y]);
		}		
		
		//debug(gvf_v,-1,-1);
		//debug(gvf_u,-1,-1);
		
		
		// show flow + gradient
		int[] rgb = new int[3];
		BufferedImage imgflow = new BufferedImage(W, H, ColorSpace.TYPE_RGB);
		
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				int vflow = (int) ((channel_flow[x][y]/2)+0.5);
				int vgrad = binarygradient[x][y]?255:0;

				if (vgrad > 0) {
					rgb[0] = 0;
					rgb[1] = vgrad;
					rgb[2] = 0;
				} else {
					rgb[0] = 0;//Math.max(0, 255 - vflow);
					rgb[1] = 0;
					rgb[2]  = Math.max(0, 255 - vflow);
				}
				int irgb = (0xFF<<24)+(rgb[0]<<16)+(rgb[1]<<8)+rgb[2];
				imgflow.setRGB(x, y, irgb);
			}
		}
		
		// swing display
		//label0.setIcon(new ImageIcon(imgflow));
	}

	// --------- MY MODS END HERE ---------------

	// ---------------------------------------------------------------------
	//                         START SNAKE SEGMENTATION
	// ---------------------------------------------------------------------

	private void startsnake() {
		int W = image.getWidth();
		int H = image.getHeight();
		int MAXLEN = Integer.parseInt(txtMaxlen.getText()); /* max segment length */

		// initial points
		double radius = ((W)/2 + (H)/2) / 2;
		double perimeter = 6.28 * radius*0.6;
		int nmb = (int) (perimeter / MAXLEN);
		Point[] circle = new Point[nmb];
		for (int i = 0; i < circle.length; i++) {
			double x = (W / 2 + 0) + (W / 2 - 2) * Math.cos((6.28 * i) / circle.length);
			double y = (H / 2 + 0) + (H / 2 - 2) * Math.sin((6.28 * i) / circle.length);
			circle[i] = new Point((int) x, (int) y);
		}

		// create snake instance
		if(SNAKE == Snake.SNAKE_GVF){
			snakeinstance = new Snake(W, H, channel_gradient, gvf_u, gvf_v, circle);
		}else{
			snakeinstance = new Snake(W, H, channel_gradient, channel_flow, circle);
		}

		// snake base parameters
		snakeinstance.alpha = Double.parseDouble(txtAlpha.getText());
		snakeinstance.beta = Double.parseDouble(txtBeta.getText());
		snakeinstance.gamma = Double.parseDouble(txtGamma.getText());
		snakeinstance.delta = Double.parseDouble(txtDelta.getText());
		snakeinstance.SHOWANIMATION=false;

		// snake extra parameters
		snakeinstance.SNAKEGUI = this;
		snakeinstance.SHOWANIMATION = true;//.isSelected();
		snakeinstance.AUTOADAPT = true;//cbAutoadapt.isSelected();
		snakeinstance.AUTOADAPT_LOOP = Integer.parseInt(txtStep.getText());
		snakeinstance.AUTOADAPT_MINLEN = Integer.parseInt(txtMinlen.getText());
		snakeinstance.AUTOADAPT_MAXLEN = Integer.parseInt(txtMaxlen.getText());
		snakeinstance.MAXITERATION = slideMaxiter.getValue();

		// animate snake
		System.out.println("initial snake points:" + snakeinstance.snake.size());
		int nmbloop = snakeinstance.loop();
		System.out.println("final snake points:" + snakeinstance.snake.size());
		System.out.println("iterations: " + nmbloop);

		// display final result
		display();

	}


	// ---------------------------------------------------------------------

	/*public static void main(String[] args) {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		new SnakeGUI();
	}*/






	// ------------ MY MODS --------------
	///*
	public ImagePlus runSnake_MYMOD(ImagePlus imp, ImagePlus mask, Rectangle box){
		ImagePlus res=null;
		res=startsnake_MYMOD(imp,mask,box);
		return res;
	}
	//*/

	/*
	public ImagePlus runSnake_MYMOD2(ImagePlus imp, ImagePlus mask, Rectangle box){
		//ImagePlus res=null;
		this.imp=imp;
		this.mask=mask;
		this.box=box;
		///
		// Snake Runnable
		final Runnable snakerunner = new Runnable() {
			public void run() {
				try {
					startsnake_MYMOD(imp,mask,box);
				} catch (Exception ex) {
					error(ex.getMessage(), ex);
				}
			}
		};

		new Thread(snakerunner).start();
		///
		//res=startsnake_MYMOD(imp,mask,box);
		return impRes;
	}
	*/

	public ImagePlus startsnake_MYMOD(ImagePlus imp, ImagePlus mask, Rectangle box) {
		// crop image by the mask
		ImagePlus imp2=new ImagePlus("orig",imp.getProcessor());
		imp2.show();
		imp2.setRoi((int)box.getX(),(int)box.getY(),(int)box.getWidth(),(int)box.getHeight());
		Resizer resizerObj=new Resizer();
		resizerObj.run("crop");
		Roi emptyRoi=null;
		imp2.setRoi(emptyRoi);

		image=imp2.getBufferedImage();
		// run pre-processing of image to find edges so that the gvf looks normal
		preprocImage();

		//imageanimation = new BufferedImage(image.getWidth(),image.getHeight(),ColorSpace.TYPE_RGB);
		SNAKE = Snake.SNAKE_GVF;
		//computegflow();

		// param optimized for small fluo nucleus
		int gvfIterNum=1;
		computegflow_mymod(gvfIterNum);

		int Wi = image.getWidth();
		int Hi = image.getHeight();

		int W = mask.getWidth();
		int H = mask.getHeight();
		IJ.log("image: "+String.valueOf(Wi)+"x"+String.valueOf(Hi)+" | bin: "+String.valueOf(W)+"x"+String.valueOf(H));

		// initial points
		// collect initial points from the mask

		long lsize2 = (long)W*H;
		int size2 = (int)lsize2;
        byte[] pixels222;
        pixels222 = new byte[size2];

        mask.show();

        byte[] tmpPixels=(byte[])mask.getProcessor().getPixelsCopy();
		ByteProcessor maskBinary=new ByteProcessor(W,H,tmpPixels,null);
		ImagePlus impBin=new ImagePlus("binarymask",maskBinary);

		//dispim=new ImagePlus("displaying",new ByteProcessor(W,H,(byte[])imp2.getProcessor().getPixelsCopy(),null));

		maskBinary.outline();
		maskBinary.skeletonize(); // to have truly 1-pixel-width outline

		// check if the image should be inverted
		boolean need2invert=false;

		int cornerCount=0;
		if (impBin.getProcessor().getPixelValue(0,0)>0) {
			cornerCount+=1;
		}
		if (impBin.getProcessor().getPixelValue(0,H-1)>0) {
			cornerCount+=1;
		}
		if (impBin.getProcessor().getPixelValue(W-1,0)>0) {
			cornerCount+=1;
		}
		if (impBin.getProcessor().getPixelValue(W-1,H-1)>0) {
			cornerCount+=1;
		}

		IJ.log("******** found "+String.valueOf(cornerCount)+" corners");
		if (cornerCount>1) {
			// need to invert the image as the background is white
			need2invert=true;
		}

		if (need2invert) {
			maskBinary.invert(); // for unknown reason the image gets inverted, so we need to invert it again
		}

		impBin.show();
		//dispim.show();

		// contour point list as 2-by-points matrix
		double[][] pointsTemp=new double[2][5000];
		int contourPointCount=0;
		for (short i=0; i<W; i++) {
			for (short j=0; j<H; j++) {
				int curPixelVal=maskBinary.get(i,j);
				//if ((!need2invert &&curPixelVal>0) || (need2invert &&curPixelVal==0)) {
				if (curPixelVal>0) {
					pointsTemp[0][contourPointCount]=i;
					pointsTemp[1][contourPointCount]=j;
					contourPointCount+=1;
				}
			}
		}
		double[][] points=new double[2][contourPointCount];
		for (int k=0; k<2; k++) {
			System.arraycopy(pointsTemp[k],0,points[k],0,contourPointCount);
		}

		Point[] circle = new Point[contourPointCount];
		for (int i = 0; i < circle.length; i++) {
			circle[i] = new Point((int) points[0][i], (int) points[1][i]);
		}


		// create snake instance
		if(SNAKE == Snake.SNAKE_GVF){
			snakeinstance = new Snake(W, H, channel_gradient, gvf_u, gvf_v, circle);
		}else{
			snakeinstance = new Snake(W, H, channel_gradient, channel_flow, circle);
		}


		// show how the points look initially
		/*
		ByteProcessor maskBinaryInit=new ByteProcessor(W,H);
		ImagePlus impResInit=new ImagePlus("init snake points",maskBinaryInit);


		List<Point> initPoints=snakeinstance.snake;
		for(int i=0;i<initPoints.size();i++) {
			Point cur=initPoints.get(i);
			double xInit=cur.getX();
			double yInit=cur.getY();
			maskBinaryInit.set((int)xInit, (int)yInit, 255);
		}
		impResInit.setProcessor(maskBinaryInit);
		impResInit.show();
		*/


		// snake base parameters
		/*
		snakeinstance.alpha = 1.0;
		snakeinstance.beta = 1.0;
		snakeinstance.gamma = 1.0;
		snakeinstance.delta = 1.0;
		*/

		//snakeinstance.gamma = 5.0;
		//snakeinstance.MAXITERATION=1;

		// params optimized for fluo
		snakeinstance.alpha = 1.1;
		snakeinstance.beta = 1.2;
		snakeinstance.gamma = 3.0;
		snakeinstance.delta = 2.0;
		snakeinstance.MAXITERATION=300;
		snakeinstance.AUTOADAPT_MINLEN = 3;
		snakeinstance.AUTOADAPT_MAXLEN = 4;


		// snake extra parameters
		snakeinstance.SNAKEGUI = this;
		snakeinstance.SHOWANIMATION = false;//.isSelected();
		snakeinstance.AUTOADAPT = true;//cbAutoadapt.isSelected();
		snakeinstance.AUTOADAPT_LOOP = 10;
		//snakeinstance.AUTOADAPT_MINLEN = 6;
		//snakeinstance.AUTOADAPT_MAXLEN = 9;
		//snakeinstance.MAXITERATION = 400;

		// animate snake
		System.out.println("initial snake points:" + snakeinstance.snake.size());
		int nmbloop = snakeinstance.loop();
		System.out.println("final snake points:" + snakeinstance.snake.size());
		System.out.println("iterations: " + nmbloop);

		// display final result
		//display();
		//display_mymod(dispim);

		// convert final points to something else:
		ByteProcessor maskBinary2=new ByteProcessor(W,H);
		//ImagePlus impRes=new ImagePlus("result",maskBinary2);
		impRes=new ImagePlus("result",maskBinary2);


		List<Point> resultPoints=snakeinstance.snake;
		for(int i=0;i<resultPoints.size();i++) {
			Point cur=resultPoints.get(i);
			double x=cur.getX();
			double y=cur.getY();
			maskBinary2.set((int)x, (int)y, 255);
		}
		impRes.setProcessor(maskBinary2);
		impRes.show();
		return impRes;

	}


	public void preprocImage(){
		ImagePlus tmpImp = new ImagePlus("Pre process", image);	
		IJ.run(tmpImp, "Find Edges", "");
		IJ.run(tmpImp, "Invert", "");
		Prefs.blackBackground = false;
		IJ.run(tmpImp, "Make Binary", "");
		
		image = tmpImp.getBufferedImage();
	}
	// ------------ MY MODS END HERE -------------
}
