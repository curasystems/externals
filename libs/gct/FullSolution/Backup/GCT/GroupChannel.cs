using System;
using System.Diagnostics;
using System.Collections;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Messaging;
using System.Threading;

using GCT.Util;
using GCT.Stack;
using GCT.Remoting;

namespace GCT
{
	/// <remarks>
	/// This Channel can be used as a standard group communication channel 
	/// or as a group extention to the .NET Remoting framework
	/// 
	/// </remarks>
	/// <summary>
	/// Channel for group communication (including .NET Remoting)
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class GroupChannel : Channel, IChannelSender, IChannelReceiver
	{
		/// <summary>Priority of Remoting Channel</summary>
		private int    _channelPriority = 1;  // channel priority
		/// <summary>Name of Channel</summary>
		private String channel_name = "C#Group"; // channel name
        
		/// <summary>Default Channel properties</summary>
		private String props = "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=32):" +
			"PING(timeout=3000;num_initial_members=6):" +
			"FD(timeout=3000):" +
			"VERIFY_SUSPECT(timeout=1500):" +
			"STABLE(desired_avg_gossip=10000):" +
			"NAKACK(gc_lag=10;retransmit_timeout=3000):" +
			"UNICAST(timeout=2000):" +
			"GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";

		/// <summary>Stack of Protocols used to send group messages</summary>
		private ProtocolSinkStack	prot_stack;
		/// <summary>Current membership view</summary>
		private View				my_view=null;
		/// <summary>Event sent to upHandler to signify exiting channel</summary>
		private Event				exitEvent = null;
		/// <summary>If true, they channel is connected</summary>
		private bool				connected = false;
		/// <summary>Synchronises channel connection</summary>
		private Object				connect_mutex = new Object();
		/// <summary>Synchronises channel disconnection</summary>
		private Object				disconnect_mutex = new Object();
		/// <summary>Local Address</summary>
		private Address				local_addr =null;
		/// <summary>Synchronises retrieving local address</summary>
		private Object				local_addr_mutex = new Object();
		/// <summary>Queue of messages received</summary>
		private MQueue				mq=new MQueue();
		/// <summary>If set, will receive message sent from this member</summary>
		private bool				receive_local_msgs = true;
		/// <summary>If set, will send suspect events to UpHandler</summary>
		private bool				receive_suspects=true;
		/// <summary>If set, will send view events to UpHandler</summary>
		private bool				receive_views=true;
		/// <summary>If set, will send state events to UpHandler</summary>
		private bool				receive_state_events=true;

		/// <summary>Time to wait for reply to disconnect event</summary>
		private int					connectTimeout = 15000;
		/// <summary>Time to wait for reply to disconnect event</summary>
		private int					disconnectTimeout = 5000;
		/// <summary>Time to wait for retrieval of local address</summary>
		private int					LOCAL_ADDR_TIMEOUT = 3000;
		/// <summary>If set, channel is closed</summary>
		private bool				closed = false;
		
		/// <summary>Chooses which Remoting response to deliver</summary>
		private RemotingRespChooser	responseChooser = null;

		/// <summary>
		/// Constructor: Uses default stack
		/// </summary>
		public GroupChannel() : this("")
		{   
		} 

		/// <summary>
		/// Constructor: Uses custom stack and Remoting chooser
		/// </summary>
		/// <param name="properties">Custom stack configuration</param>
		public GroupChannel(String properties)
		{
			if (properties != null && properties != "")
				props = properties;

			SetupChannel();
			SetupServerChannel();
			if(Trace.trace)
				Trace.info("GroupChannel","Channel Created!!");
		}
        
		/// <summary>
		/// Gets or sets the RemotingChooser
		/// </summary>
		public RemotingRespChooser ResponseChooser
		{
			get{return responseChooser;}
			set
			{
				responseChooser = value;
				prot_stack.ResponseChooser = responseChooser;
			}
		}

		// -=-=-=-=-=-=-=- IChannel implementation -=-=-=-=-=-=-=-

		/// <summary>
		/// Gets the priority of the Channel
		/// </summary>
		public int ChannelPriority
		{
			get { return _channelPriority; }    
		}

		/// <summary>
		/// Gets the Channel name
		/// </summary>
		public String ChannelName
		{
			get { return channel_name;}
		}

		/// <summary>
		/// Returns channelURI and places object uri into out parameter
		/// </summary>
		/// <param name="url">The URL to parse</param>
		/// <param name="objectURI">The object URI</param>
		/// <returns>The channel URI</returns>
		public String Parse(String url, out String objectURI)
		{            
			return GroupChannelHelper.ParseURL(url, out objectURI);
		} // Parse


		// -=-=-=-=-=-=-=- IChannelSender implementation -=-=-=-=-=-=-=-

		/// <summary>
		/// Returns the ProtocolSinkStack to act as the message sink
		/// </summary>
		/// <param name="url"></param>
		/// <param name="remoteChannelData"></param>
		/// <param name="objectURI"></param>
		/// <returns></returns>
		public virtual IMessageSink CreateMessageSink(String url, Object remoteChannelData, out String objectURI)
		{
			// Set the out parameters
			objectURI = null;
			String channelURI = null;
           
			if (url != null) // Is this a well known object?
			{
				// Parse returns null if this is not one of our url's
				channelURI = Parse(url, out objectURI);
			}
			else // determine if we want to connect based on the channel data
			{
				if (remoteChannelData != null)
				{
					if (remoteChannelData is IChannelDataStore)
					{
						IChannelDataStore cds = (IChannelDataStore)remoteChannelData;

						// see if this is an tcp uri
						String simpleChannelUri = Parse(cds.ChannelUris[0], out objectURI);
						if (simpleChannelUri != null)
							channelURI = cds.ChannelUris[0];
					}
				}
			}

			if (null != channelURI)
			{
				if (url == null)
					url = channelURI;

				prot_stack.ChannelURI = channelURI;
				prot_stack.ObjectURI = objectURI;

				IMessageSink msgSink = prot_stack as IMessageSink;
                 
				return msgSink;           
			}

			return null;
		} // CreateMessageSink

		// -=-=-=-=-=-=-=- IChannelReceiver implementation -=-=-=-=-=-=-=-
		/// <summary>
		/// Gets the Data associated with the Channel
		/// </summary>
		public Object ChannelData
		{
			get {return null;}
		} // ChannelData
      
        /// <summary>
        /// Does nothing.
        /// </summary>
        /// <param name="objectURI"></param>
        /// <returns></returns>
		public String[] GetUrlsForUri(String objectURI)
		{
			return null;
		} // GetUrlsforURI

		/// <summary>
		/// Does nothing.
		/// </summary>
		/// <param name="data"></param>
		public void StartListening(Object data)
		{
		} // StartListening

		/// <summary>
		/// Does nothing.
		/// </summary>
		/// <param name="data"></param>
		public void StopListening(Object data)
		{
		} // StopListening

		// -=-=-=-=-=-=-=- Group Channel Methods -=-=-=-=-=-=-=-=-=-=-=-

		/// <summary>
		/// Sets up the ProtocolSinkStack
		/// </summary>
		private void SetupChannel()
		{
			prot_stack = new ProtocolSinkStack(this, props);
			prot_stack.ResponseChooser = responseChooser;
			prot_stack.setup();
		} // SetupChannel


		/// <summary>
		/// Configures the ProtocolSinkStack to communicate received messages with the 
		/// Remoting infrastructure.
		/// </summary>
		private void SetupServerChannel()
		{
			IServerChannelSink sink = ChannelServices.CreateServerChannelSinkChain(null, this);
			prot_stack.NextChannelSink = sink;
		}

		/// <summary>
		/// Called by the ProtocolSinkStack when messages are recevied.
		/// </summary>
		/// <param name="evt">Event that has been received.</param>
		public void up(Event evt) 
		{
			int        type=evt.Type;
			Message    msg;

			/*if the queue is not available, there is no point in
			 *processing the message at all*/
			if(mq == null) 
			{
				if(Trace.trace)
					Trace.error("GroupChannel.up()", "message queue is null");
				return;
			}

			switch(type) 
			{
				
				case Event.MSG:
					msg=(Message)evt.Arg;
					if(!receive_local_msgs) 
					{  // discard local messages (sent by myself to me)
						if(local_addr != null && msg.Source != null)
							if(local_addr.Equals(msg.Source))
								return;
					}
					break;
					case Event.VIEW_CHANGE:
						my_view=(View)evt.Arg;
						if(!receive_views)  // discard if client has not set receving views to on
							return;
						break;
	    
					case Event.SUSPECT:
						if(!receive_suspects)
							return;
						break;
				case Event.CONNECT_OK:
					lock(connect_mutex) 
					{
						Monitor.Pulse(connect_mutex);
					}
					break;
	    
				case Event.DISCONNECT_OK:
					lock(disconnect_mutex) 
					{
						Monitor.Pulse(disconnect_mutex);
					}
					break;
				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;
					
					case Event.EXIT:
						handleExit(evt);
						return;  // no need to pass event up; already done in handleExit()
				default:
					break;
			}


			// If UpHandler is installed, pass all events to it and return (UpHandler is e.g. a building block)
			if(up_handler != null) 
			{
				up_handler.up(evt);
				return;
			}
			if(type == Event.MSG || type == Event.VIEW_CHANGE || type == Event.SUSPECT ||
				 type == Event.EXIT) //type == Event.GET_APPLSTATE || type == Event.BLOCK
			{
				try 
				{
					mq.Add(evt);
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("GroupChannel.up()", "exception: " + e);
				}
			}
		}


		// -=-=-=-=-=-=-=- Channel Methods -=-=-=-=-=-=-=-=-=-=-=-
		/// <summary>
		/// Sends an Event down the stack
		/// </summary>
		/// <param name="evt">Event to send down</param>
		public override void down(Event evt) 
		{
			if(prot_stack != null)
				prot_stack.down(evt);
			else
				if(Trace.trace)
					Trace.error("GroupChannel.down()", "no protocol stack available");
		}

		/// <summary>
		/// Connects the channel the specified group
		/// </summary>
		/// <param name="channel_name">Group to connect to.</param>
		public override void connect(String channel_name)
		{
			if (!connected)
			{
				/*make sure we have a valid channel name*/
				if(channel_name == null) 
				{
					if(Trace.trace)
						Trace.error("GroupChannel.connect()", "channel_name is null");
					return;
				}
				else
					this.channel_name = channel_name; 

				prot_stack.start();
			    
				/* Wait LOCAL_ADDR_TIMEOUT milliseconds for local_addr to have a non-null value (set by SET_LOCAL_ADDRESS) */
				lock(local_addr_mutex) {
					long wait_time=LOCAL_ADDR_TIMEOUT;
					long start = System.Environment.TickCount;

					while(local_addr == null && wait_time > 0) {
						try 
						{ 
							Monitor.Wait(local_addr_mutex,(int)wait_time);
						} 
						catch(ThreadInterruptedException ex) 
						{;}
						wait_time-=System.Environment.TickCount - start;
					}
				}

				// ProtocolStack.start() must have given us a valid local address; if not we won't be able to continue
				if(local_addr == null) 
				{
					if(Trace.trace)
						Trace.error("GroupChannel.connect()", "local_addr == null; cannot connect");
					throw new Exception("local_addr is null");
				}

				ArrayList t=new ArrayList();
				t.Add(local_addr);
				my_view=new View(local_addr, 0, t);  // create a dummy view

				/* Send down a CONNECT event. The CONNECT event travels down to the GMS, where a
				 *  CONNECT_OK response is generated and sent up the stack. GroupChannel blocks until a
				 *  CONNECT_OK has been received, or until timeout has elapsed.
				 */

				//Event connect_event = new Event(Event.CONNECT, channel_name);
				//down(connect_event);

				lock(connect_mutex) 
				{
					try 
					{
						Event connect_event = new Event(Event.CONNECT, channel_name);
						down(connect_event);  // CONNECT is handled by each layer
						Monitor.Wait(connect_mutex,connectTimeout,true);  // wait for CONNECT_OK event
					}
					catch(Exception e) 
					{
						if(Trace.trace)
							Trace.error("GroupChannel.connect()", "exception: " + e);
					}
				}

				connected = true;

				
				/*notify any channel listeners*/
				connected=true;
				if(channel_listener != null)
					channel_listener.channelConnected(this);
			}
		}

		/// <summary>
		/// Disconnect from current group.
		/// </summary>
		public override void disconnect()
		{
			if(connected) 
			{

				/* Send down a DISCONNECT event. The DISCONNECT event travels down to the GMS, where a
				 *  DISCONNECT_OK response is generated and sent up the stack. GroupChannel blocks until a
				 *  DISCONNECT_OK has been received, or until timeout has elapsed.
				 */

				lock(disconnect_mutex) 
				{
					try 
					{
						Event disconnect_event=new Event(Event.DISCONNECT, local_addr);
						down(disconnect_event);   // DISCONNECT is handled by each layer
						Monitor.Wait(disconnect_mutex,disconnectTimeout,true);  // wait for DISCONNECT_OK event
					}
					catch(Exception e) 
					{
						if(Trace.trace)
							Trace.error("GroupChannel.disconnect()", "exception: " + e);
					}
				}

				/*stop the protocol stack*/
				try 
				{
					prot_stack.stop();
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("GroupChannel.disconnect()", "exception: " + e);
				}
				connected=false;

				if(channel_listener != null)
					channel_listener.channelDisconnected(this);
			}

		}

		/// <summary>
		/// Disconnect from the group and close the channel.
		/// </summary>
		public override void close()
		{
			close(true, true); // by default disconnect before closing channel and close mq
		}

		/// <summary>
		/// Set configurable options within the channel
		/// </summary>
		/// <param name="option">Type of option</param>
		/// <param name="value">Value of option</param>
		public override void setOpt(string option, Object value) 
		{
			try
			{
				switch(option) 
				{
					case "receive_local_msgs":
						receive_local_msgs = Convert.ToBoolean(value);
						break;
					case "receive_suspects":
						receive_suspects = Convert.ToBoolean(value);
						break;
					case "receive_views":
						receive_views = Convert.ToBoolean(value);
						break;
					case "receive_state_events":
						receive_state_events = Convert.ToBoolean(value);
						break;
					default:
						if(Trace.trace)
							Trace.error("GroupChannel.setOpt()", "option [" + option + "] not known");
						break;
				}
			}
			catch(Exception e)
			{
				if(Trace.trace)
					Trace.error("GroupChannel.setOpt()", "option " + option + ": value is incorrect");
			}
		}

		/// <summary>
		/// Reopen channel after its been closed
		/// </summary>
		public override void open()
		{
							
			if(!closed)
				throw new Exception("GroupChannel.open(): channel is already open");

			try 
			{
				mq.reset();
				prot_stack.setup();
				closed=false;
			}
			catch(Exception e) 
			{
				throw new Exception("GroupChannel.open(): ");
			}
		}

		/// <summary>
		/// Checks if Channel is open
		/// </summary>
		/// <returns>True if channel is open, otherwise false</returns>
		public override bool isOpen(){return !closed;}

		/// <summary>
		/// Checks if Channel is connected
		/// </summary>
		/// <returns>True if channel is connected, otherwise false</returns>
		public override bool isConnected(){return connected; }

		/// <summary>
		/// Sends a message to the group.
		/// </summary>
		/// <param name="msg"></param>
		public override void send(Message msg)
		{
			checkClosed();
			checkNotConnected();
			down(new Event(Event.MSG, msg));
		}

		/// <summary>
		/// Removes the first received event.
		/// </summary>
		/// <param name="timeout">Time to wait on event arriving if all events have been delivered. Setting this less than 0 causes the method to wait indefinately on an event</param>
		/// <returns>First received event</returns>
		public override Event receive(int timeout)
		{
			Event		evt;

			checkClosed();
			checkNotConnected();

			try 
			{
				if(timeout < 0)
					evt = (Event)mq.Remove();
				else
					evt = (Event)mq.Remove(timeout);
				return evt;
			}
			catch(QueueClosedException queue_closed) 
			{
				throw new ChannelClosedException();
			}
			//catch(Exception e) 
			//{
			//	if(Trace.trace)
			//		Trace.error("GroupChannel.receive()", "exception: " + e + "\n" + e.StackTrace);
			//	return null;
			//}
		}

		/// <summary>
		/// Peeks at the queue of received events
		/// </summary>
		/// <param name="timeout">Time to wait on event arriving if all events have been delivered</param>
		/// <returns>Event at the front of the queue</returns>
		public override Event peek(int timeout) 
		{
			Event		evt;

			checkClosed();
			checkNotConnected();

			try 
			{
				evt= (Event)mq.peek(timeout);
				return evt;
			}
			catch(QueueClosedException queue_closed) 
			{
				if(Trace.trace)
					Trace.error("GroupChannel.peek()", "exception: " + queue_closed);
				return null;
			}
			catch(Exception e) 
			{
				if(Trace.trace)
					Trace.error("GroupChannel.peek()", "exception: " + e);
				return null;
			}
		}

		/// <summary>
		/// Returns current View of the group
		/// </summary>
		/// <returns>Current View of the group</returns>
		public override View getView(){return closed || !connected ? null : my_view;}

		/// <summary>
		/// Returns local address
		/// </summary>
		/// <returns>Local address</returns>
		public override Address getLocalAddress(){return local_addr;}

		/// <summary>
		/// Returns channel name
		/// </summary>
		/// <returns>Channel name</returns>
		public override String getChannelName(){return channel_name;}

		// -=-=-=-=-=-=-=- Private Methods -=-=-=-=-=-=-=-=-=-=-=-

		/// <remarks>
		/// This method does the folloing things
		/// 1. Calls <code>this.disconnect</code> if the disconnect parameter is true
		/// 2. Calls <code>Queue.close</code> on mq if the close_mq parameter is true
		/// 3. Calls <code>ProtocolStack.stop</code> on the protocol stack
		/// 4. Calls <code>ProtocolStack.destroy</code> on the protocol stack
		/// 5. Sets the channel closed and channel connected flags to true and false
		/// 6. Notifies any channel listener of the channel close operation
		/// </remarks>
		/// <summary>
		/// Disconnects and closes the channel.
		/// </summary>
		/// <param name="disconnect"></param>
		/// <param name="close_mq"></param>
		private void close(bool disconnect, bool close_mq) 
		{
			if(closed)
				return;

			if(disconnect)
				this.disconnect();                     // leave group if connected

			if(close_mq) 
			{
				try 
				{
					mq.close();              // closes and removes all messages
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("GroupChannel._close()", "exception: " + e);
				}
			}

			if(prot_stack != null) 
			{
				try 
				{
					if(!disconnect)
						prot_stack.stop(); // called in disconnect
					prot_stack.destroy();
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("GroupChannel._close()", "exception: " + e);
				}
			}
			closed=true;
			connected=false;
			if(channel_listener != null)
				channel_listener.channelClosed(this);
		}

		/// <summary>
		/// Throws Exception if channel is closed
		/// </summary>
		private void checkClosed()
		{
			if(closed)
				throw new Exception("Channel is Closed!!");
		}

		/// <summary>
		/// Throws Exception if channel is not connected
		/// </summary>
		private void checkNotConnected()
		{
			if(!connected)
				throw new Exception("Channel not connected!!");
		}

		/// <summary>
		/// Delivers exit event to UpHandler.
		/// </summary>
		/// <param name="evt"></param>
		private void handleExit(Event evt) 
		{
			if(channel_listener != null)
				channel_listener.channelShunned();
			exitEvent = evt;

			Thread closerThrd = new Thread(new ThreadStart(closerThread));
			closerThrd.Name = "CloserThread";
			closerThrd.IsBackground = true;
			closerThrd.Start();
		}

		/// <summary>
		/// Delivers exit event to UpHandler.
		/// </summary>
		private void closerThread()
		{
			try 
			{
				close(false, false); // do not disconnect before closing channel, do not close mq

				if(up_handler != null)
					up_handler.up(exitEvent);
				else 
				{
					try 
					{
						mq.Add(exitEvent);
					} 
					catch(Exception ex) 
					{
						if(Trace.trace)
							Trace.error("GroupChannel.CloserThread.run()", "exception: " + ex);
					}
				}
				if(mq != null) 
				{
					Thread.Sleep(500); // give the mq thread a bit of time to deliver EXIT to the application
					try {mq.close();}
					catch(Exception ex) {}
				}

			} 
			catch(Exception ex) 
			{
				if(Trace.trace)
					Trace.error("GroupChannel.CloserThread.run()", "exception: " + ex);
			}
		}

	}

}

