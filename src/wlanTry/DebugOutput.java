package wlanTry;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class DebugOutput {
	public int time=0;
	public static boolean isDebug=false;
	public static void output(String s){
		if (isDebug){
			System.out.println(s);
		}
	}
	
	public static void outputNeighbor(int id, ArrayList<Integer> al){
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
	public void fileDebugInit(String s){
        try {

            System.setOut(new PrintStream(new FileOutputStream(s+".txt")));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return;
        }
	}

	public static void outputAlways(String s){
		System.out.println(s);
	}
	public static void outputAlways(Double s){
		System.out.println(s);
	}

}
