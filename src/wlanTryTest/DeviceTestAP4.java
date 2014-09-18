package wlanTryTest;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import wlanTry.*;


public class DeviceTestAP4 {

	ArrayList<Location> map;
	DeviceMapAP4 dm;
	private void neighborInit(){
		dm=new DeviceMapAP4(1);
		map=new ArrayList<Location>();
		map.add(new Location(25,25));
		map.add(new Location(-25,25));
		map.add(new Location(-25,-25));
		map.add(new Location(25,-25));

		map.add(new Location(5,5));
		map.add(new Location(-5,5));
		map.add(new Location(-5,-5));
		map.add(new Location(5,-5));
		dm.createMap(map);
	}
	private void requestsInit(){
		RequestsQueue req1=new RequestsQueue();
		RequestsQueue req2=new RequestsQueue();
		RequestsQueue req3=new RequestsQueue();
		for (int i=0;i<100;i++){
			req1.addRequest(new Request(dm.getAPofIndex(5), 800*i+500, 5,PacketType.DATA, 10, 100));
			req2.addRequest(new Request(dm.getAPofIndex(6), 800*i+500, 6, PacketType.DATA, 10, 100));
			req3.addRequest(new Request(dm.getAPofIndex(7), 800*i+500, 7, PacketType.DATA, 10, 100));
			//req1.addRequest(new Request(2, 310, 4, PacketType.DATA, 5, 30));
		}
		ArrayList<RequestsQueue> reqs=new ArrayList<RequestsQueue>();
		
		for (int i=0;i<5;i++){
			reqs.add(new RequestsQueue());
		}
		reqs.add(req1);
		reqs.add(req2);
		reqs.add(req3);
		dm.setRequest(reqs);
	}
	@Test
	public void test() {
		final Channel dataChannel=new Channel(8);
		final Channel controlChannel=new Channel(8);
		final DebugOutput debugChannel=new DebugOutput(Param.outputPath+"channel.txt");

		final Object key=new Object();
		
		CyclicBarrier cb=new CyclicBarrier(8,new Runnable(){
			@Override
			public void run() {
				dataChannel.tic();
				controlChannel.tic();
				int time=dataChannel.getTime();
				debugChannel.outputInit(time);
				if (time%1!=0) return;
				String channelString=dataChannel.ToString();
				String controlString=controlChannel.ToString();
				if (channelString!=null)
					debugChannel.output(channelString);
				if (controlString!=null)
					debugChannel.output(controlString);
				debugChannel.outputToFile();
			}
		});
		//this.neighborInit();
		//this.requestsInit();
		dm=new DeviceMapAP4(1);
		dm.createMap();
		//dm.createRequest(1/Param.packetRequestRates);
		this.neighborInit();
		this.requestsInit();
		Device d[]=new Device[8];
		for (int i=0;i<4;i++){
			switch (Param.deviceType){
			case ControlChannelNACK:
				d[i]=new DeviceControlNACK(i, -1, cb, key, dataChannel, controlChannel, dm);
				break;
			case ControlChannelRTS:
				d[i]=new DeviceControlRTS(i, -1, cb, key, dataChannel, controlChannel, dm);
				break;
			case CSMA:
				break;
			default:
				break;
			}
		}
		for (int i=0;i<4;i++){
			switch (Param.deviceType){
			case ControlChannelNACK:
				d[i+4]=new DeviceControlNACK(i+4, dm.getAPofIndex(i+4), cb, key, dataChannel, controlChannel, dm);
				break;
			case ControlChannelRTS:
				d[i+4]=new DeviceControlRTS(i+4, dm.getAPofIndex(i+4), cb, key, dataChannel, controlChannel, dm);
				break;
			case CSMA:
				break;
			default:
				break;
			}
		}

		ExecutorService es = Executors.newCachedThreadPool();
		ArrayList<Future<DeviceResult>> results = new ArrayList<Future<DeviceResult>>();
		for (int i=0;i<8;i++){
			results.add(es.submit(d[i]));
		}
		
		ArrayList<DeviceResult> results2=new ArrayList<DeviceResult>();
		for (int i=0;i<4;i++){
			try {
				results2.add(results.get(i).get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i=0;i<4;i++){
			System.out.println("MT"+i+": "+results2.get(i).getThroughputRx()+" "+results2.get(i).getThroughputTx()+" "+results2.get(i).getDelayTime());
		}
		debugChannel.close();
	}

}
