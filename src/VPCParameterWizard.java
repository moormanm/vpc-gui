import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSlider;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VPCParameterWizard {

	private BufferedImage tuningImage = null;
	private File tuningImageFile = null;

	private int userPlateRadius = 0;
	private int userMaxPlaqueRadius = 0;
	private int userMinPlaqueRadius = 0;
	private JFrame parentFrame;

	private int keyMult = 1;
	
	public VPCParams run(JFrame parentFrame) {
		this.parentFrame = parentFrame;

		if (!runStepOne() || !runStepTwo() || !runStepThree() || !runStepFour()) {
			return null;
		}

		return new VPCParams().
				setPlateRadius(userPlateRadius).
				setMaxPlaqueRadius(userMaxPlaqueRadius).
				setMinPlaqueRadius(userMinPlaqueRadius);
	

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
	    final JSlider slider = new JSlider(0,5);
	    slider.setValue(0);
	    slider.setFocusable(false);
		slider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				int val = slider.getValue();
				imgPanel.setScale(imgPanel.getDefaultScale() + val / 5.0);
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
				if(imgPanel.getImage() == null) {
					Utilities.showError("No image selected. Select an image.");
					return;
				}
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
					if(tuningImage != null) {
						tuningImageFile = new File(selectedFileTextField.getText());
						slider.setValue(0);
					}
					
				} else { 
					tuningImage = null;
					return;
				}
			}		
		});
		
		JPanel grid = new JPanel(new GridLayout(2,1));
		flowPanel.add(chooseButton);
		flowPanel.add(Utilities.standardLabel("Selected Image:"));
		flowPanel.add(selectedFileTextField);
		grid.add(flowPanel);
		grid.add(slider);
		
		
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.add(cancelButton);
		okCancelPanel.add(okButton);
		
	
		content.add(grid, BorderLayout.NORTH);
		JScrollPane imgScroller = new JScrollPane(imgPanel);
		content.add(imgScroller, BorderLayout.CENTER);
		content.add(okCancelPanel, BorderLayout.SOUTH);
		
		
		dlg.setContentPane(content);
		dlg.setVisible(true);
		
		return goToNextStep.get();
	}

	private boolean segmentationTest(int idealRadius) {
	    System.out.println("Plate radius is :" + idealRadius);
		//Copy the image to temp file
		File tmp = null;
		try {
			tmp = File.createTempFile("tempImg", ".jpg");
			Utilities.copyFile(tuningImageFile, tmp);
		} catch (IOException e) {
			Utilities.showError("Could not create temporary file: " + e);
			return false;
		}
		
		String launcherPath = this.getClass().getResource("vpc.exe").getPath();
		Runtime r = Runtime.getRuntime();
		try {
			String cmd = launcherPath + " " + "\"" + tmp.getAbsolutePath() + "\"" + " --segment --plateRadius " + idealRadius;

			Process p = r.exec(cmd);
			p.waitFor();

		if (p.exitValue() != 0) {
				Utilities.showError("Could not segment image");
				return false;
			}
		} catch (IOException e) {
			Utilities.showError("Internal error: " + e.toString());
		} catch (InterruptedException e) {
			Utilities.showError("Internal error: " + e.toString());
		}		
		
		
		//Make some fancy dialog asking y or n
		final JDialog dlg = new  JDialog(parentFrame,true);
		dlg.setSize(800,600);
		dlg.setFocusable(true);
		JPanel content = new JPanel(new BorderLayout());
		final ImagePanel imgPanel = new ImagePanel();
		imgPanel.setImage(tmp.getAbsolutePath());
		imgPanel.setScale(imgPanel.getDefaultScale() * 4.0);
		
		JScrollPane imgScroller = new JScrollPane(imgPanel);
		final MyBoolean okPressed = new MyBoolean();
	    final JButton cancelButton = new JButton("Go Back");
	    cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				okPressed.set(false);
				dlg.setVisible(false);
			}
	    });

	    final JButton okButton = new JButton("OK");
	    okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				okPressed.set(true);
				dlg.setVisible(false);
			}
	    });	
	    
	    content.add(Utilities.standardLabel("<html>Is the image segmented properly? The rims of the plates should not appear. <br/> Only artifacts inside the plates should be visible.</html>"),  BorderLayout.NORTH);
	    content.add(imgScroller, BorderLayout.CENTER);
	    JPanel okCancelPanel = new JPanel();
	    okCancelPanel.add(cancelButton);
	    okCancelPanel.add(okButton);
	    content.add(okCancelPanel, BorderLayout.SOUTH);
	    dlg.setContentPane(content);
	    dlg.setVisible(true);
	    
		return okPressed.get();
	}
	
	private Integer runGetParamDialog(String title, String instructions, int suggestedSize) {
		//Create a modal dialog
		final JDialog dlg = new  JDialog(parentFrame,true);
		dlg.setFocusable(true);
		dlg.setSize(800,600);
		JPanel content = new JPanel(new BorderLayout());
		final PlateSizeImagePanel imgPanel = new PlateSizeImagePanel();
		final JSlider slider = new JSlider(0,5);
		slider.setValue(0);
		slider.setFocusable(false);
		slider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				int val = slider.getValue();
				imgPanel.setScale(imgPanel.getDefaultScale() + val / 5.0);
			}
		});
		
		imgPanel.setImage(tuningImage);
		imgPanel.setScale(imgPanel.getDefaultScale() * 4.0);
		imgPanel.setCrossHairSize(suggestedSize);
		JScrollPane imgScroller = new JScrollPane(imgPanel);
		Utilities.standardBorder(content, title);
		

		final MyBoolean cancel = new MyBoolean();

	
	    final JButton cancelButton = new JButton("Cancel");
	    cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancel.set(true);
				dlg.setVisible(false);
			}
	    });

	    final JButton okButton = new JButton("OK");
	    okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		        dlg.setVisible(false);
			}
	    });	

	    
	    dlg.addKeyListener(imgPanel.keyListener);
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.add(cancelButton);
		okCancelPanel.add(okButton);
		
		JPanel grid = new JPanel(new GridLayout(2,1));
		grid.add(Utilities.standardLabel(instructions));
		grid.add(slider);
		content.add(grid, BorderLayout.NORTH);
		content.add(imgScroller, BorderLayout.CENTER);
		content.add(okCancelPanel, BorderLayout.SOUTH);
		
		dlg.setContentPane(content);
	
		dlg.setVisible(true); 
		if(cancel.get()) {
			return null;
		}
		else return imgPanel.getCrossHairSize();
	}
	
	private boolean runStepTwo() {
		
	    Integer ret = runGetParamDialog("2) Set Plate Size.", 
	    		"<html>Use the arrow keys and the + and - keys to move and size the circular guide.<br/> The guide should be sized to cover an entire plate, but NOT including the bright edges.</html>",
	    		185);
		if(ret == null) {
			return false;
		}
	    if(segmentationTest(ret)) {
	    	userPlateRadius = ret;
	    	return true;
	    }
	    //recurse please
		return runStepTwo();
		
	}

	private boolean runStepThree() {
		Integer ret = runGetParamDialog("3) Set Maximum Plaque Size",
				                        "<html>Set the maximum plaque size.</html>",
				                        25);
		if(ret == null) {
			return false;
		}
		userMaxPlaqueRadius = ret;
		return true;
	}

	private boolean runStepFour() {
			Integer ret = runGetParamDialog("4) Set Minimum Plaque Size", 
					                        "<html>Set the minimum plaque size.</html>",
					                        10);
			if(ret == null) {
				return false;
			}
			userMinPlaqueRadius = ret;
			return true;
	}
	

	
	@SuppressWarnings("serial")
	private class PlateSizeImagePanel extends ImagePanel {
		private int crossHairSize = 185;
		private Point crossHairLocation = new Point(300,300);
		public void setCrossHairSize(int sz) {
			crossHairSize = sz;
			repaint();
		}
		public int getCrossHairSize() {
			return crossHairSize;
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
		        
	        
	        //Make a scaled, local version of the crosshair
	        double crossHairLocationX = Math.round(this.crossHairLocation.x * scale);
	        double crossHairLocationY = Math.round(this.crossHairLocation.y * scale);
	        
	        double crossHairSizeScaled = this.crossHairSize * scale;
	        //Draw the crosshair
	        //draw the x axis
	        g.setColor(Color.WHITE);
	        g.drawLine((int)(crossHairLocationX - Math.round(crossHairSizeScaled)),
	        		   (int)(crossHairLocationY),
	        		   (int)(crossHairLocationX + Math.round(crossHairSizeScaled)),
	        		   (int)(crossHairLocationY));
	        
	        //draw the y axis
	        g.drawLine((int)(crossHairLocationX ),
	        		   (int)(crossHairLocationY - Math.round(crossHairSizeScaled)),
	        		   (int)(crossHairLocationX),
	        		   (int)(crossHairLocationY + Math.round(crossHairSizeScaled)));
	        
	        //Draw the oval around the xhair
	        g.drawOval( (int)(crossHairLocationX - Math.round(crossHairSizeScaled)),
	        		    (int)(crossHairLocationY - Math.round(crossHairSizeScaled)),
	        		    (int) Math.round(crossHairSizeScaled*2),
	        		    (int) Math.round(crossHairSizeScaled*2));
	    }  
	    
	   public KeyListener keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				int ptx = getCrossHairLocation().x;
				int pty = getCrossHairLocation().y;
				int sz = getCrossHairSize();
			    int keyCode = e.getKeyCode();
			    switch( keyCode ) { 
			        case KeyEvent.VK_UP:
			        	setCrossHairLocation(new Point(ptx, pty-keyMult));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_DOWN:
			        	setCrossHairLocation(new Point(ptx, pty+keyMult));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_LEFT:
			        	setCrossHairLocation(new Point(ptx-keyMult, pty));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_RIGHT :
			        	setCrossHairLocation(new Point(ptx+keyMult, pty));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_ADD :
			        	setCrossHairSize(Math.min(600, sz + keyMult));
			        	keyMult++;
			        	break;
			        case KeyEvent.VK_SUBTRACT :
			        	setCrossHairSize(Math.max(0, sz - keyMult));
			        	keyMult++;
			        	break;
			     }
			    //enforce Max key mult 
			    keyMult = Math.min(keyMult, 20);
			}
			

			@Override
			public void keyReleased(KeyEvent e) {
				//reset the key multiplier when release is detected
				keyMult = 1;
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
	    };
	    
	    
	}
}
