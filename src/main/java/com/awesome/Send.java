package com.awesome;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Send {
	  private final static String QUEUE_NAME = "hello";

	  public static void main(String[] argv)
	      throws java.io.IOException, TimeoutException {
		    ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    Connection connection = factory.newConnection();
		    Channel channel = connection.createChannel();
		    
		    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		    String message = "Hello World!2";
		    List<Applicant> inputData = Arrays.asList(
		  	      new Applicant(1, "John", "Doe", 10000, 568),
		  	      new Applicant(2, "John", "Greg", 12000, 654),
		  	      new Applicant(3, "Mary", "Sue", 100, 568),
		  	      new Applicant(4, "Greg", "Darcy", 1000000, 788),
		  	      new Applicant(5, "Jane", "Stuart", 10, 788)
		  	    );
		    Gson gson=new Gson();
		   // System.out.println(gson.toJson(inputData));
		    channel.basicPublish("", QUEUE_NAME, null, gson.toJson(inputData).getBytes());
		    System.out.println(" [x] Sent '" + message + "'");
		    
		    channel.close();
		    connection.close();
	  }
	}