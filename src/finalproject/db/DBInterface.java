package finalproject.db;
import java.sql.*;
import java.util.ArrayList;

import finalproject.entities.Person;

public class DBInterface {

	/* implementing or using this class isn't strictly required, but
	 * you might want to abstract some of the interactions with and queries
	 * to the database to a separate class.
	 */
	
	Connection conn;
	
	public DBInterface() {
		
	}
	
	public Connection getConn() {
		return this.conn;
	}
	
	public void setConnection() throws SQLException {
		//this.conn =
	}
	
}
