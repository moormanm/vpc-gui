import java.io.File;


public class VPCParams {
	public  int rows;
	public  int cols;
	public  int plateDiameter;
	public  int dilationFactor;
	public  int erosionFactor;

	public File imageFile;

	public VPCParams() {
		
	}
	
	public VPCParams setRows(int rows) {
		this.rows=rows;
		return this;
	}
	
	public VPCParams setCols(int cols) {
		this.cols=cols;
		return this;
	}
	
	public VPCParams setPlateDiameter(int plateDiameter) {
		this.plateDiameter=plateDiameter;
		return this;
	}
	
	public VPCParams setImageFile(File imageFile) {
		this.imageFile = imageFile;
		return this;
	}

	public VPCParams setErosionFactor(int erosionFactor) {
		this.erosionFactor = erosionFactor;
		return this;
	}
	
	public VPCParams setDilationFactor(int dilationFactor) {
		this.dilationFactor = dilationFactor;
		return this;
	}
	



}