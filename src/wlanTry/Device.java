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
public abstract class Device implements Callable<DeviceResult> {
	protected Channel dataChannel;  //shared by all devices
	protected Channel controlChannel;
	final int id;
	final int timeLength=Param.simTimeLength;
	final int AP;  //The ID of AP, -1 means this is AP
	final CyclicBarrier barrier;
	final Object key;  //key for synchronize
	final ArrayList<Integer> neighbor;  //neighbor
	final DeviceMap dMap;

	protected int stateTransmit;
	protected int stateReply;

	RequestsQueue replyRequests;
	RequestsQueue requests;  //The time of sending data
	//DebugOutput debugChannel;
	
	ArrayList<Signal> dataSignals;
	ArrayList<Signal> controlSignals;
	
	//Signal receivedSignal;
	DeviceResult ret;
	DebugOutput debugOutput = null;
	boolean carrierSense;


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
		this.replyRequests=new RequestsQueue();
    } 
	
	@Override
	public DeviceResult call() throws Exception {
		ret=new DeviceResult();
		debugOutput=new DebugOutput(Param.outputPath+"D"+this.id+".txt");//Debug file
		
		try {
			while (this.dataChannel.getTime()<timeLength){
				debugOutput.outputInit(dataChannel.getTime());
				synchronized(key){
					dataSignals=this.dataChannel.checkSignalOver(this.id);
					if (controlChannel!=null) 
						controlSignals=this.controlChannel.checkSignalOver(id);
					carrierSense=(dataChannel.getSignal(id)!=null);
					
				}
				
				this.receiveProcess();
				
				if (this.checkReply()){
					this.replyProcess();
				}
				if (this.checkTransmit()){
					this.transmitProcess();
				}
				barrier.await();
				debugOutput.outputToFile();
			}
		} catch (Exception e) {
			debugOutput.close();
			e.printStackTrace();
		}

		debugOutput.close();
		return this.ret;
	}
	private boolean checkReply(){
		if (stateReply>0)
			return true;
		else {
			return replyRequests.getTranmitTime()<dataChannel.currentTime;
		}
	}
	private boolean checkTransmit() {
		if (stateTransmit>0)
			return true;
		else{
			//debugOutput.output("  ---REQ "+requests.getTranmitTime());
			return requests.getTranmitTime()<dataChannel.currentTime;
		}
	}
	
	protected abstract void receiveProcess() throws Exception;
	protected abstract void transmitProcess();
	protected abstract void replyProcess();
}