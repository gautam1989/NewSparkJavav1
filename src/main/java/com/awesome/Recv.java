package com.awesome;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;


public class Recv {
	  private final static String QUEUE_NAME = "hello";

	  public static void main(String[] argv)
	      throws java.io.IOException,
	             java.lang.InterruptedException, TimeoutException {

	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	   
	    
	    Consumer consumer = new DefaultConsumer(channel) {
	       @Override
	    public void handleDelivery(String consumerTag, Envelope envelope,
	    		BasicProperties properties, byte[] body) throws IOException {
	    	   String message = new String(body, "UTF-8");
	    	 
	    	   Type listType = new TypeToken<ArrayList<Applicant>>() {
               }.getType();
       List<Applicant> yourClassList = new Gson().fromJson(message, listType);
		    
       yourClassList.forEach(a->System.out.println(a.getFirstName()));
	    }
	      };
	      channel.basicConsume(QUEUE_NAME, true, consumer);
	    }
	}