package wlanTry;

import java.util.ArrayList;
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
	public void addSignal(List<Integer> neighbor,int IDFrom, Request request){
		for (int destnation : neighbor) {
			this.addSignal(destnation, IDFrom, request);
		}
	}
/*
	public void addSignal(List<Integer> neighbor,int IDFrom, int IDTo,int IDPacket,PacketType type,int subPacket,int length){
		for (int destnation : neighbor) {
			this.addSignal(destnation, IDFrom, IDTo, IDPacket, type, subPacket, length);
		}
	}
	*/
	private void addSignal(int destnation,int IDFrom, Request request){
		Signal signal=request.toSignal(IDFrom, currentTime);
		this.chArray.get(destnation).add(signal);
		this.checkCollision(destnation);
	}
	/*
	private void addSignal(int destnation,int IDFrom, int IDTo,int IDPacket, PacketType type,int subPacket,int length){
		Signal s=new Signal(IDFrom, IDTo,IDPacket,type,subPacket, this.currentTime, length);
		this.chArray.get(destnation).add(s);
		this.checkCollision(destnation);
	}
	*/
	
	public void retrieveSignal(List<Integer> neighbor, int IDTo, PacketType type){
		for (int destation : neighbor) {
			this.retrieveSignal(destation, IDTo, type);
		}
	}
	private void retrieveSignal(int destation, int IDTo, PacketType type){
		//List<Signal> delList = new ArrayList<Signal>();
		ListIterator<Signal> li=chArray.get(destation).listIterator();
		Signal s;
		//try {
			while (li.hasNext()){
				s=li.next();
				if (s.IDTo==IDTo && type==s.type){
					li.remove();
				}
			}
		//} catch (Exception e) {
		//	System.out.println("DD");
		//}
		
		//chArray.get(destation).removeAll(delList);
	}
		
	public ArrayList<Signal> checkSignalOver(int num){
		ArrayList<Signal> ret=new ArrayList<Signal>();
		ListIterator<Signal> li=chArray.get(num).listIterator();
		//List<Signal> delList = new ArrayList<Signal>();
		while (li.hasNext()){
			Signal s=li.next();
			if (s.getEndTime()<=this.currentTime){
				ret.add(s.getClone());
				if (s.getSub()<=1)
					li.remove();
				else {
					s.removeSubpacket();
					if (this.chArray.get(num).size()>1)
						s.errorHappen();
				}
			}
		}
		//chArray.get(num).removeAll(delList);
		return ret;
	}
	public void setTime(int time){
		this.currentTime=time;
	}
	public int getTime(){
		return currentTime;
	}
	public void tic(){
		currentTime++;
	}
	
	/**
	 * Return Signal with correct IDTo.
	 * If no such signal, return any other one.
	 * Return null when there is no signals.
	 * @param IDDevice - the ID of Device
	 * @return - the signal
	 */
	public Signal getSignal(int IDDevice){
		ListIterator<Signal> li=chArray.get(IDDevice).listIterator();
		while (li.hasNext()){
			Signal s=li.next();
			if (s.IDTo==IDDevice){
				li.remove();
				chArray.get(IDDevice).addFirst(s);
			}
			break;
		}
		return chArray.get(IDDevice).peekFirst();
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
	//-------------------------------DEBUG------------------------------------------
	public String[] getString(int num){
		String[] ret=new String[chArray.get(num).size()];
		int j=0;
		for (Signal s:chArray.get(num)){
			ret[j++]=s.getString();
		}
		return ret;
	}
	public String ToString(){
		boolean hasSignal=false;
		StringBuilder sb=new StringBuilder();
		for (LinkedList<Signal> signals:chArray){
			if (signals.isEmpty())
				sb.append("-----");
			else {
				hasSignal=true;
				ListIterator<Signal> li=signals.listIterator();
				Signal s;
				while (li.hasNext())
				{
					s=li.next();
					sb.append(s.type);
					sb.append(s.IDPacket);
					sb.append('-');
					sb.append(s.getSub());
					sb.append('/');
				}
			}
			sb.append("\t");
		}
		if (hasSignal) 
			return sb.toString();
		else {
			return null;
		}
	}
}
