package org.jboss.fis.example.springboot.camel.amq;

import static org.junit.Assert.*;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@MockEndpoints
@UseAdviceWith
public class CamelContextXml2Test {
	
	protected Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
    CamelContext context;

    @Autowired
    ProducerTemplate producer;
    
	@Before
	public void configureMocks() throws Exception {
        context.getRouteDefinition("simple-route")
            .adviceWith(context, new AdviceWithRouteBuilder() {
              @Override
              public void configure() throws Exception {
            	// mocking endpoints can be done through annotations
  		        //mockEndpoints();
  			    replaceFromWith("direct:input");
  		        // weaveById("log:route-log").after().to("mock:result");

                 // weaveByToString(".*myEndPointId.*")
                 //     .replace()
                 //     .to(MOCK_MY_ENDPOINT);
              }
            });
	}
		
	@Test
	public void testRouterunning() throws Exception {
		context.start();
		assertTrue(context.getRouteStatus("simple-route").isStarted()); 
		context.stop();
	}
	
	@Test
	public void testCamelRoute() throws Exception {
		context.start();
     	MockEndpoint inputEndpoint = MockEndpoint.resolve(context, "mock:log:com.sandbox.input");
		MockEndpoint resultEndpoint = MockEndpoint.resolve(context, "mock:log:com.sandbox.output");
        // log.info("endpoint id:" + resultEndpoint.getId());
        // log.info("endpoint name:" + resultEndpoint.getName());
		inputEndpoint.expectedMessageCount(1);
		inputEndpoint.expectedBodiesReceived("<hello>world!</hello>");
        resultEndpoint.expectedMessageCount(1);
        producer.sendBody("direct:input", "<hello>world!</hello>");
        // Validate our expectations
        inputEndpoint.assertIsSatisfied();
        resultEndpoint.assertIsSatisfied();     	
        context.stop();
		
	}
}
