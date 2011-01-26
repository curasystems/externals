using System;
using System.Threading;
using GCT;
using GCT.Protocols;
using GCT.Stack;

namespace Testing
{
	/// <summary>
	/// Summary description for TestUNICAST.
	/// </summary>
	public class TestUNICAST
	{

		
		static void Main(string[] args)
		{
			Tester.SetupDebug();
			Event evt = null;
			UNICAST uni = new UNICAST();
			Protocol bottom = null;
			Protocol top = Tester.createStack(uni, out bottom);
			Address remote1 = new Address("1.1.1.1",8005);
			Address remote2 = new Address("1.1.1.2",8005);
			Address remote3 = new Address("1.1.1.3",8005);


			Console.WriteLine("-=-=-=-=- Send msgs 0-2 to Remote 1 -=-=-=-=-=-");
			evt = new Event(Event.MSG,new Message(remote1,null,"Message 1"));
			top.down(evt);
			evt = new Event(Event.MSG,new Message(remote1,null,"Message 2"));
			top.down(evt);
			evt = new Event(Event.MSG,new Message(remote1,null,"Message 3"));
			top.down(evt);

			Thread.Sleep(7000);

			Console.WriteLine("-=-=-=-=- Ack 0 from Remote 1 -=-=-=-=-=-");
			Message msg = new Message(Tester.localAddr,remote1,"Incoming Message 0");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA_ACK, 0));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Console.WriteLine("-=-=-=-=- Ack 1 from Remote 1 -=-=-=-=-=-");
			msg = new Message(Tester.localAddr,remote1,"Incoming Message 0");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA_ACK, 1));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Thread.Sleep(7000);
			
			Console.WriteLine("-=-=-=-=- Ack 2 from Remote 1 -=-=-=-=-=-");
			msg = new Message(Tester.localAddr,remote1,"Incoming Message 0");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA_ACK, 2));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Thread.Sleep(2000);

			Console.WriteLine("-=-=-=-=- Recv msg 0 from Remote 1 -=-=-=-=-=-");
			msg = new Message(Tester.localAddr,remote1,"Incoming Message 0");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA, 0));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Console.WriteLine("-=-=-=-=- Recv msg 1 from Remote 1 -=-=-=-=-=-");
			msg = new Message(Tester.localAddr,remote1,"Incoming Message 1");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA, 1));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);
			
			Console.WriteLine("-=-=-=-=- Recv msg 3 from Remote 1 -=-=-=-=-=-");
			msg = new Message(Tester.localAddr,remote1,"Incoming Message 1");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA, 3));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);
		
			Thread.Sleep(10000);			

			Console.WriteLine("-=-=-=-=- Recv msg 2 from Remote 1 -=-=-=-=-=-");
			msg = new Message(Tester.localAddr,remote1,"Incoming Message 1");
			msg.Headers.Add("UNICAST",new UnicastHeader(UnicastHeader.DATA, 2));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);


			
			Thread.Sleep(10000);
		}
	}
}
