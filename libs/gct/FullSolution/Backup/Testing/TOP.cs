using System;
using System.Threading;
using GCT;
using GCT.Stack;

namespace Testing
{
	/// <summary>
	/// Summary description for OutputProtocol.
	/// </summary>
	public class TOP : Protocol
	{
		Address localAddr = null;

		public TOP(String name)
		{
			this.name = name;
		}

		public override void start(){}

		public override void stop(){}

		public override void up(Event evt)
		{
			Console.WriteLine(name.PadRight(5) + " [UP]  : " + Event.type2String(evt.Type));
			switch(evt.Type)
			{
				case(Event.GET_DIGEST):
				{
					Thread.Sleep(10000);
					Digest d = new Digest(1);
					d.add(localAddr,0,0,0);
					evt = new Event(Event.GET_DIGEST_OK,d);
					passDown(evt);
					break;
				}
				case(Event.SET_LOCAL_ADDRESS):
				{
					localAddr = (Address)evt.Arg;
					return;
				}
			}
		}

		public override void down(Event evt)
		{
			Console.WriteLine(name.PadRight(5) + " [DOWN]: " + Event.type2String(evt.Type));
			passDown(evt);
		}
	}
}
