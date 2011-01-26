using System;
using System.Diagnostics;
using System.Data;
using System.Collections;
using System.Threading;

using GCT.Stack;

namespace GCT.Protocols
{
	/// <summary>
	///  Protocol: PING gets the initial members in the group
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class PING : Protocol
	{
		/// <summary>Current members in the group</summary>
		ArrayList			members = ArrayList.Synchronized(new ArrayList());
		/// <summary>List of responses to initial members request</summary>
		ArrayList			initial_members =  ArrayList.Synchronized(new ArrayList());
		/// <summary>Local Address</summary>
		Address				local_addr=null;
		/// <summary>Group Multicast Address</summary>
		String				group_addr=null;
		/// <summary>Time interval to wait for responses</summary>
		long				timeout=3000;
		/// <summary>Number of responses required to override timeout</summary>
		long				num_initial_members=2;
		/// <summary>If set channel is a member of the group</summary>
		bool				is_server=false;
		
		/// <summary>
		/// Constructor.
		/// </summary>
		public PING()
		{
			name = "PING";
		}

		/// <summary>
		/// Provided services for layers below NAKACK
		/// </summary>
		/// <returns>List of provided service</returns>
		public override ArrayList providedUpServices() 
		{
			ArrayList ret=new ArrayList();
			ret.Add((int)Event.FIND_INITIAL_MBRS);
			return ret;
		}

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props)
		{
			if(props.Contains("num_initial_members")) 
			{
				num_initial_members = Convert.ToInt32(props["num_initial_members"]);
				props.Remove("num_initial_members");
			}
			if(props.Contains("timeout")) 
			{
				timeout = Convert.ToInt64(props["timeout"]);
				props.Remove("timeout");
			}
			
			if (props.Count > 0)
				return false;

			return true;
		}

		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt)
		{
			Message      msg, rsp_msg;
			Object       obj;
			PINGHeader   hdr, rsp_hdr;
			PingRsp      rsp;
			Address      coord;

			switch(evt.Type) 
			{
				case Event.MSG:
					msg=(Message)evt.Arg;
					obj=msg.getHeader(name);
					if(obj == null || !(obj is PINGHeader)) 
					{
						passUp(evt);
						return;
					}
					hdr=(PINGHeader)msg.removeHeader(name);

				switch(hdr.type) 
				{

					case PINGHeader.GET_MBRS_REQ:   // return Rsp(local_addr, coord)
						if(!is_server) 
						{
							return;
						}
						lock(members) 
						{
							coord=members.Count > 0 ? (Address)members[0] : local_addr;
						}
						rsp_msg=new Message(msg.Source, null, null);
						rsp_hdr=new PINGHeader(PINGHeader.GET_MBRS_RSP, new PingRsp(local_addr, coord));
						rsp_msg.putHeader(name, rsp_hdr);
						if(Trace.trace) Trace.info("PING.up()", "received GET_MBRS_REQ from " + msg.Source+ ", returning " + rsp_hdr);
						passDown(new Event(Event.MSG, rsp_msg));
						return;

					case PINGHeader.GET_MBRS_RSP:   // add response to vector and notify waiting thread
						rsp=(PingRsp)hdr.arg;
						lock(initial_members) 
						{
							if(Trace.trace)
								Trace.info("PING.up()", "received FIND_INITAL_MBRS_RSP, rsp=" + rsp);
							initial_members.Add(rsp);
							Monitor.Pulse(initial_members);
						}
						return;

					default:
						if(Trace.trace)
							Trace.warn("PING.up()", "got PING header with unknown type (" + hdr.type + ")");
						return;
				}

				case Event.SET_LOCAL_ADDRESS:
					passUp(evt);
					local_addr=(Address)evt.Arg;
					break;

				default:
					passUp(evt);            // Pass up to the layer above us
					break;
			}
		}

		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt)
		{
			Message			msg;
			PINGHeader		hdr;
			long			time_to_wait, start_time;

			switch(evt.Type) 
			{
				case Event.FIND_INITIAL_MBRS:   // sent by GMS layer, pass up a GET_MBRS_OK event
					
					initial_members.Clear();

					// 1. Mcast GET_MBRS_REQ message
					if(Trace.trace) Trace.info("PING.down()", "FIND_INITIAL_MBRS");
					hdr=new PINGHeader(PINGHeader.GET_MBRS_REQ, null);
					msg=new Message(null, null, null);  // mcast msg
					msg.putHeader(name, hdr);
					passDown(new Event(Event.MSG, msg));


					// 2. Wait 'timeout' ms or until 'num_initial_members' have been retrieved
					lock(initial_members) 
					{
						start_time = System.Environment.TickCount;
						time_to_wait=timeout;

						while(initial_members.Count < num_initial_members && time_to_wait > 0) 
						{

							if(Trace.trace)
								Trace.info("PING.down()", "waiting for initial members: time_to_wait=" + time_to_wait +
								", got " + initial_members.Count + " rsps");

							Monitor.Wait(initial_members, (int)time_to_wait);
							time_to_wait -= System.Environment.TickCount - start_time;
						}
						
						if(Trace.trace)
							Trace.info("PING.down()", "No longer waiting for members as Initial Members = " + initial_members.Count.ToString()  + "|" + num_initial_members + "   TimeToWait = " + time_to_wait );
					}

					// 3. Send response
					if(Trace.trace)
						Trace.info("PING.down()", "initial mbrs are " + initialMembersString());
					passUp(new Event(Event.FIND_INITIAL_MBRS_OK, initial_members));
					break;
				//case Event.TMP_VIEW:
				case Event.VIEW_CHANGE:
					ArrayList tmp;
					if((tmp=((View)evt.Arg).getMembers()) != null) 
					{
						lock(members) 
						{
							members.Clear();
							for(int i=0; i < tmp.Count; i++)
								members.Add(tmp[i]);
						}
					}
					passDown(evt);
					break;

				case Event.BECOME_SERVER: // called after client has joined and is fully working group member
					passDown(evt);
					is_server=true;
					break;

				case Event.CONNECT:
					group_addr=(String)evt.Arg;
					passDown(evt);
					break;
				default:
					passDown(evt);          // Pass on to the layer below us
					break;
			}
		}
		
		/// <summary>
		/// Stops responding to requests 
		/// </summary>
		public override void stop()
		{
			is_server = false;
		}

		/// <summary>
		/// Returns string representation of initial members
		/// </summary>
		/// <returns>String representation of initial members</returns>
		private String initialMembersString()
		{
			if (initial_members.Count == 0)
				return "NO MEMBERS";

			String retValue = "";
			for(int i = 0; i<initial_members.Count;i++)
			{
				retValue = retValue + ((PingRsp)initial_members[i]).OwnAddress + ":";
			}
			retValue = retValue.Substring(0,retValue.Length-1);
			return retValue;

		}
	}
}

