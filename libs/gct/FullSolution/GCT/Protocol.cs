using System;
using System.Data;
using System.Collections;
using System.Threading;
using System.Diagnostics;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Messaging;
using System.Runtime.Remoting.Channels;
using System.IO;

using GCT.Util;

namespace GCT.Stack
{
	/// <summary>
	/// Base class for all Protocol layers.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public abstract class Protocol 
	{
		/// <summary>Properties to be set</summary>
		protected PropertyCollection	props=null;
		/// <summary>Protocol 'above' this Protocol</summary>
		protected Protocol				up_protocol=null;
		/// <summary>Protocol 'below' this Protocol</summary>
		protected Protocol				down_protocol=null;
		/// <summary>ProtocolSinkStack this Protocol belongs to</summary>
		protected ProtocolSinkStack		stack=null;
		/// <summary>Queue of messages that have been passed up</summary>
		protected MQueue				up_queue=new MQueue();
		/// <summary>Queue of messages that have been passed down</summary>
		protected MQueue				down_queue=new MQueue();
		/// <summary>Thread for passing messages up in to the Protocol</summary>
		protected Thread				up_handler = null;
		/// <summary>Thread for passing messages down in to the Protocol</summary>
		protected Thread				down_handler = null;
		/// <summary>Determines whether the down_handler thread should be started</summary>
		protected bool					down_thread=true; 
		/// <summary>Determines whether the up_handler thread should be started</summary>
		protected bool					up_thread=true;
		/// <summary>Each Protocol must have a unique name</summary>
		protected string				name;

		/// <remarks>Called after instance has been created (null constructor) and before protocol is started</remarks>
		/// <summary>Initilises the Protocol</summary>
		public virtual void             init() {} //throws Exception {}
		/// <summary>Starts the Protocol</summary>
		public virtual void             start() {}
		/// <summary>Stops the Protocol</summary>
		public virtual void             stop() {}

		/// <summary>
		/// Returns the Up Queue
		/// </summary>
		/// <returns>The Up Queue</returns>
		public MQueue            getUpQueue()   {return up_queue;}   
		/// <summary>
		/// Returns the Down Queue
		/// </summary>
		/// <returns>The Down Queue</returns>
		public MQueue            getDownQueue() {return down_queue;} 

		/// <summary>
		/// List of events that are required to be answered by some layer above.
		/// </summary>
		/// <returns>An array of ints</returns>
		public virtual ArrayList           requiredUpServices()   {return null;}

		/// <summary>
		/// List of events that are required to be answered by some layer below.
		/// </summary>
		/// <returns>An array of ints</returns>
		public virtual ArrayList           requiredDownServices() {return null;}

		/// <summary>
		/// List of events that are provided to layers above (they will be handled when sent down from above).
		/// </summary>
		/// <returns>An array of ints</returns>
		public virtual ArrayList           providedUpServices()   {return null;}

		/// <summary>
		/// List of events that are provided to layers below (they will be handled when sent down from below).
		/// </summary>
		/// <returns>An array of ints</returns>
		public virtual ArrayList           providedDownServices() {return null;}


		/// <summary>
		/// Gets and sets the unique Protocol name
		/// </summary>
		public String Name  
		{
			get{ return name;}
			set{ this.name=value;}
		}

		/// <summary>
		/// Gets and sets the Protocol above this Protocol
		/// </summary>
		public Protocol UpProtocol
		{
			get{ return up_protocol;}
			set{ this.up_protocol=value;}
		}

		/// <summary>
		/// Gets and sets the Protocol below this Protocol
		/// </summary>
		public Protocol DownProtocol
		{
			get{ return down_protocol;}
			set{ this.down_protocol=value;}
		}

		/// <summary>
		/// Sets the ProtocolSinkStack associated with this Protocol
		/// </summary>
		public ProtocolSinkStack ProtocolSinkStack
		{
			set {this.stack = value;}
		}

		/// <summary>
		/// Starts the Up Handler thread if up_thread is true
		/// </summary>
		public virtual void startUpHandler() 
		{
			if(up_thread) 
			{
				if(up_handler == null) 
				{
					up_handler = new Thread(new ThreadStart(upHandler));
					up_handler.Name = ("UpHandler (" + name + ")");
					up_handler.Start();
				}
			}
		}
		
		/// <summary>
		/// Starts the Down Handler thread if down_thread is true
		/// </summary>
		public virtual void startDownHandler() 
		{
			if(down_thread) 
			{
				if(down_handler == null) 
				{
					down_handler = new Thread(new ThreadStart(downHandler));
					down_handler.Name = ("DownHandler (" + name + ")");
					down_handler.Start();

				}
			}
		}
		/// <summary>
		/// Stops the Up and Down Handler threads
		/// </summary>
		public void stopInternal() 
		{
			up_queue.close();  // this should terminate up_handler thread
			down_queue.close(); // this should terminate down_handler thread
		}

		/// <remarks>If the up_handler thread
		/// is not available (up_thread == false), then directly call the up() methodelse add the
		/// event to the up queue.
		/// </remarks>
		/// <summary>
		/// Receive an event from the layer below
		/// </summary>
		/// <param name="evt">Event passed up</param>
		protected virtual void receiveUpEvent(Event evt) 
		{
			if(up_handler == null) 
			{
				up(evt);
				return;
			}
			try 
			{
				up_queue.Add(evt);
			}
			catch(Exception e) 
			{
				if(Trace.trace)
					Trace.warn("Protocol.receiveUpEvent()", "exception: " + e);
			}
		}

		
		/// <remarks>
		/// If the down_handler thread
		/// is not available (down_thread == false), then directly call the down() method, else add the
		/// event to the down queue.
		/// </remarks>
		/// <summary>
		/// Receive an event from the layer above
		/// </summary>
		/// <param name="evt">Event passed down</param>
		protected virtual void receiveDownEvent(Event evt) 
		{
			if(down_handler == null) 
			{
				down(evt);
				return;
			}
			try 
			{
				down_queue.Add(evt);
			}
			catch(Exception e) 
			{
				if(Trace.trace)
					Trace.warn("Protocol.receiveDownEvent()", "exception: " + e);
			}
		}

		/// <remarks>
		/// Typically called
		/// by the implementation of <code>Up</code> (when done).
		/// </remarks>
		/// <summary>
		/// Causes the event to be forwarded to the next layer up in the hierarchy.
		/// </summary>
		/// <param name="evt">Event to passed up</param>
		public virtual void passUp(Event evt) 
		{
			if(up_protocol != null) 
			{
				up_protocol.receiveUpEvent(evt);
			}
		}

		/// <remarks>
		/// Typically called
		/// by the implementation of <code>Down</code> (when done).
		/// </remarks>
		/// <summary>
		/// Causes the event to be forwarded to the next layer down in the hierarchy.
		/// </summary>
		/// <param name="evt">Event to passed down</param>
		public virtual void passDown(Event evt) 
		{
			if(down_protocol != null)
				down_protocol.receiveDownEvent(evt);
		}

		/// <remarks>
		/// Usually the current layer will want to examine
		/// the event type and - depending on its type - perform some computation
		/// (e.g. removing headers from a MSG event type, or updating the internal membership list
		/// when receiving a VIEW_CHANGE event).
		/// Finally the event is either a) discarded, or b) an event is sent down
		/// the stack using <code>passDown()</code> or c) the event (or another event) is sent up
		/// the stack using <code>passUp()</code>.
		/// </remarks>
		/// <summary>
		/// An event was received from the layer below.
		/// </summary>
		/// <param name="evt"></param>
		public virtual void up(Event evt) 
		{
			passUp(evt);
		}

		/// <remarks>
		/// The layer may want to examine its type and perform
		/// some action on it, depending on the event's type. If the event is a message MSG, then
		/// the layer may need to add a header to it (or do nothing at all) before sending it down
		/// the stack using <code>passDown()</code>. In case of a GET_ADDRESS event (which tries to
		/// retrieve the stack's address from one of the bottom layers), the layer may need to send
		/// a new response event back up the stack using <code>passUp()</code>.
		/// </remarks>
		/// <summary>
		/// An event is to be sent down the stack.
		/// </summary>
		/// <param name="evt"></param>
		public virtual void down(Event evt) 
		{
			passDown(evt);
		}

		/// <summary>
		/// Removes and sets all generic properties of a Protocol, then passes remainder of properties on to implementation
		/// </summary>
		/// <param name="properties">Collection of properties</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public bool setPropertiesInternal(PropertyCollection properties) 
		{

			this.props = properties;

			if(props.Contains("down_thread")) 
			{
				down_thread = Convert.ToBoolean(props["down_thread"]);
				props.Remove("down_thread");
			}

			if(props.Contains("up_thread")) 
			{
				up_thread = Convert.ToBoolean(props["up_thread"]);
				props.Remove("up_thread");
			}

			return setProperties(props);
		}	

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public virtual bool setProperties(PropertyCollection props)
		{
			if (props.Count > 0)
				return false;

			return true;
		}

		/// <summary>
		/// Thread to allow messages to be passed down asynchronously 
		/// </summary>
		public void downHandler() 
		{
			Event evt;
			while (true)
			{
				try 
				{
					evt=(Event)down_queue.Remove();
					if(evt == null) 
					{
						if(Trace.trace)
							Trace.warn("Protocol [" + Name + "].DownHandler.run()", "removed null event");
						continue;
					}

					down(evt);
					evt=null;
				}
				catch(QueueClosedException queue_closed) 
				{
					break;
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.warn("Protocol [" + Name + "].DownHandler.run()", Name + " exception: " + e + "\n" + e.StackTrace);
				}
			}
		}

		/// <summary>
		/// Thread to allow messages to be passed up asynchronously 
		/// </summary>
		public void upHandler() 
		{
			Event evt = null;
			while (true)
			{
				try 
				{
					evt=(Event)up_queue.Remove();
					if(evt == null) 
					{
						if(Trace.trace)
							Trace.warn("Protocol [" + Name + "].UpHandler.run()", "removed null event");
						continue;
					}
					up(evt);
					evt=null;
				}
				catch(QueueClosedException queue_closed) 
				{
					break;
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.warn("Protocol [" + Name + "].UpHandler.run()", Name + " exception: " + e + "\n" + e.StackTrace);
				}
			}
		}		
	}
}
