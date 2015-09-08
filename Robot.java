/*
 * @author lijian
 */
import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;

class DrawingBoard extends JPanel {
	Graphics graph;
	boolean	flag;
	int scale = 5;
	int[] x = new int[133];
	int[] y = new int[133];

	public DrawingBoard(){flag = false;}
	
	public DrawingBoard(int[] x, int[] y) {
		flag = true;
		this.x = x;
		this.y = y;
	}
	
	public DrawingBoard(int[] x, int[] y, int scale) {
		flag = true;
		this.x = x;
		this.y = y;
		this.scale = scale;
	}
	
	public void set(int[] x, int[] y, int scale) {
		this.x = x;
		this.y = y;
		this.scale = scale;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		graph = g;
		graph.setColor(Color.white);
		graph.fillRect(0, 0, this.getWidth(), this.getHeight());
		for (int i = 1; i < 50; i++) {
			if(i%5 == 0) graph.setColor(Color.blue);
			else graph.setColor(Color.green);
			graph.drawLine(i*10, 0, i*10, 500);
			graph.drawLine(0, i*10, 500, i*10);
		}
		graph.setColor(Color.red);
		graph.drawLine(0, 400, 500, 400);
		graph.drawLine(250, 0, 250, 500);
		
		/*
		 * Scales standards:
		 * scale=5, 5cm/p
		 * scale=10, 10cm/p
		 * scale=100, 100cm/p
		 */
		if(flag) {
			System.out.println("--------lines to draw");
			graph.setColor(Color.black);
			for(int i=0; i<132; i++) {
				int x1 = 250 - (int)(x[i]/scale);
				int x2 = 250 - (int)(x[i+1]/scale);
				int y1 = 400 - (int)(y[i]/scale);
				int y2 = 400 - (int)(y[i+1]/scale);
				
				graph.drawLine(x1, y1, x2, y2);
			}
		}
		
		double unit = scale*0.05;
		
		graph.drawString("0", 250, 410);
		graph.drawString(unit+"m", 300, 410);
		graph.drawString(unit*2+"m", 350, 410);
		graph.drawString(unit*3+"m", 400, 410);
		graph.drawString(unit*4+"m", 450, 410);
		graph.drawString(unit+"m", 200, 410);
		graph.drawString(unit*2+"m", 150, 410);
		graph.drawString(unit*3+"m", 100, 410);
		graph.drawString(unit*4+"m", 50, 410);
	}
	
	
	public void addLine(int x1, int y1, int x2, int y2) {		
		graph.setColor(Color.black);
		graph.drawLine(x1, y1, x2, y2);
	}

}

public class Robot {
	JFrame frame = new JFrame();
	JButton button = new JButton("START");
	JButton stop = new JButton("STOP");
	JButton send = new JButton("SEND");
	JPanel dataBoard = new JPanel();
	JPanel inputBoard = new JPanel();
	JPanel consoleBoard = new JPanel();
	JPanel dumBoard = new JPanel();
	JPanel complexBoard = new JPanel();
	JPanel cmdBoard = new JPanel();
	JLabel selectComPort = new JLabel("COM#: ");
	JLabel consoleContent = new JLabel("Here is the console...");
	JLabel selectBaudRate = new JLabel("baud rate:");
	JLabel command = new JLabel("Please enter command: ");
	JLabel cm = new JLabel("(cm)");
	JLabel cm2 = new JLabel("(cm)");
	JTextArea dataContent = new JTextArea(27, 16);
	JComboBox portSelect = new JComboBox();
	JComboBox baudRateSelect = new JComboBox();
	JComboBox scaleSelect = new JComboBox();
	JComboBox modeSelect = new JComboBox();
	JTextField cmd = new JTextField(20);
	
	JPanel inputBoard2 = new JPanel();
	JLabel selectComPort2 = new JLabel("COM#: ");
	JLabel selectBaudRate2 = new JLabel("baud rate: ");
	JLabel selectScale = new JLabel("scale: ");
	JLabel selectMode = new JLabel("Mode: ");
	JLabel scanner = new JLabel("Scanner Control: ");
	JComboBox portSelect2 = new JComboBox();
	JComboBox baudRateSelect2 = new JComboBox();
	JButton open = new JButton("OPEN");
	JButton close = new JButton("CLOSE");
	
	Handler drawHandler;
	Handler controlHandler;

	DrawingBoard drawingBoard;
	
	String mode;
	String portName;
	String baudRate;
	
	String portName2;
	String baudRate2;
	
	boolean first = true;
	
	int[] x = new int[133];
	int[] y = new int[133];
	int scale = 5;
	int bRate;
	
	Timer timer;
	ActionListener timerAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			try {
				drawHandler = new Handler(consoleContent);
				
				int status = drawHandler.openCommPort(portName, bRate, "Handler");
				
				int flag = drawHandler.receiveBytes(dataContent);

				x = drawHandler.valueOfX();
				y = drawHandler.valueOfY();
				if(first) {
					drawingBoard = new DrawingBoard(x, y, scale);
					setDrawingBoard();
					dumBoard.add(drawingBoard);
					first = false;
				} else {
					drawingBoard.set(x, y, scale);
				}

				drawHandler.closePort();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
		}
	};
	
	MouseAdapter mouseHandler = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			int goalx = e.getX()-250;
			int goaly = 400-e.getY();
			double goalDistance = Math.sqrt(Math.pow((double)goalx, 2) + Math.pow((double)goaly, 2));

            while(goalDistance >= 10) {
				try {
					drawHandler = new Handler(consoleContent);
					drawHandler.openCommPort(portName, bRate, "Handler");
					drawHandler.receiveBytes(dataContent);
	
					drawHandler.closePort();
					
					/* Navigation */
					Obstacle.categorize(goalx, goaly);
					double desiredAngle = 90 - Obstacle.sectors.get(Obstacle.SELECT).getA()*180/Math.PI;
					double min = 65536;
					if(Obstacle.SELECT == 134) {
						System.out.println("No path found!");
						break;
					}
					if (Obstacle.SELECT > 66) {
						for (int i = 66; i < Obstacle.SELECT; i++) {
							if(Obstacle.sectors.get(i).get() < min) min = Obstacle.sectors.get(i).get();
						}
					} else {
						for (int i = 66; i > Obstacle.SELECT; i--) {
							if(Obstacle.sectors.get(i).get() < min) min = Obstacle.sectors.get(i).get();
						}
					}
					if (min > Math.min(goalDistance, 30)) min = Math.min(goalDistance, 30);
					double offset = min;
					System.out.println("Decided angle is "+Obstacle.sectors.get(Obstacle.SELECT).getA()*180/Math.PI);
					System.out.println("Decided offset is "+offset);
					
					// Calculate the new location of the goal
					int prex = goalx, prey = goaly;
					goalx = (int)((double)prex*Math.cos(desiredAngle*Math.PI/180) - (double)prey*Math.sin(desiredAngle*Math.PI/180) + 5.65*Math.sin(desiredAngle*Math.PI/180));
					goaly = (int)((double)prex*Math.sin(desiredAngle*Math.PI/180) + (double)prey*Math.cos(desiredAngle*Math.PI/180) - offset);
					goalDistance = Math.sqrt(Math.pow((double)goalx, 2) + Math.pow((double)goaly, 2));
					
					/* Motor Control */
					controlHandler = new Handler(consoleContent);
					controlHandler.openCommPort(portName2, Integer.parseInt(baudRate2), "Controler");
					
					Controller c = new Controller(controlHandler.getPort());
					c.rotate(desiredAngle);
					Thread.sleep(3000);
					c.move(offset);
					Thread.sleep(5000);
					
					controlHandler.closePort();
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
			}
		}
	};
	
	public Robot() throws IOException {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDumBoard();
		setDataBoard();
		setInputBoard1();
		setInputBoard2();
		setCmdBoard();
		setConsoleBoard();
		combine();
		
		frame.setTitle("Robot Terminal");
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		frame.getContentPane().add(complexBoard);
		frame.getContentPane().add(inputBoard);
		frame.getContentPane().add(inputBoard2);
		frame.getContentPane().add(cmdBoard);
		frame.getContentPane().add(consoleBoard);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	// Set the input
	public void setInput() {
		Enumeration _portList = CommPortIdentifier.getPortIdentifiers();
		Vector<String> portList = new Vector<String>();
		String[] b = {"", "1200", "9600", "19200", "38400", "57600", "115200"};
		String[] s = {"", "5", "10", "20", "50", "100"};
		String[] m = {"", "Manual", "Auto"};
		System.out.println("Very good");
		while(_portList.hasMoreElements()) {
			CommPortIdentifier id = (CommPortIdentifier) _portList.nextElement();
			portList.add(id.getName());
			System.out.println(id.getName());
		}
		portSelect = new JComboBox(portList);
		baudRateSelect = new JComboBox(b);
		scaleSelect = new JComboBox(s);
		modeSelect = new JComboBox(m);
		
		portName = (String) portSelect.getSelectedItem();
		portSelect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					portName = (String) e.getItem();
					portSelect.setEditable(false);
					
				}
			}
		});
		
		baudRateSelect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					baudRate = (String) e.getItem();
					baudRateSelect.setEditable(false);
					
				}
			}
		});
		
		scaleSelect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					scale = Integer.parseInt((String) e.getItem());
					scaleSelect.setEditable(false);
				}
			}
		});
		
		modeSelect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					mode = (String) e.getItem();
					modeSelect.setEditable(false);
				}
			}
		});
	}
	
	//
	public void setInput2() {
		Enumeration _portList = CommPortIdentifier.getPortIdentifiers();
		Vector<String> portList = new Vector();
		String[] b = {"", "1200", "9600", "19200", "38400", "57600", "115200"};
		System.out.println("Very good");
		while(_portList.hasMoreElements()) {
			CommPortIdentifier id = (CommPortIdentifier) _portList.nextElement();
			portList.add(id.getName());
			System.out.println(id.getName());
		}
		portSelect2 = new JComboBox(portList);
		baudRateSelect2 = new JComboBox(b);
		
		portName2 = (String) portSelect2.getSelectedItem();
		portSelect2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					portName2 = (String) e.getItem();
					portSelect2.setEditable(false);
					
				}
			}
		});
		
		baudRateSelect2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					baudRate2 = (String) e.getItem();
					baudRateSelect2.setEditable(false);
					
				}
			}
		});
	}
	
	// Set the dum board
	public void setDumBoard() {
		dumBoard.setPreferredSize(new Dimension(500, 500));
	}
	// Set the drawing board
	public void setDrawingBoard() {
		drawingBoard.setPreferredSize(new Dimension(500, 500));
		drawingBoard.addMouseListener(mouseHandler);
	}

	// Set the data board
	public void setDataBoard () {
		dataBoard.add(new JScrollPane(dataContent));
		dataContent.setText("Here are data received:\n");
		dataContent.setLineWrap(true);
		dataBoard.setPreferredSize(new Dimension(200, 500));
	}
	
	// Combine the data board and the drawing board
	public void combine () {
		complexBoard.setLayout(new FlowLayout());
		complexBoard.add(dumBoard);
		complexBoard.add(dataBoard);
	}
	
	// Manually display the floor plan
	public void manual() {
		System.out.println(portName);
		System.out.println(baudRate);
		if (portName==null || baudRate==null || baudRate.equals("")) {
			consoleContent.setText("The port or the baud rate is not legal");
			System.out.println("gotcha!");
			return;
		} else {
			int baudrate = Integer.parseInt(baudRate);
			int status = 0; 

			// Create a handler
			if(!first) dumBoard.remove(drawingBoard);
			try {
				drawHandler = new Handler(consoleContent);
				
				status = drawHandler.openCommPort(portName, baudrate, "Handler");
				System.out.println("status = "+status);
				
				if (status == 0) {
					drawHandler.receiveBytes(dataContent);
					x = drawHandler.valueOfX();
					y = drawHandler.valueOfY();
					drawingBoard = new DrawingBoard(x, y, scale);
					setDrawingBoard();
					dumBoard.add(drawingBoard);
					frame.pack();
					first = false;
			 	}
				drawHandler.closePort();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		
		}
	}
	
	// Auto mode
	public void auto() {
		System.out.println(portName);
		System.out.println(baudRate);
		if (portName==null || baudRate==null || baudRate.equals("")) {
			consoleContent.setText("The port or the baud rate is not legal");
			System.out.println("gotcha!");
			return;
		} else {
			bRate = Integer.parseInt(baudRate);

			// Create a handler
			timer = new Timer(1000, timerAction);
			timer.start();
		}
	}

	// Set the input board
	public void setInputBoard1() throws IOException {
		setInput();
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		 		// Stop the timer
				timer.stop();
			}
		});
		
		// Add action for the START button
		/* the main logic and operation are here */
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(mode.equals("Manual")) {
					manual();
				} else if (mode.equals("Auto")) {
					auto();
				}
			}
		});
		
		// Allocate the elements
		inputBoard.add(scanner);
		inputBoard.add(selectMode);
		inputBoard.add(modeSelect);
		inputBoard.add(selectComPort);
		inputBoard.add(portSelect);
		inputBoard.add(selectBaudRate);
		inputBoard.add(baudRateSelect);
		inputBoard.add(selectScale);
		inputBoard.add(scaleSelect);
		inputBoard.add(cm2);
		inputBoard.add(button);
		inputBoard.add(stop);
	}
	
	//
	public void setInputBoard2() throws IOException {
		setInput2();
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		 		// Close the port
				controlHandler.closePort();
				consoleContent.setText("The port for motor controler is closed!");
			}
		});
		
		// Add action for the START button
		/* the main logic and operation are here*/
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(portName2);
				System.out.println(baudRate2);
				if (portName2==null || baudRate2==null || baudRate2.equals("")) {
					consoleContent.setText("The port or the baud rate is not legal");
					System.out.println("gotcha!");
					return;
				} else {
					int baudrate = Integer.parseInt(baudRate2);
					int status = 0; 

					// Create a handler
					try {
						controlHandler = new Handler(consoleContent);

						status = controlHandler.openCommPort(portName2, baudrate, "Controller");
						System.out.println("status = "+status);
						
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
			}
		});		
		
		// Allocate the elements
		portSelect2.setPreferredSize(new Dimension(200, 30));
		baudRateSelect2.setPreferredSize(new Dimension(200, 30));
		inputBoard2.add(selectComPort2);
		inputBoard2.add(portSelect2);
		inputBoard2.add(selectBaudRate2);
		inputBoard2.add(baudRateSelect2);
		inputBoard2.add(open);
		inputBoard2.add(close);
	}
	
	// Set the command board
	public void setCmdBoard() {
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Controller(controlHandler.getPort(), cmd.getText())).start();
				cmd.setText("");
			}
		});
		cmdBoard.setLayout(new FlowLayout(FlowLayout.LEFT));
		cmdBoard.setPreferredSize(new Dimension(700, 50));
		cmd.setPreferredSize(new Dimension(200, 30));
		cmdBoard.add(command);
		cmdBoard.add(cmd);
		cmdBoard.add(cm);
		cmdBoard.add(send);
	}

	// Set the size of the console board
	public void setConsoleBoard() {
		consoleContent.setVerticalAlignment(SwingConstants.TOP);
		consoleContent.setPreferredSize(new Dimension(700, 50));
		consoleBoard.add(consoleContent);
	}
	public static void main(String[] args) throws IOException {
		new Robot();
	}

}
