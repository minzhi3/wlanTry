package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;

public class DeviceControlNACK extends Device {
	RequestsQueue replyRequests;
	//Signal receivedControlSignal;
	
	final int IDSlot;
	int stateTransmit;
	int stateReply;
	int countIFS;
	int countBackoff;
	int countTransmit;
	int countReply;
	int sizeCW;
	boolean carrierSense;
	
	public DeviceControlNACK(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, Channel controlChannel, DeviceMap dm) {
		super(id, AP, barrier, key, ch, controlChannel, dm);
		this.replyRequests=new RequestsQueue();
		if (dm.numMT>0) 
			this.IDSlot=id%(dm.numMT);
		else {
			this.IDSlot=0;
		}
	}
	

	@Override
	protected void receiveProcess() {
		//receivedSignal=dataChannel.getSignal(id);
		//receivedControlSignal=controlChannel.getSignal(id);

		carrierSense=(dataChannel.getSignal(id)!=null);
		
		if (!dataSignals.isEmpty()){
			Signal receivedSignal=dataSignals.get(0);
			debugOutput.output(receivedSignal.getString()+" Received");
			//Receive for data channel
			if (receivedSignal.IDTo==id){
				debugOutput.output(" --ID OK");
				switch (receivedSignal.type){
					case DATA:  //Reply the ACK signal
						if (receivedSignal.error){
							ret.receiveError();
							debugOutput.output(" --Error Detected --Reply NACK");
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
									dataChannel.currentTime, 
									receivedSignal.IDPacket, 
									PacketType.NACK,
									1,
									Param.timeControlSlot-2));
						}else{
							debugOutput.output(" --Available DATA --Reply ACK");
							ret.receiveDATA();
							replyRequests.addRequest(new Request(
									receivedSignal.IDFrom, 
									dataChannel.currentTime, 
									receivedSignal.IDPacket, 
									PacketType.ACK,
									1,
									Param.timeControlSlot-2));
						}
						break;
				default:
					break;
				}
			}
		}
		
		//receive for control channel

		if (!controlSignals.isEmpty()){
			Signal receivedControlSignal=controlSignals.get(0);
			debugOutput.output(receivedControlSignal.getString()+" Received");
			switch (receivedControlSignal.type){
			case ACK:
				if (receivedControlSignal.IDTo==id){
					debugOutput.output(" --Type ACK");
					if (requests.getSubpacket()<=1)
						stateTransmit=0;
					
					ret.receiveACK();
					boolean wholePacket=requests.popSubpacket();
					if (wholePacket) ret.transmittingEnds(dataChannel.getTime()); 
				}
				break;
			case NACK://Any NACK except from itself can stop transmitting
				if (receivedControlSignal.IDFrom!=id){
				debugOutput.output(" --Type NACK");
				ret.receiveNACK();
				stateTransmit=0;
				synchronized(key){
					dataChannel.retrieveSignal(
							neighbor,
							receivedControlSignal.IDFrom,
							PacketType.DATA
							);
					}
				}
				break;
				
			default:
				break;
			}
			debugOutput.output("|");
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
			if (requests.getFirst().numSub==10) ret.transmittingStart(dataChannel.getTime());
			stateTransmit=1;
			countIFS=Param.timeDIFS;
			sizeCW=Param.sizeCWmin;
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
			break;
		case 1:
			debugOutput.output(" --Waiting Slots");
			int currentSlot=(dataChannel.getTime()/Param.timeControlSlot)%dMap.numMT;
			debugOutput.output(" --Now "+currentSlot+" Need "+this.IDSlot);
			if (currentSlot==this.IDSlot){
				debugOutput.output(" --Reply "+replyRequests.getFirst().type);
				
				synchronized(key){
					controlChannel.addSignal(neighbor, id, replyRequests.getFirst());
				}

				ret.reply(replyRequests.getFirst().type);
				replyRequests.popSubpacket();
				stateReply=2;
				countReply=Param.timeControlSlot;
			}
			break;
		case 2:
			if (countReply<=0){
				stateReply=0;
			}else {
				countReply--;
			}
		}

	}

}
