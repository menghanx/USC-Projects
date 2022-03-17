import java.util.ArrayList;

class FetchUrl{
	String url;
	int status;
	
	public FetchUrl(String url, int status) {
		this.url = url;
		this.status = status;
	}
}

class VisitUrl{
	String url;
	int size;
	int outLinks;
	String contentType;
	
	public VisitUrl(String url, int size, int outLinks, String contentType) {
		this.url = url;
		this.size = size;
		this.outLinks = outLinks;
		this.contentType = contentType;
	}
}

class DiscoverUrl {
	String url;
	String residenceIndicator;
	
	public DiscoverUrl(String url, String indicator) {
		this.url = url;
		this.residenceIndicator = indicator;
	}
}


public class CrawlData {
	ArrayList<FetchUrl> fetchedUrls;
	ArrayList<VisitUrl> visitedUrls;
	ArrayList<DiscoverUrl> discoveredUrls;
	
	public CrawlData() {
		fetchedUrls = new ArrayList<FetchUrl>();
		visitedUrls = new ArrayList<VisitUrl>();
		discoveredUrls = new ArrayList<DiscoverUrl>();
	}

	public void addFetchedUrls(String url, int status) {
		this.fetchedUrls.add(new FetchUrl(url, status));
	}
	
	public void addVisitedUrls(String url, int size, int outLinks, String contentType) {
		this.visitedUrls.add(new VisitUrl(url, size, outLinks, contentType));
	}
	
	public void addDiscoveredUrls(String url, String indicator) {
		this.discoveredUrls.add(new DiscoverUrl(url, indicator));
	}
}