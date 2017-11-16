package com.borasoft.naqcc.sprint;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AutoLoggerReader {

	private BufferedReader reader;
	String[] fields = new String[] 
	       {"CALLSIGN:","EMAIL:","CATEGORY:","CALLAREA:",
			"ANTENNAS:","POWER:","QSOS:","MEMBER_QSOs:",
			"MULTIPLIERS:","BONUS_MULT:","LOG:","SOAPBOX:"};
	
	public AutoLoggerReader(InputStreamReader reader) {
		this.reader = new BufferedReader(reader);
	}
	
	public LogEntry readLogEntry() throws IOException {
		String line;
		int index;
		String buffer="";
		LogEntry entry = new LogEntry();
		
		while((line=reader.readLine())!=null) {
			if(line.trim().isEmpty()) {
				continue;
			}
			for(int i=0; i<fields.length; i++) {
				index = line.indexOf(fields[i]);
				if(index!=-1) {
					buffer = line.substring(fields[i].length()+index).trim();
					switch(i) {
						case 0: // CALLSIGN
							entry.setCallsign(buffer);
							break;
						case 1: // EMAIL
							entry.setEmail(buffer);
							break;
						case 2: // CATEGORY
							entry.setCategory(buffer);
							break;
						case 3: // CALLAREA
							entry.setCallArea(buffer);
							break;
						case 4: // ANTENNAS
							entry.setAntenna(buffer);
							break;
						case 5: // POWER
							entry.setPower(buffer);
							break;
						case 6: // QSOS
							entry.setQSOs(buffer);
							break;
						case 7: // MEMBER_QSOs
							entry.setMemberQSOs(buffer);
							break;
						case 8: // MULTIPLIERS
							entry.setMultipliers(buffer);
							break;
						case 9: // BONUS_MULT
							entry.setBonusMult(buffer);
							break;
						case 10: break; // LOG - do not handle
						case 11: //SOAPBOX
							while((line=reader.readLine())!=null) {
								buffer += "\n";
								buffer += line;
							}
							entry.setSoapbox(buffer);
							break; 
					}
				} 
			}
			//System.out.println(buffer);
			buffer="";
		}
		return entry;
	}
	
	// test only
	public static void main(String[] args) throws IOException {
		FileInputStream stream = new FileInputStream("autologger_sample.txt");
	  //FileInputStream stream = new FileInputStream("part0.txt");
		InputStreamReader reader = new InputStreamReader(stream);
		AutoLoggerReader logReader = new AutoLoggerReader(reader);
		LogEntry record = logReader.readLogEntry();
		reader.close();	
		System.exit(0);
	}

}
