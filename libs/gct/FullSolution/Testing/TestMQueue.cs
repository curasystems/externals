using System;
using System.Threading;
using GCT.Util;

namespace Testing
{
	/// <summary>
	/// Summary description for TestMQueue.
	/// </summary>
	public class TestMQueue
	{
		MQueue q = new MQueue();
		public TestMQueue()
		{
			Thread tick = new Thread(new ThreadStart(ticker));
			tick.IsBackground = true;
			tick.Start();
		}

		public void run()
		{
			Console.WriteLine("-=-=-=-=- Add 1 object -=-=-=-=-");
			q.Add(1);

			Console.WriteLine("-=-=-=-=- Peek 5sec -=-=-=-=-");
			Console.WriteLine("Peek: " + q.peek(5000));
			Console.WriteLine("-=-=-=-=- Remove 5sec -=-=-=-=-");
			Console.WriteLine("Remv: " + q.Remove(5000));
			Console.WriteLine("-=-=-=-=- Peek 5sec -=-=-=-=-");
			Console.WriteLine("Peek: " + q.peek(5000));
			Console.WriteLine("-=-=-=-=- Add 1 object -=-=-=-=-");
			q.Add(1);
			Console.WriteLine("-=-=-=-=- Remove 5sec -=-=-=-=-");
			Console.WriteLine("Remv: " + q.Remove(5000));
			Console.WriteLine("-=-=-=-=- Remove 5sec -=-=-=-=-");
			Console.WriteLine("Remv: " + q.Remove(5000));
			Console.WriteLine("-=-=-=-=- Close Queue -=-=-=-=-");
			q.close();
			Console.WriteLine("Closed?: " + q.Closed);

			try
			{
				Console.WriteLine("-=-=-=-=- Remove 5sec -=-=-=-=-");
				Console.WriteLine("Remv: " + q.Remove());
			}
			catch(Exception e)
			{
				Console.WriteLine("-=-=-=-=- Exception Thrown -=-=-=-=-");
				Console.WriteLine("Exception: " + e.Message);
			}

		}

		public void ticker()
		{
			while(true)
			{
				Thread.Sleep(1000);
				Console.WriteLine("\t\t\t\t\t\t\t\t" + DateTime.Now.ToLongTimeString());
			}
		}

		static void Main(string[] args)
		{
			TestMQueue test = new TestMQueue();
			test.run();
			Console.ReadLine();
		}
	}
}
