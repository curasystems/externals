using System;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using System.Diagnostics;
using System.Collections;
using System.IO;
using System.Data;

namespace GCT
{
	/// <remarks>
	/// A Message encapsulates data sent to members of a group. 
	/// It contains among other things the address of the sender, 
	/// the destination address, a payload (byte buffer) and a list of 
	/// headers. Headers are added by protocols on the sender side and 
	/// removed by protocols on the receiver's side.
	/// </remarks>
	/// <summary>
	/// Message passed between members of a group.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class Message : ISerializable 
	{
		/// <summary>Destination of the message</summary>
		protected Address dest_addr = null;
		/// <summary>Source of the message</summary>
		protected Address src_addr = null;
		/// <summary>Byte buffer of payload associated with the message</summary>
		protected byte[] buf = null;
		/// <summary>Headers added to the message</summary>
		protected PropertyCollection headers = null;
		
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="dest">Destination of the message</param>
		/// <param name="src">Source of the message</param>
		/// <param name="buf">Byte buffer of payload associated with the message</param>
		public Message(Address dest, Address src, byte[] buf) 
		{
			dest_addr = dest;
			src_addr = src;
			this.buf = buf;
			headers = new PropertyCollection();
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="dest">Destination of the message</param>
		/// <param name="src">Source of the message</param>
		/// <param name="obj">Serialisable payload OR array of <c>Message</c>s</param>
		public Message(Address dest, Address src, Object obj) 
		{
			dest_addr = dest;
			src_addr = src;
			headers = new PropertyCollection();
			if(obj!=null)
			{
				if(obj is Array)
					setMessageArray((Array)obj);
				else if(obj.GetType().IsSerializable)
					setObject(obj);
				else
					throw new Exception("Message can only contain an Array of messages or a ISerializable object");
			}
		}
		
		/// <summary>
		/// Gets and sets the destination of the message
		/// </summary>
		public Address Destination
		{
			get{return dest_addr;}
			set{dest_addr = value;}
		}

		/// <summary>
		/// Gets and sets the source of the message
		/// </summary>
		public Address Source
		{
			get{return src_addr;}
			set{src_addr = value;}
		}

		/// <summary>
		/// Gets and sets the payload (bute buffer) of the message
		/// </summary>
		public byte[] Buffer
		{
			get{return buf;}
			set{buf = value;}
		}

		/// <summary>
		/// Gets the collection of Headers added to the message
		/// </summary>
		public PropertyCollection Headers
		{
			get{return headers;}
		}

		/// <summary>
		/// Compares a second <c>Message</c> for equality
		/// </summary>
		/// <param name="obj">Second Message object</param>
		/// <returns>True if Messages are equal</returns>
		public override bool Equals(Object obj)
		{
			if (!(obj is Message))
				return false;
			Message msg2 = (Message)obj;
			if ((dest_addr == null || msg2.Destination == null) && 
				(msg2.Destination!=null || dest_addr != null))
				return false;
			else if(dest_addr != null && !dest_addr.Equals(msg2.Destination))
				return false;
			
			if ((src_addr == null || msg2.Source == null) && 
				(msg2.Source!=null || src_addr != null))
				return false;
			else if(src_addr != null && !src_addr.Equals(msg2.Source))
				return false;

			if (buf != msg2.Buffer ||
				headers.Count != msg2.Headers.Count)
				return false;

			foreach(DictionaryEntry h in headers)
			{
				if(!msg2.Headers.Contains(h.Key))
					return false;
			}

			return true;
		}

		/// <summary>
		/// Returns the hash value for a Message
		/// </summary>
		/// <returns>The hash value for a Message</returns>
		public override int GetHashCode()
		{
			int retValue = headers.GetHashCode();
			ArrayList hc = new ArrayList();
			if (dest_addr!=null)
				hc.Add(dest_addr.GetHashCode());
			if (src_addr!=null)
				hc.Add(src_addr.GetHashCode());
			if (buf!=null)
				hc.Add(buf.GetHashCode());

			for(int i=0;i<hc.Count;i++)
				retValue = retValue.GetHashCode() ^ hc[i].GetHashCode();

			return retValue;
		}

		/// <summary>
		/// Serialises an object in to the payload
		/// </summary>
		/// <param name="obj">Object to serialise</param>
		public void setObject(Object obj) 
		{
			if (buf != null)
				return;
			if(!obj.GetType().IsSerializable)
				throw new Exception("Specified object for message is not serializable");

			BinaryFormatter bf = new BinaryFormatter();
			MemoryStream ms = new MemoryStream();
			bf.Serialize(ms,obj);
			buf = new byte[ms.Length];
			ms.Position = 0;
			ms.Read(buf,0,buf.Length);
			ms.Close();
			
		}

		/// <summary>
		/// Deserialises an Object from the payload
		/// </summary>
		/// <returns>Deserialised Object</returns>
		public Object getObject() 
		{
			if (buf == null)
				return null;
			BinaryFormatter bf = new BinaryFormatter();
			Object obj = bf.Deserialize(new MemoryStream(buf));
			return obj;
		}

		/// <remarks>
		/// This is used by the NAKACK layer to send multiple missed messages. 
		/// The alternative is to sent each missed message one at a time.
		/// </remarks>
		/// <summary>
		/// Writes an array of messages into the payload.
		/// </summary>
		/// <param name="obj">Array of Message objects</param>
		public void setMessageArray(Array obj) 
		{
			if (buf != null)
				return;

			BinaryFormatter bf = new BinaryFormatter();
			MemoryStream ms = new MemoryStream();
		
			MemoryStream tmp = new MemoryStream();

			long length;
			int count = 0;
			foreach(Message m in obj)
			{
				count++;
				tmp = new MemoryStream();
				bf.Serialize(tmp,m);
				length = tmp.Length;

				ms.Write(WriteInt64(length),0,8);
				bf.Serialize(ms,m);
			}
			ms.Write(WriteInt64(0),0,8);

			buf = new byte[ms.Length];
			ms.Position = 0;
			ms.Read(buf,0,(int)ms.Length);
		}

		/// <summary>
		/// Deserialises an array of Messages from the payload
		/// </summary>
		/// <returns>An array of Messages </returns>
		public Array getMessageArray() 
		{
			if (buf == null)
				return null;
			try 
			{
				MemoryStream in_stream = new MemoryStream(buf);
				BinaryFormatter bf = new BinaryFormatter();
				ArrayList messages =new ArrayList();
				
				byte[] size = new byte[8];
				long firstSize = -1;
				byte[] tmpMsg;
				
				while(firstSize!=0)
				{
					in_stream.Read(size,0,size.Length);
					firstSize = convertToLong(size);
					if (firstSize==0)
						break;
					tmpMsg = new byte[firstSize];
					in_stream.Read(tmpMsg,0,tmpMsg.Length);
					Message msg = (Message)bf.Deserialize(new MemoryStream(tmpMsg));
					messages.Add(msg);
				}

				return messages.ToArray(typeof(Message));
			}
			catch (Exception e) 
			{
				if(Trace.trace)
					Trace.error("Message.getObject()", "exception=" + e);
				return null;
			}
		}

		/// <summary>
		/// Creates a byte buffer representation of a <c>long</c>
		/// </summary>
		/// <param name="value"><c>long</c> to be converted</param>
		/// <returns>Byte Buffer representation of a <c>long</c></returns>
		public byte[] WriteInt64(long value)
		{
			byte[] _byteBuffer = new byte[8];
			_byteBuffer[0] = (byte)value;
			_byteBuffer[1] = (byte)(value >> 8);
			_byteBuffer[2] = (byte)(value >> 16);
			_byteBuffer[3] = (byte)(value >> 24);
			_byteBuffer[4] = (byte)(value >> 32);
			_byteBuffer[5] = (byte)(value >> 40);
			_byteBuffer[6] = (byte)(value >> 48);
			_byteBuffer[7] = (byte)(value >> 56);
			return _byteBuffer;
		} // WriteInt32

		/// <summary>
		/// Creates a <c>long</c> from a byte buffer representation
		/// </summary>
		/// <param name="_byteBuffer">Byte Buffer representation of a <c>long</c></param>
		/// <returns></returns>
		private long convertToLong(byte[] _byteBuffer) 
		{        
			return (long)((_byteBuffer[0] & 0xFF) |
				_byteBuffer[1] << 8 |
				_byteBuffer[2] << 16 |
				_byteBuffer[3] << 24 |
				_byteBuffer[4] << 32 |
				_byteBuffer[5] << 40 |
				_byteBuffer[6] << 48 |
				_byteBuffer[7] << 56);
		} // ReadInt32


		/// <summary>
		/// Copy the Message
		/// </summary>
		/// <returns>A copy of the current Message</returns>
		public Message copy() 
		{
			Message retval = new Message(null,null,null);
			retval.dest_addr = dest_addr;
			retval.src_addr = src_addr;
			if(buf != null)
				retval.buf=buf;
	
			if(headers != null)
			{
				retval.headers = new PropertyCollection();
				foreach(DictionaryEntry h in headers)
				{
					retval.headers.Add(h.Key,h.Value);
				}
			}
			return retval;
		}

		/// <remarks>
		/// Quite an expensive method as it has to serialise the message to 
		/// get the size of it. It's use should be limited
		/// </remarks>
		/// <summary>
		/// Serialised size of the message
		/// </summary>
		/// <returns></returns>
		public int size()
		{
			MemoryStream ms = new MemoryStream();
			BinaryFormatter bf = new BinaryFormatter();
			bf.Serialize(ms,this);
			return (int)ms.Length;
		}

		/*---------------------- Used by protocol layers ----------------------*/
		/// <summary>
		/// Adds a header in to the Message
		/// </summary>
		/// <param name="key">Protocol Name associated with the header</param>
		/// <param name="hdr">Implementation of the Header class</param>
		public void putHeader(String key, Header hdr) 
		{
			headers.Add(key, hdr);
		}

		/// <summary>
		/// Removes a header associated with a Protocol layer
		/// </summary>
		/// <param name="key">Protocol Name associated with the header</param>
		/// <returns>Implementation of the Header class</returns>
		public Header removeHeader(String key) 
		{
			Header retValue = (Header)headers[key];
			headers.Remove(key);
			return retValue;
		}

		/// <summary>
		/// Clears all Headers from message
		/// </summary>
		public void removeHeaders() 
		{
			if(headers != null)
				headers.Clear();
		}

		/// <summary>
		/// Gets a header associated with a Protocol layer
		/// </summary>
		/// <param name="key">Protocol Name associated with the header</param>
		/// <returns>Implementation of the Header class</returns>
		public Header getHeader(String key) 
		{
			Header retValue = (Header)headers[key];
			return retValue;
		}
		/*---------------------------------------------------------------------*/

		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the message. Called by the <c>BinaryFormatter</c>.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public void GetObjectData(SerializationInfo info, StreamingContext context)
		{			
			try
			{
				if (dest_addr == null)
					info.AddValue("b_dest_addr", false);
				else
				{
					info.AddValue("b_dest_addr", true);
					info.AddValue("dest_addr", dest_addr);
				}

				if (src_addr == null)
					info.AddValue("b_src_addr", false);
				else
				{
					info.AddValue("b_src_addr", true);
					info.AddValue("src_addr", src_addr);
				}
	
				if (buf != null)
				{
					info.AddValue("bufferLength", buf.Length);
					for (int i = 0 ; i< buf.Length;i++)
					{
						info.AddValue(i.ToString(), buf[i]);
					}
				}
				else
					info.AddValue("bufferLength", (int)0);

				String strHeaders = null;
				foreach (DictionaryEntry h in headers) 
				{
					if(strHeaders==null)
						strHeaders = "";
					strHeaders = strHeaders + Convert.ToString(h.Key) + ":";
				}
				
				if(strHeaders==null)
					strHeaders = "NULL";
				else
					strHeaders = strHeaders.Substring(0,strHeaders.Length-1);


				info.AddValue("strHeaders", strHeaders);
				
				foreach (DictionaryEntry h in headers) 
				{
					info.AddValue(Convert.ToString(h.Key), (Header)h.Value);
				}
			}
			catch(Exception e)
			{
				if(Trace.trace)
					Trace.error("Message.GetObjectData()","Error: "  + e);
			}
			
		}

		/// <summary>
		/// Constructor: used for deserialization
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public Message(SerializationInfo info, StreamingContext ctxt)
		{
			bool b_dest_addr = info.GetBoolean("b_dest_addr");
			if (b_dest_addr)
				dest_addr = (Address)info.GetValue("dest_addr",typeof(object));
			
			bool b_src_addr = info.GetBoolean("b_src_addr");
			if (b_src_addr)
				src_addr = (Address)info.GetValue("src_addr",typeof(object));

			int bufferLength = info.GetInt32("bufferLength");

			if (bufferLength > 0)
			{
				buf = new byte[bufferLength];
				for(int i = 0; i< bufferLength;i++)
				{
					buf[i] = info.GetByte(i.ToString());
				}
			}

			String strHeaders = info.GetString("strHeaders");
			
			headers = new PropertyCollection();

			if (String.Compare(strHeaders,"NULL")!=0)
			{
				String[] headerArray = strHeaders.Split(':');
				for(int j = 0;j<headerArray.Length;j++)
				{
					headers.Add(headerArray[j],info.GetValue(headerArray[j],typeof(object)));
				}
			}
		
		}
		
	}
}

