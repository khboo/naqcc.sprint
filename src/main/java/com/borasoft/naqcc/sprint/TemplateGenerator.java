package com.borasoft.naqcc.sprint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.borasoft.naqcc.sprint.utils.Logger;

public class TemplateGenerator {
	private String filename;
	private InputStream html;
	private BufferedReader reader;
	private Logger logger=Logger.getInstance();
	public TemplateGenerator(String filepath) throws FileNotFoundException {
		html=new FileInputStream(new File(filepath));
		filename=filepath.substring(0,filepath.lastIndexOf("."));
		reader = new BufferedReader(new InputStreamReader(html,Charset.forName("UTF-8")));
	}
	public TemplateGenerator(String filepath, InputStream is) {
		filename=filepath.substring(0,filepath.lastIndexOf("."));
		reader = new BufferedReader(new InputStreamReader(is));
	}
	private String getTemplateFilename() {
		return filename+".template";
	}
	public void run() throws IOException {
		OutputStream template=new FileOutputStream(new File(getTemplateFilename()));
		BufferedWriter out=new BufferedWriter(new OutputStreamWriter(template,Charset.forName("UTF-8")));
		String line=null;
		while((line=reader.readLine())!=null) {
			//logger.trace("TemplateGenerator",line);
			out.write(line);
			out.newLine();
			// Turn the timer on
			
			// Find "SWA - STRAIGHT KEY CATEGORY"
			if(line.contains("SWA - STRAIGHT KEY CATEGORY")) {
				insertReplacementTag(out,"KEY");
			}
			// Find "SWA - BUG CATEGORY"
			else if(line.contains("SWA - BUG CATEGORY")) {
				insertReplacementTag(out,"BUG");
			}			
			// Find "SWA - KEYER/KEYBOARD CATEGORY"
			else if(line.contains("SWA - KEYER/KEYBOARD CATEGORY")) {
				insertReplacementTag(out,"KEYER");
			}			
			// Find "GAIN ANTENNA CATEGORY"
			else if(line.contains("class=\"blueboldmedium\">GAIN ANTENNA CATEGORY")) {
				insertReplacementTagForGain(out);
			}		
			// Find SOAPBOX
			else if(line.contains("SOAPBOX:")) {
				logger.info("Inserting a tag for SOAPBOX...");
				out.write("${SOAPBOX}");
				out.newLine();				
			}
		}
		reader.close();
		out.close();
	}
	private void insertReplacementTagForGain(BufferedWriter out) throws IOException {
		String line=null;
		logger.info("Inserting tags for GAIN Antenna...");
		while((line=reader.readLine())!=null) {
			if(line.contains("Straight Key")) {
				out.write(line);
				out.newLine();
				out.write("${GainKEY}");
				out.newLine();
			} else if(line.contains("Bug")){
				out.write(line);
				out.newLine();
				out.write("${GainBUG}");
				out.newLine();		
			} else if(line.contains("Keyer/Keyboard")){
				out.write(line);
				out.newLine();
				out.write("${GainKEYER}");
				out.newLine();	
				break;
			} else {
				out.write(line);
				out.newLine();
			}
		}
	}
	private void insertReplacementTag(BufferedWriter out,String string) throws IOException {
		String line=null;
		logger.info("Inserting tags: "+string+"...");
		int area=1;
		while((line=reader.readLine())!=null) {
			if(line.contains("W"+area+" Division")) {
				out.write(line);
				out.newLine();
				out.write("${W"+area+string+"}");
				area=area+1;
				out.newLine();
			} else if(line.contains("W0 Division")){
				out.write(line);
				out.newLine();
				out.write("${W0"+string+"}");
				out.newLine();		
			} else if(line.contains("Canada Division")){
				out.write(line);
				out.newLine();
				out.write("${Canada"+string+"}");
				out.newLine();	
			} else if(line.contains("DX Division")){
				out.write(line);
				out.newLine();
				out.write("${DX"+string+"}");
				out.newLine();	
				break;
			} else {
				out.write(line);
				out.newLine();
			}
		}
	}
}
