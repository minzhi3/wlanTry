package wlanTryTest;

import static org.junit.Assert.*;

import org.junit.Test;

import wlanTry.Channel;

public class ChannelTest {
	Channel ch;
	@Test
	public void test() {
		ch=new Channel(3);
		ch.addSignal(0, 1, "data", 10);
		this.stringArray(ch.getString(0));
		ch.setTime(5);
		ch.addSignal(0, 1, "ack", 10);
		this.stringArray(ch.getString(0));
		ch.setTime(5);
		ch.checkSignalOver(0);
		this.stringArray(ch.getString(0));
		ch.setTime(5);
		ch.checkSignalOver(0);
		ch.addSignal(0, 1, "DATA2", 10);
		this.stringArray(ch.getString(0));
		
		
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
