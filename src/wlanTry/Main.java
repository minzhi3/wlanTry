package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Main {

	public static void main(String[] args) throws Exception {
		//DebugOutput.fileDebugInit(args[0]);
		double sum[]=new double[100];
		int numAP=1;
		int numMT=25;
		int RP=1;
		for (int repeat=0;repeat<RP;repeat++){
			ArrayList<Future<Double>> results = new ArrayList<Future<Double>>();
			ExecutorService es = Executors.newCachedThreadPool();
			DebugOutput.outputAlways("#"+repeat+"#");
			long begintime = System.nanoTime();

			
			for (int i=0;i<numMT*numAP;i+=numAP){
				results.add(es.submit(new God(i,numAP)));
			}
			for (int i=0;i<numMT;i++){
				sum[i]+=results.get(i).get();
			}
			es.shutdown();

			long endtime = System.nanoTime();
			double costTime = (endtime - begintime)/1e9;
			DebugOutput.outputAlways("Num="+repeat+" Time:"+costTime);
		}
		for (int i=0;i<numMT;i++){
			DebugOutput.outputAlways(sum[i]/RP);
		}
		DebugOutput.outputAlways("Over");
		/*
		CyclicBarrier cb=new CyclicBarrier(2,new Runnable(){
			public void run(){
				System.out.println("Barrier:"+ThreadTest.f2);
			}
		});
		Object o=new Object();
		ThreadTest r1=new ThreadTest(cb,o);
		ThreadTest r2=new ThreadTest(cb,o);
		Thread t1=new Thread(r1);
		Thread t2=new Thread(r2);
		t1.start();
		t2.start();
		*/
		
	}

}
