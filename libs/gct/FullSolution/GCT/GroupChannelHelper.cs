using System;
using System.Net;
using System.Runtime.Remoting.Channels;

namespace GCT.Util
{
	/// <summary>
	/// Helper class for Group Channel
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class GroupChannelHelper
	{
		/// <summary>Host name of local computer</summary>
		private static String s_hostName = null; 
		/// <summary>Machine name of local computer</summary>
		private static String s_MachineName = null;
		/// <summary>IP address of local computer</summary>
		private static String s_MachineIp = null;

		/// <summary>
		/// URI Prefix for this channel
		/// </summary>
		public const String _protocolName = "C#Groups://";

		/// <summary>
		/// Converts a url into the channel URI and the object URI
		/// </summary>
		/// <param name="url">URL to breakdown</param>
		/// <param name="objectURI">Returning value of obhect URI</param>
		/// <returns>Channel URI</returns>
		public static String ParseURL(String url, out String objectURI)
		{      
			// Set the out parameters
			objectURI = null;

			int separator;

			// Find the starting point of C#Groups://
			if (url.ToLower().StartsWith(_protocolName.ToLower()))
			{
				separator = _protocolName.Length;
			}
			else
			{
				return null;
			}

			// find next slash (after end of scheme)
			separator = url.IndexOf('/', separator);
			if (-1 == separator)
			{
				return url; // means that the url is just "tcp://foo:90" or something like that
			}

			// Extract the channel URI which is the prefix
			String channelURI = url.Substring(0, separator);

			// Extract the object URI which is the suffix
			objectURI = url.Substring(separator); // leave the slash

			return channelURI;
		}

		/// <summary>
		/// Gets the local machine IP Address in string form
		/// </summary>
		/// <returns>The local machine IP Address in string form</returns>
		public static String GetMachineIp()
		{      
			if (s_MachineIp == null)
			{
				String hostName = GetMachineName();

				// NOTE: We intentionally allow exceptions from these api's
				//  propagate out to the caller.
				IPHostEntry ipEntries = Dns.GetHostByName(hostName);
				if ((ipEntries != null) && (ipEntries.AddressList.Length > 0))
				{
					s_MachineIp = ipEntries.AddressList[0].ToString();
				}               
    
				if (s_MachineIp == null)
				{
					throw new ArgumentNullException("ip");
				}
			}
            
			return s_MachineIp;    
		}
		
		/// <summary>
		/// Gets the local host name of the computer
		/// </summary>
		/// <returns>The local host name of the computer</returns>
		public static String GetHostName()
		{
			if (s_hostName == null)
			{
				s_hostName = Dns.GetHostName();

				if (s_hostName == null)
				{
					throw new ArgumentNullException("hostName");
				}
			}

			return s_hostName;
		} // GetHostName

		/// <summary>
		/// Gets the local machine name of the computer
		/// </summary>
		/// <returns>The local machine name of the computer</returns>
		public static String GetMachineName()
		{
			if (s_MachineName == null)
			{     
				String machineName = GetHostName();
				if (machineName != null)
				{
					IPHostEntry host = Dns.GetHostByName(machineName);
					if (host != null)
						s_MachineName = host.HostName;
				} 

				if (s_MachineName == null)
				{
					throw new ArgumentNullException("machine");
				}
			}
            
			return s_MachineName;      
		} // GetMachineName
	}
}
