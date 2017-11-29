package com.borasoft.naqcc.sprint;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class TemplateGeneratorTest extends TestCase {

	@Test 
	public void testPrintSprintHtml() {
		TemplateGenerator gen=null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sprint/sprint201712mw.html");
		gen=new TemplateGenerator("sprint201712mw.html",is);
		try {
			gen.run();
		} catch (IOException e) {
			Assert.fail("Template generation failed: "+e.getMessage());
		}
	}
}
