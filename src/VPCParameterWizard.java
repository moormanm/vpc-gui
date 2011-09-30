import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VPCParameterWizard {

	private BufferedImage tuningPlate = null;
	private BufferedImage tuningImage = null;
	private int userRows = 0;
	private int userCols = 0;
	private int userPlateDiameter = 0;
	private int userErosionFactor = 0;
	private int userDilationFactor = 0;
	private JFrame parentFrame;

	public VPCParams run(JFrame parentFrame) {
		this.parentFrame = parentFrame;

		if (!runStepOne() || !runStepTwo() || !runStepThree() || !runStepFour()) {
			return null;
		}

		return new VPCParams().setCols(userCols).setRows(userRows)
				.setPlateDiameter(userPlateDiameter)
				.setErosionFactor(userErosionFactor)
				.setDilationFactor(userDilationFactor);

	}

	//Stupid class used to make action handling easier. Can't use the 'Boolean' final class
	//so im making my own.
	private class MyBoolean  {
		boolean val = false;
		public boolean get() {
			return val;
		}
		public void set(boolean val) {
			this.val = val;
		}
	}

	private boolean runStepOne() {
		
		//Create a modal dialog
		final JDialog dlg = new  JDialog(parentFrame,true);
		dlg.setSize(800,600);
		JPanel content = new JPanel(new BorderLayout());
		final ImagePanel imgPanel = new ImagePanel();
		Utilities.standardBorder(content, "1) Choose the image to use for parameter tuning.");
	    final JTextField selectedFileTextField = new JTextField();
	    final MyBoolean goToNextStep = new MyBoolean();
	    
	    final JButton cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				goToNextStep.set(false);
				dlg.setVisible(false);
			}
	    });

	    final JButton okButton = new JButton("Next Step");
	    okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				goToNextStep.set(true);
				dlg.setVisible(false);
			}
	    });	    
	    
		selectedFileTextField.setColumns(50);
		selectedFileTextField.setEditable(false);
		
		JPanel flowPanel = new JPanel();
		JButton chooseButton = new JButton("Choose");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Choose a sample viral plaque image.");
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					selectedFileTextField.setText(chooser.getSelectedFile()
							.toString());
					imgPanel.setImage(selectedFileTextField.getText());
					tuningImage = imgPanel.getImage();
				} else {
					tuningImage = null;
					return;
				}
			}		
		});
		flowPanel.add(chooseButton);
		flowPanel.add(Utilities.standardLabel("Selected Image:"));
		flowPanel.add(selectedFileTextField);
		
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.add(cancelButton);
		okCancelPanel.add(okButton);
		
		content.add(flowPanel, BorderLayout.NORTH);
		content.add(imgPanel, BorderLayout.CENTER);
		content.add(okCancelPanel, BorderLayout.SOUTH);
		
		
		dlg.setContentPane(content);
		dlg.setVisible(true);
		
		return goToNextStep.get();
	}

	private boolean runStepTwo() {
		//Create a modal dialog
		final JDialog dlg = new  JDialog(parentFrame,true);
		dlg.setSize(800,600);
		JPanel content = new JPanel(new BorderLayout());
		final PlateSizeImagePanel imgPanel = new PlateSizeImagePanel();
		imgPanel.setImage(tuningImage);
		Utilities.standardBorder(content, "2) Set plate size.");
		final MyBoolean goToNextStep = new MyBoolean();
		final JSlider sizeSlider = new JSlider(50,500);
	
		
		sizeSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				int val = sizeSlider.getValue();
				imgPanel.setCrossHairSize(val);
			}
		});
		
		
	    final JButton cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				goToNextStep.set(false);
				dlg.setVisible(false);
			}
	    });

	    final JButton okButton = new JButton("Next Step");
	    okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				goToNextStep.set(true);
				dlg.setVisible(false);
			}
	    });	
	    final JButton upButton = new JButton("U");
	    final JButton downButton = new JButton("D");
	    final JButton leftButton = new JButton("L");
	    final JButton rightButton = new JButton("R");
	    
	    ActionListener directionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int ptx = imgPanel.getCrossHairLocation().x;
				int pty = imgPanel.getCrossHairLocation().y;
				if(e.getSource() == upButton) {
					imgPanel.setCrossHairLocation(new Point(ptx, pty-1));
				}
				else if(e.getSource() == downButton) {
					imgPanel.setCrossHairLocation(new Point(ptx, pty+1));
				}
				else if(e.getSource() == leftButton) {
					imgPanel.setCrossHairLocation(new Point(ptx-1, pty));
				}
				else if(e.getSource() == rightButton) {
					imgPanel.setCrossHairLocation(new Point(ptx+1, pty));
				}
				
			}
	    	
	    };
	    
	    upButton.addActionListener(directionListener);
	    downButton.addActionListener(directionListener);
	    leftButton.addActionListener(directionListener);
	    rightButton.addActionListener(directionListener);
	    
	    
	    
	    JPanel controlsPanel = new JPanel();
		controlsPanel.add(sizeSlider);
		
		JPanel dirPanel = new JPanel(new BorderLayout());
		dirPanel.add(upButton, BorderLayout.NORTH);
		dirPanel.add(downButton, BorderLayout.SOUTH);
		dirPanel.add(leftButton, BorderLayout.WEST);
		dirPanel.add(rightButton, BorderLayout.EAST);
	
		controlsPanel.add(dirPanel);
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.add(cancelButton);
		okCancelPanel.add(okButton);
		
		content.add(controlsPanel, BorderLayout.NORTH);
		content.add(imgPanel, BorderLayout.CENTER);
		content.add(okCancelPanel, BorderLayout.SOUTH);
		
		dlg.setContentPane(content);
		dlg.setVisible(true);
		return goToNextStep.get();
		
	}

	private boolean runStepThree() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean runStepFour() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private class PlateSizeImagePanel extends ImagePanel {
		private int crossHairSize = 360;
		private Point crossHairLocation = new Point(300,300);
		public void setCrossHairSize(int sz) {
			crossHairSize = sz;
			repaint();
		}
		
		public Point getCrossHairLocation() {
			   return new Point(crossHairLocation);
			}
		public void setCrossHairLocation(Point pt) {
		   crossHairLocation = pt;
		   repaint();
		}
		
	    protected void paintComponent(Graphics g)  
	    {  
	        super.paintComponent(g);
	        if(image == null) {
	        	return;
	        }
		        
	        System.out.println("repainting... " + crossHairLocation);
	        
	        //Make a scaled, local version of the crosshair
	        double crossHairLocationX = Math.round(this.crossHairLocation.x * scale);
	        double crossHairLocationY = Math.round(this.crossHairLocation.y * scale);
	        
	        double crossHairSizeScaled = this.crossHairSize * scale;
	        //Draw the crosshair
	        //draw the x axis
	        g.setColor(Color.WHITE);
	        g.drawLine((int)(crossHairLocationX - Math.round(crossHairSizeScaled / 2.0)),
	        		   (int)(crossHairLocationY),
	        		   (int)(crossHairLocationX + Math.round(crossHairSizeScaled / 2.0)),
	        		   (int)(crossHairLocationY));
	        
	        //draw the y axis
	        g.drawLine((int)(crossHairLocationX ),
	        		   (int)(crossHairLocationY - Math.round(crossHairSizeScaled / 2.0)),
	        		   (int)(crossHairLocationX),
	        		   (int)(crossHairLocationY + Math.round(crossHairSizeScaled / 2.0)));
	        
	        //Draw the oval around the xhair
	        g.drawOval( (int)(crossHairLocationX - Math.round(crossHairSizeScaled / 2.0)),
	        		    (int)(crossHairLocationY - Math.round(crossHairSizeScaled / 2.0)),
	        		    (int) Math.round(crossHairSizeScaled),
	        		    (int) Math.round(crossHairSizeScaled));
	    }  
	}
}
