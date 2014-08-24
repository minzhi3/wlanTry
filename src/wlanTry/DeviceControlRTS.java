package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

public class DeviceControlRTS extends Device {
	RequestsQueue replyRequests;
	//Signal receivedControlSignal;
	final int numMT;
	final int IDSlot;
	int stateTransmit;
	int stateReply;
	int countIFS;
	int countBackoff;
	int countTransmit;
	int countReply;
	int sizeCW;
	boolean carrierSense;
	
	public DeviceControlRTS(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, Channel controlChannel, DeviceMap dm) {
		super(id, AP, barrier, key, ch, controlChannel, dm);
		this.replyRequests=new RequestsQueue();
		this.numMT=dMap.numMT;
		this.IDSlot=id%(numMT);
	}

	@Override
	protected void receiveProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean checkReply() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean checkTransmit() {
		// TODO Auto-generated method stub
		return false;
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
