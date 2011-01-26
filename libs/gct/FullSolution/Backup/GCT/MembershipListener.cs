using System;

namespace GCT
{
	/// <summary>
	/// Allows reception of Membership changes
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public interface MembershipListener 
	{
		/// <summary>
		/// Notify the target object of a change of membership.
		/// </summary>
		/// <param name="new_view">New view of group</param>
		void viewAccepted(View new_view);

		/// <summary>
		/// Notify the target object of a suspected member
		/// </summary>
		/// <param name="suspected_mbr"></param>
		void suspect(Address suspected_mbr);
	}
}
