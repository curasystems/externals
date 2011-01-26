using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// UNICAST Header
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class UnicastHeader : Header 
	{
		/// <summary>Header Type: Standard message</summary>
		public const int DATA=0;
		/// <summary>Header Type: Message Acknowledgement</summary>
		public const int DATA_ACK=1;
	
		/// <summary>Header Type</summary>
		public int		type=DATA;
		/// <summary>Sequence number of the message</summary>
		public long		seqno=0; 
		/// <summary>Determines if the message is the first message</summary>
		public bool		first=false;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Header Type</param>
		/// <param name="seqno">Sequence number of message</param>
		public UnicastHeader(int type, long seqno) 
		{
			this.type=type == DATA_ACK ? DATA_ACK : DATA;
			this.seqno=seqno;
		}
	
		/// <summary>
		/// String representation of the Header
		/// </summary>
		/// <returns>String representation of the Header</returns>
		public String toString() 
		{
			return "[UNICAST: " + type2Str(type) + ", seqno=" + seqno + "]";
		}
	
		/// <summary>
		/// Converts a type into a string represention
		/// </summary>
		/// <param name="t">The header type required</param>
		/// <returns>A type into a string represention</returns>
		public String type2Str(int t) 
		{
			switch(t) 
			{
				case DATA: return "DATA";
				case DATA_ACK: return "DATA_ACK";
				default: return "<unknown>";
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
			info.AddValue("seqno", seqno);
			info.AddValue("first", first);
			
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public UnicastHeader(SerializationInfo info, StreamingContext ctxt)
		{
			seqno = info.GetInt64("seqno");
			type = info.GetInt32("type");
			first = info.GetBoolean("first");
		}
	}
}
