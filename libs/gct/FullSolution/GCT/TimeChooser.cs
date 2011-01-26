using System;
using System.Collections;
using System.Runtime.Remoting.Messaging;

namespace GCT.Remoting
{
	/// <summary>
	/// Selects the median DateTime from the Remoting responses.
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class TimeChooser : RemotingRespChooser
	{
		/// <summary>
		/// Called to choose the median of the responses
		/// </summary>
		/// <param name="responses">List of Remoting Responses</param>
		/// <returns>The median of the DateTime responses</returns>
		public IMessage choice(ArrayList responses)
		{
			MethodResponse retValue = null;
			long total = 0;
			long count = 0;
			long average;

			foreach(MethodResponse rsp in responses)
			{
				if(!(rsp.ReturnValue is DateTime))
					return null;
				total += ((DateTime)rsp.ReturnValue).Ticks;
				count++;
				if(Trace.trace)
					Trace.info("TimeChooser.choice()","Time Response = " + ((DateTime)rsp.ReturnValue).ToString("HH:mm:ss:ff"));
			}
			average = (long)total/count;

			if(Trace.trace)
				Trace.info("TimeChooser.choice()","Average Time = " + new DateTime(average).ToString("HH:mm:ss:ff"));

			//Get the closest value
			long date1,date2,diff1,diff2;

			foreach(MethodResponse rsp in responses)
			{
				if(retValue==null)
				{
					retValue = rsp;
					continue;
				}
				date1 = ((DateTime)rsp.ReturnValue).Ticks;
				date2 = ((DateTime)retValue.ReturnValue).Ticks;
				diff1 = date1-average;
				diff2 = date2-average;

				if(diff1<0)
					diff1 = -1*diff1;
				diff2 = date2-average;

				if(diff2<0)
					diff2 = -1*diff2;

				if(diff1<diff2)
					retValue = rsp;
			}
			if(Trace.trace)
				Trace.info("TimeChooser.choice()","Choosen Time = " + ((DateTime)retValue.ReturnValue).ToString("HH:mm:ss:ff"));
			return retValue;
		}
	}
}
