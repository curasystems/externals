using System;
using GCT;
using GCT.Stack;

namespace Testing
{
	/// <summary>
	/// Summary description for BOTTOM.
	/// </summary>
	public class BOTTOM : Protocol
	{
		Address localAddr;

		public BOTTOM()
		{
			this.name = "Bottom";
		}

		public override void start(){}

		public override void stop(){}

		public override void up(Event evt)
		{
			Console.WriteLine(name.PadRight(5) + " [UP]  : " + Event.type2String(evt.Type));
			switch(evt.Type)
			{
				case(Event.SET_LOCAL_ADDRESS):
				{
					localAddr = (Address)evt.Arg;
					break;
				}
			}
			passUp(evt);
		}

		public override void down(Event evt)
		{
			Console.WriteLine(name.PadRight(5) + " [DOWN]: " + Event.type2String(evt.Type));
			Event tmpEvent = evt;
			switch(evt.Type)
			{
				case(Event.SET_LOCAL_ADDRESS):
				{
					localAddr = (Address)evt.Arg;
					return;
				}
				case(Event.MSG):
				{
					if(((Message)tmpEvent.Arg).Source==null)
						((Message)tmpEvent.Arg).Source = localAddr;

					if(((Message)tmpEvent.Arg).Destination==null)
						passUp(tmpEvent);
					break;
				}
			}
		}
	}
}
