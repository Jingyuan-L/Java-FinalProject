package finalproject.server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import finalproject.db.DBInterface;
import finalproject.entities.Person;

public class Server extends JFrame implements Runnable {

	public static final int DEFAULT_PORT = 8001;
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 800;
	final int AREA_ROWS = 10;
	final int AREA_COLUMNS = 40;
	
	JMenuBar menuBar;
	JLabel dbLabel;
	JButton queryButton;
	JTextArea textArea;
    
	PreparedStatement insertStatement, queryStatement; 
	ResultSet resultSet ;
	Connection connection;
	
	String successFlag = "Failed\n";
	int clientNo = 0;
	
	public Server() throws IOException, SQLException {
		this(DEFAULT_PORT, "server.db");
	}
	
	public Server(String dbFile) throws IOException, SQLException {
		this(DEFAULT_PORT, dbFile);
	}

	public Server(int port, String dbFile) throws IOException, SQLException {

		this.setSize(Server.FRAME_WIDTH, Server.FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
	    menuBar.add(createFileMenu());
	    textArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
	    textArea.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    dbLabel = new JLabel("DB: server.db");
	    queryButton = new JButton("Query DB");
	    
	    JPanel northPanel = new JPanel();
	    northPanel.add(dbLabel);
	    northPanel.add(queryButton);
	    this.add(northPanel, BorderLayout.NORTH);
	    this.add(scrollPane);
	    
	    
	    
	    Thread t = new Thread(this);
	    t.start();
	}
	    
	    
	public JMenu createFileMenu() {
		JMenu menu = new JMenu("File");
	    menu.add(createFileExitItem());
	    return menu;
	}
	
	public JMenuItem createFileExitItem() {
	      JMenuItem item = new JMenuItem("Exit");      
	      class MenuItemListener implements ActionListener
	      {
	         public void actionPerformed(ActionEvent event)
	         {
	            System.exit(0);
	         }
	      }      
	      ActionListener listener = new MenuItemListener();
	      item.addActionListener((e) -> System.exit(0));
	      return item;
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:server.db");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		textArea.append("Connected to server.db successfully!\n");
		
		/* sets up the prepared statement for SQL */
		String querySQL = "SELECT * FROM People";
		try {
			queryStatement = connection.prepareStatement(querySQL);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String insertSQL = "INSERT INTO People (first, last, age, city, sent, id) " + "Values (?,?,?,?,?,?)";
		try {
			insertStatement = connection.prepareStatement(insertSQL);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//add ActionListener to queryButton
		queryButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					resultSet = queryStatement.executeQuery();
					ResultSetMetaData mateData = resultSet.getMetaData();
					for(int i = 1; i <= 6; i++) {
						textArea.append( mateData.getColumnName(i) + "\t");
					}
					textArea.append("\n");
					for(int i = 1; i <= 6; i++) {
						textArea.append("-----" + "\t");
					}
					textArea.append("\n");
					while(resultSet.next()) {
						textArea.append(resultSet.getString(1) + "\t");
						textArea.append(resultSet.getString(2) + "\t");
						textArea.append(resultSet.getString(3) + "\t");
						textArea.append(resultSet.getString(4) + "\t");
						textArea.append(resultSet.getString(5) + "\t");
						textArea.append(resultSet.getString(6) + "\t");
						textArea.append("\n");
					}
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		try {
	        // Create a server socket
	        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
	        textArea.append("Listening on port 8001\n");
	    
	        while (true) {
	          // Listen for a new connection request
	          Socket socket = serverSocket.accept();
	    
	          // Increment clientNo
	          clientNo++;
	          
	          textArea.append("Starting thread for client " + clientNo +
	              " at " + new Date() + '\n');

	            // Find the client's host name, and IP address
	            InetAddress inetAddress = socket.getInetAddress();
	            textArea.append("Client " + clientNo + "'s host name is "
	              + inetAddress.getHostName() + "\n");
	            textArea.append("Client " + clientNo + "'s IP Address is "
	              + inetAddress.getHostAddress() + "\n");
	          
	          // Create and start a new thread for the connection
	          new Thread(new HandleAClient(socket, clientNo)).start();
	        }
	      }
	      catch(IOException ex) {
	        System.err.println(ex);
	      }
		
		

		
		
	}
	
	class HandleAClient implements Runnable {
	    private Socket socket; // A connected socket
	    private int clientNum;
	    
	    /** Construct a thread */
	    public HandleAClient(Socket socket, int clientNum) {
	      this.socket = socket;
	      this.clientNum = clientNum;
	    }

	    /** Run a thread */
	    public void run() {
	      try {
	    	  // Continuously serve the client
	    	  while (true) {
		    	  
		    	  // Create data input and output streams
		    	  ObjectInputStream inputFromClient = new ObjectInputStream(
		          socket.getInputStream());
		    	  ObjectOutputStream outputToClient = new ObjectOutputStream(
		          socket.getOutputStream());

	    		  // Receive person object from the client
	    		  Object object = null;
	    		  try {
	    			  object = inputFromClient.readObject();
	        	  	} catch (ClassNotFoundException e) {
	        	  		e.printStackTrace();
	        	  		outputToClient.writeObject(successFlag);
	        	  	}
	    		  Person p = (Person) object;
	    		  textArea.append("got person " + p.toString());
	    		  //insert the person object into server.db
	    		  try {
					insertStatement.setString(1, p.getFirst());
					insertStatement.setString(2, p.getLast());
					insertStatement.setInt(3, p.getAge());
					insertStatement.setString(4, p.getCity());
					insertStatement.setBoolean(5, true);
					insertStatement.setInt(6, p.getId());
					insertStatement.execute();
					textArea.append("Inserted Successfully\n");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					outputToClient.writeObject(successFlag);
				}
	    		  // Send successFlag back to the client
	    		  successFlag = "Success\n";
	    		  outputToClient.writeObject(successFlag);
	          
	        
	    	  }
	        }
	      catch(IOException ex) {
	        ex.printStackTrace();
	      }
	    }
	}
	
	public static void main(String[] args) {

		Server sv;
		try {
			sv = new Server("server.db");
			sv.setVisible(true);
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
