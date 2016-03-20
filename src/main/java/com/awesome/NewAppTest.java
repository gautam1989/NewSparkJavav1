package com.awesome;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.apache.spark.api.java.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;

public class NewAppTest {
	
	 private final static String QUEUE_NAME = "hello";
	
  public static void main(String[] args)  throws java.io.IOException,
  java.lang.InterruptedException, TimeoutException {
    

    
  List<Applicant> inputData = Arrays.asList(
	      new Applicant(1, "John", "Doe", 10000, 568),
	      new Applicant(2, "John", "Greg", 12000, 654),
	      new Applicant(3, "Mary", "Sue", 100, 568),
	      new Applicant(4, "Greg", "Darcy", 1000000, 788),
	      new Applicant(5, "Jane", "Stuart", 10, 788)
	    );
    
    SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
    JavaSparkContext sc = new JavaSparkContext(conf);

    KieBase rules = loadRules();
    Broadcast<KieBase> broadcastRules = sc.broadcast(rules);

    JavaRDD<Applicant> applicants = sc.parallelize(inputData);

    long numApproved = applicants.map( a -> applyRules(broadcastRules.value(), a) )
                                 .filter( a -> a.isApproved() )
                                 .count();
    
    List<Applicant> selectedApplicants=applicants.map( a -> applyRules(broadcastRules.value(), a) ).filter(a->a.isApproved()).collect();

    selectedApplicants.forEach(a->System.out.println(a.getFirstName()));
    System.out.println("Number of applicants approved: " + numApproved);
  }

  public static KieBase loadRules() {
    KieServices kieServices = KieServices.Factory.get();
    
    KieFileSystem kfs = kieServices.newKieFileSystem();
	kfs.write("src/main/resources/com/awesome/app.drl",ResourceFactory
			.newClassPathResource("app.drl"));
	
	KieBuilder kb = kieServices.newKieBuilder(kfs);
	kb.buildAll(); // kieModule is automatically deployed to KieRepository
					// if successfully built.

	if(kb.getResults().hasMessages(Level.ERROR)){
		throw new RuntimeException("Build Errors:\n"
				+ kb.getResults().toString());
	}
	
	final KieRepository kr = kieServices.getRepository();
	
	
	KieContainer kContainer = kieServices.newKieContainer(kr.getDefaultReleaseId());

    return kContainer.getKieBase();
  }

  public static Applicant applyRules(KieBase base, Applicant a) {
    StatelessKieSession session = base.newStatelessKieSession();
    session.execute(CommandFactory.newInsert(a));
    return a;
  }
}
