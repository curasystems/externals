using System;
using System.Diagnostics;
using System.Collections;
using System.Data;

using GCT.Util;
using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// Messages are assigned a monotonically increasing sequence number (seqno).
	/// Receivers deliver messages ordered according to seqno and request retransmission of missing messages. Retransmitted
	/// messages are bundled into bigger ones, e.g. when getting an xmit request for messages 1-10, instead of sending 10
	/// unicast messages, we bundle all 10 messages into 1 and send it. 
	/// We only bundle messages up to max_xmit_size bytes to prevent too large messages. For example, if the bundled message
	/// size was a total of 34000 bytes, and max_xmit_size=16000, we'd send 3 messages: 2 16K and a 2K message.
	/// </remarks>
	/// <summary>
	/// Protocol: Multicast Negative AcKnowledgement layer (NAK)
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class NAKACK : Protocol, Retransmitter.RetransmitCommand 
	{
		/// <summary>Interval to wait before requesting retransmission</summary>
		long[]			retransmit_timeout={1000,2000,4000,8000};
		/// <summary>If set, process is a member of a group</summary>
		bool			is_server=false;
		/// <summary>Local <c>Address</c></summary>
		Address			local_addr=null;
		/// <summary>Current members in the group</summary>
		ArrayList		members=new ArrayList();   
		/// <summary>Current message sequence number (starts with 0)</summary> 
		long			seqno = 0;						
		/// <summary>Keeps track of lowest sequence number garbage collected</summary>
		long			deleted_up_to = 0;
		/// <summary>Maximum size of a retransmit message</summary>
		long			max_xmit_size = 8192;				
		/// <summary>Number of msgs garbage collection lags behind</summary>
		int				gc_lag = 20;	
		/// <summary>Holds senders and their associated NakReceiverWindows</summary>
		Hashtable		received_msgs = new Hashtable();	
		/// <summary>Holds sent message sequence numbers and the associated message</summary>
		Hashtable		sent_msgs = new Hashtable();
		/// <summary>If set then currently disconnecting from the group</summary>
		bool			leaving=false;
		/// <summary>Used for running tasks.</summary>
		TimeScheduler	timer=null;

		/// <summary>
		/// Constructor.
		/// </summary>
		public NAKACK() 
		{
			name = "NAKACK";
		}

		/// <summary>
		/// Returns unique <c>Protocol</c> name
		/// </summary>
		/// <returns>Unique <c>Protocol</c> name</returns>
		public  String  getName()       {return "NAKACK";}

		/// <summary>
		/// Provided services for layers below NAKACK
		/// </summary>
		/// <returns>List of provided service</returns>
		public override ArrayList providedUpServices() 
		{
			ArrayList retval=new ArrayList();
			retval.Add((int)Event.GET_DIGEST);
			retval.Add((int)Event.SET_DIGEST);
			retval.Add((int)Event.MERGE_DIGEST);
			return retval;
		}


		/// <summary>
		/// Provided services for layers above NAKACK
		/// </summary>
		/// <returns>List of provided services</returns>
		public override ArrayList providedDownServices() 
		{
			ArrayList retval=new ArrayList();
			retval.Add((int)Event.GET_DIGEST);
			return retval;
		}

		/// <summary>
		/// Uses TimeScheduler from ProtocolSinkStack.
		/// </summary>
		public override  void start()
		{
			timer=stack != null ? stack.timer : null;
			if(timer == null)
				throw new Exception("NAKACK.up(): timer is null");
		}

		/// <summary>
		/// Stops any running tasks
		/// </summary>
		public override void stop() 
		{
			if(timer != null) 
			{
				try 
				{
					timer.stop();
				}
				catch(Exception ex) 
				{
				}
			}
			removeAll();  // clears sent_msgs and destroys all NakReceiverWindows
		}


		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt) 
		{
			Message msg;
			Digest  digest;
			ArrayList  mbrs;

			switch(evt.Type) 
			{

				case Event.MSG:
					msg=(Message)evt.Arg;
					if(msg.Destination != null && !msg.Destination.isMulticastAddress())
						break; // unicast address: not null and not mcast, pass down unchanged

					send(msg);
					return;    // don't pass down the stack

				case Event.GET_DIGEST:
					digest=getDigest();
					passUp(new Event(Event.GET_DIGEST_OK, digest));
					return;	    

				case Event.SET_DIGEST:
					setDigest((Digest)evt.Arg);
					return;

				case Event.MERGE_DIGEST:
					mergeDigest((Digest)evt.Arg);
					return;
	    
				case Event.TMP_VIEW:
					mbrs=((View)evt.Arg).getMembers();
					members.Clear();
					members.AddRange(mbrs);
					adjustReceivers();
					break;

				case Event.VIEW_CHANGE:
					mbrs=((View)evt.Arg).getMembers();
					members.Clear();
					members.AddRange(mbrs);
					adjustReceivers();
					is_server=true;  // check vids from now on
					break;

				case Event.BECOME_SERVER:
					is_server=true;
					break;

				case Event.DISCONNECT:
					leaving=true;
					removeAll();
					seqno=0;
					break;
			}

			passDown(evt);
		}

		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			Object       obj;
			NakAckHeader  hdr;
			Message       msg;
			Digest        digest;


			switch(evt.Type) 
			{

				case Event.STABLE:  // generated by STABLE layer. Delete stable messages passed in arg
					stable((Digest)evt.Arg);
					return; 

				case Event.GET_DIGEST:
					digest=getDigestHighestDeliveredMsgs();
					passDown(new Event(Event.GET_DIGEST_OK, digest));
					return;	    

				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;

				case Event.MSG:
					msg=(Message)evt.Arg;
					obj=msg.getHeader(getName());
					if(obj == null || !(obj is NakAckHeader))
							break;  // pass up (e.g. unicast msg)

					// discard messages while not yet server (i.e., until JOIN has returned)
					if(!is_server)
					{
						if(Trace.trace)
							Trace.info("NAKACK.up()", "message was discarded (not yet server)");
						return;
					}
					hdr=(NakAckHeader)msg.removeHeader(getName());
				switch(hdr.type) 
				{
					case NakAckHeader.MSG:
						handleMessage(msg, hdr);
						return;        // transmitter passes message up for us!

					case NakAckHeader.XMIT_REQ:
						if(hdr.range == null) 
						{
							if(Trace.trace)
								Trace.error("NAKACK.up()", "XMIT_REQ: range of xmit msg == null; discarding request from " +
									msg.Source);
							return;
						}
						handleXmitReq(msg.Source, hdr.range.low, hdr.range.high);
						return;
		
					case NakAckHeader.XMIT_RSP:
						if(Trace.trace)
							Trace.info("NAKACK.handleXmitRsp()", "received missing messages " + hdr.range);
						handleXmitRsp(msg);
						return;

					default:
						if(Trace.trace)
							Trace.error("NAKACK.up()", "NakAck header type " + hdr.type + " not known !");
						return;
				}
			}
			passUp(evt);
		}

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props) 
		{
			if(props.Contains("retransmit_timeout")) 
			{
				String[] strArray = Convert.ToString(props["retransmit_timeout"]).Split(new Char[] {','});
				for(int i = 0 ; i<strArray.Length;i++)
				{
					retransmit_timeout[i] = Convert.ToInt64(strArray[i]);
				}
				props.Remove("retransmit_timeout");
			}
			if(props.Contains("gc_lag")) 
			{
				gc_lag =  Convert.ToInt32(props["gc_lag"]);
				if(gc_lag < 1) 
				{
					if(Trace.trace)
						Trace.error("NAKACK.setProperties()","gc_lag has to be at least 1 (set to 10)");
					gc_lag=10;
				}
				props.Remove("gc_lag");
			}
			if(props.Contains("max_xmit_size")) 
			{
				max_xmit_size =  Convert.ToInt64(props["max_xmit_size"]);
				props.Remove("max_xmit_size");
			}
			if(props.Count > 0) 
			{
				return false;
			}
			return true;
		}




		/* --------------------------------- Private Methods --------------------------------------- */

		/// <summary>
		/// Increments and returns the current sequence number
		/// </summary>
		/// <returns>The next sequence number</returns>
		private long getNextSeqno() {return seqno++;}

		/// <summary>
		/// Adds the message to the sent_msgs table and then passes it down the stack.
		/// </summary>
		/// <param name="msg">The message to be sent</param>
		void send(Message msg) 
		{
			long msg_id = getNextSeqno();
			if(Trace.trace)
				Trace.info("NAKACK.send()", "sending msg #" + msg_id);
			msg.putHeader(name, new NakAckHeader(NakAckHeader.MSG, msg_id));

			sent_msgs.Add((long)msg_id, msg);
			passDown(new Event(Event.MSG, msg));
		}

		/// <remarks>
		/// Finds the corresponding NakReceiverWindow and adds the message to it (according to seqno). Then removes
		/// as many messages as possible from the NRW and passes them up the stack. Discards messages from non-members.
		/// </remarks>
		/// <summary>
		/// Results in all possible messages from sender being delivered up the stacks
		/// </summary>
		/// <param name="msg"></param>
		/// <param name="hdr"></param>
		void handleMessage(Message msg, NakAckHeader hdr) 
		{
			NakReceiverWindow  win=null;
			Message            msg_to_deliver;
			Address            sender;

			if(msg == null || hdr == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.handleMessage()", "msg or header is null");
				return;
			}
			sender=msg.Source;
			if(sender == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.handleMessage()", "sender of message is null");
				return;
			}
			if(Trace.trace)
				Trace.info("NAKACK.handleMessage()", "[" + local_addr + "] received <" + sender + "#" + hdr.seqno + ">");

			// msg is potentially re-sent later as result of XMIT_REQ reception; that's why hdr is added !
			msg.putHeader(getName(), hdr); 

			win=(NakReceiverWindow)received_msgs[sender];
			if(win == null) 
			{  // discard message if there is no entry for sender
				if(leaving)
					return;
				if(Trace.trace)
					Trace.warn("NAKACK.handleMessage()", "[" + local_addr + "] discarded message from non-member " + sender);
				return;
			}
			win.add(hdr.seqno, msg);  // add in order, then remove and pass up as many msgs as possible
			while((msg_to_deliver=win.remove()) != null) 
			{
				msg_to_deliver.removeHeader(getName());
				passUp(new Event(Event.MSG, msg_to_deliver));
			}
		}

		/// <remarks>
		/// Retransmit from sent-table, called when XMIT_REQ is received. Bundles all messages to be xmitted into
		/// one large message and sends them back with an XMIT_RSP header. Note that since we cannot count on a
		/// fragmentation layer below us, we have to make sure the message doesn't exceed max_xmit_size bytes. If
		/// this is the case, we split the message into multiple, smaller-chunked messages. But in most cases this
		/// still yields fewer messages than if each requested message was retransmitted separately.
		/// </remarks>
		/// <summary>
		/// Retransmits all the missing messages.
		/// </summary>
		/// <param name="dest"></param>
		/// <param name="first_seqno"></param>
		/// <param name="last_seqno"></param>
		void handleXmitReq(Address dest, long first_seqno, long last_seqno) 
		{
			Message   m, tmp;
			ArrayList list;
			long      size=0, marker=first_seqno;

			if(first_seqno > last_seqno) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.handleXmitReq()", "first_seqno (" +
					first_seqno + ") > last_seqno (" + last_seqno + "): not able to retransmit");
				return;
			}
			list=new ArrayList();
			for(long i=first_seqno; i <= last_seqno; i++) 
			{
				m=(Message)sent_msgs[(long)i];
				if(m == null) 
				{
					if(Trace.trace)
						Trace.error("NAKACK.handleXmitReq()", "(requester=" + dest + ") message with " + "seqno=" + i + " not found in sent_msgs ! sent_msgs=" + printSentMsgs());
					continue;
				}
				size+=m.size();
				if(size >= max_xmit_size) 
				{
					// size has reached max_xmit_size. go ahead and send message (excluding the current mressage)
					if(Trace.trace)
						Trace.info("NAKACK.handleXmitReq()", "xmitting msgs [" + marker + "-" + (i-1) + "] to " + dest);
					sendXmitRsp(new Message(dest, null, list.ToArray(typeof(Message))), marker, i-1);
					marker=i;
					list.Clear();
					size=0;
				}
				tmp=m;
				tmp.Destination = dest;
				tmp.Source = local_addr;
				list.Add(tmp);
			}
	
			if(list.Count > 0) 
			{
				if(Trace.trace)
					Trace.info("NAKACK.handleXmitReq()", "xmitting msgs [" + marker + "-" + last_seqno + "] to " + dest);
				sendXmitRsp(new Message(dest, null, list.ToArray(typeof(Message))), marker, last_seqno);
			}
		}

		/// <summary>
		/// Sends a XMIT_RSP message
		/// </summary>
		/// <param name="msg">Message containing missing messages</param>
		/// <param name="first_seqno">First sequence number of missing messages</param>
		/// <param name="last_seqno">Last sequence number of missing messages</param>
		void sendXmitRsp(Message msg, long first_seqno, long last_seqno) 
		{
			msg.putHeader(getName(), new NakAckHeader(NakAckHeader.XMIT_RSP, first_seqno, last_seqno));
			passDown(new Event(Event.MSG, msg));
		}


		/// <summary>
		/// Delivers all received messages from XMIT_RSP 
		/// </summary>
		/// <param name="msg">XMIT_RSP Message</param>
		void handleXmitRsp(Message msg) 
		{
			ArrayList    list;

			if(msg == null) 
			{
				if(Trace.trace)
					Trace.warn("NAKACK.handleXmitRsp()", "message is null");
				return;
			}
			try 
			{
				list= new ArrayList((Array)msg.getMessageArray());
				if(list != null) 
				{
					foreach(Message m in list)
					{
						up(new Event(Event.MSG, m));
					}
				}
			}
			catch(Exception ex) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.handleXmitRsp()",
					"message did not contain a list (ArrayList) of retransmitted messages: " + ex);
			}	
		}

		/// <summary>
		/// Removes all old members and creates NakReceiverWindows for all new members
		/// </summary>
		void adjustReceivers() 
		{
			Address				newSender;
			NakReceiverWindow	win;
			Hashtable			oldSenders = new Hashtable();

			// 1. Remove all senders in received_msgs that are not members anymore
			foreach(Address sender in received_msgs.Keys)
			{
				if(!members.Contains(sender)) 
				{
					oldSenders.Add(sender, received_msgs[sender]);
				}
			}

			foreach(Address sender in oldSenders.Keys)
			{
				win=(NakReceiverWindow)received_msgs[sender];
				win.reset();
				if(Trace.trace)
					Trace.info("NAKACK.adjustReceivers()", "removing " + sender +
						" from received_msgs (not member anymore)");
				received_msgs.Remove(sender);
			}

			// 2. Add newly joined members to received_msgs (starting seqno=0)
			for(int i=0; i < members.Count; i++) 
			{
				newSender=(Address)members[i];
				if(!received_msgs.ContainsKey(newSender)) 
				{
					win = new NakReceiverWindow(newSender, this, 0, timer);
					win.setRetransmitTimeouts(retransmit_timeout);
					received_msgs.Add(newSender, win);
				}
			}
		}

		/// <summary>
		/// Returns a message digest
		/// </summary>
		Digest getDigest() 
		{
			Digest             digest;
			Address            sender;
			Range              range;

			digest=new Digest(members.Count);
			for(int i=0; i < members.Count; i++) 
			{
				sender=(Address)members[i];
				range=getLowestAndHighestSeqno(sender, false);  // get the highest received seqno
				if(range == null) 
				{
					if(Trace.trace)
						Trace.error("NAKACK.getDigest()", "range is null");
					continue;
				}
				digest.add(sender, range.low, range.high);  // add another entry to the digest
			}
			return digest;
		}

		/// <remarks>
		/// Returns a message digest: for each member P the highest seqno received from P <em>without a gap</em>
		/// is added to the digest. E.g. if the seqnos received from P are [+3 +4 +5 -6 +7 +8], then 5 will be returned.
		/// Also, the highest seqno <em>seen</em> is added. The max of all highest seqnos seen will be used (in STABLE)
		/// to determine whether the last seqno from a sender was received).
		/// </remarks>
		/// <summary>
		/// Returns a message digest.
		/// </summary>
		Digest getDigestHighestDeliveredMsgs() 
		{
			Digest             digest;
			Address            sender;
			Range              range;
			long               high_seqno_seen=0;

			digest=new Digest(members.Count);
			for(int i=0; i < members.Count; i++) 
			{
				sender=(Address)members[i];
				range=getLowestAndHighestSeqno(sender, true);  // get the highest deliverable seqno
				if(range == null) 
				{
					if(Trace.trace)
						Trace.error("NAKACK.getDigest()", "range is null");
					continue;
				}
				high_seqno_seen=getHighSeqnoSeen(sender);
				digest.add(sender, range.low, range.high, high_seqno_seen);  // add another entry to the digest
			}
			return digest;
		}

		/// <summary>
		/// Installs new NakReceiverWindows with the values specified in the Digest.
		/// </summary>
		/// <param name="d"></param>
		void setDigest(Digest d) 
		{
			Address            sender;
			NakReceiverWindow  win;
			long               initial_seqno;

			clear();
			if(d == null || d.senders == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.setDigest()", "digest or digest.senders is null");
				return;
			}
			for(int i=0; i < d.size(); i++) 
			{
				sender=d.senderAt(i);
				if(sender == null) 
				{
					if(Trace.trace)
						Trace.error("NAKACK.setDigest()", "sender at index " + i + " in digest is null");
					continue;
				}
				initial_seqno=d.highSeqnoAt(i);
				win=new NakReceiverWindow(sender, this, initial_seqno, timer);
				win.setRetransmitTimeouts(retransmit_timeout);
				received_msgs.Add(sender, win);
			}
		}


		/// <remarks>
		/// For all members of the digest, adjust the NakReceiverWindows in the received_msgs hashtable.
		/// If the member already exists, sets its seqno to be the max of the seqno and the seqno of the member
		/// in the digest. If no entry exists, create one with the initial seqno set to the seqno of the
		/// member in the digest.
		/// </remarks>
		/// <summary>
		/// Merges two digests together
		/// </summary>
		/// <param name="d">Digest to merge</param>
		void mergeDigest(Digest d) 
		{
			Address            sender;
			NakReceiverWindow  win;
			long               initial_seqno;

			if(d == null || d.senders == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.mergeDigest()", "digest or digest.senders is null");
				return;
			}
			for(int i=0; i < d.size(); i++) 
			{
				sender=d.senderAt(i);
				if(sender == null) 
				{
					if(Trace.trace)
						Trace.error("NAKACK.mergeDigest()", "sender at index " + i + " in digest is null");
					continue;
				}
				initial_seqno=d.highSeqnoAt(i);
				win=(NakReceiverWindow)received_msgs[sender];
				if(win == null) 
				{
					win=new NakReceiverWindow(sender, this, initial_seqno, timer);
					win.setRetransmitTimeouts(retransmit_timeout);
					received_msgs.Add(sender, win);
				}
				else 
				{
					if(win.getHighestReceived() < initial_seqno) 
					{
						win.reset();
						received_msgs.Remove(sender);
						win=new NakReceiverWindow(sender, this, initial_seqno, timer);
						win.setRetransmitTimeouts(retransmit_timeout);
						received_msgs.Add(sender, win);
					}
				}
			}
		}


		/// <remarks>
		/// Returns the lowest seqno still in cache (so it can be retransmitted) and the highest seqno received so far.
		/// <p>
		/// If <c>stop_at_gaps</c> is true, the highest seqno *deliverable* will be returned. If false, the highest seqno 
		/// *received* will be returned. E.g. for [+3 +4 +5 -6 +7 +8], the highest_seqno_received is 8,
		/// whereas the higheset_seqno_seen (deliverable) is 5.
		/// </p>
		/// </remarks>
		/// <summary>
		/// Returns the Digest range for a sender
		/// </summary>
		/// <param name="sender">The address for which the highest and lowest seqnos are to be retrieved</param>
		/// <param name="stop_at_gaps">See Remarks</param>
		/// <returns>Range of sequence numbers</returns>
		private Range getLowestAndHighestSeqno(Address sender, bool stop_at_gaps) 
		{
			Range              r=null;
			NakReceiverWindow  win;

			if(sender == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.getLowestAndHighestSeqno()", "sender is null");
				return r;
			}
			win=(NakReceiverWindow)received_msgs[sender];
			if(win == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.getLowestAndHighestSeqno()", "sender " + sender +
					" not found in received_msgs");
				return r;
			}
			if(stop_at_gaps)
				r=new Range(win.getLowestSeen(), win.getHighestSeen());       // deliverable messages (no gaps)
			else
				r=new Range(win.getLowestSeen(), win.getHighestReceived()+1); // received messages
			return r;
		}

		/// <summary>
		/// Returns the highest seqno seen from sender. 
		/// </summary>
		/// <param name="sender">The address for which the highest seqno is to be retrieved</param>
		/// <returns>Highest seen sequence number</returns>
		long getHighSeqnoSeen(Address sender) 
		{
			NakReceiverWindow  win;
			long               ret=0;

			if(sender == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.getHighSeqnoSeen()", "sender is null");
				return ret;
			}
			if(sender.Equals(local_addr))
				return seqno-1;

			win=(NakReceiverWindow)received_msgs[sender];
			if(win == null) 
			{
				if(Trace.trace)
					Trace.error("NAKACK.getHighSeqnoSeen()", "sender " + sender + " not found in received_msgs");
				return ret;
			}
			ret=win.getHighestReceived();
			return ret;
		}


		/// <remarks>
		/// Garbage collect all messages less than or equal to seqno at digest[P]. Update received_msgs:
		/// for each sender P in the digest and its highest seqno seen SEQ, garbage collect all delivered_msgs in the
		///	 NakReceiverWindow corresponding to P.
		/// </remarks>
		/// <summary>
		/// Garbage collect messages that have been seen by all members.
		/// </summary>
		/// <param name="d"></param>
		void stable(Digest d) 
		{
			long               seqno;
			NakReceiverWindow  recv_win;
			Address            sender;
			long               my_highest_rcvd;        // highest seqno received in my digest for a sender P
			long               stability_highest_rcvd; // highest seqno received in the stability ArrayList for a sender P

			if(members == null || local_addr == null || d == null) 
			{
				if(Trace.trace) Trace.warn("NAKACK.stable()", "members, local_addr or digest are null !");
				return;
			}

			if(Trace.trace)
				Trace.info("NAKACK.stable()", "received digest " + d);

			for(int i=0; i < d.size(); i++) 
			{
				sender = d.senderAt(i);
				seqno = d.highSeqnoAt(i);
				if(sender == null)
					continue;

				// check whether the last seqno received for a sender P in the stability ArrayList is > last seqno 
				// received for P in my digest. if yes, request retransmission 
				recv_win=(NakReceiverWindow)received_msgs[sender];
				if(recv_win != null) 
				{
					my_highest_rcvd = recv_win.getHighestReceived();
					stability_highest_rcvd = d.highSeqnoSeenAt(i);
		
					if(stability_highest_rcvd >= 0 && stability_highest_rcvd > my_highest_rcvd) 
					{
						if(Trace.trace)
							Trace.info("NAKACK.stable()", "my_highest_rcvd (" + my_highest_rcvd + 
								") < stability_highest_rcvd (" + stability_highest_rcvd + 
								"): requesting retransmission of " + sender + "#" + stability_highest_rcvd);
						retransmit(stability_highest_rcvd, stability_highest_rcvd, sender);
					}
				}

				seqno -= gc_lag;
				if(seqno < 0)
					continue;

				if(Trace.trace) Trace.info("NAKACK.stable()", "deleting stable msgs < " + sender + "#" + seqno);

				// garbage collect from sent_msgs if sender was myself
				if(sender.Equals(local_addr)) 
				{
					if(Trace.trace)
						Trace.info("NAKACK.stable()", "removing [" + deleted_up_to + " - " + seqno + "] from sent_msgs");
					for(long j=deleted_up_to; j <= seqno; j++)
						sent_msgs.Remove((long)j);
					deleted_up_to=seqno;
				}

				// delete *delivered* msgs that are stable
				if (received_msgs.Contains(sender))
					recv_win=(NakReceiverWindow)received_msgs[sender];
				if(recv_win != null)
					recv_win.stable(seqno);  // delete all messages with seqnos <= seqno
			}
		}



		/* ---------------------- Interface Retransmitter.RetransmitCommand ---------------------- */

		/// <summary>
		/// Implementation of Retransmitter.RetransmitCommand, sends XMIT_REQ to originator of message
		/// </summary>
		/// <param name="first_seqno">First sequence number missing</param>
		/// <param name="last_seqno">Last sequence number missing</param>
		/// <param name="sender">Origin of the message</param>
		public void retransmit(long first_seqno, long last_seqno, Address sender) 
		{
			lock(this)
			{
				NakAckHeader hdr;
				Message      retransmit_msg = new Message(sender, null, null);

				if(Trace.trace)
					Trace.info("NAKACK.retransmit()", "sending XMIT_REQ ([" + first_seqno +
					", " + last_seqno + "]) to " + sender);

				hdr=new NakAckHeader(NakAckHeader.XMIT_REQ, first_seqno, last_seqno);
				retransmit_msg.putHeader(getName(), hdr);
				passDown(new Event(Event.MSG, retransmit_msg));
			}
		}

		/* ------------------- End of Interface Retransmitter.RetransmitCommand -------------------- */

		/// <summary>
		/// Resets NAKACK and resets the NakReceiverWindow of each sender
		/// </summary>
		void clear() 
		{
			sent_msgs.Clear();
			foreach(NakReceiverWindow win in received_msgs.Values)
			{
				win.reset();
			}
			received_msgs.Clear();
		}

		/// <summary>
		/// Resets NAKACK and destorys the NakReceiverWindow of each sender
		/// </summary>
		void removeAll() 
		{
			sent_msgs.Clear();
			foreach(NakReceiverWindow win in received_msgs.Values)
			{
				win.destroy();
			}
			received_msgs.Clear();
		}

		/// <summary>
		/// Returns a string representation of the sent messages.
		/// </summary>
		String printSentMsgs() 
		{
			String sb= "";
			foreach(Address key in sent_msgs.Keys)
				sb += key + " ";
			return sb;
		}
    
    /* ----------------------------- End of Private Methods ------------------------------------ */
	}
	
}
