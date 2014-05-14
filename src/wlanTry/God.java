package wlanTry;

import java.util.concurrent.CyclicBarrier;


public class God {
	public static int time=0;
	public void run() throws InterruptedException{
		int threadNum = 3;
		Device.channel=new int[threadNum];
		//Initializing
		for (int i=0;i<threadNum;i++){
			Device.channel[i]=-1;
		}
		//Map
		DeviceMap dm=new DeviceMap();
		dm.addDevice(25, 25);//AP
		dm.addDevice(25, 20);
		dm.addDevice(25, 30);
		//Start
		CyclicBarrier cb=new CyclicBarrier(threadNum,null);
		Object key=new Object();
		for (int i=0;i<threadNum;i++){
			new Thread(new Device(i,cb,key,dm.getNeighbour(i))).start();
		}
	}

}
