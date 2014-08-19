package wlanTry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;

public class DeviceControlNACK extends Device {
	LinkedList<Request> replyRequests;
	Channel controlChannel;
	Signal receivedControlSignal;
	public DeviceControlNACK(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, Channel controlChannel, ArrayList<Integer> senseRange,
			LinkedList<Request> requests) {
		super(id, AP, barrier, key, ch, senseRange, requests);
		this.controlChannel=controlChannel;
	}


	@Override
	protected void receiveProcess() {
		receivedSignal=dataChannel.getSignal(id);
		receivedControlSignal=controlChannel.getSignal(id);
		
		//Receive for data channel
		if (receivedSignal!=null){
			switch (receivedSignal.type){
				case DATA:  //Reply the ACK signal
					ret.packetRx+=1;
					replyRequests.add(new Request(
							receivedSignal.idFrom, 
							dataChannel.currentTime, 
							receivedSignal.IDPacket, 
							1, 
							PacketType.ACK));
					break;
			default:
				break;
			}
		}
		
		//receive for control channel
		if (receivedControlSignal!=null){
			switch (receivedSignal.type){
			case ACK:
				ret.packetTx+=1;
				break;
			case NACK:
				dataChannel.retrieveSignal(
						neighbor, 
						receivedSignal.idFrom,
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
		if (replyRequests.peekFirst()!=null){
			return replyRequests.peekFirst().time<dataChannel.currentTime;
		}
		else {
			return false;
		}
	}

	@Override
	protected boolean checkTransmit() {
		if (requests.peekFirst()!=null){
			return requests.peekFirst().time<dataChannel.currentTime;
		}
		else {
			return false;
		}
	}

	@Override
	protected void transmitProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void replyProcess() {
		// TODO Auto-generated method stub

	}

}
