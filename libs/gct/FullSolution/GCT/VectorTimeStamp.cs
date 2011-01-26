using System;
using System.Collections;

namespace GCT.Protocols
{
	/// <summary>
	/// Vector Time Stamp for causal message ordering.
	/// </summary>
	public class VectorTimeStamp
	{
		/// <summary>Sorted list of members. Key=Address Value=Number Messages sent</summary>
		private SortedList members;
		/// <summary>Address of the owner of the timestamp</summary>
		private Address owner;
		/// <summary>Index of the owner inside the members collection</summary>
		private int ownerIndex; 

		/// <summary>
		/// Constructor. Initialises the Vector time stamp to [0].
		/// </summary>
		/// <param name="owner">The owner of the timestamp</param>
		public VectorTimeStamp(Address owner)
		{
			members = new SortedList();
			this.owner = owner;
			ownerIndex = 0;
			members.Add(owner,0);
		}

		/// <summary>
		/// Gets the current members in the vector time stamp
		/// </summary>
		public IList Members
		{
			get{return members.GetKeyList();}
		}

		/// <summary>
		/// Gets the current values in the vector time stamp
		/// </summary>
		public IList Values
		{
			get{return members.GetValueList();}
		}

		/// <summary>
		/// Returns the number of messages sent by the owner
		/// </summary>
		/// <returns>The number of messages sent by the owner</returns>
		public int getLocalTimeStamp()
		{
			return (int)members.GetByIndex(ownerIndex);
		}

		/// <summary>
		/// Increments the owners count by 1
		/// </summary>
		public void increment()
		{
			members.SetByIndex(ownerIndex,((int)members.GetByIndex(ownerIndex))+1);
		}

		/// <summary>
		/// Returns a representation of the object in a smaller transportable form.
		/// </summary>
		/// <returns>A representation of the object in a smaller transportable form.</returns>
		public TransportableVectorTimeStamp getTransportableVectorTimeStamp()
		{
			return new TransportableVectorTimeStamp(getValues(),ownerIndex);
		}

		/// <summary>
		/// Sets all the values equal to zero.
		/// </summary>
		public void reset()
		{
			SortedList temp = new SortedList();
			foreach(DictionaryEntry e in members)
				temp.Add(e.Key,0);
			members = temp;
		}

		/// <summary>
		/// Increases each value in the time stamp to the larger of either
		/// the current value or the corresponding value in the specified 
		/// time stamp.
		/// </summary>
		/// <param name="other">Time Stamp to compare to.</param>
		public void max(TransportableVectorTimeStamp other)
		{
			for(int i = 0;i<members.Count;i++)
				members.SetByIndex(i,Math.Max((int)members.GetByIndex(i),other.MessagesSent[i]));
		}

		/// <summary>
		/// Calculates if the value is causally next to the current value.
		/// </summary>
		/// <param name="other"></param>
		/// <param name="diag"></param>
		/// <returns></returns>
		public bool causallyNext(TransportableVectorTimeStamp other, out string diag)
		{
			diag = "";
			if(other==null)
				return false;
			int senderIndex = other.SenderPosition;
			int receiverIndex = ownerIndex;

			int[] sender = other.MessagesSent;
			int[] receiver = getValues();

			bool nextCasualFromSender = false;
			bool nextCasual = true;

			diag = "Sender = " + senderIndex + "	Receiver = " + receiverIndex;
			diag += "	[";
			foreach(int i in sender)
				diag +=i + ",";
			diag +="]	[";
			foreach(int j in receiver)
				diag +=j + ",";
			diag += "]\n";

			if (receiverIndex == senderIndex) 
				return true;

			for (int k = 0; k < receiver.Length; k++)
			{
				if ((k == senderIndex) && (sender[k] == receiver[k] + 1))
				{
					nextCasualFromSender = true;
					continue;
				}
				if (k == receiverIndex)
					continue;
				if (sender[k] > receiver[k])
					nextCasual = false;
			}
			return (nextCasualFromSender && nextCasual);
		}

		private int[] getValues()
		{
			int[] values = new int[members.Count];
			int count = 0;
			foreach(DictionaryEntry e in members)
				values[count++] = (int)e.Value;
			return values;
		}

		/// <summary>
		/// Any members not present already will be added to the time stamp vector.
		/// </summary>
		/// <param name="newMembers"></param>
		public void merge(ArrayList newMembers)
		{
			foreach(DictionaryEntry e in members)
				newMembers.Remove(e.Key);

			for (int i = 0; i < newMembers.Count; i++)
				members.Add(newMembers[i],0);

			ownerIndex = members.IndexOfKey(owner);
		}
	}
}
