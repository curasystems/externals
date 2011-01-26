using System;
using System.Collections;
using System.Threading;

using GCT.Util;

namespace GCT.Stack
{
	/// <remarks>
	/// Maintains a pool of sequence numbers of messages that need to be retransmitted. Messages
	/// are aged and retransmission requests sent according to age (linear backoff used). If a
	/// TimeScheduler instance is given to the constructor, it will be used, otherwise Reransmitter
	/// will create its own. The retransmit timeouts have to be set first thing after creating an instance.
	/// The <code>add()</code> method adds a range of sequence numbers of messages to be retransmitted. The
	/// <code>remove()</code> method removes a sequence number again, cancelling retransmission requests for it.
	/// Whenever a message needs to be retransmitted, the <code>RetransmitCommand.retransmit()</code> method is called.
	/// It can be used e.g. by an ack-based scheme (e.g. AckSenderWindow) to retransmit a message to the receiver, or
	/// by a nak-based scheme to send a retransmission request to the sender of the missing message.
	/// </remarks>
	/// <summary>
	/// Maintains a pool of sequence numbers of messages that need to be retransmitted.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban, John Giorgiadis</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class Retransmitter 
	{
		/// <summary>Default retransmit intervals (ms) - exponential approx.</summary>
		private static long[] RETRANSMIT_TIMEOUTS = { 2000, 3000, 5000, 8000 };
		/// <summary>Default retransmit thread suspend timeout (ms).</summary>
		private static long SUSPEND_TIMEOUT = 2000;

		/// <summary>Address of the sender.</summary>
		private Address				sender = null;
		/// <summary>List of all messages received</summary>
		private ArrayList			msgs = new ArrayList();
		/// <summary>Method called to retransmit</summary>
		private RetransmitCommand	cmd = null;
		/// <summary>Signifys that the Scheduler is owned internally</summary>
		private bool				retransmitter_owned;
		/// <summary></summary>
		private TimeScheduler		retransmitter = null;

		/// <summary>
		/// Retransmit command used to retrieve missing messages
		/// </summary>
		public interface RetransmitCommand 
		{
			/// <summary>
			/// Get the missing messages between sequence numbers
			/// <code>first_seqno</code> and <code>last_seqno</code>. This can either be done by sending a
			/// retransmit message to destination <code>sender</code> (nak-based scheme), or by
			/// retransmitting the missing message(s) to <code>sender</code> (ack-based scheme).
			/// </summary>
			/// <param name="first_seqno">The sequence number of the first missing message</param>
			/// <param name="last_seqno">The sequence number of the last missing message</param>
			/// <param name="sender">The destination of the member to which the retransmit request will be sent</param>
			void retransmit(long first_seqno, long last_seqno, Address sender);
		}

		/// <summary>
		/// Constructor: Create a new Retransmitter associated with the given sender address
		/// </summary>
		/// <param name="sender">The address from which retransmissions are expected or to which retransmissions are sent</param>
		/// <param name="cmd">The retransmission callback reference</param>
		/// <param name="sched">The retransmissions scheduler</param>
		public Retransmitter(Address sender, RetransmitCommand cmd, TimeScheduler sched) 
		{
			init(sender, cmd, sched, false);
		}

		/// <summary>
		/// Constructor: Create a new Retransmitter associated with the given sender address
		/// </summary>
		/// <param name="sender">The address from which retransmissions are expected or to which retransmissions are sent</param>
		/// <param name="cmd">The retransmission callback reference</param>
		public Retransmitter(Address sender, RetransmitCommand cmd) 
		{
			init(sender, cmd, new TimeScheduler(SUSPEND_TIMEOUT), true);
		}

		/// <summary>
		/// Sets the retransmission timeouts
		/// </summary>
		/// <param name="timeouts"></param>
		public void setRetransmitTimeouts(long[] timeouts) 
		{
			if(timeouts != null)
				RETRANSMIT_TIMEOUTS=timeouts;
		}

		/// <summary>
		/// Add the given range [first_seqno, last_seqno] in the list of
		/// entries eligible for retransmission. If first_seqno > last_seqno,
		/// then the range [last_seqno, first_seqno] is added instead
		/// </summary>
		/// <param name="first_seqno">First sequence number to retransmit</param>
		/// <param name="last_seqno">Last sequence number to retransmit</param>
		public void add(long first_seqno, long last_seqno) 
		{
			Entry e;
	
			if(first_seqno > last_seqno) 
			{
				long tmp    = first_seqno;
				first_seqno = last_seqno;
				last_seqno  = tmp;
			}
			lock(msgs) 
			{
				e = new Entry(first_seqno, last_seqno, RETRANSMIT_TIMEOUTS, sender,cmd);
				msgs.Add(e);
				retransmitter.add(e);
			}
		}

		/// <summary>
		/// Remove the given sequence number from the list of seqnos eligible
		/// for retransmission. If there are no more seqno intervals in the
		/// respective entry, cancel the entry from the retransmission
		/// scheduler and remove it from the pending entries
		/// </summary>
		/// <param name="seqno">Sequence number to remove</param>
		public void remove(long seqno) 
		{
			Entry e;

			lock(msgs) 
			{
				for(int i = 0; i < msgs.Count; i++) 
				{
					e = (Entry)msgs[i];
					lock(e) 
					{
						if(seqno < e.low || seqno > e.high) continue;
						e.remove(seqno);
						if (e.low > e.high) 
						{
							e.cancel();
							msgs.RemoveAt(i);
						}
					}
					break;
				}
			}
		}

		/// <summary>
		/// Reset the retransmitter: clear all msgs and cancel all the
		/// respective tasks
		/// </summary>
		public void reset() 
		{
			Entry entry;

			lock(msgs) 
			{
				for (int i = 0; i < msgs.Count; ++i) 
				{
					entry = (Entry)msgs[i];
					entry.cancel();
				}
				msgs.Clear();
			}
		}

		/// <remarks>
		/// If this retransmitter has been provided an externally managed
		/// scheduler, then just clear all msgs and the associated tasks, else
		/// stop the scheduler. In this case the method blocks until the
		/// scheduler's thread is dead. Only the owner of the scheduler should
		/// stop it.
		/// </remarks>
		/// <summary>
		/// Stop the retransmission and clear all pending msgs.
		/// </summary>
		public void stop() 
		{
			Entry entry;

			// i. If retransmitter is owned, stop it else cancel all tasks
			// ii. Clear all pending msgs
			lock(msgs) 
			{
				if (retransmitter_owned) 
				{
					try 
					{
						retransmitter.stop();
					} 
					catch(ThreadInterruptedException ex) 
					{
						if(Trace.trace)
							Trace.error("Retransmiter.stop()", ex.StackTrace);
					}
				} 
				else 
				{
					for (int i = 0; i < msgs.Count; ++i) 
					{
						entry = (Entry)msgs[i];
						entry.cancel();
					}
				}
				msgs.Clear();
			}
		}

		/* ------------------------------- Private Methods -------------------------------------- */
		/// <summary>
		/// Initialises this object
		/// </summary>
		/// <param name="sender">The address from which retransmissions are expected</param>
		/// <param name="cmd">The retransmission callback reference</param>
		/// <param name="sched">Schedular for retransmit tasks</param>
		/// <param name="sched_owned">sched_owned whether the scheduler parameter is owned by this object or is externally provided</param>
		private void init(Address sender, RetransmitCommand cmd, TimeScheduler sched, bool sched_owned) 
		{
			this.sender         = sender;
			this.cmd            = cmd;
			retransmitter_owned = sched_owned;
			retransmitter       = sched;
		}
		/* ---------------------------- End of Private Methods ------------------------------------ */

		/// <summary>
		/// The retransmit task executed by the scheduler in regular intervals
		/// </summary>
		private abstract class Task : TimeScheduler.Task 
		{
			/// <summary>Intervals for transmission</summary>
			private Interval	intervals;
			/// <summary>Determines if Task is running</summary>
			private bool		stopped;
			/// <summary>Name of task</summary>
			private String		name;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="intervals">Intervals betweening sending message</param>
			protected Task(long[] intervals) 
			{
				this.intervals = new Interval(intervals);
				this.stopped = false;
				name = "Task";
			}
			/// <summary>Returns the next interval</summary>
			/// <returns>The next interval</returns>
			public long nextInterval() { return(intervals.Next()); }

			/// <summary>Checks if Task is cancelled</summary>
			/// <returns>True, if Task is stopped</returns>
			public bool cancelled() { return(stopped); }

			/// <summary>Cancels the Task</summary>
			public void cancel() { stopped = true; }

			/// <summary>Returns the name of the Task</summary>
			/// <returns>The name of the Task</returns>
			public String getName() { return name; }

			/// <summary>Retransmits the messages</summary>
			public abstract void run();
		}
	    
		/// <remarks>
		/// Groups are stored in a list as long[2] arrays of the each group's
		/// bounds. For speed and convenience, the lowest and highest bounds of
		/// all the groups in this entry are also stored separately
		/// <p>
		/// For Example: <br></br>
		/// - initial group: [5-34]
		/// - msg 12 is acknowledged, now the groups are: [5-11], [13-34]</p>
		/// </remarks>
		/// <summary>
		/// The entry associated with an initial group of missing messages
		/// with contiguous sequence numbers and with all its subgroups.
		/// </summary>
		private class Entry : Task 
		{
			/// <summary>Lowest sequence number in the Entry</summary>
			public long low;
			/// <summary>Highest sequence number in the Entry</summary>
			public long high;
			/// <summary>List of groups of sequence numbers</summary>
			public ArrayList list;
			/// <summary>Sender of the messages</summary>
			public Address initialSender;
			/// <summary>Method to call when retransmitting</summary>
			public RetransmitCommand retCmd;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="low">Lowest sequence number in the Entry</param>
			/// <param name="high">Highest sequence number in the Entry</param>
			/// <param name="intervals">Time intervals between retransmission</param>
			/// <param name="initialSender">Sender of the messages</param>
			/// <param name="retCmd">Method to call when retransmitting</param>
			public Entry(long low, long high, long[] intervals, Address initialSender, RetransmitCommand retCmd) : base(intervals)
			{
				this.low  = low;
				this.high = high;
				this.initialSender = initialSender;
				this.retCmd = retCmd;

				list      = new ArrayList();
				list.Add(new long[]{low,high});
			}

			/// <remarks>
			/// Remove the given seqno and resize or partition groups as
			/// necessary. The algorithm is as follows:
			/// <p>
			/// i. Find the group with low 'less than' seqno 'less than' high<br></br>
			/// ii. If seqno == low,<br></br>
			///	a. if low == high, then remove the group
			/// Adjust global low. If global low was pointing to the group
			/// deleted in the previous step, set it to point to the next group.
			/// If there is no next group, set global low to be higher than
			/// global high. This way the entry is invalidated and will be removed
			/// all together from the pending msgs and the task scheduler
			/// iii. If seqno == high, adjust high, adjust global high if this is
			/// the group at the tail of the list<br></br>
			/// iv. Else low less than seqno less than high, break [low,high] into [low,seqno-1]
			/// and [seqno+1,high]
			/// </p>
			/// </remarks>
			/// <summary>
			/// Remove the given seqno
			/// </summary>
			/// <param name="seqno">Sequence number to remove</param>
			public void remove(long seqno) 
			{
				int i;
				long[] bounds, newBounds;

				bounds = null;
				lock(this) 
				{
					for (i = 0; i < list.Count; ++i) 
					{
						bounds = (long[])list[i];
						if (seqno < bounds[0] || seqno > bounds[1]) continue;
						break;
					}
					if (i == list.Count)
						return;

					if (seqno == bounds[0]) 
					{
						if (bounds[0] == bounds[1]) list.RemoveAt(i);
						else bounds[0]++;
						if (i == 0)
							low = list.Count==0? high+1:((long[])list[i])[0];
					} 
					else if (seqno == bounds[1]) 
					{
						bounds[1]--;
						if (i == list.Count-1) high = ((long[])list[i])[1];
					} 
					else 
					{
						newBounds    = new long[2];
						newBounds[0] = seqno + 1;
						newBounds[1] = bounds[1];
						bounds[1]    = seqno - 1;
						//list.Add(i+1, newBounds);
						list.Insert(i+1, newBounds);
					}
				}
			}

			/// <summary>
			/// For each interval, call the retransmission callback command
			/// </summary>
			public override void run() 
			{
				long[] bounds;

				lock(this) 
				{
					for (int i = 0; i < list.Count; ++i) 
					{
						bounds = (long[])list[i];
						retCmd.retransmit(bounds[0], bounds[1], initialSender);
					}
				}
			}
		} // end class Entry

		/// <summary>
		/// Contains a series of intervals to wait between transmissions
		/// </summary>
		private class Interval 
		{
			/// <summary>Next Interval to use</summary>
			private int    next=0;
			/// <summary>Array of transmission intervals</summary>
			private long[] interval=null;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="interval">Array of transmission intervals</param>
			public Interval(long[] interval) 
			{
				if (interval.Length == 0)
					throw new FormatException("Interval() Length == 0");
				this.interval=interval;
			}

			/// <summary>
			/// Returns first interval in the array
			/// </summary>
			/// <returns>First interval in the array</returns>
			public long first() { return interval[0]; }
    
			/// <summary>
			/// Returns next interval in the array
			/// </summary>
			/// <returns>Next interval in the array</returns>
			public long Next() 
			{
				lock(this)
				{
					if (next >= interval.Length)
						return(interval[interval.Length-1]);
					else
						return(interval[next++]);
				}
			}
    
			/// <summary>
			/// Returns all the intervals
			/// </summary>
			/// <returns>All the intervals</returns>
			public long[] getInterval() { return interval; }

			/// <summary>
			/// Resets the position of the next interval to the first interval
			/// </summary>
			public void reset() 
			{
				lock(this)
				{
					next = 0; 
				}
			}
		}
	}
}
