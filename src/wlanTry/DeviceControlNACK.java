package wlanTry;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;

public class DeviceControlNACK extends Device {
	RequestsQueue replyRequests;
	Channel controlChannel;
	Signal receivedControlSignal;
	
	final int IDSlot;
	int stateTransmit;
	int stateReply;
	int countIFS;
	int countBackoff;
	int countTransmit;
	int sizeCW;
	boolean carrierSense;
	
	public DeviceControlNACK(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, Channel controlChannel, ArrayList<Integer> senseRange,
			RequestsQueue requests) {
		super(id, AP, barrier, key, ch, senseRange, requests);
		this.controlChannel=controlChannel;
		this.replyRequests=new RequestsQueue();
		this.IDSlot=id%(Param.numMT+1);
	}


	@Override
	protected void receiveProcess() {
		receivedSignal=dataChannel.getSignal(id);
		receivedControlSignal=controlChannel.getSignal(id);
		
		
		carrierSense=(receivedSignal!=null);
		if (carrierSense) debugOutput.output(receivedSignal.getString()+" Received");
		//Receive for data channel
		if (receivedSignal!=null && receivedSignal.IDTo==id){
			debugOutput.output(" --ID OK");
			switch (receivedSignal.type){
				case DATA:  //Reply the ACK signal
					if (receivedSignal.error==false){
						ret.receiveDATA();
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
		
		//receive for control channel
		if (receivedControlSignal!=null) debugOutput.output(receivedControlSignal.getString()+" Received");
		if (receivedControlSignal!=null && receivedControlSignal.IDTo==id){
			switch (receivedSignal.type){
			case ACK:
				debugOutput.output(" --Type ACK");
				if (requests.getSubpacket()<=1)
					stateTransmit=0;
				
				ret.receiveACK();
				requests.popSubpacket();
				break;
			case NACK:
				debugOutput.output(" --Type NACK");
				ret.receiveNACK();
				stateTransmit=0;
				dataChannel.retrieveSignal(
						neighbor, 
						receivedSignal.IDFrom,
						receivedSignal.IDPacket,
						receivedSignal.type
						);
				break;
				
			default:
				break;
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
			sizeCW=Param.sizeCWmin;
			break;
			
		case 1://DIFS
			debugOutput.output(" --DIFS "+countIFS);
			if (countIFS<=0){
				stateTransmit=2;
				if (countBackoff<=0)
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
				
				dataChannel.addSignal(neighbor, id, requests.getFirst());
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
			int currentSlot=(dataChannel.getTime()/Param.timeControlSlot)/(Param.numMT+1);
			if (currentSlot==this.IDSlot){
				debugOutput.output(" --Reply "+replyRequests.getFirst().type);
				controlChannel.addSignal(neighbor, id, replyRequests.getFirst());

				ret.reply(replyRequests.getFirst().type);
				replyRequests.popSubpacket();
				stateReply=0;
			}
			break;
		}

	}

}
