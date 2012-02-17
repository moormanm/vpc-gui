import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import ch.rakudave.suggest.JSuggestField;
public class RecipeClassifier extends JFrame {

	
	private Vector<String> ingredients = getIngredients();
	private static Vector<String> getIngredients() {
		InputStream is = RecipeClassifier.class.getResourceAsStream(
        "/ingredients");
		try {
		  BufferedReader in = new BufferedReader(new InputStreamReader(is));
          Vector<String> ret = new Vector<String>();
          String line;
	      while((line = in.readLine()) != null) { 
	        	ret.add(line.toLowerCase());	        	
	      }
	      return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
	static public class Recipe {
		String name;
		HashSet<String> ingredients = new HashSet<String>();
	}
	private Vector<Recipe> recipes = getRecipes();
	private static Vector<Recipe> getRecipes() {
		InputStream is = RecipeClassifier.class.getResourceAsStream(
        "/recipes");
		try {
		  BufferedReader in = new BufferedReader(new InputStreamReader(is));
          Vector<Recipe> ret = new Vector<Recipe>();
          String line;
          while((line = in.readLine()) != null) {
	    	    Recipe r = new Recipe();
	        	String[] fields = line.split("\\|");
	        	r.name = fields[0];
	        	for(int i=1; i< fields.length; i++) {
	        		r.ingredients.add(fields[i].toLowerCase());
	        	}
	        	ret.add(r);
	        	
	      }
	      return ret;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param args
	 */
	JSuggestField ingredEntry;
	class MyTableModel extends DefaultTableModel {
		public MyTableModel(Object[][] data, Object[] names) {
			super(data, names);
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
	MyTableModel leftTableModel = new MyTableModel( null, new String[]{"Ingredients"});
	MyTableModel rightTableModel = new MyTableModel( null, new String[]{"Ingredients"});
	
	HashSet<String> ingredientHash = new HashSet<String>(ingredients); 


	JTable leftTable = new JTable(leftTableModel);
	JTable rightTable = new JTable(rightTableModel);
	JButton addButton = new JButton("Add");
	JButton classifyButton = new JButton("Classify");
	JButton removeButton = new JButton("Remove");
	JButton resetButton = new JButton("Reset");
	JLabel resultLabel = new JLabel("Result:");
	public RecipeClassifier() {
		
		
		GridLayout myLayout = new GridLayout(1,2);
		myLayout.setHgap(40);
		myLayout.setVgap(20);
		
		JPanel myPanel = new JPanel(myLayout);
		
		ingredEntry = new JSuggestField(this, ingredients);
		ingredEntry.setPreferredSize(new Dimension(300,25));
		
		leftTable.setFillsViewportHeight(true);
		rightTable.setFillsViewportHeight(true);
		JPanel leftArea = new JPanel();
		JPanel rightArea = new JPanel();
		
		leftArea.setLayout(new BorderLayout());
		rightArea.setLayout(new BorderLayout());
		
		JPanel entryArea = new JPanel();
		entryArea.add(ingredEntry);
		entryArea.add(addButton);
		
		
		leftArea.add(entryArea, BorderLayout.NORTH);
		
		JPanel controlsAndTableArea = new JPanel(new BorderLayout());
		JPanel controlsArea = new JPanel();
		controlsArea.add(removeButton);
		controlsArea.add(resetButton);
		
		
		controlsAndTableArea.add(controlsArea, BorderLayout.NORTH);
		controlsAndTableArea.add(new JScrollPane(leftTable), BorderLayout.CENTER);
		leftArea.add(controlsAndTableArea, BorderLayout.CENTER);
		
		JPanel classifyArea = new JPanel();
		classifyArea.add(classifyButton);
		
		
		rightArea.add(classifyArea, BorderLayout.NORTH);
		
		JPanel resultArea = new JPanel();
		JPanel resultAndTableArea = new JPanel(new BorderLayout());
		resultArea.add(resultLabel);
		resultAndTableArea.add(resultArea, BorderLayout.NORTH);
		resultAndTableArea.add(new JScrollPane(rightTable), BorderLayout.CENTER);

		rightArea.add(resultAndTableArea);
		
		
		myPanel.add(leftArea); myPanel.add(rightArea);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(myPanel, BorderLayout.CENTER);
		
		add(topPanel);
		
		setPreferredSize(new Dimension(800,600));
		

		//Set up action handlers
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if( ! ingredientHash.contains( ingredEntry.getText())) {
					JOptionPane.showMessageDialog(null, "Use a valid ingredient", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Object[] data = new Object[1];
				data[0] = ingredEntry.getText() ;
				leftTableModel.insertRow(leftTableModel.getRowCount(), data);
				ingredEntry.setText("");
			}
		});

		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ingredEntry.setText("");
				while(leftTableModel.getRowCount() > 0) {
				  leftTableModel.removeRow(0);
				}
				while(rightTableModel.getRowCount() > 0) {
					  rightTableModel.removeRow(0);
				}
				resultLabel.setText("Result:");
			}
		});
		
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selRow = leftTable.getSelectedRow();
				System.out.println(selRow);
				if(selRow == -1) return;
				leftTableModel.removeRow(selRow);
			}
		});
		
		classifyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
			  if(leftTableModel.getRowCount() == 0) return;

			  //Clear the results area
			  while(rightTableModel.getRowCount() > 0) {
					  rightTableModel.removeRow(0);
			  }
			  resultLabel.setText("Result:");
			  
			  //Make a hashset using the left table data. This is A.
			  HashSet<String> a = new HashSet<String>();
			  int rowCount = leftTableModel.getRowCount();
			  
			  for(int i=0; i < rowCount; i++) {
				  a.add((String)leftTableModel.getValueAt(i,0));
			  }
			  
			  
			  //For each recipe in our database, compare
			  double bestScore = 0;
			  Recipe bestMatch = null;
			  for(Recipe r: recipes) {
				  double tmp = calculateLikeness(a, r.ingredients); 
				  if(tmp > bestScore) {
					  bestScore = tmp;
					  bestMatch = r;
				  }
			  }
			  
			  if(bestMatch == null) {
				  JOptionPane.showMessageDialog(null, "Could not find anything like this", "Error", JOptionPane.ERROR_MESSAGE);
				  return;
			  }
			  
			  resultLabel.setText("Result: " + bestMatch.name);
			  for(String s : bestMatch.ingredients) {
    				Object[] data = new Object[1];
					data[0] = s;
					rightTableModel.insertRow(rightTableModel.getRowCount(), data);
	    		
			  }
			  
			  
				
			}
			
		});
	}
	
	
	public static void createAndShowGUI() {
		System.out.println("ASDF");
        //Create and set up the window.
        JFrame frame = new RecipeClassifier();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);

		
	}
	
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
	}
	
	
	public double calculateLikeness(HashSet<String> a, HashSet<String> b) {
		int c = 0;
		System.out.println(b);
		//Count the commonalities of A and B
	    for(String aItem : a) {
		   if(b.contains(aItem)) {
			   c++;
		   }
	    }
	    
	    //Divide the number of commonalities by the number of items in b
	    double score = (double)c ;
	    
	    return score;
	    
	}
		

}
