using System;
using System.Collections;

using GCT.Util;

namespace GCT.Stack
{
	/// <remarks>
	/// Messages are added to the window keyed by seqno
	/// When an ACK is received, the corresponding message is removed. The Retransmitter
	/// continously iterates over the entries in the hashmap, retransmitting messages based on their
	/// creation time and an (increasing) timeout. When there are no more messages in the retransmission
	/// table left, the thread terminates. It will be re-activated when a new entry is added to the
	/// retransmission table.
	/// </remarks>
	/// <summary>
	/// ACK-based sliding window for a sender.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class AckSenderWindow : Retransmitter.RetransmitCommand 
	{
		/// <summary>Called to request XMIT of message</summary>
		RetransmitCommand	retransmit_command=null;
		/// <summary>Collection of sent messages.</summary>
		Hashtable			msgs = new Hashtable();		// keys: seqnos (Long), values: Messages
		/// <summary>Intervals to wait between sending</summary>
		long[]				interval = new long[]{1000,2000,3000,4000};
		/// <summary>Sends retransmit requests</summary>
		Retransmitter		retransmitter = null;
		/// <summary>Stores messages is msgs is 'full'</summary>
		MQueue				msg_queue = new MQueue();	// for storing messages if msgs is full
		/// <summary>The max number of messages in the window, when exceeded messages will be queued</summary>
		int					window_size = -1;			// the max size of msgs, when exceeded messages will be queued
		/// <summary>When queueing, after msgs size falls below this value, msgs are added again (queueing stops)</summary>
		int					min_threshold = -1;
		/// <summary>Enables use of a sliding window</summary>
		bool				use_sliding_window = false;
		/// <summary>If set there are queued messages</summary>
		bool				queueing = false;
		/// <summary>Protocol to send messages through</summary>
		Protocol			transport = null;			// used to send messages

		/// <summary>
		/// Called when the a message should be re-sent. Should be implemented by
		/// the Protocol using AckSenderWindow.
		/// </summary>
		public interface RetransmitCommand 
		{
			/// <summary>
			/// Called when <c>AckSenderWindow.retransmit()</c> is caled from the Retransmitter.
			/// </summary>
			/// <param name="seqno">Sequence Number to be resent</param>
			/// <param name="msg">Message to be resent</param>
			void retransmit(long seqno, Message msg);
		}

		/// <summary>
		/// Creates a new instance.
		/// </summary>
		/// <param name="com">
		/// If not null, its method <code>retransmit()</code> will be called when a message
		/// needs to be retransmitted (called by the Retransmitter).
		/// </param>
		public AckSenderWindow(RetransmitCommand com) 
		{
			retransmitter = new Retransmitter(null, this);
			retransmit_command = com;
			retransmitter.setRetransmitTimeouts(interval);	
		}


		/// <summary>
		/// Allows the intervals between retransmission to be specified.
		/// </summary>
		/// <param name="com">
		/// If not null, its method <code>retransmit()</code> will be called when a message
		/// needs to be retransmitted (called by the Retransmitter).
		/// </param>
		/// <param name="interval">Array of intervals between retransmissions</param>
		public AckSenderWindow(RetransmitCommand com, long[] interval) 
		{
			retransmitter=new Retransmitter(null, this);
			retransmit_command=com;
			this.interval=interval;
			retransmitter.setRetransmitTimeouts(interval);
		}

		/// <summary>
		/// This constructor whould be used when we want AckSenderWindow to send the message added
		/// by add(), rather then ourselves
		/// </summary>
		/// <param name="com">Method to be called when transmission occurs</param>
		/// <param name="interval">Array of intervals between retransmissions</param>
		/// <param name="transport">The Protocol that should be used to <code>passDown</code> the message</param>
		public AckSenderWindow(RetransmitCommand com, long[] interval, Protocol transport) 
		{
			retransmitter=new Retransmitter(null, this);
			retransmit_command=com;
			this.interval=interval;
			this.transport=transport;
			retransmitter.setRetransmitTimeouts(interval);
		}

		/// <summary>
		/// Sets the size of the sender window
		/// </summary>
		/// <param name="window_size"></param>
		/// <param name="min_threshold"></param>
		public void setWindowSize(int window_size, int min_threshold) 
		{
			this.window_size=window_size;
			this.min_threshold=min_threshold;

			// sanity tests for the 2 values:
			if(min_threshold > window_size) 
			{
				this.min_threshold=window_size;
				this.window_size=min_threshold;
				if(Trace.trace)
					Trace.warn("AckSenderWindow.setWindowSize()", "min_threshold (" + min_threshold +
					") has to be less than window_size ( " + window_size + "). Values are swapped");
			}
			if(this.window_size <= 0) 
			{
				this.window_size=this.min_threshold > 0? (int)(this.min_threshold * 1.5) : 500;
				if(Trace.trace)
					Trace.warn("AckSenderWindow.setWindowSize()", "window_size is <= 0, setting it to " + this.window_size);
			}
			if(this.min_threshold <= 0) 
			{
				this.min_threshold=this.window_size > 0? (int)(this.window_size * 0.5) : 250;
				if(Trace.trace)
					Trace.warn("AckSenderWindow.setWindowSize()", "min_threshold is <= 0, setting it to " + this.min_threshold);
			}

			if(Trace.trace)
				Trace.info("AckSenderWindow.setWindowSize()", "window_size=" + this.window_size +
					", min_threshold=" + this.min_threshold);
			use_sliding_window=true;
		}

		/// <summary>
		/// Clears all the messages in the sender window and resets the retransmitter
		/// </summary>
		public void reset() 
		{
			lock(msgs) 
			{
				msgs.Clear();
			}
			retransmitter.reset();
		}


		/// <summary>
		/// Adds a new message to the retransmission table. If the message won't have received an ack within
		/// a certain time frame, the retransmission thread will retransmit the message to the receiver. If
		/// a sliding window protocol is used, we only add up to <code>window_size</code> messages. If the table is
		/// full, we add all new messages to a queue. Those will only be added once the table drains below a certain
		/// threshold (<code>min_threshold</code>)
		/// </summary>
		/// <param name="seqno">The sequence number of the message</param>
		/// <param name="msg">The message to be retransmitted</param>
		public void add(long seqno, Message msg) 
		{
			long tmp= seqno;

			lock(msgs) 
			{
				if(msgs.ContainsKey(tmp))
					return;

				if(!use_sliding_window) 
				{
					addMessage(seqno, tmp, msg);
				}
				else 
				{  // we use a sliding window
					if(queueing)
						addToQueue(seqno, msg);
					else 
					{
						if(msgs.Count +1 > window_size) 
						{
							queueing=true;
							addToQueue(seqno, msg);
							if(Trace.trace)
								Trace.info("AckSenderWindow.add()", "window_size (" + window_size + ") was exceeded, " +
									"starting to queue messages until window size falls under " + min_threshold);
						}
						else 
						{
							addMessage(seqno, tmp, msg);
						}
					}
				}
			}
		}
    

		/// <summary>
		/// Removes the message from <code>msgs</code>, removing them also from retransmission. If
		/// sliding window protocol is used, and was queueing, check whether we can resume adding elements.
		/// Add all elements. If this goes above window_size, stop adding and back to queueing. Else
		/// set queueing to false.
		/// </summary>
		/// <param name="seqno"></param>
		public void ack(long seqno) 
		{
			long     tmp= seqno;
			Entry    entry;

			lock(msgs) 
			{
				msgs.Remove(tmp);
				retransmitter.remove(seqno);
	    
				if(use_sliding_window && queueing) 
				{
					if(msgs.Count < min_threshold) 
					{ // we fell below threshold, now we can resume adding msgs

						if(Trace.trace)
							Trace.info("AckSenderWindow.ack()", "number of messages in table fell " +
								"under min_threshold (" + min_threshold + "): adding " +
								msg_queue.Count + " messages on queue");
		    

						while(msgs.Count < window_size) 
						{
							if((entry=removeFromQueue()) != null)
								addMessage(entry.seqno, (long)entry.seqno, entry.msg);
							else
								break;
						}

						if(msgs.Count +1 > window_size) 
						{
							if(Trace.trace)
								Trace.info("AckSenderWindow.ack()", "exceded window_size (" + window_size +
									") again, will still queue");
							return; // still queueuing
						}
						else
							queueing=false; // allows add() to add messages again
						if(Trace.trace)
							Trace.info("AckSenderWindow.ack()","set queueing to false (table size=" + msgs.Count + ")");
					}
				}
			}
		}

		/* -------------------------------- Retransmitter.RetransmitCommand interface ------------------- */
		/// <summary>
		/// Called by the Retransmitter when a message should be retransmitted.
		/// </summary>
		/// <param name="first_seqno">The first sequence number to be retransmitted</param>
		/// <param name="last_seqno">The last sequence number to be retransmitted. This may be the same as the first if it is only one message.</param>
		/// <param name="sender">The destination naddress of the message.</param>
		public void retransmit(long first_seqno, long last_seqno, Address sender) 
		{
			Message msg;
			if(retransmit_command != null) 
			{
				for(long i=first_seqno; i <= last_seqno; i++) 
				{
					if((msg=(Message)msgs[(long)i]) != null) 
					{
						// find the message to retransmit
						retransmit_command.retransmit(i, msg);
					}
				}
			}
		}
		/* ----------------------------- End of Retransmitter.RetransmitCommand interface ---------------- */

		/* ---------------------------------- Private methods --------------------------------------- */
		/// <summary>
		/// Used internally to add a message to the retransmitter. The message 
		/// will be passed down the stack by the Protocol using this Object.
		/// </summary>
		/// <param name="seqno">Sequence No of the message.</param>
		/// <param name="local_seqno">Local Sequence No of the message.</param>
		/// <param name="msg">Message to be sent and added to the Retransmitter</param>
		private void addMessage(long seqno, long local_seqno, Message msg) 
		{
			if(transport != null)
				transport.passDown(new Event(Event.MSG, msg));
			msgs.Add(local_seqno, msg);
			retransmitter.add(seqno, seqno);
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="seqno"></param>
		/// <param name="msg"></param>
		private void addToQueue(long seqno, Message msg) 
		{
			try 
			{
				msg_queue.Add(new Entry(seqno, msg));
			}
			catch(Exception ex) 
			{
				if(Trace.trace)
					Trace.error("AckSenderWindow.addToQueue()", "exception=" + ex);
			}
		}

		/// <summary>
		/// Removes the first <c>Entry</c> in the message collection.
		/// </summary>
		/// <returns>If the msg queue contains values the first value is returned</returns>
		private Entry removeFromQueue() 
		{
			try 
			{
				return msg_queue.Count == 0? null : (Entry)msg_queue.Remove();
			}
			catch(Exception ex) 
			{
				if(Trace.trace)
					Trace.error("AckSenderWindow.removeFromQueue()", "exception=" + ex);
				return null;
			}


		}
		/* ------------------------------ End of Private methods ------------------------------------ */

		/// <summary>
		/// Class used to store message alongside with its seqno in the message queue
		/// </summary>
		protected class Entry 
		{
			/// <summary>
			/// Sequence number of the message.
			/// </summary>
			public long    seqno;

			/// <summary>
			/// Message associated with the sequence number
			/// </summary>
			public Message msg;

			/// <summary>
			/// Creates a new instance with the specified values.
			/// </summary>
			/// <param name="seqno">Sequence number of the message.</param>
			/// <param name="msg">Message associated with the sequence number</param>
			public Entry(long seqno, Message msg) 
			{
				this.seqno=seqno;
				this.msg=msg;
			}
		}
	}


}

