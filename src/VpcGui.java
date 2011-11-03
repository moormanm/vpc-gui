import java.awt.BorderLayout;
import java.awt.Dimension;

import java.awt.FlowLayout;

import java.awt.GraphicsEnvironment;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import javax.swing.JTable;
import javax.swing.JTextField;

import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import net.java.dev.designgridlayout.*;

@SuppressWarnings("serial")
public class VpcGui extends JFrame {
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new VpcGui();
			}
		});
	}

	private VPCParams currentParameters = null;
	private JLabel currentParametersName = new JLabel("None");
	
	private JMenuItem saveResultsToFileMenuItem =  new JMenuItem("Save Results to File"); 
	private JButton chooseFolderButton = new JButton("Choose Folder");
	private JButton countAllButton = new JButton("Count All");
	private JButton countSelectedButton = new JButton("Count Selected");
	private JTextField currentFolderTextField = new JTextField();
	private DefaultTableModel model = new DefaultTableModel();
	private JTable fileNameTable = new JTable(model) {
		public boolean isCellEditable(int rowIndex, int colIndex) {
			return false; // Disallow the editing of any cell
		}
	};
	private JSlider slider = new JSlider(0, 5);
	private JButton newCalibrationButton = new JButton("New");
	private JButton loadCalibrationButton = new JButton("Load");
	private JButton saveCalibrationButton = new JButton("Save");

	private ImagePanel imagePanel = new ImagePanel();
	private Vector<JLabel> resultsLabels = new Vector<JLabel>();
	private JPanel CountPlaquesPanel = new JPanel();
	private JPanel CalibrationsPanel = new JPanel();
	private JPanel ImagesPanel = new JPanel();
	private JScrollPane imageScroller = new JScrollPane();
	private JPanel ResultsPanel = new JPanel();

	private JFrame topFrame = new JFrame();


	private JMenuBar makeMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		//JMenu viewMenu = new JMenu("View");
		//JMenu helpMenu = new JMenu("Help");
		
		fileMenu.add(saveResultsToFileMenuItem);
		saveResultsToFileMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Create the file to be written
				FileWriter out = null;
				// Run the save file dialog
				JFileChooser chooser = new JFileChooser();
				;
				int returnVal = chooser.showSaveDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File saveFile = new File(chooser.getSelectedFile().getPath()
						+ ".csv");
				try {

					out = new FileWriter(saveFile);
				} catch (IOException e1) {
					Utilities.showError("Could not create file: "
							+ e);
					return;
				}

				// Write out the header
				int maxWells = 0;
				HashMap<String, VPCResult> onScreenResults = getOnScreenResults();
				for (VPCResult r : onScreenResults.values()) {
					int tmpWells = 0;
					if(r.count == null) {
						continue;
					}
					for (Vector<Integer> row : r.count) {
						tmpWells += row.size();
					}
					maxWells = Math.max(maxWells, tmpWells);
				}

				try {
					out.append("Filename,");
					for (int i = 1; i <= maxWells; i++) {
						out.append("W" + i + ",");
					}
					out.append(Utilities.newLine);
					
					for (String fileName : onScreenResults.keySet()) {
						VPCResult r = onScreenResults.get(fileName);
						out.append(fileName + ",");
						if(r.count == null) {
							out.append(Utilities.newLine);
							continue;
						}
						for (Vector<Integer> row : r.count) {
							for (Integer val : row) {
								out.append(val + ",");
							}
						}
						out.append(Utilities.newLine);

					}
					out.flush();
					out.close();
					Utilities.showInfo( "Successfully saved to file.", "");
				} catch (IOException e1) {
					Utilities.showError("IO Error");
					return;
				}
			    
			    
			}
			
		});

		//menuItem = new JMenuItem("Generate Report");
		//fileMenu.add(menuItem);

        //menuItem = new JMenuItem("About");
		//helpMenu.add(menuItem);

		menuBar.add(fileMenu);
		//menuBar.add(viewMenu);
		//menuBar.add(helpMenu);
		return menuBar;
	}

	private void setSizes() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		Rectangle r = graphicsEnvironment.getMaximumWindowBounds();
		setMaximizedBounds(r);
		Dimension windowSize = new Dimension(r.width, r.height);
		this.setPreferredSize(windowSize);

		// Make Calibration panel and CountPlaquesPanel the same size
		int maxH = Math.max(CalibrationsPanel.getPreferredSize().height,
				CountPlaquesPanel.getPreferredSize().height);
		CalibrationsPanel.setPreferredSize(new Dimension(CalibrationsPanel
				.getPreferredSize().width, maxH));
		CountPlaquesPanel.setPreferredSize(new Dimension(CountPlaquesPanel
				.getPreferredSize().width, maxH));

		// Make the Results and the Images Panel the same size
		maxH = Math.max(ResultsPanel.getPreferredSize().height,
				ImagesPanel.getPreferredSize().height);
		// ResultsPanel.setPreferredSize(new
		// Dimension(ResultsPanel.getPreferredSize().width,maxH));
		ImagesPanel.setPreferredSize(new Dimension(ImagesPanel
				.getPreferredSize().width, maxH));

		resizeImagePanel();

	}

	@Override
	public void addNotify() {
		super.addNotify();
		setSizes();
	}

	private class CountSelectedButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			int[] selectedRows = fileNameTable.getSelectedRows();

			if (selectedRows.length == 0)
				return;

			for (int i : selectedRows) {
				Object o = model.getValueAt(i, 0);
				File f = (File) o;
				results.put(f.toString(), vpcExec(f, currentParameters));
			}

			// Display the data for the first selected row
			Object o = model.getValueAt(selectedRows[0], 0);
			File f = (File) o;
			displayResults(results.get(f.toString()));
		}

	}

	
	private class CountAllButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			for (int i = 0; i < model.getRowCount(); i++) {
				File f = (File) model.getValueAt(i, 0);
				results.put(f.toString(), vpcExec(f,currentParameters));
			}

			int rowIndex = fileNameTable.getSelectedRow();
			if (rowIndex == -1)
				return;
			// Get the result and set it. Null is OK
			File f = (File) model.getValueAt(rowIndex, 0);
			displayResults(results.get(f.toString()));

		}

	}

	private class NewCalibrationButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			VPCParams params = new VPCParameterWizard().run(topFrame);
			if(params == null) {
				return;
			}
			String response = "";
			
			while(response.length() == 0) {
			//Pop up a dialog asking the user to name the parameters
			  response = JOptionPane.showInputDialog(null,
					  "Name this parameter set.",
					  "Enter a name.",
					  JOptionPane.QUESTION_MESSAGE);
			}
			
			
			currentParametersName.setText(response);
			params.setName(response);
			
			currentParameters = params;
			updateUI();
			  
			
		}
		
	}
	
	void updateUI() {
		//update the params label
		currentParametersName.setText( currentParameters != null ? currentParameters.name : "None");
		
		//allow the count buttons and save param button when parameters are chosen
		boolean val = currentParameters != null ? true : false;
		
		countSelectedButton.setEnabled(val);
		//countAllButton.setEnabled(val);
		saveCalibrationButton.setEnabled(val);
		
		saveResultsToFileMenuItem.setEnabled(getOnScreenResults().size() != 0);
		
		
		
	}
	private class SaveCalibrationButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			 JFileChooser jfc = new JFileChooser();
			 File f = new File(currentParameters.name + ".xml");
			 jfc.setSelectedFile(f);
			 int ret = jfc.showSaveDialog(null);
			 if(ret != JFileChooser.APPROVE_OPTION) {
				 return;
			 }
			 
			 try {
				 FileOutputStream fos = new FileOutputStream(jfc.getSelectedFile());
				currentParameters.storeToXml(fos, "This is a parameter set for VPC");
				Utilities.showInfo( "Successfully saved parameters to file.", "");
			} catch (IOException e1) {
				Utilities.showError("Could not open file for writing.");
				return;
			}
		}
	}
	
	private class LoadCalibrationButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			 JFileChooser jfc = new JFileChooser();
			 
			 
			 int ret = jfc.showOpenDialog(null);
			 if(ret != JFileChooser.APPROVE_OPTION) {
				 return;
			 }
			 
			 try {
				 FileInputStream fis = new FileInputStream(jfc.getSelectedFile());
				 currentParameters = VPCParams.loadFromXml(fis);
				 
				 updateUI();
			} catch (IOException e1) {
				Utilities.showError("Could not read file.");
				return;
			}
		}
	}
	
	
	private class FolderButtonHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Choose a folder containing viral plaque images.");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				currentFolderTextField.setText(chooser.getSelectedFile()
						.toString());
				populateTable();
			} else {
				System.out.println("No Selection ");
			}
		}
	}

	private class RowListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int rowIndex = fileNameTable.getSelectedRow();
			if (rowIndex == -1)
				return;

			File f = (File) model.getValueAt(rowIndex, 0);

			setImage(f.toString());

			// Get the result and set it. Null is OK
			displayResults(results.get(f.toString()));
			slider.setValue(0);

		}

	}

	private class VPCResult {
		public Vector<Vector<Integer>> count;
		public String errMsg;
	}

	private HashMap<String, VPCResult> results = new HashMap<String, VPCResult>();

	private VPCResult vpcExec(File f, VPCParams params) {

		File tmpFile = null;
		
		//There seems to be a problem with passing a file that has a space followed by a
		//hyphen when calling command line vpc. Copy the file to a temp file if this is the case.
		if(f.getAbsolutePath().contains(" ")) {
			try {
				tmpFile = File.createTempFile("tmpImage", f.getName().substring(f.getName().lastIndexOf(".")));
				Utilities.copyFile(f, tmpFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Vector<Vector<Integer>> ret = new Vector<Vector<Integer>>();
		VPCResult res = new VPCResult();

		Runtime r = Runtime.getRuntime();
		String imgPath = (tmpFile == null) ? f.getAbsolutePath() : tmpFile.getAbsolutePath();
		
		String launcherPath = this.getClass().getResource("vpc.exe").getPath();
		try {
			String cmd = launcherPath + " " + "--img \""
					+ imgPath + "\" --plateRadius " + params.plateRadius + " --maxPlaqueRadius " + params.maxPlaqueRadius +
					" --minPlaqueRadius " + params.minPlaqueRadius ;
			
			System.out.println(cmd);
			Process p = r.exec(cmd);
			BufferedReader stdout = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			try {
				p.waitFor();
			} catch (InterruptedException e) {
				Utilities.showError("Failed to count plaques for file: " + f + " : " + e);
			}
			
			if(tmpFile != null) {
				tmpFile.delete();
			}

			
			if (p.exitValue() != 0) {
				System.out.println("Bad ret value for " + f);
				System.out.println(p.exitValue());
				res.errMsg = "Could not process image.";

				return res;
			}

			String s = null;
			//process the output
			while ((s = stdout.readLine()) != null) {
				String[] tokens = s.split(", ");
				if (tokens == null || tokens.length == 0
						|| tokens[0].length() == 0) {
					continue;
				}
				Vector<Integer> row = new Vector<Integer>();
				for (String token : tokens) {
					row.add(Integer.parseInt(token));
				}
				ret.add(row);
			}
			System.out.println("Done with " + f);
		} catch (IOException e) {
			Utilities.showError("Internal error: " + e.toString());
		}
		
		
		res.count = ret;
		return res;
	}

	private void configure() {
		// Choose folder action
		chooseFolderButton.addActionListener(new FolderButtonHandler());

		// Count selected action
		countSelectedButton.addActionListener(new CountSelectedButtonHandler());

		// Count all action
		countAllButton.addActionListener(new CountAllButtonHandler());

		// New parameters action
		newCalibrationButton.addActionListener(new NewCalibrationButtonHandler());
		
		// Save params
		saveCalibrationButton.addActionListener(new SaveCalibrationButtonHandler());
		
		//Load params
		loadCalibrationButton.addActionListener(new LoadCalibrationButtonHandler());
	
		// Create table model columns
		model.addColumn("File name");
		fileNameTable.getSelectionModel().addListSelectionListener(
				new RowListener());
		fileNameTable.getColumnModel().getSelectionModel()
				.addListSelectionListener(new RowListener());

	}

	private void populateTable() {
		// Clear rows
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}

		// Repopulate
		File folder = new File(currentFolderTextField.getText());
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			model.addRow(new Object[] { f });
		}

	}

	private class ExceptionCatcher implements Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			PrintStream sw = new PrintStream(buf);
			sw.append("Internal error: " + e + Utilities.newLine);
			sw.append("   File: " + e.getStackTrace()[0].getFileName() + Utilities.newLine);
			sw.append("   Line: " + e.getStackTrace()[0].getLineNumber());
			sw.flush();
			Utilities.showError(buf.toString());
		}

	}

	private void build() {

		this.topFrame = this;
		
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setSizes();
			}
		});

		setJMenuBar(makeMenuBar());

		JPanel outer = new JPanel(new BorderLayout());

		JPanel top = new JPanel(new FlowLayout(5));
		JPanel bottom = new JPanel(new BorderLayout());

		Utilities.standardBorder(CountPlaquesPanel, "1) Select Directory");
		Utilities.standardBorder(CalibrationsPanel, "2) Calibrate");
		Utilities.standardBorder(ImagesPanel, "3) Select Images");
		Utilities.standardBorder(ResultsPanel, "4) Count");

		// Build the count Count Plaques panel
		DesignGridLayout l = new DesignGridLayout(CountPlaquesPanel);
		l.emptyRow();
		l.row().bar().withOwnRowWidth().left(chooseFolderButton);
		l.emptyRow();
		currentFolderTextField.setColumns(20);
		currentFolderTextField.setEditable(false);
		JPanel flowPanel = new JPanel();
		flowPanel.add(Utilities.standardLabel("Selected Folder:"));
		flowPanel.add(currentFolderTextField);
		l.row().left().add(flowPanel);

		// Build the Calibrations panel
		l = new DesignGridLayout(CalibrationsPanel);
		l.emptyRow();
		l.row()
				.bar()
				.withOwnRowWidth()
				.left(newCalibrationButton, loadCalibrationButton,
						saveCalibrationButton);
		l.row().left().add(Utilities.standardLabel("Currently Loaded Parameters:")).add(currentParametersName);

		// Build the Images panel
		ImagesPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(fileNameTable);
		fileNameTable.setFillsViewportHeight(true);
		ImagesPanel.add(scrollPane, BorderLayout.CENTER);

		// Build the Results Panel
		ResultsPanel.setLayout(new BorderLayout());
		JPanel tmpPanel = new JPanel();
		l = new DesignGridLayout(tmpPanel);
		l.row().left().add(countSelectedButton);

		slider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				int val = slider.getValue();
				imagePanel.setScale(imagePanel.getDefaultScale() + val / 5.0);
			}
		});
		l.row().left().withOwnRowWidth().add(slider);
		imageScroller = new JScrollPane(imagePanel, 
				                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		imageScroller.setPreferredSize(new Dimension(610, 400));
		imageScroller.setWheelScrollingEnabled(false);
		l.row().left().withOwnRowWidth().add(imageScroller);
		for (int i = 0; i < 6; i++) {
			JLabel tmp = new JLabel();
			tmp.setFont(Defaults.RESULTS_FONT);
			resultsLabels.add(tmp);
			l.row().left().withOwnRowWidth().add(resultsLabels.get(i));
		}
		ResultsPanel.add(tmpPanel, BorderLayout.NORTH);

		top.add(CountPlaquesPanel);
		top.add(CalibrationsPanel);
		bottom.add(ImagesPanel, BorderLayout.WEST);
		bottom.add(ResultsPanel, BorderLayout.CENTER);
		outer.add(top, BorderLayout.NORTH);
		outer.add(bottom, BorderLayout.CENTER);
		this.add(outer);

		this.pack();
		updateUI();
	}

	public HashMap<String, VPCResult> getOnScreenResults() {
		HashMap<String, VPCResult> ret = new HashMap<String, VPCResult>();
		for (int i = 0; i < model.getRowCount(); i++) {
			File f = (File) model.getValueAt(i, 0);
			if(results.containsKey(f.toString())) {
				ret.put(f.toString(), results.get(f.toString()));
			}
		}

		return ret;
	}
	
	public void displayResults(VPCResult res) {

		for (int i = 0; i < resultsLabels.size(); i++)
			resultsLabels.get(i).setText("");

		resultsLabels.get(0).setText("Image not yet processed.");

		if (res == null) {
			return;
		}
		if (res.count == null) {
			resultsLabels.get(0).setText(res.errMsg);
			return;
		}
		Vector<Vector<Integer>> cnt = res.count;

		int i = 0;
		for (Vector<Integer> v : cnt) {
			String lbl = new String();
			for (Integer num : v) {
				lbl = lbl + String.format("%5d", num) + " ";
			}
			resultsLabels.get(i++).setText(lbl);

		}

	}

	public void resizeImagePanel() {

		imageScroller.invalidate();

	}

	public BufferedImage img;

	public void setImage(String path) {

		imagePanel.setImage(path);

	}

	public VpcGui() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set the exception handler for the GUI
		if (SwingUtilities.isEventDispatchThread()) {
			Thread.setDefaultUncaughtExceptionHandler(new ExceptionCatcher());
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						Thread.setDefaultUncaughtExceptionHandler(new ExceptionCatcher());
					}

				});
			} catch (Exception e) {
				Utilities.showError("Internal error: " + e);
			}
		}
		build();
		configure();
		setVisible(true);

	}



}
