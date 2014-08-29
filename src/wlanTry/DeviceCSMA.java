package wlanTry;

import java.util.concurrent.CyclicBarrier;


public class DeviceCSMA extends Device {

	public DeviceCSMA(int id, int AP, CyclicBarrier barrier, Object key,
			Channel ch, DeviceMap dm) {
		super(id, AP, barrier, key, ch, null, dm);
		// TODO Auto-generated constructor stub
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
