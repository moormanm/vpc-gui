import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.io.File;


public class Defaults {
  public static final FontUIResource FONT = new FontUIResource(new Font("tahoma", Font.PLAIN, 12 ));
  public static final FontUIResource TITLE_FONT = new FontUIResource(new Font("tahoma", Font.BOLD, 24 ));
  public static final FontUIResource LABEL_FONT = new FontUIResource(new Font("tahoma", Font.BOLD, 12 ));
  public static final FontUIResource RESULTS_FONT = new FontUIResource(new Font("mono", Font.BOLD, 24 ));
  public static File  fileChooserDir  = new File(System.getProperty("user.home"));
}

