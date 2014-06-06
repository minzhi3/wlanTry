package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class God implements Callable<GodResult>{
	public int time=0;
	int MTNum;
	int ThreadNum;
	Channel channel;
	DeviceMap dm;
	DebugOutput debugOutput;
	int APNum;
	public God(int n,int type){
		this.MTNum=n;
		switch (type){
		case 1:
			dm=new DeviceMapAP1();
			break;
		case 4:
			dm=new DeviceMapAP4();
			break;
		default:
			dm=null;
		}
		if (dm!=null) {
			dm.createMap(MTNum);
			this.ThreadNum=dm.getDeviceNum();
			debugOutput=new DebugOutput();
			channel=new Channel(this.ThreadNum);
			APNum=ThreadNum-MTNum;
		}
	}
	@Override
	public GodResult call() throws Exception {
		if (dm==null) return null;
		//Initializing
		CyclicBarrier cb=new CyclicBarrier(ThreadNum,new Runnable(){
			@Override
			public void run() {
				debugOutput.time++;
				//if (DebugOutput.time%10000==0){
					//DebugOutput.outputAlways("Time="+DebugOutput.time);
				//}
			}
		});
		Object key=new Object();
		Device[] devices=new Device[ThreadNum];
		for (int i=0;i<ThreadNum;i++){
			devices[i]=new Device(i, cb, key, channel, dm.getNeighbour(i));
			if (i>=APNum){
				devices[i].AP=dm.getAPofIndex(i);
			}
		}
		//Build request
		double pps=1/(1.0/3000/8);
		//devices[0].buildRequestList(pps, 1, Num-1, 0);
		for (int i=APNum;i<ThreadNum;i++){
			devices[i].buildRequestList(pps, devices[i].AP, devices[i].AP, 2000);
		}

		//Start
		ArrayList<Future<DeviceResult>> results = new ArrayList<Future<DeviceResult>>();
		ExecutorService es = Executors.newCachedThreadPool();
		for (int i=0;i<ThreadNum;i++){
			results.add(es.submit(devices[i]));
		}
		GodResult gr=new GodResult();
		
		int withDelayCount=0;
		for (int i=APNum;i<ThreadNum;i++){
			try {
				gr.add(results.get(i).get());
				double delay=results.get(i).get().getDelayTime();
				if (delay>0){
					gr.DelayTime+=delay;
					withDelayCount++;
				}
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		es.shutdown();
		//gr.ThroughputRx/=(ThreadNum-APNum);
		//gr.ThroughputTx/=(ThreadNum-APNum);
		gr.DelayTime/=withDelayCount;
		//DebugOutput.outputAlways("GOD "+APNum);
		return gr;
	}

}
