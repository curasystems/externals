using System;
using System.Threading;


namespace GCT.Util
{
	/// <remarks>
	/// The caller may choose to check
	/// for the result at a later time, or immediately and it may block or not. Both the caller and responder have to
	/// know the promise.
	/// </remarks>
	/// <summary>
	/// Allows a thread to submit an asynchronous request and to wait for the result.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class Promise 
	{
		/// <summary>The result of the request</summary>
		Object result=null;
		/// <summary>Used to wait on the result</summary>
		Object mutex=new Object();
    
		/// <summary>
		/// If result was already submitted, returns it immediately, else blocks until
		/// results becomes available. 
		/// </summary>
		/// <param name="timeout">Maximum time to wait for result.</param>
		/// <returns>Promise result</returns>
		public Object getResult(long timeout) 
		{
			Object ret=null;

			lock(mutex) 
			{
				if(result != null) 
				{
					ret=result;
					result=null;
					return ret;
				}
				if(timeout <= 0) 
				{
					try 
					{
						Monitor.Wait(mutex);
					} 
					catch(Exception ex) {}
				}
				else 
				{
					try 
					{
						Monitor.Wait(mutex,(int)timeout,true);} 
					catch(Exception ex) {}
				}
				if(result != null) 
				{
					ret=result;
					result=null;
					return ret;
				}
				return null;
			}
		}
    
		/// <summary>
		/// Checks whether result is available. Does not block.
		/// </summary>
		/// <returns>Result if available</returns>
		public Object checkForResult() 
		{
			lock(mutex) 
			{
				return result;
			}
		}

		/// <summary>
		/// Sets the result and notifies any threads waiting for it
		/// </summary>
		/// <param name="obj">Result of request</param>
		public void setResult(Object obj) 
		{
			lock(mutex) 
			{
				result=obj;
				Monitor.PulseAll(mutex);
			}
		}


		/** Causes all waiting threads to return */
		/// <summary>
		/// Clears the result and causes all waiting threads to return
		/// </summary>
		public void reset() 
		{
			lock(mutex) 
			{
				result=null;
				Monitor.PulseAll(mutex);
			}
		}

		/// <summary>
		/// String representation of the result
		/// </summary>
		/// <returns>String representation of the result</returns>
		public String toString() 
		{
			return "result=" + result;
		}

	}
}
