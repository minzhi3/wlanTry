package signal;

import wlanTry.PacketType;

public class Signal implements Cloneable{
	public int IDFrom;
	public int IDTo;
	public PacketType type;
	public int timeBegin;
	public int timeLength;
	public int IDPacket;
	public int numSubpacket;
	boolean error;
	public Signal(int IDFrom, int IDTo,int IDPacket,PacketType type,int subpacket, int begin,int length){
		this.IDFrom=IDFrom;
		this.IDTo=IDTo;
		this.IDPacket=IDPacket;
		this.type=type;
		this.timeLength=length;
		this.timeBegin=begin;
		this.numSubpacket=subpacket;
		this.error=false;
	}
	public Signal getClone(){
		try {
			return (Signal)(this.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public int getEndTime(){
		return this.timeLength+this.timeBegin;
	}
	public void removeSubpacket(){
		numSubpacket--;
		timeBegin+=timeLength;
		this.error=false;
	}
	public void errorHappen(){
		this.error=true;
	}
	public boolean getErrorState(){
		return error;
	}
	public int getSub(){
		return this.numSubpacket;
	}
	public String getString(){
		return this.IDFrom+"->"+
				this.IDTo+", ID:"+
				this.IDPacket+"-"+
				this.numSubpacket+" "+
				this.type.getName()+" "+
				this.timeBegin+" "+
				this.timeLength+" "+
				(this.type==PacketType.DATA?(this.error?"-ERROR":"-OK"):"");
	}
	@Override
	protected Object clone() throws CloneNotSupportedException{
		return super.clone();
		
	}
	
}
