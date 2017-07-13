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
	private Integer index = 0;
	
	public WatchFileWorker(Settings settings, ESLogger logger)
	{
		this.logger = logger;
		this.logger.debug("WatchFileWorker  created");
		parseConfig(settings);
	}
	
	
	private void parseConfig(Settings settings)
	{
		try{		
			for(int i = 0; i < maxTasks; i++)
				if(
					settings.get("watchfiles.task"+i+".elastichost") != null &&
					settings.get("watchfiles.task"+i+".elasticport") != null &&
					settings.get("watchfiles.task"+i+".elasticendpoint") != null &&
					settings.get("watchfiles.task"+i+".querybody") != null &&
					settings.get("watchfiles.task"+i+".folder") != null)
					
				{
					File file = new File(settings.get("watchfiles.task"+i+".folder"));
					if(file.exists() && file.canRead() && file.canWrite())
					{
						if(settings.get("watchfiles.task"+i+".querybody").contains("%FILE%"))
						{
							logger.info("Found task " + i);
							WatchFileTask watchFilesTask = new WatchFileTask();
							watchFilesTask.setElastichost(settings.get("watchfiles.task"+i+".elastichost"));
							watchFilesTask.setElasticport(settings.get("watchfiles.task"+i+".elasticport"));
							watchFilesTask.setQuery(settings.get("watchalert.task"+i+".query"));
							watchFilesTask.setFolder(settings.get("watchfiles.task"+i+".folder"));
							watchFilesTask.setTimeformat(settings.get("watchalert.task"+i+".timeformat"));
							watchFilesTask.setQuerybody(settings.get("watchfiles.task"+i+".querybody"));
							watchFileTask[i] = watchFilesTask;
							//printConfig(watchFilesTask, i);
						}
						else
						{
							logger.error("The watchfiles.task"+i+".querybody does not contain %FILE% passphrase.");
						}
					}
					else
					{
						logger.error("The ditectory for task "+ i +" is not exists or is not readable or writable.");
					}
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
	
	
	private String replaceKeywords(String str, WatchFileTask watchAlertTask)
	{
		try{
			
			str = str.replaceAll("%YEAR%", getDateTime("yyyy"));
			str = str.replaceAll("%MONTH%", getDateTime("MM"));
			str = str.replaceAll("%DAY%", getDateTime("dd"));
			str = str.replaceAll("%TIMESTAMP%", getTimeStamp(0));
			str = str.replaceAll("%EPOCHTIME%", Long.toString(getEpochTime()));
			str = str.replaceAll("%CURDATE%", getDateTime(watchAlertTask.getTimeformat()));	
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
	
	
	private boolean addFile(WatchFileTask watchFileTask, String fileName)
	{
		try
		{
			
            String base64String = encodeFileToBase64Binary(fileName);
            String fileType = identifyFileTypeUsingDefaultTikaForFile(fileName);
			
            //String elasticEndpoint = new String("{\"cv\":\"%FILE%\"}");
            //String fileType = new String("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String elasticEndpoint = watchFileTask.getQuerybody();
            elasticEndpoint  = elasticEndpoint.replaceAll(" ", "%20");
            elasticEndpoint  = elasticEndpoint.replaceAll("%FILE%", base64String);
            elasticEndpoint  = elasticEndpoint.replaceAll("%FILETYPE%", fileType);
            
            logger.info("elasticEndpoint: " + elasticEndpoint);
            
			//String alert = replaceKeywords(watchAlertTask.getEmsHost(), watchAlertTask);
			
			int contentLength = elasticEndpoint.getBytes().length;
		
			//URL url = new URL("http://localhost:9200/trying-out-mapper-attachments/person/3/");
			URL url = new URL(watchFileTask.getQuery());
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
			for(int i = 0; i < maxTasks; i++)
			{
				index = 0;
				if(watchFileTask[i] != null)
				{
					logger.info("We are in executeJob "); 
					File file = new File(watchFileTask[i].getFolder());

					if (file.exists() && file.canRead() && file.canWrite()) 
					{        		  
						Stream<Path> paths = Files.walk(Paths.get(watchFileTask[i].getFolder())); 
						{
							logger.info("Found directory " + watchFileTask[i].getFolder());
							paths.forEach(filePath -> 
							{
								logger.info("Found entry:  " + filePath);
								if (Files.isRegularFile(filePath)) 
								{
									logger.info("Found file : " + filePath.toString());
									try {
										if(addFile(watchFileTask[index], filePath.toString()))
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
						logger.error("The ditectory \""+watchFileTask[i].getFolder()+"\" is not exists or is not readable or writable.");
					}
				}
			}
		}catch (Exception e) {
			logger.error(e.toString());
		}

	}

	
	@Override
	public void run() {
		logger.info("ElasticOperationWorker run");
		while (true) 
		{
			logger.info("executeJob() WatchFile");
			executeJob();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}
		}
		
	}

}
