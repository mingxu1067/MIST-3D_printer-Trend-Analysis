import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 

import java.util.*;
import java.text.*;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import java.io.IOException;

public class WordsCluster{

	private static ILexicalDatabase wordNetDB;

	private List<String> words;
	private Map<String, List<WordAttr>> wordDisTable;

	private class WordAttr{
		public String word;
		public double dis;

		public WordAttr(String word , double dis ){
			this.word = word;
			this.dis = dis;
		}
	}


	public WordsCluster(){
		words = new ArrayList<String>();
		wordDisTable = new HashMap<String, List<WordAttr>>();
		wordNetDB = new NictWordNet();

	}
	public void getWordFromSQL(String SQL_URL,String user, String password){

		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "select Word from TFIDF group by Word";
		    Statement st = SQLcon.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			while(rs.next()){
				words.add(rs.getString("Word"));
			}

			// for (String s : words)
			// 	System.out.println(s);
			

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

		CalWordDis();

	}

	public void CalWordDis(){

		WS4JConfiguration.getInstance().setMFS(true);
		WuPalmer wordnet = new WuPalmer(wordNetDB);


		int count = 1;
		for (String wd : words){
			if(count > 1000) {
				storeToSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
				count = 1;
				wordDisTable.clear();
				System.out.println("================================================");
			}

			System.out.println("The Word : " + count++ + " - " + wd.toLowerCase());

			for (String s : words){
				try {
					double dis = wordnet.calcRelatednessOfWords(wd.toLowerCase(),s.toLowerCase());
					if (dis != 1.7976931348623157E308 || dis <= 1.0) { //Error Dis
						if (wordDisTable.containsKey(wd.toLowerCase())) {
							wordDisTable.get(wd.toLowerCase()).add(new WordAttr(s.toLowerCase(),dis));
						}
						else {
							wordDisTable.put(wd.toLowerCase(), new LinkedList<WordAttr>());
							wordDisTable.get(wd.toLowerCase()).add(new WordAttr(s.toLowerCase(),dis));	
						}
					}
					throw (new IOException());
				}
				catch (IOException ioe){}
			}
		}

	}

	public void storeToSQL(String SQL_URL,String user, String password){

		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "insert into WordDis(Word1,Word2,dis)" + " values (?,?,?)";
		    PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

		    for (Map.Entry<String, List<WordAttr>> e : wordDisTable.entrySet()) {
		    	for (WordAttr war : e.getValue()){
		    		preparedStmt.setString(1,e.getKey());
		    		preparedStmt.setString(2,war.word);
		    		preparedStmt.setDouble(3,war.dis);
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