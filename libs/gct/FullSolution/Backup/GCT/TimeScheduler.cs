using System;
using System.Collections;
using System.Threading;

namespace GCT.Util
{
	/// <remarks>
	/// The scheduler supports varying scheduling intervals by asking the task
	/// every time for its next preferred scheduling interval. Scheduling can
	/// either be <i>fixed-delay</i> or <i>fixed-rate</i>. 
	/// In fixed-delay scheduling, the task's new schedule is calculated
	/// as:<br></br>
	/// new_schedule = time_task_starts + scheduling_interval
	/// <p>
	/// In fixed-rate scheduling, the next schedule is calculated as:<br></br>
	/// new_schedule = time_task_was_supposed_to_start + scheduling_interval</p>
	/// <p>
	/// The scheduler internally holds a queue of tasks sorted in ascending order
	/// according to their next execution time. A task is removed from the queue
	/// if it is cancelled, i.e. if <tt>TimeScheduler.Task.isCancelled()</tt>
	/// returns true.
	/// </p>
	/// <p>
	/// Initially, the scheduler is in <tt>SUSPEND</tt>ed mode, <tt>start()</tt>
	/// need not be called: if a task is added, the scheduler gets started
	/// automatically. Calling <tt>start()</tt> starts the scheduler if it's
	/// suspended or stopped else has no effect. Once <tt>stop()</tt> is called,
	/// added tasks will not restart it: <tt>start()</tt> has to be called to
	/// restart the scheduler.
	/// </p>
	/// </remarks>
	/// <summary>
	/// Fixed-delay and fixed-rate single thread scheduler
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class TimeScheduler
	{
		/// <summary>
		/// The interface that submitted tasks must implement
		/// </summary>
		public interface Task 
		{
			/// <summary>
			/// Returns true if task is cancelled and shouldn't be scheduled again
			/// </summary>
			/// <returns></returns>
			bool cancelled();

			/// <summary>
			/// The next schedule interval
			/// </summary>
			/// <returns>The next schedule interval</returns>
			long nextInterval();

			/// <summary>
			/// Execute the task
			/// </summary>
			void run();

			/// <summary>
			/// Gets the Task name
			/// </summary>
			/// <returns></returns>
			string getName();
		}

		/// <summary>
		/// Internal task class.
		/// </summary>
		private class IntTask : IComparable 
		{
			/// <summary>The user task</summary>
			public Task task;
			/// <summary>The next execution time</summary>
			public long sched;
			/// <summary>Whether this task is scheduled fixed-delay or fixed-rate</summary>
			public bool relative;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="task">The task to schedule and execute</param>
			/// <param name="sched">The next schedule</param>
			/// <param name="relative">Whether scheduling for this task is soft or hard</param>
			public IntTask(Task task, long sched, bool relative) 
			{
				this.task     = task;
				this.sched    = sched;
				this.relative = relative;
			}

			/// <remarks>
			/// If obj is not instance of <tt>IntTask</tt>, then return -1
			/// If obj is instance of <tt>IntTask</tt>, compare the
			/// contained tasks' next execution times. If these times are equal,
			/// then order them randomly <b>but</b> consistently!: return the diff
			/// of their <tt>hashcode()</tt> values
			/// </remarks>
			/// <summary>
			/// Compares two tasks with each other
			/// </summary>
			/// <param name="obj">Object to compare</param>
			/// <returns>An integer representation of the comparison</returns>
			public int CompareTo(Object obj) 
			{
				IntTask other;

				if (!(obj is IntTask)) return(-1);

				other = (IntTask)obj;
				if (sched < other.sched) return(-1);
				if (sched > other.sched) return(1);
				return(task.GetHashCode()-other.task.GetHashCode());
			}

			/// <summary>
			/// Returns the name of the Task
			/// </summary>
			/// <returns>The name of the Task</returns>
			public String toString() 
			{
				if(task == null) return "<unnamed>";
				else return task.getName();
			}
		}

		/// <summary>
		/// The task queue used by the scheduler. Tasks are ordered in increasing
		/// order of their next execution time
		/// </summary>
		private class TaskQueue 
		{
			/// <summary>Sorted list of <code>IntTask</code>s </summary>
			private ArrayList list;

			/// <summary>
			/// Constructor
			/// </summary>
			public TaskQueue() 
			{
				list = new ArrayList();
			}

			/// <summary>
			/// Adds a Task to the list and reorders the queue.
			/// </summary>
			/// <param name="t">Task to add</param>
			public void add(IntTask t)
			{
				list.Add(t);
				list.Sort();
			}

			/// <summary>
			/// Returns the first task in the list
			/// </summary>
			/// <returns></returns>
			public IntTask getFirst()
			{ 
				if(list[0]==null)
					return null;
				return (IntTask)list[0]; 
			}

			/// <summary>
			/// Removes the first Task
			/// </summary>
			public void removeFirst() 
			{
				list.RemoveAt(0);
			}

			/// <summary>
			/// Reschedules the first element
			/// </summary>
			/// <param name="sched">Next execution time for the Task</param>
			public void rescheduleFirst(long sched) 
			{
				IEnumerator it  = list.GetEnumerator();
				it.MoveNext();
				IntTask t = (IntTask)it.Current;
				list.RemoveAt(0);
				t.sched = sched;
				add(t);
			}

			/// <summary>
			/// Checks if the list is empty
			/// </summary>
			/// <returns></returns>
			public bool isEmpty() { return(list.Count==0); }

			/// <summary>
			/// Clears all Tasks
			/// </summary>
			public void clear() { list.Clear(); }

			/// <summary>
			/// Returns the number of Tasks
			/// </summary>
			/// <returns></returns>
			public int size() { return list.Count; }
		}


		/// <summary>Default suspend interval (ms)</summary>
		private const long SUSPEND_INTERVAL = 2000;
		/// <remarks>
		/// Needed in case all tasks have been
		/// cancelled and we are still waiting on the schedule time of the task
		/// at the top
		/// </remarks>
		/// <summary>Regular wake-up intervals for scheduler</summary>
		private const long TICK_INTERVAL = 1000;

		/// <summary>State Constant</summary>
		private const int RUN = 0;
		/// <summary>State Constant</summary>
		private const int SUSPEND = 1;
		/// <summary>State Constant</summary>
		private const int STOPPING = 2;
		/// <summary>State Constant</summary>
		private const int STOP = 3;
		/// <summary>TimeScheduler thread name</summary>
		private const String THREAD_NAME = "TimeScheduler.Thread";

		/// <summary>The scheduler thread</summary>
		private Thread thread = null;
		/// <summary>The thread's running state</summary>
		private int thread_state = SUSPEND;
		/// <summary>Time that task queue is empty before suspending the scheduling thread</summary>
		private long suspend_interval = SUSPEND_INTERVAL;
		/// <summary>The task queue ordered according to task's next execution time</summary>
		private TaskQueue queue;

		/// <summary>
		/// Set the thread state to running, create and start the thread
		/// </summary>
		private void _start() 
		{
			thread_state = RUN;
			thread = new Thread(new ThreadStart(_run));
			thread.Name = THREAD_NAME;
			thread.IsBackground = true;
			thread.Start();
		}

		/// <summary>
		/// Restart the suspended thread
		/// </summary>
		private void _unsuspend() 
		{
			thread_state = RUN;
			thread = new Thread(new ThreadStart(_run));
			thread.Name = THREAD_NAME;
			thread.IsBackground = true;
			thread.Start();
		}

		/// <summary>
		/// Set the thread state to suspended
		/// </summary>
		private void _suspend() 
		{
			thread_state = SUSPEND;
			thread = null;
		}

		/// <summary>
		/// Set the thread state to stopping
		/// </summary>
		private void _stopping() 
		{
			thread_state = STOPPING;
		}

		/// <summary>
		/// Set the thread state to stopped
		/// </summary>
		private void _stop() 
		{
			thread_state = STOP;
			thread = null;
		}


		/// <remarks>
		/// Get the first task, if the running time hasn't been
		/// reached then wait a bit and retry. Else reschedule the task and then
		/// run it. 
		/// </remarks>
		/// <summary>
		/// If the task queue is empty, sleep until a task comes in or if slept
		/// for too long, suspend the thread.
		/// </summary>
		private void _run() 
		{
			IntTask intTask;
			Task    task;
			long    currTime, execTime, waitTime, intervalTime, schedTime;

			while(true) 
			{
				lock(this) 
				{
					if (thread == null) return;
				}

				lock(queue) 
				{
					while(true) 
					{
						if (!queue.isEmpty())
							break;
						try 
						{
							Monitor.Wait(queue, (int)suspend_interval);
						} 
						catch(ThreadInterruptedException ex) 
						{ 
							return;
						}
						if (!queue.isEmpty())
							break;
						_suspend();
						return;
					}

					intTask = queue.getFirst();
					lock(intTask) 
					{
						task = intTask.task;
						if (task.cancelled()) 
						{
							queue.removeFirst();
							continue;
						}
						currTime = System.Environment.TickCount;
						execTime = intTask.sched;
						if ((waitTime = execTime - currTime) <= 0) 
						{
							// Reschedule the task
							intervalTime = task.nextInterval();
							schedTime = intTask.relative?
								currTime+intervalTime : execTime+intervalTime;
							queue.rescheduleFirst(schedTime);
						}
					}
					if (waitTime > 0) 
					{
						try 
						{
							Monitor.Wait(queue,(int)waitTime);
						} 
						catch(ThreadInterruptedException ex) { return; }
						continue;
					}
				}

				try 
				{
					task.run();
				} 
				catch(Exception ex) 
				{
					Trace.error("TimeScheduler._run()", ex.Message + "/n" + ex.StackTrace);
				}
			}
		}

		/// <summary>
		/// Create a scheduler that executes tasks in dynamically adjustable
		/// intervals
		/// </summary>
		/// <param name="suspend_interval">
		/// The time that the scheduler will wait for
		/// at least one task to be placed in the task queue before suspending
		/// the scheduling thread
		/// </param>
		public TimeScheduler(long suspend_interval) 
		{
			queue = new TaskQueue();
			this.suspend_interval = suspend_interval;
		}

		/// <summary>
		/// Create a scheduler that executes tasks in dynamically adjustable
		/// intervals
		/// </summary>
		public TimeScheduler() : this(SUSPEND_INTERVAL){}

		/// <remarks>
		/// <b>Relative Scheduling</b>
		/// <tt>true</tt>:<br></br>
		/// Task is rescheduled relative to the last time it <i>actually</i>
		/// started execution
		///	<p>
		/// <tt>false</tt>:<br></br>
		/// Task is scheduled relative to its <i>last</i> execution schedule. This
		/// has the effect that the time between two consecutive executions of
		/// the task remains the same.
		/// </p>
		/// </remarks>
		/// <summary>
		/// Add a task for execution at adjustable intervals
		/// </summary>
		/// <param name="t">The task to execute</param>
		/// <param name="relative">Use relative scheduling</param>
		public void add(Task t, bool relative) 
		{
			long interval, sched;

			if ((interval = t.nextInterval()) < 0) return;
			sched = System.Environment.TickCount + interval;

			lock(queue) 
			{
				queue.add(new IntTask(t, sched, relative));
				switch(thread_state) 
				{
					case RUN: Monitor.PulseAll(queue); break;
					case SUSPEND: _unsuspend(); break;
					case STOPPING: break;
					case STOP: break;
				}
			}
		}

		/// <summary>
		/// Add a task for execution at adjustable intervals
		/// </summary>
		/// <param name="t">The task to execute</param>
		public void add(Task t) { add(t, true); }

		/// <summary>
		/// Start the scheduler, if it's suspended or stopped
		/// </summary>
		public void start() 
		{
			lock(queue) 
			{
				switch(thread_state) 
				{
					case RUN: break;
					case SUSPEND: _unsuspend(); break;
					case STOPPING: break;
					case STOP: _start(); break;
				}
			}
		}


		/// <summary>
		/// Stop the scheduler if it's running. Switch to stopped, if it's
		/// suspended. Clear the task queue.
		/// </summary>
		public void stop()
		{
			// i. Switch to STOPPING, interrupt thread
			// ii. Wait until thread ends
			// iii. Clear the task queue, switch to STOPPED,
			lock(queue) 
			{
				switch(thread_state) 
				{
					case RUN: _stopping(); break;
					case SUSPEND: _stop(); return;
					case STOPPING: return;
					case STOP: return;
				}
				thread.Interrupt();
			}

			thread.Join();
	    
			lock(queue) 
			{
				queue.clear();
				_stop();
			}
		}
	}
}
