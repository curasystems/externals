using System;
using System.Collections;
using System.Data;
using System.Threading;

using GCT.Util;
using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// Computes the broadcast messages that are stable, i.e. have been received by all members. Sends
	/// STABLE events up the stack when this is the case. This allows NAKACK to garbage collect messages that
	/// have been seen by all members.
	/// <p>
	/// Works as follows: periodically we mcast our highest seqnos (seen for each member) to the group.
	/// A stability vector, which maintains the highest seqno for each member and initially contains no data,
	/// is updated when such a message is received. The entry for a member P is computed set to 
	/// min(entry[P], digest[P]). When messages from all members have been received, a stability
	/// message is mcast, which causes all members to send a STABLE event up the stack (triggering garbage collection
	/// in the NAKACK layer).</p>
	/// <p>
	/// The stable task now terminates after max_num_gossips if no messages or view changes have been sent or received
	/// in the meantime. It will resume when messages are received. This effectively suspends sending superfluous
	/// STABLE messages in the face of no activity.</p>
	/// </remarks>
	/// <summary>
	/// Protocol : STABLE computes the broadcast messages that are stable
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class STABLE : Protocol 
	{
		/// <summary>Local Address</summary>
		Address             local_addr=null;
		/// <summary>Current Members in the group</summary>
		ArrayList           mbrs=new ArrayList();
		/// <summary>Keeps track of the highest seqnos from all members</summary>
		Digest              digest=new Digest(1); 
		/// <summary>Synchronises the reception of the digest</summary>
		Promise				digest_promise=new Promise();
		/// <summary>Keeps track of the members that have already responded</summary>
		ArrayList           heard_from=new ArrayList(); 
		/// <summary>Time to wait until digest is received </summary>
		long                digest_timeout=10000;  
		/// <summary>Receive a STABLE gossip every x seconds</summary>
		long                desired_avg_gossip=20000;  
		/// <summary>Delay before we send STABILITY msg</summary>
		long                stability_delay=6000;
		/// <summary>Scheduled task the sends stability message</summary>
		StabilitySendTask   stability_task=null;
		/// <summary>Synchronizes on stability_task</summary>
		Object              stability_mutex=new Object();
		/// <summary>Multicasts periodic STABLE message</summary>
		StableTask          stable_task=null;
		/// <summary>To send periodic STABLE/STABILITY msgs</summary>
		TimeScheduler       timer=null;     
		/// <summary>maximum number of times the StableTask runs before terminating</summary>
		int                 max_gossip_runs=3;
		/// <summary>Decrementing gossips still to be performed</summary>
		int                 num_gossip_runs=3;
		/// <summary>Ranom number generator</summary>
		Random				random = new Random();

		/// <summary>
		/// Constructor
		/// </summary>
		public STABLE()
		{
			name="STABLE";
		}

		/// <summary>
		/// Returns unique protocol name
		/// </summary>
		/// <returns>Unique protocol name</returns>
		public String  getName() {return name;}

		/// <summary>
		/// List of Events that need to be handled by layers above.
		/// </summary>
		/// <returns>List of Events that need to be handled by layers above.</returns>
		public override ArrayList requiredUpServices() 
		{
			ArrayList retval=new ArrayList();
			retval.Add((int)Event.GET_DIGEST);  // NAKACK layer
			return retval;
		}

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props) 
		{

			if(props.Contains("digest_timeout")) 
			{
				digest_timeout = Convert.ToInt64(props["digest_timeout"]);
				props.Remove("digest_timeout");
			}
			if(props.Contains("desired_avg_gossip")) 
			{
				desired_avg_gossip = Convert.ToInt64(props["desired_avg_gossip"]);
				props.Remove("desired_avg_gossip");
			}
			if(props.Contains("stability_delay")) 
			{
				stability_delay = Convert.ToInt64(props["stability_delay"]);
				props.Remove("stability_delay");
			}
			if(props.Contains("max_gossip_runs")) 
			{
				max_gossip_runs = Convert.ToInt32(props["max_gossip_runs"]);
				num_gossip_runs=max_gossip_runs;
				props.Remove("max_gossip_runs");
			}

			if(props.Count > 0) 
			{
				return false;
			}
			return true;
		}

		/// <summary>
		/// Sets the Scheduler to the Scheduler present in the ProtocolSinkStack
		/// </summary>
		public override  void start()
		{
			if(stack != null && stack.timer != null)
				timer = stack.timer;
			else
				throw new Exception("STABLE.up(): timer cannot be retrieved from protocol stack");
		}

		/// <summary>
		/// Stops any stability gossiping
		/// </summary>
		public override  void stop() 
		{
			stopStableTask();
		}

		
		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			Message      msg;
			StableHeader hdr;
			Header       obj;
			int          type=evt.Type;

			switch(evt.Type) 
			{
				case Event.MSG:
					msg=(Message)evt.Arg;
					obj=msg.getHeader(getName());
					if(obj == null || !(obj is StableHeader))
						break;
					hdr=(StableHeader)msg.removeHeader(getName());
					if(hdr.type ==StableHeader.STABLE_GOSSIP)
						handleStableGossip(msg.Source, hdr.digest);
					else if(hdr.type == StableHeader.STABILITY)
						handleStabilityMessage(hdr.digest);
					else
					{
						if(Trace.trace)
							Trace.error("STABLE.up()", "StableHeader type " + hdr.type + " not known");
					}
					return; // don't pass STABLE or STABILITY messages up the stack
				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;
			}

			passUp(evt);
			if(type == Event.VIEW_CHANGE || type == Event.MSG)
				startStableTask(); // only start if not yet running
		}
    
		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override  void down(Event evt) 
		{
			int  type=evt.Type;

			if(type == Event.VIEW_CHANGE || type == Event.MSG)
				startStableTask(); // only start if not yet running

			switch(evt.Type) 
			{

				case Event.VIEW_CHANGE:
					View v=(View)evt.Arg;
					ArrayList tmp=v.getMembers();
					mbrs.Clear();
					mbrs.AddRange(tmp);
					ArrayList toBeRemoved = new ArrayList();
					// removes all elements from heard_from that are not in new view
					foreach(Object obj in heard_from) 
					{
						if(!tmp.Contains(obj))
							toBeRemoved.Add(obj);
					}
					foreach(Object obj in toBeRemoved) 
					{
							heard_from.Remove(obj);
					}
					break;

				case Event.GET_DIGEST_OK:
					digest_promise.setResult(evt.Arg);
					return; // don't pass down
			}
			passDown(evt);
		}

		/* --------------------------------------- Private Methods ---------------------------------------- */
		/// <summary>
		/// Initialises (resets) the members heard from and the stable digest.
		/// </summary>
		private void initialize() 
		{
			lock(digest) 
			{
				digest.reset(mbrs.Count);
				for(int i=0; i < mbrs.Count; i++)
					digest.add((Address)mbrs[i], -1, -1);
				heard_from.Clear();
				heard_from.AddRange(mbrs);
			}
		}

		/// <summary>
		/// Starts the checking for stability
		/// </summary>
		private void startStableTask() 
		{
			num_gossip_runs=max_gossip_runs;
			if(stable_task != null && !stable_task.cancelled()) 
			{
				return;  // already running
			}
			stable_task = new StableTask(this);
			timer.add(stable_task, true); // fixed-rate scheduling
			if(Trace.trace)
				Trace.info("STABLE.startStableTask()", "stable task started; num_gossip_runs=" + num_gossip_runs +
					", max_gossip_runs=" + max_gossip_runs);
		}


		/// <summary>
		/// Stops the checking for stability
		/// </summary>
		private void stopStableTask() 
		{
			if(stable_task != null) 
			{
				stable_task.stop();
				stable_task=null;
			}
		}

		/// <remarks>
		/// Digest d contains (a) the highest seqnos <em>deliverable</em> for each sender and (b) the highest seqnos
		/// <em>seen</em> for each member. (Difference: with 1,2,4,5, the highest seqno seen is 5, whereas the highest
		/// seqno deliverable is 2). The minimum of all highest seqnos deliverable will be taken to send a stability
		/// message, which results in garbage collection of messages lower than the ones in the stability vector. The
		/// maximum of all seqnos will be taken to trigger possible retransmission of last missing seqno.
		/// </remarks>
		/// <summary>
		/// Called when a stability gossip is received.
		/// </summary>
		/// <param name="sender">Sender of the gossip</param>
		/// <param name="d">The digest on the senders channel</param>
		private void handleStableGossip(Address sender, Digest d) 
		{
			Address  mbr;
			long     highest_seqno, my_highest_seqno;
			long     highest_seen_seqno, my_highest_seen_seqno;
			int      index;

			if(d == null || sender == null) 
			{
				if(Trace.trace)
					Trace.error("STABLE.handleStableGossip()", "digest or sender is null");
				return;
			}

			if(Trace.trace)
				Trace.info("STABLE.handleStableGossip()", "received digest " + printStabilityDigest(d) + 
					" from " + sender);

			if(!heard_from.Contains(sender)) 
			{  
				// already received gossip from sender; discard it
				if(Trace.trace)
					Trace.info("STABLE.handleStableGossip()", "already received gossip from " + sender);
				return;
			}
	
			for(int i=0; i < d.size(); i++) 
			{
				mbr = d.senderAt(i);
				highest_seqno = d.highSeqnoAt(i);
				highest_seen_seqno = d.highSeqnoSeenAt(i);
				if((index = digest.getIndex(mbr)) == -1) 
				{
					if(Trace.trace)
						Trace.info("STABLE.handleStableGossip()", "sender " + mbr + " not found in stability vector");
					continue;
				}

				// compute the minimum of the highest seqnos deliverable (for garbage collection)
				my_highest_seqno = digest.highSeqnoAt(mbr);
				if(my_highest_seqno < 0) 
				{
					if(highest_seqno >= 0)
						digest.setHighSeqnoAt(mbr, highest_seqno);
				}
				else 
				{
					digest.setHighSeqnoAt(mbr, Math.Min(my_highest_seqno, highest_seqno));
				}

				// compute the maximum of the highest seqnos seen (for retransmission of last missing message)
				my_highest_seen_seqno = digest.highSeqnoSeenAt(mbr);
				if(my_highest_seen_seqno < 0) 
				{
					if(highest_seen_seqno >= 0)
						digest.setHighSeqnoSeenAt(mbr, highest_seen_seqno);
				}
				else 
				{
					digest.setHighSeqnoSeenAt(mbr, Math.Max(my_highest_seen_seqno, highest_seen_seqno));
				}
			}

			heard_from.Remove(sender);

			if(heard_from.Count == 0) 
			{	    
				if(Trace.trace)
					Trace.info("STABLE.handleStableGossip()", "sending stability msg " + printStabilityDigest(digest));
				sendStabilityMessage(digest.copy());
				initialize();
			}
		}

		/// <remarks>
		/// The reason for waiting a random amount of time is that, in the worst case, all members receive a
		/// STABLE_GOSSIP message from the last outstanding member at the same time and would therefore mcast the
		/// STABILITY message at the same time too. To avoid this, each member waits random N msecs. If, before N
		/// elapses, some other member sent the STABILITY message, we just cancel our own message. If, during
		/// waiting for N msecs to send STABILITY message S1, another STABILITY message S2 is to be sent, we just 
		/// discard S2.
		/// </remarks>
		/// <summary>
		/// Schedules a stability message to be mcast after a random number of milliseconds.
		/// </summary>
		/// <param name="tmp">Stable Digest</param>
		private void sendStabilityMessage(Digest tmp) 
		{
			long         delay;

			if(timer == null) 
			{
				if(Trace.trace)
					Trace.error("STABLE.sendStabilityMessage()", "timer is null, cannot schedule " + "stability message to be sent");
				timer=stack != null ? stack.timer : null;
				return;
			}

			// give other members a chance to mcast STABILITY message. if we receive STABILITY by the end of
			// our random sleep, we will not send the STABILITY msg. this prevents that all mbrs mcast a
			// STABILITY msg at the same time
			delay=random.Next((int)stability_delay);
			if(Trace.trace)
				Trace.info("STABLE.sendStabilityMessage()", "stability_task=" + stability_task + ", delay is " + delay);
			lock(stability_mutex) 
			{
				if(stability_task != null && !stability_task.cancelled())  // schedule only if not yet running
					return;
				stability_task =new StabilitySendTask(this, tmp, delay);
				timer.add(stability_task, true); // run it 1x after delay msecs. use fixed-rate scheduling
			}
		}

		/// <summary>
		/// Stops any awaiting stability multicasts, passes STABLE Event up the stack.
		/// </summary>
		/// <param name="d">Stable Digest</param>
		private void handleStabilityMessage(Digest d) 
		{
			if(d == null) 
			{
				if(Trace.trace)
					Trace.error("STABLE.handleStabilityMessage()", "stability vector is null");
				return;
			}

			if(Trace.trace)
				Trace.info("STABLE.handleStabilityMessage()", "stability vector is " + d.printHighSeqnos());

			lock(stability_mutex) 
			{
				if(stability_task != null) 
				{
					if(Trace.trace)
						Trace.info("STABLE.handleStabilityMessage()", "cancelling stability task (running=" + !stability_task.cancelled() + ")");
					stability_task.stop();
					stability_task=null;
				}
			}

			// pass STABLE event up the stack, so NAKACK can garbage collect old messages
			passUp(new Event(Event.STABLE, d));
		}

		/// <summary>
		/// Returns a string representation of the stable digest
		/// </summary>
		/// <param name="d">Stable Digest</param>
		/// <returns>A string representation of the stable digest</returns>
		private String printStabilityDigest(Digest d) 
		{
			String sb="";
			bool      first=true;

			if(d != null) 
			{
				for(int i=0; i < d.size(); i++) 
				{
					if(!first)
						sb += ", ";
					else
						first=false;
					sb += d.senderAt(i) + "#" + d.highSeqnoAt(i) + " (" + d.highSeqnoSeenAt(i) + ")";
				}
			}
			return sb;
		}

		/* ------------------------------------End of Private Methods ------------------------------------- */
		
		/// <remarks>
		/// Interval between sends varies. Terminates after num_gossip_runs is 0.
		/// However, UP or DOWN messages will reset num_gossip_runs to max_gossip_runs. This has the effect that the
		/// stable_send task terminates only after a period of time within which no messages were either sent 
		/// or received
		/// </remarks>
		/// <summary>
		/// Multicasts periodic STABLE message.
		/// </summary>
		private class StableTask : TimeScheduler.Task 
		{
			/// <summary>Determines if task is active</summary>
			bool stopped=false;
			/// <summary>STABLE protocol associated with the Task</summary>
			STABLE stable;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="stable">STABLE protocol associated with the Task</param>
			public StableTask(STABLE stable)
			{
				this.stable = stable;
			}

			/// <summary>
			/// Sets the task active
			/// </summary>
			public void reset() 
			{
				stopped=false;
			}

			/// <summary>
			/// Sets the task to inactive
			/// </summary>
			public void stop() 
			{
				stopped=true;
			}

			/// <summary>
			/// Returns the name of the Task
			/// </summary>
			/// <returns>The name of the Task</returns>
			public String getName()
			{
				return "StableTask";
			}

			/// <summary>
			/// Checks if the Task is cancelled (inactive)
			/// </summary>
			/// <returns></returns>
			public bool cancelled()  
			{
				return stopped;
			}
	
			/// <summary>
			/// Gets the next interval til gossip is received
			/// </summary>
			/// <returns>The next interval til gossip is received</returns>
			public long nextInterval() 
			{
				return computeSleepTime();
			}

			/// <summary>
			/// Sends stable gossip messages until maximum is reached
			/// </summary>
			public void run() 
			{
				stable.initialize();
				sendStableMessage();
				stable.num_gossip_runs--;
				if(stable.num_gossip_runs <= 0) 
				{
					if(Trace.trace)
						Trace.info("STABLE.StableTask.run()", "stable task terminating (num_gossip_runs=" + stable.num_gossip_runs + ", max_gossip_runs=" + stable.max_gossip_runs + ")");
					stop();
				}
			}

			/// <summary>
			/// Calculates the next interval time
			/// </summary>
			/// <returns>The next interval time</returns>
			long computeSleepTime() 
			{
				Random random = new Random();
				double perc = (double)random.Next(100)/(double)100;
				int retValue = (int)(random.Next((int)(stable.mbrs.Count * stable.desired_avg_gossip * 2)) * perc);
				return (long)retValue;
			}

			/// <remarks>
			/// Message contains highest seqnos of all members
			/// seen by this member. Highest seqnos are retrieved from the NAKACK layer above.
			/// </remarks>
			/// <summary>
			/// Multicasts a STABLE message to all group members.
			/// </summary>
			private void sendStableMessage() 
			{
				Digest       d=null;
				Message      msg=new Message(null,null,null); // mcast message
				StableHeader hdr;
	    
				d=getDigest();
				if(d != null && d.size() > 0) 
				{
					if(Trace.trace)
						Trace.info("STABLE.sendStableMessage()", "mcasting digest " + d + 
							" (num_gossip_runs=" + stable.num_gossip_runs + ", max_gossip_runs=" + stable.max_gossip_runs + ")");
					hdr=new StableHeader(StableHeader.STABLE_GOSSIP, d);
					msg.putHeader("STABLE", hdr);
					stable.passDown(new Event(Event.MSG, msg));
				}
			}

			/// <summary>
			/// Gets the current digest in the channel
			/// </summary>
			/// <returns>The current digest in the channel</returns>
			private Digest getDigest() 
			{
				Digest ret=null;
				stable.passUp(new Event(Event.GET_DIGEST));
				ret=(Digest)stable.digest_promise.getResult(stable.digest_timeout);
				if(ret == null)
					if(Trace.trace)
						Trace.error("STABLE.getDigest()", "digest could not be fetched (from above). Timeout was " + stable.digest_timeout + " msecs");
				return ret;
			}
		}

		/// <summary>
		/// Multicasts a STABILITY message.
		/// </summary>
		private class StabilitySendTask : TimeScheduler.Task 
		{
			/// <summary>Stable Digest</summary>
			Digest		d=null;
			/// <summary></summary>
			Protocol	stable_prot=null;
			/// <summary>STABLE protocol associated with the Task</summary>
			bool		stopped=false;
			/// <summary>Delay before sending message </summary>
			long		delay=2000;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="stable_prot">STABLE protocol associated with the Task</param>
			/// <param name="d">Stable Digest</param>
			/// <param name="delay">Delay before sending message</param>
			public StabilitySendTask(Protocol stable_prot, Digest d, long delay) 
			{
				this.stable_prot=stable_prot;
				this.d=d;
				this.delay=delay;
			}

			/// <summary>
			/// Return the name of the Task
			/// </summary>
			/// <returns>The name of the Task</returns>
			public String getName()
			{
				return "StabilitySendTask";
			}
				
			/// <summary>
			/// Stops the Task
			/// </summary>
			public void stop() 
			{
				stopped=true;
			}
	
			/// <summary>
			/// Checks if the Task is stopped
			/// </summary>
			/// <returns></returns>
			public bool cancelled()  
			{
				return stopped;
			}
		
			/// <summary>
			/// Wait a random number of msecs (to give other a chance to send the STABILITY msg first)
			/// </summary>
			/// <returns></returns>
			public long nextInterval() {return delay;}
	
			/// <summary>
			/// Sends the stability message to all members.
			/// </summary>
			public void run() 
			{
				Message      msg;
				StableHeader hdr;
	    
				if(d != null && !stopped) 
				{
					msg=new Message(null,null,null);
					hdr=new StableHeader(StableHeader.STABILITY, d);
					msg.putHeader("STABLE", hdr);
					stable_prot.passDown(new Event(Event.MSG, msg));
					d=null;
				}
				stopped=true;
			}


		}
	}
}
