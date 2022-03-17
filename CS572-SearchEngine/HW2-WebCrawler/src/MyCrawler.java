import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {

	CrawlData crawlData;
	String newsSiteName = "usatoday";
	String newsSiteDomain = "usatoday.com";
	static String regex = ".*(\\.(css|js|json|mp3|zip|gz|vcf|xml|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v))$";
	private final static Pattern FILTERS = Pattern.compile(regex);

	public MyCrawler() {
		crawlData = new CrawlData();
	}
	
//	public static void main(String[] args) throws Exception {
//		String url = "https://www.ny,times.com/foo/";
//		url = parseUrl(url);
//		System.out.println(url);
//	}
	
	private static String parseUrl(String href) {
		if (href.endsWith("/")) {
			href = href.substring(0, href.length()-1);
		}
		
		return href.replace(",", "_").replaceFirst("^http(s)?://(www.)?", "");
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = parseUrl(url.getURL());

		if (href.startsWith(newsSiteDomain))
		{
			crawlData.addDiscoveredUrls(url.getURL(), "OK");
		}
		else
		{
			crawlData.addDiscoveredUrls(url.getURL(), "N_OK");
		}

		return !FILTERS.matcher(href).matches()
				&& href.startsWith(newsSiteDomain);
	}

	
	@Override
	public void handlePageStatusCode(WebURL url, int statusCode, String statusDescription)
	{
		// add fetched urls
		crawlData.addFetchedUrls(url.getURL(), statusCode);
	}
	
	/**
	 * This function is called when a page is fetched and ready
	 * to be processed by your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		String contentType = page.getContentType().toLowerCase().split(";")[0];

		// text or html content
		if (contentType.equals("text/html")) 
		{
			if (page.getParseData() instanceof HtmlParseData) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				Set<WebURL> links = htmlParseData.getOutgoingUrls();
				// add visited urls
				crawlData.addVisitedUrls(url, page.getContentData().length, links.size(), contentType);
			}
		}
		// other acceptable content
		else if (contentType.equals("application/pdf") || contentType.equals("application/document") ||
				contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
				contentType.equals("application/msword") || contentType.startsWith("image")) 
		{
			// no outgoing links
			crawlData.addVisitedUrls(url, page.getContentData().length, 0, contentType);
		}
		
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

		}
	}

	/**
	 * This function is called by controller to get the local data of this crawler when job is
	 * finished
	 */
	@Override
	public Object getMyLocalData() {
		return crawlData;
	}

}
