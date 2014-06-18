package wlanTry;

public class RequestElement{
	public double time;
	public int id;
	public int numSub;
	public RequestElement(double time, int id){
		this.time=time;
		this.id=id;
		this.numSub=1;
	}
	public RequestElement(double time, int id,int numSub){
		this.time=time;
		this.id=id;
		this.numSub=numSub;
	}
}

