package wlanTry;

public class GodResult {
	public double ThroughputTx;
	public double ThroughputRx;
	public double DelayTime;
	public GodResult(){
		this.ThroughputRx=0;
		this.ThroughputTx=0;
		this.DelayTime=0;
	}
	public void add(GodResult b){
		this.ThroughputRx+=b.ThroughputRx;
		this.ThroughputTx+=b.ThroughputTx;
		this.DelayTime+=b.DelayTime;
	}
	public void div(double d){
		this.ThroughputRx/=d;
		this.ThroughputTx/=d;
		this.DelayTime/=d;
	}
}
