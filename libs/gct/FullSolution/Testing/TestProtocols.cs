using System;
using System.Threading;
using GCT;
using GCT.Protocols;
using GCT.Stack;

namespace Testing
{
	/// <summary>
	/// Summary description for TestProtocols.
	/// </summary>
	public class TestProtocols
	{
		public TestProtocols()
		{
			Tester.SetupDebug();
			bool threads = true;

			HOLD h1 = new HOLD("HOLD1");
			HOLD h2 = new HOLD("HOLD2");
			HOLD h3 = new HOLD("HOLD3", true, false);
			HOLD h4 = new HOLD("HOLD4", false,true);
			HOLD h5 = new HOLD("HOLD5");

			h1.DownProtocol = h2;
			h2.DownProtocol = h3;
			h3.DownProtocol = h4;
			h4.DownProtocol = h5;

			h1.UpProtocol = null;
			h2.UpProtocol = h1;
			h3.UpProtocol = h2;
			h4.UpProtocol = h3;
			h5.UpProtocol = h4;
			
			if(threads)
			{
				h1.startDownHandler();
				h2.startDownHandler();
				h3.startDownHandler();
				h4.startDownHandler();
				h5.startDownHandler();

				h1.startUpHandler();
				h2.startUpHandler();
				h3.startUpHandler();
				h4.startUpHandler();
				h5.startUpHandler();
			}

			h5.up(new Event(Event.CONNECT));

			h1.down(new Event(Event.EXIT));
			h5.up(new Event(Event.MSG));
			
			h1.down(new Event(Event.UNSUSPECT));
			h5.up(new Event(Event.DISCONNECT));
			h1.down(new Event(Event.BECOME_SERVER));

			Console.ReadLine();
		}
	}
}
