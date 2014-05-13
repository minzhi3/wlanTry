package wlanTry;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Device implements Runnable {

	public static int[] channel;
	CyclicBarrier barrier;
	Object key;
	final int id;
	int count;
	int sendState;
	int state=1;
	int time;
	public Device(int id,CyclicBarrier cb,Object o) {
		this.barrier=cb;
		this.key=o;
		this.id=id;
    } 
	@Override
	public void run() {
		long begintime = System.nanoTime();
		for (time=0;time<1000000;time++){
			if (state==0){
				if (checkRequest()){
					sendInit();
				};
			}
			if (state==1){
				sendNextStep();
			}
			if (time%100==0){
				try {
					barrier.await();
				} catch (BrokenBarrierException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		long endtime = System.nanoTime();
		double costTime = (endtime - begintime)/1e9;
		System.out.println(costTime);
	}
	private boolean checkRequest() {
		Random r=new Random();
		return r.nextDouble()<0.01;
	}
	private void sendInit(){
		state=1;
		sendState=0;
		count=34;
	}
	private void sendComplete(){
		sendState=-1;
		state=0;
	}
	private void sendNextStep(){
		boolean carrierSense=false;
		for (int i=0;i<100;i++){
			if (i==this.id) continue;
			if (channel[i]!=0) carrierSense=true; 
		}
		if (carrierSense){
			switch (sendState){
			case 0://DIFS
				count=34;
				break;
			case 2://Transmitting
				count--;
				break;
			}
		}else{
			switch (sendState){
			default:
				count--;
				break;
			}
		}
		
		if (count<=0){
			synchronized(this.key){
				switch (sendState){
				case 0://DIFS
					sendState=1;
					count=new Random().nextInt(16)*9+9;
					break;
				case 1://Back-off
					sendState=2;
					//System.out.println(this.time+": MT "+id+" is sending");
					count=1000;
					channel[id]=1;
					break;
				case 2://Transmitting
					System.out.println(this.time+": MT "+id+" has finished sending");
					channel[id]=0;
					sendComplete();
					break;
				}
			}
		}
	}

}
