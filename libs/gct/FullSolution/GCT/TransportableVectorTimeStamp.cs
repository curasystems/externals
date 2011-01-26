using System;
using System.Collections;

namespace GCT.Protocols
{
	/// <summary>
	/// Representation of current number of messages sent by each group member. This is the 
	/// lightweight version of VectorTimeStamp as it doesnt store the actual member addresses.
	/// </summary>
	[Serializable]
	public class TransportableVectorTimeStamp
	{
		/// <summary>Array of the current max number of messages send by each member.</summary>
		private int[] messagesSent;
		/// <summary>The position in the messagesSent array of the message sender</summary>
		private int senderPosition;
		/// <summary>The associated message. Used to hold the message if the timestamp is not causally next</summary>
		private Message associatedMessage;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="messagesSent">Array of the current max number of messages send by each member.</param>
		/// <param name="senderPosition">The position in the messagesSent array of the message sender</param>
		public TransportableVectorTimeStamp(int[] messagesSent, int senderPosition): this(messagesSent,senderPosition,null)
		{}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="messagesSent">Array of the current max number of messages send by each member.</param>
		/// <param name="senderPosition">The position in the messagesSent array of the message sender</param>
		/// <param name="associatedMessage">The message associated with the vector time stamp</param>
		public TransportableVectorTimeStamp(int[] messagesSent, int senderPosition, Message associatedMessage)
		{
			this.messagesSent = messagesSent;
			this.senderPosition = senderPosition;
			this.associatedMessage = associatedMessage;
		}

		/// <summary>
		/// Gets or Sets the vector time stamp
		/// </summary>
		public int[] MessagesSent
		{
			get{return messagesSent;}
			set{messagesSent = value;}
		}

		/// <summary>
		/// Gets or sets the senders position in the vector time stamp
		/// </summary>
		public int SenderPosition
		{
			get{return senderPosition;}
			set{senderPosition = value;}
		}

		/// <summary>
		/// Gets or sets the time stamps associated message.
		/// </summary>
		public Message AssosicatedMessage
		{
			get{return associatedMessage;}
			set{associatedMessage = value;}
		}

		/// <summary>
		/// A vector time stamp, A, is less than or equal to another vector time stamp, B,
		/// if each element in A is less than or equal to the corresponding element in B.
		/// </summary>
		/// <param name="vStamp2">The vector time stamp to compare to</param>
		/// <returns>True if the specified arguement is greater than the object</returns>
		public bool lessThanOrEquals(TransportableVectorTimeStamp vStamp2)
		{
			bool lessThanOrEqual = true;
			for(int i =0; i < messagesSent.Length;i++)
			{
				if(messagesSent[i] > vStamp2.MessagesSent[i])
				{
					lessThanOrEqual=false;
					break;
				}
			}
			return lessThanOrEqual;
		}
		
		/// <summary>
		/// A vector time stamp, A, is lequal to another vector time stamp, B,
		/// if each element in A is equal to the corresponding element in B.
		/// </summary>
		/// <param name="vStamp2"></param>
		/// <returns></returns>
		public bool equals(TransportableVectorTimeStamp vStamp2)
		{
			bool equal = true;
			for(int i =0; i< messagesSent.Length;i++)
			{
				if(vStamp2.MessagesSent[i]!=messagesSent[i])
				{
					equal=false;
					break;
				}
			}
			return equal;
		}
	}
}
