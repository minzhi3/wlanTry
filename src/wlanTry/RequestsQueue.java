package wlanTry;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class RequestsQueue {
	LinkedList<Request> requests;
	public RequestsQueue(){
		requests=new LinkedList<Request>();
	}
	
	public Request getFirst(){
		return requests.peekFirst();
	}
	public boolean popSubpacket(){
		this.getFirst().numSub--;
		if (this.getFirst().numSub==0){
			requests.removeFirst();
			return true;
		}
		return false;
	}
	public void pop(){
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
	public void sort(){
		Collections.sort(this.requests, new Comparator<Request>() {
			@Override
			public int compare(Request o1, Request o2) {
				return o1.time<o2.time?-1:1;
			}
		});
	}

}
