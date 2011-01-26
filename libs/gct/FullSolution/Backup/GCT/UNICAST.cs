using System;
using System.Collections;
using System.Data;

using GCT.Util;
using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// Uses acknowledgement scheme similar to TCP to provide lossless transmission
	/// of unicast messages (for reliable multicast see NAKACK layer). When a message is sent to a peer for
	/// the first time, we add the pair (peer_addr, Entry) to the hashtable (peer address is the key). All
	/// messages sent to that peer will be added to hashtable.peer_addr.sent_msgs. When we receive a
	/// message from a peer for the first time, another entry will be created and added to the hashtable
	/// (unless already existing). Msgs will then be added to hashtable.peer_addr.received_msgs.
	/// <p> 
	/// This layer is used to reliably transmit point-to-point messages, that is, either messages sent to a
	/// single receiver (vs. messages multicast to a group) or for example replies to a multicast message. The 
	/// sender uses an <code>AckSenderWindow</code> which retransmits messages for which it hasn't received
	/// an ACK, the receiver uses <code>AckReceiverWindow</code> which keeps track of the lowest seqno
	/// received so far, and keeps messages in order.</p>
	/// <p>
	/// Messages in both AckSenderWindows and AckReceiverWindows will be removed. A message will be removed from
	/// AckSenderWindow when an ACK has been received for it and messages will be removed from AckReceiverWindow
	/// whenever a message is received: the new message is added and then we try to remove as many messages as
	/// possible (until we stop at a gap, or there are no more messages).</p>
	/// </remarks>
	/// <summary>
	/// Protocol: Reliable unicast layer.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class UNICAST : Protocol, AckSenderWindow.RetransmitCommand 
	{
		/// <summary>Signifies the channel is active</summary>
		private bool				operational=false;
		/// <summary>Current members in the group</summary>
		private ArrayList			members=new ArrayList();
		/// <summary>Contains Entries for each member</summary>
		private Hashtable			connections=new Hashtable();   // Object (sender or receiver) -- Entries
		/// <summary>For AckSenderWindow: max time to wait for missing acks</summary>
		private long[]				timeout={800,1600,3200,6400}; 
		/// <summary>Local Address</summary>
		private Address				local_addr=null;
		/// <summary>Scheduler used for retransmissions (AckSenderWindow)</summary>
		private TimeScheduler		timer = null;  
		/// <summary>Sliding window: Max number of msgs in table (AckSenderWindow)</summary>
		private int					window_size=-1;    
		/// <summary>Sliding window: Number under which table has to fall before we resume adding msgs (AckSenderWindow)</summary>
		private int					min_threshold=-1;      
    
		/// <summary>
		/// An Entry is stored for every member that sends a message
		/// </summary>
		protected class Entry
		{
			/// <summary>Stores all msgs received by a certain peer in seqno-order</summary>
			public AckReceiverWindow  received_msgs=null; 
			/// <summary>Stores (and retransmits) msgs sent by us to a certain peer</summary>
			public AckSenderWindow    sent_msgs=null;   
			/// <summary>Sequence numebr for msgs sent by this channel</summary>
			public long               sent_msgs_seqno;
			/// <summary>UNICAST Protocol associated with the Entry</summary>
			UNICAST					  uni = null;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="uni">UNICAST Protocol associated with the Entry</param>
			public Entry(UNICAST uni)
			{
				this.uni = uni;
				sent_msgs_seqno = uni.getInitialSeqno();
			}

			/// <summary>
			/// Resets the AckReceiverWindow and the AckSenderWindow
			/// </summary>
			public void reset() 
			{
				if(sent_msgs != null)
					sent_msgs.reset();
				if(received_msgs != null)
					received_msgs.reset();
			}

			/// <summary>
			/// Returns a string representation of the Entry
			/// </summary>
			/// <returns>A string representation of the Entry</returns>
			public String toString() 
			{
				String sb="";
				if(sent_msgs != null)
					sb += "sent_msgs=" + sent_msgs + "\n";
				if(received_msgs != null)
					sb += "received_msgs=" + received_msgs + "\n";	    
				return sb;
			}
		}

		/// <summary>
		/// Protocol : STABLE computes the broadcast messages that are stable
		/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
		/// <p><b>Date:</b>  12/03/2003</p>
		/// </summary>
		public UNICAST()
		{
			name = "UNICAST";
		}

		/// <summary>
		/// Returns unique protocol name
		/// </summary>
		/// <returns>Unique protocol name</returns>
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
				String[] strArray = Convert.ToString(props["timeout"]).Split(new Char[] {','});
				for(int i = 0 ; i<strArray.Length;i++)
				{
					timeout[i] = Convert.ToInt64(strArray[i]);
				}
				props.Remove("timeout");
			}
			if(props.Contains("window_size")) 
			{
				window_size = Convert.ToInt32(props["window_size"]);
				props.Remove("window_size");
			}
			if(props.Contains("min_threshold")) 
			{
				min_threshold = Convert.ToInt32(props["min_threshold"]);
				props.Remove("min_threshold");
			}

			if(props.Count > 0) 
			{
				return false;
			}

			// Some sanity checks
			if((window_size > 0 && min_threshold <= 0) || (window_size <= 0 && min_threshold > 0)) 
			{
				if(Trace.trace)
					Trace.error("UNICAST.setProperties()",
					"window_size and min_threshold have to be both set if one of them is set");
				return false;
			}
			if(window_size > 0 && min_threshold > 0 && window_size < min_threshold) 
			{
				if(Trace.trace)
					Trace.error("UNICAST.setProperties()", "min_threshold (" + min_threshold + 
					") has to be less than window_size (" + window_size + ")");
				return false;
			}
			return true;
		}

		
		/// <summary>
		/// Sets the Scheduler to the Scheduler present in the ProtocolSinkStack
		/// </summary>
		public override void start()
		{
			timer=stack != null ? stack.timer : null;
			if(timer == null)
				if(Trace.trace)
					Trace.error("UNICAST.up()", "[START] timer is null");
		}

		/// <summary>
		/// Resets and removes all sender and receiver windows.
		/// </summary>
		public override void stop()
		{
			removeAllConnections();
			operational=false;
		}

		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			Message        msg;
			Address        dst, src;
			UnicastHeader  hdr;
	
			switch(evt.Type) 
			{

				case Event.MSG:
					msg=(Message)evt.Arg;
					dst=msg.Destination;
					src=msg.Source;
					if(dst == null || dst.isMulticastAddress())  // only handle unicast messages
						break;  // pass up

					hdr=(UnicastHeader)msg.removeHeader(getName());
					if(hdr == null) break;
				switch(hdr.type) 
				{
					case UnicastHeader.DATA:      // received regular message
						sendAck(src, hdr.seqno);
						handleDataReceived(src, hdr.seqno, hdr.first, msg);
						break;
					case UnicastHeader.DATA_ACK:  // received ACK for previously sent message
						handleAckReceived(src, hdr.seqno);
						break;
					default:
						if(Trace.trace)
							Trace.error("UNICAST.up()", "UnicastHeader type " + hdr.type + " not known !");
						break;
				}
					return;

				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;
			}

			passUp(evt);   // Pass up to the layer above us
		}

		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt) 
		{
			Message        msg;
			Object         dst;
			Entry          entry;
			UnicastHeader  hdr;

			switch(evt.Type) 
			{
	    
				case Event.MSG: // Add UnicastHeader, add to AckSenderWindow and pass down
					msg=(Message)evt.Arg;
					dst=msg.Destination;

					/* only handle unicast messages */
					if(dst == null || ((Address)dst).isMulticastAddress())
						break;

					entry = (Entry)connections[dst];
					if(entry == null) 
					{
						entry = new Entry(this);
						connections.Add(dst, entry);
					}

					hdr = new UnicastHeader(UnicastHeader.DATA, entry.sent_msgs_seqno);
					if(entry.sent_msgs == null) 
					{ // first msg to peer 'dst'
						hdr.first=true;
						entry.sent_msgs = new AckSenderWindow(this, timeout, this);
						if(window_size > 0)
							entry.sent_msgs.setWindowSize(window_size, min_threshold);
					}
					msg.putHeader(getName(), hdr);

					if(Trace.trace) 
						Trace.info("UNICAST.down()", "[" + local_addr + "] --> DATA(" + dst + ": #" + entry.sent_msgs_seqno + ", first=" + hdr.first + ")");
					entry.sent_msgs.add(entry.sent_msgs_seqno, msg);	 // add *including* UnicastHeader
					entry.sent_msgs_seqno++;
					return; // AckSenderWindow will send message for us

				case Event.BECOME_SERVER:
					operational=true;
					break;

				case Event.VIEW_CHANGE:  // remove connections to peers that are not members anymore !
					ArrayList new_members=((View)evt.Arg).getMembers();
					ArrayList left_members = new ArrayList();
					lock(members) 
					{
						foreach(Address a in members)
							left_members.Add(a.Copy());
						members.Clear();
						if(new_members != null)
							members.AddRange(new_members);
		
						foreach(Address a in members)
						{
							if(left_members.Contains(a))
								left_members.Remove(a);
						}

						foreach(Address oldMbr in left_members)
						{
							entry=(Entry)connections[oldMbr];
							entry.reset();
							connections.Remove(oldMbr);
						}
					}
					break;
			}  // switch
			passDown(evt);          // Pass on to the layer below us
		}
		


		/// <summary>
		/// Resets and removes all sender and receiver windows.
		/// </summary>
		private void removeAllConnections() 
		{
			lock(connections) 
			{
				foreach(Entry e in connections.Values)
				{
					e.reset();
				}
				connections.Clear();
			}
		}

		/// <summary>
		/// Returns random initial sequence number between 1 and 100
		/// </summary>
		/// <returns>Random initial sequence number between 1 and 100</returns>
		private long getInitialSeqno() 
		{
			Random r = new Random();
			//return (long)r.Next(0,100);
			return 0;
		}

		/// <summary>
		/// Called by AckSenderWindow to resend messages for which no ACK has been received yet
		/// </summary>
		/// <param name="seqno">Sequence number of Message to retransmit</param>
		/// <param name="msg">Message to retransmit</param>
		public void retransmit(long seqno, Message msg) 
		{
			Object dst = msg.Destination;
			if(Trace.trace)
				Trace.info("UNICAST.retransmit()", "[" + local_addr + "] --> XMIT(" + dst + ": #" + seqno + ")");
			passDown(new Event(Event.MSG, msg));
		}

		/// <remarks>
		/// Check whether the hashtable contains an entry e for <code>sender</code> (create if not). If
		/// e.received_msgs is null and <code>first</code> is true: create a new AckReceiverWindow(seqno) and
		/// add message. Set e.received_msgs to the new window. Else just add the message. If first is false,
		/// but we don't yet have hashtable.received_msgs, then just discard the message. If first is true, but
		/// hashtable.received_msgs already exists, also discard the message (redundant message).
		/// </remarks>
		/// <summary>
		/// Processes a unicast Message once received
		/// </summary>
		/// <param name="sender">Sender of the Message</param>
		/// <param name="seqno">Sequence number of received Message</param>
		/// <param name="first">True if first message from sender</param>
		/// <param name="msg">Message received</param>
		void handleDataReceived(Object sender, long seqno, bool first, Message msg) 
		{
			Entry    entry;
			Message  m;  

			if(Trace.trace) 
				Trace.info("UNICAST.handleDataReceived()", "[" + local_addr + "] <-- DATA(" + sender + ": #" + seqno + ", first=" + first);
	
			entry=(Entry)connections[sender];
			if(entry == null) 
			{
				entry = new Entry(this);
				connections.Add(sender, entry);
			}

			if(entry.received_msgs == null) 
			{
				if(first)
					entry.received_msgs = new AckReceiverWindow(seqno);
				else 
				{
					if(operational) 
					{
						if(Trace.trace)
							Trace.warn("UNICAST.handleDataReceived()", "[" + local_addr + "] seqno " + seqno + " from " + 
								sender + " is not tagged as the first message sent by " + sender + 
								"; however, the table for received messages from " + sender + 
								" is still null ! We probably haven't received the first message from " 
								+ sender + " ! Discarding message (operational=" + operational + ")");
						return;
					}
				}
			}

			if(entry.received_msgs != null) 
			{
				entry.received_msgs.add(seqno, msg);
        
				// Try to remove (from the AckReceiverWindow) as many messages as possible as pass them up
				while((m=entry.received_msgs.remove()) != null)
					passUp(new Event(Event.MSG, m));
			}
		}

		/// <summary>
		/// Add the acknowledgement (ACK) to hashtable.sender.sent_msgs
		/// </summary>
		/// <param name="sender">Sender of the Message</param>
		/// <param name="seqno">Sequence number of received Message</param>
		void handleAckReceived(Object sender, long seqno) 
		{
			Entry           entry;
			AckSenderWindow win;

			if(Trace.trace) 
				Trace.info("UNICAST.handleAckReceived()", "[" + local_addr + "] <-- ACK(" + sender + ": #" + seqno + ")");
			entry=(Entry)connections[sender];
			if(entry == null || entry.sent_msgs == null) 
			{
				return;
			}
			win=entry.sent_msgs;
			win.ack(seqno); // removes message from retransmission
		}

		/// <summary>
		/// Sends an acknowledgement (ACK) to the sender of the message
		/// </summary>
		/// <param name="dst">Sender of the Message</param>
		/// <param name="seqno">Sequence number of received Message</param>
		private void sendAck(Address dst, long seqno) 
		{
			Message ack=new Message(dst, null, null);
			ack.putHeader(getName(), new UnicastHeader(UnicastHeader.DATA_ACK, seqno));
			if(Trace.trace)
				Trace.info("UNICAST.sendAck()", "[" + local_addr + "] --> ACK(" + dst + ": #" + seqno + ")");
			passDown(new Event(Event.MSG, ack));
		}

   
	}
}
