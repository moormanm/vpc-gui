import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VPCParameterWizard {

	private BufferedImage tuningPlate = null;
	private BufferedImage tuningImage = null;
	private File tuningImageFile = null;
	private int userRows = 0;
	private int userCols = 0;
	private int userPlateRadius = 0;
	private int userErosionFactor = 0;
	private int userDilationFactor = 0;
	private JFrame parentFrame;

	private int keyMult = 1;
	
	public VPCParams run(JFrame parentFrame) {
		this.parentFrame = parentFrame;

		if (!runStepOne() || !runStepTwo() || !runStepThree() || !runStepFour()) {
			return null;
		}

		return new VPCParams().setCols(userCols).setRows(userRows)
				.setPlateRadius(userPlateRadius)
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
					if(tuningImage != null) {
						tuningImageFile = new File(selectedFileTextField.getText());
					}
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
			String cmd = launcherPath + " " + "\"" + tmp.getAbsolutePath() + "\"" + " --segment --idealRadius " + idealRadius;

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
	    
	    content.add(new JLabel("Is the image segmented properly? The rims of the plates should not appear. Only artifacts inside the plates should be visible."),  BorderLayout.NORTH);
	    content.add(imgScroller, BorderLayout.CENTER);
	    JPanel okCancelPanel = new JPanel();
	    okCancelPanel.add(cancelButton);
	    okCancelPanel.add(okButton);
	    content.add(okCancelPanel, BorderLayout.SOUTH);
	    dlg.setContentPane(content);
	    dlg.setVisible(true);
	    
		return okPressed.get();
	}
	private boolean runStepTwo() {
		//Create a modal dialog
		final JDialog dlg = new  JDialog(parentFrame,true);
		dlg.setFocusable(true);
		dlg.setSize(800,600);
		JPanel content = new JPanel(new BorderLayout());
		final PlateSizeImagePanel imgPanel = new PlateSizeImagePanel();
		imgPanel.setImage(tuningImage);
		imgPanel.setScale(imgPanel.getDefaultScale() * 4.0);
		JScrollPane imgScroller = new JScrollPane(imgPanel);
		Utilities.standardBorder(content, "2) Set plate size");
		final MyBoolean goToNextStep = new MyBoolean();
		final MyBoolean retry = new MyBoolean();
	
		
		
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
				goToNextStep.set(false);
				dlg.setVisible(false);
		        if(segmentationTest(imgPanel.getCrossHairSize())) {
		        	goToNextStep.set(true);
		        	return;
		        }
		        retry.set(true);
			}
	    });	

	    KeyListener keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				int ptx = imgPanel.getCrossHairLocation().x;
				int pty = imgPanel.getCrossHairLocation().y;
				int sz = imgPanel.getCrossHairSize();
			    int keyCode = e.getKeyCode();
			    switch( keyCode ) { 
			        case KeyEvent.VK_UP:
			        	imgPanel.setCrossHairLocation(new Point(ptx, pty-keyMult));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_DOWN:
			        	imgPanel.setCrossHairLocation(new Point(ptx, pty+keyMult));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_LEFT:
			        	imgPanel.setCrossHairLocation(new Point(ptx-keyMult, pty));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_RIGHT :
			        	imgPanel.setCrossHairLocation(new Point(ptx+keyMult, pty));
			        	keyMult++;
			            break;
			        case KeyEvent.VK_ADD :
			        	imgPanel.setCrossHairSize(sz + keyMult);
			        	keyMult++;
			        	break;
			        case KeyEvent.VK_SUBTRACT :
			        	imgPanel.setCrossHairSize(sz - keyMult);
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
	    
	    dlg.addKeyListener(keyListener);
	 
		JPanel okCancelPanel = new JPanel();
		okCancelPanel.add(cancelButton);
		okCancelPanel.add(okButton);
		
		content.add(Utilities.standardLabel("Use the arrow keys and the + and - keys to move and size the circular guide. The guide should be sized to cover an entire plate, but NOT including the bright edges."), BorderLayout.NORTH);
		content.add(imgScroller, BorderLayout.CENTER);
		content.add(okCancelPanel, BorderLayout.SOUTH);
		
		dlg.setContentPane(content);

		//Keep retrying until user is OK with result
		do {
			retry.set(false); 
		    dlg.setVisible(true); 
		} while (retry.get());
		
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
	}
}
