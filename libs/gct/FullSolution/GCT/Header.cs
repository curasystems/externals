using System;
using System.Runtime.Serialization;

namespace GCT
{
	/// <summary>
	/// Used to append messages with stack information
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public abstract class Header : ISerializable
	{
		/// <summary>
		/// Name of associated clss
		/// </summary>
		public string className = "<ERROR NOT SET>";

		/// <summary>
		/// Default String representation of the Header
		/// </summary>
		/// <returns>String representation of the Header</returns>
		public override String ToString() 
		{
			return "[" + className + " Header]";
		}
		
		/// <remarks>
		/// An abstract class cannot be marked as [Serializable] therefore this
		/// method is needed to tell sub-classes to be serializable
		/// </remarks>
		/// <summary>
		/// Used to enforce serialization property of sub-class.
		/// </summary>
		/// <param name="info"></param>
		/// <param name="context"></param>
		public abstract void GetObjectData(SerializationInfo info, StreamingContext context);
	}
}
