package com.example;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultProducerTemplate;
import org.apache.camel.spi.Synchronization;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(properties = "camel.springboot.main-run-controller=true")
public class SpringBootNarayanaApplicationTests {

	public static final int MAX_WAIT_TIME = 1000;


	@Autowired
	CamelContext camelContext;

	@Test
	public void contextLoads() throws InterruptedException {
		assertNotNull(camelContext);
		System.out.println("Sleeping....");
		TimeUnit.SECONDS.sleep(2);

		MockEndpoint mockOut = getMockEndpoint(camelContext, "mock:out");
		mockOut.whenAnyExchangeReceived(new ExceptionThrowingProcessor());

		ProducerTemplate template = camelContext.createProducerTemplate();
		ConsumerTemplate consumer = camelContext.createConsumerTemplate();

		String message = "this message will explode";
		template.sendBody("nonTxJms:inbound", message);

//		assertEquals(message, consumer.receiveBody("jms:ActiveMQ.DLQ", MAX_WAIT_TIME, String.class));

		// the send operation is performed while a database transaction is going on, so it is rolled back
		// on exception
		assertNull(consumer.receiveBody("jms:outbound", MAX_WAIT_TIME, String.class));
	}


	protected MockEndpoint getMockEndpoint(CamelContext context, String uri) throws NoSuchEndpointException {
		Endpoint endpoint = context.hasEndpoint(uri);
		if(endpoint instanceof MockEndpoint) {
			return (MockEndpoint)endpoint;
		} else {
			throw new NoSuchEndpointException(String.format("MockEndpoint %s does not exist.", new Object[]{uri}));
		}
	}


}
