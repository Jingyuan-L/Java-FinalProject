package finalproject.client;

import java.util.ArrayList;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import finalproject.client.ClientInterface.ComboBoxItem;
import finalproject.db.DBInterface;
import finalproject.entities.Person;
import finalproject.server.Server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientInterface extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_PORT = 8001;
	
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 400;
	final int AREA_ROWS = 10;
	final int AREA_COLUMNS = 40;

	JComboBox peopleSelect;
	JFileChooser jFileChooser;
	Socket socket;
	int port;
	
	JMenuBar menuBar;
	JLabel dbName;
	JLabel connNameLabel;
	JButton openConnButton;
	JButton closeConnButton;
	JButton sendButton;
	JButton queryButton;
	JTextArea textArea;
	
	Connection connection;
	ResultSet resultSet ;
	
	public ClientInterface() {
		this(DEFAULT_PORT);
	}
	
	public ClientInterface(int port) {
		this.port = port;
		
		this.setSize(ClientInterface.FRAME_WIDTH, ClientInterface.FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(0, 1));
		
		jFileChooser = new JFileChooser();
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		
		JLabel staticLabel1 = new JLabel("Active DB: ");
		dbName = new JLabel("<None>");
		JLabel staticLabel2 = new JLabel("Active Connection: ");
		connNameLabel = new JLabel("<None>");
		openConnButton = new JButton("Open Connection");
		closeConnButton = new JButton("Close Connection");
		sendButton = new JButton("Send Data");
		queryButton = new JButton("Query DB Data");
		textArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
	    textArea.setEditable(false);
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    
	    peopleSelect = new JComboBox();
	    clearComboBox();
	    
	    JPanel controlPanel = new JPanel(new GridLayout(0, 1));
	    this.add(controlPanel);
	    this.add(scrollPane);
	    
	    JPanel panel1 = new JPanel();
	    JPanel panel2 = new JPanel();
	    JPanel panel3 = new JPanel();
	    JPanel panel4 = new JPanel();
	    JPanel panel5 = new JPanel();
	    controlPanel.add(panel1);
	    controlPanel.add(panel2);
	    controlPanel.add(panel3);
	    controlPanel.add(panel4);
	    controlPanel.add(panel5);
	    
	    panel1.add(staticLabel1);
	    panel1.add(dbName);
	    panel2.add(staticLabel2);
	    panel2.add(connNameLabel);
	    panel3.add(peopleSelect);
	    panel4.add(openConnButton);
	    panel4.add(closeConnButton);
	    panel5.add(sendButton);
	    panel5.add(queryButton);
	    
	    openConnButton.addActionListener(new OpenConnectionListener());
	    closeConnButton.addActionListener((e) -> { try { socket.close(); textArea.append("connection closed\n");} catch (Exception e1) {System.err.println("error"); }});
		sendButton.addActionListener(new SendButtonListener());
		queryButton.addActionListener(new QueryDBListener());
		
		
	}  
	
	
	private void fillComboBox() throws SQLException {
		List<ComboBoxItem> l = getNames();
		if (l.isEmpty()) {
			l.add(new ComboBoxItem(1, "<Empty>"));
		}
		peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));
		}

	private void clearComboBox() {
		List<ComboBoxItem> l = new ArrayList();
		l.add(new ComboBoxItem(1, "<Empty>"));
		peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));
	}
	public JMenu createFileMenu(){
		JMenu menu = new JMenu("File");
		menu.add(createFileOpenItem());
		menu.add(createFileExitItem());
		return menu;
	}
   
	private JMenuItem createFileOpenItem() {
		
		JMenuItem item = new JMenuItem("Open DB");
		
		class OpenDBListener implements ActionListener{
			
	         	public void actionPerformed(ActionEvent event){ 
	         		
	         		int returnVal = jFileChooser.showOpenDialog(getParent());
	         		if (returnVal == JFileChooser.APPROVE_OPTION) {
	         			System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
	         			String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
	         			try {
	         				connection = DriverManager.getConnection("jdbc:sqlite:" + dbFileName);
	         				dbName.setText(dbFileName.substring(dbFileName.lastIndexOf("/")+1));
	         				//queryButtonListener.setConnection(conn);
	         				//clearComboBox();
	         				fillComboBox();
	         				} catch (Exception e ) {
	         					System.err.println("error connection to db: "+ e.getMessage());
	         					e.printStackTrace();
	         					dbName.setText("<None>");
	         					clearComboBox();
	         				}
	         		}
	         		
	         	}
		}
		item.addActionListener(new OpenDBListener());
		return item;
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
	
	
	class OpenConnectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				socket = new Socket("localhost", port);
				textArea.append("connected\n");
				connNameLabel.setText("localhost: " + port);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				textArea.append("connection Failure\n");
			}
		}
	}
	
	
	class SendButtonListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {

			if (socket == null || socket.isClosed()) {
				textArea.append("Socket is closed! Please open connection!\n");
				return;
			}
	        try {
				
	        	// responses are going to come over the input as text, and that's tricky,
	        	// which is why I've done that for you:
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				// now, get the person on the object dropdownbox we've selected
				ComboBoxItem personEntry = (ComboBoxItem)peopleSelect.getSelectedItem();
				if (personEntry.getName() == "<Empty>") {
					textArea.append("Name is Empty!\n");
					return;
				}
				
				// That's tricky which is why I have included the code. the personEntry
				// contains an ID and a name. You want to get a "Person" object out of that
				// which is stored in the database
				Person person = null;
				int id = 0;
				if (personEntry.getName() != "<None>") {
					PreparedStatement query = connection.prepareStatement("SELECT * FROM People WHERE id = " + String.valueOf(personEntry.getId()));
					resultSet = query.executeQuery();
					String first = resultSet.getString(1);
					String last = resultSet.getString(2);
					int age = resultSet.getInt(3);
					String city = resultSet.getString(4);
					boolean sent = true;
					id = resultSet.getInt(6);
					person = new Person(first, last, age, city, sent, id);

				} else {
					textArea.append("Person can not be None!\n");
				}
				
				// Send the person object here over an output stream that you got from the socket.
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				outputStream.writeObject(person);
				
				String response = br.readLine();
				if (response.contains("Success")) {
					System.out.println("Success");
					// what do you do after we know that the server has successfully
					// received the data and written it to its own database?
					// you will have to write the code for that.
					textArea.append("Person " + id + " sent successfully!\n");
					PreparedStatement query = connection.prepareStatement("UPDATE People SET sent=1 WHERE id=" + String.valueOf(id));
					query.executeUpdate();
					clearComboBox();
					fillComboBox();
				} else {
					System.out.println("Failed");
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}catch (SQLException e1) {
				e1.printStackTrace();
			}
		}	
	}
	
	class QueryDBListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (connection == null) {
				textArea.append("Please connect to database!\n");
				return;
			}
			try {
				PreparedStatement query = connection.prepareStatement("SELECT * FROM People;");
				resultSet = query.executeQuery();
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
		
	}
	
	private List<ComboBoxItem> getNames() throws SQLException {
		List<ComboBoxItem> nameList = new ArrayList();
		PreparedStatement query = connection.prepareStatement("SELECT * FROM People WHERE sent=0;");
		resultSet = query.executeQuery();
		while (resultSet.next()) {
			Object first = resultSet.getObject(1);
			Object last = resultSet.getObject(2);
			Object id = resultSet.getObject(6);
			nameList.add(new ComboBoxItem(Integer.parseInt(id.toString()), first.toString() + " " + last.toString()));
		}
		return nameList;
	}
	
	// a JComboBox will take a bunch of objects and use the "toString()" method
	// of those objects to print out what's in there. 
	// So I have provided to you an object to put people's names and ids in
	// and the combo box will print out their names. 
	// now you will want to get the ComboBoxItem object that is selected in the combo box
	// and get the corresponding row in the People table and make a person object out of that.
	class ComboBoxItem {
		private int id;
		private String name;
		
		public ComboBoxItem(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getId() {
			return this.id;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String toString() {
			return this.name;
		}
	}
	

	
	public static void main(String[] args) {
		ClientInterface ci = new ClientInterface();
		ci.setVisible(true);
	}
}
