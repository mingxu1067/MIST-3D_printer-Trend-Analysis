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
import java.util.LinkedList;
import java.util.ArrayList;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.gtranslate.Audio;
import com.gtranslate.Language;
import com.gtranslate.Translator;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.WordDictionary;

import java.nio.file.Paths;
import java.util.Random;

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


public class TrendAnalysis {

	private Map<String, List<String>> areaEncoding;
	private Map<String, Date> paperTime;
	private Map<String, List<String>> areasOfPaper;
	private Map<String, List<String>> trend;
	private Map<String, List<String>> trendWithArea;

	private List<SegToken> tokenList;
    private JiebaSegmenter jieba;
    private Translator translate;

    private static ILexicalDatabase wordNetDB;

	public TrendAnalysis() {
		areaEncoding = new HashMap<String, List<String>>();
		paperTime = new HashMap<String, Date>();
		areasOfPaper = new HashMap<String, List<String>>();
		trend = new HashMap<String, List<String>>();
		trendWithArea = new HashMap<String, List<String>>();
		jieba = new JiebaSegmenter();
		tokenList = new ArrayList<SegToken>();
		translate = Translator.getInstance();

		wordNetDB = new NictWordNet();
		loadWordDBtoJiabe();
	}

	public void getPaperDataFromDB (String SQL_URL,String user, String password) throws ParseException{

		Connection SQLcon;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			SQLcon = DriverManager.getConnection("jdbc:mysql://"+SQL_URL+"?useUnicode=true&characterEncoding=UTF8", user, password);

			String query = "select No, Date from papers";
		    Statement st = SQLcon.createStatement();
			ResultSet rs = st.executeQuery(query);


			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			while(rs.next()){
				if ( !rs.getString("Date").equals("-1")) {
					Date tempDate = sdf.parse(rs.getString("Date"));
					paperTime.put( rs.getString("No"), tempDate);
				}
			}

			query = "select PaperNo, Area from PaperClassify";
			
			rs = st.executeQuery(query);

			while(rs.next()){
				if ( areasOfPaper.containsKey(rs.getString("PaperNo")) ) {
					areasOfPaper.get(rs.getString("PaperNo")).add(rs.getString("Area"));
				}
				else {
					areasOfPaper.put(rs.getString("PaperNo"), new LinkedList<String>());
					areasOfPaper.get(rs.getString("PaperNo")).add(rs.getString("Area"));	
				}
			}

			query = "select Encode, EnglishName from AreaEncodeMap";
			
			rs = st.executeQuery(query);

			while(rs.next()){
				areaEncoding.put(rs.getString("Encode"), new ArrayList<String>());
				char[] name = rs.getString("EnglishName").toCharArray();
				int tmepIdx = 0;
				for(int i = 0; i < name.length; i++) {
					if (Character.isUpperCase(name[i]) && i > 0) {
						areaEncoding.get(rs.getString("Encode")).add(rs.getString("EnglishName").substring(tmepIdx,i));
						tmepIdx = i;
					}
				}
				areaEncoding.get(rs.getString("Encode")).add(rs.getString("EnglishName").substring(tmepIdx));

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

	public Map<String, List<String>> trendAnalysis (int startYear, int endYear) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		// because Using the Date compare (after, before), the date set from startYear-1 12/31 to endYear+1 01/01
		Date startDate = sdf.parse(startYear-1 + "1231"); 
		Date endDate = sdf.parse(endYear+1 + "0101");


		sdf = new SimpleDateFormat("yyyy/MM/dd");
		for (Map.Entry<String, Date> pt : paperTime.entrySet()) {
			if (pt.getValue().after(startDate) && pt.getValue().before(endDate)) {
				if (areasOfPaper.containsKey(pt.getKey())) {
					for (String area : areasOfPaper.get(pt.getKey())) {
						if (trend.containsKey(sdf.format(pt.getValue()))) {
							trend.get(sdf.format(pt.getValue())).add(area);
						}
						else {
							trend.put(sdf.format(pt.getValue()), new LinkedList<String>());
							trend.get(sdf.format(pt.getValue())).add(area);
						}
					}
				}
			}
		}


		WS4JConfiguration.getInstance().setMFS(true);
		WuPalmer wordnet = new WuPalmer(wordNetDB);

		for (Map.Entry<String, List<String>> e : trend.entrySet()) {
			trendWithArea.put(e.getKey(), new LinkedList<String>());
			System.out.println(e.getKey());
			for (String s : e.getValue()){
				tokenList = jieba.process(s, SegMode.INDEX);
				for (SegToken st : tokenList) {
					String word = translate.translate(st.word, Language.CHINESE ,Language.ENGLISH);
					System.out.println(word);
					for (Map.Entry<String, List<String>> area : areaEncoding.entrySet()) {
						for (String eName : area.getValue()) {
							if ( wordnet.calcRelatednessOfWords(eName, word) >= 0.7 ){
								trendWithArea.get(e.getKey()).add(area.getKey());
							}
						}
					}

					Random rand = new Random();
					try {
					    Thread.sleep(rand.nextInt(8000) + 8000);
					} catch(InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
				}
			}
		}

		return trendWithArea;
	}


	private void loadWordDBtoJiabe() {
        WordDictionary dictAdd = WordDictionary.getInstance();
        dictAdd.loadUserDict(Paths.get("./JiebaDB/dict.txt"));
    }


}