using System;
using System.Collections;

using GCT.Util;

namespace GCT.Stack
{
	/// <remarks>
	/// Keeps track of messages according to their sequence numbers. Allows
	/// messages to be added out of order, and with gaps between sequence numbers.
	/// Method <code>remove()</code> removes the first message with a sequence
	/// number that is 1 higher than <code>next_to_remove</code> (this variable is
	/// then incremented), or it returns null if no message is present, or if no
	/// message's sequence number is 1 higher.
	/// <p>
	/// When there is a gap upon adding a message, its seqno will be added to the 
	/// Retransmitter, which (using a timer) requests retransmissions of missing
	/// messages and keeps on trying until the message has been received, or the
	/// member who sent the message is suspected.
	/// </p>
	/// </remarks>
	/// <summary>
	/// Negative Acknowledgement receiver window. Detects gaps and request retransmission.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class NakReceiverWindow 
	{
		/// <summary>
		/// Maintains association between sequence number and message
		/// </summary>
		private class Entry 
		{
			/// <summary>Sequence number</summary>
			public long seqno = 0;
			/// <summary>Associated Message</summary>
			public Message msg = null;

			/// <summary>
			/// Constructor
			/// </summary>
			public Entry() 
			{
				seqno = 0;
				msg   = null;
			}

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="seqno">Sequence number</param>
			/// <param name="msg">Associated Message</param>
			public Entry(long seqno, Message msg) 
			{
				this.seqno = seqno;
				this.msg   = msg;
			}

			/// <summary>
			/// Returns a deep-copy of the Entry
			/// </summary>
			/// <returns>A deep-copy of the Entry</returns>
			public Entry copy() 
			{
				Entry retval = new Entry();
				retval.seqno = seqno;
				if(msg != null) retval.msg=msg.copy();
				return retval;
			}

			/// <summary>
			/// Returns a string representation of the Entry
			/// </summary>
			/// <returns>A string representation of the Entry</returns>
			public String toString() 
			{
				String ret = seqno.ToString();
				if(msg == null) 
					ret += "-";
				else
				{
					ret += "+";
					//ret += msg.ToString();
				}
				return ret;
			}
		}

        /// <summary>The sender that this window is associated with</summary>
		private Address sender = null;
		/// <summary>Maintains synchronisation throughout the window</summary>
		private RWLock rwlock = new RWLock();
		/// <summary>Keep track of *next* seqno to remove and highest received</summary>
		private long head = 0;
		/// <summary>Last seqno received</summary>
		private long tail = 0;
		/// <summary>lowest seqno delivered so far</summary>
		private long lowest_seen = 0;
		/// <summary>highest deliverable (or delivered) seqno so far</summary>
		private long highest_seen = 0;
		/// <summary>List received Entrys (i.e. seqno + message)</summary>
		private ArrayList msgs = new ArrayList();
		/// <summary>Messages delivered; list of Entries</summary>
		private ArrayList delivered_msgs = new ArrayList();
		/// <summary>Method to call for retransmission</summary>
		private Retransmitter.RetransmitCommand cmd = null;
		/// <summary>Retransmitter of missing messages</summary>
		private Retransmitter retransmitter = null;

		/// <summary>
		/// Constructor: Creates a new instance with the given retransmit command
		/// </summary>
		/// <param name="sender">The sender associated with this instance</param>
		/// <param name="cmd">The command used to retransmit a missing message</param>
		/// <param name="start_seqno">The first sequence number to be received</param>
		/// <param name="sched">The external scheduler to use for retransmission</param>
		public NakReceiverWindow(Address sender, Retransmitter.RetransmitCommand cmd,
			long start_seqno, TimeScheduler sched) 
		{
			this.sender = sender;
			this.cmd    = cmd;
			head        = start_seqno;
			tail        = head;

			if (cmd != null) 
				retransmitter = sched==null ?
								 new Retransmitter(sender, cmd):
								 new Retransmitter(sender, cmd, sched);
		}

		/// <summary>
		/// Constructor: Creates a new instance with the given retransmit command
		/// </summary>
		/// <param name="sender">The sender associated with this instance</param>
		/// <param name="cmd">The command used to retransmit a missing message</param>
		/// <param name="start_seqno">The first sequence number to be received</param>
		public NakReceiverWindow(Address sender, Retransmitter.RetransmitCommand cmd, long start_seqno) : this(sender, cmd, start_seqno, null)
		{
		}

		/// <summary>
		/// Sets the invervals between retransmit requests
		/// </summary>
		/// <param name="timeouts">Invervals between retransmit requests</param>
		public void setRetransmitTimeouts(long[] timeouts) 
		{
			if(retransmitter != null)
				retransmitter.setRetransmitTimeouts(timeouts);
		}

		/// <remarks>
		/// Variables <code>head</code> and <code>tail</code> mark the start and
		/// end of the messages received, but not delivered yet. When a message is
		/// received, if its seqno is smaller than <code>head</code>, it is
		/// discarded (already received). If it is bigger than <code>tail</code>,
		/// we advance <code>tail</code> and add empty elements. If it is between
		/// <code>head</code> and <code>tail</code>, we set the corresponding
		/// missing (or already present) element. If it is equal to
		/// <code>tail</code>, we advance the latter by 1 and add the message
		/// (default case).
		/// </remarks>
		/// <summary>
		/// Adds a message according to its sequence number.
		/// </summary>
		/// <param name="seqno">Sequence number of the message.</param>
		/// <param name="msg">Message to add.</param>
		public void add(long seqno, Message msg) 
		{
			Entry current = null;
			long  old_tail;

			rwlock.writeLock();
			try 
			{
				old_tail = tail;
				if(seqno < head) 
				{
					if(Trace.trace)
						Trace.info("NakReceiverWindow.add()", "seqno " + seqno + 
										 " is smaller than " + head + "); discarding message");
					return;
				}

				// add at end (regular expected msg)
				if(seqno == tail) 
				{
					msgs.Add(new Entry(seqno, msg));
					tail++;
				}
					// gap detected
					// i. add placeholders, creating gaps
					// ii. add real msg
					// iii. tell retransmitter to retrieve missing msgs
				else if(seqno > tail) 
				{
					for(long i = tail; i < seqno; i++) 
					{
						msgs.Add(new Entry(i, null));
						tail++;
					}
					msgs.Add(new Entry(seqno, msg));
					tail = seqno + 1;
					if(retransmitter != null) 
					{
						retransmitter.add(old_tail, seqno-1);
					}
					// finally received missing message
				}
				else if(seqno < tail) 
				{
					if(Trace.trace)
						Trace.info("NakReceiverWindow.add()", "added missing msg " + msg.Source + "#" + seqno);
				
					for(int i = 0 ; i < msgs.Count; i++)
					{
						current = (Entry)msgs[i];
						// overwrite any previous message (e.g. added by down()) and
						// remove seqno from retransmitter
						if(seqno == current.seqno) 
						{
							current.msg = msg;
							if(retransmitter != null)
								retransmitter.remove(seqno);
							break;
						}
					}
					
				}
				_updateLowestSeen();
				_updateHighestSeen();
			}
			finally 
			{
				rwlock.writeUnlock();
			}
		}

		/// <summary>
		/// Returns the next received message.
		/// </summary>
		/// <returns>The next received message. May return null indicating a gap</returns>
		public Message remove() 
		{
			Entry   e= null;
			Message retval = null;

			rwlock.writeLock();
			try 
			{
				if(msgs.Count > 0)
					e = (Entry)msgs[0];
				if((e != null) && (e.msg != null)) 
				{
					retval = e.msg;
					msgs.RemoveAt(0);
					delivered_msgs.Add(e.copy());
					head++;
				}
				return retval;
			} 
			finally { rwlock.writeUnlock(); }
		}

		/// <remarks>
		/// A message is stable when it has been received by all members
		/// </remarks>
		/// <summary>
		/// Deletes all messages that are less than the seqno.
		/// </summary>
		/// <param name="seqno"></param>
		public void stable(long seqno) 
		{
			rwlock.writeLock();
			try 
			{
				while(delivered_msgs.Count > 0)
				{
					if(((Entry)delivered_msgs[0]).seqno > seqno)
						break;
					else
						delivered_msgs.RemoveAt(0);
				}
				_updateLowestSeen();
				_updateHighestSeen();
			} 
			finally { rwlock.writeUnlock(); }
		}

		/// <summary>
		/// Reset the retransmitter and the nak window
		/// </summary>
		public void reset() 
		{
			rwlock.writeLock(); 
			try 
			{
				if(retransmitter != null)
					retransmitter.reset();
				_reset();
			} 
			finally { rwlock.writeUnlock(); }
		}

		/// <summary>
		/// Stop the retransmitter and reset the nak window
		/// </summary>
		public void destroy() 
		{
			rwlock.writeLock(); 
			try 
			{
				if(retransmitter != null)
					retransmitter.stop();
				_reset();
			} 
			finally { rwlock.writeUnlock(); }
		}

		/// <summary>
		/// Return the highest sequence number of a message consumed by the application
		/// </summary>
		/// <returns>The highest sequence number of a message consumed by the application</returns>
		public long getHighestDelivered() 
		{
			rwlock.readLock(); 
			try 
			{
				return(Math.Max(head-1, -1));
			} 
			finally { rwlock.readUnlock(); }
		}


		/// <summary>
		/// Returns the lowest sequence number of a message that has been
		/// delivered or is a candidate for delivery
		/// </summary>
		/// <returns>The lowest sequence number of a message that has been
		/// delivered or is a candidate for delivery</returns>
		public long getLowestSeen() 
		{
			rwlock.readLock(); 
			try 
			{
				return(lowest_seen);
			} 
			finally { rwlock.readUnlock(); }
		}

		/// <summary>
		/// Returns the highest deliverable seqno
		/// </summary>
		/// <returns></returns>
		public long getHighestSeen() 
		{
			rwlock.readLock(); 
			try 
			{
				return(highest_seen);
			} 
			finally { rwlock.readUnlock(); }
		}

		/// <summary>
		/// Find all messages between, and including, 'low' and 'high' that have a null msg.
		/// </summary>
		/// <param name="low">Lowest seqno of interest</param>
		/// <param name="high">Highest seqno of interest</param>
		/// <returns>A list of integers, sorted in ascending order.</returns>
		public ArrayList getMissingMessages(long low, long high) 
		{
			ArrayList  retval = new ArrayList();
			Entry entry;
			long  my_high;

			if(low > high) 
			{
				if(Trace.trace)
					Trace.error("NakReceiverWindow.getMissingMessages()", "invalid range: low (" + low +
						") is higher than high (" + high + ")");
				return null;
			}

			rwlock.readLock();
			try 
			{

				my_high = Math.Max(head-1, 0);
				// check only received messages, because delivered messages *must*
				// have a non-null msg
				for(int i = 0 ; i < msgs.Count; i++)
				{
					entry = (Entry)msgs[i];
					if((entry.seqno >= low) && (entry.seqno <= high) &&
						(entry.msg == null))
						retval.Add((long)entry.seqno);
				}

				if(msgs.Count > 0) 
				{
					entry = (Entry)msgs[0];
					if(entry != null)
						my_high = entry.seqno;
				}
				for(long i = my_high+1; i <= high; i++)
					retval.Add((long)i);

				return(retval.Count == 0? null:retval);

			} 
			finally { rwlock.readUnlock(); }
		}


		/// <remarks>
		/// Returns the highest sequence number received so far (which may be
		/// higher than the highest seqno <em>delivered</em> so far, e.g. for
		/// 1,2,3,5,6 it would be 6
		/// </remarks>
		/// <summary>
		/// Returns the highest sequence number received so far.
		/// </summary>
		/// <returns></returns>
		public long getHighestReceived() 
		{
			rwlock.readLock(); 
			try 
			{
				return Math.Max(tail-1, -1);
			} 
			finally { rwlock.readUnlock(); }
		}

		/// <summary>
		/// Number of messages received
		/// </summary>
		/// <returns>Number of messages received</returns>
		public int size() 
		{
			rwlock.readLock();
			try 
			{
				return msgs.Count;
			} 
			finally { rwlock.readUnlock(); }
		}

		/// <summary>
		/// String representation of the Receiver Window
		/// </summary>
		/// <returns>String representation of the Receiver Window</returns>
		public String toString() 
		{
			String sb = "";
			rwlock.readLock();
			try 
			{
				sb += "delivered_msgs: ";
				for(int i= 0;i<delivered_msgs.Count;i++)
					sb += ((Entry)delivered_msgs[i]).toString();

				sb += "\nreceived_msgs: ";
				for(int i= 0;i<msgs.Count;i++)
					sb += ((Entry)msgs[i]).toString();
			} 
			finally { rwlock.readUnlock(); }

			return sb;
		}

		/* ------------------------------- Private Methods -------------------------------------- */
		
		/// <summary>
		/// Updates the lowest seen sequence number
		/// </summary>
		private void _updateLowestSeen() 
		{
			Entry entry = null;

			// If both delivered and received messages are empty, let the highest
			// seen seqno be the one *before* the one which is expected to be
			// received next by the NakReceiverWindow (head-1)
			if((delivered_msgs.Count == 0) && (msgs.Count == 0)) 
			{
				lowest_seen = 0;
				return;
			}

			// Else let is be the first of the delivered messages
			// get last seqno of delivered messages
			//entry = (Entry)delivered_msgs[0];
			if(delivered_msgs.Count != 0)//entry != null)
			{
				entry = (Entry)delivered_msgs[0];
				lowest_seen = entry.seqno;
			}
			else 
			{
				if(msgs.Count != 0) 
				{
					entry = (Entry)msgs[0];
					if((entry != null) && (entry.msg != null))
						lowest_seen = entry.seqno;
				}
			}
		}

		/// <remarks>
		/// Find the highest seqno that is deliverable or was actually delivered.
		/// Returns seqno-1 if there are no messages in the queues (the first
		/// message to be expected is always seqno).
		/// </remarks>
		/// <summary>
		/// Updates the highest seen sequence number
		/// </summary>
		private void _updateHighestSeen() 
		{
			long  ret   = 0;
			Entry entry = null;

			// If both delivered and received messages are empty, let the highest
			// seen seqno be the one *before* the one which is expected to be
			// received next by the NakReceiverWindow (head-1)
			if((delivered_msgs.Count == 0) && (msgs.Count == 0)) 
			{
				highest_seen = 0;
				return;
			}
			// Else let is be the last of the delivered messages, to start with,
			// or again the one before the first seqno expected (if no delivered
			// msgs)
			// Get last seqno of delivered messages
			//entry = (Entry)delivered_msgs[0];
			if(delivered_msgs.Count != 0)//entry != null) 
			{
				entry = (Entry)delivered_msgs[0];
				ret = entry.seqno;
			}
			else 
				ret = Math.Max(head-1, 0);

			// Now check the received msgs head to tail. if there is an entry
			// with a non-null msg, increment ret until we find an entry with
			// a null msg
			for(int i = 0;i<msgs.Count;i++)
			{
				entry = (Entry)msgs[i];
				if(entry.msg != null) ret = entry.seqno;
				else break;
			}
			highest_seen = Math.Max(ret, 0);
		}


		/// <remarks>
		/// <list type="">
		/// <item>i. Delete all received entries</item>
		/// <item>ii. Delete alll delivered entries</item>
		/// <item>Reset all indices (head, tail, etc.)</item>
		/// </list>
		/// </remarks>
		/// <summary>
		/// Reset the Nak window.
		/// </summary>
		private void _reset() 
		{
			msgs.Clear();
			delivered_msgs.Clear();
			head         = 0;
			tail         = 0;
			lowest_seen  = 0;
			highest_seen = 0;
		}
		/* --------------------------- End of Private Methods ----------------------------------- */


	}
}
