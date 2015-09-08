import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The controller handles executing commands received from users,
 * and defines the movements of the robot.
 */
public class Controller implements Runnable{
	OutputStream outputStream;
	SerialPort port;
	String command;
	
	public Controller(SerialPort p, String cmd) {
		port = p;
		command = cmd;
		try {
			outputStream = port.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
    /* Constructor */
	public Controller(SerialPort p) {
		port = p;
		try {
			outputStream = port.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /* Override the run method */
	@Override
	public void run() {
		String [] op = command.split(" ");
		System.out.println("IN");
		for(int i = 0; i < op.length; i++) {
			switch(op[i]) {
				case "stop": String [] s = {op[i]}; execute(s); break;
				default: String [] d = {op[i], op[i+1]}; execute(d); i++; break;
			}
			
		}
	}
	
	/* Execute the command */
	public void execute(String [] op) {
		switch(op[0]) {
		case "move": try {
			move(Integer.parseInt(op[1]));break;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		case "stop": try {
			stop();break;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		case "rotate": try {
			rotate(Double.parseDouble(op[1]));break;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		case "rotatemove": try {
			rotate(0, 0); break;
		} catch (IOException e) {
			e.printStackTrace();
		}
		default: System.out.println("Illegal command!");
	}
	}
	
	/* Move straight */
	public void move(double length) throws IOException {
		String cmd_1 = "1lr" + (int)(length*1163) + "\r";
		String cmd_2 = "2lr" + (int)(-length*1163) + "\rm\r";
		outputStream.write(cmd_1.getBytes());
		outputStream.write(cmd_2.getBytes());
		System.out.println("Move "+length+" cm");
	}
	
	/* Simple rotate */
	public void rotate(double degree) throws IOException {
		String cmd1 = "2lr" + (int)(-254*degree) + "\r";
		String cmd2 = "1lr" + (int)(-254*degree) + "\rm\r";
		outputStream.write(cmd1.getBytes());
		outputStream.write(cmd2.getBytes());
	}
	
	/* Rotate and move */
	public void rotate(double radius, double degree) throws IOException {
		String cmd;
		cmd = "1v-1000\r";
		outputStream.write(cmd.getBytes());
		cmd = "2v1000\r";
		outputStream.write(cmd.getBytes());
	}
	
	/* Stop */
	public void stop() throws IOException {
		String cmd = "v0\r";
		outputStream.write(cmd.getBytes());
	}
}
