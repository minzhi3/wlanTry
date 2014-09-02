package wlanTryTest;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import wlanTry.Channel;
import wlanTry.DebugOutput;
import wlanTry.DeviceControlNACK;
import wlanTry.DeviceMapAP1;
import wlanTry.DeviceMapAP4;
import wlanTry.DeviceResult;
import wlanTry.Location;
import wlanTry.PacketType;
import wlanTry.Param;
import wlanTry.Request;
import wlanTry.RequestsQueue;


public class DeviceTestAP4 {

	ArrayList<Location> map;
	DeviceMapAP4 dm;

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
				debugChannel.output(dataChannel.getTime()+":\t"+dataChannel.ToString());
				debugChannel.output("CH:\t"+controlChannel.ToString());
				debugChannel.output("\n");
				System.out.println(dataChannel.getTime());
			}
		});
		//this.neighborInit();
		//this.requestsInit();
		dm=new DeviceMapAP4(1);
		dm.createMap();
		dm.createRequest(1/Param.packetRequestRates);
		DeviceControlNACK d[]=new DeviceControlNACK[8];
		for (int i=0;i<4;i++){
			d[i]=new DeviceControlNACK(i, -1, cb, key, dataChannel, controlChannel, dm);
		}
		for (int i=0;i<4;i++){
			d[i+4]=new DeviceControlNACK(i+4, dm.getAPofIndex(i+4), cb, key, dataChannel, controlChannel, dm);
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
	}

}
