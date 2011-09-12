import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.print.DocFlavor.URL;
import javax.swing.JPanel;

class ImagePanel extends JPanel 
{  
	

	
    private BufferedImage image;  
    private double scale;  

    
    
    public ImagePanel()  
    {    
        scale = 1.0;  
        setBackground(Color.black); 

    }  
   
    protected void paintComponent(Graphics g)  
    {  
        super.paintComponent(g);
        if(image == null) {
        	return;
        }
        
        
        g.drawImage(image, 0, 0, null);
    }  
   
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
        	return;
        }
        

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
   