using System;
using System.Collections;
using System.Runtime.Remoting.Messaging;

namespace GCT.Remoting
{
	/// <summary>
	/// Allows a selection from the Remoting responses.
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public interface RemotingRespChooser
	{
		/// <summary>
		/// Called from the ProtocolSinkStack once a collection of Remoting response are recevied.
		/// </summary>
		IMessage choice(ArrayList responses);
	}
}
