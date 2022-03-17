
public class SiteObject {
	private String url;
	private int status;
	private boolean inside;
	private int size;
	private int outlinks;
	private String contentType;
	
	public SiteObject() {
		inside = true;
		size = 0;
		outlinks = 0;
		contentType = "null";
	}
	
	String getUrl() {
		return url;
	}
	void setUrl(String url) {
		this.url = url;
	}
	int getStatus() {
		return status;
	}
	void setStatus(int status) {
		this.status = status;
	}
	boolean isInside() {
		return inside;
	}
	void setInside(boolean inside) {
		this.inside = inside;
	}
	int getSize() {
		return size;
	}
	void setSize(int size) {
		this.size = size;
	}
	int getOutlinks() {
		return outlinks;
	}
	void setOutlinks(int outlinks) {
		this.outlinks = outlinks;
	}
	String getContentType() {
		return contentType;
	}
	void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
