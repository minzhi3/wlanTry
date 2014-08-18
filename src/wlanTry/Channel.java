package wlanTry;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import signal.Signal;
import signal.SignalData;

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

	public void addSignal(List<Integer> neighbor,int IDFrom,int IDPacket,String content,int length){
		for (int IDTo : neighbor) {
			this.addSignal(IDTo, IDFrom, IDPacket, content, length);
		}
	}
	public void addSignal(int IDTo,int IDFrom,int IDPacket, String content,int length){
		Signal s=new Signal(IDFrom,IDPacket,content, this.currentTime, length);
		this.chArray.get(IDTo).add(s);
		this.checkCollision(IDTo);
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
				if (s instanceof SignalData){
					s=s.next();
				}else
					li.remove();
			}
		}
	}
	public void setTime(int time){
		this.currentTime=time;
	}
	public LinkedList<Signal> getList(int num){
		return this.chArray.get(num);
	}
	public void checkCollision(int num){
		if (this.chArray.get(num).size()>1){
			ListIterator<Signal> li=chArray.get(num).listIterator();
			while (li.hasNext()){
				Signal s=li.next();
				s.errorHappen();
			}
		}
	}
}
