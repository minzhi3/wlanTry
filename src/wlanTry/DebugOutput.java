package wlanTry;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public  class DebugOutput {
	public static int time=0;
	public static boolean isDebug=false;
	public static void output(String s){
		if (isDebug){
			System.out.println(s);
		}
	}
	
	public static void outputNeighbor(int id, ArrayList al){
		if (isDebug){
			StringBuilder sb=new StringBuilder();
			sb.append("Neighbor of MT "+id+": ");
			for (int i=0;i<al.size();i++){
				sb.append(al.get(i));
				sb.append(' ');
			}
			System.out.println(sb.toString());
		}
	}
	public static void fileDebugInit(){
        try {

            System.setOut(new PrintStream(new FileOutputStream("d:\\system_out4.txt")));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
	}
	public static void outputChannel(int time){
		if (isDebug){
			DebugOutput.outputChannel(time,"");
		}
	}
	public static void outputChannel(int time,String s){
		if (isDebug && time%10==0){
			StringBuilder sb=new StringBuilder();
			sb.append(time);
			sb.append(": Channel:");
			for (int i=0;i<Device.channel.length;i++){
				if (Device.channel[i]<0)
					sb.append('*');
				else
					sb.append(Device.channel[i]);
				sb.append(" ");
			}
			sb.append(s);
			System.out.println(sb.toString());
		}
	}
	public static void outputAlways(String s){
		System.out.println(s);
	}
	public static void outputAlways(Double s){
		System.out.println(s);
	}

}
