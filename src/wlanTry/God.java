package wlanTry;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
