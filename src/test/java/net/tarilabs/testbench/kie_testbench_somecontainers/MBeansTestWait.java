package net.tarilabs.testbench.kie_testbench_somecontainers;

import java.lang.management.ManagementFactory;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;

import org.drools.compiler.CommonTestMethodBase;
import org.drools.core.ClockType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.conf.MBeansOption;
import org.kie.api.management.KieContainerMonitorMXBean;
import org.kie.api.management.StatelessKieSessionMonitoringMXBean;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.KnowledgeBaseFactoryService;

public class MBeansTestWait extends CommonTestMethodBase {

    public static final String KSESSION1 = "KSession1";
	public static final String KBASE1 = "KBase1";
    private String mbeansprop;

    @Before
    public void setUp() throws Exception {
        mbeansprop = System.getProperty( MBeansOption.PROPERTY_NAME );
        System.setProperty( MBeansOption.PROPERTY_NAME, "enabled" );
        
    }

    @After
    public void tearDown()
            throws Exception {
    	if (mbeansprop != null) {
    		System.setProperty( MBeansOption.PROPERTY_NAME, mbeansprop );
    	} else {
    		System.setProperty( MBeansOption.PROPERTY_NAME, MBeansOption.DISABLED.toString() );
    	}
    }
    
	@Test
    public void testContainerMBeans() throws Exception {
    	String drl = "package org.drools.compiler.test\n"+
    			"rule S\n" +
    			"when\n" +
    			"    String()\n" +
    			"then\n" +
    			"end";

    	KieServices ks = KieServices.Factory.get();

    	KieModuleModel kproj = ks.newKieModuleModel();

    	KieBaseModel kieBaseModel1 = kproj.newKieBaseModel( KBASE1 ).setDefault( true )
    			.setEventProcessingMode( EventProcessingOption.STREAM );
    	KieSessionModel ksession1 = kieBaseModel1.newKieSessionModel( KSESSION1 ).setDefault( true )
    			.setType( KieSessionModel.KieSessionType.STATEFUL )
    			.setClockType( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() ) );
    	kieBaseModel1.newKieSessionModel( "stateless" ).setDefault( false )
            .setType( KieSessionModel.KieSessionType.STATELESS )
            ;

    	ReleaseId releaseId1 = ks.newReleaseId( "org.kie.test", "mbeans", "1.0.0" );
    	createKJar( ks, kproj, releaseId1, null, drl );
    	
    	KieContainer kc = ks.newKieContainer( releaseId1 );
    	KieBase kiebase = kc.getKieBase( KBASE1 );
    	    	
    	kc.newKieSession(KSESSION1);
    	kiebase.newKieSession();
    	
    	KieContainer kc2 = ks.newKieContainer( "Matteo", ks.newReleaseId("org.kie.test", "mbeans", "RELEASE" ) );
    	
//    	KieContainer kc2 = ks.newKieContainer( ks.newReleaseId("org.kie.test", "mbeans", "RELEASE" ) );
    	
    	kc2.newKieSession(KSESSION1);
    	
    	StatelessKieSession stateless = kc2.newStatelessKieSession("stateless");
    	stateless.execute("ciao");
    	stateless.execute("ciao");
    	stateless.execute("ciao");
    	
    	MBeanServer mbserver = ManagementFactory.getPlatformMBeanServer();
        System.out.println( mbserver.queryMBeans(new ObjectName("org.kie:kcontainerId="+ObjectName.quote("Matteo")+",*"), Query.isInstanceOf(Query.value(KieContainerMonitorMXBean.class.getName()))) );
    	
    	blockOnSystemINforENTER();
    }
    
    /**
     * Utility method to add locally in test methods so to deliberately block on system.in, allowing time to use jvisualvm to inspecti the JMX manually.
     */
    public static void blockOnSystemINforENTER() throws Exception {
    	System.err.println("***MATTEO***");
    	System.out.println("***MATTEO***");
    	Runnable task2 = new Runnable() {
			@Override
			public void run() { System.out.println("Press ENTER to continue: "); Scanner sin = new Scanner(System.in); sin.nextLine(); }
		};
    	ExecutorService es = Executors.newCachedThreadPool();
    	es.submit(task2).get();
    }
}
