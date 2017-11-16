package com.borasoft.naqcc.sprint.utils;

public class Logger {
  static private Logger logger;
  
  static public Logger getInstance() {
    if(logger==null) {
      logger = new Logger();
    }
    return logger;
  }
  
  public void message(String s) {
    System.out.println(s);
  }
  
  public void info(String s) {
    System.out.println("INFO: " + s);
  }
    
  public void warning(String s) {
    System.out.println("WARNING: " + s);
  }
  
  public void error(String s) {
    System.err.println("ERROR: " + s);
  }
  
  public void trace(String component, String s) {
    System.out.println(component+": " + s);
  }

}
