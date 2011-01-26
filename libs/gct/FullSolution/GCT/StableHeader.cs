using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// STABLE Header.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class StableHeader : Header 
	{
		/// <summary>Header Type: Request for digest</summary>
		public const int STABLE_GOSSIP = 1;
		/// <summary>Header Type: Stable Digest</summary>
		public const int STABILITY     = 2;
		/// <summary>Header Type</summary>s
		public int    type=0;
		/// <summary>Digest sent with both types of Header</summary>
		public Digest digest = null; 

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Header type</param>
		/// <param name="digest">Stable or unstable digest</param>
		public StableHeader(int type, Digest digest) 
		{
			this.type=type;
			this.digest=digest;
		}
	
		/// <summary>
		/// Converts a type into a string represention
		/// </summary>
		/// <param name="t">The header type required</param>
		/// <returns>A type into a string represention</returns>	
		public static String type2String(int t) 
		{
			switch(t) 
			{
				case STABLE_GOSSIP: return "STABLE_GOSSIP";
				case STABILITY:     return "STABILITY";
				default:            return "<unknown>";
			}
		}

		/// <summary>
		/// String representation of the Header
		/// </summary>
		/// <returns>String representation of the Header</returns>
		public String toString() 
		{
			return "[" + type2String(type) + "]: digest is " + digest;
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
			info.AddValue("digest", digest);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public StableHeader(SerializationInfo info, StreamingContext ctxt)
		{
			type = info.GetInt32("type");
			digest = (Digest)info.GetValue("digest", typeof(object));
		}
	}
}
