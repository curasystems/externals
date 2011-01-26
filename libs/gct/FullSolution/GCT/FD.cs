using System;
using System.Collections;
using System.Data;

using GCT.Util;
using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// Regularly polls members for
	/// liveness. Passes SUSPECT message up the stack when a member is not reachable. The simple
	/// algorithms works as follows: the membership is known and ordered. Each HB protocol
	/// periodically sends a 'are-you-alive' message to its *neighbor*. A neighbor is the next in
	/// rank in the membership list. It is recomputed upon a view change. When a response hasn't
	/// been received for n milliseconds and m tries, the corresponding member is suspected (and
	/// eventually excluded if faulty).<p>
	/// FD starts when it detects (in a view change notification) that there are at least
	/// 2 members in the group. It stops running when the membership drops below 2.</p><p>
	/// When a message is received from the monitored neighbor member, it causes the pinger thread to
	/// 'skip' sending the next are-you-alive message. Thus, traffic is reduced.</p><p>
	/// When we receive a ping from a member that's not in the membership list, we shun it by sending it a
	/// NOT_MEMBER message. That member will then leave the group (and possibly rejoin). This is only done if
	/// <code>shun</code> is true.</p>
	/// </remarks>
	/// <summary>
	/// Protocol: Failure Detection based on simple heartbeat protocol
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class FD : Protocol 
	{
		/// <summary>Member to check if active</summary>
		Address				ping_dest=null;
		/// <summary>Local address</summary>
		Address				local_addr=null;
		/// <summary>Number of millisecs to wait for an are-you-alive msg</summary>
		long				timeout=3000; 
		/// <summary>Time of last acknowledgement</summary>
		long				last_ack=System.Environment.TickCount;
		/// <summary>Number of heartbeats still to try before suspecting member</summary>
		int					num_tries=0;
		/// <summary>Maximum number of times to send a heartbeat</summary>
		int					max_tries=2; 
		/// <summary>Current members in the group</summary>
		ArrayList			members=new ArrayList();
		/// <summary>Collection of pingers that aren't members (keys=Address, val=int(number of heartbeats received))</summary>
		Hashtable			invalid_pingers=new Hashtable(); 
		/// <summary>If set, nonmember pingers will be shunned</summary> 
		bool				shun=true;
		/// <summary>Used to run Monitor tasks</summary> 
		TimeScheduler		timer=null;
		/// <summary>Task that performs the actual monitoring for failure detection</summary> 
		Monitor				monitor=null; 

		/// <summary>
		/// Constructor.
		/// </summary>
		public FD()
		{
			name = "FD";
		}

		/// <summary>
		/// Returns unique <c>Protocol</c> name
		/// </summary>
		/// <returns>Unique <c>Protocol</c> name</returns>
		public String  getName() {return name;}


		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props) 
		{
			if(props.Contains("timeout")) 
			{
				timeout = Convert.ToInt64(props["timeout"]);
				props.Remove("timeout");
			}
			if(props.Contains("max_tries")) 
			{
				max_tries = Convert.ToInt32(props["max_tries"]);
				props.Remove("max_tries");
			}
			if(props.Contains("shun")) 
			{
				shun = Convert.ToBoolean(props["shun"]);
				props.Remove("shun");
			}

			if (props.Count > 0)
				return false;
			return true;
		}

		/// <summary>
		/// Calculate the member that should be checked if alive
		/// </summary>
		/// <param name="members">All member in the group</param>
		/// <returns>The member that should be checked if alive</returns>
		private Object getPingDest(ArrayList members) 
		{
			Object tmp, retval=null;

			if(members == null || members.Count < 2 || local_addr == null)
				return null;
			for(int i=0; i < members.Count; i++) 
			{
				tmp=members[i];
				if(local_addr.Equals(tmp)) 
				{
					if(i + 1 >= members.Count)
						retval=members[0];
					else
						retval=members[i+1];
					break;
				}
			}
			return retval;
		}


		/// <summary>
		/// Processes <c>Events</c> traveling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			Message   msg;
			FdHeader  hdr=null;
			Object    sender, tmphdr;

			switch(evt.Type) 
			{

				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;
   
				case Event.MSG:
					msg=(Message)evt.Arg;
					tmphdr=msg.getHeader(getName());
					if(tmphdr == null || !(tmphdr is FdHeader)) 
					{
						if(ping_dest != null && (sender=msg.Source) != null) 
						{
							if(ping_dest.Equals(sender)) 
							{
								last_ack=System.Environment.TickCount;
								if(Trace.trace)
									Trace.info("FD.up()", "received msg from " + sender + " (counts as ack)");
								num_tries=0;
							}
						}
						break;  // message did not originate from FD layer, just pass up
					}

					hdr=(FdHeader)msg.removeHeader(getName());
				switch(hdr.type) 
				{
					case FdHeader.HEARTBEAT:                       // heartbeat request; send heartbeat ack
						Address  hb_sender=msg.Source;
						Message  hb_ack=new Message(msg.Source, null, null);
						FdHeader tmp_hdr=new FdHeader(FdHeader.HEARTBEAT_ACK);

						// 1.  Send an ack
						tmp_hdr.from=local_addr;
						hb_ack.putHeader(getName(), tmp_hdr);
						passDown(new Event(Event.MSG, hb_ack));

						// 2. Shun the sender of a HEARTBEAT message if that sender is not a member. This will cause
						//    the sender to leave the group (and possibly rejoin it later)
						if(shun)
							shunInvalidHeartbeatSender(hb_sender);

						if(ping_dest == null) // we're being pinged but not pinging anyone
						{
							stop();
							ping_dest=(Address)getPingDest(members);
							if(ping_dest != null)
								startMonitor();
						}
						break;                                     // don't pass up !

					case FdHeader.HEARTBEAT_ACK:                   // heartbeat ack
						if(ping_dest != null && ping_dest.Equals(hdr.from)) 
						{
							last_ack=System.Environment.TickCount;
							num_tries=0;
							if(Trace.trace)
								Trace.info("FD.up()", "received ack from " + hdr.from);
						}
						else 
						{
							stop();
							ping_dest=(Address)getPingDest(members);
							if(ping_dest != null)
								startMonitor();
						}
						break;

					case FdHeader.SUSPECT:
						if(hdr.suspected_mbr != null) 
						{
							if(Trace.trace)
								Trace.info("FD.up()", "[SUSPECT] suspect hdr is " + hdr);
		    
							if(local_addr != null && hdr.suspected_mbr != null && hdr.suspected_mbr.Equals(local_addr)) 
								if(Trace.trace)
									Trace.warn("FD.up()", "I was suspected, but will not remove myself from membership " + "(waiting for EXIT message)");
							else
								members.Remove(hdr.suspected_mbr);

							ping_dest = (Address)getPingDest(members);
							passUp(new Event(Event.SUSPECT, hdr.suspected_mbr));
							passDown(new Event(Event.SUSPECT, hdr.suspected_mbr));
						}
						break;

					case FdHeader.NOT_MEMBER:
						if(shun) 
						{
							if(Trace.trace)
								Trace.info("FD.up()", "[NOT_MEMBER] I'm being shunned; exiting");
							passUp(new Event(Event.EXIT));
						}
						break;
				}
					return;
			}
			passUp(evt);                                        // pass up to the layer above us
		}



		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt) 
		{
			switch(evt.Type) 
			{
				case Event.VIEW_CHANGE:
					lock(this) 
					{
						stop();
						View v=(View)evt.Arg;
						members.Clear();
						members.AddRange(v.getMembers());
						passDown(evt);
						ping_dest=(Address)getPingDest(members);
						if(ping_dest != null)
							startMonitor();
					}
					break;
				default:
					passDown(evt);
					break;
			}
		}

		/// <summary>
		/// Sets the <c>TimeScheduler</c> to be used to the one present in the <c>ProtocolSinkStack</c>
		/// </summary>
		public override void init() 
		{
			if(stack != null && stack.timer != null)
				timer=stack.timer;
			else
				if(Trace.trace)
				Trace.error("FD.up()", "[START]: timer cannot be retrieved from protocol stack");
		}

		/// <summary>
		/// Starts the Monitor task.
		/// </summary>
		public void startMonitor()
		{
			if(monitor != null && monitor.started == false) 
			{
				monitor=null;
			}
			if(monitor == null) 
			{
				monitor=new Monitor(this);
				last_ack=System.Environment.TickCount;  // start from scratch
				timer.add(monitor, true);  // fixed-rate scheduling
				num_tries=0;

			}
		}

		/// <summary>
		/// Stops the Monitor task if its active.
		/// </summary>
		public override void stop() 
		{
			if(monitor != null) 
			{
				monitor.stop();
				monitor=null;
			}
		}

		/// <summary>
		/// Shun member pinging this member if it is not a member of the group.
		/// </summary>
		/// <param name="hb_sender"></param>
		private void shunInvalidHeartbeatSender(Address hb_sender) 
		{
			int      num_pings=0;
			Message  shun_msg;

			if(hb_sender != null && members != null && !members.Contains(hb_sender)) 
			{
				if(invalid_pingers.ContainsKey(hb_sender)) 
				{
					num_pings=(int)invalid_pingers[hb_sender];
					if(num_pings >= max_tries)
					{
						if(Trace.trace)
							Trace.info("FD.shunInvalidHeartbeatSender()", "sender " + hb_sender +
							" is not member in " + members + " ! Telling it to leave group");
						shun_msg=new Message(hb_sender, null, null);
						shun_msg.putHeader(getName(), new FdHeader(FdHeader.NOT_MEMBER));
						passDown(new Event(Event.MSG, shun_msg));
						invalid_pingers.Remove(hb_sender);
					}
					else 
					{
						num_pings++;
						invalid_pingers[hb_sender] = (int)num_pings;
					}
				}
				else 
				{
					num_pings++;
					invalid_pingers[hb_sender] = (int)num_pings;
				}
			}
		}

		/// <summary>
		/// Internal Class used to sent Heartbeat messages.
		/// </summary>
		private class Monitor : TimeScheduler.Task 
		{
			/// <summary>
			/// Task runs when set.
			/// </summary>
			public bool started=true;
			private FD fd;

			/// <summary>
			/// Stops the task
			/// </summary>
			public void stop() 
			{
				started=false;
			}

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="fd">FD associated with the task</param>
			public Monitor(FD fd)
			{
				this.fd = fd;
			}

			/// <summary>
			/// Returns the name of the task
			/// </summary>
			/// <returns>The name of the task</returns>
			public String getName()
			{
				return "FD_Monitor";
			}

			/// <summary>
			/// Checks if the task is cancelled
			/// </summary>
			/// <returns>True is the task is cancelled</returns>
			public bool cancelled()  
			{
				return !started;
			}
    
			/// <summary>
			/// Returns the time until the task should be ran again
			/// </summary>
			/// <returns>The time until the task should be ran again</returns>
			public long nextInterval() 
			{
				return fd.timeout;
			}

			/// <summary>
			/// Sends a Heartbeat to the <i>next</i> member in the group
			/// </summary>
			public void run() 
			{
				Message   suspect_msg, hb_req;
				FdHeader  hdr;
				long      not_heard_from=0; // time in msecs we haven't heard from ping_dest

				if(fd.ping_dest == null) 
				{
					if(Trace.trace)
						Trace.error("FD.Monitor.run()", "ping_dest is null");
					return;
				}
	    
				// 1. send heartbeat request
				hb_req=new Message(fd.ping_dest, null, null);
				hb_req.putHeader(fd.Name, new FdHeader(FdHeader.HEARTBEAT));  // send heartbeat request
				if(Trace.trace)
					Trace.info("FD.Monitor.run()", "sending are-you-alive msg to " + fd.ping_dest);
				fd.passDown(new Event(Event.MSG, hb_req));

				// 2. If the time of the last heartbeat is > timeout and max_tries heartbeat messages have not been
				//    received, then broadcast a SUSPECT message. Will be handled by coordinator, which may install
				//    a new view
				not_heard_from=System.Environment.TickCount - fd.last_ack;

				if(not_heard_from >= fd.timeout) 
				{ 
					// no heartbeat ack for more than timeout msecs
					if(fd.num_tries >= fd.max_tries) 
					{
						if(Trace.trace) 
							Trace.info("FD.Monitor.run()", "[" + fd.local_addr + "]: received no heartbeat ack from " + fd.ping_dest + ", suspecting it");
						hdr=new FdHeader(FdHeader.SUSPECT);
						hdr.suspected_mbr=fd.ping_dest;
						hdr.from=fd.local_addr;
						suspect_msg=new Message(null,null,null);                       // mcast SUSPECT to all members
						suspect_msg.putHeader(fd.Name, hdr);
						fd.passDown(new Event(Event.MSG, suspect_msg));
					}
					else 
					{
						if(Trace.trace)
							Trace.info("FD.Monitor.run()", "heartbeat missing from " + fd.ping_dest + 
								" (number=" + fd.num_tries + ")");
		    
						fd.num_tries++;
					}
				}	    
			}

		}

	}
}
