import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import net.java.dev.designgridlayout.*;

public class VpcGui extends JFrame {

	private JButton chooseFolderButton = new JButton("Choose Folder");
	private JButton countAllButton = new JButton("Count All");
	private JButton countSelectedButton = new JButton("Count Selected");
	private JTextField  currentFolderTextField = new JTextField();
	private JButton stopButton = new JButton("Stop");
	private JTable fileNameTable = new JTable();
	private JButton newCalibrationButton = new JButton("New");
	private JButton loadCalibrationButton = new JButton("Load");
	private JButton saveCalibrationButton = new JButton("Save");
	private JLabel  rowsCalibrationLabel = new JLabel();
	private JLabel  columnsCalibrationLabel = new JLabel();
	private JLabel  apdCalibrationLabel = new JLabel();
	private JLabel  pdCalibrationLabel = new JLabel();
	private ImagePanel imageViewer = new ImagePanel();
	
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
	
	class ImagePanel extends JPanel {
		private String path;
		private Image img;

		public void set(String imgPath) throws IOException {
			System.out.println(imgPath);
			img = ImageIO.read(new File(imgPath));
			
			
		}
		public void unset() {
			img = null;
		}
		

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (img != null) g.drawImage(img,0,0, getPreferredSize().width, getPreferredSize().height, null);
		}
	}
	
	private void build() {

		
		setJMenuBar(makeMenuBar());
		
		JPanel top = new JPanel();
		DesignGridLayout layout = new DesignGridLayout(top);
		JPanel CountPlaquesPanel = new JPanel();
		JPanel CalibrationsPanel = new JPanel();
		JPanel ImagesPanel = new JPanel();
		JPanel ResultsPanel = new JPanel();
		
		standardBorder(CalibrationsPanel, "Calibrations");
		standardBorder(ImagesPanel, "Images");
		standardBorder(ResultsPanel, "Results");
		standardBorder(CountPlaquesPanel, "Count Plaques");
		
		//Build the count Count Plaques panel
		DesignGridLayout l = new DesignGridLayout(CountPlaquesPanel);
		l.emptyRow();
		l.row().left().add(chooseFolderButton);
		l.emptyRow();
		l.row().left().add(standardLabel("Selected Folder:")).fill().add(currentFolderTextField);
		l.emptyRow();
	    l.row().left().add(countAllButton, countSelectedButton, stopButton);
		l.emptyRow();
		l.emptyRow();
		
	    //Build the Calibrations panel
	    l = new DesignGridLayout(CalibrationsPanel);
	    l.emptyRow();
	    l.row().bar().withOwnRowWidth().center(newCalibrationButton, loadCalibrationButton, saveCalibrationButton);
	    l.row().left().add(standardLabel("Rows:"), rowsCalibrationLabel);
	    l.row().left().add(standardLabel("Columns:"), columnsCalibrationLabel);
	    l.row().left().add(standardLabel("Average Plaque Diameter:"), apdCalibrationLabel);
	    l.row().left().add(standardLabel("Plate Diameter:"), pdCalibrationLabel);
	    
	    
	    //Build the Images panel 
	    l = new DesignGridLayout(ImagesPanel);
	    l.emptyRow();
	    JScrollPane scrollPane = new JScrollPane(fileNameTable);
	    fileNameTable.setFillsViewportHeight(true);
	    scrollPane.setPreferredSize(new Dimension(300,scrollPane.getPreferredSize().height));
	    l.row().left().add(scrollPane);
	    
	    
	    //Build the Results Panel
	    l = new DesignGridLayout(ResultsPanel);
	    l.emptyRow();
	    l.row().left().add(imageViewer);
	    try {
			imageViewer.set("C:\\61.jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    
	    
		
		
		layout.row().grid().add(CountPlaquesPanel).add(CalibrationsPanel);
		layout.row().left().add(ImagesPanel).fill().add(ResultsPanel);
		
				
		
		this.add(top);
		this.pack();
	}

	public VpcGui() {
		build();
		setVisible(true);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VpcGui g = new VpcGui();
	}

}
