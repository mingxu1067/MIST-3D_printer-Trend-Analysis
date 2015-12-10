import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class CalTFIDF{

	private int numOfDoc;
	private Map<String,Integer> numPaperWords;
	private Map<String, Map<String,WordTerm>> wordCollection;
	private Map<String, Map<String, Double>> tfidfOfTerm;

	public CalTFIDF(){
		numOfDoc = 0;
		numPaperWords = new HashMap<String,Integer>();
		wordCollection = new HashMap<String, Map<String,WordTerm>>();
		tfidfOfTerm = new HashMap<String, Map<String, Double>>();
	}

	private class paperTermScore{
		public int score;
		public String paperNo;

		public paperTermScore(String paperNo, int score){
			this.paperNo = paperNo;
			this.score = score;
		}
	}

	private class WordTerm{
		public int num;
		public WordTerm(){
			num = 1; 
		}
	}
	
	public void getWordFromDB(String SQL_URL,String user, String password){
		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "select count(Noun), PaperNo from AbstractNoun group by PaperNo";
		    Statement st = SQLcon.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			while(rs.next()){
				numOfDoc++;
				numPaperWords.put(rs.getString("PaperNo"), new Integer(rs.getInt("count(Noun)")));
			}

			// System.out.println(numOfDoc);
			// for (Map.Entry<String,Integer> m : numPaperWords.entrySet()) {
			// 	System.out.println(m.getKey() + " : " + m.getValue().toString());
				
			// }
			
			query = "select Noun, PaperNo from AbstractNoun";
			rs = st.executeQuery(query);

			while(rs.next()){

				if(wordCollection.containsKey(rs.getString("Noun"))){
					if (wordCollection.get(rs.getString("Noun")).containsKey(rs.getString("PaperNo")))
						wordCollection.get(rs.getString("Noun")).get(rs.getString("PaperNo")).num ++;
					else
						wordCollection.get(rs.getString("Noun")).put(rs.getString("PaperNo"),new WordTerm());
				}
				else{
					wordCollection.put(rs.getString("Noun"), new HashMap<String,WordTerm>());
					wordCollection.get(rs.getString("Noun")).put(rs.getString("PaperNo"),new WordTerm());
				}
			}
			// for (Map.Entry<String,Map<String,WordTerm>> m : wordCollection.entrySet()) {
			// 	for (Map.Entry<String,WordTerm> n : m.getValue().entrySet())
			// 		System.out.println(m.getKey() + " : " + n.getKey() + " - " + n.getValue().num);
				
			// }

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

		TFIDF();

	}


	public void TFIDF (){

		for (Map.Entry<String, Map<String,WordTerm>> e : wordCollection.entrySet()){

			tfidfOfTerm.put(e.getKey(), new HashMap<String, Double>());

			for (Map.Entry<String,WordTerm> n : e.getValue().entrySet()){
				double tf = (double)(n.getValue().num) / (double)numPaperWords.get(n.getKey());
				double idf = (double)e.getValue().size() / (double)numOfDoc;

				tfidfOfTerm.get(e.getKey()).put(n.getKey(),(tf / idf));
			}
		}

		// for (Map.Entry<String, Map<String, Double>> e : tfidfOfTerm.entrySet()){

		// 	System.out.println("!!!" + e.getKey() + "!!!");

		// 	for (Map.Entry<String, Double> n : e.getValue().entrySet()){
		// 			System.out.println(n.getKey() + " : " + n.getValue().doubleValue());
		// 	}
		// }

		StoreToDB("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
	}

	public void StoreToDB (String SQL_URL,String user, String password){

		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "insert into TFIDF(Word,PaperNo,tfidfValue)" + " values (?,?,?)";
		    PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

		    for (Map.Entry<String, Map<String, Double>> e : tfidfOfTerm.entrySet()){
				for (Map.Entry<String, Double> n : e.getValue().entrySet()){
					preparedStmt.setString(1, e.getKey());
		    		preparedStmt.setInt(2, Integer.parseInt(n.getKey()));
		    		preparedStmt.setDouble(3, n.getValue().doubleValue());
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

