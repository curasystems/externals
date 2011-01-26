using System;
using System.Collections;
using System.Threading;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <remarks>
	/// Accepts JOIN and LEAVE requests and emits view changes accordingly.
	/// </remarks>
	/// <summary>
	/// Coordinator role of the GMS protocol.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class CoordGmsImpl : GmsImpl 
	{
		/// <summary>
		/// Indicates if the member is currently leaving
		/// </summary>
		public bool leaving = false;


		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="g">GMS Protocol using this implementation</param>
		public CoordGmsImpl(GMS g) 
		{
			gms = g;
			name = "CoordGmsImpl";
		}

		/// <summary>
		/// Not implemented by Coord GMS
		/// </summary>
		/// <param name="mbr"></param>
		public override void join(Address mbr) 
		{
			wrongMethod("join");
		}

		/// <remarks>
		/// This method will can <c>handleLeave()</c> as if it was received by participant member.
		/// </remarks>
		/// <summary>
		/// Leave current group. Another member will assume the coordinator role
		/// </summary>
		/// <param name="mbr">Local Address</param>
		public override void leave(Address mbr) 
		{
			if (mbr == null) 
			{
				if(Trace.trace)
					Trace.error("CoordGmsImpl.leave()", "member's address is null !");
				return;
			}

			if (mbr.Equals(gms.local_addr))
				leaving = true;

			handleLeave(mbr, false); // regular leave
		}

		/// <summary>
		/// Not implemented by Coord GMS
		/// </summary>
		/// <param name="join_rsp"></param>
		public override void handleJoinResponse(JoinRsp join_rsp) 
		{
			wrongMethod("handleJoinResponse");
		}

		/// <summary>
		/// Not implemented by Coord GMS
		/// </summary>
		public override void handleLeaveResponse() 
		{
			wrongMethod("handleLeaveResponse");
		}

		/// <summary>
		/// Forces the suspected member to leave the group.
		/// </summary>
		/// <param name="mbr">Member to suspect</param>
		public override void suspect(Address mbr) 
		{
			handleSuspect(mbr);
		}

		/// <summary>
		/// No action is taken to unsuspect a member
		/// </summary>
		/// <param name="mbr">Member to unsuspect</param>
		public override void unsuspect(Address mbr) 
		{
		}

		/// <summary>
		/// Adds members to the Membership and multicast the new view.
		/// </summary>
		/// <param name="mbr">New Member requesting join</param>
		/// <returns><c>JoinRsp</c> containing new view and digest</returns>
		public override JoinRsp handleJoin(Address mbr) 
		{
			lock(this)
			{
				ArrayList new_mbrs = new ArrayList();
				View v = null;
				Digest d, tmp;

				if (Trace.trace)
					Trace.info("CoordGmsImpl.handleJoin()", "mbr=" + mbr);

				if (gms.local_addr.Equals(mbr)) 
				{
					if(Trace.trace)
						Trace.error("CoordGmsImpl.handleJoin()", "cannot join myself !");
					return null;
				}

				if (gms.members.contains(mbr)) 
				{
					if (Trace.trace)
						Trace.error("CoordGmsImpl.handleJoin()","member "+ mbr+ " already present; returning existing view "+ gms.members.getMembers());
					return new JoinRsp(new View(gms.view_id, gms.members.getMembers()),gms.getDigest());
					// already joined: return current digest and membership
				}
				new_mbrs.Add(mbr);
				tmp = gms.getDigest(); // get existing digest
				if (Trace.trace)
					Trace.info("CoordGmsImpl.handleJoin()", "got digest=" + tmp);

				d = new Digest(tmp.size() + 1);
				// create a new digest, which contains 1 more member
				d.add(tmp); // add the existing digest to the new one
				d.add(mbr, 0, 0);
				// ... and add the new member. it's first seqno will be 1
				v = gms.getNextView(new_mbrs, null, null);
				if (Trace.trace)
					Trace.info("CoordGmsImpl.handleJoin()","joined member " + mbr + ", view is " + v.getMembers());
				return new JoinRsp(v, d);
			}
		}

		/// <remarks>
		///   Exclude <code>mbr</code> from the membership. If <code>suspected</code> is true, then
		///   this member crashed and therefore is forced to leave, otherwise it is leaving voluntarily.
		/// </remarks>
		/// <summary>
		/// Excludes the member from the group
		/// </summary>
		/// <param name="mbr">The member that will be removed.</param>
		/// <param name="suspected">If true the member is being forced to leave</param>
		public override void handleLeave(Address mbr, bool suspected) 
		{
			lock(this)
			{
				ArrayList v = new ArrayList();
				// contains either leaving mbrs or suspected mbrs

				if (Trace.trace)
					Trace.info("CoordGmsImpl.handleLeave()", "mbr=" + mbr);
				if (!gms.members.contains(mbr)) 
				{
					if (Trace.trace)
						Trace.error(
							"CoordGmsImpl.handleLeave()",
							"mbr " + mbr + " is not a member !");
					return;
				}
				v.Add(mbr);
				if (suspected)
					gms.castViewChange(null, null, v);
				else
					gms.castViewChange(null, v, null);
			}
		}

		/// <summary>
		/// Installs a new view within the process
		/// </summary>
		/// <param name="new_view">The new view to be installed</param>
		/// <param name="digest">The digest associated with the new view</param>
		public override void handleViewChange(View new_view, Digest digest) 
		{
			ArrayList mbrs = new_view.getMembers();
			if (Trace.trace) 
			{
				if (digest != null)
					Trace.info(
						"CoordGmsImpl.handleViewChange()",
						"view=" + new_view + ", digest=" + digest);
				else
					Trace.info(
						"CoordGmsImpl.handleViewChange()",
						"view=" + new_view);
			}
			if (leaving && !mbrs.Contains(gms.local_addr)) 
			{
				return;
			}
			gms.installView(new_view, digest);
		}

		/// <summary>
		/// Forces the suspected member to leave the group.
		/// </summary>
		/// <param name="mbr">Suspected member</param>
		public override void handleSuspect(Address mbr) 
		{
			if (mbr.Equals(gms.local_addr)) 
			{
				Trace.warn(
					"CoordGmsImpl.handleSuspect()",
					"I am the coord and I'm being am suspected -- will probably leave shortly");
				return;
			}
			handleLeave(mbr, true); // irregular leave - forced
		}
	
	}
}
