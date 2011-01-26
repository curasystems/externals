using System;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Diagnostics;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;

using GCT;
using GCT.Stack;
using GCT.Protocols;
using GCT.Remoting;
using GCT.Util;

namespace Testing
{
	class RemotingTest
	{
		[STAThread]
		static void Main(string[] args)
		{
			DateTime dt = DateTime.Now;
			String time = dt.ToString("yyyy-MM-dd HH.mm.ss.ff");

			Debug.Listeners.Add(new TextWriterTraceListener("CLIENT LOG (" + time + ").txt")); //Console.out
			Debug.AutoFlush = true;

			Debug.WriteLine(DateTime.Now.ToString("HH.mm.ss.ff") + " [Client.Main]");

			GroupChannel chan = new GroupChannel();
			chan.ResponseChooser = new TimeChooser();

			Debug.WriteLine(DateTime.Now.ToString("HH.mm.ss.ff") + "[i] - Registering the channel");
			ChannelServices.RegisterChannel(chan);
			
			chan.setOpt("receive_local_msgs",true);

			TimeServer currentTime = null;

			String input = "";
			while(input != "Exit" && input != "exit" && input != "e")
			{
				System.Threading.Thread.Sleep(250);
				Console.WriteLine("********* What do you want to do? (Press ? for help)");
				input = Console.ReadLine();
				switch (input)
				{
					case("c"):
						Console.WriteLine("Enter group to join: ");
						String groupname = Console.ReadLine();
						chan.connect(groupname);
						break;
					case("d"):
						chan.disconnect();
						break;
					case("s"):
						for(int i = 0;i<5;i++)
							Console.WriteLine("");
						break;
					case("r"):
						RemotingConfiguration.RegisterWellKnownServiceType(new TimeServer().GetType(), "TimeServer", WellKnownObjectMode.SingleCall);
						break;
					case("t"):
						Debug.WriteLine(DateTime.Now.ToString("HH.mm.ss.ff") + "Getting a reference to the TaskRunner Object");
						if (currentTime==null)
							currentTime = (TimeServer)Activator.GetObject(typeof(TimeServer), "C#Groups://null:8090/TimeServer");
						
						DateTime responseTime = currentTime.getCurrentTime();
						
						Console.WriteLine("Current Time is: " + DateTime.Now.ToString("HH:mm:ss:ff"));
						Console.WriteLine("TimeServer says: " + responseTime.ToString("HH:mm:ss:ff"));
						Debug.WriteLine("");
						break;
					case("?"):
						Console.WriteLine("Commands Are:\n");
						Console.WriteLine("		c: Connect to Group");
						Console.WriteLine("		d: Disconnect from Group");
						Console.WriteLine("		r: Register TimeServer");
						Console.WriteLine("		t: Get Current Time From Servers");
						Console.WriteLine("		e: Exit");
						break;
				}
			}
			Console.WriteLine("Exiting.....");
			chan.close();
		}
	}
}

