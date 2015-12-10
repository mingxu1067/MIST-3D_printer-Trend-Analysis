import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import java.util.List;
import java.util.ArrayList;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


public class PaperClassify {

	public Map<String, List<String>> resultOfPaperClassification;

	//http://terms.naer.edu.tw/search/?q=Test&field=text
	private String onlineService = "http://terms.naer.edu.tw/search/?";
	private String para = "";

	private Map<String, List<String>> wordsOfPaper;

	private Document serveicResult;

	public PaperClassify(){
		wordsOfPaper = new HashMap<String, List<String>>();
		resultOfPaperClassification = new HashMap<String, List<String>>();
	}

	public void classify() throws Exception{

		getWordFromDB("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
		connectToService();
		storeToSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
	}


	public void getWordFromDB(String SQL_URL,String user, String password){
		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

			String query = "select Noun, PaperNo from AbstractNoun";
		    Statement st = SQLcon.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			rs = st.executeQuery(query);

			while(rs.next()){
				if (wordsOfPaper.containsKey(rs.getString("PaperNo"))) {
					wordsOfPaper.get(rs.getString("PaperNo")).add(rs.getString("Noun"));
				}
				else {
					wordsOfPaper.put(rs.getString("PaperNo"), new ArrayList<String>());
					wordsOfPaper.get(rs.getString("PaperNo")).add(rs.getString("Noun"));
				}
			}

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

	public void connectToService() throws Exception{

		for (Map.Entry<String, List<String>> entry : wordsOfPaper.entrySet()) {
			for (String s : entry.getValue()){

				para = "q=" + s + "&field=text";
				serveicResult = Jsoup.connect(onlineService+para).userAgent("Mozilla").get();
				Elements category = serveicResult.select("div div ul li h4");
				//tempElm = serveicResult.select("div div ul li ul[class=category_list] li");
				int idx = 0;
				for (Element e : category) {
					if (e.text().equals("學術名詞")) {
						Elements resultElm = serveicResult.select("div div ul li ul[class=category_list] li");
						for (Element ee : resultElm.eq(idx) ){

							String area = ee.text().substring(0, ee.text().indexOf("("));

							if (resultOfPaperClassification.containsKey(entry.getKey()) ) {
								if( !resultOfPaperClassification.get(entry.getKey()).contains(area) ){
									resultOfPaperClassification.get(entry.getKey()).add(area);
									System.out.println( entry.getKey() +" : "+area);
								}
							}
							else {
								resultOfPaperClassification.put(entry.getKey(), new ArrayList<String>());
								resultOfPaperClassification.get(entry.getKey()).add(area);
								System.out.println( entry.getKey() +" : "+area);
							}

						}
						break;
					}

					idx++;
				}

			}
		}

	}

	public void storeToSQL(String SQL_URL,String user, String password){

		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "insert into PaperClassify(PaperNo,Area)" + " values (?,?)";
		    PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

		    for (Map.Entry<String, List<String>> entry : resultOfPaperClassification.entrySet()) {
		    	for (String s : entry.getValue()){
		    		preparedStmt.setInt(1,Integer.parseInt(entry.getKey()));
		    		preparedStmt.setString(2,s);
		    		preparedStmt.execute();
		    	}
		    }
			
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

