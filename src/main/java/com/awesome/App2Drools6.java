package com.awesome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;



public class App2Drools6 {
public static void main(String[] args) {
	
	
	KieServices ks = KieServices.Factory.get();
	final KieRepository kr = ks.getRepository();
	kr.addKieModule(new KieModule() {
		@Override
		public ReleaseId getReleaseId() {
			return kr.getDefaultReleaseId();
		}
	});
	KieFileSystem kfs = ks.newKieFileSystem();
	kfs.write("src/main/resources/com/awesome/app.drl",ResourceFactory
			.newClassPathResource("app.drl"));
	
	
	
	KieBuilder kb = ks.newKieBuilder(kfs);
	kb.buildAll(); // kieModule is automatically deployed to KieRepository
					// if successfully built.

	if(kb.getResults().hasMessages(Level.ERROR)){
		throw new RuntimeException("Build Errors:\n"
				+ kb.getResults().toString());
	}
	
	
	
	KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
	KieSession ksession=kContainer.newKieSession();
	
	/*Applicant a1=new Applicant(1, "John", "Doe", 10000, 700);
	ksession.insert(a1);
	System.out.println(a1.isApproved());
	ksession.fireAllRules();
	System.out.println(a1.isApproved());*/
	
	
	 List<Applicant> inputData = Arrays.asList(
		      new Applicant(1, "John", "Doe", 10000, 568),
		      new Applicant(2, "John", "Greg", 12000, 654),
		      new Applicant(3, "Mary", "Sue", 100, 568),
		      new Applicant(4, "Greg", "Darcy", 1000000, 788),
		      new Applicant(5, "Jane", "Stuart", 10, 788)
		    );
	 SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
	    JavaSparkContext sc = new JavaSparkContext(conf);
	    JavaRDD<Applicant> applicants = sc.parallelize(inputData);
	    Broadcast<KieSession> brdSession=sc.broadcast(ksession);
	    
	    List<Applicant> selectedApplicants=applicants.map( a -> applyRules(brdSession.value(), a) ).filter(a->a.isApproved()).collect();
	 // ksession.fireAllRules();
	    System.out.println("FINAL");
	   selectedApplicants.forEach(a->System.out.println(a.getCreditScore()));

}

public static Applicant applyRules(KieSession Ksession, Applicant a) {
    //StatelessKieSession session = base.newStatelessKieSession();
    Ksession.execute(CommandFactory.newInsert(a));
    return a;
  }

}
