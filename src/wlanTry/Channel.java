package wlanTry;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import signal.Signal;

/**
 * The feature of retrieve need to be added
 * It happens when the NACK is send and transmitter need to retrieve signal from receivers.
 * @author Huang
 *
 */
public class Channel {
	int currentTime;
	Vector<LinkedList<Signal>> chArray;
	final int count;
	public Channel(int numNode){
		this.currentTime=0;
		this.chArray=new Vector<LinkedList<Signal>>();
		for (int i=0;i<numNode;i++){
			this.chArray.add(new LinkedList<Signal>());
		}
		this.count=numNode;
	}

	public void addSignal(List<Integer> neighbor,int IDFrom,int IDPacket,PacketType type,int length){
		for (int IDTo : neighbor) {
			this.addSignal(IDTo, IDFrom, IDPacket, type, length);
		}
	}
	private void addSignal(int IDTo,int IDFrom,int IDPacket, PacketType type,int length){
		Signal s=new Signal(IDFrom,IDPacket,type, this.currentTime, length);
		this.chArray.get(IDTo).add(s);
		this.checkCollision(IDTo);
	}
	
	public void retrieveSignal(List<Integer> neighbor,int IDFrom, int IDPacket, PacketType type){
		for (int IDTo : neighbor) {
			this.retrieveSignal(IDTo, IDFrom, IDPacket, type);
		}
	}
	private void retrieveSignal(int IDTo,int IDFrom,int IDPacket, PacketType type){
		ListIterator<Signal> li=chArray.get(IDTo).listIterator();
		while (li.hasNext()){
			Signal s=li.next();
			if (s.IDPacket==IDPacket && s.type==type){
				li.remove();
			}
		}
	}
		
	public String[] getString(int num){
		String[] ret=new String[chArray.get(num).size()];
		int i=0; int j=0;
		for (Signal s:chArray.get(i)){
			ret[j++]=s.getString();
		}
		return ret;
	}
	public void checkSignalOver(int num){
		ListIterator<Signal> li=chArray.get(num).listIterator();
		while (li.hasNext()){
			Signal s=li.next();
			if (s.getEndTime()<=this.currentTime){
				li.remove();
			}
		}
	}
	public void setTime(int time){
		this.currentTime=time;
	}
	public int getTime(){
		return currentTime;
	}
	public Signal getSignal(int num){
		return this.chArray.get(num).peekFirst();
	}
	private void checkCollision(int num){
		if (this.chArray.get(num).size()>1){
			ListIterator<Signal> li=chArray.get(num).listIterator();
			while (li.hasNext()){
				Signal s=li.next();
				s.errorHappen();
			}
		}
	}
}
