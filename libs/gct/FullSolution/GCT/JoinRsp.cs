using System;

namespace GCT.Protocols
{
	/// <summary>
	/// Response to join request from GMS.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class JoinRsp
	{
		/// <summary>Current view of the group</summary>
		View    view=null;
		/// <summary>Current digest of the group</summary>
		Digest  digest=null;

		/// <summary>
		/// Constructor.
		/// </summary>
		/// <param name="view">Current view of the group</param>
		/// <param name="digest">Current digest of the group</param>
		public JoinRsp(View view, Digest digest) 
		{
			this.view=view;
			this.digest=digest;
		}

		/// <summary>Gets the returned View</summary>
		public View   getView()   {return view;}
		/// <summary>Gets the returned Digest</summary>
		public Digest getDigest() {return digest;}

		/// <summary>
		/// Returns a string representation of the current object
		/// </summary>
		/// <returns>A string representation of the current object</returns>
		public String toString() 
		{
			String sb= "";
			sb = "view: ";
			if(view == null)
				sb = sb + "<null>";
			else
				sb = sb + view;
			sb = sb + ", digest: ";
			if(digest == null)
				sb = sb + "<null>";
			else
				sb = sb + digest;
			return sb;
		}

	}
}
