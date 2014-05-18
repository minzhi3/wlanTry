package wlanTry;

import java.util.concurrent.CyclicBarrier;


public class God {
	public static int time=0;
	public void run() throws InterruptedException{
		int Num = 8;
		Device.channel=new int[Num+4];
		//Initializing
		for (int i=0;i<Num+4;i++){
			Device.channel[i]=-1;
		}
		//Map
		DeviceMap dm=new DeviceMap();
		dm.createMap(Num);
		
		//Initializing
		CyclicBarrier cb=new CyclicBarrier(Num+4,null);
		Object key=new Object();
		Device[] devices=new Device[Num+4];
		for (int i=0;i<Num+4;i++){
			devices[i]=new Device(i, cb, key, dm.getNeighbour(i));
			if (i>=4){
				devices[i].AP=dm.getAPofIndex(i);
			}
		}
		//Build request
		double pps=1/(2.0/1500/8);
		//devices[0].buildRequestList(pps, 1, Num-1, 0);
		for (int i=4;i<Num+4;i++){
			devices[i].buildRequestList(pps, devices[i].AP, devices[i].AP, 1000);
		}

		//Start
		for (int i=0;i<Num+4;i++){
			new Thread(devices[i]).start();
		}
	}

}
