using System;

namespace GCT
{
	/// <remarks>
	/// A message digest, which is used by NAKACK for keeping track of current 
	/// seqnos for all members. It contains pairs of senders and a range of seqnos 
	/// (low and high), where each sender is associated with its highest and 
	/// lowest seqnos seen so far. That is, the lowest seqno which was not yet garbage-collected and the highest that was seen so far and is deliverable (or was already delivered) to the application. A range of [0 - 0] means no messages have been received yet. 
	/// <p>
	/// The highest <i>seen</i> sequence number is used to disseminate information about the last (highest) message M 
	/// received from a sender P. Since we might be using a negative acknowledgment 
	/// message numbering scheme, we would never know if the last message was lost. 
	/// Therefore we periodically gossip and include the last message seqno. Members 
	/// who haven't seen it (e.g. because msg was dropped) will request a 
	/// retransmission.
	/// </p>
	/// </remarks>
	/// <author>Chris Koiak</author>
	/// <author>Bela Ban</author>
	/// <summary>
	/// Message Digest for every member in the current group.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class Digest
	{
		/// <summary>
		/// All members in the group
		/// </summary>
		public Address[]		senders = null;
		long[]					low_seqnos = null;       // lowest seqnos seen
		long[]					high_seqnos = null;      // highest seqnos seen so far *that are deliverable*, initially 0
		long[]					high_seqnos_seen = null; // highest seqnos seen so far (not necessarily deliverable), initially -1
		int						index = 0;               // current index of where next member is added
        
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="size">Size of the Digest (i.e. number of members</param>
		public Digest(int size) 
		{
			reset(size);
		}
    

		/// <summary>
		/// Adds a member into the Digest.
		/// </summary>
		/// <param name="sender">Address of new member to add</param>
		/// <param name="low_seqno">Lowest Sequence num received from this member</param>
		/// <param name="high_seqno">Highest Sequence num received from this member</param>
		public void add(Address sender, long low_seqno, long high_seqno) 
		{
			if(index >= senders.Length) 
			{
				if(Trace.trace)
					Trace.error("Digest.add()", "index " + index + 
					" out of bounds, please create new Digest if you want more members !");
				return;
			}
			if(sender == null) 
			{
				if(Trace.trace)
					Trace.error("Digest.add()", "sender is null, will not add it !");
				return;
			}
			senders[index]=sender;
			low_seqnos[index]=low_seqno;
			high_seqnos[index]=high_seqno;
			high_seqnos_seen[index]=-1;
			index++;
		}

		/// <summary>
		/// Adds a member into the Digest.
		/// </summary>
		/// <param name="sender">Address of new member to add</param>
		/// <param name="low_seqno">Lowest Sequence num received from this member</param>
		/// <param name="high_seqno">Highest Sequence num received from this member</param>
		/// <param name="high_seqno_seen">Highest Sequence num seen from this member</param>
		public void add(Address sender, long low_seqno, long high_seqno, long high_seqno_seen) 
		{
			if(index >= senders.Length) 
			{
				if(Trace.trace)
					Trace.error("Digest.add()", "index " + index + 
					" out of bounds, please create new Digest if you want more members !");
				return;
			}
			if(sender == null) 
			{
				if(Trace.trace)
					Trace.error("Digest.add()", "sender is null, will not add it !");
				return;
			}
			senders[index]=sender;
			low_seqnos[index]=low_seqno;
			high_seqnos[index]=high_seqno;
			high_seqnos_seen[index]=high_seqno_seen;
			index++;
		}

		/// <summary>
		/// Adds a number of members into the Digest.
		/// </summary>
		/// <param name="d">Digest to add into this Digest</param>
		public void add(Digest d) 
		{
			Address sender;
			long    low_seqno, high_seqno, high_seqno_seen;

			if(d != null) 
			{
				for(int i=0; i < d.size(); i++) 
				{
					sender=d.senderAt(i);
					low_seqno=d.lowSeqnoAt(i);
					high_seqno=d.highSeqnoAt(i);
					high_seqno_seen=d.highSeqnoSeenAt(i);
					add(sender, low_seqno, high_seqno, high_seqno_seen);
				}
			}
		}


		/// <remarks>
		/// For each member in the new digest the <c>merge()</c> method will be called. The digest must
		/// have enough space to merge the new digest.
		/// </remarks>
		/// <summary>
		/// Merge two digest together.
		/// </summary>
		/// <param name="d">Digest to merge into this Digest</param>
		public void merge(Digest d) 
		{
			Address sender;
			long    low_seqno, high_seqno, high_seqno_seen;

			if(d == null) 
			{
				if(Trace.trace)
					Trace.error("Digest.merge()", "digest to be merged with is null");
				return;
			}
			for(int i=0; i < d.size(); i++) 
			{
				sender=d.senderAt(i);
				low_seqno=d.lowSeqnoAt(i);
				high_seqno=d.highSeqnoAt(i);
				high_seqno_seen=d.highSeqnoSeenAt(i);
				merge(sender, low_seqno, high_seqno, high_seqno_seen);
			}
		}

    
		/**
		 * Similar to add(), but if the sender already exists, its seqnos will be modified (no new entry) as follows:
		 * <ol>
		 * <li>this.low_seqno=min(this.low_seqno, low_seqno)
		 * <li>this.high_seqno=max(this.high_seqno, high_seqno)
		 * <li>this.high_seqno_seen=max(this.high_seqno_seen, high_seqno_seen)
		 * </ol>
		 * If the sender doesn not exist, a new entry will be added (provided there is enough space)
		 */
		/// <remarks>
		/// Similar to add(), but if the sender already exists, its seqnos will be modified (no new entry) as follows:
		/// <list>
		/// <item>this.low_seqno=min(this.low_seqno, low_seqno)</item>
		/// <item>this.high_seqno=max(this.high_seqno, high_seqno)</item>
		/// <item>this.high_seqno_seen=max(this.high_seqno_seen, high_seqno_seen)</item>
		/// </list>
		/// If the sender doesn not exist, a new entry will be added (provided there is enough space)
		/// </remarks>
		/// <summary>
		/// Merge member into Digest. Also called from <c>merge(Digest)</c>.
		/// </summary>
		/// <param name="sender">Member to merge into Digest</param>
		/// <param name="low_seqno">Lowest sequence number associated with this member</param>
		/// <param name="high_seqno">Highest sequence number associated with this member</param>
		/// <param name="high_seqno_seen">Highest sequence number seen associated with this member</param>
		public void merge(Address sender, long low_seqno, long high_seqno, long high_seqno_seen) 
		{
			int  index;
			long my_low_seqno, my_high_seqno, my_high_seqno_seen;
			if(sender == null) 
			{
				if(Trace.trace)
					Trace.error("Digest.merge()", "sender == null");
				return;
			}
			index=getIndex(sender);
			if(index == -1) 
			{
				add(sender, low_seqno, high_seqno, high_seqno_seen);
				return;
			}
	
			my_low_seqno=lowSeqnoAt(index);
			my_high_seqno=highSeqnoAt(index);
			my_high_seqno_seen=highSeqnoSeenAt(index);
			if(low_seqno < my_low_seqno)
				setLowSeqnoAt(index, low_seqno);
			if(high_seqno > my_high_seqno)
				setHighSeqnoAt(index, high_seqno);
			if(high_seqno_seen > my_high_seqno_seen)
				setHighSeqnoSeenAt(index, high_seqno_seen);
		}

		/// <summary>
		/// Gets the position of the member in the Digest
		/// </summary>
		/// <param name="sender">Member to check</param>
		/// <returns>Position of member</returns>
		public int getIndex(Address sender) 
		{
			int ret=-1;
	
			if(sender == null)
				return ret;
			for(int i=0; i < senders.Length; i++)
				if(sender.Equals(senders[i]))
					return i;
			return ret;
		}

		/// <summary>
		/// Checks if a member is in the Digest
		/// </summary>
		/// <param name="sender">Member to check</param>
		/// <returns>True if the message is in the Digest</returns>
		public bool contains(Address sender) 
		{
			return getIndex(sender) != -1;
		}

		/// <summary>
		/// Increment the sender's high_seqno by 1
		/// </summary>
		/// <param name="sender">Member to increment</param>
		public void incrementHighSeqno(Address sender) 
		{
			if(sender == null) return;
			for(int i=0; i < senders.Length; i++) 
			{
				if(senders[i] != null && senders[i].Equals(sender)) 
				{
					high_seqnos[i]=high_seqnos[i]+1;
					break;
				}
			}
		}

		/// <summary>
		/// Number of members in the Digest
		/// </summary>
		/// <returns>Number of members in the Digest</returns>
		public int size() {return senders.Length;}

		/// <summary>
		/// Gets the member at the specified position
		/// </summary>
		/// <param name="index">Position to get member from</param>
		/// <returns>Address of member</returns>
		public Address senderAt(int index) 
		{
			if(index < size())
				return senders[index];
			else 
			{
				if(Trace.trace)
					Trace.error("Digest.senderAt()", "index " + index + " is out of bounds");
				return null;
			}
		}


		/**
		 * Resets the seqnos for the sender at 'index' to 0. This happens when a member has left the group,
		 * but it is still in the digest. Resetting its seqnos ensures that no-one will request a message
		 * retransmission from the dead member.
		 */
		/// <remarks>
		/// This happens when a member has left the group,
		/// but it is still in the digest. Resetting its seqnos ensures that no-one will request a message
		/// retransmission from the dead member.
		/// </remarks>
		/// <summary>
		/// Resets the seqnos for the sender at 'index' to 0.
		/// </summary>
		/// <param name="index">Index to reset</param>
		public void resetAt(int index) 
		{
			if(index < size()) 
			{
				low_seqnos[index]=0;
				high_seqnos[index]=0;
				high_seqnos_seen[index]=-1;
			}    
			else
				if(Trace.trace)
				Trace.error("Digest.resetAt()", "index " + index + " is out of bounds");
		}
    
		/// <summary>
		/// Resets the digest.
		/// </summary>
		/// <param name="size">New Size of the Digest</param>
		public void reset(int size) 
		{
			senders=new Address[size];
			low_seqnos=new long[size];
			high_seqnos=new long[size];
			high_seqnos_seen=new long[size];
			for(int i=0; i < size; i++)
				high_seqnos_seen[i]=-1;
			index=0;
		}

		/// <summary>
		/// Gets the lowest sequence number
		/// </summary>
		/// <param name="index">Index to get the lowest seq numbr from</param>
		/// <returns>Lowest sequence number</returns>
		public long lowSeqnoAt(int index) 
		{
			if(index < size())
				return low_seqnos[index];
			else 
			{
				if(Trace.trace)
					Trace.error("Digest.lowSeqnoAt()", "index " + index + " is out of bounds");
				return 0;
			}
		}


		/// <summary>
		/// Gets the highest sequence number
		/// </summary>
		/// <param name="index">Index to get the highest seq numbr from</param>
		/// <returns>Highest sequence number</returns>
		public long highSeqnoAt(int index) 
		{
			if(index < size())
				return high_seqnos[index];
			else 
			{
				if(Trace.trace)
					Trace.error("Digest.highSeqnoAt()", "index " + index + " is out of bounds");
				return 0;
			}
		}

		/// <summary>
		/// Gets the highest seen sequence number
		/// </summary>
		/// <param name="index">Index to get the highest seen seq numbr from</param>
		/// <returns>Highest seen sequence number</returns>
		public long highSeqnoSeenAt(int index) 
		{
			if(index < size())
				return high_seqnos_seen[index];
			else 
			{
				if(Trace.trace)
					Trace.error("Digest.highSeqnoSeenAt()", "index " + index + " is out of bounds");
				return 0;
			}
		}

		/// <summary>
		/// Gets the highest sequence number
		/// </summary>
		/// <param name="sender">Member that the highest sequence number is required for</param>
		/// <returns>Highest sequence number</returns>
		public long highSeqnoAt(Address sender) 
		{
			long    ret=-1;
			int     index;

			if(sender == null)
				return ret;
			index = getIndex(sender);
			if(index == -1)
				return ret;
			else
				return high_seqnos[index];
		}


		/// <summary>
		/// Gets the highest seen sequence number
		/// </summary>
		/// <param name="sender">Member that the highest seen sequence number is required for</param>
		/// <returns>Highest seen sequence number</returns>
		public long highSeqnoSeenAt(Address sender) 
		{
			long    ret=-1;
			int     index;

			if(sender == null) return ret;
			index=getIndex(sender);
			if(index == -1)
				return ret;
			else
				return high_seqnos_seen[index];
		}

		/// <summary>
		/// Sets the lowest sequence number
		/// </summary>
		/// <param name="index">Index to set the lowest seq number</param>
		/// <param name="low_seqno">New lowest value</param>
		public void setLowSeqnoAt(int index, long low_seqno) 
		{
			if(index < size()) 
			{
				low_seqnos[index]=low_seqno;
			}
			else
				if(Trace.trace)
				Trace.error("Digest.setLowSeqnoAt()", "index " + index + " is out of bounds");
		}

		/// <summary>
		/// Sets the highest sequence number
		/// </summary>
		/// <param name="index">Index to set the highest seq number</param>
		/// <param name="high_seqno">New highest value</param>
		public void setHighSeqnoAt(int index, long high_seqno) 
		{
			if(index < size()) 
			{
				high_seqnos[index]=high_seqno;
			}
			else
				if(Trace.trace)
				Trace.error("Digest.setHighSeqnoAt()", "index " + index + " is out of bounds");
		}

		/// <summary>
		/// Sets the highest seen sequence number
		/// </summary>
		/// <param name="index">Index to set the highest seen seq number</param>
		/// <param name="high_seqno_seen">New highest seen value</param>
		public void setHighSeqnoSeenAt(int index, long high_seqno_seen) 
		{
			if(index < size()) 
				high_seqnos_seen[index]=high_seqno_seen;
			else
				if(Trace.trace)
				Trace.error("Digest.setHighSeqnoSeenAt()", "index " + index + " is out of bounds");
		}

		/// <summary>
		/// Sets the highest sequence number
		/// </summary>
		/// <param name="sender">Member to set the highest seq number for</param>
		/// <param name="high_seqno">New highest seq number</param>
		public void setHighSeqnoAt(Address sender, long high_seqno) 
		{
			int index=getIndex(sender);
			if(index < 0)
				return;
			else
				setHighSeqnoAt(index, high_seqno);
		}

		/// <summary>
		/// Sets the highest seen sequence number
		/// </summary>
		/// <param name="sender">Member to set the highest seen seq number for</param>
		/// <param name="high_seqno_seen">New highest seen seq number</param>
		public void setHighSeqnoSeenAt(Address sender, long high_seqno_seen) 
		{
			int index=getIndex(sender);
			if(index < 0)
				return;
			else
				setHighSeqnoSeenAt(index, high_seqno_seen);
		}


		/// <summary>
		/// Returns a deep-copy of the digest.
		/// </summary>
		/// <returns>A deep-copy of the digest</returns>
		public Digest copy() 
		{
			Digest ret=new Digest(senders.Length);
			ret.senders=(Address[])senders.Clone();
			ret.low_seqnos=(long[])low_seqnos.Clone();
			ret.high_seqnos=(long[])high_seqnos.Clone();
			ret.high_seqnos_seen=(long[])high_seqnos_seen.Clone();
			return ret;
		}

		/// <summary>
		/// Returns a string representation of the Digest
		/// </summary>
		/// <returns>A string representation of the Digest</returns>
		public String toString() 
		{
			String sb = "";
			bool      first=true;
			for(int i=0; i < senders.Length; i++) 
			{
				if(!first) 
				{
					sb = sb + ", ";
				}
				else 
				{
					sb = sb + "[";
					first=false;
				}
				sb = sb + senders[i] + ": [" + low_seqnos[i] + " : ";
				sb = sb + high_seqnos[i];
				if(high_seqnos_seen[i] >= 0)
					sb = sb + " (" + high_seqnos_seen[i] + ")]";
			}
			sb = sb + "]";
			return sb;
		}

		/// <summary>
		/// Returns all the Highest seq num for each member 
		/// </summary>
		/// <returns>All the Highest seq num for each member </returns>
		public String printHighSeqnos() 
		{
			String sb="";
			bool     first=true;
			for(int i=0; i < senders.Length; i++) 
			{
				if(!first) 
				{
					sb = sb + ", ";
				}
				else 
				{
					sb = sb + "[";
					first=false;
				}
				sb = sb + senders[i] + "#" + high_seqnos[i];
			}
			sb = sb + "]";
			return sb;
		}


		/// <summary>
		/// Returns all the Highest seen seq num for each member 
		/// </summary>
		/// <returns>All the Highest seen seq num for each member</returns>
		public String printHighSeqnosSeen() 
		{
			String sb= "";
			bool      first=true;
			for(int i=0; i < senders.Length; i++) 
			{
				if(!first) 
				{
					sb = sb + ", ";
				}
				else 
				{
					sb = sb + "[";
					first=false;
				}
				sb = sb + senders[i];
				sb = sb + "#";
				sb = sb + high_seqnos_seen[i];
			}
			sb = sb + "]";
			return sb;
		}
	}
}
