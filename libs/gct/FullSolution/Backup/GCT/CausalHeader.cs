using System;
using System.Runtime.Serialization;

namespace GCT.Protocols
{
	/// <summary>
	/// Causal Ordering Header.
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  17/06/2003</p>
	/// </summary>
	[Serializable]
	public class CausalHeader : Header 
	{
		/// <summary>Representation of current Vector Time Stamp</summary>
		private TransportableVectorTimeStamp vStamp;

		/// <summary>Constructor</summary>
		/// <param name="vStamp">Vector time stamp</param>
		public CausalHeader(TransportableVectorTimeStamp vStamp)
		{
			this.vStamp = vStamp;
		}

		/// <summary>
		/// Gets the vector time stamp
		/// </summary>
		public TransportableVectorTimeStamp VectorTime
		{
			get{return vStamp;}
		}

		/// <summary>
		/// Returns a string representation of the Header object
		/// </summary>
		/// <returns>A string representation of the Header object</returns>
		public override String ToString() 
		{
			string retValue = "[";
			foreach(int i in vStamp.MessagesSent)
			{
				retValue += i + ",";
			}
			retValue = retValue.Substring(0,retValue.Length-1);
			retValue += "]";
			return retValue;
		}

		// -------------- ISerializable Interface ------------------------------
		/// <summary>
		/// Serialises the information
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="context">Standard <c>StreamingContext</c> object</param>
		public override void GetObjectData(SerializationInfo info, StreamingContext context)
		{
			info.AddValue("vStamp", vStamp);
		}

		/// <summary>
		/// Constructor: Deserialises the information and recreates the instance.
		/// </summary>
		/// <param name="info">Standard <c>SerializationInfo</c> object</param>
		/// <param name="ctxt">Standard <c>StreamingContext</c> object</param>
		public CausalHeader(SerializationInfo info, StreamingContext ctxt)
		{
			vStamp = (TransportableVectorTimeStamp)info.GetValue("vStamp",typeof(object));
		}
	}
}
