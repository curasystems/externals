using System;
using System.Net;
using System.Runtime.Serialization;

using GCT.Util;

namespace GCT
{
	/// <remarks>
	/// An <c>Address</c> is also used to uniquely identify a member in a group.
	/// Each machine can only have 1 locol port open for receiving unicast messages.
	/// </remarks>
	/// <summary>
	/// Identifes an endpoint. Contains an <c>IPAddress</c> and a port.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	[Serializable]
	public class Address : IComparable
	{
		IPAddress ipAddr;
		int port;

		/// <summary>
		/// Constructor.
		/// </summary>
		/// <param name="addr">String representation of the IP Address</param>
		/// <param name="p">The Port to use</param>
		public Address(String addr, int p) 
		{
			if(addr == "localhost")
				ipAddr = IPAddress.Parse(GroupChannelHelper.GetMachineIp()); 
			else
				ipAddr = IPAddress.Parse(addr);
			port = p;
		}

		/// <summary>
		/// Constructor.
		/// </summary>
		/// <param name="addr"><c>IPAddress</c> object of the Address</param>
		/// <param name="p">The Port to use</param>
		public Address(IPAddress addr, int p) 
		{
			ipAddr = addr;
			port = p;
		}

		/// <summary>
		/// Constructor: will use localhost IP Address
		/// </summary>
		/// <param name="port">Local Port</param>
		public Address(int port) 
		{
			ipAddr = IPAddress.Parse(GroupChannelHelper.GetMachineIp());  // get first NIC found (on multi-homed systems)
			this.port = port;
		}

		/// <summary>
		/// Get or Set the current IP Address
		/// </summary>
		public IPAddress IP
		{
			get{ return ipAddr;}
			set{ ipAddr = value;}
		}

		/// <summary>
		/// Get or Set the current IP Port
		/// </summary>
		public int Port
		{
			get{ return port;}
			set{ port = value;}
		}

		/// <summary>
		/// Checks whether the IP Address is a multicast address.
		/// </summary>
		/// <returns>True if the Address is a multicast address, otherwise false.</returns>
		public bool isMulticastAddress()
		{
			string mcast = null; 
			if (ipAddr != null)
				mcast = ipAddr.ToString();
			int pos = mcast.IndexOf(".",0);
			int range = (int)Convert.ToInt32(mcast.Substring(0,pos));
				
			if(range > 223 && range < 240)
				return true;
			return false;
		}

		/// <summary>
		/// Checks for Equality with another object. Overrides base class method.
		/// </summary>
		/// <param name="obj">The object to check for equality</param>
		/// <returns>True is objects are equal, otherwise false</returns>
		public override bool Equals(Object obj)
		{
			return CompareTo((Address)obj)==0;
		}

		/// <summary>
		/// Overrides the base class method. Used when the Address used as a key
		/// in Hashtables.
		/// </summary>
		/// <returns>The hashcode of the Address object</returns>
		public override int GetHashCode()
		{
			int portH = port.GetHashCode();
			int ipH = ipAddr.GetHashCode();
			int hashcode = portH ^ ipH;
			return hashcode;
		}

		/// <summary>
		/// Compares the current object to the specified object. The object must be an Address.
		/// An Address is said to be larger if it's IP Address is greater, or the IP Address are the same but the port number is greater.
		/// </summary>
		/// <param name="obj">Object to Compare the Address with</param>
		/// <returns>int value representing the comparision.</returns>
		public int CompareTo(object obj)
		{
			Address a = (Address)obj;
			if(ipAddr.Address < a.IP.Address)
				return -1;
			else if (ipAddr.Address > a.IP.Address)
				return 1;
			else if (port < a.Port) // from here they have same IP
				return -1;
			else if (port > a.Port)
				return 1;
			else
				return 0;
		}

		/// <summary>
		/// Returns string representation of the object
		/// </summary>
		/// <returns>String representation of the object</returns>
		public override String ToString()
		{
			return ipAddr.ToString() + ":" + port;
		}

		/// <summary>
		/// Returns a deep-copy of the Address
		/// </summary>
		/// <returns>A deep-copy of the Address</returns>
		public Address Copy()
		{
			return new Address(ipAddr.ToString(),port);
		}
	}
}
