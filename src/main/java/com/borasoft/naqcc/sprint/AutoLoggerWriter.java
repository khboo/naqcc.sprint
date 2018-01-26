package com.borasoft.naqcc.sprint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.borasoft.naqcc.sprint.utils.Logger;

// TODO: Externalize the maximum power limit for the sprint. Currently it is set to 1 watt.

public class AutoLoggerWriter {
	private PrintWriter writer;
	private final String[] callAreas = {
	    "W1","W2","W3","W4","W5","W6","W7","W8","W9","W0","Canada","DX","Gain"
	};
	private Logger logger=Logger.getInstance();
	
	private double maximumPower;
	private int sprintMode;
	private String inputFileTemplate;

	public AutoLoggerWriter(OutputStreamWriter writer, String inputFileTemplate) {
		this.writer = new PrintWriter(writer);
		this.inputFileTemplate = inputFileTemplate;
	}
	
	public void writeHTML(Hashtable<String,LogEntry[]> entries,Vector<String> submissionOrder,int mode,double maxPower) throws IOException{
	  sprintMode=mode;
	  maximumPower=maxPower;
	  generateScores(entries);
	}
	
	private void generateScores(Hashtable<String,LogEntry[]> entries) throws IOException {
	  String key;
	  LogEntry[] entryArray;
	  LogEntry entry;

	  StringBuffer outputHTML = new StringBuffer();
    GenerateHTML htmlGen = getHTMLGen();
    StringBuffer inputTemplate = readInTemplate(inputFileTemplate);
    StringBuffer temp = inputTemplate;
    
    Vector<String> stringsKey;
    Vector<String> stringsBug;
    Vector<String> stringsKeyer;
    Vector<String> stringsKeyQRO;
    Vector<String> stringsBugQRO;
    Vector<String> stringsKeyerQRO;    
    Vector<String> soapbox = new Vector<String>();
    
	  for (int i=0; i<callAreas.length;i++) {
	    key = callAreas[i];
	    entryArray = entries.get(key);
	    stringsKey = new Vector<String>();
	    stringsBug = new Vector<String>();
	    stringsKeyer = new Vector<String>();
      stringsKeyQRO = new Vector<String>();
      stringsBugQRO = new Vector<String>();
      stringsKeyerQRO = new Vector<String>();	    
	    String bonus = "1";
	    boolean isQRO = false;
	    if(entryArray!=null) {
	      for(int j=entryArray.length-1; j>=0; j--) { // print in descending order
	        entry = entryArray[j];
	        isQRO = isQRO(entry); 
//	        if(isQRO) {
//	          entry.setCallsign("$"+entry.getCallsign());
//	        }
	        bonus = entry.getBonusMult();
	        if (bonus.equalsIgnoreCase("2")) {
	          if(isQRO) {
	            stringsKeyQRO.add(writeFormattedScore(entry));
	          } else {
	            stringsKey.add(writeFormattedScore(entry));
	          }
	        } else if (bonus.equalsIgnoreCase("1.5")) {
	          if(isQRO) {
	            stringsBugQRO.add(writeFormattedScore(entry));
	          } else {
	            stringsBug.add(writeFormattedScore(entry));
	          }
	        } else { // "1"
	          if(isQRO) {
	            stringsKeyerQRO.add(writeFormattedScore(entry));
	          } else {
	            stringsKeyer.add(writeFormattedScore(entry));
	          }
	        }
	        // Build soapbox content
	        if(entry.getSoapbox()!=null && entry.getSoapbox().trim().length() != 0) {
	          soapbox.add(entry.getCallsign()+" - "+entry.getSoapbox()+"<br><br>");
	        }
	      }
	    }
	    if(!stringsKeyQRO.isEmpty()) {
	      stringsKey.addAll(stringsKeyQRO);
	    }
	    if(!stringsBugQRO.isEmpty()) {
	      stringsBug.addAll(stringsBugQRO);
	    }
	    if(!stringsKeyerQRO.isEmpty()) {
	      stringsKeyer.addAll(stringsKeyerQRO);
	    }
	    // Now, we know all the participants in each category so let's get to work
	    htmlGen.replaceTokens(temp,outputHTML,"${"+key+"KEY}",stringsKey);
	    temp = outputHTML;
      outputHTML = new StringBuffer();
	    htmlGen.replaceTokens(temp,outputHTML,"${"+key+"BUG}",stringsBug);
	    temp = outputHTML;
      outputHTML = new StringBuffer();
	    htmlGen.replaceTokens(temp,outputHTML,"${"+key+"KEYER}",stringsKeyer);
	    temp = outputHTML;
	    outputHTML = new StringBuffer();
	  }
	  htmlGen.replaceTokens(temp,outputHTML,"${SOAPBOX}",soapbox);
	  writer.print(outputHTML);
	  writer.close();
	}
	
  private GenerateHTML getHTMLGen() {
    return new GenHTML();
  }	
  
  private StringBuffer readInTemplate(String filename) throws IOException {
    File file = new File(filename);
    BufferedReader br = new BufferedReader(new FileReader(file));
    StringBuffer fileContents = new StringBuffer();
    String line = br.readLine();
    while (line != null) {
        fileContents.append(line+"\n");
        line = br.readLine();
    }
    br.close();
    return fileContents;
  }
  
  private boolean isQRO(LogEntry entry) {
    PowerLexer lexer=new PowerLexer(entry.getPower());
    try {
      if(lexer.getPower()<=maximumPower) {
        return false;
      } else {
        return true;
      }
    } catch (Exception e) {
      logger.error("Unknown power: "+ entry.getPower()+ " from "+entry.getCallsign());
      logger.error("The log submission for "+entry.getCallsign()+" is not processed.");
      return false;
    }
  }
		
	private String writeFormattedScore(LogEntry entry) {
	  // formatting for callsign
    String callsign =  entry.getCallsign().trim().toUpperCase();
    if(isQRO(entry)) {
      callsign = "$"+callsign;
    }
    int length = callsign.length();
    if (length<6) {
      for(int k=0;k<6-length;k++) {
        callsign += " ";
      }
    }
    // formatting for bonus
    String bonus = entry.getBonusMult();
    if (bonus.equalsIgnoreCase("1")) {
      bonus = "";
    } else {
      bonus = "x" + bonus;
    }
    return String.format("%6s %4s %4s %3d %4s %4d %4s %5d  %s\n",callsign,entry.getQSOs(),entry.getMemberQSOs(),entry.getPoints(),entry.getMultipliers(),entry.getScore(),bonus,entry.getFinal(),entry.getAntenna());
  }
	
  public void generateSoapbox(Hashtable<String,LogEntry[]> entries,Vector<String> submissionOrder) {
    // <span class="blackboldmedium">SOAPBOX:</span><br>
    writer.println("<span class=\"blackboldmedium\">SOAPBOX:</span><br>");
    
    Enumeration<String> enu = entries.keys();
    String key;
    LogEntry[] entryArray;
    LogEntry entry;
    Hashtable<String,LogEntry> submissions = new Hashtable<String,LogEntry>();
    while(enu.hasMoreElements()) {
      key = enu.nextElement();
      entryArray = entries.get(key);
      for(int i=0;i<entryArray.length;i++) {
        entry = entryArray[i];
        submissions.put(entry.getCallsign(),entry);
      }
    }
    // Write the soapbox contents in 'submissions' according to the order in submissions.lst. 
    Enumeration<String> e=submissionOrder.elements();
    while(e.hasMoreElements()) {
      key=e.nextElement();
      logger.info("Generating soapbox comments for: "+key);
      entry=submissions.get(key);
      if(entry==null) { // there could be zero entries, '$'+key, or '@'+key
        entry=submissions.get("$"+key);
        if(entry==null) {
          entry=submissions.get("@"+key);
          if(entry==null) {
            continue;
          }
        }
      }
      if(entry.getSoapbox()!=null && entry.getSoapbox().trim().length() != 0) {
        writer.println(key + " - " + filterXMLEncodings(entry.getSoapbox()));
        //writer.println(entry.getCallsign() + " - " + entry.getSoapbox()); //TODO - remove $callsign, @callsign
        writer.println("<br/><br/>");
      }
    }
  }	
  private String filterXMLEncodings(String s) {
	  String t=s.replaceAll("&amp;","&");
	  t=t.replaceAll("&quot;", "'");
	  return t;
  }
 
  // test only - obsolete
  @SuppressWarnings("unused")
  public static void main(String[] args) throws FileNotFoundException, IOException {
		FileInputStream stream = new FileInputStream("autologger_sample.txt");
		InputStreamReader reader = new InputStreamReader(stream);
		AutoLoggerReader logReader = new AutoLoggerReader(reader);
		LogEntry logEntry = logReader.readLogEntry();
		reader.close();	
		
		FileOutputStream ostream = new FileOutputStream("autologger_sample.html");
		OutputStreamWriter writer = new OutputStreamWriter(ostream);
		AutoLoggerWriter loggerWriter = new AutoLoggerWriter(writer,"");
		//loggerWriter.writeHTML();
		writer.close();
	}

}
