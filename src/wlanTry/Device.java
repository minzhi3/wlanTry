package wlanTry;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

import signal.Signal;

/**
 * The base class of device.
 *
 */
abstract class Device implements Callable<DeviceResult> {
	protected Channel dataChannel;  //shared by all devices
	final int id;
	final int timeLength=Param.simTimeLength;
	final int AP;  //The ID of AP, -1 means this is AP
	final CyclicBarrier barrier;
	final Object key;  //key for synchronize
	final ArrayList<Integer> neighbor;  //neighbor
	RequestsQueue requests;  //The time of sending data
	
	Signal receivedSignal;
	DeviceResult ret;
	DebugOutput debugOutput;


	public Device(int id,int AP,CyclicBarrier barrier,Object key,Channel ch,ArrayList<Integer> neighbor, RequestsQueue requests) {
		this.id=id;
		this.AP=AP;
		this.barrier=barrier;
		this.key=key;
		this.dataChannel=ch;
		this.neighbor=neighbor;
		this.requests=requests;
    } 
	@Override
	public DeviceResult call() throws Exception {
		ret=new DeviceResult();
		debugOutput=new DebugOutput(Param.outputPath+"D"+this.id+".txt");//Debug file
		
		while (this.dataChannel.getTime()<timeLength){
			this.dataChannel.checkSignalOver(this.id);
			
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
	
	protected abstract void receiveProcess();
	protected abstract boolean checkReply();
	protected abstract boolean checkTransmit();
	protected abstract void transmitProcess();
	protected abstract void replyProcess();
}