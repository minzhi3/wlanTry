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
	protected Channel channel;

	final int timeLength=Param.simTimeLength;
	
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
	boolean canSend; //Flag for whether the MT have received data and can continue sending
	int storeState,storePartner;
	int beginSend;
	int backoffTime;
	DeviceResult ret;
	DebugOutput debugOutput;
	Pair receivedSignal;

	public Device(int id,CyclicBarrier cb,Object o,Channel ch,ArrayList<Integer> range) {
		this.barrier=cb;
		this.key=o;
		this.id=id;
		this.senseRange=range;
		this.state=0;
		this.request=new TransmissionRequest();
		this.AP=-1;
		this.contentionWindow=Param.sizeCWmin;
		this.channel=ch;
		ret=new DeviceResult();
		this.canSend=true;
		this.storeState=-1;
		debugOutput=new DebugOutput(Param.outputPath+"D"+this.id+".txt");
		this.beginSend=-1;
		this.backoffTime=0;
		this.receivedSignal=new Pair();
    } 
	@Override
	public DeviceResult call() throws Exception {
		for (time=0;time<timeLength;time++){
			this.receiveSignal();
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
		state=2;
		receiveState=0;
		partner=receivedSignal.id;
		count=0;
		debugOutput.output(this.time+": From "+this.partner+" receiving initialized");
	}
	/**
	 * check whether there is packet transmitting in channel.
	 * @return boolean (state of channel).
	 */
	private boolean checkChannel() {
		return (receivedSignal.value==id);
	}
	/**
	 * received signal from neighbor terminal
	 * @return 
	 * integer[2]=(id, value);
	 */
	protected void receiveSignal(){
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
					id=99999;
					val=99999;
					break;
				}
			}
		}
		ret.id=id; ret.value=val;
		this.receivedSignal=ret;
	}
	/**
	 * The transformation for receiving state
	 * State 0: Receiving Data
	 * State 1: SIFS
	 * State 2: Sending ACK
	 * State 3: Receiving is failed
	 */
	protected void receiveNextStep() {
		boolean carrierSense=false;
		int signal=receivedSignal.value;
		if (signal!=-1) carrierSense=true; 
		
		if (carrierSense){
			switch (receiveState){
			case 0:
				if (signal!=id){
					debugOutput.output(this.time+": From "+this.partner+" Error happens");
					receiveState=3;
				}
				break;
			case 1:
				debugOutput.output(this.time+": From "+this.partner+" SIFS reset");
				count=Param.timeSIFS;
				break;
			default:
				count--;
			}
		}else{
			switch (receiveState){
			case 0://Receiving Data finished
				debugOutput.output(this.time+": From "+this.partner+"receiving finished, SIFS starts");
				receiveState=1;
				count=Param.timeSIFS;
				break;
			case 3://Receiving is failed
				this.receiveComplete(false);
				break;
			default:
				count--;
			}
		}
		if (count<=0){
			switch (receiveState){
			case 1://Starting sending ACK
				receiveState=2;
				debugOutput.output(this.time+": To "+this.partner+" sending ACK");
				count=Param.timeACK;
				synchronized(this.key){
					this.channel.ch[id]=1000+this.partner;
				}
				break;
			case 2:
				debugOutput.output(this.time+": To "+this.partner+" finished sending ACK");
				synchronized(this.key){
					this.channel.ch[id]=-1;
				}
				this.receiveComplete(true);
				break;
			}
		}
	}
	protected void receiveComplete(boolean success) {
		if (success){
			this.ret.packetRx+=1;
			debugOutput.output(this.time+": From "+this.partner+" receive Complete");
			if (Param.withDownlink){
				if (this.AP<0){
					this.replyDataAP();
				}else{
					this.canSend=true;
				}
			}
		}
		else{
			this.ret.packetRxFails+=1;
			debugOutput.output(this.time+": From "+this.partner+" receivie is failed");
		}
		receiveState=-1;
		state=0;
		
	}
	protected void replyDataAP(){
		this.request.addRequest(this.time, this.partner);
	}
	// -----------------------RECEIVE END---------------------
	

	// -----------------------SEND------------------------
	public void buildRequestList(double pps, int a, int b, int count){
		request.buildRequestList(pps, a, b, count);
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<Param.numRequest;i++){
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
		//if (this.canSend==0 && this.time-request.getTime().time>10000){
			//canSend=1;
		//}
		/*
		if (this.time-request.getTime().time>100000){
			request.popFront();
		}
		*/
		if (Param.withDownlink)
			return (this.time>request.getTime().time && this.canSend);
		else
			return this.time>request.getTime().time;
	}
	/**
	 * Initialize parameters for sending. 
	 */
	protected void sendInit(){
		state=1;
		sendState=0;
		if (storeState>=0) 
			sendResume();
		else{
			count=Param.timeDIFS;
			partner=request.getTime().id;
		}
		if (beginSend<0) beginSend=this.time;
		debugOutput.output(this.time+": To "+this.partner+" tranmission initialized");
	}
	protected void sendComplete(boolean success){
		if (success){
			this.ret.sumDelay+=(this.time-beginSend);
			if (Param.withDownlink){
				if (this.AP>=0)
					this.canSend=false;
			}
			this.ret.packetTx+=1;
			debugOutput.output(this.time+": To "+this.partner+" tranmission successful");
			request.popFront();
			this.contentionWindow=Param.sizeCWmin;
			this.beginSend=-1;
		}
		else{
			this.ret.packetTxFails+=1;
			debugOutput.output(this.time+": To "+this.partner+" tranmission failed");
			this.changeCW();
		}
		sendState=-1;
		state=0;
	}
	protected void changeCW() {
		if (this.contentionWindow<Param.sizeCWmax){
			this.contentionWindow*=2;
		}
	}
	protected void sendInterrupt(){
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
	
	/**
	 * The transformation among different send state.
	 * State 0: DIFS
	 * State 1: Backoff
	 * State 2: Transmitting
	 * State 3: Waiting ACK
	 * State 4: Receiving ACK
	 * State 5: ACK failed
	 */
	protected void sendNextStep(){
		boolean carrierSense=false;
		int signal=receivedSignal.value;
		//for (int i=0;i<100;i++){
			//if (i==this.id) continue;
		if (signal==id){
			if (sendState==0 || sendState==1){
				sendInterrupt();
				return;
			}
		}
		if (signal!=-1) carrierSense=true; 
		//}
		//The transformation when count>0 and carriersense
		if (carrierSense){
			switch (sendState){
			case 0://DIFS
				count=Param.timeDIFS;
				break;
			case 1://Backoff
				sendState=0;
				count=Param.timeDIFS;
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
			case 1:
				backoffTime--;
				count--;
				break;
			case 4:
				count=0;
				
				break;
			default:
				count--;
				break;
			}
		}
		
		//The tranformation when count is 0
		if (count<=0){
			synchronized(this.key){
				switch (sendState){
				case 0://DIFS
					sendState=1;
					if (backoffTime<=0) 
						backoffTime=(new Random().nextInt(contentionWindow)+1)*Param.timeSlot;
					count=backoffTime;
					debugOutput.output(this.time+": To "+this.partner+" backoff "+count);
					break;
				case 1://Back-off
					sendState=2;
					debugOutput.output(this.time+": To "+this.partner+" transmitting");
					count=Param.timeData;
					this.channel.ch[id]=partner;
					break;
				case 2://waiting ACK
					debugOutput.output(this.time+": To "+this.partner+" waiting ACK");
					this.channel.ch[id]=-1;
					sendState=3;
					count=Param.timeEIFS;//EIFS
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
