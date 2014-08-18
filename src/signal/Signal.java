package signal;

public class Signal{
	public int idFrom;
	public String contentString;
	public int timeBegin;
	public int timeLength;
	public int IDPacket;
	public boolean error;
	public int type;
	public Signal(int id,int IDPacket,String s,int b,int l){
		this.idFrom=id;
		this.IDPacket=IDPacket;
		this.contentString=s;
		this.timeLength=l;
		this.timeBegin=b;
		this.error=false;
	}
	public int getEndTime(){
		return this.timeLength+this.timeBegin;
	}
	public Signal next(){
		return null;
	}
	public void errorHappen(){
		this.error=true;
	}
	public boolean getErrorState(){
		return error;
	}
	public String getString(){
		return this.idFrom+" "+this.IDPacket+" "+this.contentString+" "+this.timeBegin+" "+this.timeLength+(this.error?"x":"o");
	}
}
