using System;

namespace Testing
{
	public class TimeServer : MarshalByRefObject
	{
		public TimeServer()
		{
		}

		public DateTime getCurrentTime()
		{
			return DateTime.Now;
		}
	}
}
