package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public class God implements Callable<GodResult>{
	int numMT;
	int ThreadNum;
	Channel dataChannel;
	Channel controlChannel;
	DeviceMap dm;

	public God(int n,int type){
		this.numMT=n;
		switch (type){
		case 1:
			dm=new DeviceMapAP1(numMT);
			break;
		case 4:
			dm=new DeviceMapAP4(numMT);
			break;
		default:
			dm=null;
		}
		if (dm!=null) {
			dm.createMap();
			
			double pps=1/Param.packetRequestRates;
			dm.createRequest(pps);
			this.ThreadNum=dm.getDeviceNum();

		}

	}
	@Override
	public GodResult call() throws Exception {
		if (dm==null) return null;
		//Initializing
		dataChannel=new Channel(ThreadNum);
		controlChannel=new Channel(ThreadNum);
		final DebugOutput debugChannel=new DebugOutput(Param.outputPath+"channel.txt");
		CyclicBarrier cb=new CyclicBarrier(ThreadNum,new Runnable(){
			@Override
			public void run() {
				dataChannel.tic();
				controlChannel.tic();
				debugChannel.output(dataChannel.getTime()+":\t"+dataChannel.ToString());
				debugChannel.output("CH:\t"+controlChannel.ToString());
				debugChannel.output("\n");
				//if (dataChannel.getTime()%10000==0) System.out.println(dataChannel.getTime());
			}
		});
		
		Object key=new Object();
		Device[] devices=new Device[ThreadNum];
		for (int i=0;i<ThreadNum;i++){//Add devices
			switch (Param.deviceType){
			case CSMA:
				devices[i]=new DeviceCSMA(i,dm.getAPofIndex(i),cb,key,dataChannel,dm);
				break;
			case ControlChannelNACK:
				devices[i]=new DeviceControlNACK(i,dm.getAPofIndex(i), cb,key, dataChannel, controlChannel, dm);
				break;
			default:
				break;
			}
		}
		//Start
		ArrayList<Future<DeviceResult>> results = new ArrayList<Future<DeviceResult>>();
		ExecutorService es = Executors.newCachedThreadPool();
		for (int i=0;i<ThreadNum;i++){
			results.add(es.submit(devices[i]));
		}
		GodResult gr=new GodResult();
		
		for (int i=Param.numAP;i<ThreadNum;i++){
			if (dm.inCenter(i)){
				try {
					gr.add(results.get(i).get());
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (Param.isDebug){
			for (int i=0;i<ThreadNum;i++){
				if (dm.inCenter(i)){
					System.out.println("MT"+i+": "+results.get(i).get().getThroughputRx()+" "+results.get(i).get().getThroughputTx()+" "+results.get(i).get().getDelayTime());
				}
			}
		}
		es.shutdown();
		//DebugOutput.outputAlways("GOD "+APNum);
		return gr;
	}

}
