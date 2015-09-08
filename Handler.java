import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * The handler handles connecting to the robot via the serial
 * port, and process signals received from the robot.
 */
public class Handler extends JComponent{
	CommPortIdentifier portID;
	SerialPort port;
	
	final int MIN_SEGMENT = 1;
	final int MAX_SEGMENT = 529;
	final double DELTA_ANGLE = 1.44;
	double MIN_ANGLE = -5.04;
	double MAX_ANGLE = 185.04;
	
	InputStream 	inputStream;
	String 			receivedData;
	byte[] 			readBuffer = new byte [1024];
	int 			numOfBytes = 1024;
	int 			nearestDistance;
	int[] 			x = new int [133];
	int[] 			y = new int [133];
	double 			nearestDegree = 0;
	JLabel			console;
	Graphics		graph;
	
	boolean			dataProcessed = false;
	
	public ArrayList<Obstacle> list;
	
	public Handler(JLabel console) 
	throws IOException {
		receivedData = "";
		this.console = console;
	}
	
	/* Open the port */
	public int openCommPort(String portName, int baudRate, String appName) throws IOException {
			
			// Find the expected port
			Enumeration portList = CommPortIdentifier.getPortIdentifiers();
			while(portList.hasMoreElements()) {
				CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
				if(id.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& id.getName().equals(portName)) {
					console.setText("The serial port "+portName+" is open at a baud rate of "+baudRate);
					System.out.println("Port "+portName+" found!");
					portID = id;
					break;
				}
			}
			
			if(portID == null) {
				console.setText("Port not found!");
				System.err.println("Port not found!");
				return 1;
			}
			
			// Open the port
			try {
				port = (SerialPort) portID.open(appName, 2000);
			} catch (PortInUseException e) {System.out.println(e);}
			try {
	            inputStream = port.getInputStream();
	        } catch (IOException e) {System.out.println(e);}
	        try {
	            port.setSerialPortParams(baudRate,
						                 SerialPort.DATABITS_8,
						                 SerialPort.STOPBITS_1,
						                 SerialPort.PARITY_NONE);
	        } catch (UnsupportedCommOperationException e) {System.out.println(e);}
	        
	        return 0;
	}
	
	public void closePort() {
		port.close();
	}
	
	/* Read from the port */
	public int receiveBytes(JTextArea dataContent) throws IOException {
		boolean loopAgain = true;
		System.out.println("Receive Bytes");
		System.out.println("In Read");
		int [] extractedData = new int [133];
		while (true) {
			if(!(inputStream.available() > 0)) continue;
			byte [] buffer = new byte [1];
			inputStream.read(buffer);
			String oneByte = Integer.toHexString(buffer[0] & 0xff);
			if(oneByte.equals("fe")) {
				inputStream.read(buffer);
				oneByte = Integer.toHexString(buffer[0]&0xff);
				if(oneByte.equals("4")) {
					inputStream.read(buffer);
					oneByte = Integer.toHexString(buffer[0]&0xff);
					if(oneByte.equals("0")) {
						inputStream.read(buffer);
						oneByte = Integer.toHexString(buffer[0]&0xff);
						if(oneByte.equals("1")) {
							inputStream.read(buffer);
							oneByte = Integer.toHexString(buffer[0]&0xff);
							if(oneByte.equals("2")) {
								inputStream.read(buffer);
								oneByte = Integer.toHexString(buffer[0]&0xff);
								if(oneByte.equals("11")){
									numOfBytes = 0;
									System.out.println(translate(buffer[0]));
									dataContent.setText("Here are data received:");
									String tmp = "";
									byte [] data = new byte[266];									int count = 0;
									while((count=inputStream.read(buffer))!=-1) {
										if(count == 0) continue;
										else {
											data[numOfBytes] = buffer[0];
											numOfBytes++;
										}
										if(numOfBytes == 266) break;
									}
									for(int i = 0; i < 266; i++) {
										if(i%2 == 0) {
											tmp = translate(data[i]);
										} else {
											tmp += translate(data[i]);
											extractedData[i/2] = Integer.parseInt(tmp, 16);
											dataContent.append(extractedData[i/2]+"-");
										}
									}
									String end = "";
									inputStream.read(buffer); System.out.println("Check bit: "+translate(buffer[0]));
									inputStream.read(buffer);end += Integer.toHexString(buffer[0]&0xff);
									inputStream.read(buffer);end += Integer.toHexString(buffer[0]&0xff);
									inputStream.read(buffer);end += Integer.toHexString(buffer[0]&0xff);
									inputStream.read(buffer);end += Integer.toHexString(buffer[0]&0xff);
									inputStream.read(buffer);end += Integer.toHexString(buffer[0]&0xff);
									System.out.println("This is the end: "+end);

									if(!end.equals("00000")) {
										System.out.println("Problem!");
										loopAgain = true;
									}
									else {
										loopAgain = false;
										System.out.println("OK");
										dataProcess(extractedData);
									}
									
									
								}
							}
						}
					}
				}
			}
			if(loopAgain == true) {
				continue;
			} else break;
		}
		return 0;
			
	}
	
	/* Translate the binary message to a hex string */
	public String translate(byte data) {
		return String.format("%2s", Integer.toHexString(data & 0xFF)).replace(' ', '0');
	}
	
	/* Deal with the extracted data */
	public void dataProcess(int[] data) {
		System.out.println("========dataProcess entered, "+numOfBytes);
		if(numOfBytes > 133) numOfBytes = 133;

		list = new ArrayList<Obstacle>();
		Obstacle.clearSectors();
		Obstacle.setSectors();
		
		for(int i=0; i<numOfBytes; i++) {
			y[i] = (int)(data[i] * Math.sin(MIN_ANGLE*Math.PI/180));
			x[i] = (int)(data[i] * Math.cos(MIN_ANGLE*Math.PI/180));
			
			list.add(new Obstacle(data[i]/10, Math.abs(90-MIN_ANGLE), -x[i]/10, y[i]/10, i));
			
			MIN_ANGLE = MIN_ANGLE + DELTA_ANGLE;
			if(nearestDistance > data[i]) {
				nearestDistance = data[i];
				nearestDegree = MIN_ANGLE;
			}
		}
		Obstacle.scanSectors();
		dataProcessed = true;

	}
	
	public int[] valueOfX() {
		return x;
	}
	
	public int[] valueOfY() {
		return y;
	}
	
	public SerialPort getPort() {
		return port;
	}
	
}
