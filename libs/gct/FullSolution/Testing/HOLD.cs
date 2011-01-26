using System;
using System.Threading;
using GCT;
using GCT.Stack;

namespace Testing
{
	public class HOLD : Protocol
	{
		public bool		up_hold = false;
		public bool		down_hold = false;

		public HOLD(String name)
		{
			this.name = name;
		}

		public HOLD(String name,bool up_hold, bool down_hold) : this(name)
		{
			this.up_hold = up_hold;
			this.down_hold = down_hold;
		}

		public override void start(){}

		public override void stop(){}

		public override void up(Event evt)
		{
			Console.WriteLine(name.PadRight(5) + " [UP]  : " + Event.type2String(evt.Type));
			if (up_hold && evt.Type == Event.MSG)
			{
				//Thread.Sleep(10000);
				for(int i = 0; i<10000000;i++)
				{
					Console.Write("");
				}
				Console.WriteLine("");
				Console.WriteLine("FINISHED HOLDING");
				Console.WriteLine("");
			}
			passUp(evt);
			
		}

		public override void down(Event evt)
		{
			Console.WriteLine(name.PadRight(5) + " [DOWN]: " + Event.type2String(evt.Type));
			if (down_hold && evt.Type == Event.MSG)
				return;
			passDown(evt);
		}
	}
}
