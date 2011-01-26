using System;
using GCT;
using System.Threading;
using GCT.Protocols;
using GCT.Stack;
using System.Collections;

namespace Testing
{
	/// <summary>
	/// Summary description for TestNAKACK.
	/// </summary>
	public class TestNAKACK
	{
		static Event evt = null;
		
		static void Main(string[] args)
		{
			Tester.SetupDebug();
			NAKACK nak = new NAKACK();
			Protocol bottom = null;
			Protocol top = Tester.createStack(nak, out bottom);
			Address remote1 = new Address("1.1.1.1",8005);
			Address remote2 = new Address("1.1.1.2",8005);
			Address remote3 = new Address("1.1.1.3",8005);

			ArrayList members = new ArrayList();
			members.Add(Tester.localAddr);
			members.Add(remote1);
			members.Add(remote2);
			members.Add(remote3);

			View newView = new View(new ViewId(Tester.localAddr,0),members);
			evt = new Event(Event.VIEW_CHANGE,newView);
			top.down(evt);

			evt = new Event(Event.MSG,new Message(null,null,"Message 1"));
			top.down(evt);
			evt = new Event(Event.MSG,new Message(null,null,"Message 2"));
			top.down(evt);
			evt = new Event(Event.MSG,new Message(null,null,"Message 3"));
			top.down(evt);

			Console.WriteLine("-=-=-=-=- Received Msg 0 from Remote 1 -=-=-=-=-=-");
			Message msg = new Message(null,remote1,"Incoming Message 0");
			msg.Headers.Add("NAKACK",new NakAckHeader(NakAckHeader.MSG, 0));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Console.WriteLine("-=-=-=-=- Received Msg 0 from Remote 2 -=-=-=-=-=-");
			msg = new Message(null,remote2,"Incoming Message 0");
			msg.Headers.Add("NAKACK",new NakAckHeader(NakAckHeader.MSG, 0));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);
			
			Console.WriteLine("-=-=-=-=- Received Msg 1 from Remote 1 -=-=-=-=-=-");
			msg = new Message(null,remote1,"Incoming Message 1");
			msg.Headers.Add("NAKACK",new NakAckHeader(NakAckHeader.MSG, 1));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Console.WriteLine("-=-=-=-=- Received Msg 2 from Remote 2 -=-=-=-=-=-");
			msg = new Message(null,remote2,"Incoming Message 2");
			msg.Headers.Add("NAKACK",new NakAckHeader(NakAckHeader.MSG, 2));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Thread.Sleep(20000);
			
			Console.WriteLine("-=-=-=-=- Received Msg 1 from Remote 2 -=-=-=-=-=-");
			msg = new Message(null,remote2,"Incoming Message 1");
			msg.Headers.Add("NAKACK",new NakAckHeader(NakAckHeader.MSG, 1));
			evt = new Event(Event.MSG,msg);
			bottom.up(evt);

			Thread.Sleep(3000);


			Console.WriteLine("-=-=-=-=- Receiving STABLE event -=-=-=-=-=-");
			Digest digest = new Digest(4); // 4 members
			digest.add(Tester.localAddr, 0, 2, 2);
			digest.add(remote1, 0, 1, 1);
			digest.add(remote2, 0, 2, 2);
			digest.add(remote3, 0, 0, 0);

			evt = new Event(Event.STABLE,digest);
			bottom.up(evt);

			Console.ReadLine();
			Tester.stopProtocols(top);
			
			Console.ReadLine();
		}
	}
}
