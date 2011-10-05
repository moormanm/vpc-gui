import java.io.File;


public class VPCParams {
	public  int rows;
	public  int cols;
	public  int plateRadius;
	public  int dilationFactor;
	public  int erosionFactor;



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
	
	public VPCParams setPlateRadius(int plateRadius) {
		this.plateRadius=plateRadius;
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