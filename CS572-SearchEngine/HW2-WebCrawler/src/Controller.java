import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	static int numberOfCrawlers = 10;
	static String newsSiteName = "usatoday";
	static CrawlData crawlData;

	// output parameters
	static String crawlStorageFolder = "D:\\xmh91\\git\\WebCrawler\\data\\crawl";


	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();

		crawlData = new CrawlData();

		List<Object> allCrawlData = performCrawling();

		for (Object obj : allCrawlData) {
			CrawlData data = (CrawlData) obj;
			crawlData.fetchedUrls.addAll(data.fetchedUrls);
			crawlData.visitedUrls.addAll(data.visitedUrls);
			crawlData.discoveredUrls.addAll(data.discoveredUrls);
		}

		System.out.println("Fetched count: " + crawlData.fetchedUrls.size());
		System.out.println("Visited count: " + crawlData.visitedUrls.size());
		System.out.println("Discovered count: " + crawlData.discoveredUrls.size());

		long crawlEnd = System.currentTimeMillis();
		System.out.println("Time taken: " + (crawlEnd - startTime)/1000 + " seconds");
		dumpData();

		long dumpEnd = System.currentTimeMillis();
		System.out.println("Time taken: " + (dumpEnd - crawlEnd)/1000 + " seconds");

		printStats();
	}

	private static void printStats() throws Exception {
		int fetchAttempts = crawlData.fetchedUrls.size();

		HashMap<Integer, Integer> statusCodes = new HashMap<>();

		for (FetchUrl url : crawlData.fetchedUrls) {
			if (statusCodes.containsKey(url.status)) {
				statusCodes.put(url.status, statusCodes.get(url.status) + 1);
			}else {
				statusCodes.put(url.status, 1);
			}
		}

		int fetchSucceeded = statusCodes.get(200);
		int fetchFailedAborted = fetchAttempts - fetchSucceeded;

		int totalUrls = crawlData.discoveredUrls.size();
		int uniqueInsideUrls = 0;

		HashSet<String> uniqueUrls = new HashSet<>();

		for (DiscoverUrl dUrl : crawlData.discoveredUrls) {
			if (!uniqueUrls.contains(dUrl.url)) {
				uniqueUrls.add(dUrl.url);
				if (dUrl.residenceIndicator == "OK") {
					uniqueInsideUrls ++;
				}
			}
		}

		int uniqueUrlsCount = uniqueUrls.size();
		int uniqueOutsideUrls = uniqueUrlsCount - uniqueInsideUrls;

		int oneK = 0; int tenK = 0; int hundredK = 0; int oneM = 0; int other = 0;
		HashMap<String, Integer> contentTypes = new HashMap<>();

		for (VisitUrl vUrl : crawlData.visitedUrls) {
			if (vUrl.size < 1024) {
				oneK++;
			}else if (vUrl.size < 10240) {
				tenK++;
			}else if (vUrl.size < 102400) {
				hundredK++;
			}else if (vUrl.size < 1024 * 1024) {
				oneM++;
			}else {
				other++;
			}

			if (contentTypes.containsKey(vUrl.contentType)) {
				contentTypes.put(vUrl.contentType, contentTypes.get(vUrl.contentType) + 1);
			}else {
				contentTypes.put(vUrl.contentType, 1);
			}
		}

		File newFile = new File("CrawlReport_" + newsSiteName + ".txt");
		newFile.delete();
		newFile.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.write("Name: Menghan Xu\nUSC ID: 7375827771\n");
		bw.write("News site crawled: " + newsSiteName +".com\nNumber of threads: " + numberOfCrawlers + "\n\n");
		
		bw.write("Fetch Statistics\n================\n");
		bw.write("# fetches attempted: " + fetchAttempts + "\n# fetches succeeded: " + fetchSucceeded +
				"\n# fetches failed or aborted: " + fetchFailedAborted + "\n\n");
		
		bw.write("Outgoing URLs:\n==============\n");
		bw.write("Total URLs extracted: " + totalUrls + "\n# unique URLs extracted: " + uniqueUrlsCount + "\n");
		bw.write("# unique URLs within News Site: " + uniqueInsideUrls +
					"\n# unique URLs outside News Site: " + uniqueOutsideUrls + "\n\n");
		
		bw.write("Status Codes:\n=============\n");
		bw.write("200 OK: " + statusCodes.get(200) + "\n");
		bw.write("301 Moved Permanently: " + statusCodes.get(301) + "\n");
		bw.write("302 Found: " + statusCodes.get(302) + "\n");
		bw.write("400 Bad Request Response: " + statusCodes.get(400) + "\n");
		bw.write("401 Unauthorized: " + statusCodes.get(401) + "\n");
		bw.write("403 Forbidden: " + statusCodes.get(403) + "\n");
		bw.write("404 Not Found: " + statusCodes.get(404) + "\n");
		bw.write("410 Gone: " + statusCodes.get(410) + "\n");
		bw.write("500 Internal Server Error: " + statusCodes.get(500) + "\n\n");
		
		bw.write("File Sizes:\n===========\n");
		bw.write("< 1KB: "+ oneK + "\n");
		bw.write("1KB ~ <10KB: "+ tenK + "\n");
		bw.write("10KB ~ <100KB: "+ hundredK + "\n");
		bw.write("100KB ~ <1MB: "+ oneM + "\n");
		bw.write(">= 1MB: "+ other + "\n\n");
		
		bw.write("Content Types:\n==============\n");
		for (String type : contentTypes.keySet()) {
			bw.write(type + ": " + contentTypes.get(type) + "\n");
		}
		
		bw.close();

		for(int key: statusCodes.keySet())
		{
			System.out.println(key + " " + statusCodes.get(key));
		}
	}
	
	
	private static void dumpData() throws Exception {

		System.out.println("Dumping data to fetch_" + newsSiteName + ".csv ..." );
		File newFile = new File("fetch_" + newsSiteName + ".csv");
		newFile.delete();
		newFile.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.append("URL,Status\n");

		for(FetchUrl fetchUrl : crawlData.fetchedUrls) {
			bw.append(fetchUrl.url + "," + fetchUrl.status + "\n");
		}
		bw.close();

		System.out.println("Dumping data to visit_" + newsSiteName + ".csv ..." );
		newFile = new File("visit_" + newsSiteName + ".csv");
		newFile.delete();
		newFile.createNewFile();
		bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.append("URL,Size in Bytes,Num of outlinks,Content-type\n");

		for(VisitUrl visitUrl : crawlData.visitedUrls) {
			bw.append(visitUrl.url + "," + visitUrl.size + "," + visitUrl.outLinks + "," + visitUrl.contentType + "\n");
		}
		bw.close();

		System.out.println("Dumping data to urls_" + newsSiteName + ".csv ..." );
		newFile = new File("urls_" + newsSiteName + ".csv");
		newFile.delete();
		newFile.createNewFile();
		bw = new BufferedWriter(new FileWriter(newFile, true));
		bw.append("URL,Residence\n");

		for (DiscoverUrl discoverUrl : crawlData.discoveredUrls) {
			bw.append(discoverUrl.url + "," + discoverUrl.residenceIndicator + "\n");
		}
		bw.close();
	}

	private static List<Object> performCrawling() throws Exception {
		String newsSiteUrl = "https://www.usatoday.com";
		String crawlStorageFolder = "D:\\xmh91\\git\\WebCrawler\\data\\";
		int maxPagesToFetch = 20000;
		int maxDepthOfCrawling = 16;
		int politenessDelay = 100;


		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxPagesToFetch(maxPagesToFetch);
		config.setMaxDepthOfCrawling(maxDepthOfCrawling);
		config.setPolitenessDelay(politenessDelay);
		config.setIncludeBinaryContentInCrawling(true);

		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		controller.addSeed(newsSiteUrl);
		controller.start(MyCrawler.class, numberOfCrawlers);

		return controller.getCrawlersLocalData(); 

	}

}
