package wlanTry;

public class DeviceResult {
	int packetTx,packetRx,packetTxFails,packetRxFails; //For calculation of throughput
	double sumDelay;  //For calculation of delay time
	final int timeLength;
	public DeviceResult(int timeLength){
		this.packetRx=0;
		this.packetTx=0;
		this.packetTxFails=0;
		this.packetRxFails=0;
		this.sumDelay=0;
		this.timeLength=timeLength;
	}
	public double getThroughputTx(){
		/*System.out.println(
		costTime+
		": MT "+this.id+
		" Tx "+(double)this.packetTx*12000.0/((double)time/1000000.0)/1000000.0+
		", Rx "+(double)this.packetRx*12000.0/((double)time/1000000.0)/1000000.0
		);*/
		return (double)this.packetTx*24000.0/((double)timeLength/1000000.0)/1000000.0;
	}

	public double getThroughputRx(){
		return (double)this.packetRx*24000.0/((double)timeLength/1000000.0)/1000000.0;
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
