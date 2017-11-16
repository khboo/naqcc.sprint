package com.borasoft.naqcc.sprint;

import java.util.Vector;

/*
 * Replace occurrences of symbol with content from inputTemplate. outputHTML contains the contents of
 * inputTemplate with all the symbols replaced with the content value provided. Each member in content
 * is written to outputHEML with the new line character added at the end.
 * replaceTokens can be called as many times as needed.
 * Example)
 *   StringBuffer template = ...
 *   StringBuffer html = ...
 *   Vector<String> scores1 = ...
 *   replaceTokens(template,html,"$score_category_1",scores1);
 *   Vector<String> scores2 = ...
 *   replaceTokens(template,html,"$score_category_2",scores2);
 */
public interface GenerateHTML {
  public void replaceTokens(StringBuffer inputTemplate, StringBuffer outputHTML, String symbol, Vector<String> content); 
}
