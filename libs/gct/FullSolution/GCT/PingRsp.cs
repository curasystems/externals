using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// Response object to request from PING
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class PingRsp : ISerializable
	{
		/// <summary>Local Address</summary>
		public Address own_addr=null;
		/// <summary>Coordinator Address</summary>
		public Address coord_addr=null;
    
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="own_addr">Local Address</param>
		/// <param name="coord_addr">Coordinator Address</param>
		public PingRsp(Address own_addr, Address coord_addr) 
		{
			this.own_addr=own_addr;
			this.coord_addr=coord_addr;
		}

		/// <summary>
		/// Checks if the response is from the coordinator
		/// </summary>
		/// <returns>True if the response is from the coordinator</returns>
		public bool isCoord() 
		{
			if(own_addr != null && coord_addr != null)
				return own_addr.Equals(coord_addr);
			return false;
		}

		/// <summary>
		/// Gets the Local Address
		/// </summary>
		public Address OwnAddress
		{
			get{return own_addr;}
		}

		/// <summary>
		/// Gets the Coordinator Address
		/// </summary>
		public Address CoordAddress
		{
			get{return coord_addr;}
		}

		/// <summary>
		/// Returns a string representation of the current object
		/// </summary>
		/// <returns>A string representation of the current object</returns>
		public String toString() 
		{
			return "[own_addr=" + own_addr + ", coord_addr=" + coord_addr + "]";
		}

		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the information
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			if (own_addr == null)
				info.AddValue("b_own_addr", false);
			else
			{
				info.AddValue("b_own_addr", true);
				info.AddValue("own_addr", own_addr);
			}

			if (coord_addr == null)
				info.AddValue("b_coord_addr", false);
			else
			{
				info.AddValue("b_coord_addr", true);
				info.AddValue("coord_addr", coord_addr);
			}
			
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public PingRsp(SerializationInfo info, StreamingContext ctxt)
		{
			bool b_own_addr = info.GetBoolean("b_own_addr");
			if (b_own_addr)
				own_addr = (Address)info.GetValue("own_addr",typeof(object));
			
			bool b_coord_addr = info.GetBoolean("b_coord_addr");
			if (b_coord_addr)
				coord_addr = (Address)info.GetValue("coord_addr",typeof(object));

		}
	}
}
