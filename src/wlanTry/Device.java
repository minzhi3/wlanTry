package wlanTry;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
/**
 * Class of device in WLAN
 * Sending:set value of channel[id] as partner
 * Receiving:
 * 		sense the value of channel[range] is id. 
 * 		The id of device in "range" sending my id is partner.
 * @author Minzhi
 *
 */
public class Device implements Runnable {

	public static int[] channel;
	CyclicBarrier barrier;
	Object key;
	final int id;
	int count;
	int sendState;
	int receiveState;
	int partner;
	int senseRange;
	int state=1;
	int time;
	int contentionWindow;
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
				if (checkChannel()){
					receiveInit();
				}
			}
			if (state==1){
				sendNextStep();
			}
			if (state==2){
				receiveNextStep();
			}
			//if (time%100==0){
				try {
					barrier.await();
				} catch (BrokenBarrierException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//}
		}
		long endtime = System.nanoTime();
		double costTime = (endtime - begintime)/1e9;
		System.out.println(costTime);
	}
	// -----------------------RECEIVE------------------------
	/**
	 * Initialize parameters for receiving. 
	 */
	private void receiveInit() {
		state=2;
		receiveState=0;
		partner=channel[senseRange];
		count=0;
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkChannel() {
		return (channel[senseRange]!=0);
	}
	private void receiveNextStep() {
		count--;
		if (count<=0){
			switch (receiveState){
			case 0:
				if (channel[id]==0){
					receiveState=2;
					count=16;
				}
				else if (channel[id]!=partner)
					receiveState=1;
				break;
			case 1:
				receiveComplete();
				break;
			case 2:
				receiveState=3;
				count=40;
				synchronized(this.key){
					channel[id]=100;
				}
			case 3:
				synchronized(this.key){
					channel[id]=-1;
				}
				receiveComplete();
			}
		}
		// TODO Auto-generated method stub
		
	}
	private void receiveComplete() {
		receiveState=-1;
		state=0;
		
	}
	// -----------------------RECEIVE END---------------------
	

	// -----------------------SEND------------------------
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkRequest() {
		Random r=new Random();
		return r.nextDouble()<0.01;
	}
	/**
	 * Initialize parameters for sending. 
	 */
	private void sendInit(){
		state=1;
		sendState=0;
		count=34;
		contentionWindow=16;
		partner=id^1;
	}
	private void sendComplete(){
		sendState=-1;
		state=0;
	}
	private void sendNextStep(){
		boolean carrierSense=false;
		//for (int i=0;i<100;i++){
			//if (i==this.id) continue;
			if (channel[senseRange]!=-1) carrierSense=true; 
		//}
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
					count=new Random().nextInt(contentionWindow)*9+9;
					break;
				case 1://Back-off
					sendState=2;
					//System.out.println(this.time+": MT "+id+" is sending");
					count=1000;
					channel[id]=partner;
					break;
				case 2://Transmitting
					System.out.println(this.time+": MT "+id+" has finished sending");
					channel[id]=-1;
					sendComplete();
					break;
				}
			}
		}
	}
	// -----------------------SEND------------------------

}
