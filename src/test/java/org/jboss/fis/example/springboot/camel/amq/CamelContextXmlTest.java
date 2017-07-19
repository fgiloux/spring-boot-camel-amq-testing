package org.jboss.fis.example.springboot.camel.amq;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

// CamelSpringTestSupport base classes provide feature parity with the simple CamelTestSupport classes from Camel Test
// but do not support Spring annotations on the test class such as @Autowired, @DirtiesContext, and @ContextConfiguration
// http://camel.apache.org/spring-testing.html
public class CamelContextXmlTest extends CamelSpringTestSupport {
	
	@Override
	protected AbstractApplicationContext createApplicationContext() {
		AnnotationConfigApplicationContext ctx =  new AnnotationConfigApplicationContext(Application.class);
		return ctx;
	}
	
	@Override
	public boolean isUseAdviceWith() {
        return true;
    }

	@Override
	public void setUp() throws Exception {
	    replaceRouteFromWith("simple-route", "direct:input");
	    super.setUp();
	}
	
	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
	    return new RouteBuilder() {
	    	@Override
	        public void configure() throws Exception {
	            // no routes added by default
	    		context.getRouteDefinition("simple-route").adviceWith(context, new AdviceWithRouteBuilder() {
	    		    @Override
	    		    public void configure() throws Exception {
	    		        // mock all endpoints
	    		        mockEndpoints();
//	    	        	from("log:com.sandbox.message?level=INFO").to(resultEndpoint);
//	    	        	weaveById("log:route-log").after().to("mock:result");
	    		    }
	    		});
	        }
	    };
	}
	
	@Test
	public void testRouterunning() throws Exception {
		context.start();
			assertTrue(context().getRouteStatus("simple-route").isStarted());
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
        sendBody("direct:input", "<hello>world!</hello>");
        // Validate our expectations
     	assertMockEndpointsSatisfied();
        context.stop();
		
	}
}
