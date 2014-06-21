package wlanTry;

import java.util.ArrayList;

public class GodResult {
	public double ThroughputTx;
	public double ThroughputRx;
	public double DelayTime;
	double packetTx,packetRx,packetTxFails,packetRxFails; //For calculation of throughput
	public ArrayList<Double> sender;
	public GodResult(){
		this.ThroughputRx=0;
		this.ThroughputTx=0;
		this.DelayTime=0;
		this.packetRx=0;
		this.packetRxFails=0;
		this.packetTx=0;
		this.packetTxFails=0;
	}
	public void add(DeviceResult b){
		this.ThroughputRx+=b.getThroughputRx();
		this.ThroughputTx+=b.getThroughputTx();
		this.packetRx+=b.getPacketRx();
		this.packetRxFails+=b.getPacketRxFails();
		this.packetTx+=b.getPacketTx();
		this.packetTxFails+=b.getPacketTxFails();

	}
	public void add(GodResult b){
		this.ThroughputRx+=b.ThroughputRx;
		this.ThroughputTx+=b.ThroughputTx;
		this.DelayTime+=b.DelayTime;
		this.packetRx+=b.packetRx;
		this.packetRxFails+=b.packetRxFails;
		this.packetTx+=b.packetTx;
		this.packetTxFails+=b.packetTxFails;

	}
	public void div(double d){
		this.ThroughputRx/=d;
		this.ThroughputTx/=d;
		this.DelayTime/=d;
		this.packetRx/=d;
		this.packetRxFails/=d;
		this.packetTx/=d;
		this.packetTxFails/=d;
	}
}
