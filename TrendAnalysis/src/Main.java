import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import java.util.List;
import java.util.LinkedList;
public class Main{

	public static void main(String[] args) throws Exception{

		/**
		*	Fetch Nouns from abstract of all papers;
		*	Tool : Stanford parser-tagger
		*/
		//NounFetch NFetcher = new NounFetch();
		//NFetcher.GetAbstractFromSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
		//NFetcher.Tag_And_FetchNoun();

		/**
		*	Calculate all words TF-IDF value in every paper.
		*/
		//CalTFIDF tfidf = new CalTFIDF();
		//tfidf.getWordFromDB("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");

		/**
		*	Calculate all-pair word distance of each word in abstract of all papers;
		*	Tool : ws4j.jar, WordNet
		*/
		// WordsCluster wC = new WordsCluster();
		// wC.getWordFromSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");


		/**
		*	Map paper to area through by noun classification online service.
		*	URL : http://terms.naer.edu.tw/search/?q=%22computers%22&field=text
		*/
		//PaperClassify classifier = new PaperClassify();
		//classifier.classify();

		TrendAnalysis trender = new TrendAnalysis();
		trender.getPaperDataFromDB("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
		Map<String, List<String>> trend = trender.trendAnalysis(2011, 2015);

	}
}