public class SearchFactory{

	public static final int ACM_DL = 0;
	public static final int IEEE_xplore = 1;	

	public SearchFactory(){}

	public SearchPaperIF Born_Crawler(int Wed_number,String Search_keyword) throws Exception{
		if (Wed_number == ACM_DL){
			return (new JsoupToACM_DL("http://dl.acm.org/",Search_keyword));
		}
		else if(Wed_number == IEEE_xplore){
			return (new JsoupToIEEE_xplore("http://ieeexplore.ieee.org/",Search_keyword));
		}
		else{
			return null;
		}
	}
}