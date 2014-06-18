package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

class PairDualChannel{
	public int id;
	public int value;
	public int control;
};

public class DeviceDualChannel extends Device {
	private Channel controlChannel;
	PairDualChannel receivedSignal;
	int receiveCount;
	int sendCount;
	int sendControlState;
	int receiveControlState;
	int SlotNum;
	int countControl;
	final int timeSubPacket;
	final int numSub;
	int numSubCurrentPacket;
	final int myNum;
	
	public DeviceDualChannel(int i, CyclicBarrier cb, Object key,
			Channel channel,Channel controlCh, ArrayList<Integer> neighbour, int numSub, int myNum) {
		super(i, cb, key, channel, neighbour);
		this.timeSubPacket=Param.timeData/numSub;
		this.numSub=numSub;
		this.myNum=myNum;
		this.controlChannel=controlCh;
		this.receivedSignal=new PairDualChannel();

	}
	
	//--------------------------------RECEIVE-BEGIN-----------------------------------------
	/**
	 * The transformation for receiving state
	 * State 0: Receiving Data
	 * State 1: Receiving CRC
	 * State 2: Receiving is finished
	 * 
	 * For control state
	 * State 0: Receiving
	 * State 1: WaitSlot ACK
	 * State 2: WaitSlot NACK
	 * State 3: Sending ACK
	 * State 4: Sending NACK
	 * State 5: Waiting Final ACK
	 * State 6: Sending Final ACK
	 */
	protected void receiveNextStep() {
		int signal=receivedSignal.value;
		
		if (signal!=-1){
			switch (receiveState){
			case 0:
				if (signal!=id){
					if (signal!=id+500){
						debugOutput.output(this.time+": From "+this.partner+" Error happens");
						this.receiveControlState=2;
						super.receiveState=2;
					}else{
						receiveState=1;
					}
				}
				break;
			case 1:
				if (signal!=id+500){
					this.receiveCount+=1;
					this.receiveControlState=1;
					receiveState=0;
					debugOutput.output(this.time+": From "+this.partner+" received sub at "+this.receiveCount);
				}
				break;
			}
		}else{
			switch (receiveState){
			case 1:
				this.receiveCount+=1;
				this.receiveControlState=5;
				receiveState=2;
				debugOutput.output(this.time+": From "+this.partner+" received sub at "+this.receiveCount);
			}
		}
		if (this.SlotNum%(Param.numMT+1)==this.myNum){
			synchronized (key) {
				switch (receiveControlState){
				case 1:
					this.controlChannel.ch[id]=1000+this.partner;
					receiveControlState=3;
					break;
				case 2:
					this.controlChannel.ch[id]=-1000-this.partner;
					receiveControlState=4;
					break;
				case 5:
					this.controlChannel.ch[id]=1000+this.partner;
					receiveControlState=6;
					break;
				}
			}
		}else{
			synchronized (key) {
				switch (receiveControlState){
				case 3:
					this.controlChannel.ch[id]=-1;
					receiveControlState=0;
					debugOutput.output(this.time+": To "+this.partner+" sended ACK at "+this.receiveCount);
					break;
				case 4:
					this.controlChannel.ch[id]=-1;
					receiveControlState=0;
					receiveComplete(false);
					debugOutput.output(this.time+": To "+this.partner+" sended NACK at "+this.receiveCount);
					break;
				case 6:
					this.controlChannel.ch[id]=-1;
					receiveControlState=0;
					receiveComplete(true);
					debugOutput.output(this.time+": To "+this.partner+" sended final ACK ");
					break;
				}
			}
		}
		
	}
	protected void receiveComplete(boolean success) {
		if (success){
			this.ret.packetRx+=(double)this.receiveCount/this.numSub;
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
			this.ret.packetRx+=(double)this.receiveCount/this.numSub;
			debugOutput.output(this.time+": From "+this.partner+" receivie is failed at "+this.receiveCount);
		}
		receiveState=-1;
		state=0;
		
	}
	//--------------------------------RECEIVE-END-----------------------------------------
	protected void receiveSignal(){
		int id=-1;
		int val=-1;
		int control=-1;
		for (int i=0;i<senseRange.size();i++){
			int k=senseRange.get(i);
			int p=super.channel.ch[k];
			int pc=this.controlChannel.ch[k];
			if (p!=-1){
				if (val==-1){
					val=p;
					id=k;
				}else{
					val=99999;
					break;
				}
			}
			if (pc!=-1){
				if (control==-1){
					control=pc;
					id=k;
				}else{
					control=99999;
					break;
				}
			}
			if (control>10000 && val>10000){
				id=99999;
				break;
			}
		} 
		this.receivedSignal.id=id;
		this.receivedSignal.value=val;
		this.receivedSignal.control=control;
		
		super.receivedSignal.id=val<10000?id:99999;
		super.receivedSignal.value=val;
		this.SlotNum=super.time/Param.timeControlSlot/Param.numMT;
	}
	//------------------------------------------SEND----------------------------------------------------
	public void buildRequestList(double pps, int a, int b, int count){
		request.buildRequestList(pps, a, b, count);
		StringBuilder sb=new StringBuilder();
		for (int i=0;i<Param.numRequest;i++){
			sb.append(request.requestList.get(i).time+":"+request.requestList.get(i).id);
			sb.append(' ');
		}
		for (int j=0;j<super.request.requestList.size();j++){
			super.request.requestList.get(j).numSub=this.numSub;
		}
		debugOutput.output(sb.toString());
		
	}
	protected void sendInit(){
		super.sendInit();
		this.receiveCount=0;
		this.sendControlState=0;
		this.countControl=0;
		this.sendCount=0;
		this.numSubCurrentPacket=this.request.getTime().numSub;
	}
	protected void sendComplete(boolean success){
		if (success){
			this.ret.sumDelay+=(this.time-beginSend);
			if (Param.withDownlink){
				if (this.AP>=0)
					this.canSend=false;
			}
			this.ret.packetTx+=(double)this.receiveCount/this.numSub;
			debugOutput.output(this.time+": To "+this.partner+" tranmission successful");
			request.popFront();
			this.contentionWindow=Param.sizeCWmin;
			this.beginSend=-1;
		}
		else{
			this.ret.packetTx+=(double)this.receiveCount/this.numSub;
			debugOutput.output(this.time+": To "+this.partner+" tranmission failed at "+ this.receiveCount);
			request.getTime().numSub-=this.receiveCount;
			if (request.getTime().numSub<=0){
				request.popFront();
				debugOutput.output("but finished");
			}
		}
		sendState=-1;
		state=0;
	}

	/**
	 * The transformation among different send state.
	 * State 0: DIFS
	 * State 1: Back-off
	 * State 2-0: Transmitting
	 * State 3: Waiting ACK/NACK
	 * State 4: Interrupting
	 * State 5: Complete
	 */
	protected void sendNextStep(){
		int signal=receivedSignal.value;
		int controlSignal=receivedSignal.control;

		if (signal==id){
			if (sendState==0 || sendState==1){
				super.sendInterrupt();
				return;
			}
		}

		//The transformation when carriersense
		if (signal!=-1){
			switch (sendState){
			case 0://DIFS
				count=Param.timeDIFS;
				break;
			case 1://Backoff
				sendState=0;
				count=Param.timeDIFS;
				break;
			case 2://Transmitting
			case 3:
			case 4:
				count--;
				break;
			default:
				break;
			}
		}else{
			switch (sendState){
			case 0:
				count--;
				break;
			case 1:
				backoffTime--;
				count--;
				break;
			case 2:
			case 3:
			case 4:
				count--;
				break;
			default:
				break;
			}
		}
		//The transformation when carriersense at controlchannel
		if (controlSignal!=-1){
			switch (sendControlState){
			case 0:
				if (this.receiveCount>=this.numSubCurrentPacket){
					sendState=6;
					count=0;
					debugOutput.output(this.time+": To "+this.partner+" finished with signal " +"C"+this.sendControlState+" "+this.sendState);
				}else{
					if (controlSignal==this.id+1000){
						this.sendControlState=1;
						debugOutput.output(this.time+": To "+this.partner+" get ACK start " +"C"+this.sendControlState+" "+this.sendState);
					}else{
						debugOutput.output(this.time+": To "+this.partner+" get ACK failed " +"C"+this.sendControlState+" "+this.sendState);
						this.sendState=5;
						count=0;
					}
				}
				break;
			case 1:
				if (controlSignal!=this.id+1000){
					this.receiveCount+=1;
					this.sendControlState=0;
					debugOutput.output(this.time+": To "+this.partner+" get ACK end with other signal " +"C"+this.sendControlState+" "+this.sendState);
				}
				break;
			}
		}else{
			switch (sendControlState){
			case 0:
				if (this.receiveCount>=this.numSubCurrentPacket){
					sendState=6;
					count=0;
					debugOutput.output(this.time+": To "+this.partner+" finished with quiet " +"C"+this.sendControlState+" "+this.sendState);
				}
				break;
			case 1:
				this.receiveCount+=1;
				this.sendControlState=0;
				debugOutput.output(this.time+": To "+this.partner+" get ACK end with quiet " +"C"+this.sendControlState+" "+this.sendState);
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
					count=this.timeSubPacket-Param.timeCRC;
					this.channel.ch[id]=partner;
					break;
				case 2://subpacket complete
					debugOutput.output(this.time+": To "+this.partner+" send subpacket at " + this.sendCount +" C"+this.sendControlState+" "+this.sendState);
					this.channel.ch[id]=partner+500;
					sendState=3;
					count=Param.timeCRC;//EIFS
					this.sendCount+=1;
					break;
				case 3://next subpacket or waiting enough ack
					if (this.sendCount>=this.numSubCurrentPacket){
						sendState=4;
						count=Param.timeEIFS;
						this.channel.ch[id]=-1;
					}else{
						count=this.timeSubPacket-Param.timeCRC;
						this.channel.ch[id]=partner;
						sendState=2;
					}
					break;
				case 4://Waiting ACK timeout
					debugOutput.output(this.time+": To "+this.partner+" not enough ACK at " + this.receiveCount);
					sendComplete(false);
					break;
				case 5://receive ACK
					this.channel.ch[id]=-1;
					debugOutput.output(this.time+": To "+this.partner+" interruptted at " + this.receiveCount);
					sendComplete(false);
					break;
				case 6://All ACK received
					this.channel.ch[id]=-1;
					debugOutput.output(this.time+": To "+this.partner+" all ACK received");
					sendComplete(true);
					break;
				}
			}
		}
	}
	//--------------------------------SEND------------------------------------------

}
