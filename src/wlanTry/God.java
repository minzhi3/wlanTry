package wlanTry;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hamcrest.core.Is;


public class God implements Callable<GodResult>{
	public int time=0;
	int MTNum;
	int ThreadNum;
	Channel channel;
	Channel[] controlCh;
	DeviceMap dm;
	DebugOutput debugOutput;
	int APNum;
	int[] MTcount;
	public final int numSubPacket;
	Set<Integer> center;
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
			debugOutput=new DebugOutput("C:\\Users\\Huang\\mt\\"+"g.txt");
			channel=new Channel(this.ThreadNum);
			
			APNum=ThreadNum-MTNum;

			MTcount=new int[APNum];
			controlCh=new Channel[4];
			for (int i=0;i<APNum;i++){
				MTcount[i]=0;
				this.controlCh[i]=new Channel(this.MTNum+1);
			}
		}

		this.numSubPacket=(Param.timeData-1)/Param.timeControlSlot/(this.MTNum+1)+1;
	}
	@Override
	public GodResult call() throws Exception {
		if (dm==null) return null;
		//Initializing
		CyclicBarrier cb=new CyclicBarrier(ThreadNum,new Runnable(){
			@Override
			public void run() {
				debugOutput.time++;
				if (debugOutput.time%10==0){
					StringBuilder sb=new StringBuilder();
					for (int i=0;i<ThreadNum;i++){
						sb.append(channel.ch[i]);
						sb.append(' ');
					}
					sb.append(" -- ");
					for (int i=0;i<MTNum+1;i++){
						sb.append(controlCh[0].ch[i]);
						sb.append(' ');
					}
					debugOutput.output(sb.toString());
				}
			}
		});
		Object key=new Object();
		Device[] devices=new Device[ThreadNum];
		this.center=dm.getCenter();
		for (int i=0;i<ThreadNum;i++){
			int myAP=dm.getAPofIndex(i);
			if (myAP==-1) myAP=i;
			devices[i]=new Device(i, cb, key, channel,dm.getNeighbour(i));
			if (i>=APNum){
				devices[i].AP=dm.getAPofIndex(i);
			}
		}
		//Build request
		double pps=1/Param.packetRequestRates;
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
			if (!center.contains(i))
				continue;
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
		if (withDelayCount>0) {
			gr.DelayTime/=withDelayCount;
		}else {
			gr.DelayTime=0;
		}
		//DebugOutput.outputAlways("GOD "+APNum);
		debugOutput.close();
		return gr;
	}
	private boolean center(int partner2) {
		return center.contains(partner2);
	}

}
