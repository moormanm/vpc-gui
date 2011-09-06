import java.awt.List;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import net.java.dev.designgridlayout.*;

public class VpcGui extends JFrame {

	private JButton chooseFolderButton = new JButton("Choose Folder");
	private JButton countAllButton = new JButton("Count All");
	private JButton countSelectedButton = new JButton("Count Selected");
	private JLabel  currentFolderLabel = new JLabel();
	private JButton stopButton = new JButton("Stop");
	private JTable fileNameTable = new JTable();
	private JButton newCalibrationButton = new JButton("New");
	private JButton loadCalibrationButton = new JButton("Load");
	private JButton saveCalibrationButton = new JButton("Save");
	private void standardBorder(JPanel jp, String name) {
		jp.setBorder(BorderFactory.createTitledBorder(
		        BorderFactory.createEtchedBorder(), name, TitledBorder.LEFT,
		        TitledBorder.TOP, Defaults.TITLE_FONT));
		
	}
	private void build() {
		JPanel top = new JPanel();
		DesignGridLayout layout = new DesignGridLayout(top);
		JPanel CountPlaquesPanel = new JPanel();
		JPanel CalibrationsPanel = new JPanel();
		JPanel ImagesPanel = new JPanel();
		JPanel ResultsPanel = new JPanel();
		
		standardBorder(CalibrationsPanel, "Calibrations");
		standardBorder(ImagesPanel, "Images");
		standardBorder(ResultsPanel, "Results");
		
		//Build the count Count Plaques panel
		standardBorder(CountPlaquesPanel, "Count Plaques");
		DesignGridLayout l2 = new DesignGridLayout(CountPlaquesPanel);
		
		

		
		
		layout.row().grid().add(CountPlaquesPanel).add(CalibrationsPanel);
		layout.row().grid().add(ImagesPanel).add(ResultsPanel);
		
		
		
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
