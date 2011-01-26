using System;
using System.Collections;
using System.Threading;

using GCT.Util;

namespace GCT.Protocols
{
	/// <remarks>
	/// Whenever a new member wants to join a group, it starts in the CLIENT role.
	/// No multicasts to the group will be received and processed until the member has been joined and
	/// turned into a SERVER (either coordinator or participant, mostly just participant). This class
	/// only implements <code>Join</code> (called by clients who want to join a certain group, and
	/// <code>ViewChange</code> which is called by the coordinator that was contacted by this client, to
	/// tell the client what its initial membership is.</remarks>
	/// <summary>
	/// Client role of the GMS protocol.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class ClientGmsImpl : GmsImpl 
	{
		/// <summary>Holds the initial members responses</summary>
		public ArrayList		initial_mbrs=new ArrayList();
		/// <summary>Synchronises leave requests and resposnes</summary>
		Promise					join_promise=new Promise();

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="g">GMS Protocol using this implementation</param>
		public ClientGmsImpl(GMS g) 
		{
			gms=g;
			name = "ClientGmsImpl";
		}

		/// <remarks>
		/// Determines the coordinator and sends a unicast 
		/// handleJoin() message to it. The coordinator returns a JoinRsp and then broadcasts the new view, which
		/// contains a message digest and the current membership (including the joiner). The joiner is then
		/// supposed to install the new view and the digest and starts accepting mcast messages. Previous
		/// mcast messages were discarded as we were not a member.<p>
		/// If successful, impl is changed to an instance of ParticipantGmsImpl. 
		/// Otherwise, we continue trying to send join() messages to the coordinator, 
		/// until we succeed (or there is no member in the group. In this case, we create our own singleton group).
		/// <p>When GMS.disable_initial_coord is set to true, then we won't become coordinator on receiving an initial
		/// membership of 0, but instead will retry (forever) until we get an initial membership of > 0.</p>
		/// </p>
		/// </remarks>
		/// <summary>
		/// Joins this process to a group.
		/// </summary>
		/// <param name="mbr">The local Address of the process</param>
		public override void join(Address mbr) 
		{
			Address		coord=null;
			Digest		tmp_digest=null;
			JoinRsp		rsp=null;
	
			while(true) 
			{
				findInitialMembers();
				if(Trace.trace)
					Trace.info("ClientGmsImpl.join()", "initial_mbrs are " + initial_mbrs);
				if(initial_mbrs.Count == 0) 
				{
					if(gms.disable_initial_coord) 
					{
						if(Trace.trace)
							Trace.info("ClientGmsImpl.join()", "received an initial membership of 0, but " +
								"cannot become coordinator (disable_initial_coord=" + gms.disable_initial_coord +
								"), will retry fetching the initial membership");
						continue;
					}
					if(Trace.trace)
						Trace.info("ClientGmsImpl.join()", "no initial members discovered: " +
							"creating group as first member");
					becomeSingletonMember(mbr);
					return;
				}
		
				coord=determineCoord(initial_mbrs);
				if(coord == null) 
				{
					if(Trace.trace)
						Trace.error("ClientGmsImpl.join()", "could not determine coordinator " +
						"from responses " + initial_mbrs);
					continue;
				}

				try 
				{
					if(Trace.trace)
						Trace.info("ClientGmsImpl.join()", "sending handleJoin(" + mbr + ") to " + coord);
					sendJoinMessage(coord, mbr);
					rsp=(JoinRsp)join_promise.getResult(gms.join_timeout);
	
					if(rsp == null) 
					{
						if(Trace.trace)
							Trace.warn("ClientGmsImpl.join()", "handleJoin(" + mbr + ") failed, retrying");
					}
					else 
					{
						// 1. Install digest
						tmp_digest=rsp.getDigest();

						if(tmp_digest != null) 
						{
							tmp_digest.incrementHighSeqno(coord); 
							if(Trace.trace) Trace.info("ClientGmsImpl.join()", "digest is " + tmp_digest);
							gms.setDigest(tmp_digest);
						}
						else
							if(Trace.trace)
							Trace.error("ClientGmsImpl.join()", "digest of JOIN response is null");

						// 2. Install view
						if(Trace.trace)
							Trace.info("ClientGmsImpl.join()", "[" + gms.local_addr +
								"]: JoinRsp=" + rsp.getView() + " [size=" + rsp.getView().size() + "]\n\n");

						if(rsp.getView() != null) 
						{
							if(!installView(rsp.getView())) 
							{
								if(Trace.trace)
									Trace.error("ClientGmsImpl.join()", "view installation failed, " +
										"retrying to join group");
								continue;
							}
							gms.passUp(new Event(Event.BECOME_SERVER));
							gms.passDown(new Event(Event.BECOME_SERVER));
							return;
						}
						else
							if(Trace.trace)
							Trace.error("ClientGmsImpl.join()", "view of JOIN response is null");
					}
				}
				catch(Exception e) 
				{
					Trace.info("ClientGmsImpl.join()", "exception=" + e.ToString() + ", retrying");
				}

				Thread.Sleep((int)gms.join_retry_timeout);
			}  // end while
		}

		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		/// <param name="mbr"></param>
		public override void leave(Address mbr) 
		{
			wrongMethod("leave");
		}


		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		/// <param name="join_rsp"></param>
		public override void handleJoinResponse(JoinRsp join_rsp) 
		{
			join_promise.setResult(join_rsp); // will wake up join() method
		}

		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		public override void handleLeaveResponse() 
		{
			wrongMethod("handleLeaveResponse");
		}

		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		/// <param name="mbr"></param>
		public override void suspect(Address mbr) 
		{
			wrongMethod("suspect");
		}

		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		/// <param name="mbr"></param>
		public override void unsuspect(Address mbr) 
		{
			wrongMethod("unsuspect");
		}

		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		/// <param name="mbr"></param>
		public override JoinRsp handleJoin(Address mbr) 
		{
			wrongMethod("handleJoin");
			return null;
		}

		/// <summary>
		/// Not implemented by Client GMS
		/// </summary>
		/// <param name="mbr"></param>
		/// <param name="suspected"></param>
		public override void handleLeave(Address mbr, bool suspected) 
		{
			wrongMethod("handleLeave");
		}

		/// <summary>
		/// All views are ignored while GMS is a client
		/// </summary>
		/// <param name="new_view">Ignored</param>
		/// <param name="digest">Ignored</param>
		public override void handleViewChange(View new_view, Digest digest) 
		{
				if(Trace.trace)
					Trace.info("ClientGmsImpl.handleViewChange()", "view " + new_view.getMembers() + 
						" is discarded as we are not a participant");
		}
    
		/// <summary>
		/// All views are ignored while GMS is a client
		/// </summary>
		/// <param name="mbr"></param>
		public override void handleSuspect(Address mbr) 
		{
			wrongMethod("handleSuspect");
			return;
		}

		/// <summary>
		/// Called by the GMS if it can't process the Event.
		/// </summary>
		/// <param name="evt">The Event passed up to the GMS</param>
		public override bool handleUpEvent(Event evt) 
		{
			ArrayList tmp;

			switch(evt.Type) 
			{

				case Event.FIND_INITIAL_MBRS_OK:
					tmp=(ArrayList)evt.Arg;
					lock(initial_mbrs) 
					{
						if(tmp != null && tmp.Count > 0)
							for(int i=0; i < tmp.Count; i++)
								initial_mbrs.Add(tmp[i]);
						Monitor.Pulse(initial_mbrs);
					}
					return false;  // don't pass up the stack
			}
			return true;
		}

		/* --------------------------- Private Methods ------------------------------------ */


		/// <remarks>
		/// A value of false will be returned if the new view doesn't contain the 
		/// this member.
		/// </remarks>
		/// <summary>
		/// Installs the new view returned from GMS Coord.
		/// </summary>
		/// <param name="new_view">The new <code>View</code> to install</param>
		/// <returns>True if successful, otherwise false.</returns>
		private bool installView(View new_view) 
		{
			ArrayList mems=new_view.getMembers();
			if(Trace.trace) Trace.info("ClientGmsImpl.installView()", "new_view=" + new_view);
			if(gms.local_addr == null || mems == null || !mems.Contains(gms.local_addr)) 
			{
				Trace.error("ClientGmsImpl.installView()", "I (" + gms.local_addr + 
					") am not member of " + mems + ", will not install view");
				return false;
			}
			gms.installView(new_view);
			gms.becomeParticipant();
			gms.passUp(new Event(Event.BECOME_SERVER));
			gms.passDown(new Event(Event.BECOME_SERVER));
			return true;
		}
    
		/// <summary>
		/// Sends a join message to the coordinator of the group.
		/// </summary>
		/// <param name="coord">The Address of the Coordinator</param>
		/// <param name="mbr">The local Address (i.e. this member)</param>
		private void sendJoinMessage(Address coord, Address mbr) 
		{
			Message			msg;
			GmsHeader		hdr;

			msg=new Message(coord, null, null);
			hdr=new GmsHeader(GmsHeader.JOIN_REQ, mbr);
			msg.putHeader(gms.getName(), hdr);
			gms.passDown(new Event(Event.MSG, msg));
		}

		/// <remarks>
		/// The PING layer will intercept this Event and return the initial members of the group.
		/// </remarks>
		/// <summary>
		/// Sends Event down stack requesting initial members. Causes private list <c>initial_mbrs</c> to be populated.
		/// </summary>
		private void findInitialMembers() 
		{
			PingRsp ping_rsp;

			lock(initial_mbrs) 
			{
				initial_mbrs.Clear();
				gms.passDown(new Event(Event.FIND_INITIAL_MBRS));
				try 
				{
					Monitor.Wait(initial_mbrs,100000,true);
				}
				catch(Exception e) {}

				for(int i=0; i < initial_mbrs.Count; i++) 
				{
					ping_rsp=(PingRsp)initial_mbrs[i];
					if(ping_rsp.own_addr != null && gms.local_addr != null && 
						ping_rsp.own_addr.Equals(gms.local_addr)) 
					{
						initial_mbrs.RemoveAt(i);
						break;
					}
				}
			}
		}

		/// <summary>
		/// Determines the most likely coordinator from the responses
		/// </summary>
		/// <param name="mbrs">The list of suggested coordinators</param>
		/// <returns>The coordinator with the most votes</returns>
		private Address determineCoord(ArrayList mbrs) 
		{
			PingRsp   mbr;
			Hashtable votes;
			int       count, most_votes;
			Address   winner=null;

			if(mbrs == null || mbrs.Count < 1)
				return null;

			votes=new Hashtable();

			for(int i=0; i < mbrs.Count; i++) 
			{
				mbr=(PingRsp)mbrs[i];
				if(mbr.coord_addr != null) 
				{
					if(!votes.ContainsKey(mbr.coord_addr))
						votes.Add(mbr.coord_addr,(int)1);
					else 
					{
						count=((int)votes[mbr.coord_addr]);
						votes[mbr.coord_addr] = (int)count+1;
					}
				}
			}

			if(Trace.trace) 
			{
				if(votes.Count > 1)
					Trace.warn("ClientGmsImpl.determineCoord()",
						"there was more than 1 candidate for coordinator: " + votes);
				else
					Trace.info("ClientGmsImpl.determineCoord()", "election results: " + votes);
			}
	

			// determine who got the most votes
			most_votes=0;
			foreach(Address addr in votes.Keys)
			{
				count=((int)votes[addr]);
				if(count > most_votes) 
				{
					winner=addr;
					most_votes++;
				}
			}
			votes.Clear();
			return winner;
		}


		/// <remarks>
		/// Called by <c>Join()</c> when no initial members can be detected
		/// </remarks>
		/// <summary>
		/// Changes the GMS to a Coord implementation
		/// </summary>
		/// <param name="mbr">Local Address representing this Channel</param>
		private void becomeSingletonMember(Address mbr) 
		{
			Digest		initial_digest;
			ViewId		view_id=null;
			ArrayList   mbrs=new ArrayList();

			// set the initial digest (since I'm the first member)
			initial_digest = new Digest(1);             // 1 member (it's only me)
			initial_digest.add(gms.local_addr, 0, 0); // initial seqno mcast by me will be 1 (highest seen +1)	
			gms.setDigest(initial_digest);

			view_id=new ViewId(mbr);       // create singleton view with mbr as only member
			mbrs.Add(mbr);
			gms.installView(new View(view_id, mbrs));
			gms.becomeCoordinator();
	
			gms.passUp(new Event(Event.BECOME_SERVER));
			gms.passDown(new Event(Event.BECOME_SERVER));
			if(Trace.trace) Trace.info("ClientGmsImpl.becomeSingletonMember()", 
								"created group (first member). My view is " + gms.view_id + 
								", impl is " + gms.getImpl().getName());
		}
	}
}
