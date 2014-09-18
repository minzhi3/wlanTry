package wlanTry;

public class Flag {
	boolean f[];
	int count;
	public Flag(int n){
		f=new boolean[n];
		count=0;
	}
	public void add(int n){
		if (!f[n]){
			count++;
			f[n]=true;
		}
	}
	public void remove(int n){
		if (f[n]){
			count--;
			f[n]=false;
		}
	}
	public int getNum(){
		return count;
	}

}
