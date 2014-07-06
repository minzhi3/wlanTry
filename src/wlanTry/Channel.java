package wlanTry;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;


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

	public void addSignal(List<Integer> neighbor,int id,String content,int length){
		for (int i : neighbor) {
			this.addSignal(i, id, content, length);
		}
	}
	public void addSignal(int i,int id,String content,int length){
		Signal s=new Signal(id, content, this.currentTime, length);
		this.chArray.get(i).add(s);
	}
	public String[] getString(int num){
		String[] ret=new String[chArray.get(num).size()];
		int i=0; int j=0;
		for (Signal s:chArray.get(i)){
			ret[j++]=(s.idFrom+" "+s.contentString+" "+s.timeBegin+" "+s.timeLength);
		}
		return ret;
	}
	public void checkSignalOver(int num){
		int i=0; int j=0;
		ListIterator<Signal> li=chArray.get(num).listIterator();
		while (li.hasNext()){
			Signal s=li.next();
			if (s.getEndTime()<=this.currentTime) li.remove();
		}
	}
	public void setTime(int i){
		this.currentTime+=i;
	}
	public LinkedList<Signal> getList(int num){
		return this.chArray.get(num);
	}
}
