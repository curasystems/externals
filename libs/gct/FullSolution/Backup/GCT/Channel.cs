using System;
using System.Collections;

namespace GCT
{
	/// <summary>
	/// Abstract class for any implementation of a channel
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public abstract class Channel : Transport 
		{
		/// <summary>
		/// All events will be sent to this class once received.
		/// </summary>
		protected UpHandler        up_handler=null; 

		/// <summary>
		/// Allows listening on the channel. The listener will be notified of channel closing, etc.
		/// </summary>
		protected ChannelListener  channel_listener=null;

		/// <summary>
		/// Constructor: Uses predefined stack.
		/// </summary>
		protected Channel(){}

		/// <summary>
		/// Constructor: Stack and properties are specified
		/// </summary>
		/// <param name="properties">Properties of Channel stack</param>
		protected Channel(Object properties) {}

		/// <summary>
		/// Connects the Channel to a group.
		/// </summary>
		/// <param name="channel_name">Group to connect to (or create).</param>
		abstract public void connect(String channel_name) ;

		/// <summary>
		/// Disconnects the Channel from the group
		/// </summary>
		abstract public void disconnect();

		/// <summary>
		/// Disconnects and closes the Channel.
		/// </summary>
		abstract public void close();

		/// <summary>
		/// Re-opens a closed channel.
		/// </summary>
		abstract public void open();

		/// <summary>
		/// Checks if the Channel is Open.
		/// </summary>
		/// <returns>True if the Channel is open</returns>
		abstract public bool isOpen();

		/// <summary>
		/// Checks if the Channel is Connected.
		/// </summary>
		/// <returns>True if the Channel is Connected</returns>
		abstract public bool isConnected();

		/// <summary>
		/// Sends a message through the Channel
		/// </summary>
		/// <param name="msg">Message to be sent</param>
		abstract public void send(Message msg);

		/// <summary>
		/// Passes an event down the protocol stack
		/// </summary>
		/// <param name="evt">Event to be passed down the stack</param>
		virtual public void down(Event evt) {}

		/// <summary>
		/// Receives an event from the channel
		/// </summary>
		/// <param name="timeout">Time (ms) to wait for a message</param>
		/// <returns>The next Event received by the channel</returns>
		abstract public Event receive(int timeout);

		/// <summary>
		/// Performs the same as <c>receive()</c> but does not remove the Event
		/// </summary>
		/// <param name="timeout">Time (ms) to wait for a message</param>
		/// <returns>The next Event received by the channel</returns>
		abstract public Event peek(int timeout) ;

		/// <summary>
		/// Returns the current view of the Channel.
		/// </summary>
		/// <returns>The current view of the Channel</returns>
		abstract public View getView();

		/// <summary>
		/// Returns the current Address (IP + Port) the Channel is using
		/// </summary>
		/// <returns>The local Address of the Channel</returns>
		abstract public Address getLocalAddress();

		/// <summary>
		/// Returns the name of the group the Channel is connected to.
		/// </summary>
		/// <returns>The name of the group the Channel is connected to</returns>
		abstract public String getChannelName();

		/// <summary>
		/// Sets the UpHandler that will receive all Events from the Channel
		/// </summary>
		/// <param name="up_handler">The Uphandler object to receive the events</param>
		public void setUpHandler(UpHandler up_handler) 
		{
			this.up_handler=up_handler;
		}

		/// <summary>
		/// Sets the ChannelListener that will be notified of changes to the channel
		/// </summary>
		/// <param name="channel_listener">The ChannelListener object to receive changes</param>
		public void setChannelListener(ChannelListener channel_listener) 
		{
			this.channel_listener=channel_listener;
		}

		/// <summary>
		/// Sets a variety of options within the channel
		/// </summary>
		/// <param name="option">The string representation of the option</param>
		/// <param name="value">The value that the option should be set to</param>
		abstract public void    setOpt(string option, Object value);
	}

}
