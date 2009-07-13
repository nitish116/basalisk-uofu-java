package basilisk;

import java.util.*;
import java.io.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExportToDatabase {

	public static final String _URL = "jdbc:mysql://localhost:3306/basilisk";
	public static final String _username = "root";
	public static final String _password = "i2b2";
	public static final String _prepInsertStmtSql = "INSERT INTO ctakes_lookup VALUES ('cFAKE','FAKE', ?, ?)";
	public static final String _prepDropTableStmtSql = "DROP TABLE IF EXISTS `basilisk`.`ctakes_lookup`;"; 
	public static final String _prepCreateTableStmtSql = "CREATE TABLE  `basilisk`.`ctakes_lookup` (\n" + 
																"  `cui` char(8) NOT NULL,\n" +
																"  `code` varchar(50) NOT NULL,\n" +  
																"  `first_word` text,\n" + 
																"  `full_text` text NOT NULL,\n" + 
																"  KEY `idx_first_word` (`first_word`(25))\n" + 
																") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
	
	private PreparedStatement _insertPrepStmt;
	private PreparedStatement _dropTablePrepStmt;
	private PreparedStatement _createTablePrepStmt;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExportToDatabase e2db = new ExportToDatabase("output/drugs-scat.lexicon");
	}
	
	public ExportToDatabase(String nameOfResultFile){
		//Read the list of words to put in the database
		List<String> wordsToPutInDatabase = readResults(nameOfResultFile);
		
		if(wordsToPutInDatabase == null)
			return;
		
		//Initialize database connection
		try{
			initializeDatabaseConnection();
		}
		catch (Exception e){
			System.err.println("Error initializing database: " + e.getMessage());
			return;
		}
		
		//Drop the table and create a new one
		try{
			clearTable();
		}
		catch (Exception e){
			System.err.println("Error clearing table: " + e.getMessage());
			System.err.println(_prepDropTableStmtSql);
			return;
		}
		
		//Put the words in the database
		for(String word: wordsToPutInDatabase){
			try	{
				putWordInDatabse(word);
			}
			catch(SQLException e){
				System.err.println("Error putting word into database: " + e.getMessage());
				return;
			}
		}
		
		//Create an index
		
	}

	private void clearTable() throws SQLException{
		_dropTablePrepStmt.execute();
		_createTablePrepStmt.execute();
	}

	private void initializeDatabaseConnection() throws Exception {
		//Load jdbc driver
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(_URL, _username, _password);
		
		_insertPrepStmt = conn.prepareStatement(_prepInsertStmtSql);
		_dropTablePrepStmt = conn.prepareStatement(_prepDropTableStmtSql);
		_createTablePrepStmt = conn.prepareStatement(_prepCreateTableStmtSql);
	}

	private void putWordInDatabse(String word) throws SQLException {
		_insertPrepStmt.setString(1, word);
		_insertPrepStmt.setString(2, word);
		_insertPrepStmt.execute();
	}

	private List<String> readResults(String nameOfResultFile) {
		File resultFile = new File(nameOfResultFile);
		ArrayList<String> result = new ArrayList<String>();
		
		if(!resultFile.exists()){
			System.err.println("Could not find file " + resultFile.getAbsolutePath());
			return null;
		}
		
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(resultFile));
			
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim().toLowerCase();
				if(!line.equals(""));
					result.add(line.trim().toLowerCase());
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
			return null;
		}


		return result;
	}

}
