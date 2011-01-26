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
	public class TestAckReceiverWindow
	{
		public AckReceiverWindow ackRecv = null;
		public Address localAddr = new Address(8085);
		public Address remoteAddr = new Address("1.2.3.4",8085);

		public TestAckReceiverWindow()
		{
			ackRecv = new AckReceiverWindow(0);
		}
		
		public void remove()
		{
			Message msg_to_deliver = null;
			int count = 0;
			Console.WriteLine(ackRecv.ToString());
			while((msg_to_deliver=ackRecv.remove()) != null) 
			{
				count++;
			}
			Console.WriteLine("Delivered " + count + " Messages");
			Console.WriteLine(ackRecv.ToString());
		}

		static void Main(string[] args)
		{
			TestAckReceiverWindow test = new TestAckReceiverWindow();
			
			Console.WriteLine("-=-=-=-=- Receive msgs 0-2 -=-=-=-=-");
			Message msg = new Message(null,null,"Sending message 0");
			test.ackRecv.add(0,msg);
			msg = new Message(null,null,"Sending message 1");
			test.ackRecv.add(1,msg);
			msg = new Message(null,null,"Sending message 2");
			test.ackRecv.add(2,msg);

			
			Console.WriteLine("-=-=-=-=-=- Remove -=-=-=-=-=-");
			test.remove();

			
			Console.WriteLine("-=-=-=-=- Receive msgs 4-5 -=-=-=-=-");
			msg = new Message(null,null,"Sending message 4");
			test.ackRecv.add(4,msg);
			msg = new Message(null,null,"Sending message 5");
			test.ackRecv.add(5,msg);

			Console.WriteLine("-=-=-=-=-=- Remove -=-=-=-=-=-");
			test.remove();
			

			Console.WriteLine("-=-=-=-=- Receive msgs 3 -=-=-=-=-");
			msg = new Message(null,null,"Sending message 3");
			test.ackRecv.add(3,msg);
			
			Console.WriteLine("-=-=-=-=-=- Remove -=-=-=-=-=-");
			test.remove();
			Console.ReadLine();

		}
	}
}