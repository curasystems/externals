using System;
using System.Diagnostics;
using System.Threading;
using System.Collections;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Messaging;
using System.IO;

using GCT.Util;
using GCT.Remoting;

namespace GCT.Stack
{
	/// <remarks>
	/// The ProtocolStack manages a number of protocols layered above each other. 
	/// It creates all protocol classes, initializes them and, when ready, starts 
	/// all of them, beginning with the bottom most protocol. It also dispatches 
	/// messages received from the stack to registered objects (e.g. channel, GMP) 
	/// and sends messages sent by those objects down the stack.
	/// <p>
	/// The ProtocolStack is also the connecting class with the client and server Remoting 
	/// infrastructures. Remoting request messages are passed from the infrastructure and
	/// responses are passed back.
	/// </p>
	/// </remarks>
	/// <summary>
	/// Representation of stack of Protocols
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class ProtocolSinkStack : Protocol, IServerChannelSink, IMessageSink 
	{
		/// <summary>Top most Protocol in the stack</summary>
		private Protocol				top_prot=null;
		/// <summary>Bottom most Protocol in the stack</summary>
		private Protocol                bottom_prot=null;
		/// <summary>Helper class for creating and configurating Protocol layers</summary>
		private Configurator            conf=new Configurator();
		/// <summary>Initial stack configuration string</summary>
		private String                  setup_string;
		/// <summary>Channel associated with the stack</summary>
		private GroupChannel		    channel=null;
		/// <summary>Local Address</summary>
		private Address					local_addr=null;
		/// <summary>Determine whether the Stack is stopped</summary>
		private bool					stopped=true;
		/// <summary>Scheduler of tasks</summary>
		public  TimeScheduler			timer = new TimeScheduler(5000);

		//IMessage variables
		/// <summary>Object URI</summary>
		private String					objectURI = null;
		/// <summary>Current Channel URI</summary>
		private String					channelURI = null;
		/// <summary>Time to wait for a response to a Remoting request</summary>
		private int						remotingTimeout = 50000;
		/// <summary>Used to sync requests and responses</summary>
		private Object					remotingMutex=new Object();
		/// <summary>List of Reomting responses</summary>
		private ArrayList				remotingResponses = new ArrayList();
		/// <summary>Members in current group</summary>
		private ArrayList				members = ArrayList.Synchronized(new ArrayList());
		/// <summary>Stores the current Remoting request in transmission</summary>
		private IMethodCallMessage		currentMethodCall = null;
		/// <summary>Chooses which response to deliver to the Remoting infrastructure</summary>
		private RemotingRespChooser		responseChooser = null;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="chan">Channel associated with the stack</param>
		/// <param name="setup">Initial configuration string</param>
		public ProtocolSinkStack(GroupChannel chan, string setup)
		{
			this.channel = chan;
			this.setup_string=setup;
			name = "ProtocolStack";
		}

		/// <summary>
		/// Gets or sets the RemotingChooser
		/// </summary>
		public RemotingRespChooser ResponseChooser
		{
			get{return responseChooser;}
			set{responseChooser = value;}
		}

		/// <summary>
		/// Gets and sets the ObjectURI
		/// </summary>
		public String ObjectURI
		{
			get{return objectURI;}
			set{objectURI = value;}
		}

		/// <summary>
		/// Gets and sets the ChannelURI
		/// </summary>
		public String ChannelURI
		{
			get{return channelURI;}
			set{channelURI = value;}
		}

		/// <remarks>
		/// The Up and Down handlers will be started.
		/// </remarks>
		/// <summary>
		/// Configures and initilises stack.
		/// </summary>
		public void setup()
		{
			if(top_prot == null) 
			{	
				top_prot = conf.setupStack(setup_string, this);
				
				if(top_prot == null)
					throw new Exception("ProtocolStack.setup(): couldn't create protocol stack");
				
				top_prot.UpProtocol = (Protocol)this;

				bottom_prot=conf.getBottommostProtocol(top_prot);
				conf.startProtocolStack(bottom_prot);        // sets up queues and threads
			}
		}

		/// <summary>
		/// Passes Events down the stack
		/// </summary>
		/// <param name="evt">Event to be passed down</param>
		public override void down(Event evt) 
		{
			if(top_prot != null)
				top_prot.down(evt);
			else
				if(Trace.trace)
					Trace.error("ProtocolStack.down()", "no down protocol available !");
		}

		/// <summary>
		/// Overrides base class stopping superfluous threads
		/// </summary>
		public override void startUpHandler() 
		{
			// DON'T REMOVE !!!!  Avoids a superfluous thread
		}

		/// <summary>
		/// Overrides base class stopping superfluous threads
		/// </summary>
		public override void startDownHandler() 
		{
			// DON'T REMOVE !!!!  Avoids a superfluous thread
		}

		/// <summary>
		/// Start all layers. 
		/// </summary>
		public override void start() 
		{
			Protocol p;
			ArrayList   prots=getProtocols();
        
			if(stopped == false)
				return;
			for(int i=0; i < prots.Count; i++) 
			{
				p=(Protocol)prots[i];
				p.start();
			}
			stopped=false;
		}

		/// <summary>
		/// Returns a list of all the Protocols in the stack
		/// </summary>
		/// <returns></returns>
		public ArrayList getProtocols() 
		{
			Protocol p;
			ArrayList   v = new ArrayList();
			p=top_prot;
			while(p != null) 
			{
				v.Add(p);
				p=p.DownProtocol;
			}
			return v;
		}

		/// <summary>
		/// Stop all layers.
		/// </summary>
		public override void stop() 
		{
			if(timer != null) 
			{
				try 
				{
					timer.stop();
				}
				catch(Exception ex) 
				{
				}
			}
        
			if(stopped) return;
			Protocol p;
			ArrayList  prots=getProtocols(); // from top to bottom
			MQueue    down_queue;
            
			for(int i=0; i < prots.Count; i++) 
			{
				p=(Protocol)prots[i];
                
				// 1. Wait until down queue is empty
				down_queue=p.getDownQueue();

				if(down_queue != null) 
				{
					while(down_queue.Count > 0 && !down_queue.Closed) 
					{
						Monitor.Wait(down_queue,100);
					}
				}

				// 2. Call stop() on protocol
				p.stop();
			}
			stopped=true;
		}

		/// <summary>
		/// Destroys message queues and threads
		/// </summary>
		public void destroy()
		{
			if(top_prot != null) 
			{
				conf.stopProtocolStack(top_prot); 
				top_prot=null;
			}
		}

		/// <summary>
		/// Receives events from the top of the stack
		/// </summary>
		/// <param name="evt">Event received</param>
		public override void up(Event evt) 
		{
			Object        obj;
			Message       msg;
			StackHeader   hdr;
			
			switch(evt.Type) 
			{
				
				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;
				case Event.MSG:
					msg=(Message)evt.Arg;
					obj=msg.getHeader(name);
					if(obj == null || !(obj is StackHeader))
						break;
					hdr=(StackHeader)msg.removeHeader(name);
					if(Trace.trace)
						Trace.info("ProtocolSinkStack.up()", "received MSG : " + StackHeader.type2String(hdr.Type));
					if (hdr.Type == StackHeader.REMOTE_RSP) 
					{
						handleRemotingResponse(hdr);
					}
					else if (hdr.Type == StackHeader.REMOTE_REQ)
					{
						handleRemotingRequest(hdr);
					}
					else
					{
						if(Trace.trace)
							Trace.error("GMS.up()", "StackHeader with type=" + hdr.Type + " not known");
					}
					return; // don't pass up
				case Event.VIEW_CHANGE:
					ArrayList tmp;
					if((tmp=((View)evt.Arg).getMembers()) != null) 
					{
						lock(members) 
						{
							members.Clear();
							for(int i=0; i < tmp.Count; i++)
								members.Add(tmp[i]);
						}
					}
					break;
			}

			if(channel != null)
				channel.up(evt);
		}

		
		/// <summary>
		/// Determines the Remoting response to send to the infrastructure.
		/// </summary>
		/// <returns>The IMessage response</returns>
		private IMessage determineResponse()
		{
			if(remotingResponses == null || remotingResponses.Count < 1)
				return null;

			if(responseChooser==null)
			{
				int firstOKResponse = 0;
				for(int i = 0;i<remotingResponses.Count;i++)
				{
					firstOKResponse = i;
					if(((MethodResponse)remotingResponses[0]).Exception == null)
						break;
				}
				return (IMessage)remotingResponses[firstOKResponse];
			}
			else
			{
				return responseChooser.choice(remotingResponses);
			}
		}

		/// <summary>
		/// Sends request into infrastructure, then passes back result.
		/// </summary>
		/// <param name="hdr">Header contain request.</param>
		private void handleRemotingRequest(StackHeader hdr)
		{
			IMessage responseMsg;
			ITransportHeaders responseHeaders;
			Stream responseStream;
			IMessage req = hdr.Message;

			ServerChannelSinkStack sinkStack = new ServerChannelSinkStack();
			ProcessMessage(sinkStack,req,null,null,out responseMsg,out responseHeaders,out responseStream);

			//send response back down the stack
			Address dest = hdr.Source; 
			Message remotingMsg = new Message(dest,local_addr,null);
			StackHeader rspHdr = new StackHeader(StackHeader.REMOTE_RSP,responseMsg, "");
			remotingMsg.putHeader(name,rspHdr);

			down(new Event(Event.MSG,remotingMsg));

		}

		/// <summary>
		/// Stores all Remoting responses.
		/// </summary>
		/// <param name="hdr">Header contain response.</param>
		private void handleRemotingResponse(StackHeader hdr)
		{
			hdr.deserializeResponse(currentMethodCall);
			IMessage resp = hdr.Message;
			lock(remotingResponses) 
			{
				remotingResponses.Add(resp);
				Monitor.Pulse(remotingResponses);
			}
		}

		// -=-=-=-=-=-=-=-=- IServerChannelSink -=-=-=-=-=-=-=-=-
		/// <remarks>
		/// Acts as though Remoting request has travalled up the server
		/// side Remoting channel.
		/// </remarks>
		/// <summary>
		/// Retrieves response to Remoting request.
		/// </summary>
		/// <param name="sinkStack">Default ServerChannelSinkStack</param>
		/// <param name="requestMsg">The request message</param>
		/// <param name="requestHeaders">No used.</param>
		/// <param name="requestStream">No used.</param>
		/// <param name="responseMsg">The response message to return</param>
		/// <param name="responseHeaders">No used.</param>
		/// <param name="responseStream">No used.</param>
		/// <returns></returns>
		public ServerProcessing ProcessMessage(IServerChannelSinkStack sinkStack,
			IMessage requestMsg,
			ITransportHeaders requestHeaders, Stream requestStream,
			out IMessage responseMsg, out ITransportHeaders responseHeaders,
			out Stream responseStream)
		{
			ServerProcessing sp;
			requestHeaders  = new TransportHeaders();
			sp =  ChannelServices.DispatchMessage(sinkStack, requestMsg, out responseMsg);
			responseHeaders = null;
			responseStream = null;
			return sp;
		}
		
		/// <summary>
		/// Not Supported by this channel.
		/// </summary>
		/// <param name="sinkStack"></param>
		/// <param name="state"></param>
		/// <param name="msg"></param>
		/// <param name="headers"></param>
		/// <param name="stream"></param>
		public void AsyncProcessResponse(IServerResponseChannelSinkStack sinkStack, Object state,
			IMessage msg, ITransportHeaders headers, Stream stream)
		{
			throw new NotSupportedException();
		}                   
		
		/// <summary>
		/// This should never get called since we're behave as the last in the chain.
		/// </summary>
		/// <param name="sinkStack"></param>
		/// <param name="state"></param>
		/// <param name="msg"></param>
		/// <param name="headers"></param>
		/// <returns></returns>
		public Stream GetResponseStream(IServerResponseChannelSinkStack sinkStack, Object state,
			IMessage msg, ITransportHeaders headers)
		{           
			throw new NotSupportedException();
		}                               
		
		/// <summary>
		/// Should always be null.
		/// </summary>
		public IServerChannelSink NextChannelSink 
		{
			get{return null;}
			set{;}
		}

		/// <summary>
		/// Should always be null.
		/// </summary>
		public IDictionary Properties
		{
			get{return null;}
		}

		// --------------------- IMessageSink Interface ------------------------
		/// <summary>
		/// There will be no other message sinks in the chain
		/// </summary>
		public IMessageSink NextSink
		{
			get{throw new NotSupportedException("");}
		}
		
		/// <summary>
		/// Not Supported by this channel.
		/// </summary>
		/// <param name="msg"></param>
		/// <param name="replySink"></param>
		/// <returns></returns>
		public IMessageCtrl AsyncProcessMessage(IMessage msg,IMessageSink replySink)
		{
			throw new NotSupportedException("");
		}

		/// <remarks>
		/// The Remoting call is packaged as a message and sent to all group members.
		/// The response will be choosen using a chooser if present otherwise the 
		/// response with the most 'votes' will be chosen.
		/// </remarks>
		/// <summary>
		/// Processes the Remoting call.
		/// </summary>
		/// <param name="msg"></param>
		/// <returns></returns>
		public IMessage SyncProcessMessage(IMessage msg)
		{
			IMethodCallMessage mcm = msg as IMethodCallMessage;
			currentMethodCall = mcm;
			IMessage retMsg = null;

			long		time_to_wait;
			long		start_time;

			try
			{
				Address dest = null;;
				if (objectURI == null)
					return null;
				
				if (channelURI != null)
				{
					String ip = "";
					int port = 0;
					int startpos = (channelURI.IndexOf("//")+2);
					int endpos = (channelURI.IndexOf(":",startpos));
					ip = channelURI.Substring(startpos,endpos-startpos);
					if(String.Compare(ip,"null")==0)
						dest = null;
					else
					{
						++endpos;
						string tmpPort = channelURI.Substring(endpos,channelURI.Length-endpos);
						port = Convert.ToInt32(tmpPort);
						dest = new Address(ip, port);
					}
				}
					
				if(dest!=null && !members.Contains(dest))
					throw new Exception("Remoting request cannot be sent to non-member");

				Message remotingMsg = new Message(dest,null,null);
				StackHeader hdr = new StackHeader(StackHeader.REMOTE_REQ,mcm,objectURI);
				remotingMsg.putHeader(name,hdr);

				remotingResponses.Clear();
								
				down(new Event(Event.MSG,remotingMsg));


				lock(remotingResponses)
				{
					start_time = System.Environment.TickCount;
					time_to_wait = remotingTimeout;
					int responsesDesired = 1;
					if(dest==null)
						responsesDesired = members.Count;
					
					while(remotingResponses.Count < responsesDesired && time_to_wait > 0) 
					{

						if(Trace.trace)
							Trace.info(name + ".SyncProcessMessage()", "waiting for responses: time_to_wait=" + time_to_wait +
							", got " + remotingResponses.Count + "|"+  responsesDesired + " rsps");

						Monitor.Wait(remotingResponses, (int)time_to_wait, true);
						time_to_wait -= System.Environment.TickCount - start_time;
					}
					if(Trace.trace)
						Trace.info(name + ".SyncProcessMessage()", "No longer waiting for responses as Initial Members = " + remotingResponses.Count   + "|" +responsesDesired + "   TimeToWait = " + time_to_wait );
				
					retMsg = determineResponse();

					if (retMsg ==null)
						throw new NullReferenceException("No remoting response to request");

				}
				
			}
			catch (Exception e)
			{
				if(Trace.trace)
					Trace.error(name + ".SyncProcessMessage()", "General error occured: " + e + "/n" + e.StackTrace);
				retMsg = new ReturnMessage(e, mcm);
			}

			currentMethodCall = null;
			return retMsg;
			
		}
	}

	

}
