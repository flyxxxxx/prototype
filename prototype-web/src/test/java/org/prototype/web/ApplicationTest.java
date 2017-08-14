package org.prototype.web;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 应用程序测试
 * 
 * @author lj
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration(classes = MvcApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class ApplicationTest {

	@LocalServerPort
	private int port = 0;

	@Resource
	private ApplicationContext context;

	@Test
	public void test(){
		
	}
}

