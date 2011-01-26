using System;
using System.Collections;

namespace GCT
{
	/// <summary>
	/// Represents the current 'View' of the members of the group
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class View
	{
		/// <remarks>
		/// The view id contains the creator address and a lamport time.
		/// the lamport time is the highest timestamp seen or sent from a view.
		/// if a view change comes in with a lower lamport time, the event is discarded.
		/// </remarks>
		/// <summary>
		/// A view is uniquely identified by its ViewID
		/// </summary>
		protected ViewId      vid = null;

		/// <remarks>
		/// This list is always ordered, with the coordinator being the first member.
		/// the second member will be the new coordinator if the current one disappears
		/// or leaves the group.
		/// </remarks>
		/// <summary>
		/// A list containing all the members of the view
		/// </summary>
		protected ArrayList  members = null;

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="vid">The view id of this view (can not be null)</param>
		/// <param name="members">Contains a list of all the members in the view, can be empty but not null.</param>
		public View(ViewId vid, ArrayList members)
		{
			this.vid = vid;
			this.members = members;
		}

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="creator">The creator of this view</param>
		/// <param name="id">The lamport timestamp of this view</param>
		/// <param name="members">Contains a list of all the members in the view, can be empty but not null.</param>
		public View(Address creator, long id, ArrayList members) : this( new ViewId(creator, id), members)
		{
		}

		/// <summary>
		/// Returns the view ID of this view
		/// </summary>
		/// <returns>The view ID of this view</returns>
		public ViewId  getVid()
		{
			return vid;
		}

		/// <summary>
		/// Returns the creator of this view
		/// </summary>
		/// <returns>The creator of this view</returns>
		public Address getCreator()
		{
			return vid != null ? vid.getCoordAddress() : null;
		}

		/// <remarks>
		/// Do NOT change this list, hence your will invalidate the view
		/// Make a copy if you have to modify it.
		/// </remarks>
		/// <summary>
		/// Returns a reference to the List of members (ordered)
		/// </summary>
		/// <returns></returns>
		public ArrayList getMembers()
		{
			return members;
		}

		/// <summary>
		/// Returns true, if this view contains a certain member
		/// </summary>
		/// <param name="mbr">The address of the member</param>
		/// <returns>True, if this view contains a certain member</returns>
		public bool containsMember( Address mbr )
		{
			if ( mbr == null || members == null )
			{
				return false;
			}
			return members.Contains(mbr);
		}

		/// <summary>
		/// Returns the number of members in this view
		/// </summary>
		/// <returns>The number of members in this view</returns>
		public int size()
		{
			if (members == null)
				return 0;
			else
				return members.Count;
		}

		/// <summary>
		/// Returns a string representation of the View
		/// </summary>
		/// <returns>A string representation of the View</returns>
		public override String ToString()
		{
			String retValue = "";
			retValue =  vid.ToString() + " [";

			for(int i = 0; i< members.Count; i++)
			{
				retValue += ((Address)members[i]).ToString() + " - ";
			}
			retValue = retValue.Substring(0, retValue.Length-3);
			retValue += "]";
			return retValue;
		}

		/// <summary>
		/// Copys the View
		/// </summary>
		/// <returns>A copy of the View</returns>
		public View copy()
		{
			return new View(vid.Copy(), (ArrayList)members.Clone());
		}

	}
}
