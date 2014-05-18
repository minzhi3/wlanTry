package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class God {
	public int time=0;
	private int Num;
	public God(int n){
		this.Num=n;
	}
	public void run() throws InterruptedException{
		
		Device.channel=new int[Num+1];
		//Initializing
		for (int i=0;i<Num+1;i++){
			Device.channel[i]=-1;
		}
		//Map
		DeviceMap dm=new DeviceMap();
		dm.createMapSingle(Num);
		
		//Initializing
		CyclicBarrier cb=new CyclicBarrier(Num+1,new Runnable(){
			@Override
			public void run() {
				DebugOutput.time++;
				//if (DebugOutput.time%10000==0){
					//DebugOutput.outputAlways("Time="+DebugOutput.time);
				//}
			}
		});
		Object key=new Object();
		Device[] devices=new Device[Num+1];
		for (int i=0;i<Num+1;i++){
			devices[i]=new Device(i, cb, key, dm.getNeighbour(i));
			if (i>=1){
				devices[i].AP=0;
			}
		}
		//Build request
		double pps=1/(2.0/3000/8);
		//devices[0].buildRequestList(pps, 1, Num-1, 0);
		for (int i=1;i<Num+1;i++){
			devices[i].buildRequestList(pps, devices[i].AP, devices[i].AP, 1000);
		}

		//Start
		ArrayList<Future<Double>> results = new ArrayList<Future<Double>>();
		ExecutorService es = Executors.newCachedThreadPool();
		for (int i=0;i<Num+1;i++){
			results.add(es.submit(devices[i]));
		}
		Double sum=(double) 0;
		for (int i=0;i<Num+1;i++){
			try {
				sum+=results.get(i).get();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DebugOutput.outputAlways(sum);
	}

}
