using System;
using System.Collections;
using System.Threading;

namespace GCT.Util
{
	/// <summary>
	/// Wrapper class of <c>Queue</c>. Extends operations by allowing remove and peek
	/// operations to have timeouts
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class MQueue
	{
		/// <summary>If <c>MQueue</c> is closed then no messages can be removed.</summary>
		private bool closed = false;

		/// <summary>Internal <c>Queue</c></summary>
		private Queue q  = new Queue();

		/// <summary>
		/// Constructor.
		/// </summary>
		public MQueue()
		{
			if (!q.IsSynchronized)
				q = Queue.Synchronized(q);
		}

		/// <summary>
		/// Number of Objects in the queue
		/// </summary>
		public int Count
		{
			get{return q.Count;}
		}

		/// <summary>
		/// Gets and sets the access state of the queue 
		/// </summary>
		public bool Closed
		{
			get{return closed;}
			set{closed = value;}
		}

		/// <summary>
		/// Adds an object to the queue
		/// </summary>
		/// <param name="obj">Object to add to the queue</param>
		public void Add(Object obj)
		{
			lock(q)
			{
				if(closed)
					throw new QueueClosedException();
				q.Enqueue(obj);
				Monitor.Pulse(q);
			}
		}

		/// <summary>
		/// Removes the first element in the queue.
		/// </summary>
		/// <param name="timeout">Time to wait for an element in the queue</param>
		/// <returns>The first element or <c>null</c> if timeout expired</returns>
		public Object Remove(int timeout)
		{
			Object retValue = null;
			lock(q)
			{
				if(closed)
					throw new QueueClosedException();
				if(q.Count == 0)
				{
					Monitor.Wait(q,timeout);

				}
				if(closed)
					throw new QueueClosedException();
				if(q.Count != 0)
					retValue = q.Dequeue();
			}
			return retValue;
		}

		/// <summary>
		/// Removes the first element in the queue. Waits indefinately for an element to arrive
		/// </summary>
		/// <returns>The first element in the queue.</returns>
		public Object Remove()
		{
			Object retValue = null;
			lock(q)
			{
				if(closed)
					throw new QueueClosedException();
				if(q.Count == 0)
				{
					Monitor.Wait(q);
				}
				if(closed)
					throw new QueueClosedException();
					retValue = q.Dequeue();
			}
			return retValue;
		}


		/// <summary>
		/// Returns the first element in the queue.
		/// </summary>
		/// <param name="timeout">Time to wait for an element in the queue</param>
		/// <returns>The first element or <c>null</c> if timeout expired</returns>
		public Object peek(int timeout)
		{
			Object retValue = null;
			lock(q)
			{
				if(closed)
					throw new QueueClosedException();
				if(q.Count == 0)
				{
					Monitor.Wait(q,timeout);
				}
				if(closed)
					throw new QueueClosedException();
				if(q.Count != 0)
					retValue = q.Peek();
			}
			return retValue;
		}

		/// <summary>
		/// Closes access to the queue
		/// </summary>
		public void close() 
		{
			closed=true;
			
			lock(q) 
			{
				closed=true;
				try 
				{
					Monitor.PulseAll(q);
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("Queue.close()", "exception=" + e);
				}
			}
		}
		
		/// <summary>
		/// Closes the queue, clears all elements and then opens the queue for access
		/// </summary>
		public void reset()
		{
			if(!closed)
				close();
			lock(q) 
			{
				q.Clear();
				closed=false;
			}
		}
	}
}
