using System;
using GCT;
using GCT.Util;

namespace Testing
{
	public class TimerTask : TimeScheduler.Task
	{
		long interval = 1000;
		String name = "TimerTask";
		public int start = 0;
		int increment = 0;
		bool stopped = false;
		int running_count = 0;

		public TimerTask(int interval, String name, int start, int increment)
		{
			this.name = name;
			this.interval = interval;
			this.start = start;
			this.increment = increment;
		}

		public void run()
		{
			if(running_count==10)
				stopped = true;

			if (!stopped)
			{
				start += increment;
				running_count++;
			}
		}

		public bool cancelled()
		{
			return stopped;
		}

		public String getName()
		{
			return name;
		}

		public long nextInterval()
		{
			return interval;
		}
	}
}
