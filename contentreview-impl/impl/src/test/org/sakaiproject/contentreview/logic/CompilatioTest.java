package org.sakaiproject.contentreview.logic;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.contentreview.impl.compilatio.CompilatioReviewServiceImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration({"/hibernate-test.xml", "/spring-hibernate.xml"})
public class CompilatioTest extends AbstractTransactionalJUnit4SpringContextTests {
	private static final Log log = LogFactory.getLog(CompilatioTest.class);

	@Test
	public void testFileEscape() {
		CompilatioReviewServiceImpl compilatioService = new CompilatioReviewServiceImpl();
		String someEscaping = compilatioService.escapeFileName("Practical%203.docx", "contentId");
		Assert.assertEquals("Practical_3.docx", someEscaping);
		
		someEscaping = compilatioService.escapeFileName("Practical%203%.docx", "contentId");
		Assert.assertEquals("contentId", someEscaping);
		
		someEscaping = compilatioService.escapeFileName("Practical3.docx", "contentId");
		Assert.assertEquals("Practical3.docx", someEscaping);
		
		
	}
	
	
}
