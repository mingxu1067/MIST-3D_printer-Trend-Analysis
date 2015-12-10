import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class JsoupToIEEE_xplore implements SearchPaperIF{
	private String IEEE_URL;
	private Document IEEE_Xplore_Crawler;
	private String Search_str;
	private Elements TempElm;	//For temp searching storge the elements
	private PaperAttribute TempPaperAtt;
	private int pageNUm;

	public JsoupToIEEE_xplore(String IEEE_search,String Searching) throws Exception{
		
		TempPaperAtt = new PaperAttribute();
		IEEE_URL = IEEE_search;
	    Search_str = Searching;
	    pageNUm = 1;
	    IEEE_Xplore_Crawler = Jsoup.connect(IEEE_URL).userAgent("Mozilla").get();
	} 


	/*
	*	Change the html page to the search page with searching keyword you wanted
	*/
	public void getSearchPage() throws Exception{
		TempElm = IEEE_Xplore_Crawler.select("form[id]");
		if(TempElm.attr("id").equals("search_form")){
			IEEE_Xplore_Crawler = Jsoup.connect(IEEE_URL + TempElm.attr("action") + "?queryText="+Search_str+"&pageNumber="+pageNUm)
									.timeout(10000).userAgent("Mozilla").get();

			System.out.println("The Searching Page : "+IEEE_Xplore_Crawler.location());
		}
		
	}

	public void storePaperInfo(int NumOfPage) throws Exception{
	}
}