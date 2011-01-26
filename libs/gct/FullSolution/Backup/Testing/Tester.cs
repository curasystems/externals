using System;
using System.Collections;
using System.Threading;
using GCT;
using GCT.Stack;
using GCT.Protocols;
using System.Diagnostics;

namespace Testing
{
	class Tester
	{
		public static Address localAddr = new Address(8085);

		public static void SetupDebug()
		{
			TextWriterTraceListener myWriter = new TextWriterTraceListener();
			myWriter.Writer = System.Console.Out;
			Debug.Listeners.Add(myWriter); 
			Debug.AutoFlush = true;
			GCT.Trace.trace = true;
		}

		public static void stopProtocols(Protocol top)
		{
			Protocol next = top;
			while (next!=null)
			{
				next.stop();
				next = next.DownProtocol;
			}
		}

		public static Protocol createStack(Protocol middle, out Protocol bot)
		{
			ProtocolSinkStack stack = new ProtocolSinkStack(null,null);
			middle.ProtocolSinkStack = stack;

			TOP top = new TOP("TOP");
			BOTTOM bottom = new BOTTOM();
			bot = bottom;
			top.DownProtocol = middle;
			middle.DownProtocol = bottom;
			bottom.DownProtocol = null;
			
			top.UpProtocol = null;
			middle.UpProtocol = top;
			bottom.UpProtocol = middle;

			bottom.startDownHandler();
			bottom.startUpHandler();
			middle.startDownHandler();
			middle.startUpHandler();
			top.startDownHandler();
			top.startUpHandler();


			top.start();
			middle.start();
			bottom.start();

			bottom.up(new Event(Event.SET_LOCAL_ADDRESS,localAddr));

			return top;
		}
	}
}
