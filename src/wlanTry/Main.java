package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Main {

	public static void main(String[] args){
		if (Param.isDebug)
			singleEvaluation();
		else
			multipleEvaluation();
	}
	private static void singleEvaluation(){
		int numAP=Param.numAP;
		int numMT=Param.maximumMT;
		int RP=Param.simRepeat;
		
		GodResult sum=new GodResult();
		ArrayList<Future<GodResult>> results = new ArrayList<Future<GodResult>>();
		ExecutorService es = Executors.newCachedThreadPool();
		
		long begintime = System.nanoTime();
		for (int i=0;i<RP;i++){
			if (Param.deviceType==DeviceType.ControlChannelRTS)
				results.add(es.submit(new GodQueue(Param.fixedMT,numAP,Param.fixedError)));
			else
				results.add(es.submit(new God(Param.fixedMT,numAP,Param.fixedError)));
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
		
		long endtime = System.nanoTime();
		double costTime = (endtime - begintime)/1e9;
		DebugOutput.outputAlways(" Time:"+costTime);
		
		DebugOutput.outputAlways(sum.getThroughputRx()/numAP+" "+sum.getThroughputTx()/numAP+" "+sum.getDelayTime()+" MT:"+sum.getNum());
		DebugOutput.outputAlways("Over");
		
	}
	private static void multipleEvaluation(){
		int numAP=Param.numAP;
		int numMT=Param.maximumMT;
		int RP=Param.simRepeat;

		GodResult sum[]=new GodResult[numMT];
		//God g=new God(1,1);
		//g.call();
		int cnt=0;
		for (int repeat=0;repeat<RP;repeat++){
			ArrayList<Future<GodResult>> results = new ArrayList<Future<GodResult>>();
			ExecutorService es = Executors.newCachedThreadPool();
			DebugOutput.outputAlways("#"+repeat+"#");
			long begintime = System.nanoTime();
			

			if (Param.vsBER){
				cnt=0;
				for (double p=Param.minError;p<Param.maxError;p*=Math.pow(10, 0.25)){
					cnt++;
					if (Param.deviceType==DeviceType.ControlChannelRTS)
						results.add(es.submit(new GodQueue(Param.fixedMT,numAP,p)));
					else
						results.add(es.submit(new God(Param.fixedMT,numAP,p)));
				}
			}else{
				cnt=numMT;
				for (int i=0;i<numMT;i++){
					if (Param.deviceType==DeviceType.ControlChannelRTS)
						results.add(es.submit(new GodQueue(i,numAP,Param.fixedError)));
					else
						results.add(es.submit(new God(i,numAP,Param.fixedError)));
				}
			}
			for (int i=0;i<cnt;i++){
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
			DebugOutput.outputAlways("RP="+repeat+" Time:"+costTime);
		}
		double cntp=Param.minError;
		for (int i=0;i<cnt;i++){
			//DebugOutput.outputAlways(sum[i].ge/tThroughputRx()/numAP+" "+sum[i].getThroughputTx()/numAP+" "+sum[i].getDelayTime()+" ");
			//DebugOutput.outputAlways((Param.vsBER?cntp:"")+" "+sum[i].getRxPerDevice()+" "+sum[i].getTxPerDevice()+" "+sum[i].getDelayTime()+" ");
			DebugOutput.outputAlways((Param.vsBER?cntp:"")+" "+sum[i].getRxPerDevice()+sum[i].getTxPerDevice());
			cntp*=Math.pow(10, 0.25);
			//DebugOutput.outputAlways(sum[i].packetTx+" "+sum[i].packetTxFails);
			//DebugOutput.outputAlways(sum[i].packetRx+" "+sum[i].packetRxFails);
		}
		DebugOutput.outputAlways("Over");
	}

}
