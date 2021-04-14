package controller;

public class Stopwatch
{
	private static long begin=-1;	//time in milliseconds
	private static long end=-1;
	
	private static long now()
	{
		return System.currentTimeMillis();
	}
	
	public static void start()
	{
		reset();
		begin=now();
	}
	
	public static double lap()
	{
		return (now()-begin)/1000.0;
	}
	
	public static void stop()
	{
		end=now();
	}
	
	public static double getTime()
	{
		return (end!=-1)?(begin-end)/1000.0:-1;
	}
	
	public static void reset()
	{
		begin=end=-1;
	}
}
