package wlanTry;

import java.util.ArrayList;

public class GodResult {
	double sumDelay;
	int countDelay;
	private int numDevice,numGod;
	ArrayList<Integer> arrayPacket;
	int packetTx,packetRx,packetTxFails,packetRxFails; //For calculation of throughput
	public GodResult(){
		this.sumDelay=0;
		this.packetRx=0;
		this.packetRxFails=0;
		this.packetTx=0;
		this.packetTxFails=0;
		this.countDelay=0;
		this.numGod=0;
		this.arrayPacket=new ArrayList<Integer>();
	}
	public void add(DeviceResult b){
		this.numDevice++;
		this.packetRx+=b.packetRx;
		this.packetRxFails+=b.packetRxFails;
		this.packetTx+=b.packetTx;
		this.packetTxFails+=b.packetTxFails;
		this.sumDelay+=b.sumDelay;
		this.countDelay+=b.countDelay;
		
		this.arrayPacket.add(b.packetRx+b.packetTx);
	}
	public void add(GodResult b){
		this.numDevice+=b.numDevice;
		this.numGod+=1;
		this.packetRx+=b.packetRx;
		this.packetRxFails+=b.packetRxFails;
		this.packetTx+=b.packetTx;
		this.packetTxFails+=b.packetTxFails;
		this.countDelay+=b.countDelay;
		this.sumDelay+=b.sumDelay;
		
		this.arrayPacket.addAll(b.arrayPacket);
	}
	public double getPacketRx(){
		return (double)this.packetRx/this.numGod;
	}
	public double getPacketTx(){
		return (double)this.packetTx/this.numGod;
	}
	public double getPacketRxFails(){
		return (double)this.packetRxFails/this.numGod;
	}
	public double getPacketTxFails(){
		return (double)this.packetTxFails/this.numGod;
	}
	public double getDelayTime(){
		return (double)this.sumDelay/this.countDelay;
	}
	public int getNum(){
		return numDevice;
	}
	public double getThroughputTx(){
		double throughputTx=(double)this.packetTx*Param.sizeData/Param.simTimeLength/numGod;
		if (Param.deviceType!=DeviceType.CSMA){
			return (double)throughputTx/Param.numSubpacket;
		}else{
			return (double)throughputTx;
		}
	}
	public double getThroughputRx(){
		double throughputRx=(double)this.packetRx*Param.sizeData/Param.simTimeLength/numGod;
		if (Param.deviceType!=DeviceType.CSMA){
			return (double)throughputRx/Param.numSubpacket;
		}else{
			return (double)throughputRx;
		}
	}
	public double getTxPerDevice(){
		return getThroughputTx()*numGod/this.numDevice;
	}
	public double getRxPerDevice(){
		return getThroughputRx()*numGod/this.numDevice;
	}
}
