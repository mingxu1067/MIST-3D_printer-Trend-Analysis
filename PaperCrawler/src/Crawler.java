public class Crawler{

	public SearchFactory crawler;
	public static final int ACM_DL = 0;
	public static final int IEEE_xplore = 1;	

	public Crawler(SearchFactory crawler) throws Exception{
		this.crawler = crawler;
	}

	public void CrawPaper(int Wed_number,int NumOfPage,String Search_keyword) throws Exception{
		SearchPaperIF SubCrawler;
		SubCrawler = crawler.Born_Crawler(Wed_number,Search_keyword);

		SubCrawler.getSearchPage();
		SubCrawler.storePaperInfo(NumOfPage);
	}
}
