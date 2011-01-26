using System;
using System.Collections;
using System.Data;
using System.Threading;

using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// Verifies that the suspected member is really dead. If yes,
	/// passes SUSPECT event up the stack, otherwise discards it. Has to be placed somewhere above the FD layer and
	/// below the GMS layer (receiver of the SUSPECT event).
	/// </remarks>
	/// <summary>
	/// Protocol: Verifies SUSPECT events traveling up the stack.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class VERIFY_SUSPECT : Protocol //implements Runnable {
	{
		/// <summary>Local Address</summary>
		Address      local_addr=null;
		/// <summary>Number of millisecs to wait for an <i>i-am-not-dead</i> msg</summary>
		long         timeout=2000; 
		/// <summary>Number of <i>are-you-alive</i> msgs and <i>i-am-not-dead</i> responses</summary>
		int          num_msgs=1;
		/// <summary>Collection of current supsects and time (ms) since suspected</summary>
		Hashtable    suspects=new Hashtable(); 
		/// <summary>Suspects members that havent responded by the timeout</summary>
		Thread       timer = null;

		/// <summary>
		/// Constructor
		/// </summary>
		public VERIFY_SUSPECT()
		{
			name = "VERIFY_SUSPECT";
		}

		/// <summary>
		/// Returns unique protocol name
		/// </summary>
		/// <returns>Unique protocol name</returns>
		public String  getName() {return name;}

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props)
		{
			if(props.Contains("timeout")) 
			{
				timeout = Convert.ToInt64(props["timeout"]);
				props.Remove("timeout");
			}

			if(props.Contains("num_msgs")) 
			{
				num_msgs = Convert.ToInt32(props["num_msgs"]);
				if(num_msgs <= 0) 
				{
					if(Trace.trace)
						Trace.warn("VERIFY_SUSPECT.setProperties()", "num_msgs is invalid (" +
						num_msgs + "): setting it to 1");
					num_msgs=1;
				}
				props.Remove("num_msgs");
			}
	
			if(props.Count > 0) 
			{
				return false;
			}
			return true;
		}

	    
		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt) 
		{
			Address       suspected_mbr;
			Message       msg, rsp;
			Object        obj;
			VerifyHeader  hdr;

			switch(evt.Type) 
			{

				case Event.SET_LOCAL_ADDRESS:
					local_addr=(Address)evt.Arg;
					break;

				case Event.SUSPECT:  // it all starts here ...
					suspected_mbr=(Address)evt.Arg;
					if(suspected_mbr == null) 
					{
						if(Trace.trace)
							Trace.error("VERIFY_SUSPECT.up()", "suspected member is null");
						return;
					}
					suspect(suspected_mbr);
					return;  // don't pass up; we will decide later (after verification) whether to pass it up


				case Event.MSG:
					msg=(Message)evt.Arg;
					obj=msg.getHeader(getName());
					if(obj == null || !(obj is VerifyHeader))
						break;
					hdr=(VerifyHeader)msg.removeHeader(getName());
				switch(hdr.type) 
				{
					case VerifyHeader.ARE_YOU_DEAD:
						if(hdr.from == null)
							if(Trace.trace)
								Trace.error("VERIFY_SUSPECT.up()", "ARE_YOU_DEAD: hdr.from is null");
						else 
						{
							for(int i=0; i < num_msgs; i++) 
							{
								rsp=new Message(hdr.from, null, null);
								rsp.putHeader(getName(), new VerifyHeader(VerifyHeader.I_AM_NOT_DEAD, local_addr));
								passDown(new Event(Event.MSG, rsp));
							}
						}
						return;
					case VerifyHeader.I_AM_NOT_DEAD:
						if(hdr.from == null) 
						{
							if(Trace.trace)
								Trace.error("VERIFY_SUSPECT.up()", "I_AM_NOT_DEAD: hdr.from is null");
							return;
						}
						unsuspect(hdr.from);
						return;
				}
					return;
			}
			passUp(evt);
		}

		/// <remarks>
		/// Will be started when a suspect is added to the suspects hashtable. Continually iterates over the
		/// entries and removes entries whose time have elapsed. For each removed entry, a SUSPECT event is passed
		/// up the stack (because elapsed time means verification of member's liveness failed). Computes the shortest
		/// time to wait (min of all timeouts) and waits(time) msecs. Will be woken up when entry is removed (in case
		/// of successful verification of that member's liveness). Terminates when no entry remains in the hashtable.
		/// </remarks>
		/// <summary>
		/// Executed in a seperate Thread. Suspects members that haven't responded with <i>i-am-not-dead</i> by the timeout
		/// </summary>
		public void run() 
		{
			long    val, curr_time, diff;
			ArrayList definiteSuspects = new ArrayList();

			while(timer != null && suspects.Count > 0) 
			{
				diff=0;

				lock(suspects) 
				{
					foreach(Address mbr in suspects.Keys)
					{
						val=(long)suspects[mbr];
						curr_time = System.Environment.TickCount;
						diff = curr_time - val;
						if(diff >= timeout) 
						{  // haven't been unsuspected, pass up SUSPECT
							if(Trace.trace)
								Trace.info("VERIFY_SUSPECT.run()", "diff=" + diff + ", mbr " + mbr + 
									" is dead (passing up SUSPECT event)");
							passUp(new Event(Event.SUSPECT, mbr));
							definiteSuspects.Add(mbr);
							continue;
						}
						diff=Math.Max(diff, timeout - diff);		    
					}

					foreach(Address susp in definiteSuspects)
					{
						suspects.Remove(susp);
					}
				}

				if(diff > 0)
					Thread.Sleep((int)diff);
			}
			timer=null;
		}



		/* --------------------------------- Private Methods ----------------------------------- */

		/// <summary>
		/// Sends ARE_YOU_DEAD message to suspected_mbr, wait for return or timeout
		/// </summary>
		/// <param name="mbr">Suspected member</param>
		private void suspect(Address mbr) 
		{
			Message msg;
			if(mbr == null) return;
	
			lock(suspects) 
			{
				if(suspects.ContainsKey(mbr))
					return;
				suspects.Add(mbr, (long)System.Environment.TickCount);
				if(Trace.trace)
					Trace.info("VERIFY_SUSPECT.suspect()", "verifying that " + mbr + " is dead");
				for(int i=0; i < num_msgs; i++) 
				{
					msg=new Message(mbr, null, null);
					msg.putHeader(getName(), new VerifyHeader(VerifyHeader.ARE_YOU_DEAD, local_addr));	
					passDown(new Event(Event.MSG, msg));	
				}
			}
			if(timer == null)
				startTimer();
		}

		/// <summary>
		/// Unsuspects reponding member
		/// </summary>
		/// <param name="mbr">Reponding member</param>
		private void unsuspect(Address mbr) 
		{
			if(mbr == null) return;	
			lock(suspects) 
			{
				if(suspects.ContainsKey(mbr)) 
				{
					if(Trace.trace)
						Trace.info("VERIFY_SUSPECT.unsuspect()", "member " + mbr + " is not dead !");
					suspects.Remove(mbr);
					passDown(new Event(Event.UNSUSPECT, mbr));
					passUp(new Event(Event.UNSUSPECT, mbr));
				}
			}
		}

		/// <summary>
		/// Starts the timer, <c>run()</c>, to wait for responses
		/// </summary>
		void startTimer() 
		{
			if(timer == null) 
			{
				timer=new Thread(new ThreadStart(run));
				timer.Name = "VERIFY_SUSPECT.TimerThread"; 
				timer.IsBackground = true;
				timer.Start();
			}
		}

		/// <summary>
		/// Stops the timer, <c>run()</c>.
		/// </summary>
		public override void stop() 
		{
			Thread tmp;
			if(timer != null && timer.IsAlive) 
			{
				tmp=timer;
				timer=null;
				tmp.Abort();
				tmp=null;
			}
			timer=null;
		}
		/* ----------------------------- End of Private Methods -------------------------------- */
	}
}
