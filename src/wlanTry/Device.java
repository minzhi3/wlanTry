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
	protected Channel controlChannel;
	final int id;
	final int timeLength=Param.simTimeLength;
	final int AP;  //The ID of AP, -1 means this is AP
	final CyclicBarrier barrier;
	final Object key;  //key for synchronize
	final ArrayList<Integer> neighbor;  //neighbor
	final DeviceMap dMap;
	RequestsQueue requests;  //The time of sending data
	//DebugOutput debugChannel;
	
	ArrayList<Signal> dataSignals;
	ArrayList<Signal> controlSignals;
	
	//Signal receivedSignal;
	DeviceResult ret;
	DebugOutput debugOutput = null;


	public Device(int id,int AP,CyclicBarrier barrier,Object key,Channel ch, Channel controlChannel, DeviceMap dm) {
		this.id=id;
		this.AP=AP;
		this.barrier=barrier;
		this.key=key;
		this.dataChannel=ch;
		this.controlChannel=controlChannel;
		this.dMap=dm;
		this.neighbor=dMap.getNeighbour(id);
		this.requests=dMap.getRequests(id);
    } 
	
	@Override
	public DeviceResult call() throws Exception {
		ret=new DeviceResult();
		debugOutput=new DebugOutput(Param.outputPath+"D"+this.id+".txt");//Debug file
		
		try {
			while (this.dataChannel.getTime()<timeLength){
				debugOutput.output(dataChannel.getTime()+": ");
				synchronized(key){
					dataSignals=this.dataChannel.checkSignalOver(this.id);
					controlSignals=this.controlChannel.checkSignalOver(id);
				}
				
				this.receiveProcess();
				
				if (this.checkReply()){
					this.replyProcess();
				}
				if (this.checkTransmit()){
					this.transmitProcess();
				}
				barrier.await();

				debugOutput.output("\n");
			}
		} catch (Exception e) {
			debugOutput.close();
			e.printStackTrace();
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