package com.borasoft.naqcc.sprint;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.borasoft.naqcc.sprint.utils.FTPUploader;
import com.borasoft.naqcc.sprint.utils.Logger;

public class ContestResultGeneratorControl {
	private Logger logger=Logger.getInstance();
	private final ScheduledExecutorService scheduler =Executors.newScheduledThreadPool(1);
	private DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
	private String host;
	private String user;
	private String password;
	private String naqcc_root;
	private String sprintfilename;
	private String emailMode;
	private String emailServer;
	private String emailUser;
	private String emailPassword;
	private String log_closing;
	{
    	Properties props=new Properties();
    	try {
    		InputStream is = new FileInputStream("mail.properties");
			props.load(is);
			emailMode=props.getProperty("emailMode");
			emailServer=props.getProperty("emailServer");
			emailUser=props.getProperty("emailUser");
			emailPassword=props.getProperty("emailPassword");
		} catch (IOException e) {
			logger.error("mail.properties file not found.");
		}
	}
	public ContestResultGeneratorControl(String host,String user,String password,String naqccRoot,String sprintfilename,String log_closing) {
		this.host=host;
		this.user=user;
		this.password=password;
		this.naqcc_root=naqccRoot;
		this.sprintfilename=sprintfilename;
		this.log_closing=log_closing;
	}
    public boolean updateSprintResult() {
        logger.message("\nNAQCC Sprint Result Page Generator\n");
        logger.message("Program starting...");  
		Date now=new Date();
        ContestResultGenerator gen = new ContestResultGenerator();
		System.out.println("Program started at: "+now.toString());
        try {
			gen.initialize(); // load properties values.
	        logger.message("Program initialized successfully.");
	        String[] args={"-D","-T",emailMode,"-H",emailServer,"-U",emailUser,"-P",emailPassword,"-O","./tmp","-S","NAQCC Sprint Log"};
	        gen.readEMailAndArchive(args);
	        gen.generateHTMLFromArchive(args); // read the value of -O, archive directory from args
	        gen.addVA3PENComments();
	        logger.info("Sprint results generated successfully.");
		} catch (IOException e) {
			logger.error("Sprint results generation failed.\n"+e.getMessage());
			return false;
		} 
        /* Upload the sprint result file - e.g, sprint201712mw.html */
        FTPUploader ftpUploader;
		try {
			ftpUploader = new FTPUploader(host,user,password);
		} catch (Exception e) {
			logger.error("ftp connection failed for uploading sprint result file.\n"+e.getMessage());
			return false;
		}
		try {
			ftpUploader.uploadFile(naqcc_root+"/"+sprintfilename,sprintfilename,"/sprint/");
			logger.info("Sprint result file, "+sprintfilename+", has been uploaded successfully.");
		} catch (Exception e) {
			logger.error("Upload failed for the sprint result file..\n"+e.getMessage());
			return false;
		}
		ftpUploader.disconnect();
        logger.message("Completed.");
    	return true;
    }

	public void performScheduledUpdates() throws ParseException {
		final Date closingDate1=df.parse(log_closing);
		final Date closingDate2=new Date(closingDate1.getTime()+1000*60*60*3); // 3 hours after the closing date		
		logger.info("The scheduled updates will be continued until "+closingDate1.toString()+".");
		logger.info("The scheduled task will be shutdown as late as: "+closingDate2.toString());
		final Runnable updater = new Runnable() {
			public void run() { 
				updateSprintResult(); 
				Date now=new Date();	
				logger.info("Current datetime is: "+now.toString());
				if(now.after(closingDate1)) {
					logger.info("The scheduled updates will be terminated.");
					scheduler.shutdownNow();
					new App().close();
				}
			}
		};
		final ScheduledFuture<?> updaterHandle = scheduler.scheduleAtFixedRate(updater,10,60*60*3,SECONDS);     
		scheduler.schedule(new Runnable() {
				public void run() { 
					updaterHandle.cancel(true); 
				}
		    }, closingDate2.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS); 
	}
}
