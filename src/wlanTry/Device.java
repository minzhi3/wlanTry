package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

class Pair{
	public int id;
	public int value;
};
/**
 * Class of device in WLAN
 * Sending:set value of channel[id] as partner
 * Receiving:
 * 		sense the value of channel[range] is id. 
 * 		The id of device in "range" sending my id is partner.
 * @author Minzhi
 *
 */
public class Device implements Callable<DeviceResult> {
	private Channel channel;

	final int timeLength=1000000;
	
	public int AP;
	CyclicBarrier barrier;
	Object key;
	final int id;
	int count;
	int sendState;
	int receiveState;
	int partner;
	final ArrayList<Integer> senseRange;
	TransmissionRequest request;  //The time of sending data
	int state;
	int time;
	int contentionWindow;
	int canSend; //Flag for whether the MT have received data and can continue sending
	int storeState,storePartner;
	int beginSend;
	DeviceResult ret;
	DebugOutput debugOutput;

	public Device(int id,CyclicBarrier cb,Object o,Channel ch,ArrayList<Integer> range) {
		this.barrier=cb;
		this.key=o;
		this.id=id;
		this.senseRange=range;
		this.state=0;
		this.request=new TransmissionRequest();
		this.AP=-1;
		this.contentionWindow=16;
		this.channel=ch;
		ret=new DeviceResult(timeLength);
		this.canSend=0;
		this.storeState=-1;
		debugOutput=new DebugOutput("C:\\Users\\Huang\\mt\\"+this.id+".txt");
		this.beginSend=-1;
    } 
	@Override
	public DeviceResult call() throws Exception {
		this.canSend=1;
		for (time=0;time<timeLength;time++){
			if (state==0){
				if (checkChannel()){
					receiveInit();
				}
				if (checkRequest()){
					sendInit();
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
		debugOutput.close();
		return this.ret;
	}
	// -----------------------RECEIVE------------------------
	/**
	 * Initialize parameters for receiving. 
	 */
	private void receiveInit() {
		debugOutput.output(this.time+": From "+this.partner+" receiving initialized");
		state=2;
		receiveState=0;
		partner=receiveSignal().id;
		count=0;
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkChannel() {
		return (receiveSignal().value==id);
	}
	/**
	 * received signal from neighbor terminal
	 * @return 
	 * integer[2]=(id, value);
	 */
	private Pair receiveSignal(){
		Pair ret=new Pair();
		int id=-1;
		int val=-1;
		for (int i=0;i<senseRange.size();i++){
			int k=senseRange.get(i);
			int p=this.channel.ch[k];
			if (p!=-1){
				if (val==-1){
					val=p;
					id=k;
				}else{
					id=0xFFFF;
					val=0xFFFF;
					break;
				}
			}
		}
		ret.id=id; ret.value=val;
		return ret;
	}
	private void receiveNextStep() {
		boolean carrierSense=false;
		int signal=receiveSignal().value;
		if (signal!=-1) carrierSense=true; 

		if (count>0){
			switch (receiveState) {
			case 2:
				if (carrierSense){
					count=10;
				}else{
					count--;
				}
				break;
			default:
				count--;
				break;
			}
		}else{
			switch (receiveState){
			case 0:
				if (signal==-1){
					receiveState=2;
					count=10;
				}
				else if (signal!=id)
					receiveState=1;
				break;
			case 1:
				receiveComplete(false);
				break;
			case 2://start sending ACK
				debugOutput.output(this.time+": To "+this.partner+" sending ACK");
				receiveState=3;
				count=40;
				synchronized(this.key){
					this.channel.ch[id]=1000+this.partner;
				}
				break;
			case 3://finished sending ACK
				debugOutput.output(this.time+": To "+this.partner+" finished sending ACK");
				synchronized(this.key){
					this.channel.ch[id]=-1;
				}
				receiveComplete(true);
			}
		}
	}
	private void receiveComplete(boolean success) {
		if (success){
			this.ret.packetRx++;
			debugOutput.output(this.time+": From "+this.partner+" receiving Complete");
			if (this.AP<0){
				this.replyDataAP();
			}else{
				this.canSend=1;
			}
		}
		else{
			this.ret.packetRxFails++;
			debugOutput.output(this.time+": From "+this.partner+" receiving failed");
		}
		receiveState=-1;
		state=0;
		
	}
	private void replyDataAP(){
		this.request.addRequest(this.time, this.partner);
	}
	// -----------------------RECEIVE END---------------------
	

	// -----------------------SEND------------------------
	public void buildRequestList(double pps, int a, int b, int count){
		request.buildRequestList(pps, a, b, count);
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<40;i++){
			sb.append(request.requestList.get(i).time+":"+request.requestList.get(i).id);
			sb.append(' ');
		}
		debugOutput.output(sb.toString());
		
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkRequest() {
		if (request.getTime()==null) return false;
		//if (this.canSend>0 && this.time-request.getTime().time>100000){
			//canSend=1;
		//}
		/*
		if (this.time-request.getTime().time>100000){
			request.popFront();
		}
		*/
		return (this.time>request.getTime().time && this.canSend>0);
	}
	/**
	 * Initialize parameters for sending. 
	 */
	private void sendInit(){
		debugOutput.output(this.time+": To "+this.partner+" tranmission initialized");
		state=1;
		sendState=0;
		if (storeState>=0) 
			sendResume();
		else{
			count=34;
			partner=request.getTime().id;
		}
		if (beginSend<0) beginSend=this.time;
	}
	private void sendComplete(boolean success){
		if (success){
			this.ret.sumDelay+=(this.time-beginSend);
			if (this.AP>=0)
				this.canSend=0;
			this.ret.packetTx++;
			debugOutput.output(this.time+": To "+this.partner+" tranmission successful");
			request.popFront();
			this.contentionWindow=16;
			this.beginSend=-1;
		}
		else{
			this.ret.packetTxFails++;
			debugOutput.output(this.time+": To "+this.partner+" tranmission failed");
			if (this.contentionWindow<1024){
				this.contentionWindow*=2;
			}
		}
		sendState=-1;
		state=0;
	}
	private void sendInterrupt(){
		debugOutput.output(this.time+": To "+this.partner+" tranmission interruptted");
		if (sendState==1){
			storeState=count;
			storePartner=this.partner;
		}
		else storeState=-1;
		sendState=-1;
		receiveInit();
	}
	private void sendResume(){
		sendState=1;
		count=storeState;
		storeState=-1;
		this.partner=storePartner;
		storePartner=-1;
	}
	private void sendNextStep(){
		boolean carrierSense=false;
		int signal=receiveSignal().value;
		//for (int i=0;i<100;i++){
			//if (i==this.id) continue;
		if (signal==id){
			if (sendState==0 || sendState==1){
				sendInterrupt();
			}
		}
		if (signal!=-1) carrierSense=true; 
		//}
		if (carrierSense){
			switch (sendState){
			case 0://DIFS
				count=28;
				break;
			case 2://Transmitting
				count--;
				break;
			case 3:
				if (signal==1000+this.id){
					count=999;
					sendState=4;
					
				}
				break;
			case 4:
				if (signal!=1000+this.id){
					sendState=5;
					count=0;
				}
				break;
			}
		}else{
			switch (sendState){
			case 4:
				count=0;
				
				break;
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
					debugOutput.output(this.time+": To "+this.partner+" backoff "+count);
					break;
				case 1://Back-off
					sendState=2;
					debugOutput.output(this.time+": To "+this.partner+" transmitting");
					count=1000;
					this.channel.ch[id]=partner;
					break;
				case 2://waiting ACK
					debugOutput.output(this.time+": To "+this.partner+" waiting ACK");
					this.channel.ch[id]=-1;
					sendState=3;
					count=78;//EIFS
					break;
				case 3://No ACK
					debugOutput.output(this.time+": To "+this.partner+" No ACK");
					this.channel.debugOutput(this.time);
					sendComplete(false);
					break;
				case 4://receive ACK
					debugOutput.output(this.time+": To "+this.partner+" get ACK");
					sendComplete(true);
					break;
				case 5://No ACK
					debugOutput.output(this.time+": To "+this.partner+" ACK failed");
					sendComplete(false);
					break;
				}
			}
		}
	}
	// -----------------------SEND-----------------------

}
