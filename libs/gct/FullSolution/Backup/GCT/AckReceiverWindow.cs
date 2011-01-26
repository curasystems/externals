using System;
using System.Collections;

namespace GCT.Stack
{
	/// <remarks>
	/// Counterpart of AckSenderWindow.
	/// Every message received is ACK'ed (even duplicates) and added to a hashmap
	/// keyed by seqno. The next seqno to be received is stored in <code>next_to_remove</code>. When a message with
	/// a seqno less than next_to_remove is received, it will be discarded. The <code>remove()</code> method removes
	/// and returns a message whose seqno is equal to next_to_remove, or null if not found.
	/// </remarks>
	/// <summary>
	/// Stores messages that have been received and acknowledged.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class AckReceiverWindow 
	{
		/// <summary>Sequence number of the first expected message</summary>
		long		initial_seqno=0;
		/// <summary>Sequence number of the next message to be delivered</summary>
		long		next_to_remove=0;
		/// <summary>Collection of messages received</summary>
		Hashtable   msgs=new Hashtable();  // keys: seqnos (Long), values: Messages

		/// <summary>
		/// AckReceiverWindow Constructor
		/// </summary>
		/// <param name="initial_seqno">Sequence number to start from.</param>
		public AckReceiverWindow(long initial_seqno) 
		{
			this.initial_seqno=initial_seqno;
			next_to_remove=initial_seqno;
		}

		/// <summary>
		/// Adds a message
		/// </summary>
		/// <param name="seqno">The sequence num of the message</param>
		/// <param name="msg">The message to add as received</param>
		public void add(long seqno, Message msg) 
		{
			if(seqno < next_to_remove) 
			{
				if(Trace.trace)
					Trace.warn("AckReceiverWindow.add()", "discarded msg with seqno=" + seqno +
						" (next msg to receive is " + next_to_remove + ")");
				return;
			}
			msgs.Add((long)(seqno), msg);
		}
	
		/// <summary>
		/// Removes the next received message. Will only return a <c>Message</c> if all message before it 
		/// have been received.
		/// </summary>
		/// <returns>The next message</returns>
		public Message remove() 
		{
			Message retval = null;
			if(msgs.ContainsKey((long)next_to_remove))
			{
				retval = (Message)msgs[(long)next_to_remove];
				msgs.Remove((long)next_to_remove);
			}
			if(retval != null)
				next_to_remove++;
			return retval;
		}

		/// <summary>
		/// Clears all received messages and resets the expectant sequence number.
		/// </summary>
		public void reset() 
		{
			msgs.Clear();
			next_to_remove=initial_seqno;
		}
		
		/// <summary>
		/// Returns string representation of the object
		/// </summary>
		/// <returns>String representation of the object</returns>
		public override String ToString() 
		{
			String sb = "";
			sb += "Messages (" + next_to_remove + "): ";
			foreach(long key in msgs.Keys)
			{
				sb += key + "+";
			}
			return sb;
		}

	}
}
