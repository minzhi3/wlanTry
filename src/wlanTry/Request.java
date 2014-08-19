package wlanTry;

public class Request{
	public double time;
	public int toID;
	public int numSub;
	public PacketType content;
	public int packetID;

	public Request(int toID, double time,int packetID, int numSub, PacketType content){
		this.time=time;
		this.toID=toID;
		this.numSub=numSub;
		this.packetID=packetID;
		this.content=content;
	}
}

