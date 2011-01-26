using System;
using System.Collections;

namespace GCT
{
	/// <remarks>
	/// Coupled with an <c>ArrayList</c>, this class extends its facilites  and adds
	/// extra constraints
	/// </remarks>
	/// <summary>
	/// Used by the GMS to store the current members in the group
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class Membership
	{
		/// <summary>
		/// List of current members
		/// </summary>
		private ArrayList members = null;

		/// <summary>
		/// Constructor: Initialises with no initial members
		/// </summary>
		public Membership()
		{
			members=new ArrayList();
			members = ArrayList.Synchronized(members);
		}

		/// <summary>
		/// Constructor: Initialises with the specified initial members 
		/// </summary>
		/// <param name="initial_members">Initial members of the membership</param>
		public Membership(ArrayList initial_members)
		{
			members=new ArrayList();
			if(initial_members != null)
				members = (ArrayList)initial_members.Clone();
			members = ArrayList.Synchronized(members);
		}

		/// <summary>
		/// Returns a clone of the members list
		/// </summary>
		/// <returns>A clone of the members list</returns>
		public ArrayList getMembers()
		{
			return (ArrayList)members.Clone();
		}

		/// <summary>
		/// Sets the members to the specified list
		/// </summary>
		/// <param name="membrs">The current members</param>
		public void setMembers(ArrayList membrs)
		{
			members = membrs;
		}

		/// <remarks>
		/// If the member already exists then the member will
		/// not be added to the membership
		/// </remarks>
		/// <summary>
		/// Adds a new member to this membership.
		/// </summary>
		/// <param name="new_member"></param>
		public void add(Address new_member)
		{
			if(new_member != null && !members.Contains(new_member))
			{
				members.Add(new_member);
			}
		}

		/// <summary>
		/// Adds a number of members to this membership
		/// </summary>
		/// <param name="v"></param>
		public void add(ArrayList v)
		{
			if(v != null)
			{
				for(int i=0; i < v.Count; i++)
				{
					add((Address)v[i]);
				}
			}
		}

		/// <summary>
		/// Removes the specified member
		/// </summary>
		/// <param name="old_member">Member that has left the group</param>
		public void remove(Address old_member)
		{
			if(old_member != null)
			{
				members.Remove(old_member);
			}
		}

		/// <summary>
		/// Removes a number of members from the membership
		/// </summary>
		/// <param name="v"></param>
		public void remove(ArrayList v)
		{
			if(v != null)
			{
				for(int i=0; i < v.Count; i++)
				{
					remove((Address)v[i]);
				}
			}
		}

		/// <summary>
		/// Removes all members
		/// </summary>
		public void clear()
		{
			members.Clear();
		}

		/// <summary>
		/// Sets the membership to the members present in the list
		/// </summary>
		/// <param name="v">New list of members</param>
		public void set(ArrayList v)
		{
			clear();
			if(v != null)
			{
				add(v);
			}
		}

		/// <summary>
		/// Sets the membership to the specified membership
		/// </summary>
		/// <param name="m">New membership</param>
		public void set(Membership m)
		{
			clear();
			if(m != null)
			{
				add( m.getMembers() );
			}
		}

		/// <summary>
		/// Returns true if the provided member belongs to this membership
		/// </summary>
		/// <param name="member">Member to check</param>
		/// <returns>True if the provided member belongs to this membership, otherwise false</returns>
		public bool contains(Address member)
		{
			if(member == null)
				return false;
			return members.Contains(member);
		}

		/// <summary>
		/// Returns a copy of this membership.
		/// </summary>
		/// <returns>A copy of this membership</returns>
		public Membership copy()
		{
			return (Membership)this.Clone();
		}

		/// <summary>
		/// Clones the membership
		/// </summary>
		/// <returns>A clone of the membership</returns>
		public Object Clone()
		{
			Membership m;
			m = new Membership();
			m.setMembers((ArrayList)members.Clone());
			return(m);
		}

		/// <summary>
		/// The number of members in the membership
		/// </summary>
		/// <returns>Number of members in the membership</returns>
		public int size()
		{
			return members.Count;
		}

		/// <summary>
		/// Gets a member at a specified index
		/// </summary>
		/// <param name="index">Index of member</param>
		/// <returns>Address of member</returns>
		public Address elementAt(int index)
		{
			if(index<members.Count)
				return (Address)members[index];
			else
				return null;
		}

		/// <summary>
		/// String representation of the Membership object
		/// </summary>
		/// <returns>String representation of the Membership object</returns>
		public String toString()
		{
			return members.ToString();
		}

	}
}
