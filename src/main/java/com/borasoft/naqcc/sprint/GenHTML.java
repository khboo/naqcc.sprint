package com.borasoft.naqcc.sprint;

import java.util.Vector;
import java.util.Iterator;

public class GenHTML implements GenerateHTML {	
	public void replaceTokens(StringBuffer inputTemplate,StringBuffer outputHTML, String symbol, Vector<String> content) {
		int idx;
		int	start= 0;		
		idx = inputTemplate.indexOf(symbol);
		if(idx==-1) { // Return inputTemplate content as is since the symbol is not found - i.e. user error
		  outputHTML.append(inputTemplate.substring(start));
		  return;
		}
		outputHTML.append(inputTemplate.subSequence(start, idx));	//	inputTemplate to substring
		Iterator<String> itr = content.iterator(); 
		while(itr.hasNext()){
			outputHTML.append(itr.next());
			//outputHTML.append("<br>");
		}
		outputHTML.append(inputTemplate.substring(idx+symbol.length()));
	}
}

