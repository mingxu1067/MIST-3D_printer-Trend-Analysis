import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.sql.SQLException; 
import java.sql.Statement; 

import java.util.ArrayList;
import java.util.List;

public class NounFetch{
	private MaxentTagger tagger;
	private List<Integer> No;
	private List<String> Abstract;

	public NounFetch(){
		tagger =  new MaxentTagger("models/english-left3words-distsim.tagger");
		Abstract = new ArrayList<String>();
		No = new ArrayList<Integer>();
	}

	/*
	* Fetch all abstract of papers from MySQL (papers table).
	*/
	public void GetAbstractFromSQL(String SQL_URL,String user, String password){
		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "select No, Abstract from papers";
		    Statement st = SQLcon.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			while(rs.next()){
				No.add(rs.getInt("No"));
				Abstract.add(rs.getString("Abstract"));
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

	/*
	* Fetch all Noun from Abstract of papers.
	*/
	public void Tag_And_FetchNoun(){

		String tempString = "";
		for(int i=0;i<Abstract.size();i++){

			tempString = tagger.tagString(Abstract.get(i));
			char[] tempCharArray = tempString.toCharArray();


			int WordHeadIdx = 0;
			int WordEndIdx = 0;
			int TagHeadIdx = 0;
			int TagEndIdx = 0;

			boolean GetNoun = false;
			String tempWord = "";
			String tempTag = "";
			for(int idx=0;idx<tempCharArray.length;idx++){

				if(tempCharArray[idx] == '_'){

					WordEndIdx = idx - 1;
					TagHeadIdx = idx + 1;
					if(tempCharArray[idx+1] == 'N'){
						GetNoun = true;
					}
				}

				if(tempCharArray[idx] == ' '){

					TagEndIdx = idx -1;
					if(GetNoun){

						for(int store_idx = WordHeadIdx;store_idx<=WordEndIdx;store_idx++)
							tempWord += tempCharArray[store_idx];

						for(int store_idx = TagHeadIdx;store_idx<=TagEndIdx;store_idx++)
							tempTag += tempCharArray[store_idx];

						storeNounToSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter",
										No.get(i),tempWord,tempTag);
						tempWord = "";
						tempTag = "";
						GetNoun = false;

					}

					WordHeadIdx = idx + 1;

				}

			}

		}
	}

	public void storeNounToSQL(String SQL_URL,String user, String password, int No, String Word, String Tag){

		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

		    String query = "insert into AbstractNoun(PaperNo,Noun,tag)" + " values (?,?,?)";
		    PreparedStatement preparedStmt = SQLcon.prepareStatement(query);

		    preparedStmt.setInt(1, No);
		    preparedStmt.setString(2, Word);
		    preparedStmt.setString(3, Tag);
			
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