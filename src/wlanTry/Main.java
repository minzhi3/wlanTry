package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Res{
	ArrayList<DeviceResult> sender;
	public Res(){
		sender=new ArrayList<DeviceResult>();
	}
}
public class Main {

	public static void main(String[] args){
		if (Param.isDebug)
			singleEvaluation();
		else
			multipleEvaluation();
	}
	private static void singleEvaluation(){
		int numAP=Param.numAP;
		int numMT=Param.numMT;
		int RP=Param.simRepeat;
		
		GodResult sum=new GodResult();
		ArrayList<Future<GodResult>> results = new ArrayList<Future<GodResult>>();
		ExecutorService es = Executors.newCachedThreadPool();
		
		long begintime = System.nanoTime();
		for (int i=0;i<RP;i++){
			results.add(es.submit(new God(numMT*numAP,numAP)));
		}
		for (int i=0;i<RP;i++){
			try {
				sum.add(results.get(i).get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		es.shutdown();
		sum.div((double)RP);
		
		long endtime = System.nanoTime();
		double costTime = (endtime - begintime)/1e9;
		DebugOutput.outputAlways(" Time:"+costTime);
		
		DebugOutput.outputAlways(sum.ThroughputTx/numAP+" "+sum.ThroughputRx/numAP+" "+sum.DelayTime+" ");
		DebugOutput.outputAlways("Over");
		
	}
	private static void multipleEvaluation(){
		int numAP=Param.numAP;
		int numMT=Param.numMT;
		int RP=Param.simRepeat;
		
		Res[] gods=new Res[numMT];
		
		GodResult sum[]=new GodResult[numMT];
		//God g=new God(1,1);
		//g.call();
		for (int i=0;i<numMT;i++)
			gods[i]=new Res();
		
		for (int repeat=0;repeat<RP;repeat++){
			ArrayList<Future<GodResult>> results = new ArrayList<Future<GodResult>>();
			ExecutorService es = Executors.newCachedThreadPool();
			DebugOutput.outputAlways("#"+repeat+"#");
			long begintime = System.nanoTime();
			God[] tmpgods=new God[numMT];
			
			for (int i=0;i<numMT;i+=1){
				tmpgods[i]=new God(i*numAP,numAP);
				results.add(es.submit(tmpgods[i]));
			}
			
			for (int i=0;i<numMT;i++){
				if (sum[i]==null){
					sum[i]=new GodResult();
				}
				try {
					sum[i].add(results.get(i).get());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			es.shutdown();

			long endtime = System.nanoTime();
			double costTime = (endtime - begintime)/1e9;
			DebugOutput.outputAlways("Num="+repeat+" Time:"+costTime);
			for (int k=0;k<numMT;k++)
				for (int j=4;j<tmpgods[k].sender.size();j++){
					gods[k].sender.add(tmpgods[k].sender.get(j));
			}
		}
		for (int i=0;i<numMT;i++){
			sum[i].div((double)RP);
			DebugOutput.outputAlways(sum[i].ThroughputTx/numAP+" "+sum[i].ThroughputRx/numAP+" "+sum[i].DelayTime+" ");
			//DebugOutput.outputAlways(sum[i].packetTx+" "+sum[i].packetTxFails);
			//DebugOutput.outputAlways(sum[i].packetRx+" "+sum[i].packetRxFails);
		}
		for (int k=0;k<numMT;k++){
			DebugOutput.outputAlways("MT: "+k);
			StringBuilder sb=new StringBuilder();
			for (int i=0;i<gods[k].sender.size();i++){
				sb.append(gods[k].sender.get(i).packetTx);
				sb.append(' ');
			}
			DebugOutput.outputAlways(sb.toString());
		}
		DebugOutput.outputAlways("Over");
	}

}
