using System;
using System.Collections;
using System.Data;

using GCT.Util;
using GCT.Stack;
using GCT;
using GCT.Protocols;

namespace GCT.Protocols
{
	/// <remarks>
	/// 
	/// </remarks>
	/// <summary>
	/// Protocol: Causal
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  16/06/2003</p>
	/// </summary>
	public class CAUSAL : Protocol 
	{
		private VectorTimeStamp		localStamp;
		private ArrayList			delayQueue;
		private Address				localAddress;

		/// <summary>
		/// Constructor.
		/// </summary>
		public CAUSAL()
		{
			name = "CAUSAL";
		}

		/// <summary>
		/// Returns unique <c>Protocol</c> name
		/// </summary>
		/// <returns>Unique <c>Protocol</c> name</returns>
		public String  getName() {return name;}

		private void addToQueue(TransportableVectorTimeStamp vts)
		{
			int i;
			for(i = 0; i< delayQueue.Count ;i++)
			{
				if(!((TransportableVectorTimeStamp)delayQueue[i]).lessThanOrEquals(vts))
					break;
			}
			delayQueue.Insert(i,vts);
		}

		/// <summary>
		/// Processes <c>Events</c> traveling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			switch(evt.Type) 
			{
				case Event.SET_LOCAL_ADDRESS:
					localAddress = (Address) evt.Arg;
					localStamp = new VectorTimeStamp(localAddress);
					delayQueue = new ArrayList();
					break;

				case Event.VIEW_CHANGE:
					ArrayList newViewMembers = ((View) evt.Arg).getMembers();
					localStamp.merge((ArrayList) newViewMembers.Clone());
					localStamp.reset();
					break;

				case Event.MSG:
					Message msg = (Message)evt.Arg;
					Header hdr = msg.getHeader(getName());

					//Check that the correct header is available
					if(hdr == null || !(hdr is CausalHeader))
					{
						if(Trace.trace)
							Trace.error("Causal.up()","No Causal Header Found!");
						passUp(evt);
						return;
					}
					
					CausalHeader cHdr = (CausalHeader)hdr;

					//Pass message up if it is causually next
					String diag = "";
					bool next = localStamp.causallyNext(cHdr.VectorTime, out diag);
					//passUp(new Event(Event.MSG, new Message(null,localAddress,diag)));
					if(Trace.trace)
						Trace.info("CAUSAL.up()", diag + "Causally Next = " + next);
					if(next)
					{
						if(Trace.trace)
							Trace.info("CAUSAL.up()","Message received in sequence.");
						passUp(evt);
						localStamp.max(cHdr.VectorTime);
					}
					else //Else add to the delayed queue
					{
						if(Trace.trace)
							Trace.warn("CAUSAL.up()","Message received out of sequence.");
						cHdr.VectorTime.AssosicatedMessage = msg;
						addToQueue(cHdr.VectorTime);
					}
					//Check the delayed queue removing all items that can be passed up
					TransportableVectorTimeStamp queuedVector = null;
					while ((delayQueue.Count > 0) &&
						localStamp.causallyNext((queuedVector = (TransportableVectorTimeStamp)delayQueue[0]), out diag))
					{
						delayQueue.Remove(queuedVector);
						passUp(new Event(Event.MSG, queuedVector.AssosicatedMessage));
						localStamp.max(queuedVector);
					}
					return;
			}
			passUp(evt); 
		}

		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt) 
		{
			switch (evt.Type)
			{
				case Event.MSG:
					Message msg = (Message)evt.Arg;
					if(msg.Destination != null && msg.Destination.isMulticastAddress())
						break;

					localStamp.increment();
					//System.Diagnostics.Trace.WriteLine("Incremented LTS = " + localStamp.getLocalTimeStamp());
					Header hdr = new CausalHeader(localStamp.getTransportableVectorTimeStamp());
					msg.putHeader(getName(),hdr);
					break;
			}
			passDown(evt);
		}
	}
}
