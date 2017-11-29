package com.borasoft.naqcc.sprint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Properties;
import com.borasoft.naqcc.sprint.utils.Logger;

public final class ContestResultGenerator {
  private String outputDir;
  private Logger logger;
  private final String submissionOrderFilename = "submissions.lst";
  private double maxPower;
  private int mode;
  private String outputFilename;
  private String inputFileTemplate;
  
  /**
   * @param args - e.g) -D -T pop3s -H pop3.live.com -U foo@hotmail.com -P Bar -O c:/temp2 -S "NAQCC Sprint Log"
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    Logger logger= Logger.getInstance();
    logger.message("\nNAQCC Sprint Result Page Generator\n");
    logger.message("Program starting...");   
    logger.message("Program initialized successfully.");
    ContestResultGenerator gen = new ContestResultGenerator();
    gen.initialize(); // load properties values.
    gen.readEMailAndArchive(args);
    gen.generateHTMLFromArchive(args); // read the value of -O, archive directory from args
    logger.message("Completed.");
    System.exit(0);
  }
  
  public void initialize() throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream("naqcc.properties"));
    if(props.containsKey("SPRINT_MODE")) {
      mode=Integer.parseInt(props.getProperty("SPRINT_MODE"));
    } else {
      logger.error("SPRINT_MODE is not found in naqcc.properties");
    }
    if(props.containsKey("MAX_POWER")) {
      maxPower=Integer.parseInt(props.getProperty("MAX_POWER"));
    } else {
      logger.error("MAX_POWER is not found in naqcc.properties");
    }
    if(props.containsKey("OUTPUT_FILENAME")) {
      outputFilename=props.getProperty("OUTPUT_FILENAME");
    } else {
      logger.error("OUTPUT_FILENAME is not found in naqcc.properties");
    }   
    if(props.containsKey("INPUT_FILE_TEMPLATE")) {
      inputFileTemplate=props.getProperty("INPUT_FILE_TEMPLATE");
    }else {
      logger.error("INPUT_FILE_TEMPLATE is not found in naqcc.properties");
    } 
  }
  
  public ContestResultGenerator() {
    logger=Logger.getInstance();
  }
  
  public void readEMailAndArchive(String[] args) throws FileNotFoundException, IOException {
    // Check out the auto logger submission and writes each submission in 
    // its own file with a temporary file name.
    logger.message("Reading email and writing to temporary files...");
    EMailReader emailReader = new EMailReader(args);
    emailReader.dumpMail();
    if(emailReader.getNumEMailProcessed()==0) {
      return;
    }
    
    // Read in the submission files, build LogEntry items for score processing.
    // Rename the temporary files using the participants' radio callsigns after completing
    // the processing.
    outputDir = emailReader.getOutputDir();
    if(outputDir==null) { // the user did not provide log files output directory
      outputDir=System.getProperty("user.dir")+"/archives";
    }
    File dir = new File(outputDir);
    File[] files = dir.listFiles();
    File file;
    File newFile;
    InputStreamReader streamReader;
    AutoLoggerReader loggerReader;
    LogEntry entry;
    boolean returnCode=true;
    logger.message("Renaming temporary files to <callsign>.log...");
    Vector<String> callsigns = new Vector<String>();
    for(int i=0; i<files.length; i++) {
      file = files[i];
      if(file.getName().equalsIgnoreCase(submissionOrderFilename))
        continue; // skip submission.lst
      if(!file.getName().startsWith("NAQCC") || !file.getName().endsWith(".txt"))
    	  continue;
      streamReader = new InputStreamReader(new FileInputStream(file));
      loggerReader = new AutoLoggerReader(streamReader);
      entry = loggerReader.readLogEntry();
      streamReader.close();
      if(entry.getCallsign().trim().contains("/")) {
        logger.error("Cannot process because the callsign contains an invalid character(forward-slash): " + entry.getCallsign());
        continue;
      }
      newFile = new File(outputDir + "/" + entry.getCallsign().trim() + ".log");
      if(newFile.exists()) {
        logger.warning(newFile + " already exists.");
        logger.warning("Did not rename " +file.getName()+" to "+newFile);
      } else {
        returnCode=file.renameTo(newFile);
        if (!returnCode) {
          logger.error("File rename failed for: " + file.getName());
        } else {
          callsigns.add(entry.getCallsign());
        }
      }
    }
    updateSubmissionOrderList(callsigns);
  }
  
  public void addVA3PENComments() throws IOException {
	  //pre-condition: outputFilename is set.
		BufferedReader br = null;
		String line;
		br=new BufferedReader(new FileReader(outputFilename));
		StringBuilder builder=new StringBuilder();
		while ((line=br.readLine()) !=null) {
			if(line.contains("VA3PEN Comments")) {
				// Add VA3PEN comment
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
				Date date = new Date();
				String datetime=dateFormat.format(date);
				builder.append("<span class=\"blackboldmedium\">VA3PEN Comments - </span>Results produced on "+datetime+" EST.<br><br>");
				builder.append("\n");
			} else {
				builder.append(line);
				builder.append("\n");
			}
		}
		br.close();	 
		BufferedWriter bw=new BufferedWriter(new FileWriter(outputFilename));
		bw.write(builder.toString());
		bw.close();
  }
  
  public void generateHTMLFromArchive(String[] args)  throws FileNotFoundException, IOException {
    for (int optind = 0; optind < args.length; optind++) {
      if (args[optind].equals("-O")) {
        outputDir = args[++optind];
      }
    }    
    logger.info("HTML file to be generated: " + outputFilename);
    // Read in the submission files from outputDir, build LogEntry items for score processing.
    File dir = new File(outputDir);
    File[] files = dir.listFiles();
    File file;
    InputStreamReader streamReader;
    AutoLoggerReader loggerReader;
    LogEntry entry;
    EntryCollector entries = new EntryCollector();
    logger.info("Number of files to be processed: " + files.length);
    logger.message("Processing started.");
    for(int i=0; i<files.length; i++) {
      file = files[i];
      if(!hasExtension(file.getName(),".log")) { // not a sprint file
        logger.warning("Skipping a file: " + file.getName());
        continue;
      }
      streamReader = new InputStreamReader(new FileInputStream(file));
      loggerReader = new AutoLoggerReader(streamReader);
      entry = loggerReader.readLogEntry();
      // Handle SWA and GAIN caterories.
      if(entry.getCategory().equalsIgnoreCase("SWA")) {
        entries.add(entry.getCallArea(),entry);
      } else if(entry.getCategory().equalsIgnoreCase("Gain")) {
        entries.add("Gain",entry);
      } else {
        logger.warning("Processing continues with unknown category: " + entry.getCategory());
        entries.add(entry.getCallArea(),entry);
      }
      streamReader.close();
    }
    logger.message("Log file processing finished.");
    
    // Sort the log entries based on the final score.
    entries.sort();
    Hashtable<String,LogEntry[]> result = entries.getSortedCollector();
    
    logger.message("Generating the results...");
    // Generate an HTML file with score results.
    FileOutputStream ostream = new FileOutputStream(outputFilename);
    OutputStreamWriter writer = new OutputStreamWriter(ostream);
    AutoLoggerWriter loggerWriter = new AutoLoggerWriter(writer,inputFileTemplate);
    Vector<String> submissionOrder = getSubmissionOrder();
    loggerWriter.writeHTML(result,submissionOrder,mode,maxPower);
    logger.message("The results are in: " + outputFilename);
    writer.close();
  }
  
  private Vector<String> getSubmissionOrder() throws IOException {
    File file=new File(outputDir+"/"+submissionOrderFilename);
    if(!file.exists()) {
      file.createNewFile();
    }
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    Vector<String> v= new Vector<String>();
    while ((line = reader.readLine()) != null) {
      v.add(line);
    }
    reader.close();
    return v;
  }
 
  private boolean hasExtension(String filename, String extension) {
    return filename.endsWith(extension);
  }
  
  // Create/Update a file with callsigns in the submitted order.
  // This is needed for soapbox generation.
  private void updateSubmissionOrderList(Vector<String> callsigns) throws IOException {
    File file = new File(outputDir+"/"+submissionOrderFilename);
    FileWriter writer=null;
    if(file.exists()) {
      writer = new FileWriter(file,true); // update the file with new submissions
    } else {
      writer = new FileWriter(file,false); // create a new file
    }
    String callsign=null;
    Enumeration<String> e=callsigns.elements();
    while(e.hasMoreElements()) {
      callsign=e.nextElement();
      writer.write(callsign+"\n");
    }
    writer.close();
  }

}
