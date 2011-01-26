using System;
using System.Runtime.Serialization;

namespace GCT.Blocks
{
	/// <summary>
	/// PullPushAdapter Header
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class PullHeader : Header 
	{
		/// <summary>
		/// Identifies which listener the message should be received by
		/// </summary>
		Object identifier = null;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="identifier">Identifies which listener the message should be received by</param>
		public PullHeader(Object identifier) 
		{
			if (identifier != null && !identifier.GetType().IsSerializable)
				throw new Exception ("Cannot construct 'PullHeader' as identifier is not serialisable.");
			this.identifier=identifier;

		}

		/// <summary>
		/// Returns the identifier object 
		/// </summary>
		/// <returns></returns>
		public Object getIdentifier() 
		{
			return identifier;
		}


		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the information
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public override void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			info.AddValue("identifier", identifier);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public PullHeader(SerializationInfo info, StreamingContext ctxt)
		{
			identifier = (Object)info.GetValue("identifier", typeof(object));
		}
	}
}
