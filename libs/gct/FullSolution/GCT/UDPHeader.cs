using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// UDP Header
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class UDPHeader : Header
	{
		/// <summary>Name of group message is intended for.</summary>
		String group_addr = null;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="group_addr">Name of group message is intended for.</param>
		public UDPHeader(String group_addr)
		{
			this.group_addr = group_addr;
		}

		/// <summary>
		/// Gets Name/Address of the current group
		/// </summary>
		public String GroupAddress
		{
			get{return group_addr;}
		}

		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the information
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public override void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			info.AddValue("group_addr", group_addr);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public UDPHeader(SerializationInfo info, StreamingContext ctxt)
		{
			group_addr = (string)info.GetValue("group_addr", typeof(string));
		}

	}
}
