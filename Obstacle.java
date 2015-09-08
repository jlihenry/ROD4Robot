import java.util.ArrayList;

/**
 * This class defines the obstacles that the robot may meet.
 */
public class Obstacle {

	public static final int OCCUPIED = 1;
	public static final int FREE = 0;
	
	public static ArrayList<Sector> sectors;
	public static int SELECT;
	
	static double robotR = 20; // radius of the robot (mm)
	static double MIN_COST = 65536;

	int number;
	double d;
	double L;
	double angle;
	
	int x;
	int y;
	
	public Obstacle(double d, double a, int x, int y, int num) {
		final double ANGLE = 0.008;
		double beta = 0;
		double alpha;
		this.x = x;
		this.y = y;
		this.d = d;
		number = num;
		angle = Math.PI*a/180;
				
		// Configuration space
		int i = 0;
		while(Math.sin(beta) <= (robotR/d)) {
			if(d < 30) {
				sectors.get(number+i).set(0);
				break;
			}
			double distance0, distance1;
			double x0, x1;
			double y0, y1;
			alpha = Math.PI/2 - angle - beta;
			if(x < 0) alpha = -alpha;

			double delta = Math.pow(robotR, 2)/Math.pow(Math.cos(alpha), 2) - Math.pow(Math.tan(alpha)*x-y, 2) ;

            x0 = ( x + y * Math.tan(alpha)
				   + Math.sqrt(delta) ) 
				   * Math.pow(Math.cos(alpha), 2);
			
			x1 = ( x + y * Math.tan(alpha)
					   - Math.sqrt(delta) )
					   * Math.pow(Math.cos(alpha), 2);
			
			y0 = Math.tan(alpha) * x0;
			y1 = Math.tan(alpha) * x1;
			
			distance0 = Math.sqrt(Math.pow(x0, 2) + Math.pow(y0, 2));
			distance1 = Math.sqrt(Math.pow(x1, 2) + Math.pow(y1, 2));
			L = Math.min(distance0, distance1);

            if(number+i < 133) {
				if(L < sectors.get(number+i).get()) {
					sectors.get(number+i).set(L);
				}
			}
			
			i++;
			beta = ANGLE*i*Math.PI;
		}
	}
	
	public static void setSectors() {
		double a = 185.04;
		if(sectors == null) sectors = new ArrayList<Sector>();
		for (int i = 0; i < 133; i++) {
			sectors.add(new Sector(i, a));
			a -= 1.44;
		}
	}
	
	public static void clearSectors() {
		if(sectors != null) sectors.clear();
		MIN_COST = 65536;
		SELECT = 134;
	}
	
	public static void categorize(int goalx, int goaly) {
		double goalDistance = Math.sqrt(Math.pow((double)goalx, 2) + Math.pow((double)goaly, 2));
		double goalAngle = Math.atan((double)goaly/(double)goalx);
		
		if(goalx < 0) goalAngle = Math.PI + goalAngle;
		
		for (int i = 0; i < sectors.size(); i++) {
			// Set type for each sector
			Sector s = sectors.get(i);
			if(s.get() >= goalDistance) s.setType(FREE);
			else {
				if(goalDistance <= 50) {
					if(s.get() <= 20) s.setType(OCCUPIED);
					else s.setType(FREE);
				} else {
					if(s.get() <= goalDistance/2) s.setType(OCCUPIED);
					else s.setType(FREE);
				}
			}
			
			// Calculate cost for each sector
			double difference = Math.abs(goalAngle - s.getA());
			double cost = 0.5*difference + 0.2*s.getA() - 0.3*s.get();
			System.out.println("angle = "+s.getA()*180/Math.PI+", Cost is "+cost+", type = "+s.getT());

            if(difference < 0.008*Math.PI && s.getT() == FREE) {
				SELECT = i; 
				System.out.println("SELECT = "+SELECT);
				System.out.println("Goal: "+goalx+", "+goaly+", "+goalDistance);
				System.out.println((double)goaly/(double)goalx);
				System.out.println("Goal angle = "+goalAngle*180/Math.PI); 
				return;
			}
			else if(s.getT() == FREE && cost < MIN_COST) {
				MIN_COST = cost;
				SELECT = i;
			}
		}
		System.out.println("Goal: "+goalx+", "+goaly+", "+goalDistance);
		System.out.println((double)goaly/(double)goalx);
		System.out.println("Goal angle = "+goalAngle*180/Math.PI);
		System.out.println("SELECT = "+SELECT);
	}
	
	public double getd() {
		return d;
	}
	
	public double getL() {
		return L;
	}
	
	public double getAngle() {
		return angle;
	}
}
