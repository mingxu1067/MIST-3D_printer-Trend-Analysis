import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 

import java.util.regex.*;


public class PaperAttribute{
	private int ID ;
	private String Title;
	private String Authors;
	private String Publisher;
	private int Date;
	private String Abstract;
	private String Keywords;
	private String URL;


	public PaperAttribute(){
		ClearInfo();
	}

	public void setID(int ID){
		this.ID = ID;
	}

	public void setTitle(String Title){
		this.Title = Title;
	}

	public void setAuthors(String Authors){
		this.Authors = Authors;
	}

	public void setPublisher(String Publisher){
		this.Publisher = Publisher;
	}

	public void setDate(int Date){
		this.Date = Date;
	}

	public void setAbstract(String Abstract){
		Pattern pattern = Pattern.compile("^\\s*\\w*");
		Matcher matcher = pattern.matcher(Abstract);
		if(matcher.matches()){
			System.out.println("Abstract of the paper is empty.");
			System.out.println("Store the URL of paper to lostAbstract table in mysql");
			StoreEmptyAbstractURL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
		}
		else
			this.Abstract = Abstract;
	}

	public void setKeywords(String Keywords){
		this.Keywords = Keywords;
	}
	
	public void setURL(String URL){
		this.URL = URL;
	}


	public int getID(){
		return ID;
	}

	public String getTitle(){
		return Title;
	}

	public String getAuthors(){
		return Authors;
	}

	public String getPublisher(){
		return Publisher;
	}

	public int getDate(){
		return Date;
	}

	public String getAbstract(){
		return Abstract;
	}

	public String getKeywords(){
		return Keywords;
	}

	public String getURL(){
		return URL;
	}

	public void ClearInfo(){
		ID  = -1;
		Title = "N";
		Authors = "N";
		Publisher = "N";
		Date = -1;
		Abstract = "N";
		Keywords = "N";
		URL = "N";
	}

	public void StoreEmptyAbstractURL(String SQL_URL,String user, String password){
		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

			String query = " insert into lostAbstract (URL)" + " values (?)";
			PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

			preparedStmt.setString (1, URL);

		    preparedStmt.execute();
		    SQLcon.close();
		}
		catch (ClassNotFoundException e)
		{
		    System.out.println("DriverClassNotFound :" + e.toString());
		}
		catch (SQLException x)
		{
		    System.out.println("Exception :" + x.toString());
		}

	}

	public void StorePaperInfoToSQL(String SQL_URL,String user, String password){

		Connection SQLcon;
		try{

			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

			String query = " insert into papers (Title, Authors, Abstract, Publisher, Date, URL)"
        					+ " values (?, ?, ?, ?, ?, ?)";
			PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

		    preparedStmt.setString (1, Title);
		    preparedStmt.setString (2, Authors);
		    preparedStmt.setString (3, Abstract);
		    preparedStmt.setString (4, Publisher);
		    preparedStmt.setInt (5, Date);
		    preparedStmt.setString (6, URL);

		    preparedStmt.execute();

		    query = "select MAX(No) as No from papers";
		    Statement st = SQLcon.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
				ID = rs.getInt("No");

			st.close();

		    SQLcon.close();

		}
		catch (ClassNotFoundException e)
		{
		    System.out.println("DriverClassNotFound :" + e.toString());
		}
		catch (SQLException x)
		{
		    System.out.println("Exception :" + x.toString());
		}
	}

	public void StorePaperKeyToSQL(String SQL_URL,String user, String password){
		
		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = " insert into paperKeyword (No, Keyword)"
	        					+ " values (?, ?)";
			PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

			preparedStmt.setInt (1, ID);
		    preparedStmt.setString (2, Keywords);	   		   

		    preparedStmt.execute();
		    SQLcon.close();
		}
		catch (ClassNotFoundException e)
		{
		    System.out.println("DriverClassNotFound :" + e.toString());
		}
		catch (SQLException x)
		{
		    System.out.println("Exception :" + x.toString());
		}
	}

}