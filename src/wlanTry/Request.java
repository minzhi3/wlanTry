package wlanTry;

import signal.Signal;

public class Request implements Cloneable{
	public double time;
	public int IDTo;
	public int numSub;
	public PacketType type;
	public int IDPacket;
	public int length;
	/**
	 * Construct a Request
	 * @param IDTo
	 * @param time
	 * @param IDPacket
	 * @param type
	 * @param numSub
	 * @param length
	 */
	public Request(int IDTo, double time,int IDPacket, PacketType type,int numSub,int length){
		this.time=time;
		this.IDTo=IDTo;
		this.numSub=numSub;
		this.IDPacket=IDPacket;
		this.type=type;
		this.length=length;
	}
	public Request getClone(){
		try {
			return (Request)(this.clone());
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public Signal toSignal(int IDFrom, int begin){
		return new Signal(IDFrom, IDTo, IDPacket, type, numSub, begin, length);
	}
	public int getLength(){
		return this.numSub*this.length;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException{
		return super.clone();
		
	}
}

