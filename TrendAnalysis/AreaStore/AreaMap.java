import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement;

import java.util.*;

public class AreaMap {

	public static void main(String[] args) {
		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+"127.0.0.1:3306/papers3dprinter"+"?useUnicode=true&characterEncoding=UTF8", "3Dprinter", "3Dprinter");

		    String query = "insert into AreaEncodeMap(Encode,ChineseName,EnglishName)" + " values (?,?,?)";
		    PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

		    Scanner k = new Scanner(System.in);
		    String encode = "";
		    String cn = "";
		    String en = "";

		    while (true) {
		    	encode = k.nextLine();
		    	cn = k.nextLine();
		    	en = k.nextLine();

		    	preparedStmt.setString(1, encode);
		    	preparedStmt.setString(2, cn);
		    	preparedStmt.setString(3, en);
		    	preparedStmt.execute();
		    }

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