package net.digitaledge.watchfile;

public class WatchFileTask {

	private String elastichost;
	private String elasticport; 
	private String folder; 
	private String elasticEndpoint;
	
	public WatchFileTask()
	{
		this.elastichost = new String();
		this.elasticport = new String();
		this.setFolder(new String());
		this.elasticEndpoint = new String();
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

	public String getElasticEndpoint() {
		return elasticEndpoint;
	}

	public void setElasticEndpoint(String elasticEndpoint) {
		this.elasticEndpoint = elasticEndpoint;
	}

}
