using System;
using System.Runtime.Remoting.Messaging;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters;
using System.Runtime.Serialization.Formatters.Binary;
using System.IO;

namespace GCT.Stack
{
	/// <summary>
	///  ProtocolSinkStack Header
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class StackHeader : Header
	{
		/// <summary>Header Type: Remoting request</summary>
		public const int REMOTE_REQ           = 1;
		/// <summary>Header Type: Remoting response</summary>
		public const int REMOTE_RSP           = 2;
		
		/// <summary>Header Type</summary>
		private int				type = 0;
		/// <summary>Request or Response</summary>
		private IMessage		msg = null;
		/// <summary>Sender of request</summary>
		private Address			source = null;
		/// <summary>Needed for deserialisation</summary>
		private string			objectUri = null;
		/// <summary>Response message stream</summary>
		private MemoryStream	stream = null;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Header type</param>
		public StackHeader(int type) 
		{
			this.type=type;
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="type">Header type</param>
		/// <param name="msg">Remoting message</param>
		/// <param name="objectUri">Remote Object Uri</param>
		public StackHeader(int type, IMessage msg, string objectUri) : this(type)
		{
			this.msg=msg;
			this.objectUri = objectUri;
		}

		/// <summary>
		/// Gets and sets the Remoting message
		/// </summary>
		public IMessage Message
		{
			get{return msg;}
			set{msg = value;}
		}
		
		/// <summary>
		/// Gets the header type
		/// </summary>
		public int Type
		{
			get{return type;}
		}

		/// <summary>
		/// Gets the sender of the message
		/// </summary>
		public Address Source
		{
			get{return source;}
		}

		/// <summary>
		/// Converts a type into a string represention
		/// </summary>
		/// <param name="t">The header type required</param>
		/// <returns>A type into a string represention</returns>
		public static String type2String(int t) 
		{
			switch(t) 
			{
				case REMOTE_REQ:			return "REMOTE_REQ";
				case REMOTE_RSP:			return "REMOTE_RSP";
				default:					return "UNDEFINED";
			}
		}
		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the information
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public override void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			BinaryFormatter bf = new BinaryFormatter();
			RemotingSurrogateSelector rss = new RemotingSurrogateSelector();
			bf.SurrogateSelector = rss;
			bf.Context = new StreamingContext(StreamingContextStates.Other);
			bf.AssemblyFormat = FormatterAssemblyStyle.Full;

			MemoryStream ms = new MemoryStream();
			bf.Serialize(ms,msg,null);

			info.AddValue("type", type);
			info.AddValue("stream", ms);
			info.AddValue("source", source);
			info.AddValue("objectUri", objectUri);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public StackHeader(SerializationInfo info, StreamingContext ctxt)
		{
			objectUri = info.GetString("objectUri");
			source = (Address)info.GetValue("source", typeof(object));

			type = info.GetInt32("type");
			MemoryStream ms = (MemoryStream)info.GetValue("stream", typeof(object));

			if(type == REMOTE_RSP) 
			{
				stream = ms;
			}
			else if(type == REMOTE_REQ) 
			{
				ms.Position = 0;
				BinaryFormatter fmt = new BinaryFormatter();
				fmt.AssemblyFormat = FormatterAssemblyStyle.Full;
				UriHeaderHandler uriHH = new UriHeaderHandler(objectUri);
				IMessage reqMsg = (IMessage)fmt.Deserialize(ms, new HeaderHandler(uriHH.HeaderHandler));
				msg = reqMsg;
			}
 
		}

		/// <remarks>
		/// This is performed seperately as the initial message request is needed
		/// to deserialize the response properly.
		/// </remarks>
		/// <summary>
		/// Deserialises the request message.
		/// </summary>
		/// <param name="reqMsg"></param>
		public void deserializeResponse(IMethodCallMessage reqMsg)
		{
			if(stream != null)
			{
				BinaryFormatter fmt = new BinaryFormatter();
				fmt.AssemblyFormat = FormatterAssemblyStyle.Full;
				stream.Position = 0;
				IMessage replyMsg = (IMessage)fmt.DeserializeMethodResponse(stream, null, reqMsg);
				msg = replyMsg;
				stream = null;
			}
		}

		/// <summary>
		/// Used to pass uri into binary serializer, so that the message
		/// gets the object uri.
		/// </summary>
		private class UriHeaderHandler
		{
			String _uri = null;

			internal UriHeaderHandler(String uri)
			{
				_uri = uri;
			}
        
			public Object HeaderHandler(System.Runtime.Remoting.Messaging.Header[] Headers)
			{
				return _uri;
			}
		}
	}
}
