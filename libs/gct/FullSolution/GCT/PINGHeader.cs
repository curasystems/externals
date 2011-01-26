using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// PING Header.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class PINGHeader : Header
	{
		/// <summary>Header Type: Get members request</summary>
		public const int GET_MBRS_REQ = 1; 
		/// <summary>Header Type: Get members response</summary>
		public const int GET_MBRS_RSP = 2; 

		/// <summary>Header Type</summary>
		public int    type=0;
		/// <summary>Response to request. Contains local and coordinator address</summary>
		public PingRsp arg = null;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of Header</param>
		/// <param name="arg">Response if applicable</param>
		public PINGHeader(int type, PingRsp arg) 
		{
			this.type = type;
			this.arg = arg;
			className = "PING";
		}

		/// <summary>
		/// String representation of the Header
		/// </summary>
		/// <returns>String representation of the Header</returns>
		public override String ToString() 
		{
			return "[PING: type=" + type2Str(type) + ", arg=" + arg + "]";
		}

		/// <summary>
		/// Converts a type into a string represention
		/// </summary>
		/// <param name="t">The header type required</param>
		/// <returns>A type into a string represention</returns>
		private String type2Str(int t) 
		{
			switch(t) 
			{
				case GET_MBRS_REQ: return "GET_MBRS_REQ";
				case GET_MBRS_RSP: return "GET_MBRS_RSP";
				default:           return "<unkown type (" + t + ")>";
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

			if (arg == null)
				info.AddValue("b_arg", false);
			else
			{
				info.AddValue("b_arg", true);
				info.AddValue("arg", arg);
			}
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public PINGHeader(SerializationInfo info, StreamingContext ctxt)
		{
			type = (int)info.GetInt32("type");
			bool b_arg = info.GetBoolean("b_arg");
			if (b_arg)
			{
				arg= (PingRsp)info.GetValue("arg",typeof(object));
			}
		}



	}
}
