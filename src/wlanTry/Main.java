package wlanTry;


public class Main {

	public static void main(String[] args) throws InterruptedException {
		God g=new God();
		g.run();
		/*
		CyclicBarrier cb=new CyclicBarrier(2,new Runnable(){
			public void run(){
				System.out.println("Barrier:"+ThreadTest.f2);
			}
		});
		Object o=new Object();
		ThreadTest r1=new ThreadTest(cb,o);
		ThreadTest r2=new ThreadTest(cb,o);
		Thread t1=new Thread(r1);
		Thread t2=new Thread(r2);
		t1.start();
		t2.start();
		*/
		
	}

}
