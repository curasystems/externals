using System;

namespace GCT.Util
{
	/// <summary>
	/// Exception by MQueue when a thread tries to access a closed queue.
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class QueueClosedException : Exception 
	{
		/// <summary>
		/// Basic Exception
		/// </summary>
		public QueueClosedException() {}
		/// <summary>
		/// Exception with custom message
		/// </summary>
		/// <param name="msg">Message to display when exception is thrown</param>
		public QueueClosedException( String msg ) : base(msg){}

		/// <summary>
		/// Creates a String representation of the Exception
		/// </summary>
		/// <returns>A String representation of the Exception</returns>
		public String toString() 
		{
			if ( this.Message != null )
				return "QueueClosedException:" + this.Message;
			else
				return "QueueClosedException";
		}
	}

	/// <summary>
	/// Exception thrown when an action is called on a unconnected Channel
	/// </summary>
	public class ChannelNotConnectedException : Exception 
	{
		/// <summary>
		/// Basic Exception
		/// </summary>
		public ChannelNotConnectedException() {}
		/// <summary>
		/// Exception with custom message
		/// </summary>
		/// <param name="msg">Message to display when exception is thrown</param>
		public ChannelNotConnectedException( String msg ) : base(msg){}
		/// <summary>
		/// Creates a String representation of the Exception
		/// </summary>
		/// <returns>A String representation of the Exception</returns>
		public String toString(){return "ChannelNotConnectedException";}
	}

	/// <summary>
	/// Exception thrown when an action is called on a closed Channel
	/// </summary>
	public class ChannelClosedException : Exception 
	{
		/// <summary>
		/// Basic Exception
		/// </summary>
		public ChannelClosedException() {}
		/// <summary>
		/// Exception with custom message
		/// </summary>
		/// <param name="msg">Message to display when exception is thrown</param>
		public ChannelClosedException( String msg ) : base(msg){}
		/// <summary>
		/// Creates a String representation of the Exception
		/// </summary>
		/// <returns>A String representation of the Exception</returns>
		public String toString(){return "ChannelClosedException";}
	}
}
