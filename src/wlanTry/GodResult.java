package wlanTry;

public class GodResult {
	public double DelayTime;
	public int countDelay;
	private int numDevice;
	double packetTx,packetRx,packetTxFails,packetRxFails; //For calculation of throughput
	public GodResult(){
		this.DelayTime=0;
		this.packetRx=0;
		this.packetRxFails=0;
		this.packetTx=0;
		this.packetTxFails=0;
	}
	public void add(DeviceResult b){
		this.numDevice++;
		this.packetRx+=b.getPacketRx();
		this.packetRxFails+=b.getPacketRxFails();
		this.packetTx+=b.getPacketTx();
		this.packetTxFails+=b.getPacketTxFails();
		this.DelayTime+=b.getDelayTime();
		this.countDelay++;
	}
	public void add(GodResult b){
		this.numDevice+=b.getNum();
		this.packetRx+=b.packetRx;
		this.packetRxFails+=b.packetRxFails;
		this.packetTx+=b.packetTx;
		this.packetTxFails+=b.packetTxFails;
		this.countDelay+=b.countDelay;
		this.DelayTime+=b.DelayTime;
	}
	public double getPacketRx(){
		return (double)this.packetRx/this.numDevice;
	}
	public double getPacketTx(){
		return (double)this.packetTx/this.numDevice;
	}
	public double getPacketRxFails(){
		return (double)this.packetRxFails/this.numDevice;
	}
	public double getPacketTxFails(){
		return (double)this.packetTxFails/this.numDevice;
	}
	public double getDelayTime(){
		return (double)this.DelayTime/this.countDelay;
	}
	public int getNum(){
		return numDevice;
	}
	public double getThroughputTx(){
		if (Param.deviceType!=DeviceType.CSMA){
			return (double)this.packetTx/Param.numSubpacket*Param.sizeData/Param.simTimeLength;
		}else{
			return (double)this.packetTx*Param.sizeData/Param.simTimeLength;
		}
	}
	public double getThroughputRx(){
		if (Param.deviceType!=DeviceType.CSMA){
			return (double)this.packetRx/Param.numSubpacket*Param.sizeData/Param.simTimeLength;
		}else{
			return (double)this.packetRx*Param.sizeData/Param.simTimeLength;
		}
	}
}
