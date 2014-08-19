package signal;

import wlanTry.PacketType;

public class Signal{
	public int idFrom;
	public PacketType type;
	public int timeBegin;
	public int timeLength;
	public int IDPacket;
	public boolean error;
	public Signal(int id,int IDPacket,PacketType type,int b,int l){
		this.idFrom=id;
		this.IDPacket=IDPacket;
		this.type=type;
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
		return this.idFrom+" "+this.IDPacket+" "+this.type.getName()+" "+this.timeBegin+" "+this.timeLength+(this.error?"x":"o");
	}
}
