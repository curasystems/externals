using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// VERIFY_SUSPECT Header
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class VerifyHeader : Header 
	{
		/// <summary>Header Type: Request</summary>
		public const int ARE_YOU_DEAD  = 1;  // 'from' is sender of verify msg
		/// <summary>Header Type: Response</summary>
		public const int I_AM_NOT_DEAD = 2;  // 'from' is suspected member
	
		/// <summary>Header Type</summary>
		public int     type=ARE_YOU_DEAD;
		/// <summary>Address of the message sender</summary>
		public Address from=null;
	
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Header type</param>
		public VerifyHeader(int type) {this.type=type;}
	
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Header type</param>
		/// <param name="from">Address of the message sender</param>
		public VerifyHeader(int type, Address from) : this(type)
		{
			this.from=from;
		}
	
		/// <summary>
		/// String representation of the Header
		/// </summary>
		/// <returns>String representation of the Header</returns>
		public String toString() 
		{
			switch(type) 
			{
				case ARE_YOU_DEAD:
					return "[VERIFY_SUSPECT: ARE_YOU_DEAD]";
				case I_AM_NOT_DEAD:
					return "[VERIFY_SUSPECT: I_AM_NOT_DEAD]";
				default:
					return "[VERIFY_SUSPECT: unknown type (" + type + ")]";
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
			info.AddValue("from", from);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public VerifyHeader(SerializationInfo info, StreamingContext ctxt)
		{
			type = info.GetInt32("type");
			from = (Address)info.GetValue("from", typeof(object));
		}

	}
    
}
