package net.digitaledge.watchfile;

public class WatchFileTask {

	private String elastichost;
	private String elasticport; 
	private String folder;
	private String query;
	private  String timeformat; 
	private String querybody;
	
	public WatchFileTask()
	{
		this.elastichost = new String();
		this.elasticport = new String();
		this.setFolder(new String());
		this.query = new String();
		this.querybody = new String();
		this.timeformat = new String("yyyy/MM/dd HH:mm:ss");
	}
	
	public String getElastichost() {
		return elastichost;
	}

	public void setElastichost(String elastichost) {
		this.elastichost = elastichost;
	}

	public String getElasticport() {
		return elasticport;
	}

	public void setElasticport(String elasticport) {
		this.elasticport = elasticport;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getTimeformat() {
		return timeformat;
	}

	public void setTimeformat(String timeformat) {
		this.timeformat = timeformat;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuerybody() {
		return querybody;
	}

	public void setQuerybody(String querybody) {
		this.querybody = querybody;
	}
}
