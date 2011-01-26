using System;
using System.Collections;
using System.Data;
using System.Threading;

using GCT.Util;
using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// Handles joins/leaves/crashes (suspicions) and emits new views accordingly.
	/// </remarks>
	/// <summary>
	/// Protocol: Group Membership System (GMS) maintains the members in a group.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class GMS : Protocol 
	{
		/// <summary>Role of the GMS at present</summary>
		private GmsImpl             impl=null;
		/// <summary>Local Address</summary>
		public  Address             local_addr=null;
		/// <summary>Group multicast Address</summary>
		public  String              group_addr=null;
		/// <summary>Membership of group</summary>
		public  Membership          members=new Membership();     // real membership
		/// <summary>Used for computing new view</summary>
		public  Membership          tmp_members=new Membership(); // base for computing next view
		/// <summary>Members joined but for which no view has been yet</summary>
		public  ArrayList           joining=new ArrayList();         // members joined but for which no view has been yet
		/// <summary>ViewID for current view</summary>
		public  ViewId              view_id=null;
		/// <summary>Lamport logical time</summary>
		public  long                ltime=0;
		/// <summary>Interval to wait for repsonse to Join</summary>
		public  long                join_timeout=5000;
		/// <summary>Interval to wait before retrying a failed Join</summary>
		public  long                join_retry_timeout=2000;
		/// <summary>Interval to wait for response to leave</summary>
		public  long                leave_timeout=5000;
		/// <summary>Interval to wait for a response to digest request</summary>
		public  long                digest_timeout=5000;        // time to wait for a digest (from PBCAST). should be fast
		/// <summary>Synchronizes event entry into impl</summary>
		public  Object              impl_mutex=new Object(); 
		/// <summary>synchronizes the GET_DIGEST/GET_DIGEST_OK events</summary>
		private Object              digest_mutex=new Object(); 
		/// <summary>Holds result of last computed digest</summary>
		private Digest              digest=null;                // holds result of GET_DIGEST event
		/// <summary>Collection of Implentations</summary>
		private Hashtable           impls=new Hashtable();
		/// <summary>If true then the GMS will leave if not in a new View</summary>
		private bool				shun=false;
		/// <summary>Displays the local address when retreived</summary>
		private bool				print_local_addr=true;
		/// <summary>Forces the a join to run continuously until a coodinator is found</summary>
		public bool					disable_initial_coord=false;
		/// <summary>Client implementation string</summary>
		const String                CLIENT="Client";
		/// <summary>Coordinator implementation string</summary>
		const String                COORD="Coordinator";
		/// <summary>Participant implementation string</summary>
		const String                PART="Participant";
		/// <summary>Used to run tasks</summary>
		TimeScheduler               timer=null;
		
		/// <summary>
		/// Constructor.
		/// </summary>
		public GMS() 
		{
			name = "GMS";
			initState();
		}
		/// <summary>
		/// Returns unique <c>Protocol</c> name
		/// </summary>
		/// <returns>Unique <c>Protocol</c> name</returns>
		public String   getName()          {return name;}

		/// <summary>
		/// Required services that must be below this <c>Protocol</c>
		/// </summary>
		/// <returns>Required services that must be below this <c>Protocol</c></returns>
		public override ArrayList  requiredDownServices() 
		{
			ArrayList retval=new ArrayList();
			retval.Add((int)Event.GET_DIGEST);
			retval.Add((int)Event.SET_DIGEST);
			retval.Add((int)Event.FIND_INITIAL_MBRS);
			return retval;
		}

		/// <summary>
		/// Sets the current GMS implementation
		/// </summary>
		/// <param name="new_impl">New GMS implementation</param>
		public void setImpl(GmsImpl new_impl) 
		{
			lock(impl_mutex) 
			{
				impl=new_impl;
				if(Trace.trace) Trace.info("GMS.setImpl()", "changed role to " + new_impl.getName());
			}
		}

		/// <summary>
		/// Gets the current GMS implementation
		/// </summary>
		/// <returns>GMS implementation</returns>
		public GmsImpl getImpl() {return impl;}


		/// <summary>
		/// Sets the TimeScheduler to the instance in the ProtocolSinkStack and
		/// initialises the GMS implementation if possible.
		/// </summary>
		public override void init() 
		{
			timer=stack != null? stack.timer : null;
			if(timer == null)
				throw new Exception("GMS.init(): timer is null");
			if(impl != null)
				impl.init();
		}

		/// <summary>
		/// Starts the GMS implementation.
		/// </summary>
		public override void start()  
		{
			if(impl != null) impl.start();
		}

		/// <summary>
		/// Stops the GMS implementation.
		/// </summary>
		public override void stop() 
		{
			if(impl != null) impl.stop();
		}

		/// <summary>
		/// Sets the GMS implementation to a coordinator
		/// </summary>
		public void becomeCoordinator() 
		{
			CoordGmsImpl tmp=(CoordGmsImpl)impls[COORD];

			if(tmp == null) 
			{
				tmp=new CoordGmsImpl(this);
				impls.Add(COORD, tmp);
			}
			tmp.leaving=false;
			setImpl(tmp);
			if(Trace.trace) Trace.info("GMS.becomeCoordinator()", local_addr + " became coordinator");
		}


		/// <summary>
		/// Sets the GMS implementation to a participant
		/// </summary>
		public void becomeParticipant() 
		{
			ParticipantGmsImpl tmp=(ParticipantGmsImpl)impls[PART];

			if(tmp == null) 
			{
				tmp=new ParticipantGmsImpl(this);
				impls.Add(PART, tmp);
			}
			tmp.leaving=false;
			setImpl(tmp);
			if(Trace.trace) Trace.info("GMS.becomeParticipant()", local_addr + " became participant");
		}

		/// <summary>
		/// Sets the GMS implementation to a client
		/// </summary>
		public void becomeClient() 
		{
			ClientGmsImpl tmp=(ClientGmsImpl)impls[CLIENT];

			if(tmp == null) 
			{
				tmp=new ClientGmsImpl(this);
				impls.Add(CLIENT, tmp);
			}
			tmp.initial_mbrs.Clear();
			setImpl(tmp);
			if(Trace.trace) Trace.info("GMS.becomeClient", local_addr + " became client");
		}

		/// <summary>
		/// Checks if GMS implementation is a coordinator
		/// </summary>
		/// <returns></returns>
		bool haveCoordinatorRole() 
		{
			return impl != null && impl is CoordGmsImpl;
		}

		/// <summary>
		/// Computes the next view, removes old and suspected members, adds new members.
		/// </summary>
		/// <param name="new_mbrs">New members to be added to the view</param>
		/// <param name="old_mbrs">Old members to be removed from the view</param>
		/// <param name="suspected_mbrs">Suspected members to be removed from the view</param>
		/// <returns>The new View</returns>
		public View getNextView(ArrayList new_mbrs, ArrayList old_mbrs, ArrayList suspected_mbrs) 
		{
			ArrayList      mbrs;
			long        vid=0;
			View        v;
			Membership  tmp_mbrs=null;
			Address     tmp_mbr;

			lock(members) 
			{
				if(view_id == null) return null; // this should *never* happen !
				vid=Math.Max(view_id.getId(), ltime) + 1;
				ltime=vid;

				if(Trace.trace)
					Trace.info("GMS.getNextView()", "VID=" + vid + ", current members=" + 
						printMembers(members.getMembers()) + 
						", new_mbrs=" + printMembers(new_mbrs) +
						", old_mbrs=" + printMembers(old_mbrs) + ", suspected_mbrs=" +
						printMembers(suspected_mbrs));

				tmp_mbrs=tmp_members.copy();  // always operate on the temporary membership
				tmp_mbrs.remove(suspected_mbrs);
				tmp_mbrs.remove(old_mbrs);
				tmp_mbrs.add(new_mbrs);
				mbrs=(ArrayList)tmp_mbrs.getMembers();
				v=new View(local_addr, vid, mbrs);

				// Update membership
				tmp_members.set(mbrs);

				// Update joining list
				if(new_mbrs != null) 
				{
					for(int i=0; i < new_mbrs.Count; i++) 
					{
						tmp_mbr=(Address)new_mbrs[i];
						if(!joining.Contains(tmp_mbr))
							joining.Add(tmp_mbr);
					}
				}

				if(Trace.trace)
					Trace.info("GMS.getNextView()", "new view is " + v);
				return v;
			}
		}

		/// <remarks>
		/// Compute a new view, given the current view, the new members and the suspected/left
		///  members. Then simply mcast the view to all members. This is different to the VS GMS protocol,
		/// in which we run a FLUSH protocol which tries to achive consensus on the set of messages mcast in
		/// the current view before proceeding to install the next view.
		/// The members for the new view are computed as follows:
		///				existing          leaving        suspected          joining
		///
		/// 1. new_view      y                 n               n                 y
		/// 2. tmp_view      y                 y               n                 y
		/// (view_dest)
		/// <p>
		/// The new view to be installed includes the existing members plus the joining ones and
		/// excludes the leaving and suspected members.
		/// </p>
		///	A temporary view is sent down the stack as an <em>event</em>. This allows the bottom layer
		/// (e.g. UDP or TCP) to determine the members to which to send a multicast message. Compared
		/// to the new view, leaving members are <em>included</em> since they have are waiting for a
		/// view in which they are not members any longer before they leave. So, if we did not set a
		/// temporary view, joining members would not receive the view (signalling that they have been
		/// joined successfully). The temporary view is essentially the current view plus the joining
		/// members (old members are still part of the current view).
		/// </remarks>
		/// <summary>
		/// Calcualates and multicasts a new view.
		/// </summary>
		/// <param name="new_mbrs">New members to be added to the view</param>
		/// <param name="old_mbrs">Old members to be removed from the view</param>
		/// <param name="suspected_mbrs">Suspected members to be removed from the view</param>
		/// <returns>The new View</returns>
		public View castViewChange(ArrayList new_mbrs, ArrayList old_mbrs, ArrayList suspected_mbrs) 
		{
			View new_view;
			// next view: current mbrs + new_mbrs - old_mbrs - suspected_mbrs
			new_view=getNextView(new_mbrs, old_mbrs, suspected_mbrs);
			castViewChange(new_view);
			return new_view;
		}

		/// <summary>
		/// Calls <c>castViewChange(View,Digest), with a null Digest</c>
		/// </summary>
		/// <param name="new_view">New View to multicast</param>
		public void castViewChange(View new_view) 
		{
			castViewChange(new_view, null);
		}

		/// <summary>
		/// Multicasts a new view.
		/// </summary>
		/// <param name="new_view">New View to send</param>
		/// <param name="digest">Digest associated with View</param>
		public void castViewChange(View new_view, Digest digest) 
		{
			Message   view_change_msg;
			GmsHeader hdr;

			if(Trace.trace)
				Trace.info("GMS.castViewChange()", "mcasting view {" + new_view + "} (" + new_view.size() + " mbrs)\n");
			view_change_msg=new Message(null,null,null); // bcast to all members
			hdr=new GmsHeader(GmsHeader.VIEW, new_view);
			hdr.digest=digest;
			view_change_msg.putHeader(getName(), hdr);
			passDown(new Event(Event.MSG, view_change_msg));
		}

		/// <summary>
		/// Sets the new view and sends a VIEW_CHANGE event up and down the stack.
		/// </summary>
		/// <param name="new_view">New View to install</param>
		/// <param name="digest">Digest associated with View</param>
		public void installView(View new_view, Digest digest) 
		{
			if(digest != null)
				mergeDigest(digest);
			installView(new_view);
		}

		/// <summary>
		/// Sets the new view and sends a VIEW_CHANGE event up and down the stack.
		/// </summary>
		/// <param name="new_view">New View to install</param>
		public void installView(View new_view) 
		{
			Address coord;
			int     rc;
			ViewId  vid=new_view.getVid();
			ArrayList  mbrs=new_view.getMembers();
	
			lock(members) 
			{                  // serialize access to views
				ltime=Math.Max(vid.getId(), ltime);  // compute Lamport logical time

				/* Check for self-inclusion: if I'm not part of the new membership, I just discard it.
					   This ensures that messages sent in view V1 are only received by members of V1 */
				if(Trace.trace)
					Trace.info("GMS.installView()","View to install contains: " + new_view.ToString());
				if(checkSelfInclusion(mbrs) == false) 
				{
					if(Trace.trace)
						Trace.warn("GMS.installView()", 
							"checkSelfInclusion() failed, not a member of view " + mbrs + "; discarding view");
					if(shun) 
					{
						if(Trace.trace)
							Trace.warn("GMS.installView()", "I'm being shunned, will leave and rejoin group");
						passUp(new Event(Event.EXIT));
					}
					return;
				}



				// Discards view with id lower than our own. Will be installed without check if first view	    
				if(view_id != null) 
				{
					rc = vid.CompareTo(view_id);
					if(rc <= 0) 
					{
						if(Trace.trace)
							Trace.error("GMS.installView()", "received view <= current view;" +
								" discarding it ! (current vid: " + view_id + ", new vid: " + vid +")");
						return;
					}
				}

				if(Trace.trace) Trace.info("GMS.installView()", "view is " + new_view);

				// assign new_view to view_id
				view_id=vid.Copy();
	    
				// Set the membership. Take into account joining members
				if(mbrs != null && mbrs.Count > 0) 
				{
					members.set(mbrs);
					tmp_members.set(members);
					foreach(Object obj in mbrs)
					{
						joining.Remove(obj); // remove all members in mbrs from joining
					}
					tmp_members.add(joining); // adjust temporary membership
				}

				// Send VIEW_CHANGE event up and down the stack:
				Event view_event=new Event(Event.VIEW_CHANGE, (View)new_view.copy());
				passDown(view_event); // needed e.g. by failure detector or UDP
				passUp(view_event);

				coord=determineCoordinator();
				if(coord != null && coord.Equals(local_addr) && !(coord.Equals(vid.getCoordAddress()))) 
				{
					becomeCoordinator();
				}
				else 
				{
					if(haveCoordinatorRole() && !local_addr.Equals(coord))
						becomeParticipant();
				}
			}
		}

		/// <summary>
		/// Returns the coordinator of the group (i.e. the first memeber).
		/// </summary>
		/// <returns>The coordinator of the group</returns>
		public Address determineCoordinator() 
		{
			lock(members) 
			{
				return members != null && members.size() > 0 ? (Address)members.elementAt(0) : null;
			}
		}

		/// <summary>
		/// Checks whether the potential coordinator would be the new coordinator
		/// </summary>
		/// <param name="potential_new_coord">Member to check</param>
		/// <returns>True if member is second in line</returns>
		protected bool wouldBeNewCoordinator(Address potential_new_coord) 
		{
			Address new_coord=null;

			if(potential_new_coord == null) return false;

			lock(members) 
			{
				if(members.size() < 2) return false;
				new_coord=(Address)members.elementAt(1);  // member at 2nd place
				if(new_coord != null && new_coord.Equals(potential_new_coord))
					return true;
				return false;
			}
		}

		/// <summary>
		/// Checks whether this member is a member of the group.
		/// </summary>
		/// <param name="mbrs">List of group members</param>
		/// <returns>True if member is in the group, otherwise false</returns>
		protected bool checkSelfInclusion(ArrayList mbrs) 
		{
			Object mbr;
			if(mbrs == null)
				return false;
			for(int i=0; i < mbrs.Count; i++) 
			{
				mbr=mbrs[i];
				if(mbr != null && local_addr.Equals(mbr))
					return true;
			}
			return false;
		}

		/// <summary>
		/// Send down a SET_DIGEST event 
		/// </summary>
		/// <param name="d">Digest to install</param>
		public void setDigest(Digest d) 
		{
			passDown(new Event(Event.SET_DIGEST, d));
		}

		/// <summary>
		/// Send down a MERGE_DIGEST event
		/// </summary>
		/// <param name="d">Digest to merge</param>
		public void mergeDigest(Digest d) 
		{
			passDown(new Event(Event.MERGE_DIGEST, d));
		}

		/// <summary>
		/// Send down a GET_DIGEST event, and wait for a response (GET_DIGEST_OK)
		/// </summary>
		/// <returns></returns>
		public Digest getDigest() 
		{
			Digest ret=null;

			lock(digest_mutex) 
			{
				digest=null;
				passDown(new Event(Event.GET_DIGEST));
				try 
				{
					Monitor.Wait(digest_mutex, (int)digest_timeout, true);
				} 
				catch(Exception ex) {}
				if(digest != null) 
				{
					ret=digest;
					digest=null;
					return ret;
				}
				else 
				{
					if(Trace.trace)
						Trace.error("GMS.getDigest()", "digest could not be fetched from PBCAST layer");
					return null;
				}
			}
		}

		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			Object        obj;
			Message       msg;
			GmsHeader     hdr;

			switch(evt.Type) 
			{

				case Event.MSG:
					msg=(Message)evt.Arg;
					obj=msg.getHeader(getName());
					if(obj == null || !(obj is GmsHeader))
						break;
					hdr=(GmsHeader)msg.removeHeader(getName());
					if(Trace.trace)
						Trace.info("GMS.up()", "Receieved Event " + GmsHeader.type2String(hdr.type));
					if(hdr.type == GmsHeader.JOIN_REQ)
					{
						handleJoinRequest(hdr.mbr);
						break;
					}
					else if(hdr.type == GmsHeader.JOIN_RSP)
					{
						impl.handleJoinResponse(hdr.join_rsp);
						break;
					}
					else if(hdr.type == GmsHeader.LEAVE_REQ)
					{
						if(Trace.trace)
							Trace.info("GMS.up()", "received LEAVE_REQ " + hdr + " from " + msg.Source);

						if(hdr.mbr == null) 
						{
							if(Trace.trace)
								Trace.error("GMS.up()", "LEAVE_REQ's mbr field is null");
							return;
						}
						sendLeaveResponse(hdr.mbr);
						impl.handleLeave(hdr.mbr, false);
						break;
					}
					else if(hdr.type == GmsHeader.LEAVE_RSP)
					{
						impl.handleLeaveResponse();
						break;
					}
					else if(hdr.type == GmsHeader.VIEW)
					{
						if(hdr.view == null) 
						{
							if(Trace.trace)
								Trace.error("GMS.up()", "[VIEW]: view == null");
							return;
						}
						impl.handleViewChange(hdr.view, hdr.digest);
						break;
					}
					else
					{
						if(Trace.trace)
							Trace.error("GMS.up()", "GmsHeader with type=" + hdr.type + " not known");
						return;  // don't pass up
					}
				
				case Event.CONNECT_OK:     // sent by someone else, but WE are responsible for sending this !
				case Event.DISCONNECT_OK:  // dito (e.g. sent by UDP layer). Don't send up the stack
					return;


				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
	    
					if(print_local_addr) 
					{
						Console.WriteLine("\n-------------------------------------------------------\n" +
								   "GMS: address is " + local_addr +
								   "\n-------------------------------------------------------");
					}
					break;                               // pass up

				case Event.SUSPECT:
					impl.suspect((Address)evt.Arg);
					break;                               // pass up

				case Event.UNSUSPECT:
					impl.unsuspect((Address)evt.Arg);
					return;                              // discard

			}

			if(impl.handleUpEvent(evt))
				passUp(evt);
		}

		/// <remarks>
		/// This method is overridden to avoid hanging on getDigest(): when a JOIN is received, the coordinator needs
		/// to retrieve the digest from the PBCAST layer. It therefore sends down a GET_DIGEST event, to which the PBCAST layer
		/// responds with a GET_DIGEST_OK event.<p>
		/// However, the GET_DIGEST_OK event will not be processed because the thread handling the JOIN request won't process
		/// the GET_DIGEST_OK event until the JOIN event returns. The receiveUpEvent() method is executed by the up-handler
		/// thread of the lower protocol and therefore can handle the event. All we do here is unblock the mutex on which
		/// JOIN is waiting, allowing JOIN to return with a valid digest. The GET_DIGEST_OK event is then discarded, because
		/// it won't be processed twice.</p>
		/// </remarks>
		/// <summary>
		/// Intercepts GET_DIGEST_OK Events
		/// </summary>
		/// <param name="evt"></param>
		protected override void receiveUpEvent(Event evt) 
		{
			if(evt.Type == Event.GET_DIGEST_OK) 
			{
				lock(digest_mutex) 
				{
					digest=(Digest)evt.Arg;
					Monitor.PulseAll(digest_mutex);
				}
				return;
			}
			base.receiveUpEvent(evt);
		}
    
		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt) 
		{
			switch(evt.Type) 
			{
				case Event.CONNECT:
					passDown(evt);
					group_addr=(String)evt.Arg;
					impl.join(local_addr);
					passUp(new Event(Event.CONNECT_OK));
					return;    // don't pass down: was already passed down

				case Event.DISCONNECT:
					impl.leave((Address)evt.Arg);
					passUp(new Event(Event.DISCONNECT_OK));
					initState(); // in case connect() is called again
					break;       // pass down
			}

			if(impl.handleDownEvent(evt))
				passDown(evt);
		}


		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props) 
		{
			if(props.Contains("shun")) 
			{
				shun = Convert.ToBoolean(props["shun"]);
				props.Remove("shun");
			}
			if(props.Contains("print_local_addr")) 
			{
				print_local_addr = Convert.ToBoolean(props["print_local_addr"]);
				props.Remove("print_local_addr");
			}
			if(props.Contains("join_timeout")) 
			{
				join_timeout = Convert.ToInt64(props["join_timeout"]);
				props.Remove("join_timeout");
			}
			if(props.Contains("join_retry_timeout")) 
			{
				join_retry_timeout = Convert.ToInt64(props["join_retry_timeout"]);
				props.Remove("join_retry_timeout");
			}
			if(props.Contains("leave_timeout")) // time to wait until coord responds to LEAVE req.
			{
				leave_timeout = Convert.ToInt64(props["leave_timeout"]);
				props.Remove("leave_timeout");
			}
			if(props.Contains("digest_timeout")) // time to wait for GET_DIGEST_OK from PBCAST
			{
				digest_timeout = Convert.ToInt64(props["digest_timeout"]);
				props.Remove("digest_timeout");
			}
			if(props.Contains("disable_initial_coord")) // time to wait for GET_DIGEST_OK from PBCAST
			{
				disable_initial_coord = Convert.ToBoolean(props["disable_initial_coord"]);
				props.Remove("disable_initial_coord");
			}

			if(props.Count > 0) 
			{
				return false;
			}
			return true;
		}



		/* ------------------------------- Private Methods --------------------------------- */

		/// <summary>
		/// Sets the current GMS implementation to Client and resets the viewID
		/// </summary>
		private void initState() 
		{
			becomeClient();
			view_id=null;
		}

		/// <summary>
		/// Deals with incoming join requests. If we are a coordinator then a response will be sent.
		/// </summary>
		/// <param name="mbr"></param>
		void handleJoinRequest(Address mbr) 
		{
			JoinRsp   join_rsp;
			Message   m;
			GmsHeader hdr;
			ArrayList    new_mbrs=new ArrayList();

			if(mbr == null) 
			{
				if(Trace.trace)
					Trace.error("GMS.handleJoinRequest()", "mbr is null");
				return;
			}

			if(Trace.trace)
				Trace.info("GMS.handleJoinRequest()", "mbr=" + mbr);

			// 1. Get the new view and digest
			join_rsp=impl.handleJoin(mbr);
			if(join_rsp == null)
				if(Trace.trace)
					Trace.error("GMS.handleJoinRequest()", impl.getName() + ".handleJoin(" + mbr + 
					") returned null: will not be able to multicast new view");

			// 2. Send down a local TMP_VIEW event. This is needed by certain layers (e.g. NAKACK) to compute correct digest
			//    in case client's next request reaches us *before* our own view change multicast.
			if(join_rsp != null && join_rsp.getView() != null)
				passDown(new Event(Event.TMP_VIEW, join_rsp.getView()));

			// 3. Return result to client
			m=new Message(mbr, null, null);
			hdr=new GmsHeader(GmsHeader.JOIN_RSP, join_rsp);
			m.putHeader(getName(), hdr);
			passDown(new Event(Event.MSG, m));

			// 4. Bcast the new view
			if(join_rsp != null)
				castViewChange(join_rsp.getView());
		}
    
		/// <summary>
		/// Acknowledges leave response.
		/// </summary>
		/// <param name="mbr"></param>
		private void sendLeaveResponse(Address mbr) 
		{
			Message   msg=new Message(mbr, null, null);
			GmsHeader hdr=new GmsHeader(GmsHeader.LEAVE_RSP);
			msg.putHeader(getName(), hdr);
			passDown(new Event(Event.MSG, msg));
		}

		/// <summary>
		/// Returns a String represention and the members in the membership
		/// </summary>
		String printMembers(ArrayList mems)
		{
			String sb="(";
			bool      first=true;
			Object       el;

			if(mems != null) 
			{
				for(int i=0; i < mems.Count; i++) 
				{
					if(!first)
						sb = sb + (", ");
					else
						first=false;
					el=mems[i];
					if(el is Address)
						sb = sb + el;
				}
			}
			sb = sb + ")";
			return sb;
		}
		/* --------------------------- End of Private Methods ------------------------------- */
	}
}
