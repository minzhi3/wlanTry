package wlanTry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;

public class DeviceControlNACK extends Device {
	LinkedList<Request> replyRequests;

	public DeviceControlNACK(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, ArrayList<Integer> senseRange,
			LinkedList<Request> requests) {
		super(id, AP, barrier, key, ch, senseRange, requests);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Signal checkReceive() {
		return super.channel.getSignal(id);
	}

	@Override
	protected void receiveProcess() {
		if (receivedSignal==null) return;  //No signal is received.
		
		switch (receivedSignal.type){
		case DATA:  //Reply the ACK signal
			ret.packetRx+=1;
			replyRequests.add(new Request(
					receivedSignal.idFrom, 
					super.channel.currentTime, 
					receivedSignal.IDPacket, 
					1, 
					PacketType.ACK));
			break;
		case ACK:
			ret.packetTx+=1;
			break;
		case NACK:
			channel.retrieveSignal(
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

	@Override
	protected boolean checkReply() {
		if (replyRequests.peekFirst()!=null){
			return replyRequests.peekFirst().time<channel.currentTime;
		}
		else {
			return false;
		}
	}

	@Override
	protected boolean checkTransmit() {
		if (requests.peekFirst()!=null){
			return requests.peekFirst().time<channel.currentTime;
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
