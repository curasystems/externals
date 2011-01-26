using System;

namespace GCT
{
	/// <summary>
	/// Used for intra-stack communication.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class Event 
	{		
		/// <summary>Message Event (Arg = Message)</summary>
		public const int MSG                       = 1;
		/// <summary>Connect Event (Arg = group name)</summary>
		public const int CONNECT                   = 2;  
		/// <summary>Reply to Connect Event (Arg = group multicast address)</summary>
		public const int CONNECT_OK                = 3; 
		/// <summary>Disconnect Event (Arg = member address)</summary>
		public const int DISCONNECT				   = 4; 
		/// <summary>Reply to Disconnect Event</summary>
		public const int DISCONNECT_OK             = 5;
		/// <summary>View Change Event (Arg = new View)</summary>
		public const int VIEW_CHANGE               = 6;  
		/// <summary>Get Local Address Event</summary>
		public const int GET_LOCAL_ADDRESS         = 7;
		/// <summary>Set Local Address Event (Arg = local Address)</summary>
		public const int SET_LOCAL_ADDRESS         = 8;
		/// <summary>Suspect Event (Arg = Address of suspected member)</summary>
		public const int SUSPECT                   = 11;
		/// <summary>Find Initial Members Event</summary>
		public const int FIND_INITIAL_MBRS         = 12;
		/// <summary>Reply to Find Initial Members Event (Arg = ArrayList of PingRsps)</summary>
		public const int FIND_INITIAL_MBRS_OK      = 13; 
		/// <summary>Temporary View Event (Arg = new View)</summary>
		public const int TMP_VIEW                  = 14; 
		/// <summary>Become Server Event</summary>
		public const int BECOME_SERVER             = 15; 
		/// <summary>Stable Event (Arg = stable Digest)</summary>
		public const int STABLE                    = 16;
		/// <summary>Get Digest Event</summary>
		public const int GET_DIGEST                = 17; 
		/// <summary>Reply to Get Digest Event (Arg = Digest)</summary>
		public const int GET_DIGEST_OK             = 18; 
		/// <summary>Set Digest Event (Arg = new Digest)</summary>
		public const int SET_DIGEST                = 19;
		/// <summary>Exit Event</summary>
		public const int EXIT                      = 20;  // received when member was forced out of the group
		/// <summary>Unsuspect Event (Arg = Address of previously suspected member)</summary>
		public const int UNSUSPECT                 = 21; 
		/// <summary>Merge Digest Event (Arg = Digest to merge)</summary>
		public const int MERGE_DIGEST              = 22;  

		/// <summary>
		/// Current type of event
		/// </summary>
		private int     type=0;  

		/// <summary>
		/// Object associated with the type
		/// </summary>
		private Object  arg=null;     // must be serializable if used for inter-stack communication

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of Event</param>
		public Event(int type) 
		{
			this.type=type;
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Type of Event</param>
		/// <param name="arg">Object associated with type</param>
		public Event(int type, Object arg) 
		{
			this.type=type;
			this.arg=arg;
		}

		/// <summary>
		/// Gets and sets the type of the Event
		/// </summary>
		public int Type
		{
			get {return type;}
			set {type = value;}
		}
		
		/// <summary>
		/// Gets and sets the object associated with the Event
		/// </summary>
		public Object Arg
		{
			get {return arg;}
			set {arg = value;}
		}	

		/// <summary>
		/// Converts an Event type to a string representation
		/// </summary>
		/// <param name="t">Type of event</param>
		/// <returns>A string representatio nof the Event type</returns>
		public static String type2String(int t) 
		{
			switch(t) 
			{
				case MSG:					return "MSG";
				case CONNECT:				return "CONNECT";
				case CONNECT_OK:			return "CONNECT_OK";
				case DISCONNECT:			return "DISCONNECT";
				case DISCONNECT_OK:			return "DISCONNECT_OK";
				case VIEW_CHANGE:			return "VIEW_CHANGE";
				case GET_LOCAL_ADDRESS:		return "GET_LOCAL_ADDRESS";
				case SET_LOCAL_ADDRESS:		return "SET_LOCAL_ADDRESS";
				case SUSPECT:				return "SUSPECT";
				case FIND_INITIAL_MBRS:		return "FIND_INITIAL_MBRS";
				case FIND_INITIAL_MBRS_OK:	return "FIND_INITIAL_MBRS_OK";
				case TMP_VIEW:				return "TMP_VIEW";
				case BECOME_SERVER:			return "BECOME_SERVER";
				case STABLE:                 return "STABLE";
				case GET_DIGEST:             return "GET_DIGEST";
				case GET_DIGEST_OK:          return "GET_DIGEST_OK";
				case SET_DIGEST:             return "SET_DIGEST";
				case EXIT:                   return "EXIT";
				case UNSUSPECT:              return "UNSUSPECT";
				case MERGE_DIGEST:           return "MERGE_DIGEST";
				default:                     return "UNDEFINED";
			}
		}

		/// <summary>
		/// Returns a string representation of the Event 
		/// </summary>
		/// <returns>A string representation of the Event</returns>
		public override string ToString() 
		{
			return "Event[type=" + type2String(type) + ", arg=" + arg + "]";
		}
	}
}
