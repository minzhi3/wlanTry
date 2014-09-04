package wlanTry;

public enum PacketType {
	DATA("DATA",0),
	ACK("ACK",1),
	NACK("NACK",2),
	RTS("RTS",3),
	CTS("CTS",4),
	ENDS("ENDS",5),
	ENDR("ENDR",6);
	private String name;
	private int index;
	private PacketType(String name,int index){
		this.name=name;
		this.index=index;
	}
	public String getName(){
		return name;
	}
	public int getIndex(){
		return index;
	}
}
