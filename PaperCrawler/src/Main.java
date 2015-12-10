public class Main {

	public static void main(String[] args) throws Exception {
		
		Crawler Paper_crawler = new Crawler(new SearchFactory());

		/*
		*	The function first parameter is Web_Number which is defined in Crawler class.
		*	The function second parameter is the number of search page.
		*	The function third parameter is the search keyword.
		*/
		Paper_crawler.CrawPaper(Paper_crawler.ACM_DL,20,"3D printer");
		//Paper_crawler.CrawPaper(Paper_crawler.IEEE_xplore,2,"3D printer");
	}

}
