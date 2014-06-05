package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Main {

	public static void main(String[] args) throws Exception {
		//DebugOutput.fileDebugInit(args[0]);
		DebugOutput.isDebug=false;
		GodResult sum[]=new GodResult[100];
		int numAP=4;
		int numMT=25;
		int RP=5;
		//God g=new God(1,1);
		//g.call();
		
		for (int repeat=0;repeat<RP;repeat++){
			ArrayList<Future<GodResult>> results = new ArrayList<Future<GodResult>>();
			ExecutorService es = Executors.newCachedThreadPool();
			DebugOutput.outputAlways("#"+repeat+"#");
			long begintime = System.nanoTime();

			
			for (int i=0;i<numMT*numAP;i+=numAP){
				results.add(es.submit(new God(i,numAP)));
			}
			for (int i=0;i<numMT;i++){
				if (sum[i]==null){
					sum[i]=new GodResult();
				}
				sum[i].add(results.get(i).get());
			}
			es.shutdown();

			long endtime = System.nanoTime();
			double costTime = (endtime - begintime)/1e9;
			DebugOutput.outputAlways("Num="+repeat+" Time:"+costTime);
		}
		for (int i=0;i<numMT;i++){
			sum[i].div((double)RP);
			DebugOutput.outputAlways(sum[i].ThroughputTx+" "+sum[i].ThroughputRx+" "+sum[i].DelayTime+" ");
		}
		DebugOutput.outputAlways("Over");
		
		
		
	}

}
