import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;

@SuppressWarnings("serial")
public class Motivation extends JFrame {

	public static HashMap<JRadioButton, ButtonGroup> buttonGroups = new HashMap<JRadioButton, ButtonGroup>();
	public static HashMap<String, JPanel> fieldPanels = new HashMap<String, JPanel>();
	public static HashMap<JPanel, String> panelFields = new HashMap<JPanel, String>();
	public static HashMap<Component, String> componentFields = new HashMap<Component, String>();
	public static HashMap<String, List<MyField>> fields = new HashMap<String, List<MyField>>();
	public static HashMap<String, String> groups = new HashMap<String, String>();
	public static HashMap<String, List<String>> groupMembers = new HashMap<String, List<String>>();
	public static HashMap<String, Integer> sequenceInGroup = new HashMap<String, Integer>();

	public static String fieldValue(String fieldName) {
		List<MyField> lst = fields.get(fieldName);
		for (MyField f : lst) {
			if (f.isSet()) {
				return f.value();
			}
		}
		throw new RuntimeException(fieldName + " is not set");
	}

	public static boolean fieldIsSet(String fieldName) {
		List<MyField> lst = fields.get(fieldName);
		for (MyField f : lst) {
			if (f.isSet()) {
				return true;
			}
		}
		return false;
	}

	public static abstract class Condition {
		public abstract boolean eval();
	}

	public static abstract class Action {
		public abstract void act(HashMap<String, Float> semanticScores,
				int numChains);
	}

	static HashMap<Condition, Action> semanticMap = new HashMap<Condition, Action>();
	static HashMap<Condition, Integer> chainedConditions = new HashMap<Condition, Integer>();

	public static void semanticScoreIteration(
			HashMap<String, Float> semanticScores) {

		// Invoke each semantic
		for (Condition cond : semanticMap.keySet()) {
			// Evaluate the condition
			if (cond.eval()) {

				// Get the current chained condition count, if any
				Integer chains = chainedConditions.get(cond);
				if (chains == null) {
					chains = 1;
				}

				// If it's applicable, apply the scoring action
				semanticMap.get(cond).act(semanticScores, chains);

				// Increment chained conditions
				chainedConditions.put(cond, chains + 1);
			} else {
				// reset chained conditions for this field
				chainedConditions.put(cond, 0);

			}
		}
	}

	public static class Pair<A, B extends Comparable<B>> implements
			Comparable<Pair<A, B>> {
		final A a;
		final B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public int compareTo(Pair<A, B> other) {
			return b.compareTo(other.b);
		}
	}

	public static MouseListener focusEventSnooper = new MouseListener() {
		public void mouseClicked(MouseEvent arg0) {
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					eval((Component) e.getSource());
				}
			};
			SwingUtilities.invokeLater(r);
		}
	};

	public static void eval(Component currentComponent) {
		String currentField = componentFields.get(currentComponent);

		// Figure out what's set
		HashMap<String, Boolean> isSetMap = new HashMap<String, Boolean>();
		for (String s : fields.keySet()) {
			List<MyField> lst = fields.get(s);
			Boolean set = false;
			for (MyField f : lst) {
				if (f.isSet()) {
					set = true;
					break;
				}
			}
			isSetMap.put(s, set);
		}

		// Calculate all the scores
		HashMap<String, Float> distScores = null;
		if (currentField != null) {
			distScores = calcDistScores(currentField, new LinkedList<String>(
					fields.keySet()));
		}

		HashMap<String, Float> groupScores = calcGroupScores(new LinkedList<String>(
				fields.keySet()));
		HashMap<String, Float> seqScores = calcSeqScores(new LinkedList<String>(
				fields.keySet()));

		// Do an iteration of semantic scores
		HashMap<String, Float> semanticScores = new HashMap<String, Float>();
		semanticScoreIteration(semanticScores);

		HashMap<String, Float> totalScores = new HashMap<String, Float>();
		for (String s : fields.keySet()) {
			// Set static weights
			Float distWeight = .30f;
			Float groupWeight = .45f;
			Float semanticWeight = .55f;
			Float seqWeight = .15f;

			// Add up to get the real score for each field
			float val = 0f;

			if (distScores != null) {
				val += distScores.get(s) * distWeight;
			}

			val += groupScores.get(s) * groupWeight;
			val += seqScores.get(s) * seqWeight;
			if (semanticScores.get(s) != null) {
				val += semanticScores.get(s) * semanticWeight;
			}
			totalScores.put(s, val);
		}

		// Zero out the fields that have already been set
		for (String fieldName : isSetMap.keySet()) {
			if (isSetMap.get(fieldName)) {
				totalScores.put(fieldName, 0f);
			}
		}

		// Normalize the scores to be a number between 0 and 1
		normalizeScore(totalScores);

		for (String s : fields.keySet()) {
			if (isSetMap.get(s)) {
				paintField(s, 0f);
			} else {
				// paintField(s, distScores.get(s));
				// paintField(s, groupScores.get(s));
				// paintField(s, seqScores.get(s));
				paintField(s, totalScores.get(s));
			}
		}

	}

	public static HashMap<String, Float> calcDistScores(String currentField,
			LinkedList<String> fields) {
		HashMap<String, Float> distScores = new HashMap<String, Float>();
		LinkedList<Pair<String, Float>> scores = new LinkedList<Pair<String, Float>>();

		for (String s : fields) {
			float dist = calcDistance(currentField, s);
			scores.add(new Pair<String, Float>(s, dist));
			distScores.put(s, dist);
		}

		Collections.sort(scores);

		float maxDist = -1f;
		for (String s : fields) {
			float dist = distScores.get(s);
			if (dist > maxDist) {
				maxDist = dist;
			}
		}
		System.out.printf("Max dist is " + maxDist);

		// adjust the score to be a number between 0 and 1
		int i = 0;
		for (Pair<String, Float> p : scores) {
			distScores.put(p.a, 1f - (float) i / (float) scores.size());
			i++;
		}

		return distScores;
	}

	public static void normalizeScore(HashMap<String, Float> score) {
		LinkedList<Pair<String, Float>> scores = new LinkedList<Pair<String, Float>>();

		for (String s : score.keySet()) {
			scores.add(new Pair<String, Float>(s, score.get(s)));
		}

		Collections.sort(scores);

		Float max = Math.max(scores.getLast().b, 1f);
		// adjust the score to be a number between 0 and 1
		for (Pair<String, Float> p : scores) {
			score.put(p.a, (float) p.b / max);
		}

		return;
	}

	public static HashMap<String, Float> calcGroupScores(
			LinkedList<String> fieldNames) {
		HashMap<String, Float> groupScores = new HashMap<String, Float>();
		for (String s : fieldNames) {
			// Get the group for this field
			String groupName = groups.get(s);

			// Get all members of the group
			List<String> members = groupMembers.get(groupName);

			int numSet = 0;
			int total = members.size();

			// Count the total fields that are set for this group
			for (String member : members) {
				// Get the components for this field
				List<MyField> lst = fields.get(member);
				for (MyField mf : lst) {
					if (mf.isSet()) {
						numSet++;
						break;
					}
				}
			}
			// Set the score value
			groupScores.put(s, (float) numSet / (float) total);
		}

		return groupScores;
	}

	public static HashMap<String, Float> calcSeqScores(
			LinkedList<String> fieldNames) {
		HashMap<String, Float> seqScores = new HashMap<String, Float>();

		for (String groupName : groupMembers.keySet()) {

			// Get all members of the group
			List<String> members = groupMembers.get(groupName);

			// Get the sequence for each field in this group
			LinkedList<Pair<String, Integer>> rankedList = new LinkedList<Pair<String, Integer>>();
			for (String member : members) {
				// Get the seq for this field
				rankedList.add(new Pair<String, Integer>(member,
						sequenceInGroup.get(member)));
			}

			// Sort the ranked list
			Collections.sort(rankedList);

			for (Pair<String, Integer> p : rankedList) {
				seqScores.put(p.a, 1f - (float) p.b / rankedList.size());
			}
		}
		return seqScores;
	}

	public static class MyField {
		public MyField(Component child, String fieldName) {
			this.child = child;
			// Setup the focus listener

			child.addMouseListener(focusEventSnooper);
			// Register
			componentFields.put(child, fieldName);
		}

		String value() {
			// Use an ugly instance of class check
			if (child instanceof JRadioButton) {
				JRadioButton rb = (JRadioButton) child;
				return rb.getText();
			} else if (child instanceof JTextField) {
				JTextField jtf = (JTextField) child;
				return jtf.getText();
			} else if (child instanceof JTextArea) {
				JTextArea jta = (JTextArea) child;
				return jta.getText();
			}
			throw new RuntimeException(
					"logic error in value(): Unknown class type: "
							+ child.getClass());
		}

		void reset() {
			// Use an ugly instance of class check
			if (child instanceof JRadioButton) {

				JRadioButton rb = (JRadioButton) child;
				ButtonGroup bg = buttonGroups.get(rb);
				if (bg == null) {
					throw new RuntimeException("Could not find button group");
				}
				bg.clearSelection();

				return;
			} else if (child instanceof JTextField) {
				JTextField jtf = (JTextField) child;
				jtf.setText("");
				return;
			} else if (child instanceof JTextArea) {
				JTextArea jta = (JTextArea) child;
				jta.setText("");
				return;
			}

			throw new RuntimeException(
					"logic error in isSet(): Unknown class type: "
							+ child.getClass());

		}

		boolean isSet() {
			// Use an ugly instance of class check to test this property
			if (child instanceof JRadioButton) {
				JRadioButton rb = (JRadioButton) child;
				return rb.isSelected();
			} else if (child instanceof JTextField) {
				JTextField jtf = (JTextField) child;
				return jtf.getText().length() != 0;
			} else if (child instanceof JTextArea) {
				JTextArea jta = (JTextArea) child;
				return jta.getText().length() != 0;
			}

			throw new RuntimeException(
					"logic error in isSet(): Unknown class type: "
							+ child.getClass());

		}

		private final Component child;

		int getX() {
			Dimension d = child.getSize();
			return (int) ((float) d.width / 2.0f + (float) child
					.getLocationOnScreen().x);
		}

		int getY() {
			Dimension d = child.getSize();
			return (int) ((float) d.height / 2.0f + (float) child
					.getLocationOnScreen().y);
		}
	}

	public static void paintField(String field, float score) {
		// Get the JPanel for this field
		JPanel panel = fieldPanels.get(field);
		if (panel == null) {
			throw new RuntimeException("Panel does not exist for field "
					+ field);
		}

		System.out.println("Score for " + field + " : " + score);
		Color c = new Color(1f, 1f, 1 - score, 1f);
		panel.setOpaque(true);
		panel.setBackground(c);

	}

	public static float calcDistance(String fieldA, String fieldB) {
		// Get the components of field A
		List<MyField> lstA = fields.get(fieldA);
		if (lstA == null) {
			throw new RuntimeException("Field " + fieldA + "does not exist!");
		}
		List<MyField> lstB = fields.get(fieldB);
		if (lstB == null) {
			throw new RuntimeException("Field " + fieldB + "does not exist!");
		}

		float shortestDist = 999999f;
		for (MyField a : lstA) {
			for (MyField b : lstB) {
				// Calculate the distance
				float distX = Math.abs(a.getX() - b.getX());
				float distY = Math.abs(a.getY() - b.getY());
				float xy = (float) Math.sqrt(distX * distX + distY * distY);
				if (xy < shortestDist) {
					shortestDist = xy;
				}
			}
		}
		return shortestDist;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				new Motivation();
			}

		});
	}

	public Motivation() {

		setSize(1024, 768);
		makeForm();
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

	}

	public void makeForm() {
		GridLayout layout = new GridLayout();
		layout.setRows(4);
		layout.setColumns(4);
		JPanel top = new JPanel(layout);
		int i = 0;
		JPanel productsCustomerServiceGroup = makeFieldGroup(
				"productsCustomerServiceGroup",
				"Please rate the quality of our products and customer service:",
				makeRadioButtonField("QualityOfServiceAtTheBar",
						"The quality of service at the bar.", "1", "2", "3",
						"4", "5"),
				makeRadioButtonField("QualityOfServiceAtTheRegister",
						"The quality of service at the Register.", "1", "2",
						"3", "4", "5"),
				makeRadioButtonField("QualityOfServiceMyFoodItems",
						"The quality of my food items.", "1", "2", "3", "4",
						"5"),
				makeRadioButtonField("QualityOfMyBeverage",
						"The quality of my beverage.", "1", "2", "3", "4", "5"));

		JPanel dateTimeGroup = makeFieldGroup("dateTimeGroup",
				"Please note the date and time of your visit:",
				makeTextField("DateOfVisit", "Date:", 10),
				makeTextField("TimeOfVisit", "Time", 10));

		JPanel cleanlinessGroup = makeFieldGroup(
				"cleanlinessGroup",
				"How would you rate our cleanliness:",
				makeRadioButtonField("outsideOfStoreCleanliness",
						"The outside of the store was clean.", "1", "2", "3",
						"4", "5"),
				makeRadioButtonField("barCleanliness", "The bar was clean.",
						"1", "2", "3", "4", "5"),
				makeRadioButtonField("tablesChairsCleanliness",
						"The tables and chairs were clean.", "1", "2", "3",
						"4", "5"),
				makeRadioButtonField("floorsCleanliness",
						"The floors were clean.", "1", "2", "3", "4", "5"),
				makeRadioButtonField("windowsAndDoorsCleanliness",
						"The windows and doors were clean.", "1", "2", "3",
						"4", "5"),
				makeRadioButtonField("restroomsCleanliness",
						"The restrooms were clean.", "1", "2", "3", "4", "5"));

		JPanel experienceGroup = makeFieldGroup(
				"experienceGroup",
				"Based on your experience, would you..",
				makeRadioButtonField("comeBackAgain", "Come back again?",
						"Yes", "No"),
				makeRadioButtonField("reccomendUs",
						"Recommend us to a friend?", "Yes", "No"),
				makeRadioButtonField("positionWithUs",
						"be interested in a position with us?", "Yes", "No"));

		JPanel generalCommentsGroup = makeFieldGroup("generalCommentsGroup",
				"Please share any other comments you have about us.",
				makeBigTextField("generalComments", ""));

		// Build the semantic scoring rules
		buildSemanticRules();

		top.add(dateTimeGroup);
		top.add(productsCustomerServiceGroup);
		top.add(cleanlinessGroup);
		top.add(experienceGroup);
		top.add(generalCommentsGroup);
		JButton resetBtn = new JButton("Reset fields");
		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Clear every field
				for (List<MyField> lst : fields.values()) {
					for (MyField mf : lst) {
						mf.reset();
					}
				}

				// Reset the focus
				List<MyField> lst = fields.get("DateOfVisit");
				lst.get(0).child.requestFocus();
				Motivation.eval(null);
			}
		});
		top.add(resetBtn);
		i++;
		add(top);
	}

	public JPanel makeTextField(String fieldName, String preamble, int width) {
		JPanel ret = new JPanel();
		LinkedList<MyField> lst = new LinkedList<MyField>();
		fields.put(fieldName, lst);
		JTextField jtf = new JTextField();
		jtf.setColumns(width);
		JLabel label = new JLabel(preamble);
		label.setLabelFor(jtf);
		// Add it to the global fields hash for this field
		lst.add(new MyField(jtf, fieldName));
		// Register the field panel
		fieldPanels.put(fieldName, ret);
		panelFields.put(ret, fieldName);
		ret.add(label);
		ret.add(jtf);
		return ret;
	}

	public JPanel makeBigTextField(String fieldName, String preamble) {
		JPanel ret = new JPanel();
		LinkedList<MyField> lst = new LinkedList<MyField>();
		fields.put(fieldName, lst);
		JTextArea jta = new JTextArea();
		jta.setColumns(45);
		jta.setRows(6);
		JLabel label = new JLabel(preamble);
		label.setLabelFor(jta);
		// Add it to the global fields hash for this field
		lst.add(new MyField(jta, fieldName));
		// Register the field panel
		fieldPanels.put(fieldName, ret);
		panelFields.put(ret, fieldName);
		ret.add(label);
		ret.add(jta);
		return ret;
	}

	public JPanel makeRadioButtonField(String fieldName, String preamble,
			String... btnLabels) {

		LinkedList<MyField> lst = new LinkedList<MyField>();
		fields.put(fieldName, lst);

		JPanel ret = new JPanel();
		ret.add(new JLabel(preamble));

		ButtonGroup bg = new ButtonGroup();
		for (String s : btnLabels) {
			JRadioButton rb = new JRadioButton(s);

			// Add it to the global fields hash for this field
			lst.add(new MyField(rb, fieldName));

			// Add it to a button group
			bg.add(rb);
			buttonGroups.put(rb, bg);

			// Add it to the JPanel
			ret.add(rb);
		}

		// Register the field panel
		fieldPanels.put(fieldName, ret);
		panelFields.put(ret, fieldName);

		return ret;
	}

	public static JPanel makeFieldGroup(String groupName, String title,
			JPanel... fieldPanelsList) {

		LinkedList<String> groupMembersList = new LinkedList<String>();
		groupMembers.put(groupName, groupMembersList);

		JPanel ret = new JPanel();
		GridLayout layout = new GridLayout(fieldPanelsList.length, 1);
		ret.setLayout(layout);

		standardBorder(ret, title);
		int i = 0;
		for (JPanel jp : fieldPanelsList) {
			JPanel wrapper = new JPanel(new BorderLayout());
			// Look up the panel and get the field
			String fieldName = panelFields.get(jp);
			if (fieldName == null) {
				throw new RuntimeException(
						"Could not find panel on panelFields");
			}
			// register this field as part of the group
			groups.put(fieldName, groupName);
			groupMembersList.add(fieldName);
			sequenceInGroup.put(fieldName, i++);

			wrapper.add(jp, BorderLayout.WEST);
			ret.add(wrapper);

		}
		return ret;

	}

	public static void standardBorder(JPanel jp, String name) {
		jp.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), name, TitledBorder.LEFT,
				TitledBorder.TOP, new FontUIResource(new Font("tahoma",
						Font.BOLD, 14))));

	}

	public static void buildSemanticRules() {
		Condition c = new Condition() {
			@Override
			public boolean eval() {
				// if the restrooms weren't clean, we really want to know what
				// time they were there
				if (fieldIsSet("restroomsCleanliness")
						&& (!fieldIsSet("DateOfVisit") || !fieldIsSet("TimeOfVisit"))) {
					String val = fieldValue("restroomsCleanliness");
					if (val.equals("1") || val.equals("2")) {
						return true;
					}
				}
				return false;
			}
		};
		Action a = new Action() {
			@Override
			public void act(HashMap<String, Float> semanticScores, int numChains) {
				// Influence the date and time field scores
				Float dov = semanticScores.get("DateOfVisit");
				Float tov = semanticScores.get("TimeOfVisit");
				if (dov == null) {
					dov = 0f;
				}
				if (tov == null) {
					tov = 0f;
				}
				semanticScores.put("DateOfVisit", dov + .3f
						* (numChains * numChains));
				semanticScores.put("TimeOfVisit", tov + .3f
						* (numChains * numChains));
			}
		};
		semanticMap.put(c, a);

		c = new Condition() {
			@Override
			public boolean eval() {
				// if the bar service was bad, we really want to know what time
				// they were there
				if (fieldIsSet("QualityOfServiceAtTheBar")
						&& (!fieldIsSet("DateOfVisit") || !fieldIsSet("TimeOfVisit"))) {
					String val = fieldValue("QualityOfServiceAtTheBar");
					if (val.equals("1") || val.equals("2")) {
						return true;
					}
				}
				return false;
			}
		};
		semanticMap.put(c, a);

		c = new Condition() {
			@Override
			public boolean eval() {
				// if the register service was bad, we really want to know what
				// time they were there
				if (fieldIsSet("QualityOfServiceAtTheRegister")
						&& (!fieldIsSet("DateOfVisit") || !fieldIsSet("TimeOfVisit"))) {
					String val = fieldValue("QualityOfServiceAtTheRegister");
					if (val.equals("1") || val.equals("2")) {
						return true;
					}
				}
				return false;
			}
		};
		semanticMap.put(c, a);

		c = new Condition() {
			@Override
			public boolean eval() {
				// if they would recommend us to a friend, we really want them
				// to fill out the comments field
				if (fieldIsSet("reccomendUs")
						&& (!fieldIsSet("generalComments"))) {
					String val = fieldValue("reccomendUs");
					if (val.equals("Yes")) {
						return true;
					}
				}
				return false;
			}
		};
		a = new Action() {
			@Override
			public void act(HashMap<String, Float> semanticScores, int numChains) {
				// Influence the generalComments field
				Float val = semanticScores.get("generalComments");
				if (val == null) {
					val = 0f;
				}
				semanticScores.put("generalComments", val + .3f
						* (numChains * numChains));
			}
		};
		semanticMap.put(c, a);

		c = new Condition() {
			@Override
			public boolean eval() {
				// if they would want a job with us, we really want them to fill
				// out the comments field
				if (fieldIsSet("positionWithUs")
						&& (!fieldIsSet("generalComments"))) {
					String val = fieldValue("positionWithUs");
					if (val.equals("Yes")) {
						return true;
					}
				}
				return false;
			}
		};
		semanticMap.put(c, a);

	}
}
