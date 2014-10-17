package wlanTry;

public class DeviceResult {
	int packetTx,packetRx,packetTxFails,packetRxFails,access; //For calculation of throughput
	int timeBegin;
	
	int sumDelay;  //For calculation of delay time
	int countDelay;
	
	final int timeLength;
	public DeviceResult(){
		this.access=0;
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
		if (Param.deviceType!=DeviceType.CSMA){
			return (double)this.packetTx/Param.numSubpacket*Param.sizeData/((double)timeLength);
		}else{
			return (double)this.packetTx*Param.sizeData/((double)timeLength);
		}
	}

	public double getThroughputRx(){
		if (Param.deviceType!=DeviceType.CSMA){
			return (double)this.packetRx/Param.numSubpacket*Param.sizeData/((double)timeLength);
		}else{
			return (double)this.packetRx*Param.sizeData/((double)timeLength);
		}
	}
	public double getPacketRxFails(){
		return this.packetRxFails;
	}
	public double getPacketTxFails(){
		return this.packetTxFails;
	}
	public double getPacketRx(){
		return this.packetRx;
	}
	public double getPacketTx(){
		return this.packetTx;
	}
	
	public double getDelayTime(){
		return (double)this.sumDelay/(double)countDelay;
		//return countDelay;
	}
	public void accessChannel(){
	}
	public void receiveACK(){
		packetTx++;
	}
	public void receiveACK(int num){
		packetTx+=num;
	}
	public void receiveNACK(){
		packetTxFails++;
	}
	public void receiveDATA(){
		packetRx++;
	}
	
	public void receiveDATA(int num){
		packetRx+=num;
	}
	
	public void retransmit(){
	}
	public void reply(PacketType type){
	}
	public void transmittingEnds(int timeBegin, int timeEnd) {
		int delayTime=timeEnd-timeBegin;
		countDelay++;
		sumDelay+=delayTime;
	}
	public void receiveError() {
		packetRxFails++;
	}
}
