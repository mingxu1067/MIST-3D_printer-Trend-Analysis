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

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class TrendAnalysis {

	private Map<String, Date> paperTime;
	private Map<String, List<String>> areasOfPaper;
	private Map<String, List<String>> trend;

	public TrendAnalysis() {
		paperTime = new HashMap<String, Date>();
		areasOfPaper = new HashMap<String, List<String>>();
		trend = new HashMap<String, List<String>>();
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

		return trend;
	}

}