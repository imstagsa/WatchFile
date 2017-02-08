package net.digitaledge.watchfile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;
import org.apache.tika.Tika;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.settings.Settings;
import org.apache.commons.codec.binary.Base64;
import java.security.*;

public class WatchFileWorker implements Runnable{

	private ESLogger logger;
	private Integer maxTasks = new Integer(20);
	private WatchFileTask[] watchFileTask = new WatchFileTask[maxTasks];
	//private String folder;
	//private String elasticEndpoint;
	//private Settings settings;
	
	public WatchFileWorker(Settings settings, ESLogger logger)
	{
		this.logger = logger;
		//this.settings = settings;
		this.logger.debug("WatchFileWorker  created");
		parseConfig(settings);
	}
	
	
/*	private String encodeFileToBase64Binary(String fileName)
	{
	    File originalFile = new File(fileName);
	    String encodedBase64 = null;
	    try {
	        //FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
	        byte[] bytes = new byte[(int)originalFile.length()];
	        //fileInputStreamReader.read(bytes);
	        encodedBase64 = new String(Base64.encodeBase64(bytes));
	        return encodedBase64;
	    } catch (Exception e) {
	        logger.info(e.toString());
	    }
	    return null;
	}*/

	
	private void parseConfig(Settings settings)
	{
		try{		
			for(int i = 0; i < maxTasks; i++)
				if(
					settings.get("watchfiles.task"+i+".elastichost") != null &&
					settings.get("watchfiles.task"+i+".elasticport") != null &&
					settings.get("watchfiles.task"+i+".folder") != null &&
					settings.get("watchfiles.task"+i+".elasticendpoint") != null)
				{
					logger.info("Found task " + i);
					WatchFileTask watchFilesTask = new WatchFileTask();
					watchFilesTask.setElastichost(settings.get("watchfiles.task"+i+".elastichost"));
					watchFilesTask.setElasticport(settings.get("watchfiles.task"+i+".elasticport"));
					watchFilesTask.setFolder(settings.get("watchfiles.task"+i+".folder"));
					//verify if folder exists
					watchFilesTask.setElasticEndpoint(settings.get("watchfiles.task"+i+".elasticendpoint"));
					watchFileTask[i] = watchFilesTask;
					//printConfig(watchFilesTask, i);
				}			
			
    	} catch (Exception e) {
    		logger.error(e.toString());
		} 	
	}

	private String encodeFileToBase64Binary(String fileName)
	{
	    File originalFile = new File(fileName);
	    String encodedBase64 = null;
	    try {
	        FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
	        byte[] bytes = new byte[(int)originalFile.length()];
	        fileInputStreamReader.read(bytes);
	        encodedBase64 = new String(Base64.encodeBase64(bytes));
	        System.out.println(encodedBase64); 
	        return encodedBase64;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public String identifyFileTypeUsingDefaultTikaForFile(final String fileName)  
	{  
	   String fileType;  
	   try  
	   {  
	      final File file = new File(fileName);  
	      Tika tika = new Tika();
	      fileType = tika.detect(file);  
	      System.out.println(fileType);
	   }  
	   catch (IOException ioEx)  
	   {  
	      System.out.println("Unable to detect type of file " + fileName + " - " + ioEx);  
	      fileType = "Unknown";  
	   }  
	   return fileType;  
	} 
	
	
	/**
	 * %YEAR% in format yyyy
	 * %MONTH% in fromat mm, possible value 01-12
	 * %DAY% in format dd, possible value 01-31
	 * %CURDATE% - see watchalert.taskX.timeformat.
	 * %MESSAGE - message body
	 * %HOST% - existing host ID in EMS
	 * %EPOCHTIME% - Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
	 * %TIMESTAMP% - in format  yyyy-MM-ddT00:00:00Z.
	 * %TIMESTAMP-PERIOD% - Current datetime minus period.Format  yyyy-MM-ddT00:00:00.000Z.
	 */
	private String replaceKeywords(String str, WatchFileTask watchAlertTask)
	{
		try{
			
			str = str.replaceAll("%YEAR%", getDateTime("yyyy"));
			str = str.replaceAll("%MONTH%", getDateTime("MM"));
			str = str.replaceAll("%DAY%", getDateTime("dd"));
			str = str.replaceAll("%TIMESTAMP%", getTimeStamp(0));
			//str = str.replaceAll("%TIMESTAMP-PERIOD%", getTimeStamp(watchAlertTask.getPeriod()));
			str = str.replaceAll("%EPOCHTIME%", Long.toString(getEpochTime()));
			//str = str.replaceAll("%CURDATE%", getDateTime(watchAlertTask.getTimeformat()));	
			str = str.replaceAll("%HOST%", "4443");
			return str;
    	} catch (Exception e) {
    		logger.error(e.toString());
    		return null;
		} 		
	}
	
	private String getTimeStamp(Integer seconds)
	{
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat df2 = new SimpleDateFormat("HH:mm:ss.SSS");
		Date today = Calendar.getInstance().getTime();
		today.setHours(today.getHours() + 5); //New_York
		today.setSeconds(today.getSeconds() - seconds);
		return  df1.format(today) + "T"+df2.format(today)+"Z";
	}
	
	private String getDateTime(String format)
	{
		DateFormat df = new SimpleDateFormat(format);
		Date today = Calendar.getInstance().getTime();        
		return  df.format(today);
	}
	
	private Long getEpochTime()
	{
		return Instant.now().getEpochSecond();
	}
	
	
	private boolean addFile(String fileName)
	{
		try
		{
			
            String base64String = encodeFileToBase64Binary(fileName);
            String fileType = identifyFileTypeUsingDefaultTikaForFile(fileName);
			
            String elasticEndpoint = new String("{\"cv\":\"%FILE%\"}");
            elasticEndpoint  = elasticEndpoint.replaceAll(" ", "%20");
            elasticEndpoint  = elasticEndpoint.replaceAll("%FILE%",base64String);
            
            logger.info("elasticEndpoint: " + elasticEndpoint);
            
			//String alert = replaceKeywords(watchAlertTask.getEmsHost(), watchAlertTask);
			String attachment = new String("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			
			int contentLength = elasticEndpoint.getBytes().length;
		
			URL url = new URL("http://localhost:9200/trying-out-mapper-attachments/person/3/");
			//logger.info("Alert string: " + alert + encodedAlertID);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();           
			connection.setRequestMethod("POST"); 
		
			connection.setDoOutput(true); // Triggers POST.
			connection.setRequestProperty("Accept-Charset", "UTF_8");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Type", Integer.toString(contentLength));
			//connection.connect();
			OutputStream  outputStream = connection.getOutputStream();
			outputStream.write(elasticEndpoint.getBytes());
			outputStream.flush();
			//PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF_8"),true);
		
			logger.info("Sent Alert: " + connection.getResponseCode());
			connection.disconnect();
		} catch (IOException e) {
			logger.info(e.toString());
			return false;
		}
		return true;
	}
	
	public void executeJob()
	{
		try
		{
			String folder = new String("/tmp/elastic");
       	  	logger.info("We are in executeJob "); 
        	File file = new File(folder);

	        if (file.exists() && file.canRead() && file.canWrite()) 
	        {        		  
	        	Stream<Path> paths = Files.walk(Paths.get(folder)); 
	        	{
	        		logger.info("Found directory " + folder);
	        		paths.forEach(filePath -> 
	        		{
	        			logger.info("Found entry:  " + filePath);
	        			if (Files.isRegularFile(filePath)) 
	        			{
	        				logger.info("Found file : " + filePath.toString());
	        				try {
	        					if(addFile(filePath.toString()))
	        					{
	        						Files.delete(filePath);
	        						logger.info("Deleted file " + filePath.toString());
	        					}
	        				} catch (Exception e) {
	        					logger.error(e.toString());
	        				}
	        			}
	        		});
	        	}
	        } 
	        else
	        {
	        	logger.error("The ditectory \""+folder+"\" is not exists or is not readable or writable.");
	        }
	    }catch (Exception e) {
	    	logger.error(e.toString());
	    }
	}
		
		
/*		try(Stream<Path> paths = Files.walk(Paths.get("/home/elasticsearch"))) {
			logger.info("Found directory /home/elasticsearch. ");
			paths.forEach(filePath -> {
				logger.info("Found entry:  " + filePath);
		        if (Files.isRegularFile(filePath)) {
		            logger.info("Found file : " + filePath.toString());
		            addFile(filePath.toString());
		        }
		    });
		} catch (IOException e) {
			logger.info(e.toString());
		}

	}*/
	
	@Override
	public void run() {
		logger.info("ElasticOperationWorker run");
		while (true) 
		{
			logger.info("executeJob() FileWatcher");
			executeJob();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}
		}
		
	}

}
