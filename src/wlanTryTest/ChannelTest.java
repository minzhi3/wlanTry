package wlanTryTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import wlanTry.Channel;
import wlanTry.PacketType;
import wlanTry.Request;

public class ChannelTest {
	Channel ch;
	List<Integer> neighbor0;
	List<Integer> neighbor1;
	List<Integer> neighbor2;
	private void neighborInit(){
		List<Integer> neighbor0=new ArrayList<Integer>();
		neighbor0.add(0);
		neighbor0.add(1);
		List<Integer> neighbor2=new ArrayList<Integer>();
		neighbor2.add(1);
		neighbor2.add(2);
		List<Integer> neighbor1=new ArrayList<Integer>();
		neighbor1.add(0);
		neighbor1.add(1);
		neighbor1.add(2);
	}
	@Test
	public void test() {
		ch=new Channel(3);
		this.neighborInit();
		Request request=new Request(1, 0, 1, PacketType.DATA, 2, 10);
		Request request1=new Request(0, 5, 2, PacketType.ACK, 1, 5);
		Request request2=new Request(1, 15, 3, PacketType.DATA, 1, 10);
		

		System.out.println("Time=0------------");
		ch.setTime(0);
		for (int i=0;i<3;i++){
			ch.checkSignalOver(i);
		}
		ch.addSignal(neighbor0,0,request);
		for (int i=0;i<3;i++){
			this.stringArray(ch.getString(i));
		}
		
		System.out.println("Time=5------------");
		ch.setTime(5);
		for (int i=0;i<3;i++){
			ch.checkSignalOver(i);
		}
		ch.addSignal(neighbor1,1,request1);
		for (int i=0;i<3;i++){
			this.stringArray(ch.getString(i));
		}
		
		System.out.println("Time=10------------");
		ch.setTime(10);
		for (int i=0;i<3;i++){
			ch.checkSignalOver(i);
		}
		for (int i=0;i<3;i++){
			this.stringArray(ch.getString(i));
		}
		
		System.out.println("Time=15------------");
		ch.setTime(15);
		for (int i=0;i<3;i++){
			ch.checkSignalOver(i);
		}
		ch.addSignal(neighbor2, 2, request2);
		for (int i=0;i<3;i++){
			this.stringArray(ch.getString(i));
		}
		System.out.println("Time=20------------");
		ch.setTime(20);
		for (int i=0;i<3;i++){
			ch.checkSignalOver(i);
		}
		for (int i=0;i<3;i++){
			this.stringArray(ch.getString(i));
		}
	}
	private void stringArray(String [] s){
		if (s.length==0){
			System.out.println("NULL");
		}else{
			for (String t:s){
				System.out.println(t);
			}
		}
		System.out.println("------------");
	}

}
