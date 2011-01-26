using System;
using System.Data;

using GCT;
using GCT.Stack;

namespace GCT.Protocols
{
	/// <remarks>
	/// This <c>Protocol</c> should only be used for testing
	/// </remarks>
	/// <summary>
	/// Protocol: Discards up or down messages based on a percentage.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class DISCARD : Protocol
	{
		/// <summary>
		/// probability of dropping up msgs
		/// </summary>
		double probUp = 0.0; 

		/// <summary>
		/// probability of dropping down msgs
		/// </summary>
		double probDown = 0.0; 

		/// <summary>
		/// if true don't discard messages sent/received in this stack
		/// </summary>
		bool excludeItself = false;  
		
		/// <summary>
		/// Local Address
		/// </summary>
		Address localAddress;

		/// <summary>
		/// Used to randomly discard messages.
		/// </summary>
		Random random = new Random();
		
		/// <summary>
		/// Constructor
		/// </summary>
		public DISCARD()
		{
			name = "DISCARD";
		}

		/// <summary>
		/// Returns unique <c>Protocol</c> name
		/// </summary>
		/// <returns></returns>
		public String getName()
		{
			return name;
		}

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props)
		{
			if(props.Contains("up")) 
			{
				probUp = Convert.ToDouble(props["up"]);
				props.Remove("up");
			}
			if(props.Contains("down")) 
			{
				probDown = Convert.ToDouble(props["down"]);
				props.Remove("down");
			}
			if(props.Contains("excludeItself")) 
			{
				excludeItself = Convert.ToBoolean(props["excludeItself"]);
				props.Remove("excludeItself");
			}

			if (props.Count > 0)
			{
				return false;
			}
			return true;
		}

		/// <summary>
		/// Processes <c>Events</c> traveling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt)
		{
			Message msg;
			double r;

			if (evt.Type == Event.SET_LOCAL_ADDRESS)
				localAddress = (Address) evt.Arg;

			if (evt.Type == Event.MSG)
			{
				msg = (Message) evt.Arg;
				if (probUp > 0)
				{
					r = (double)((double)random.Next(100)/(double)100);
					if (r < probUp)
					{
						if (excludeItself && msg.Source.Equals(localAddress))
						{
							if (Trace.trace) Trace.info("DISCARD.up()", "excluding itself");
						}
						else
						{
							if (Trace.trace) Trace.info("DISCARD.up()", "dropping message");
							return;
						}
					}
				}
			}


			passUp(evt);
		}

		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt)
		{
			Message msg;
			double r;

			if (evt.Type == Event.MSG)
			{
				msg = (Message) evt.Arg;

				if (probDown > 0)
				{
					r = (double)((double)random.Next(100)/(double)100);
					if (r < probDown)
					{
						if (excludeItself && msg.Source.Equals(localAddress))
						{
							if (Trace.trace) Trace.info("DISCARD.down()", "excluding itself");
						}
						else
						{
							if (Trace.trace) Trace.info("DISCARD.down()", "dropping message");
							return;
						}
					}
				}

			}
			passDown(evt);
		}
	}

}
