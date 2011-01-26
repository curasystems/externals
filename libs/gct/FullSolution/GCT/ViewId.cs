using System;

namespace GCT
{
	/// <summary>
	/// Identificator used to tell which View is first.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class ViewId : IComparable
	{
		/// <summary>Address of the issuer of this view</summary>
		Address       coord_addr = null; 
		/// <summary>Lamport time of the view</summary>
		long          id=0;

		/// <remarks>
		/// Creates a ViewID with the coordinator address and a Lamport timestamp of 0.
		/// </remarks>
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="coord_addr">The address of the member that issued this view</param>
		public ViewId(Address coord_addr)
		{
			this.coord_addr=coord_addr;
		}

		/// <remarks>
		/// Creates a ViewID with the coordinator address and the given Lamport timestamp.
		/// </remarks>
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="coord_addr">The address of the member that issued this view</param>
		/// <param name="id">The Lamport timestamp of the view</param>
		public ViewId(Address coord_addr, long id)
		{
			this.coord_addr=coord_addr;
			this.id=id;
		}

		/// <summary>
		/// Returns the lamport time of the view
		/// </summary>
		/// <returns>The lamport time of the view</returns>
		public long getId()
		{
			return id;
		}

		/// <summary>
		/// Returns the address of the member that issued this view
		/// </summary>
		/// <returns>The address of the member that issued this view</returns>
		public Address  getCoordAddress()
		{
			return coord_addr;
		}

		/// <summary>
		/// Returns a string representation of the ViewId
		/// </summary>
		/// <returns>A string representation of the ViewId</returns>
		public override String ToString()
		{
			return "[" + coord_addr + "|" + id + "]";
		}

		/// <summary>
		/// Establishes an order between 2 ViewIds. First compare on id. <em>Compare on coord_addr
		/// only if necessary</em> (i.e. ids are equal) !
		/// </summary>
		/// <param name="other">Second ViewId to compare to</param>
		/// <returns>0 for equality, value less than 0 if smaller, greater than 0 if greater.</returns>
		public int CompareTo(Object other)
		{
			if (other == null) 
				return 1; 
			try
			{
				if(id > ((ViewId)other).id)
					return 1;
				else if (id < ((ViewId)other).id)
					return -1;
				else
					return 0;
			}
			catch(Exception e)
			{
				throw e;
			}
		}

		/// <summary>
		/// Determines if two ViewIds are equal
		/// </summary>
		/// <param name="other_view">Second ViewId to compare to</param>
		/// <returns>True if ViewIds are equal</returns>
		public override bool Equals(Object other_view)
		{
			return (CompareTo(other_view) == 0);
		}

		/// <summary>
		/// Returns the hascode of the ViewId
		/// </summary>
		/// <returns>The hascode of the ViewId</returns>
		public override int GetHashCode()
		{
			return coord_addr.GetHashCode() ^ id.GetHashCode();
		}

		/// <summary>
		/// Returns a copy of the ViewId
		/// </summary>
		/// <returns>A copy of the ViewId</returns>
		public ViewId Copy()
		{
			return new ViewId(coord_addr, id);
		}

	}
}
