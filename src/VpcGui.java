import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import net.java.dev.designgridlayout.*;

public class VpcGui extends JFrame {
	public static void main(String[] args) {
		VpcGui g = new VpcGui();
	}

	
	private JButton chooseFolderButton = new JButton("Choose Folder");
	private JButton countAllButton = new JButton("Count All");
	private JButton countSelectedButton = new JButton("Count Selected");
	private JTextField  currentFolderTextField = new JTextField();
	private JButton stopButton = new JButton("Stop");
	private DefaultTableModel model = new DefaultTableModel();
	private JTable fileNameTable = new JTable(model) {
		   public boolean isCellEditable(int rowIndex, int colIndex) {
		        return false;   //Disallow the editing of any cell
		    }
	};
	private JSlider slider = new JSlider(0,5);
	private JButton newCalibrationButton = new JButton("New");
	private JButton loadCalibrationButton = new JButton("Load");
	private JButton saveCalibrationButton = new JButton("Save");
	private JLabel  rowsCalibrationLabel = new JLabel();
	private JLabel  columnsCalibrationLabel = new JLabel();
	private JLabel  apdCalibrationLabel = new JLabel();
	private JLabel  pdCalibrationLabel = new JLabel();
	private ImagePanel imagePanel = new ImagePanel();
	private Vector<JLabel>  resultsLabels = new Vector<JLabel>();
	private JPanel CountPlaquesPanel = new JPanel();
	private JPanel CalibrationsPanel = new JPanel();
	private JPanel ImagesPanel = new JPanel();
	private JScrollPane imageScroller = new JScrollPane();
	private JPanel ResultsPanel = new JPanel();


	private void standardBorder(JPanel jp, String name) {
		jp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), name, TitledBorder.LEFT,
				TitledBorder.TOP, Defaults.TITLE_FONT));

	}

	private JLabel standardLabel(String name) {
		JLabel tmp = new JLabel(name);
		tmp.setFont(Defaults.LABEL_FONT);
		return tmp;
	}

	private JMenuBar makeMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu viewMenu = new JMenu("View");
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);
		return menuBar;
	}


	
	
	private void setSizes() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle r = graphicsEnvironment.getMaximumWindowBounds();
		setMaximizedBounds(r);
		Dimension windowSize = new Dimension(r.width, r.height);
		this.setPreferredSize(windowSize);

		//Make Calibration panel and CountPlaquesPanel the same size
		int maxH = Math.max(CalibrationsPanel.getPreferredSize().height,  CountPlaquesPanel.getPreferredSize().height);
		CalibrationsPanel.setPreferredSize(new Dimension(CalibrationsPanel.getPreferredSize().width, maxH));
		CountPlaquesPanel.setPreferredSize(new Dimension(CountPlaquesPanel.getPreferredSize().width, maxH));



		//Make the Results and the Images Panel the same size
		maxH = Math.max(ResultsPanel.getPreferredSize().height,  ImagesPanel.getPreferredSize().height);
		//  ResultsPanel.setPreferredSize(new Dimension(ResultsPanel.getPreferredSize().width,maxH));
		ImagesPanel.setPreferredSize(new Dimension(ImagesPanel.getPreferredSize().width,maxH));

		resizeImagePanel();

	}

	@Override
	public void addNotify(){
		super.addNotify();
		setSizes();
	}

	private class CountSelectedButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			int[] selectedRows = fileNameTable.getSelectedRows();
			
			
			if(selectedRows.length == 0) return;

			for(int i : selectedRows) {
				Object o = model.getValueAt(i,0);
				File f = (File)o;
				results.put(f.toString(), vpcExec(new VPCParams(0,0,0,0,f)));
			}

			//Display the data for the first selected row
			Object o = model.getValueAt(selectedRows[0],0);
			File f = (File)o;
			displayResults(results.get(f.toString()));
		}
		
	}
	private class CountAllButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			
			for (int i =0; i < model.getRowCount(); i++) {
				File f = (File)model.getValueAt(i,0);
				String errmsg = new String();
				results.put(f.toString(), vpcExec(new VPCParams(0,0,0,0,f)));	
			}
			
			
			int rowIndex = fileNameTable.getSelectedRow();
			if(rowIndex == -1) return;
			//Get the result and set it. Null is OK
			File f = (File)model.getValueAt(rowIndex,0);
			displayResults(results.get(f.toString()));
			
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
				currentFolderTextField.setText(chooser.getSelectedFile().toString());
				populateTable();
			}
			else {
				System.out.println("No Selection ");
			}
		}
	}

	private class RowListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) {
				return;
			}
			int first=0, last;

			int rowIndex = fileNameTable.getSelectedRow();
			if(rowIndex == -1) return;

			File f = (File)model.getValueAt(rowIndex,0);

			setImage(f.toString());
			
			//Get the result and set it. Null is OK
			displayResults(results.get(f.toString()));
			slider.setValue(0);
		
		
			
		}

	}

	
	private class VPCParams {
		public final int rows;
		public final int cols;
		public final int plateDiameter;
		public final int maxPlaqueDiameter;
		public final File imageFile;
		VPCParams(int rows, int cols, int plateDiameter, int maxPlaqueDiameter, File imageFile) {
			this.rows = rows;
			this.cols = cols;
			this.plateDiameter = plateDiameter;
			this.maxPlaqueDiameter = maxPlaqueDiameter;
			this.imageFile = imageFile;
			
		}
		
	}
	
	private class VPCResult {
		public Vector<Vector<Integer>> count;
		public String errMsg;
	}
	private HashMap<String, VPCResult> results = new HashMap<String, VPCResult>();
	
	private VPCResult vpcExec(VPCParams params) {
		
		Vector<Vector<Integer>> ret = new Vector<Vector<Integer>>();
		VPCResult res =  new VPCResult();
		
		Runtime r = Runtime.getRuntime();
		String launcherPath = this.getClass().getResource("vpc.exe").getPath();
		try {
			String cmd = launcherPath + " " + "\"" + params.imageFile.getAbsolutePath() + "\"";

			Process p = r.exec( cmd);
			  BufferedReader stdout = new BufferedReader(new 
		                 InputStreamReader(p.getInputStream()));

			  try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			  

			  if(p.exitValue() != 0) {
				  System.out.println("Bad ret value for " + params.imageFile);
				  System.out.println(cmd);
				  res.errMsg = "Could not process image.";
				  return res;
			  }

			  String s = null;
			  while ((s = stdout.readLine()) != null) {
	                String[] tokens = s.split(", ");
	                if(tokens == null || tokens.length == 0 || tokens[0].length() == 0) { 
	                	continue;
	                }
	                Vector<Integer> row = new Vector<Integer>();
	                for (String token : tokens) {
	                	row.add(Integer.parseInt(token));
	                }
	                ret.add(row);
	            }  
			  System.out.println("Done with " + params.imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		res.count = ret;
		return res;
	}
	

	
	private void configure() {
		//Choose folder action
		chooseFolderButton.addActionListener(new FolderButtonHandler());

		//Count selected action
		countSelectedButton.addActionListener(new CountSelectedButtonHandler());
		
		//Count all action
		countAllButton.addActionListener(new CountAllButtonHandler());
		
		// Create table model columns
		model.addColumn("File name");
		fileNameTable.getSelectionModel().addListSelectionListener(new RowListener());
		fileNameTable.getColumnModel().getSelectionModel().addListSelectionListener(new RowListener());
	


		
		


	}

	private void populateTable() {
		//Clear rows
		while (model.getRowCount()>0){
			model.removeRow(0);
		}

		//Repopulate
		File folder = new File(currentFolderTextField.getText());
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			model.addRow(new Object[]{f});
		}

	}

	private void build() {

		this.addComponentListener(new java.awt.event.ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				setSizes();
			}
		});

		setJMenuBar(makeMenuBar());

		JPanel outer = new JPanel( new BorderLayout());

		JPanel top = new JPanel(new FlowLayout(5));
		JPanel bottom = new JPanel(new BorderLayout());


		standardBorder(CountPlaquesPanel, "1) Select Directory");
		standardBorder(CalibrationsPanel, "2) Calibrate");
		standardBorder(ImagesPanel, "3) Select Images");
		standardBorder(ResultsPanel, "4) Count");
		

		//Build the count Count Plaques panel
		DesignGridLayout l = new DesignGridLayout(CountPlaquesPanel);
		l.emptyRow();
		l.row().bar().withOwnRowWidth().left(chooseFolderButton);
		l.emptyRow();
		currentFolderTextField.setColumns(50);
		currentFolderTextField.setEditable(false);
		JPanel flowPanel = new JPanel();
		flowPanel.add(standardLabel("Selected Folder:"));
		flowPanel.add(currentFolderTextField);
		l.row().left().add(flowPanel);

		//Build the Calibrations panel
		l = new DesignGridLayout(CalibrationsPanel);
		l.emptyRow();
		l.row().bar().withOwnRowWidth().center(newCalibrationButton, loadCalibrationButton, saveCalibrationButton);
		l.row().left().add(standardLabel("Rows:"), rowsCalibrationLabel);
		l.row().left().add(standardLabel("Columns:"), columnsCalibrationLabel);
		l.row().left().add(standardLabel("Average Plaque Diameter:"), apdCalibrationLabel);
		l.row().left().add(standardLabel("Plate Diameter:"), pdCalibrationLabel);


		//Build the Images panel 
		ImagesPanel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(fileNameTable);
		fileNameTable.setFillsViewportHeight(true);
		ImagesPanel.add(scrollPane, BorderLayout.CENTER);



		//Build the Results Panel
		ResultsPanel.setLayout(new BorderLayout());
		JPanel tmpPanel = new JPanel();
		l = new DesignGridLayout(tmpPanel);
		l.row().left().add(countAllButton).add(countSelectedButton).add(stopButton);

           
        slider.addChangeListener(new ChangeListener()  
        {  
        
            public void stateChanged(ChangeEvent e)  
            {  
            	
                int val = slider.getValue();
                imagePanel.setScale( imagePanel.getDefaultScale() + val/5.0 );
            }  
        });  
        l.row().left().withOwnRowWidth().add(slider);
		imageScroller = new JScrollPane(imagePanel);
		imageScroller.setPreferredSize(new Dimension(610,400));
		imageScroller.setWheelScrollingEnabled(false);
		l.row().left().withOwnRowWidth().add(imageScroller);
		for (int i=0; i < 6; i++) {
			JLabel tmp = new JLabel();
			tmp.setFont(Defaults.RESULTS_FONT);
			resultsLabels.add(tmp);
			l.row().left().withOwnRowWidth().add(resultsLabels.get(i));
		}
		ResultsPanel.add(tmpPanel, BorderLayout.NORTH);
       

		top.add(CountPlaquesPanel); top.add(CalibrationsPanel);
		bottom.add(ImagesPanel, BorderLayout.WEST); bottom.add(ResultsPanel, BorderLayout.CENTER);
		outer.add(top, BorderLayout.NORTH); outer.add(bottom, BorderLayout.CENTER);
		this.add(outer);
		
		


		this.pack();
	}

	public void displayResults(VPCResult  res) {

		
		
		for(int i=0; i<resultsLabels.size(); i++) resultsLabels.get(i).setText("");
		
		resultsLabels.get(0).setText("Image not yet processed.");

		if(res == null) {
			return;
		}
		if(res.count == null) {
			resultsLabels.get(0).setText(res.errMsg);
			return;
		}
		Vector<Vector<Integer>> cnt = res.count;
		
		int i =0;
		for ( Vector<Integer> v : cnt) {
			String lbl = new String();
			for ( Integer num : v) {
				lbl = lbl + String.format("%5d",num) + " ";
			}
		    resultsLabels.get(i++).setText(lbl);
		    
		}
	
	}
	
	public void resizeImagePanel() {

		imageScroller.invalidate();

		
	
		
	}
	public BufferedImage img;
	public void setImage(String path){

		imagePanel.setImage(path);


	}

	public VpcGui() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		build();
		configure();
		setVisible(true);

	}

	
	private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height){
		int type=BufferedImage.TYPE_INT_RGB;
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		

		return resizedImage;
	}



	/**
	 * @param args
	 */

}
