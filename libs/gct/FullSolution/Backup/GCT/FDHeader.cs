using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// Failure Detection (FD) Header.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class FdHeader : Header 
	{
		/// <summary>Header Type: Standard Heartbeat</summary>
		public const int HEARTBEAT     = 0;
		/// <summary>Header Type: Standard Heartbeat Acknowledgement</summary>
		public const int HEARTBEAT_ACK = 1;
		/// <summary>Header Type: Suspect a member</summary>
		public const int SUSPECT       = 2;
		/// <summary>Header Type: Shun a pinger as they're not in the group</summary>
		public const int NOT_MEMBER    = 3;

		/// <summary>Header Type</summary>
		public int      type=HEARTBEAT;
		/// <summary>Address of suspected pinger</summary>
		public Address  suspected_mbr=null;
		/// <summary>Member who detected suspected pinger</summary>
		public Address  from=null; 

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of FD Header</param>
		public FdHeader(int type)
		{
			this.type=type;
		}

		/// <summary>
		/// Returns a string representation of the Header object
		/// </summary>
		/// <returns>A string representation of the Header object</returns>
		public override String ToString() 
		{
			switch(type) 
			{
				case HEARTBEAT:
					return "[FD: heartbeat]";
				case HEARTBEAT_ACK:
					return "[FD: heartbeat ack]";
				case SUSPECT:
					return "[FD: SUSPECT (suspected_mbr=" + suspected_mbr + ", from=" + from + ")]";
				case NOT_MEMBER: return "[FD: NOT_MEMBER]";
				default:
					return "[FD: unknown type (" + type + ")]";
			}
		}

		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the information
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public override void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			info.AddValue("type", type);
			info.AddValue("suspected_mbr", suspected_mbr);
			info.AddValue("from", from);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public FdHeader(SerializationInfo info, StreamingContext ctxt)
		{
			type = info.GetInt32("type");
			suspected_mbr = (Address)info.GetValue("suspected_mbr", typeof(object));
			from = (Address)info.GetValue("from", typeof(object));
		}
	}
}
