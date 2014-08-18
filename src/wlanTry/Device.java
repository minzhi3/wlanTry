package wlanTry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;
import signal.SignalData;

class Pair{
	public int id;
	public int value;
};
/**
 * The base class of device.
 *
 */
abstract class Device implements Callable<DeviceResult> {
	protected Channel channel;  //shared by all devices
	final int id;
	final int timeLength=Param.simTimeLength;
	final int AP;  //The ID of AP, -1 means this is AP
	final CyclicBarrier barrier;
	final Object key;  //key for synchronize
	final ArrayList<Integer> senseRange;  //neighbor
	TransmissionRequest request;  //The time of sending data
	
	Signal receivedSignal;
	DeviceResult ret;
	DebugOutput debugOutput;
	int time;  //current simulation time


	public Device(int id,int AP,CyclicBarrier barrier,Object key,Channel ch,ArrayList<Integer> senseRange, TransmissionRequest request) {
		this.id=id;
		this.AP=AP;
		this.barrier=barrier;
		this.key=key;
		this.channel=ch;
		this.senseRange=senseRange;
		this.request=request;
    } 
	@Override
	public DeviceResult call() throws Exception {
		ret=new DeviceResult();
		debugOutput=new DebugOutput(Param.outputPath+"D"+this.id+".txt");//Debug file
		
		for (time=0;time<timeLength;time++){
			receivedSignal=this.checkReceive();
			this.receiveProcess();
			
			if (this.checkReply()){
				this.replyProcess();
			}
			if (this.checkTransmit()){
				this.transmitProcess();
			}

			try {
				barrier.await();
			} catch (BrokenBarrierException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		debugOutput.close();
		return this.ret;
	}
	
	protected abstract Signal checkReceive();
	protected abstract void receiveProcess();
	protected abstract boolean checkReply();
	protected abstract boolean checkTransmit();
	protected abstract void transmitProcess();
	protected abstract void replyProcess();
}