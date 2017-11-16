package com.borasoft.naqcc.sprint;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public final class EntryCollector {
  private Hashtable<String,Vector<LogEntry>> collector = new Hashtable<String,Vector<LogEntry>>();
  private Hashtable<String,LogEntry[]> sortedCollector = new Hashtable<String,LogEntry[]>();
  
  public void add(String callArea, LogEntry entry) {
    if(!collector.containsKey(callArea)) {
      collector.put(callArea, new Vector<LogEntry>());
    }
    collector.get(callArea).add(entry);    
  }
  
  public void sort() {
    String area = null;
    Vector<LogEntry> v = null;
    Enumeration<String> enumerator = collector.keys(); // call areas
    QuickSort qsort = new QuickSort();
    while(enumerator.hasMoreElements()) {
      area = enumerator.nextElement();
      v = collector.get(area); // all the entries in the current area
      sortedCollector.put(area, qsort.quickSort(v.toArray(new LogEntry[1]))); // sort and store
    }     
  }
  
  public Hashtable<String,LogEntry[]> getSortedCollector() {
    return sortedCollector;
  }

}
