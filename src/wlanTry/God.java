package wlanTry;

import java.util.concurrent.CyclicBarrier;


public class God {
	public static int time=0;
	public void run() throws InterruptedException{
		int Num = 7;
		Device.channel=new int[Num];
		//Initializing
		for (int i=0;i<Num;i++){
			Device.channel[i]=-1;
		}
		//Map
		DeviceMap dm=new DeviceMap();
		dm.addDevice(25, 25);//AP
		for (int i=0;i<Num-1;i++){
			dm.addDevice(25, 25+i);
		}
		//Initializing
		CyclicBarrier cb=new CyclicBarrier(Num,null);
		Object key=new Object();
		Device[] devices=new Device[Num];
		for (int i=0;i<Num;i++){
			devices[i]=new Device(i, cb, key, dm.getNeighbour(i));
		}
		//Build request
		double pps=1/(1.0/1500/8);
		devices[0].buildRequestList(pps, 1, Num-1, 0);
		for (int i=1;i<Num;i++){
			devices[i].buildRequestList(pps, 0, 0, 1000);
		}

		//Start
		for (int i=0;i<Num;i++){
			new Thread(devices[i]).start();
		}
	}

}
