import java.util.Random;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class JsoupToACM_DL implements SearchPaperIF{

	private String ACM_URL;
	private Document ACM_DL_Crawler;
	private String Search_str;
	private Elements TempElm;	//For temp searching storge the elements
	private PaperAttribute TempPaperAtt; 

	private String[] tempKeywoed = new String[20];	


	/*
	*	JsoupToACM_DL Constructor
	*		Initialize the connected URL and the keyword that want to be searched
	*/
	public JsoupToACM_DL(String ACM_search,String Searching) throws Exception{
		
		TempPaperAtt = new PaperAttribute();
		ACM_URL = ACM_search;
	    Search_str = Searching;

	    ACM_DL_Crawler = Jsoup.connect(ACM_URL).userAgent("Mozilla").get();

	}

	/*
	*	Change the html page to the search page with searching keyword you wanted
	*/
	public void getSearchPage() throws Exception{
		TempElm = ACM_DL_Crawler.select("form");
		ACM_DL_Crawler = Jsoup.connect(ACM_URL + TempElm.attr("action") + "&query="+Search_str).timeout(100000).userAgent("Mozilla").get();
		System.out.println("The Searching Page : "+ACM_DL_Crawler.location());
	}

	public void storePaperInfo(int NumOfPage) throws Exception{
		
		Elements PapaerElm = null;
		Document PaprtDoc = null;
		String ParperURL = null;
		for (int i=0;i<NumOfPage;i++){
 			
 			//Get all paper keywords in a page,
			getPaperKeyword_a_Page();
			int Keyword_idx = 0;

			TempElm = ACM_DL_Crawler.select("td table tbody tr td a[target]");
			for (Element temp : TempElm){
				if(temp.attr("target").equals("_self")){

					//store paper keyword to temp PaperAttribute class object
					TempPaperAtt.setKeywords(tempKeywoed[Keyword_idx++]);

					
					ParperURL = ACM_URL+temp.attr("href");
					//Get paper URL, then store to temp PaperAttribute class object
					TempPaperAtt.setURL(ParperURL);

					PaprtDoc = Jsoup.connect(ParperURL).timeout(100000).userAgent("Mozilla").get();


					//Get paper title, then store to temp PaperAttribute class object
					PapaerElm = PaprtDoc.select("tr td div div h1 strong");
					TempPaperAtt.setTitle(PapaerElm.first().text());


					//Get paper all authors, then store to temp PaperAttribute class object
					PapaerElm = PaprtDoc.select("tbody tr td[valign] a[target]");
					String tempAuthor = "";
					for(Element Ptemp : PapaerElm){
						//if(Ptemp.attr("title").equals("Author Profile Page")) 
						if(Ptemp.attr("target").equals("_self"))
							tempAuthor +=Ptemp.text()+", ";
					}
					TempPaperAtt.setAuthors(tempAuthor);

					//Get paper abstract and publication, then store to temp PaperAttribute class object
					PapaerElm = PaprtDoc.select("script");
					for(Element tempAbstract :PapaerElm){
						if(tempAbstract.toString().indexOf("tab_abstract")>0){
							String abstractURL = tempAbstract.toString()
													.substring(tempAbstract.toString()
														.indexOf("tab_abstract"));
							abstractURL = abstractURL.toString().substring(
											abstractURL.toString().indexOf("tab_abstract")
												,abstractURL.toString().indexOf("']}"));
									
						
							Document AbstracrDoc = Jsoup.connect(ACM_URL+abstractURL).timeout(100000).userAgent("Mozilla").get();
							TempPaperAtt.setAbstract(AbstracrDoc.select("div div").text());
						}

						if(tempAbstract.toString().indexOf("tab_source")>0){
							String publicationURL = tempAbstract.toString().substring(
														tempAbstract.toString().indexOf("tab_source"));
							publicationURL = publicationURL.toString().substring(
									publicationURL.toString().indexOf("tab_source"),
										publicationURL.toString().indexOf("']}"));
						
							Document PublicationDoc = Jsoup.connect(ACM_URL+publicationURL).timeout(100000).userAgent("Mozilla").get();
							Elements PublicationElms = PublicationDoc.select("tr td");
							

							boolean StoragePublisher = false;
							boolean StorageDate = false;
							String Date = "";
							for(Element Ptemp : PublicationElms){
								if(StoragePublisher){
									TempPaperAtt.setPublisher(Ptemp.text());
									StoragePublisher = false;
								}

								if(StorageDate){
									Date = Ptemp.text().substring(0,4);
									Date += Ptemp.text().substring(5,7);
									Date += Ptemp.text().substring(8,10);

									TempPaperAtt.setDate(Integer.valueOf(Date));

									StorageDate = false;
								}

								if(Ptemp.text().equals("Publisher")){
									StoragePublisher = true;
								}
								else if (Ptemp.text().equals("Publication Date")){
									StorageDate = true;
								}

							}

						}
					}
					System.out.println("Title : " + TempPaperAtt.getTitle());

					if(!TempPaperAtt.getAbstract().equals("N"))
						TempPaperAtt.StorePaperInfoToSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");
					

					TempPaperAtt.StorePaperKeyToSQL("127.0.0.1:3306/papers3dprinter","3Dprinter","3Dprinter");


					TempPaperAtt.ClearInfo();

					//random to sleep the crawler
					Random rand = new Random();
					try {
					    Thread.sleep(rand.nextInt(8000) + 8000);
					} catch(InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
				}

			}


			//get the 'next' page web link
			TempElm = ACM_DL_Crawler.select("table tbody tr td[colspan] a[href]");
			for(Element Ptemp : TempElm){
				if(Ptemp.text().equals("next")){
					ACM_DL_Crawler = Jsoup.connect(ACM_URL + Ptemp.attr("href")).timeout(100000).userAgent("Mozilla").get();
					break;
				}
			}


		}
		
	}

	public void getPaperKeyword_a_Page() throws Exception{

		Elements keytmepElm;
		int idxKeyword = 0;


		for(int idx=0;idx<20;idx++){
			tempKeywoed[idx] = "N";
		}
		idxKeyword = 0;

		keytmepElm = ACM_DL_Crawler.select("td table tbody tr td div[class]");
		//Get all keyword of papers in a page	
		for (Element temp : keytmepElm){

			if(temp.attr("class").equals("abstract2")){
				if(temp.text().indexOf("Keywords")>0){
					String tt = temp.text().substring(temp.text().indexOf("Keywords"));
					tt = tt.substring(10);
					tempKeywoed[idxKeyword] = tt;
				}
				idxKeyword++;
			}
		}

		//random to sleep the crawler
		Random rand = new Random();
		try {
		    Thread.sleep(rand.nextInt(8000) + 8000);
		} catch(InterruptedException ex) {
			 Thread.currentThread().interrupt();
		}
	}

 }
