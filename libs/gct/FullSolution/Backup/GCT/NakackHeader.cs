using System;
using System.Runtime.Serialization;

using GCT.Util;

namespace GCT.Protocols
{
	/// <summary>
	/// NACACK Header
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class NakAckHeader : Header 
	{
		/// <summary>Header Type: Regular multicast message</summary>
		public const int MSG          = 1;  
		/// <summary>Header Type: Retransmit request message</summary>
		public const int XMIT_REQ     = 2; 
		/// <summary>Header Type: Retransmit response message</summary>
		public const int XMIT_RSP     = 3;  

		/// <summary>Header Type</summary>
		public int       type=0;
		/// <summary>Sequence number of regular message</summary>
		public long      seqno=-1;   
		/// <summary>Range of messages that need transmission</summary>
		public Range     range=null;  

		/// <summary>
		/// Constructor: Used for regular messages
		/// </summary>
		/// <param name="type">Header type (i.e. MSG)</param>
		/// <param name="seqno">Sequence number of message</param>
		public NakAckHeader(int type, long seqno) 
		{
			this.type=type;
			this.seqno=seqno;
		}

		/// <summary>
		/// Constructor: Used for retransmit requests/responses
		/// </summary>
		/// <param name="type">Header type (i.e. XMIT_REQ/XMIT_RSP)</param>
		/// <param name="low">Lowest message needing retransmission</param>
		/// <param name="high">Highest message needing retransmission</param>
		public NakAckHeader(int type, long low, long high) 
		{
			this.type=type;
			range=new Range(low, high);
		}

		/// <summary>
		/// Returns a string representation of the Header type
		/// </summary>
		/// <param name="t">Type of Header</param>
		/// <returns>String representation</returns>
		public static String type2Str(int t) 
		{
			switch(t) 
			{
				case MSG:        return "MSG";
				case XMIT_REQ:   return "XMIT_REQ";
				case XMIT_RSP:   return "XMIT_RSP";
				default:         return "<undefined>";
			}
		}

		/// <summary>
		/// Returns a string representation of the Header object
		/// </summary>
		/// <returns>A string representation of the Header object</returns>
		public String toString() 
		{
			String ret= "";
			ret += "[NAKACK: " + type2Str(type) + ", seqno=" + seqno;
			ret += ", range=" + range;
			ret += "]";
			return ret;
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
			info.AddValue("range", range);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public NakAckHeader(SerializationInfo info, StreamingContext ctxt)
		{
			type = info.GetInt32("type");
			seqno = info.GetInt64("seqno");
			range = (Range)info.GetValue("range", typeof(object));
		}

	}
}
