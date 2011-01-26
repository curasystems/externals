using System;
using System.Threading;

namespace GCT.Util
{
	/// <summary>
	/// Interrupted exception
	/// <p><b>Author:</b> Chris Koiak, Bela Ban, John Giorgiadis</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class IntException : Exception
	{
		/// <summary>Constructor</summary>
		public IntException() : base() {}
		/// <summary>Constructor</summary>
		public IntException(String msg) : base(msg) {}
	}
	
	/// <summary>
	/// Exception thrown when a lock request would block the caller
	/// <p><b>Author:</b> Chris Koiak, Bela Ban, John Giorgiadis</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class BlockException : Exception 
	{
		/// <summary>Constructor</summary>
		public BlockException() : base() {}
		/// <summary>Constructor</summary>
		public BlockException(String msg) : base(msg) {}
	}

	/// <summary>
	/// Lock allowing multiple reads or a single write. Waiting writes have 
	/// priority over new reads. 
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class RWLock 
	{
		/// <summary>Number of active read locks</summary>
		private int _reads;
		/// <summary>Number of pending write lock requests</summary>
		private int _waitWrites;
		/// <summary>Whether the write lock is held</summary>
		private bool _write;
	
		/// <summary>
		/// Constructor: No lock exist at initialisation
		/// </summary>
		public RWLock() 
		{
			_reads      = 0; 
			_waitWrites = 0; 
			_write      = false; 
		}
	
		/// <summary>
		/// Obtain a read lock 
		/// </summary>
		public void readLock() 
		{ 
			lock(this)
			{

				while(_write || _waitWrites != 0) 
				{
					try 
					{
						Monitor.Wait(this);
					} 
					catch(ThreadInterruptedException ex) 
					{
						throw new IntException(); 
					}
				}
				++_reads; 
			}
		}

		/// <summary>
		/// Revoke the read lock
		/// </summary>
		public void readUnlock() 
		{ 
			lock(this)
			{
				if (--_reads == 0) 
					Monitor.PulseAll(this); 
			}
		}
	
		/// <summary>
		/// Obtain the read lock immediately
		/// </summary>
		public void readLockNoBlock()
		{
			lock(this)
			{
				if (_write || _waitWrites != 0) 
					throw new BlockException("block on read");
				readLock();
			}
		}
	
	
		/// <summary>
		/// Obtain a write lock 
		/// </summary>
		public void writeLock() 
		{ 
			lock(this)
			{
				while(_write || _reads != 0) 
				{ 
					++_waitWrites; 
					try 
					{
						Monitor.Wait(this);
					} 
					catch(ThreadInterruptedException ex) 
					{
						throw new IntException();
					} 
					finally { --_waitWrites; }
				}
				_write = true; 
			}
		}

		/// <summary>
		/// Revoke the write lock 
		/// </summary>
		public void writeUnlock() 
		{ 
			lock(this)
			{
				_write = false; 
				Monitor.PulseAll(this);
			}
		}
	
		/// <summary>
		/// Obtain the write lock immediatelly
		/// </summary>
		public void writeLockNoBlock()
		{
			lock(this)
			{
				if (_write || _reads != 0) 
					throw new BlockException("block on write");
				writeLock();
			}
		}
	}
}
