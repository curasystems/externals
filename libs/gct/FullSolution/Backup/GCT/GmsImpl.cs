using System;
using System.Collections;

namespace GCT.Protocols
{
	/// <summary>
	/// GMS implementation.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public abstract class GmsImpl 
	{
		/// <summary>
		/// GMS Protocol, used to call non-implementation specific methods.
		/// </summary>
		protected GMS          gms=null;
		/// <summary>
		/// Name of GMS implementation
		/// </summary>
		protected String       name=null;

		/// <summary>
		/// Join a group.
		/// </summary>
		/// <param name="mbr">Local Address</param>
		public abstract void		join(Address mbr);
		/// <summary>
		/// Leave current group.
		/// </summary>
		/// <param name="mbr">Local Address</param>
		public abstract void		leave(Address mbr);

		/// <summary>
		/// Process response to join request
		/// </summary>
		/// <param name="join_rsp">Response to Join request</param>
		public abstract void		handleJoinResponse(JoinRsp join_rsp);

		/// <summary>
		/// Process leave request
		/// </summary>
		public abstract void		handleLeaveResponse();

		/// <summary>
		/// Supected member.
		/// </summary>
		/// <param name="mbr">Suspected member</param>
		public abstract void		suspect(Address mbr);

		/// <summary>
		/// Unsuspect member 
		/// </summary>
		/// <param name="mbr">Previously suspected member</param>
		public abstract void		unsuspect(Address mbr);

		/// <summary>
		/// Process join request
		/// </summary>
		/// <param name="mbr">Joining member</param>
		/// <returns>Response to join request</returns>
		public abstract JoinRsp		handleJoin(Address mbr);

		/// <summary>
		/// Process leave request
		/// </summary>
		/// <param name="mbr">Leaving Member</param>
		/// <param name="suspected">True if member has been forced to leave</param>
		public abstract void		handleLeave(Address mbr, bool suspected);

		/// <summary>
		/// Process change to current view
		/// </summary>
		/// <param name="new_view">New View</param>
		/// <param name="digest">Digest associated with the view</param>
		public abstract void		handleViewChange(View new_view, Digest digest);

		/// <summary>
		/// Process suspected member.
		/// </summary>
		/// <param name="mbr"></param>
		public abstract void		handleSuspect(Address mbr);

		/// <summary>
		/// Allows <c>up()</c> <c>Events</c> to be captured by the GMS implementation
		/// </summary>
		/// <param name="evt">Up Event</param>
		/// <returns>True is processing completed correctly</returns>
		public virtual bool			handleUpEvent(Event evt) {return true;}

		/// <summary>
		/// Allows <c>down()</c> <c>Events</c> to be captured by the GMS implementation
		/// </summary>
		/// <param name="evt">Down Event</param>
		/// <returns>True is processing completed correctly</returns>
		public virtual bool			handleDownEvent(Event evt) {return true;}

		/// <summary>
		/// Initilisation for GMS implementation
		/// </summary>
		public virtual void			init() {;}
		/// <summary>
		/// Start for GMS implementation
		/// </summary>
		public virtual void			start() {;}
		/// <summary>
		/// Stop for GMS implementation
		/// </summary>
		public virtual void			stop() {;}

		/// <summary>
		/// Returns GMS implementation name
		/// </summary>
		/// <returns></returns>
		public String getName()
		{
			return name;
		}

		/// <summary>
		/// Used by implementation to signify a method has been called on an incorrect implementation
		/// </summary>
		/// <param name="method_name"></param>
		protected void wrongMethod(String method_name) 
		{
			if(Trace.trace)
				Trace.error("GmsImpl.wrongMethod()", method_name + 
				"() should not be invoked on an instance of " + name);
		}

	}
}
