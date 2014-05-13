package wlanTry;

import java.util.concurrent.CyclicBarrier;


public class God {
	public static int time=0;
	public void run() throws InterruptedException{
		int threadNum = 100;
		Device.channel=new int[threadNum];
		CyclicBarrier cb=new CyclicBarrier(threadNum,null);
		Object key=new Object();
		for (int i=0;i<threadNum;i++){
			new Thread(new Device(i,cb,key)).start();
		}
	}

}
