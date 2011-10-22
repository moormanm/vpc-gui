import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;





public class VPCParams {

	public  int plateRadius;
	public  int maxPlaqueRadius;
	public  int minPlaqueRadius;
	public String name;

	public VPCParams setName(String name) {
		this.name = name;
		return this;
	}


	
	public VPCParams setPlateRadius(int plateRadius) {
		this.plateRadius=plateRadius;
		return this;
	}
	
	public VPCParams setMaxPlaqueRadius(int maxPlaqueRadius) {
		this.maxPlaqueRadius=maxPlaqueRadius;
		return this;
	}
	
	public VPCParams setMinPlaqueRadius(int minPlaqueRadius) {
		this.minPlaqueRadius=minPlaqueRadius;
		return this;
	}
	public static VPCParams loadFromXml(InputStream is) throws IOException {
	
		//Get all the properties from the properties map
		Properties p = new Properties();
		p.loadFromXML(is);
		
		VPCParams ret = new VPCParams().setName( p.getProperty("name")).
				                        setPlateRadius(Integer.parseInt(p.getProperty("plateRadius"))).
				                        setMaxPlaqueRadius(Integer.parseInt(p.getProperty("maxPlaqueRadius"))).
				                        setMinPlaqueRadius(Integer.parseInt(p.getProperty("minPlaqueRadius")));
		return ret;
	}
	

	public void storeToXml(OutputStream os, String comment) throws IOException {
		//Put all the properties into a properties map
		Properties p = new Properties();
		p.put("name", name);
		p.put("plateRadius", Integer.toString(plateRadius));
		p.put("maxPlaqueRadius", Integer.toString(maxPlaqueRadius));
		p.put("minPlaqueRadius", Integer.toString(minPlaqueRadius));
		
		p.storeToXML(os, comment);
	}

}