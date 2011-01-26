using System;
using System.Threading;
using GCT;
using GCT.Protocols;
using GCT.Stack;

namespace Testing
{
	/// <summary>
	/// Summary description for TestAckSenderWindow.
	/// </summary>
	public class TestAckSenderWindow : AckSenderWindow.RetransmitCommand
	{
		public AckSenderWindow ackSender = null;
		public Address localAddr = new Address(8085);
		public long[] retransmit_timeout = new long[] {1000,2000,4000,8000};


		public TestAckSenderWindow()
		{
			ackSender = new AckSenderWindow(this,retransmit_timeout);
		}
		

		public void retransmit(long seqno, Message msg) 
		{
			Console.WriteLine("RETRANSMIT: ("  + seqno + ")" +"\t" + DateTime.Now.ToLongTimeString());
		}

		static void Main(string[] args)
		{
			TestAckSenderWindow test = new TestAckSenderWindow();
			test.ackSender.setWindowSize(3,2);
			
			Console.WriteLine("-=-=-=-=- Sending 5 messages -=-=-=-=-");
			Message msg = new Message(null,null,"Sending message 0");
			test.ackSender.add(0,msg);

			msg = new Message(null,null,"Sending message 1");
			test.ackSender.add(1,msg);

			msg = new Message(null,null,"Sending message 2");
			test.ackSender.add(2,msg);
			msg = new Message(null,null,"Sending message 3");
			test.ackSender.add(3,msg);
			msg = new Message(null,null,"Sending message 4");
			test.ackSender.add(4,msg);

			Thread.Sleep(10000);
			
			Console.WriteLine("-=-=-=-=- Ack msg 1 -=-=-=-=-");
			test.ackSender.ack(1);
			Thread.Sleep(10000);
			
			Console.WriteLine("-=-=-=-=- Ack msg 0 -=-=-=-=-");
			test.ackSender.ack(0);

			Console.ReadLine();

		}
	}
}
