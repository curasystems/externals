using System;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Diagnostics;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;

using GCT;
using GCT.Stack;
using GCT.Protocols;
using GCT.Remoting;
using GCT.Util;

namespace Testing
{
	class OtherTests
	{
		[STAThread]
		static void Main(string[] args)
		{
			OtherTests test  = new OtherTests();
			test.testTimeScheduler();
			test.testRetransmitter();
		}
		
		private void testTimeScheduler()
		{
			TimeScheduler timer = new TimeScheduler();
			TimerTask[] array = new TimerTask[5];

			for(int i = 0; i<5 ; i++)
			{
				array[i] = new TimerTask((i+1)*1000,"TimerTask:"+i,0,1);
				timer.add(array[i]);
			}
			timer.start();
			Console.WriteLine("timer Started.");
			int seconds = 0;
			bool running = true;
			while(running)
			{
				seconds++;
				System.Threading.Thread.Sleep(1000);
				Console.WriteLine("");
				Console.WriteLine("-=-=-=-=- Seconds = " + seconds + "-=-=-=-=-=-=-");
				
				if (seconds == 5)
				{
					timer.stop();
					System.Threading.Thread.Sleep(3000);
					timer.start();
				}

				Console.WriteLine(array[0].getName() + "		 Value = " + array[0].start);
				Console.WriteLine(array[1].getName() + "		 Value = " + array[1].start);
				Console.WriteLine(array[2].getName() + "		 Value = " + array[2].start);
				Console.WriteLine(array[3].getName() + "		 Value = " + array[3].start);
				Console.WriteLine(array[4].getName() + "		 Value = " + array[4].start);
				running = false;
				for(int i = 0; i<5 ; i++)
				{
					if(!array[i].cancelled())
						running = true;
				}
			}
			Console.ReadLine();
			
		}

			
		private void testRetransmitter()
		{
			Retransmitter xmitter;
			Address       sender;

			try 
			{
				sender = new Address("localhost", 5555);
				xmitter = new Retransmitter(sender, new MyXmitter());
				xmitter.setRetransmitTimeouts(new long[]{1000,2000,4000,8000});
	    
				xmitter.add(1, 10);
				System.Threading.Thread.Sleep(8000);
				xmitter.remove(10);
				System.Threading.Thread.Sleep(8000);
				xmitter.remove(4);

				System.Threading.Thread.Sleep(8000);
				xmitter.remove(1);
				xmitter.remove(2);
				xmitter.remove(3);
				xmitter.remove(5);
				xmitter.remove(6);
				xmitter.remove(8);
				xmitter.remove(9);

				System.Threading.Thread.Sleep(8000);
				xmitter.remove(7);

				Console.ReadLine();
			}
			catch(Exception e) 
			{
				Console.WriteLine(e);
			}
		}

		
		public class MyXmitter : Retransmitter.RetransmitCommand 
		{

			public void retransmit(long first_seqno, long last_seqno, Address sender) 
			{
				Console.WriteLine("MyXmitter.retransmit() \t\t -- " + System.DateTime.Now + ": retransmit(" + first_seqno + ", " + last_seqno + ", " + sender + ")");
			}
		}
	}
}
