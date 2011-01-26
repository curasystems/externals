using System;
using System.Threading;
using GCT;
using GCT.Protocols;
using GCT.Stack;

namespace Testing
{
	public class TestNakReceiverWindow : Retransmitter.RetransmitCommand
	{
		public Address localAddr = new Address(8085);
		public Address remoteAddr = new Address("1.1.1.1",8085);
		public long[] retransmit_timeout = new long[] {1000,2000,4000,8000};
		public NakReceiverWindow nak;

		public void init()
		{
			nak = new NakReceiverWindow(remoteAddr,this,0);
			nak.setRetransmitTimeouts(retransmit_timeout);
		}

		public void retransmit(long first_seqno, long last_seqno, Address sender) 
		{
			Console.WriteLine("RETRANSMIT: ("  + first_seqno + "->" + last_seqno + ") To " + sender.ToString() +"\t" + DateTime.Now.ToLongTimeString());
		}

		public void remove()
		{
			Message msg_to_deliver = null;
			int count = 0;
			while((msg_to_deliver=nak.remove()) != null) 
			{
				count++;
			}
			Console.WriteLine("Delivered " + count + " Messages");
		}

		static void Main(string[] args)
		{
			int msgNo = 10;

			TestNakReceiverWindow test = new TestNakReceiverWindow();
			test.init();
			Message[] msgs = new Message[msgNo];
			for(int i= 0;i<msgNo;i++)
				msgs[i] = new Message(test.localAddr,test.remoteAddr, i + ": Test Message");

			Console.WriteLine("-=-=-=-=-=- Adding msgs 0-2 -=-=-=-=-=-");
			for(int i= 0;i < 3;i++)
				test.nak.add(i,msgs[i]);
			
			Console.WriteLine("-=-=-=-=-=- Remove -=-=-=-=-=-");
			Console.WriteLine(test.nak.toString());
			test.remove();
			Console.WriteLine(test.nak.toString());

			Console.WriteLine("-=-=-=-=-=- Adding msg 2 again -=-=-=-=-=-");
			test.nak.add(2,msgs[2]);
			Console.WriteLine(test.nak.toString());
			
			Console.WriteLine("-=-=-=-=-=- Adding msgs 3-4 -=-=-=-=-=-");
			test.nak.add(3,msgs[3]);
			test.nak.add(4,msgs[4]);
			
			Console.WriteLine("-=-=-=-=-=- Adding msgs 6-7 -=-=-=-=-=-");
			test.nak.add(6,msgs[6]);
			test.nak.add(7,msgs[7]);

			Thread.Sleep(10000);
			
			Console.WriteLine("-=-=-=-=-=- Remove -=-=-=-=-=-");
			test.remove();
			Console.WriteLine(test.nak.toString());

			Thread.Sleep(20000);

			Console.WriteLine("-=-=-=-=-=- Adding msg 5 -=-=-=-=-=-");
			test.nak.add(5,msgs[5]);

			Console.WriteLine("-=-=-=-=-=- Remove -=-=-=-=-=-");
			Console.WriteLine(test.nak.toString());
			test.remove();
			Console.WriteLine(test.nak.toString());
			
			Console.WriteLine("-=-=-=-=-=- Stable up to 5 -=-=-=-=-=-");
			test.nak.stable(5);
			Console.WriteLine(test.nak.toString());
			Console.WriteLine("-=-=-=-=-=- Stable up to 7 -=-=-=-=-=-");
			test.nak.stable(7);
			Console.WriteLine(test.nak.toString());

			Console.ReadLine();
		}
	}

}
