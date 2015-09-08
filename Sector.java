/**
 * POJO of a sector
 */
public class Sector {
	static double MAX = 65536;
	int number;
	int type;
	double L;
	double angle;
	
	public Sector(int n, double a) {
		number = n;
		L = MAX;
		angle = a*Math.PI/180;
	}
	
	public void set(double l) {
		L = l;
	}
	
	public double get() {
		return L;
	}
	
	public double getA() {
		return angle;
	}
	
	public int getT() {
		return type;
	}
	
	public void setType(int t) {
		type = t;
	}
}
