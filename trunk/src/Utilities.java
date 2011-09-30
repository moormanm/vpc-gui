

	import java.awt.AWTException;
	import java.awt.Component;

	import java.awt.Dimension;
	import java.awt.Rectangle;
	import java.awt.Robot;
	import java.awt.image.BufferedImage;
	import java.io.File;

	import java.io.IOException;
	import java.text.SimpleDateFormat;
	import java.util.Calendar;
	import javax.imageio.ImageIO;
	import javax.swing.JFileChooser;
	import javax.swing.JOptionPane;
	import javax.swing.UIManager;
	import javax.swing.filechooser.FileNameExtensionFilter;

	public class Utilities {

	  public static void setSwingFont(javax.swing.plaf.FontUIResource f) {
	    java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()) {
	      Object key = keys.nextElement();
	      Object value = UIManager.get(key);
	      if (value instanceof javax.swing.plaf.FontUIResource)
	        UIManager.put(key, f);
	    }
	  }
	  public static String newLine = System.getProperty("line.separator");
	  public static void showError(String error) {
	    JOptionPane.showMessageDialog(null, error, "Error",
	        JOptionPane.ERROR_MESSAGE);
	  }

	  public static void showInfo(String info, String title) {
	    JOptionPane.showMessageDialog(null, info, title,
	        JOptionPane.INFORMATION_MESSAGE);
	  }

	 
	  public static String popupAskUser(String question, String[] answers, String title) {
	    int answer = JOptionPane.showOptionDialog(null,
	        question, title, 0,
	        JOptionPane.QUESTION_MESSAGE, null, answers, answers[0]);
	    // Return null if the user closed the dialog box
	    if (answer == JOptionPane.CLOSED_OPTION) {
	      return null;
	    }

	    // Return their selection
	    return answers[answer];

	  }
	 

	  static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	  public static String timeStamp() {
	    Calendar cal = Calendar.getInstance();
	    return sdf.format(cal.getTime());
	  }

	  public static void captureScreen(Component Area) {

	    // Find out where the user would like to save their screen shot
	    String fileName = null;
	    JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "Screen Shots", "png");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showSaveDialog(null);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File saveFile = new File(chooser.getSelectedFile().getPath() + ".png");
	      fileName = saveFile.toString();

	      // Check to see if we will overwrite the file
	      if (saveFile.exists()) {
	        int overwrite = JOptionPane.showConfirmDialog(null,
	            "File already exists, do you want to overwrite?");
	        if (overwrite == JOptionPane.CANCEL_OPTION
	            || overwrite == JOptionPane.CLOSED_OPTION
	            || overwrite == JOptionPane.NO_OPTION) {
	          return;
	        }
	      }
	    }
	    // If they didn't hit approve, return
	    else{
	      return;
	    }

	    // Determine the exact coordinates of the screen that is to be captured
	    Dimension screenSize = Area.getSize();
	    Rectangle screenRectangle = new Rectangle();
	    screenRectangle.height = screenSize.height;
	    screenRectangle.width = screenSize.width;
	    screenRectangle.x = Area.getLocationOnScreen().x;
	    screenRectangle.y = Area.getLocationOnScreen().y;

	    // Here we have to make the GUI Thread sleep for 1/4 of a second
	    // just to give the save dialog enough time to close off of the
	    // screen. On slower computers they were capturing the screen
	    // before the dialog was out of the way.
	    try {
	      Thread.currentThread();
	      Thread.sleep(250);
	    } catch (InterruptedException e1) {
	      e1.printStackTrace();
	    }

	    // Attempt to capture the screen at the defined location.
	    try {
	      Robot robot = new Robot();
	      BufferedImage image = robot.createScreenCapture(screenRectangle);
	      ImageIO.write(image, "png", new File(fileName));
	    } catch (AWTException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      JOptionPane.showMessageDialog(null, "Could not save screen shoot at: "
	          + fileName);
	      e.printStackTrace();
	    }
	  }

}
