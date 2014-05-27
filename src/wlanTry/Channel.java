package wlanTry;

public class Channel {
	public int[] ch;
	public int length;
	public Channel(int n){
		this.length=n;
		ch=new int[n];
		for (int i=0;i<n;i++) ch[i]=-1;
	}
	
	public void debugOutput(int time){
		if (DebugOutput.isDebug)
			this.debugOutput(time,"");
	}
	public void debugOutput(int time, String s){
		StringBuilder sb=new StringBuilder();
		sb.append(time);
		sb.append(": Channel:");
		for (int i=0;i<this.length;i++){
			if (this.ch[i]<0)
				sb.append('*');
			else
				sb.append(this.ch[i]);
			sb.append(" ");
		}
		sb.append(s);
		DebugOutput.output(sb.toString());
	}
}
