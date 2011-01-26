using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	///  Group Membership System (GMS) Header.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class GmsHeader : Header 
	{
		/// <summary>Header Type: Join Request</summary>
		public const int JOIN_REQ           = 1;
		/// <summary>Header Type: Join Response</summary>
		public const int JOIN_RSP           = 2;
		/// <summary>Header Type: Leave Request</summary>
		public const int LEAVE_REQ          = 3;
		/// <summary>Header Type: Leave Response</summary>
		public const int LEAVE_RSP          = 4;
		/// <summary>Header Type: View Change</summary>
		public const int VIEW               = 5;

		/// <summary>Header Type.</summary>
		public int				type=0;
		/// <summary>Used when type=VIEW</summary>
		public View				view=null;            // used when type=VIEW
		/// <summary>Used when type=JOIN_REQ or LEAVE_REQ</summary>
		public Address			mbr=null;             // used when type=JOIN_REQ or LEAVE_REQ
		/// <summary>Used when type=JOIN_RSP</summary>
		public JoinRsp			join_rsp=null;        // used when type=JOIN_RSP
		/// <summary>Used when type=VIEW</summary>
		public Digest			digest=null;
		

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of GMS Header</param>
		public GmsHeader(int type) 
		{
			this.type=type;
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of GMS Header</param>
		/// <param name="view">View associated with type</param>
		public GmsHeader(int type, View view) 
		{
			this.type=type;
			this.view=view;
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of GMS Header</param>
		/// <param name="mbr">Address associated with type</param>
		public GmsHeader(int type, Address mbr) 
		{
			this.type=type;
			this.mbr=mbr;
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of GMS Header</param>
		/// <param name="join_rsp">JoinRsp associated with type</param>
		public GmsHeader(int type, JoinRsp join_rsp) 
		{
			this.type=type;
			this.join_rsp=join_rsp;
		}

		/// <summary>
		/// Returns a string representation of the object
		/// </summary>
		/// <returns>A string representation of the object</returns>
		public String toString() 
		{
			String sb = "GmsHeader";
			sb += "[" + type2String(type) + "]";
			switch(type) 
			{

				case JOIN_REQ:
					sb += ": mbr=" + mbr;
					break;

				case JOIN_RSP:
					sb += ": join_rsp=" + join_rsp;
					break;

				case LEAVE_REQ:
					sb += ": mbr=" + mbr;
					break;

				case LEAVE_RSP:
					break;

				case VIEW:
					sb += ": view=" + view;
					break;

			}
			sb += "\n";
			return sb;
		}

		/// <summary>
		/// Converts a type into a string represention
		/// </summary>
		/// <param name="type">The type required</param>
		/// <returns>A type into a string represention</returns>
		public static String type2String(int type) 
		{
			switch(type) 
			{
				case JOIN_REQ:            return "JOIN_REQ";
				case JOIN_RSP:            return "JOIN_RSP";
				case LEAVE_REQ:           return "LEAVE_REQ";
				case LEAVE_RSP:           return "LEAVE_RSP";
				case VIEW:                return "VIEW";
				default:                  return "<unknown>";
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
			info.AddValue("view", view);
			info.AddValue("mbr", mbr);
			info.AddValue("join_rsp", join_rsp);
			info.AddValue("digest", digest);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public GmsHeader(SerializationInfo info, StreamingContext ctxt)
		{
			type = info.GetInt32("type");
			//merge_rejected = info.GetBoolean("merge_rejected");
			view = (View)info.GetValue("view", typeof(object));
			mbr = (Address)info.GetValue("mbr", typeof(object));
			join_rsp = (JoinRsp)info.GetValue("join_rsp", typeof(object));
			digest = (Digest)info.GetValue("digest", typeof(object));
		}

	}
}
