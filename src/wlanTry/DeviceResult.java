package wlanTry;

public class DeviceResult {
	int packetTx,packetRx,packetTxFails,packetRxFails; //For calculation of throughput
	double sumDelay;  //For calculation of delay time
	final int timeLength;
	public DeviceResult(){
		this.packetRx=0;
		this.packetTx=0;
		this.packetTxFails=0;
		this.packetRxFails=0;
		this.sumDelay=0;
		this.timeLength=Param.simTimeLength;
	}
	public double getThroughputTx(){
		/*System.out.println(
		costTime+
		": MT "+this.id+
		" Tx "+(double)this.packetTx*12000.0/((double)time/1000000.0)/1000000.0+
		", Rx "+(double)this.packetRx*12000.0/((double)time/1000000.0)/1000000.0
		);*/
		return (double)this.packetTx*Param.sizeData/((double)timeLength);
	}

	public double getThroughputRx(){
		return (double)this.packetRx*Param.sizeData/((double)timeLength);
	}
	public int getPacketRxFails(){
		return this.packetRxFails;
	}
	public int getPacketTxFails(){
		return this.packetTxFails;
	}
	public int getPacketRx(){
		return this.packetRx;
	}
	public int getPacketTx(){
		return this.packetTx;
	}
	
	public double getDelayTime(){
		if (packetTx==0)
			return -1;
		else
			return (double)this.sumDelay/(double)packetTx;
	}
}
