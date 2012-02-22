import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import ch.rakudave.suggest.JSuggestField;

public class RecipeClassifier extends JFrame {
	private static HashSet<String> ingredientHash = new HashSet<String>();
	private static HashMap<String, Recipe> recipeHash = new HashMap<String, Recipe>();
	private static Vector<String> ingredients = getIngredients();
	private static Vector<String> getIngredients() {
		InputStream is = RecipeClassifier.class
				.getResourceAsStream("/ingredients");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			Vector<String> ret = new Vector<String>();
			String line;
			while ((line = in.readLine()) != null) {
				if(!ingredientHash.contains(line.toLowerCase())) {
					ret.add(line.toLowerCase());
					ingredientHash.add(line.toLowerCase());
				}
				
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
		InputStream is = RecipeClassifier.class.getResourceAsStream("/recipes");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			Vector<Recipe> ret = new Vector<Recipe>();
			String line;
			while ((line = in.readLine()) != null) {
				Recipe r = new Recipe();
				String[] fields = line.split("\\|");

				r.name = fields[0];
				for (int i = 1; i < fields.length; i++) {
					r.ingredients.add(fields[i].toLowerCase());
				}
			    if(!recipeHash.containsKey(r.name)) {
				  recipeHash.put(r.name,r);
            	  ret.add(r);
			    }
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

	MyTableModel leftTableModel = new MyTableModel(null,
			new String[] { "Ingredients" });
	MyTableModel rightBottomTableModel = new MyTableModel(null,
			new String[] { "Ingredients" });
	MyTableModel rightTopTableModel = new MyTableModel(null,
			new String[] { "Like These Recipes" });


	JTable leftTable = new JTable(leftTableModel);
	JTable rightTopTable = new JTable(rightTopTableModel);
	JTable rightBottomTable = new JTable(rightBottomTableModel);
	JButton addButton = new JButton("Add");
	JButton classifyButton = new JButton("Classify");
	JButton removeButton = new JButton("Remove");
	JButton resetButton = new JButton("Reset");
	JLabel resultLabel = new JLabel("Result:");

	public void addAction() {
		if (!ingredientHash.contains(ingredEntry.getText())) {

			return;
		}

		Object[] data = new Object[1];
		data[0] = ingredEntry.getText();
		leftTableModel.insertRow(leftTableModel.getRowCount(), data);
		ingredEntry.setText("");
	}
	public RecipeClassifier() {

		GridLayout myLayout = new GridLayout(1, 2);
		myLayout.setHgap(40);
		myLayout.setVgap(20);

		JPanel myPanel = new JPanel(myLayout);

		ingredEntry = new JSuggestField(this, ingredients);
		ingredEntry.setPreferredSize(new Dimension(300, 25));

		leftTable.setFillsViewportHeight(true);
		rightTopTable.setFillsViewportHeight(true);
		rightBottomTable.setFillsViewportHeight(true);
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
		controlsAndTableArea.add(new JScrollPane(leftTable),
				BorderLayout.CENTER);
		leftArea.add(controlsAndTableArea, BorderLayout.CENTER);

		JPanel classifyArea = new JPanel();
		classifyArea.add(classifyButton);

		rightArea.add(classifyArea, BorderLayout.NORTH);

		JPanel resultAndTableArea = new JPanel(new GridLayout(2,1));
		resultAndTableArea.add(new JScrollPane(rightTopTable));
		resultAndTableArea.add(new JScrollPane(rightBottomTable));

		rightArea.add(resultAndTableArea);

		myPanel.add(leftArea);
		myPanel.add(rightArea);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(myPanel, BorderLayout.CENTER);

		add(topPanel);

		setPreferredSize(new Dimension(800, 600));

		// Set up action handlers
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
              addAction();
			}
		});
		ingredEntry.addKeyListener( new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					addAction();
				}
			}
		});;
		addButton.addKeyListener( new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					addAction();
				}
			}
		});

		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ingredEntry.setText("");
				while (leftTableModel.getRowCount() > 0) {
					leftTableModel.removeRow(0);
				}
				while (rightBottomTableModel.getRowCount() > 0) {
					rightBottomTableModel.removeRow(0);
				}
				while (rightTopTableModel.getRowCount() > 0) {
					rightTopTableModel.removeRow(0);
				}
				resultLabel.setText("Result:");
			}
		});

		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selRow = leftTable.getSelectedRow();
				System.out.println(selRow);
				if (selRow == -1)
					return;
				leftTableModel.removeRow(selRow);
			} 
		});

		classifyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (leftTableModel.getRowCount() == 0)
					return;

				// Clear the results area
				while (rightBottomTableModel.getRowCount() > 0) {
					rightBottomTableModel.removeRow(0);
				}
				while (rightTopTableModel.getRowCount() > 0) {
					rightTopTableModel.removeRow(0);
				}

				// Make a hashset using the left table data. This is A.
				HashSet<String> a = new HashSet<String>();
				int rowCount = leftTableModel.getRowCount();

				for (int i = 0; i < rowCount; i++) {
					a.add((String) leftTableModel.getValueAt(i, 0));
				}

				class Tuple implements Comparable<Tuple> {
					final Recipe r;
					final Double score;

					Tuple(Recipe r, Double score) {
						this.r = r;
						this.score = score;
					}

					@Override
					public int compareTo(Tuple other) {
						return score.compareTo(other.score);
					}
				}

				LinkedList<Tuple> results = new LinkedList<Tuple>();

				// Run through and calculate the likeness for each recipe
				for (Recipe r : recipes) {
					Tuple t = new Tuple(r, calculateLikeness(a, r.ingredients));
					results.add(t);
				}

				if (results.size() == 0) {
					JOptionPane.showMessageDialog(null,
							"Could not find anything like this", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				//Sort descending
				Collections.sort(results);
				Collections.reverse(results);

				
				
				int maxResults = 20;
				int i =0;
				for (Tuple t : results) {
					if (t.score <= 0)
						continue;
					i++;
					if(i>maxResults) break;
					System.out.println("Likeness for " + t.r.name + " is " + calculateLikeness(a, t.r.ingredients));

					Object[] data = new Object[1];
					data[0] = t.r.name;
					rightTopTableModel.insertRow(
							rightTopTableModel.getRowCount(), data);
				}
			}
		});

		rightTopTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				while (rightBottomTableModel.getRowCount() > 0) {
					rightBottomTableModel.removeRow(0);
				}
				int rowIndex = rightTopTable.getSelectedRow(); if (rowIndex
				 == -1) return;
				String selectedRecipe = (String)rightTopTableModel.getValueAt(rowIndex,0);
				
				Recipe r = recipeHash.get(selectedRecipe);
				for(String ing : r.ingredients) {
					Object[] data = new Object[1];
					data[0] = ing;
					rightBottomTableModel.insertRow(rightBottomTableModel.getRowCount(), data);
				}
				

			}
		});

	}

	public static void createAndShowGUI() {
		
		// Create and set up the window.
		JFrame frame = new RecipeClassifier();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frame.pack();
		frame.setVisible(true);

	}

	public static void main(String[] args) {

		// TODO Auto-generated method stub
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

	}
	
	HashMap<String,Integer> ingrGram = new HashMap<String,Integer>();
	{
	  for(Recipe r : recipes) {
		  for(String ing : r.ingredients) {
			  if (ingrGram.containsKey(ing) ) {
				  Integer val = ingrGram.get(ing);
				  val++;
				  ingrGram.put(ing,val);
			  }
			  else {
				  ingrGram.put(ing, 1);
			  }
		  }
	  }
	  
	  class Tuple implements Comparable<Tuple>{
		final String ing;
		final Integer count;
		Tuple(String ing, Integer count) {
			this.ing = ing; this.count = count;
		}
		@Override
		public int compareTo(Tuple arg0) {
			// TODO Auto-generated method stub
			return count.compareTo(arg0.count);
		}
	  }
	  
	  
	  LinkedList<Tuple> list = new LinkedList<Tuple>();
	  for(String s : ingrGram.keySet()) {
		  Tuple t = new Tuple(s, ingrGram.get(s));
		  list.add(t);
	  }
	  Collections.sort(list);
	  Collections.reverse(list);
	  
	  for(Tuple t: list) {
		System.out.println(t.ing + " : " + t.count);  
      }
	  
	  for(Recipe r : recipes) {
		  for(String i : r.ingredients) {
			  if(!ingredientHash.contains(i)) {
				  System.out.println(i);
				  ingredientHash.add(i);
			  }
		  }
	  }
 		
	  
	}

	public double calculateLikeness(HashSet<String> a, HashSet<String> b) {
		double c = 0;
		// Iterate over the common items between A and B
		// Tally scores for the commonalities.. deduct a minimum value for the
		// items that aren't common
		for (String aItem : a) {
			if (b.contains(aItem)) {
				//weight the score based on the ingredient's distinctiveness
				double score = 1.0 / ingrGram.get(aItem);
				c += score;
				c += 1.0;
			}
			else {
				//deduct the minimum score
				c -= .000000000001;
			}
		}
		
		//Get the items that are present in B but not in A
		for (String bItem : b) {
			if (!a.contains(bItem)) {
				//deduct the minimum score..
				double tmp = c;
				c -= .000000000001;
				if(c == tmp) {System.out.println("WTF!!");}
			}
		}

		return c;

	}

}
