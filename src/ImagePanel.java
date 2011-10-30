
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

class ImagePanel extends JPanel 
{  
	

	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected BufferedImage image;  
    protected double scale;  

    
    
    public ImagePanel()  
    {    
        scale = 1.0;  


    }  
   
    @Override
	protected void paintComponent(Graphics g)  
    {  
        super.paintComponent(g);
        if(image == null || realImage == null) {
           Graphics2D g2 = (Graphics2D)g;
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                   RenderingHints.VALUE_ANTIALIAS_ON);
           g.setFont(Defaults.LABEL_FONT);
           FontMetrics fm = g2.getFontMetrics();
           String msg = "No Image Loaded";
           g.drawString(msg, 
        		         getWidth()/2 - fm.stringWidth(msg)/2,
        		         getHeight()/2 + fm.getAscent() /2 - 1 );

           return;
        }
        
        
        g.drawImage(image, 0, 0, null);
    }  
   
    @Override
	public Dimension getPreferredSize()  
    {  
    	if(realImage == null) {
    		return new Dimension();
    	}
  
    	
    	int w = (int)(scale * realImage.getWidth());  
        int h = (int)(scale * realImage.getHeight());  
        return new Dimension(w, h);  
    }  
   
    public double getScale() {
    	return scale;
    }
    public void setScale(double s)  
    {
    	if(realImage == null) return;
        scale = s;  
        int w,h;
        w = (int) Math.round(realImage.getWidth() * s);
        h = (int) Math.round(realImage.getHeight() * s);
        if(w<1 || h < 1) return;
        image = resizeImage(realImage, w, h );
        revalidate();        
        repaint();  
    }  

	private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height){
		int type=BufferedImage.TYPE_INT_RGB;
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}
    
	private BufferedImage realImage;
	
	public double getDefaultScale() {

		if(realImage == null) return 1;
		
        int panelWidth = 600;
        int imageWidth = realImage.getWidth();
        double newScale = (double)panelWidth / (double)imageWidth; 
        return newScale;
	}
	
	public BufferedImage getImage() {
		return realImage;
	}
	
    public void setImage(String f)  
    {  

        try  
        {  
            realImage = ImageIO.read(new File(f));
            //set optimum scale

            setScale(getDefaultScale());
            

        }  

        catch(Exception e)  
        {  
        	realImage = null;
        }
        

        revalidate();
        repaint();
    }
    
    public void setImage(BufferedImage img)  
    {  

   
        realImage = img;
        //set optimum scale
        setScale(getDefaultScale());

        revalidate();
        repaint();
    }
    
/*
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(realImage == null) return;
		int notches = e.getWheelRotation();
		int posX = e.getX();
		int posY = e.getY();
		
		System.out.println("Mouse moved " + notches + " notches at X: " + posX + " Y: " + posY  );
		scale = scale + (notches / 20.0);
		setScale(scale);
	}
*/


}  
   