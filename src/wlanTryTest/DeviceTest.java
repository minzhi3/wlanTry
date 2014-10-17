package wlanTryTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import wlanTry.*;

public class DeviceTest {
	ArrayList<Location> map;
	DeviceMapAP1 dm;
	RequestsQueue req1,req2,req3;
	private void neighborInit(){
		dm=new DeviceMapAP1(3,0.0001);
		map=new ArrayList<Location>();
		map.add(new Location(0,0));
		map.add(new Location(-30,0));
		map.add(new Location(30,0));
		map.add(new Location(0,30));
		dm.createMap(map);
	}
	private void requestsInit(){
		req1=new RequestsQueue();
		req2=new RequestsQueue();
		req3=new RequestsQueue();
		for (int i=0;i<1;i++){
			req1.addRequest(new Request(0, 500, 1, PacketType.DATA, 10, 100));
			req2.addRequest(new Request(0, 600, 2, PacketType.DATA, 10, 100));
			req3.addRequest(new Request(0, 700, 3, PacketType.DATA, 10, 100));
			//req1.addRequest(new Request(2, 310, 4, PacketType.DATA, 5, 30));
		}
		ArrayList<RequestsQueue> reqs=new ArrayList<RequestsQueue>();

		reqs.add(new RequestsQueue());
		reqs.add(req1);
		reqs.add(req2);
		reqs.add(req3);
		dm.setRequest(reqs);
	}
	@Test
	public void test() {
		final Channel dataChannel=new Channel(4);
		final Channel controlChannel=new Channel(4);
		final DebugOutput debugChannel=new DebugOutput(Param.outputPath+"channel.txt");

		final Object key=new Object();
		
		CyclicBarrier cb=new CyclicBarrier(4,new Runnable(){
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
		this.neighborInit();
		this.requestsInit();
		//dm=new DeviceMapAP1(3);
		//dm.createMap();
		//dm.createRequest(1/Param.packetRequestRates);
		Device d0 = null,d1=null,d2=null,d3=null;
		switch (Param.deviceType){
		case ControlChannelNACK:
			d0=new DeviceControlNACK(0, -1, cb, key, dataChannel, controlChannel, dm);
			d1=new DeviceControlNACK(1, 0, cb, key, dataChannel, controlChannel, dm);
			d2=new DeviceControlNACK(2, 0, cb, key, dataChannel, controlChannel, dm);
			d3=new DeviceControlNACK(3, 0, cb, key, dataChannel, controlChannel, dm);
			break;
		case CSMA:
			d0=new DeviceCSMA(0, -1, cb, key, dataChannel, dm);
			d1=new DeviceCSMA(1, 0, cb, key, dataChannel, dm);
			d2=new DeviceCSMA(2, 0, cb, key, dataChannel, dm);
			d3=new DeviceCSMA(3, 0, cb, key, dataChannel, dm);
			break;
		case ControlChannelRTS:
			d0=new DeviceControlRTS(0, -1, cb, key, dataChannel, controlChannel, dm);
			d1=new DeviceControlRTS(1, 0, cb, key, dataChannel, controlChannel, dm);
			d2=new DeviceControlRTS(2, 0, cb, key, dataChannel, controlChannel, dm);
			d3=new DeviceControlRTS(3, 0, cb, key, dataChannel, controlChannel, dm);
			break;
		default:
			break;
		}
		
		ExecutorService es = Executors.newCachedThreadPool();
		ArrayList<Future<DeviceResult>> results = new ArrayList<Future<DeviceResult>>();
		results.add(es.submit(d0));
		results.add(es.submit(d1));
		results.add(es.submit(d2));
		results.add(es.submit(d3));
		
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
			//System.out.println("MT"+i+": "+results2.get(i).getPacketRx()+" "+results2.get(i).getPacketTx());
		}
		debugChannel.close();
	}

}
