using System;

namespace GCT
{
	/// <summary>
	/// Allows reception of Messages
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public interface MessageListener 
	{
		/// <summary>
		/// Notify the target object of a received Message.
		/// </summary>
		/// <param name="msg">Received Message</param>
		void          receive(Message msg);
	}
}
