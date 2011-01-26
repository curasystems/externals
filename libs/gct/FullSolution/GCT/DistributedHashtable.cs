using System;
using System.Runtime.Serialization;
using System.Collections;
using GCT;
using System.Threading;

namespace GCT.Blocks
{	/// <remarks>
	/// 
	/// </remarks>
	/// <summary>
	/// Provides a simple distributed hashtable. This class behaves identically to the standard
	/// system.collections.Hashtable class (e.g. Hashtable[i] = "value" can be used). However, all 
	/// non-read-only updates will be transmitted to the group. The local hashtables will then be updated
	/// on receiving the update message.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class DistributedHashtable: Hashtable, MessageListener
	{
		/// <summary>The channel that will be used for</summary>
		private Channel channel;
		/// <summary>Used to automatically retrieved messages from the channel</summary>
		private PullPushAdapter ad;
		/// <summary>Object that will be notified of Add, Remove and Clear operations to the hashtable</summary>
		private Listener listener = null;
		/// <summary>Used to synchronise initial hashtable state</summary>
		private object state_mutex = new object();

		/// <summary>
		/// Constructor: Initialises/Connects channel and retreives current hashtable state.
		/// </summary>
		/// <param name="channel">The Channel to be used for sharing updates.</param>
		/// <param name="channelName">Name of channel to connect to.</param>
		/// <param name="listener">Object that will receive notification of updates.</param>
		public DistributedHashtable(Channel channel,string channelName, Listener listener): base()
		{
			this.channel = channel;
			this.ad = new PullPushAdapter(channel, this);
			this.listener=listener;
			Console.WriteLine("About to connect to channel -->" + DateTime.Now.ToLongTimeString());
			channel.connect(channelName);
			Console.WriteLine("Connected -->" + DateTime.Now.ToLongTimeString());
			ad.start();
			if(channel.getView().size()>1)
				getState();
		}

		/// <summary>
		/// Gets or Sets the value corresponding to the specified key. Setting
		/// a value causes an update on all hashtables.
		/// </summary>
		public override object this[object key]
		{
			get
			{
				return base[key];
			}
			set
			{
				Update(key,value);
			}
		}

		/// <summary>
		/// Sends a request for the current state. Method returns once response is received.
		/// </summary>
		private void getState()
		{
			int stateTimeout= 15000;
			Message msg;
			msg = new Message(null,null,new HTUpdate(HTUpdate.GET_STATE,null,null));
			bool stateReturned = false;
			lock(state_mutex) 
			{
				channel.send(msg);
				stateReturned = Monitor.Wait(state_mutex,stateTimeout,true);
			}
			// if stateReturned = false then no coordinator present.
		}

		/// <summary>
		/// Updates an entry in the hashtable. Called from "hashtable[key] = value". 
		/// </summary>
		/// <param name="key">Key of hashtable entry</param>
		/// <param name="val">Value of hashtable entry</param>
		private void Update(object key, object val)
		{
			//shouldnt override existing members
			Message msg;
			msg = new Message(null,null,new HTUpdate(HTUpdate.UPDATE,key,val));
			channel.send(msg);
		}

		/// <summary>
		/// Adds a value into the Hashtable. This does not update an existing entry.
		/// </summary>
		/// <param name="key">Key of hashtable entry</param>
		/// <param name="val">Value of hashtable entry</param>
		public override void Add(object key, object val)
		{
			//shouldnt override existing members
			Message msg;
			msg = new Message(null,null,new HTUpdate(HTUpdate.ADD,key,val));
			channel.send(msg);
		}
		
		/// <summary>
		/// Clears the hashtable.
		/// </summary>
		public override void Clear()
		{
			Message msg;
			msg = new Message(null,null,new HTUpdate(HTUpdate.CLEAR,null,null));
			channel.send(msg);
		}

		/// <summary>
		/// Removes the specified key from the hashtable.
		/// </summary>
		/// <param name="key">Key of hashtable entry</param>
		public override void Remove(object key)
		{
			Message msg;
			msg = new Message(null,null,new HTUpdate(HTUpdate.REMOVE,key,null));
			channel.send(msg);
		}

		/// <summary>
		/// Closes the associated channel.
		/// </summary>
		public void Close()
		{
			channel.close();
			ad.stop();
		}
		
		// -=-=-=-=-=-=-=- MessageListener Interface -=-=-=-=-=-=-=-
		/// <summary>
		/// Message Listener Implementation. Receives messages from the channel and 
		/// updates the hashtable accordingly.
		/// </summary>
		/// <param name="msg">The received message</param>
		public void receive(GCT.Message msg) 
		{
			if(msg == null)
				return;
			object obj = msg.getObject();
			if(obj==null) return;
			if(!(obj is HTUpdate)) return;
			HTUpdate update = (HTUpdate)obj;
			switch(update.Type)
			{
				case HTUpdate.ADD:
					if(update.Key!=null && update.Value!=null)
						_Add(update.Key,update.Value);
					break;
				case HTUpdate.UPDATE:
					if(update.Key!=null && update.Value!=null)
						_Update(update.Key,update.Value);
					break;
				case HTUpdate.REMOVE:
					if(update.Key!=null)
						_Remove(update.Key);
					break;
				case HTUpdate.CLEAR:
					_Clear();
					break;
				case HTUpdate.SET_STATE:
					if(update.Value!=null && update.Value is Hashtable)
					{
						lock(state_mutex)
						{
							_State((Hashtable)update.Value);
							Monitor.Pulse(state_mutex);
						}
					}
					break;
				case HTUpdate.GET_STATE:
					if(channel.getView().getMembers()[0].Equals(channel.getLocalAddress()))
						channel.send(new Message(msg.Source,channel.getLocalAddress(),new HTUpdate(HTUpdate.SET_STATE,null,base.Clone())));
					break;
			}
		}

		// -=-=-=-=-=-=-=- Called Methods Interface -=-=-=-=-=-=-=-
		/// <summary>
		/// Called once remote hashtable Add is required.
		/// </summary>
		/// <param name="key">Key of hashtable entry</param>
		/// <param name="val">Value of hashtable entry</param>
		private void _Add(object key, object val)
		{
			if(base.Contains(key))
			{
				if(Trace.trace)
					Trace.warn("DistributedHashtable._Add()", "Key already exists in the hashtable");
				return;				
			}
			if(key==null)
			{
				if(Trace.trace)
					Trace.error("DistributedHashtable._Add()", "Key is NULL");
				return;				
			}
			base.Add(key,val);
			if(listener!=null)
				listener.entryAdded(key,val);
		}

		/// <summary>
		/// Called once remote hashtable Update is required.
		/// </summary>
		/// <param name="key">Key of hashtable entry</param>
		/// <param name="val">Value of hashtable entry</param>
		private void _Update(object key, object val)
		{
			if(key==null)
			{
				if(Trace.trace)
					Trace.error("DistributedHashtable._Update()", "Key is NULL");
				return;				
			}
			base[key] = val;
			if(listener!=null)
				listener.entryUpdated(key,val);
		}

		/// <summary>
		/// Called once remote hashtable Clear is required.
		/// </summary>
		private void _Clear()
		{
			base.Clear();
			if(listener!=null)
				listener.HTCleared();
		}

		/// <summary>
		/// Called once remote hashtable Remove is required.
		/// </summary>
		/// <param name="key">Key of hashtable entry</param>
		private void _Remove(object key)
		{
			if(key==null)
			{
				if(Trace.trace)
					Trace.error("DistributedHashtable._Remove()", "Key is NULL");
				return;				
			}
			base.Remove(key);
			if(listener!=null)
				listener.entryDeleted(key);
		}

		/// <summary>
		/// Called once Set_State message is received. THe current hastable is 
		/// cleared and the new values inserted.
		/// </summary>
		/// <param name="ht">The new Hashtable to install</param>
		private void _State(Hashtable ht)
		{
			if(ht==null)
			{
				if(Trace.trace)
					Trace.error("DistributedHashtable._State()", "New state (i.e. Hashtable) is NULL");
				return;				
			}
			base.Clear();
			foreach(DictionaryEntry e in ht)
				base.Add(e.Key,e.Value);
		}


		/// <summary>
		/// Interface for objects that required notification of hashtable changes
		/// </summary>
		public interface Listener
		{
			/// <summary>
			/// Notification of a added entry.
			/// </summary>
			/// <param name="key">Key of hashtable entry</param>
			/// <param name="val">Value of hashtable entry</param>
			void entryAdded(object key, object val);
			/// <summary>
			/// Notification of an updated entry.
			/// </summary>
			/// <param name="key">Key of hashtable entry</param>
			/// <param name="val">Value of hashtable entry</param>
			void entryUpdated(object key, object val);
			/// <summary>
			/// Notification of a deleted entry.
			/// </summary>
			/// <param name="key">Key of hashtable entry</param>
			void entryDeleted(object key);
			/// <summary>
			/// Notification of the hashtable being cleared.
			/// </summary>
			void HTCleared();
		}
	}

	/// <summary>
	/// Update for distributed hashtable.
	/// </summary>
	[Serializable]
	public class HTUpdate
	{
		/// <summary>Update Type: Add</summary>
		public const int ADD	= 0;
		/// <summary>Update Type: Clear</summary>
		public const int CLEAR	= 1;
		/// <summary>Update Type: Remove</summary>
		public const int REMOVE	= 2;
		/// <summary>Update Type: Update</summary>
		public const int UPDATE	= 3;
		/// <summary>Update Type: Get the full state</summary>
		public const int GET_STATE	= 4;
		/// <summary>Update Type: Set the full state</summary>
		public const int SET_STATE	= 5;

		/// <summary>Key of modified entry</summary>
		private object	key =	null;
		/// <summary>Value of modified entry</summary>
		private object	val =	null;
		/// <summary>Type of modification</summary>
		private int		type;
		
		/// <remarks> 
		/// Throws an exception if both key and value are not serializable.
		/// Depending on the update type either or both variables may be null.
		/// </remarks>
		/// <summary>
		/// Constructor.
		/// </summary>
		/// <param name="type">Type of modification</param>
		/// <param name="key">Key of modified entry</param>
		/// <param name="val">Value of modified entry</param>
		public HTUpdate(int type, object key,object val)
		{
			this.type = type;
			if(key!=null &&!key.GetType().IsSerializable)
				throw new Exception("Key for Distributed hashtable update is not serializable.");
			if(val!=null &&!val.GetType().IsSerializable)
				throw new Exception("Value for Distributed hashtable update is not serializable.");
			this.key = key;
			this.val = val;
		}

		/// <summary>Gets the dictionary key associated with the modification</summary>
		public object Key
		{
			get{return key;}
		}

		/// <summary>Gets the dictionary value associated with the modification</summary>
		public object Value
		{
			get{return val;}
		}

		/// <summary>Gets the type of hashtable modification</summary>
		public int Type
		{
			get{return type;}
		}

		/// <summary>
		/// Converts the type of modification in to a text representation
		/// </summary>
		/// <param name="t">The type of modification</param>
		/// <returns>String representation of modification type</returns>
		static string type2String(int t) 
		{
			switch(t) 
			{
				case ADD:    return "ADD";
				case REMOVE: return "REMOVE";
				case CLEAR:  return "CLEAR";
				case UPDATE: return "UPDATE";
				default:     return "<unknown>";
			}

		}
	}
}
