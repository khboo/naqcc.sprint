package com.borasoft.naqcc.sprint;

// TODO: Need to handle some unexpected cases like below.
//         - <1W

public class PowerLexer {
  
    private String in;
    private double power =0.0; // in WATT
    private int cursor=0;
    
    public PowerLexer(String s) {
      in=s;
    }
    
    private void lex() throws Exception {
      // e.g .5w, 0.5W, 0,5 watt, 5/10w, 0.5mW, 0.5 mW, 0.5, etc...
      char c;
      while(cursor<in.length()) {
        c=in.charAt(cursor);
        if(c==' ') {
          cursor++;
          continue;
        } else if((c>='a' && c<='z')||(c>='A' && c<='Z')) {
          parseUnit();
          return; // stop parsing
        } else if(c=='.' || c==',' || (c>='0' && c<='9')) {
          parseNumber();
        }
      }
    }
    
    private void parseNumber() {
      // e.g. 5, 0.5, .5, 0,5, 5/10
      StringBuffer buf=new StringBuffer("");
      char c=in.charAt(cursor++);
      if(c=='.' || c==',') {
        buf.append("0.");
        while((c=in.charAt(cursor++))>='0' && c<='9') {
          buf.append(c);
          if(in.length()<=cursor) { // there is no unit specified in input. assume 'W'.
            cursor++; // compensate for cursor-- to be done before returning below.
            break;     
          } 
        }
        buf.trimToSize();
        power=Double.valueOf(buf.toString()).doubleValue();
      } else {
        buf.append(c);
        for(;cursor<in.length();cursor++) {
          c=in.charAt(cursor);
          if((c>='0' && c<='9') || c=='.') {
            if(c==',') {
              c='.';
            }
            buf.append(c);
          } else if(c=='/') { // e.g., 7/10
            buf.trimToSize();
            Double dividend=Double.valueOf(buf.toString()).doubleValue();
            buf=new StringBuffer("");
            for(;cursor<in.length();cursor++) {
              c=in.charAt(cursor);
              if(c>='0' && c<='9') {
                buf.append(c);
              }
            }
            buf.trimToSize();
            Double divider=Double.valueOf(buf.toString()).doubleValue();     
            power=dividend/divider;
            return;
          } else
              break;
        }
        cursor++;
        buf.trimToSize();
        power=Double.valueOf(buf.toString()).doubleValue();
      }
      cursor--;
      return;    
    }
    
    private void parseUnit() throws Exception {
      // e.g. w, W, mW, etc...
      // Here, as soon as we see the first character 'w', 'W', or 'm', we stop further parsing.
      // If we see something else, raise an exception.
      char c=in.charAt(cursor);
      if(c=='w' || c=='W') {
        // the value of power remains the same
      } else if (c=='m' || c=='M') {
        power = power * 0.001;
      } else {
        throw new Exception("Unknown power unit: " + c);
      }
    }
    
    public double getPower() throws Exception {
      lex();
      return power;
    }
    
    static public void main(String[] args) throws Exception {
      PowerLexer lexer=new PowerLexer("900mW");
      double d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");
      
      lexer=new PowerLexer("900 Mw");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");      
      
      lexer=new PowerLexer(".9 W");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");
      
      lexer=new PowerLexer("900 mW");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");
      
      lexer=new PowerLexer("0.9W");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");
      
      lexer=new PowerLexer("0.9w");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");
      
      lexer=new PowerLexer("0,8w");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");
      
      lexer=new PowerLexer("5/10 w");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");      
      
      lexer=new PowerLexer("0.9");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");

      lexer=new PowerLexer("9");
      d=lexer.getPower();
      System.out.println("Power is: "+d+"watts");      
    }
      
}
