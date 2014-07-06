package wlanTry;

public class Signal{
	int idFrom;
	String contentString;
	int timeBegin;
	int timeLength;
	public Signal(int id,String s,int b,int l){
		this.idFrom=id;
		this.contentString=s;
		this.timeLength=l;
		this.timeBegin=b;
	}
	public int getEndTime(){
		return this.timeLength+this.timeBegin;
	}
}
