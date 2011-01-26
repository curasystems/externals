using System;

namespace GCT.Util
{
	/// <summary>
	/// Represents a range of messages that need retransmission. Contains the first and last seqeunce numbers.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class Range
		{
		/// <summary>The first message to be retransmitted</summary>
		public long low=-1;  // first msg to be retransmitted
		/// <summary>The last message to be retransmitted</summary>
		public long high=-1; // last msg to be retransmitted

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="low">The first message to be retransmitted</param>
		/// <param name="high">The last message to be retransmitted</param>
		public Range(long low, long high) 
		{
			this.low=low; this.high=high;
		}
    
		/// <summary>
		/// Returns a string representation of the Range.
		/// </summary>
		/// <returns>A string representation of the Range.</returns>
		public override String ToString() 
		{
			return "[" + low + " : " + high + "]";
		}
	}
}