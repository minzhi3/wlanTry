package wlanTry;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;


public class DeviceCSMA extends Device {

	int stateTransmit;
	int stateReply;
	int countIFS;
	int countBackoff;
	int countTransmit;
	int countReply;
	int sizeCW;
	RequestsQueue replyRequests;

	public DeviceCSMA(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, DeviceMap dm) {
		super(id, AP, barrier, key, ch, null, dm);
		this.replyRequests=new RequestsQueue();
		sizeCW=Param.sizeCWmin;
	}

	@Override
	protected void receiveProcess() {
		
		if (!dataSignals.isEmpty()){
			Signal receivedSignal=dataSignals.get(0);
			debugOutput.output(receivedSignal.getString()+" Received");
			
			if (receivedSignal.IDTo==id){
				debugOutput.output(" --ID OK");
				switch (receivedSignal.type){
					case DATA:  //Reply the ACK signal
						if (!receivedSignal.error){
							debugOutput.output(" --Available DATA --Reply ACK");
							ret.receiveDATA();
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
									dataChannel.currentTime, 
									receivedSignal.IDPacket, 
									PacketType.ACK,
									1,
									Param.timeACK));
						}else{
							debugOutput.output(" --Error DATA");
							ret.receiveError();
						}
						break;
					case ACK:  //Received ACK
						if (!receivedSignal.error){
							debugOutput.output(" --Available ACK");
							ret.receiveACK();
							this.sizeCW=Param.sizeCWmin;
							this.stateTransmit=0;
							ret.transmittingEnds((int)(this.requests.getTranmitTime()),dataChannel.getTime());
							this.requests.popSubpacket();
						}else{
							debugOutput.output(" --Error ACK");
							//this.stateTransmit=0;
							//this.requests.popSubpacket();
						}
						break;
				default:
					break;
				}
			}
		}
		
	}

	@Override
	protected boolean checkReply() {
		if (stateReply>0)
			return true;
		else {
			return replyRequests.getTranmitTime()<dataChannel.currentTime;
		}
	}

	@Override
	protected boolean checkTransmit() {
		if (stateTransmit>0)
			return true;
		else
			return super.requests.getTranmitTime()<dataChannel.currentTime;
	}

	@Override
	protected void transmitProcess() {
		
		switch (stateTransmit){
		case 0://Initial
			debugOutput.output("Transmitting Starts");
			stateTransmit=1;
			countIFS=Param.timeDIFS;
			break;
			
		case 1://DIFS
			debugOutput.output(" --DIFS "+countIFS);
			if (countIFS<=0){
				stateTransmit=2;
				if (countBackoff<=0)
					//countBackoff=70;
					countBackoff=(new Random().nextInt(sizeCW)+1)*Param.timeSlot;
					
			}
			if (!carrierSense){
				countIFS--;
			}else {
				countIFS=Param.timeDIFS;
			}
			break;
			
		case 2://Backoff
			debugOutput.output(" --Backoff "+countBackoff);
			if (countBackoff<=0){
				stateTransmit=3;
				countTransmit=requests.getFirst().length;
				synchronized(key){
					dataChannel.addSignal(neighbor, id, requests.getFirst());
				}
				//Access successfully
				ret.accessChannel();
			}
			if (!carrierSense){
				countBackoff--;
			}else{
				stateTransmit=1;
			}
			break;
		case 3://Transmitting Data
			debugOutput.output(" --Transmitting "+countTransmit);
			if (countTransmit<=0){
				stateTransmit=4;
				countIFS=Param.timeEIFS;
			}else{
				countTransmit--;
			}
			break;
		case 4://EIFS
			debugOutput.output(" --Waiting ACK/NACK "+countIFS);
			if (countIFS<=0){
				stateTransmit=0;
				ret.retransmit();
				stateTransmit=0;
				if (this.sizeCW<Param.sizeCWmax)
					this.sizeCW*=2;
				debugOutput.output(" --No ACK, extends CW --"+this.sizeCW);
			}
			if (!carrierSense){
				countIFS--;
			}else{
				stateTransmit=3;
			}
		}
		
	}

	@Override
	protected void replyProcess() {
		switch (stateReply){
		case 0://Initial
			debugOutput.output("Replying Starts ");
			stateReply=1;
			countIFS=Param.timeSIFS;
			break;
		case 1://SIFS
			debugOutput.output(" --SIFS "+countIFS);
			if (countIFS<=0){
				stateReply=2;
				countReply=replyRequests.getFirst().length;
				synchronized(key){
					dataChannel.addSignal(neighbor, id, replyRequests.getFirst());
				}
			}else{
				if (!carrierSense){
					countIFS--;
				}else {
					countIFS=Param.timeDIFS;
				}
			}
			break;
		case 2://Replying ACK
			debugOutput.output(" --Replying "+countReply);
			if (countReply<=0){
				stateReply=0;
				debugOutput.output(" --Reply End");
				replyRequests.popSubpacket();
			}else{
				countReply--;
			}
			break;
		default:
			break;
		}	
	}

}
