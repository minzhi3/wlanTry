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

import wlanTry.Channel;
import wlanTry.DebugOutput;
import wlanTry.DeviceControlNACK;
import wlanTry.DeviceResult;
import wlanTry.PacketType;
import wlanTry.Param;
import wlanTry.Request;
import wlanTry.RequestsQueue;

public class DeviceTest {
	ArrayList<Integer> neighbor0;
	ArrayList<Integer> neighbor1;
	ArrayList<Integer> neighbor2;
	RequestsQueue req1,req2,req0;
	private void neighborInit(){
		neighbor0=new ArrayList<Integer>();
		neighbor0.add(0);
		neighbor0.add(1);
		neighbor2=new ArrayList<Integer>();
		neighbor2.add(1);
		neighbor2.add(2);
		neighbor1=new ArrayList<Integer>();
		neighbor1.add(0);
		neighbor1.add(1);
		neighbor1.add(2);
	}
	private void requestsInit(){
		req0=new RequestsQueue();
		req1=new RequestsQueue();
		req2=new RequestsQueue();
		for (int i=0;i<100;i++){
			req0.addRequest(new Request(1, 500*i+10, 1, PacketType.DATA, 10, 30));
			req1.addRequest(new Request(0, 500*i+100, 2, PacketType.DATA, 10, 30));
			req2.addRequest(new Request(1, 500*i+210, 3, PacketType.DATA, 10, 30));
			//req1.addRequest(new Request(2, 310, 4, PacketType.DATA, 5, 30));
		}
	}
	@Test
	public void test() {
		final Channel dataChannel=new Channel(3);
		final Channel controlChannel=new Channel(3);
		final DebugOutput debugChannel=new DebugOutput(Param.outputPath+"channel.txt");
		CyclicBarrier cb=new CyclicBarrier(3,new Runnable(){
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
		Object key=new Object();
		this.neighborInit();
		this.requestsInit();
		DeviceControlNACK d0=new DeviceControlNACK(0, -1, cb, key, dataChannel, controlChannel, neighbor0, req0);
		DeviceControlNACK d1=new DeviceControlNACK(1, -1, cb, key, dataChannel, controlChannel, neighbor1, req1);
		DeviceControlNACK d2=new DeviceControlNACK(2, -1, cb, key, dataChannel, controlChannel, neighbor2, req2);
		
		ExecutorService es = Executors.newCachedThreadPool();
		ArrayList<Future<DeviceResult>> results = new ArrayList<Future<DeviceResult>>();
		results.add(es.submit(d0));
		results.add(es.submit(d1));
		results.add(es.submit(d2));
		
		ArrayList<DeviceResult> results2=new ArrayList<DeviceResult>();
		for (int i=0;i<3;i++){
			try {
				results2.add(results.get(i).get());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (int i=0;i<3;i++){
			System.out.println("MT"+i+": "+results2.get(i).getPacketRx()+" "+results2.get(i).getPacketTx()+" "+results2.get(i).getDelayTime());
		}
	}

}
