import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
				} else {
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
		// TODO Auto-generated method stub
		return false;
	}

	private boolean runStepThree() {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean runStepFour() {
		// TODO Auto-generated method stub
		return false;
	}
}
