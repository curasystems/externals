using System;
using System.Collections;
using System.Threading;

namespace GCT.Protocols
{
	/// <summary>
	/// Participant role of the GMS protocol.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class ParticipantGmsImpl : GmsImpl 
	{
		
		/// <summary>Indicates if the member is currently leaving </summary>
		public bool				leaving=false;
		/// <summary>List of suspected members</summary>
		ArrayList				suspected_mbrs=new ArrayList();
		/// <summary>Used to wait for leave response</summary>
		Object					leave_promise = new Object();
		/// <summary>If true, leave response was received</summary>
		bool					leaveRsp = false; // set to this to allow sync

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="g">GMS Protocol using this implementation</param>
		public ParticipantGmsImpl(GMS g) 
		{
			gms=g;
			name = "ParticipantGmsImpl";
			suspected_mbrs.Clear();
		}

		/// <summary>
		/// Not implemented by Participant GMS
		/// </summary>
		/// <param name="mbr"></param>
		public override void join(Address mbr) 
		{
			wrongMethod("join");
		}

    	/// <summary>
		/// Leaves the current group
		/// </summary>
		/// <param name="mbr">Local Address</param>
		public override void leave(Address mbr) 
		{
			Address		coord;
			int			max_tries=3;

			if(mbr.Equals(gms.local_addr))
				leaving=true;

			while((coord=gms.determineCoordinator()) != null && max_tries-- > 0) 
			{
				if(gms.local_addr.Equals(coord)) 
				{            
					// I'm the coordinator
					gms.becomeCoordinator();
					gms.getImpl().handleLeave(mbr, false);    // regular leave
					return;
				}
				if(Trace.trace)
					Trace.info("ParticipantGmsImpl.leave()", "sending LEAVE request to " + coord);
				sendLeaveMessage(coord, mbr);
				lock(leave_promise) 
				{
					Monitor.Wait(leave_promise,(int)gms.leave_timeout,true);
					if(leaveRsp != true)
						break;
				}
			}
			gms.becomeClient();
		}


		/// <summary>
		/// Not implemented by Participant GMS
		/// </summary>
		/// <param name="join_rsp"></param>
		public override void handleJoinResponse(JoinRsp join_rsp) 
		{
			wrongMethod("handleJoinResponse");
		}

		/// <summary>
		/// Response received to leave request
		/// </summary>
		public override void handleLeaveResponse() 
		{
			lock(leave_promise) 
			{
				leaveRsp = true;
				Monitor.Pulse(leave_promise);
			}
		}

		/// <summary>
		/// Stores the member as suspected. See handleSuspect()
		/// </summary>
		/// <param name="mbr">Suspected member</param>
		public override void suspect(Address mbr) 
		{
			handleSuspect(mbr);
		}

    	/// <summary>
		/// Removes previously suspected member from list of currently suspected members
		/// </summary>
		/// <param name="mbr">Previously suspected member</param>
		public override void unsuspect(Address mbr) 
		{
			if(mbr != null)
				suspected_mbrs.Remove(mbr);
		}

		/// <summary>
		/// Not implemented by Participant GMS
		/// </summary>
		/// <param name="mbr"></param>
		/// <returns></returns>
		public override JoinRsp handleJoin(Address mbr) 
		{
			wrongMethod("handleJoin");
			return null;
		}

		/// <summary>
		/// Not implemented by Participant GMS
		/// </summary>
		/// <param name="mbr"></param>
		/// <param name="suspected"></param>
		public override void handleLeave(Address mbr, bool suspected) 
		{
			wrongMethod("handleLeave");
		}

		/// <remarks>
		/// If we are leaving, we have to wait for the view change (last msg in the current view) that
		/// excludes us before we can leave.
		/// </remarks>
		/// <summary>
		/// Installs a new view
		/// </summary>
		/// <param name="new_view">New View to install</param>
		/// <param name="digest">Digest associated with the view</param>
		public override void handleViewChange(View new_view, Digest digest) 
		{
			ArrayList mbrs=new_view.getMembers();
			if(Trace.trace) 
				Trace.info("ParticipantGmsImpl.handleViewChange()", "view=" + new_view);
			suspected_mbrs.Clear();
			if(leaving && !mbrs.Contains(gms.local_addr)) 
			{ 
				// received a view in which I'm not member: ignore
				return;
			}
			gms.installView(new_view, digest);
		}

		/// <summary>
		/// Stores the suspect member. If the member is the coordinator and we
		/// would be the next coordinator then become the coordinator 
		/// </summary>
		/// <param name="mbr">Suspected member</param>
		public override void handleSuspect(Address mbr) 
		{
			ArrayList suspects=null;

			if(mbr == null) return;
			if(!suspected_mbrs.Contains(mbr))
				suspected_mbrs.Add(mbr);


			if(Trace.trace)
				Trace.info("ParticipantGmsImpl.handleSuspect()", "suspected mbr=" + mbr +
					", suspected_mbrs=" + suspected_mbrs);
	
			if(wouldIBeCoordinator()) 
			{
				if(Trace.trace)
					Trace.info("ParticipantGmsImpl.handleSuspect()", "suspected mbr=" + mbr + "), members are " + 
						gms.members + ", coord=" + gms.local_addr + ": I'm the new coord !");
	    
				suspects = (ArrayList)suspected_mbrs.Clone();
				suspected_mbrs.Clear();
				gms.becomeCoordinator();
				gms.castViewChange(null, null, suspects);
			}
		}


		/* ---------------------------------- Private Methods --------------------------------------- */

		/// <remarks>
		/// Determines whether this member is the new coordinator given a list of suspected members.  This is
		/// computed as follows: the list of currently suspected members (suspected_mbrs) is removed from the current
		/// membership. If the first member of the resulting list is equals to the local_addr, then it is true,
		/// otherwise false. Example: own address is B, current membership is {A, B, C, D}, suspected members are 
		/// {A,D}. The resulting list is {B, C}. The first member of {B, C} is B, which is equal to the
		/// local_addr. Therefore, true is returned.
		/// </remarks>
		/// <summary>
		/// Returns true if this process should become the coordinator
		/// </summary>
		/// <returns>True if this process should become the coordinator</returns>
		private bool wouldIBeCoordinator() 
		{
			Address new_coord=null;
			ArrayList  mbrs=gms.members.getMembers(); // getMembers() returns a *copy* of the membership vector
	
			for(int i=0; i < suspected_mbrs.Count; i++)
				mbrs.Remove(suspected_mbrs[i]);
	
			if(mbrs.Count < 1) return false;
			new_coord=(Address)mbrs[0];
			return gms.local_addr.Equals(new_coord);
		}
		
		/// <summary>
		/// Sends a leave request to the coordinator
		/// </summary>
		/// <param name="coord">Address of the coordinator</param>
		/// <param name="mbr">Local Address</param>
		private void sendLeaveMessage(Address coord, Address mbr) 
		{
			Message			msg=new Message(coord, null, null);
			GmsHeader		hdr=new GmsHeader(GmsHeader.LEAVE_REQ, mbr);

			msg.putHeader(gms.getName(), hdr);
			gms.passDown(new Event(Event.MSG, msg));
		}

		/* ------------------------------ End of Private Methods ------------------------------------ */
    
	}
}
