package wlanTry;

import java.util.LinkedList;

public class RequestsQueue {
	LinkedList<Request> requests;
	public RequestsQueue(){
		requests=new LinkedList<Request>();
	}
	
	public Request getFirst(){
		return requests.peekFirst();
	}
	public void popSubpacket(){
		this.getFirst().numSub--;
		if (this.getFirst().numSub==0)
			requests.removeFirst();
	}
	public void addRequest(Request r){
		this.requests.add(r);
	}
	public double getTranmitTime(){
		if (this.getFirst()==null)
			return Double.MAX_VALUE;
		else
			return this.getFirst().time;
	}
	public int getSubpacket(){
		if (this.getFirst()==null){
			return 0;
		}else
			return this.getFirst().numSub;
	}

}
