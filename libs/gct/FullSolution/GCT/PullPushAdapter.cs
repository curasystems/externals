using System;
using System.Threading;
using System.Collections;
using System.Runtime.Serialization;

using GCT.Util;

namespace GCT.Blocks
{

	/// <remarks>
	/// Typically used in the client role (receive()). As this class does not implement interface 
	/// <code>Transport</code>, but <b>uses</b> it for receiving messages, an underlying object
	/// has to be used to send messages (e.g. the channel on which an object of this class relies).
	/// <p>
	/// Multiple MembershipListeners can register with the PullPushAdapter; when a view is received, they
	/// will all be notified. There is one main message listener which sends and receives message. In addition,
	/// MessageListeners can register with a certain tag (identifier), and then send messages tagged with this
	/// identifier. When a message with such an identifier is received, the corresponding MessageListener will be
	/// looked up and the message dispatched to it. If no tag is found (default), the main MessageListener will
	/// receive the message.
	/// </p>
	/// </remarks>
	/// <summary>
	/// Allows a client of <em>Channel</em> to be notified when messages have been received
	/// instead of having to actively poll the channel for new messages.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class PullPushAdapter 
	{
		/// <summary>Transport (Channel) used to receive/send messages</summary>
		protected Transport				transport=null;
		/// <summary>Singular/Default message listener</summary>
		protected MessageListener		listener=null;           // main message receiver
		/// <summary>All membership listeners</summary>
		protected ArrayList				membership_listeners=new ArrayList();
		/// <summary>Continually receives messages from the channel</summary>
		protected Thread				receiver_thread=null;
		/// <summary>Collection of listeners</summary>
		protected Hashtable				listeners=new Hashtable(); // keys=identifier (Serializable), values=MessageListeners
		/// <summary>Used to identify messages sent by the adapter</summary>
		private const String			PULL_HEADER="PULL_HEADER";
    
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="transport">Transport to send and receive messages from.</param>
		public PullPushAdapter(Transport transport) : this(transport, null, null){}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="transport">Transport to send and receive messages from.</param>
		/// <param name="l">Default message listener</param>
		public PullPushAdapter(Transport transport, MessageListener l) : this(transport, l, null){}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="transport">Transport to send and receive messages from.</param>
		/// <param name="ml">Membership listener</param>
		public PullPushAdapter(Transport transport, MembershipListener ml) : this(transport, null, ml){}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="transport">Transport to send and receive messages from.</param>
		/// <param name="l">Default message listener</param>
		/// <param name="ml">Membership listener</param>
		public PullPushAdapter(Transport transport, MessageListener l, MembershipListener ml) 
		{
			this.transport=transport;
			setListener(l);
			addMembershipListener(ml);
		}

		/// <summary>
		/// Returns the Transport for sending/receiveing messages
		/// </summary>
		/// <returns>The Transport for sending/receiveing messages</returns>
		public Transport getTransport() 
		{
			return transport;
		}

		/// <summary>
		/// Starts listening to the transport
		/// </summary>
		public void start() 
		{
			if(receiver_thread == null) 
			{
				receiver_thread = new Thread(new ThreadStart(run));
				receiver_thread.Name = "PullPushAdapterThread";
				receiver_thread.IsBackground = true;
				receiver_thread.Start();
			}
		}

		/// <summary>
		/// Stops listening to the transport
		/// </summary>
		public void stop() 
		{
			if(receiver_thread != null && receiver_thread.IsAlive) 
			{
                //receiver_thread.Interrupt();
				receiver_thread.Abort();
			}
			receiver_thread=null;
		}
    
		/// <summary>
		/// Sends a message.
		/// </summary>
		/// <param name="identifier">Listener object message will be returned to. If null then the default listener will be used</param>
		/// <param name="msg">Message to be sent</param>
		public void send(Object identifier, Message msg)
		{
			if(msg == null) 
			{
				if(Trace.trace)
					Trace.error("PullPushAdapter.send()", "msg is null");
				return;
			}
			if(identifier == null)
				transport.send(msg);
			else 
			{
				msg.putHeader(PULL_HEADER, new PullHeader(identifier));
				transport.send(msg);
			}
		}
    
		/// <summary>
		/// Sends a message.
		/// </summary>
		/// <param name="msg">Message to be sent.</param>
		public void send(Message msg)
		{
			send(null, msg);
		}
    
		/// <summary>
		/// Sets the default MessageListener
		/// </summary>
		/// <param name="l">The Default message listener</param>
		public void setListener(MessageListener l) 
		{
			listener=l;
		}

		/// <summary>
		/// Registers a MessageListener with the adapter
		/// </summary>
		/// <param name="identifier">Identifing object of the listener</param>
		/// <param name="l">The MessageListener</param>
		public void registerListener(Object identifier, MessageListener l) 
		{
			if(l == null || identifier == null) 
			{
				if(Trace.trace)
					Trace.error("PullPushAdapter.registerListener()", "message listener or identifier is null");
				return;
			}
			if(listeners.ContainsKey(identifier)) 
			{
				if(Trace.trace)
					Trace.error("PullPushAdapter.registerListener()", "listener with identifier=" + identifier +
					" already exists, choose a different identifier");
			}
			listeners.Add(identifier, l);
		}

		/// <summary>
		/// Adds a MembershipListener.
		/// </summary>
		/// <param name="l">The MembershipListener</param>
		public void addMembershipListener(MembershipListener l) 
		{
			if(l != null && !membership_listeners.Contains(l))
				membership_listeners.Add(l);
		}

		/// <summary>
		/// Receiver Thread : Removes Events (messages) from the channel
		/// </summary>
		public void run() 
		{
			Event	evt;
			while(receiver_thread != null) 
			{
				try 
				{
					evt = transport.receive(-1);
					if(evt == null)
					{
						Console.WriteLine("PullPushAdapter: Received NULL message from channel");
						continue;
					}
					else
						Console.WriteLine("PullPushAdapter: Received " + Event.type2String(evt.Type) + " message from channel");
					
					switch(evt.Type)
					{
						case(Event.MSG):
							handleMessage((Message)evt.Arg);
							break;
						case(Event.VIEW_CHANGE):
							notifyViewChange((View)evt.Arg);
							break;
						case(Event.SUSPECT):
							notifySuspect((Address)evt.Arg);
							break;
						default:
							if(Trace.trace)
								Trace.error("PullPushAdapter.run()", "Event [" + Event.type2String(evt.Type) + "] should not be passed to adapter");
							break;
					}
				}
				catch(ChannelNotConnectedException conn) 
				{
					Address local_addr=((Channel)transport).getLocalAddress();
					if(Trace.trace)
						Trace.warn("PullPushAdapter.run()", "[" + (local_addr == null? "<null>" : local_addr.ToString()) +
						"] channel not connected, exception is " + conn);
					Thread.Sleep(1000);
					break;
				}
				catch(ChannelClosedException closed_ex) 
				{
					Address local_addr=((Channel)transport).getLocalAddress();
					if(Trace.trace)
						Trace.warn("PullPushAdapter.run()", "[" + (local_addr == null? "<null>" : local_addr.ToString()) +
						"] channel closed, exception is " + closed_ex);
					Thread.Sleep(1000);
					break;
				}
				catch(ThreadAbortException closed_ex) 
				{
					//Stop() has been called
					if(Trace.trace)
						Trace.info("PullPushAdapter.run()", "Adapter has stopped");
					break;
				}
				catch(Exception e) 
				{
					Console.WriteLine("PullPushAdapter.run(): Unknown Error \n" + e.StackTrace);
				}
			}
		}

		/// <remarks>
		/// Check whether the message has an identifier. If yes, lookup the MessageListener associated with the
		/// given identifier in the hashtable and dispatch to it. Otherwise just use the main (default) message
		/// listener
		/// </remarks>
		/// <summary>
		/// Called once a message is received from the Transport.
		/// </summary>
		/// <param name="msg">Message received from the Transport</param>
		protected void handleMessage(Message msg) 
		{
			PullHeader      hdr=(PullHeader)msg.getHeader(PULL_HEADER);
			Object			identifier;
			MessageListener	l = null;

			if(hdr != null && (identifier = hdr.getIdentifier()) != null) 
			{
				if(listeners.Contains(identifier))
					l=(MessageListener)listeners[identifier];
				if(l == null) 
				{
					if(Trace.trace)
						Trace.error("PullPushAdapter.handleMessage()", "received a messages tagged with identifier=" +
						identifier + ", but there is no registration for that identifier. Will drop message");
				}
				else
					l.receive(msg);
			}
			else 
			{
				if(listener != null)
					listener.receive(msg);
			}
		}
    
		/// <summary>
		/// Called once a View is received from the Transport.
		/// </summary>
		/// <param name="v">View received from the Transport</param>
		protected void notifyViewChange(View v) 
		{
			if(v == null) return;

			foreach(MembershipListener l in membership_listeners)
			{
				try 
				{
					l.viewAccepted(v);
				}
				catch(Exception ex) 
				{
					if(Trace.trace)
						Trace.error("PullPushAdapter.notifyViewChange()", "exception notifying " + l + ": " + ex);
				}
			}
		}
    
		/// <summary>
		/// Called once a suspected member is received from the Transport.
		/// </summary>
		/// <param name="suspected_mbr">The suspected member received from the Transport</param>
		protected void notifySuspect(Address suspected_mbr) 
		{
			if(suspected_mbr == null) return;

			foreach(MembershipListener l in membership_listeners)
			{
				try 
				{
					l.suspect(suspected_mbr);
				}
				catch(Exception ex) 
				{
					if(Trace.trace)
						Trace.error("PullPushAdapter.notifySuspect()", "exception notifying " + l + ": " + ex);
				}
			}
		}
    }
}
