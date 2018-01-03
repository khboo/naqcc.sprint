package com.borasoft.naqcc.sprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Properties;

import com.borasoft.naqcc.sprint.utils.FTPDownloader;
import com.borasoft.naqcc.sprint.utils.FTPUploader;
import com.borasoft.naqcc.sprint.utils.Logger;

import asg.cliche.Command;
import asg.cliche.ShellFactory;
/*
 @Command(description="Command description" name="list", abbreviation="ls")
 public int someCommand(
    @Param(name="param1", description="Description of param1")
        int param1,
    @Param(name="param2", description="Description of param2")
        int param2) {
    . . .
 }
 */
public class App {
	private Logger logger=Logger.getInstance();
	private String naqcc_root="/Users/kihupboo/naqcc";
	private String host;
	private String user;
	private String password;
	private String sprintfilename;
	{
    	Properties props=new Properties();
    	try {
    		InputStream is = new FileInputStream("ftp.properties");
			props.load(is);
	    	host=props.getProperty("host");
	    	user=props.getProperty("user");
	    	password=props.getProperty("password");
		} catch (IOException e) {
			logger.error("ftp.properties file not found.");
		}
    	props=new Properties();
    	try {
    		InputStream is = new FileInputStream("naqcc.properties");
			props.load(is);
			naqcc_root=props.getProperty("SPRINT_HOME");
			sprintfilename=props.getProperty("OUTPUT_FILENAME");
		} catch (IOException e) {
			logger.error("ftp.properties file not found.");
		}
	}
	
    public static void main( String[] args ) throws IOException {
    	// Type 'exit' to terminate the shell.
        ShellFactory.createConsoleShell("","",new App()).commandLoop();
        System.out.println("Bye.");
    }
    
    @Command(description="Prepare to start a Sprint. Sprint logs can be submitted.")
    public boolean open() {
    	/* Download the files for a Sprint opening */
        FTPDownloader ftpDownloader;
		try {
			ftpDownloader = new FTPDownloader(host,user,password);
		} catch (Exception e) {
			logger.error("ftp connection failed for download.\n"+e.getMessage());
			return false;
		}
    	// Mail Forms
        try {
            logger.info("Downloading sprintlog.txt...");
            ftpDownloader.downloadFile("/mailforms/sprintlog.txt", naqcc_root+"/mailforms/sprintlog.txt");
            logger.info("Downloading sprintlog_va3pen.txt...");
            ftpDownloader.downloadFile("/mailforms/sprintlog_va3pen.txt", naqcc_root+"/mailforms/sprintlog_va3pen.txt");
        } catch (Exception e) {
        	logger.error("Download failed.\n"+e.getMessage());
            return false;
        }  
        // Sprint HTML file - e.g., /sprint/sprint201712mw.html, /sprint/sprint2018_160.html
        if(sprintfilename==null) {
        	logger.error("Cannot find sprint filename.");
        	return false;
        }
        try {
            logger.info("Downloading "+sprintfilename+"...");
            ftpDownloader.downloadFile("/sprint/"+sprintfilename,naqcc_root+"/sprint/"+sprintfilename);
        } catch (Exception e) {
        	logger.error("Download failed.\n"+e.getMessage());
            return false;
        }  
        // Sprint open/close - /sprintlog.html <- sprintlog_on.html, sprintlog_off.html
        try {
            logger.info("Downloading sprintlog_on.html...");
            ftpDownloader.downloadFile("/sprintlog_on.html", naqcc_root+"/sprintlog_on.html");
            logger.info("Downloading sprintlog_off.html...");
            ftpDownloader.downloadFile("/sprintlog_off.html", naqcc_root+"/sprintlog_off.html");        
        } catch (Exception e) {
        	logger.error("Download failed.\n"+e.getMessage());
            return false;
        }          
        ftpDownloader.disconnect();
        logger.info("Download completed.");     
        
    	/* Rename the files */
        if(!frename(naqcc_root+"/mailforms/sprintlog.txt",naqcc_root+"/mailforms/sprintlog_original.txt")) {
        	return false;
        }
        if(!frename(naqcc_root+"/mailforms/sprintlog_va3pen.txt",naqcc_root+"/mailforms/sprintlog.txt")) {
        	return false;
        }
        if(!frename(naqcc_root+"/sprintlog_on.html",naqcc_root+"/sprintlog.html")) {
        	return false;
        }  
        
    	/* Upload the updated files, and the Sprint is ready. */
        // - sprintlog.html,mailforms/sprintlog.txt
        FTPUploader ftpUploader;
		try {
			ftpUploader = new FTPUploader(host,user,password);
		} catch (Exception e) {
			logger.error("ftp connection failed for upload.\n"+e.getMessage());
			return false;
		}
		try {
			ftpUploader.uploadFile(naqcc_root+"/sprintlog.html","sprintlog.html","/");
			ftpUploader.uploadFile(naqcc_root+"/mailforms/sprintlog.txt","sprintlog.txt","/mailforms/");
		} catch (Exception e) {
			logger.error("Upload failed.\n"+e.getMessage());
			return false;
		}
		ftpUploader.disconnect();
 
        /* Prepare sprint template file(.TEMPLATE) from sprintFilename(.HTML). */
        logger.info("Creating a sprint template file from: "+sprintfilename+".");
        if(createSprintTemplateFile(naqcc_root+"/sprint/"+sprintfilename)) {
	        logger.info("Sprint template file generated from: "+naqcc_root+"/sprint/"+sprintfilename);
        } else {
	        logger.error("Sprint template file generation failed.");
	        return false;
        }
        logger.info("All set for the sprint.");        
        return true;
    }
    private boolean frename(String source,String dest) {
    	boolean result=false;
    	result= new File(source).renameTo(new File(dest));
    	if(!result) {
    		logger.error("File rename failed: "+source+" --> "+dest);
    	} else {
    		logger.info(source+" has been renamed to "+dest+".");
    	}
    	return result;
    }
    private boolean createSprintTemplateFile(String sprintFilepath) {
		try {
			TemplateGenerator gen = new TemplateGenerator(sprintFilepath);
	        gen.run();
	        return true;
		} catch (Exception e) {
			logger.error("Template file generation failed.");
	        return false;
		}
    }
    private boolean disableTimer(String sprintFilepath) {
		try {
			TemplateGenerator gen = new TemplateGenerator(sprintFilepath);
	        gen.disableTimer();
	        return true;
		} catch (Exception e) {
			logger.error("Disabling the countdown timer failed.");
	        return false;
		}
    }
    
    @Command(description="Finish the current Sprint. No more log submissions are allowed.")
    public boolean close() {
    	/* Rename the files for a Sprint closing. */
        if(!frename(naqcc_root+"/mailforms/sprintlog.txt",naqcc_root+"/mailforms/sprintlog_va3pen.txt")) {
        	return false;
        }
        if(!frename(naqcc_root+"/mailforms/sprintlog_original.txt",naqcc_root+"/mailforms/sprintlog.txt")) {
        	return false;
        } 
        if(!frename(naqcc_root+"/sprintlog_off.html",naqcc_root+"/sprintlog.html")) {
        	return false;
        }
        
    	/* Upload the original files for a sprint closing */
        FTPUploader ftpuploader;
		try {
			ftpuploader = new FTPUploader(host,user,password);
		} catch (Exception e) {
			logger.error("ftp connection failed.\n"+e.getMessage());
			return false;
		}
    	// Mail Forms
        try {
            logger.info("Uploading sprintlog.txt...");
            ftpuploader.uploadFile(naqcc_root+"/mailforms/sprintlog.txt","sprintlog.txt","/mailforms/");
        } catch (Exception e) {
        	logger.error("Upload failed.\n"+e.getMessage());
            return false;
        }  
        try {
            logger.info("Uploading sprintlog.html...");
            ftpuploader.uploadFile(naqcc_root+"/sprintlog.html","sprintlog.html","/");
        } catch (Exception e) {
        	logger.error("Upload failed.\n"+e.getMessage());
            return false;
        }
        // Countdown timer
        disableTimer(naqcc_root+"/"+sprintfilename);
        try {
            logger.info("Disabling countdown timer...");
            ftpuploader.uploadFile(naqcc_root+"/"+sprintfilename,sprintfilename,"/sprint/");
            logger.info("The countdown timer disabled successfully.");
        } catch (Exception e) {
        	logger.error("Upload failed.\n"+e.getMessage());
            return false;
        }        
        ftpuploader.disconnect();
        logger.info("Upload completed.");   
        logger.info("The sprint closed successfully.");       
        return true;
    }

    @Command(description="Cancel automated periodic updates.",name="cancel")
    public boolean cancelUpdates() {
    	return true;
    }
    
    @Command(description="Update the sprint results. Pass true for auto, or false for manual mode.",name="update")
    public boolean updateSprintResult(boolean auto) {
    	ContestResultGeneratorControl control=new ContestResultGeneratorControl(host,user,password,naqcc_root,sprintfilename);
    	if(!auto) {
        	return control.updateSprintResult(); // manual mode
    	}
    	try {
			control.performScheduledUpdates();
			return true;
		} catch (ParseException e) {
			logger.error("Scheduled updates failed.\n"+e.getMessage());
			return false;
		}

    }
}
