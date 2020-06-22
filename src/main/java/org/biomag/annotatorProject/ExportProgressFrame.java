package org.biomag.annotatorProject;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;

import ij.IJ;

public class ExportProgressFrame implements ActionListener{
	private JFrame progressFrame;
	private Panel progressPanel;
	private JLabel lblExportingImages;
	private JLabel lblCurrentImage;
	private JProgressBar progressBar;
	private JButton btnOk;
	private JButton btnCancelProgress;

	// constructor to set up the frame for display
	public ExportProgressFrame(){
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
		//add(lblExportingImages);
		
		lblCurrentImage = new JLabel("Current image");
		//add(lblCurrentImage);
		
		progressBar = new JProgressBar();
		//add(progressBar);
		
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
		//add(btnOk);
		
		btnCancelProgress = new JButton("Cancel");
		btnCancelProgress.addKeyListener(IJ.getInstance());
		/*
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
		*/
		//add(btnCancelProgress);


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

		
		//pack();
		//GUI.center(progressFrame);
		//GUI.center(dlg);
		//dlg.setVisible(true);
		progressFrame.setVisible(true);

		btnOk.setEnabled(false);
	}

	// getters
	public JFrame getProgressFrame(){
		return this.progressFrame;
	}
	public Panel getProgressPanel(){
		return this.progressPanel;
	}
	public JLabel getLblExportingImages(){
		return this.lblExportingImages;
	}
	public JLabel getLblCurrentImage(){
		return this.lblCurrentImage;
	}
	public JProgressBar getProgressBar(){
		return this.progressBar;
	}
	public JButton getBtnOk(){
		return this.btnOk;
	}
	public JButton getBtnCancelProgress(){
		return this.btnCancelProgress;
	}

	public void setCancelListener(boolean started,boolean finished){
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}