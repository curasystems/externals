using System;
using GCT;
using System.Diagnostics;
using System.Collections;
using System.Threading;

namespace Testing
{
	/// <summary>
	/// Summary description for CausalTest.
	/// </summary>
	public class CausalTest
	{
		static void Main(string[] args)
		{
			int testSize = 1000;
			int count = 0;
			int[] values = new int[1000+10];
			string received = "Received: ";
			string props = "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=32):" +
				"PING(timeout=3000;num_initial_members=6):" +
				//"FD(timeout=3000):" +
				//"VERIFY_SUSPECT(timeout=1500):" +
				"STABLE(desired_avg_gossip=10000):" +
				"DISCARD(up=0.2;excludeItself=true):" +
				"NAKACK(gc_lag=10;retransmit_timeout=3000):" +
				"UNICAST(timeout=2000):" +
				"GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)"; 
				
			if(args.Length == 3 && args[2].Equals("C"))
				props += ":CAUSAL";

			String time = DateTime.Now.ToString("yyyy-MM-dd HH.mm.ss.ff");
			//Debug.Listeners.Add(new TextWriterTraceListener("CLIENT LOG (" + time + ").txt")); 
			System.Diagnostics.Trace.Listeners.Add(new TextWriterTraceListener("CAUSAL LOG (" + time + ").txt")); 
			Debug.AutoFlush = true;
			GCT.Trace.trace = true;


			Channel chan = new GroupChannel(props);
			Console.WriteLine("Connecting: " + DateTime.Now.ToString("yyyy-MM-dd HH.mm.ss.ff"));
			chan.connect("Causal");
			Thread.Sleep(10000);
			bool started = false;
			int currentPosition = 0;
			int mod = Convert.ToInt32(args[1]);
			Console.WriteLine("Connected!	Members=" + chan.getView().getMembers().Count + "	:" + DateTime.Now.ToString("yyyy-MM-dd HH.mm.ss.ff"));

			while(true)
			{
				if(args[0].Equals("start") && started == false)
				{
					Console.WriteLine("Sending start message");
					chan.send(new Message(null,null,new CausalMessage(count.ToString(),(Address)chan.getView().getMembers()[0])));
				}
				started = true;
				Event obj = chan.receive(-1);
				if (obj.Type == Event.MSG)
				{
					Message msg = (Message)obj.Arg;
					if(msg.getObject()==null)
					{
						String strHeaders = "";		
						if(strHeaders.Length > 0)
						{		
							foreach(DictionaryEntry d in msg.Headers)
							{
								strHeaders += (String)d.Key + ":";
							}
							strHeaders = strHeaders.Substring(0,(strHeaders.Length-1));
						}
						if(GCT.Trace.trace)
							GCT.Trace.info("UDP.handleIncomingUdpPacket()","Headers = " + strHeaders);

						Console.WriteLine("Received NULL: Frm-->" + msg.Source + " Headers: " + strHeaders);
						continue;
					}
					int recObj=  Convert.ToInt32(((CausalMessage)msg.getObject()).message);
					received += recObj + ",";
					Console.WriteLine(received);
					System.Diagnostics.Trace.WriteLine("Recevied = " + count);
					count = recObj+1;
					values[currentPosition] = count-1;
					currentPosition++;
					bool error = false;
					for(int i = 0;i<count;i++)
					{
						if(values[i]!=i && values[i]!=0)
						{
							Console.WriteLine("Position: " + i + " = " + values[i]);
							error = true;
						}
					}
					if(error)
						break;
					
					if(((CausalMessage)msg.getObject()).member.Equals(chan.getLocalAddress()))
					{
						Random r  = new Random();
						ArrayList members =chan.getView().getMembers();
						int nextTarget = r.Next(members.Count);

						//chose someone other than yourself
						while (nextTarget == members.IndexOf(chan.getLocalAddress()))
						{
							nextTarget = r.Next(members.Count);
						}
						Address next = (Address)members[nextTarget];
						//String nextChar = getNext(receivedLetter);

						Console.WriteLine("Sending = " + count + "	NextResponder--> " + next);
						chan.send(new Message(null, null, new CausalMessage(count.ToString(), next)));
					}
					
					/*
					if(count%2 == mod)
					{
						Console.WriteLine("Sending Message: " + count);
						System.Diagnostics.Trace.WriteLine("Sending = " + count);
						chan.send(new Message(null,null,count));
					}
					*/
				}
				if(count==testSize)
					break;
			}
			Console.WriteLine("Finished2!!!");
			chan.disconnect();
		}
	}

	[Serializable]
	class CausalMessage 
	{
		public String message;
		public Address member;

		public CausalMessage(String message, Address member)
		{
			this.message = message;
			this.member = member;
		}

		public String toString()
		{
			return "CausalMessage[" + message + "=" + message + "member=" + member + "]";
		}

	}

}
