using System;
using System.Diagnostics;
using System.Threading;
using System.Net;
using System.Net.Sockets;
using System.Runtime.Serialization.Formatters.Binary;
using System.Data;
using System.Collections;
using System.IO;

using GCT.Stack;

namespace GCT.Protocols
{
	/// <summary>
	/// Protocol: UDP sends and receives unicast and multicast messages.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class UDP : Protocol
	{
		/// <summary>Receives incoming multicast messages</summary>
		Thread recvMcast;
		/// <summary>Receives incoming unicast messages</summary>
		Thread recvUcast;
		/// <summary>Socket for receiving unicast packets</summary>
		Socket			ucastRecvSocket;
		/// <summary>Socket for sending unicast packets</summary>
		Socket			ucastSendSocket;
		/// <summary>Socket for sending multicast packets</summary>
		Socket			mcastSendSocket;
		/// <summary>Socket for receiving multicast packets</summary>
		Socket			mcastRecvSocket;
		/// <summary>Local address unicast address</summary>
		Address bind_addr = null;
		/// <summary>Header added to all outgoing messages</summary>
		Header udp_hdr;
		/// <summary>Group name/address to connect to</summary>
		String group_addr = null;
		/// <summary>Multicast address</summary>
		Address		mcast_addr = new Address("224.1.2.3",11000);
		/// <summary>Time-To-Live of multicast messages</summary>
		int			ip_ttl = 1;
		/// <summary>Current members in the group</summary>
		ArrayList	members = new ArrayList();
		/// <summary>Size of multicast send buffer</summary>
		int			mcast_send_buf_size = 32000;
		/// <summary>Size of multicast receive buffer</summary>
		int			mcast_recv_buf_size = 64000;
		/// <summary>Size of unicast send buffer</summary>
		int			ucast_send_buf_size = 32000;
		/// <summary>Size of unicast receive buffer</summary>
		int			ucast_recv_buf_size = 64000;

		/// <summary>
		/// Constructor.
		/// </summary>
		public UDP()
		{
			name = "UDP";
			bind_addr = new Address(8085);
		}

		/// <summary>
		/// Sets the properties specified in the configuration string
		/// </summary>
		/// <param name="props">Properties to set</param>
		/// <returns>False if properties were specified that are not know, otherwise true</returns>
		public override bool setProperties(PropertyCollection props)
		{
			if(props.Contains("mcast_addr")) 
			{
				mcast_addr.IP = IPAddress.Parse(Convert.ToString(props["mcast_addr"]));
				props.Remove("mcast_addr");
			}
			if(props.Contains("mcast_port")) 
			{
				mcast_addr.Port = Convert.ToInt32(props["mcast_port"]);
				props.Remove("mcast_port");
			}
			if(props.Contains("bind_addr")) 
			{
				bind_addr.IP = IPAddress.Parse(Convert.ToString(props["bind_addr"]));
				props.Remove("bind_addr");
			}
			if(props.Contains("bind_port")) 
			{
				bind_addr.Port = Convert.ToInt32(props["mcast_port"]);
				props.Remove("mcast_port");
			}
			if(props.Contains("ip_ttl")) 
			{
				ip_ttl = Convert.ToInt32(props["ip_ttl"]);
				props.Remove("ip_ttl");
			}
			if(props.Contains("bind_port")) 
			{
				bind_addr.Port = Convert.ToInt32(props["bind_port"]);
				props.Remove("bind_port");
			}
			if(props.Contains("mcast_send_buf_size")) 
			{
				mcast_send_buf_size = Convert.ToInt32(props["mcast_send_buf_size"]);
				props.Remove("mcast_send_buf_size");
			}
			if(props.Contains("mcast_recv_buf_size")) 
			{
				mcast_recv_buf_size = Convert.ToInt32(props["mcast_recv_buf_size"]);
				props.Remove("mcast_recv_buf_size");
			}
			if(props.Contains("ucast_send_buf_size")) 
			{
				ucast_send_buf_size = Convert.ToInt32(props["ucast_send_buf_size"]);
				props.Remove("ucast_send_buf_size");
			}
			if(props.Contains("ucast_recv_buf_size")) 
			{
				ucast_recv_buf_size = Convert.ToInt32(props["ucast_recv_buf_size"]);
				props.Remove("ucast_recv_buf_size");
			}

			if (props.Count > 0)
				return false;

			return true;
		}

		
		/// <summary>
		/// Processes <c>Events</c> travelling up the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void up(Event evt)
		{
			passUp(evt); // Just incase the stack starts here and sends a message up
		}

		/// <summary>
		/// Processes <c>Events</c> traveling down the stack
		/// </summary>
		/// <param name="evt">The Event to be processed</param>
		public override void down(Event evt)
		{
			Message      msg;
			Object       dest_addr;
	
			if(evt.Type != Event.MSG) 
			{  // unless it is a message handle it and respond
				handleDownEvent(evt);
				return;
			}

			msg = (Message)evt.Arg;
			if(!msg.Headers.Contains(name))
				msg.putHeader(name, udp_hdr);
			else
			{
				String strHeaders = "";
				foreach(DictionaryEntry d in msg.Headers)
				{
					strHeaders += (String)d.Key + ":";
				}
				if(Trace.trace)
					Trace.info("UDP.down()","Message already got UDP header: " + strHeaders);
			}
			dest_addr = msg.Destination;

			if(dest_addr == null) 
			{ 
				if(mcast_addr == null) 
				{
					if(Trace.trace)
						Trace.error("UDP.down()", "dest address of message is null, and " +
						"sending to default address fails as mcast_addr is null, too !" +
						" Discarding message " + evt.ToString());
					return;
				}
				//if we want to use IP multicast, then set the destination of the message
				msg.Destination = mcast_addr;
			}	
			try 
			{
				sendUdpMessage(msg);
			}
			catch(Exception e) 
			{
				if(Trace.trace)
					Trace.error("UDP.down()", "exception=" + e + ", msg=" + msg + ", mcast_addr=" + mcast_addr);
			}
		}

		/// <summary>
		/// Creates sockets, informs protocols of the local address 
		/// and starts listening for incoming messages.
		/// </summary>
		public override void start()
		{
			createSockets();
			passUp(new Event(Event.SET_LOCAL_ADDRESS, bind_addr));
			startThreads();
		}

		/// <summary>
		/// Handles any event that is not a Message
		/// </summary>
		/// <param name="evt">Event</param>
		private void handleDownEvent(Event evt)
		{
			switch(evt.Type) 
			{	    
				case Event.GET_LOCAL_ADDRESS:   // return local address -> Event(SET_LOCAL_ADDRESS, local)
					passUp(new Event(Event.SET_LOCAL_ADDRESS, bind_addr)); //
					break;
				case Event.CONNECT:
					group_addr = (String)evt.Arg;
					udp_hdr = new UDPHeader(group_addr);
					passUp(new Event(Event.CONNECT_OK,mcast_addr));
					break;
				case Event.DISCONNECT:
					passUp(new Event(Event.DISCONNECT_OK));
					break;
				case Event.TMP_VIEW:
				case Event.VIEW_CHANGE:
					lock(members) 
					{
						members.Clear();
						ArrayList tmpvec=((View)evt.Arg).getMembers();
						for(int i=0; i < tmpvec.Count; i++)
							members.Add(tmpvec[i]);
					}
					break;
			}
        
		}

		/// <summary>
		/// Closes all sockets and stops the Threads
		/// </summary>
		public override void stop()
		{
			closeSockets(); 
			stopThreads();  
		}

		/// <remarks>
		/// If the specified (or default) port is in use then try the next 10
		/// ports for access.
		/// </remarks>
		/// <summary>
		/// Creates four sockets; listening and sending on both multicast and unicast addresses
		/// </summary>
		private void createSockets()
		{	
			// Create socket for receiving Unicast UDP
			int numTries = 0;
			while(numTries < 10)
			{
				try
				{
					IPEndPoint localEP = new IPEndPoint(bind_addr.IP,bind_addr.Port);
					ucastRecvSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
					ucastRecvSocket.Bind(localEP);
					break;
				}
				catch(SocketException e)
				{
					numTries++;
					bind_addr.Port++;
				}
			}
			if(numTries==10)
			{
				throw new Exception("Could not find port to use.");
			}
			
			// Create socket for sending Unicast UDP
			ucastSendSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);


			// Create socket for sending Multicast UDP
			IPEndPoint ipep1 = new IPEndPoint(mcast_addr.IP,mcast_addr.Port);
			mcastSendSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
			mcastSendSocket.SetSocketOption(SocketOptionLevel.IP, SocketOptionName.AddMembership, new MulticastOption(mcast_addr.IP));
			mcastSendSocket.SetSocketOption(SocketOptionLevel.IP, SocketOptionName.MulticastTimeToLive, ip_ttl);
			mcastSendSocket.Connect(ipep1);
			
			// Create socket for receiving Multicast UDP
			IPEndPoint ipep2 = new IPEndPoint(IPAddress.Any,mcast_addr.Port);
			mcastRecvSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
			mcastRecvSocket.Bind(ipep2);
			mcastRecvSocket.SetSocketOption(SocketOptionLevel.IP, SocketOptionName.AddMembership, new MulticastOption(mcast_addr.IP,IPAddress.Any));
			
		}
		
		/// <summary>
		/// Closes the four sockets.
		/// </summary>
		private void closeSockets()
		{	
			ucastRecvSocket.Close();
			ucastRecvSocket = null;

			ucastSendSocket.Close();
			ucastSendSocket = null;

			mcastSendSocket.Close();
			mcastSendSocket = null;

			mcastRecvSocket.Close();
			mcastRecvSocket = null;
		}

		/// <summary>
		/// Starts listening for incoming packets
		/// </summary>
		private void startThreads()
		{
			recvMcast = new Thread(new ThreadStart(receiveMCast));
			recvMcast.Name = "UDP Mcast Listener";

			recvUcast = new Thread(new ThreadStart(receiveUCast));
			recvUcast.Name = "UDP Ucast Listener";

			recvMcast.Start();
			recvUcast.Start();
		}

		/// <summary>
		/// Stop listening for incoming packets
		/// </summary>
		private void stopThreads()
		{
			while (recvMcast.IsAlive)
			{
				recvMcast.Abort();
			}
			while (recvUcast.IsAlive)
			{
				recvUcast.Abort();
			}
		}

		/// <summary>
		/// Running in a seperate thread, will recieve incoming mulitcasts
		/// </summary>
		private void receiveMCast()
		{
			byte[] packet = new byte[mcast_recv_buf_size];
			
			while (mcastRecvSocket != null)
			{
				try
				{
					mcastRecvSocket.Receive(packet);
					handleIncomingUdpPacket(packet);
				}
				catch(SocketException e)
				{
					if(Trace.trace)
						Trace.error(name + ".receiveMCast()", "Socket Exception occured, socket is closed.\n");
					break;
				}
			}
		}
		
		/// <summary>
		/// Running in a seperate thread, will recieve incoming unicasts
		/// </summary>
		private void receiveUCast()
		{
			byte[] packet = new byte[ucast_recv_buf_size];

			while (ucastRecvSocket != null)
			{
				try
				{
					ucastRecvSocket.Receive(packet);
					handleIncomingUdpPacket(packet);
				}
				catch(SocketException e)
				{
					if(Trace.trace)
						Trace.error(name + ".receiveUCast()", "Socket Exception occured, socket is closed.\n");
					break;
				}
			}
		}

		/// <summary>
		/// Process <i>any</i> incoming message once it is received.
		/// </summary>
		/// <param name="packet">Message Payload</param>
		private void handleIncomingUdpPacket(byte[] packet)
		{
			Message              msg=null;
			UDPHeader            hdr=null;
			Event                evt;
				
			try 
			{
				if(packet.Length>0)
				{
					BinaryFormatter bf =new BinaryFormatter();
					msg =(Message)bf.Deserialize(new MemoryStream(packet));

					evt=new Event(Event.MSG, msg);
					String strHeaders = "";
					hdr=(UDPHeader)msg.removeHeader(name);
				
					foreach(DictionaryEntry d in msg.Headers)
					{
						strHeaders += (String)d.Key + ":";
					}
					strHeaders = strHeaders.Substring(0,(strHeaders.Length-1));
					if(Trace.trace)
						Trace.info("UDP.handleIncomingUdpPacket()","Headers = " + strHeaders);
				}
				else
				{
					if(Trace.trace)
						Trace.error("UDP.handleIncomingUdpPacket()","Buffer received has length = 0");
					return;
				}
			}
			catch(Exception e) 
			{
				if(Trace.trace)
					Trace.error("UDP.handleIncomingUdpPacket()", "exception=" + e);
				return;
			}

			/*Discard all messages destined for a channel with a different name*/
			String ch_name=null;

			if(hdr.GroupAddress != null)
				ch_name=hdr.GroupAddress;

			if(group_addr == null) 
			{
				if(Trace.trace)
					Trace.warn("UDP.handleIncomingUdpPacket()", "my group_addr is null, discarding message " + msg);
				return;
			}

			if(ch_name != null && String.Compare(group_addr,ch_name)!=0) 
			{
				if(Trace.trace)
					Trace.warn("UDP.handleIncomingUdpPacket()", "discarded message from different group (" + ch_name + "). Sender was " + msg.Source);
				return;
			}
			passUp(evt);
		}

		/// <summary>
		/// Sends a <c>Message</c> using UDP
		/// </summary>
		/// <param name="msg">Message to be sent</param>
		private void sendUdpMessage(Message msg)
		{
			Address				dest;
			byte[]				buffer;
			Stream				s;
			BinaryFormatter		bf;
			
			
			dest=(Address)msg.Destination; 
			setSourceAddress(msg);

			if(Trace.trace)
				Trace.info("UDP.SendUdpMessage()", "Sending message to [" + dest.IP.ToString() + ":" + dest.Port + ", Multicast = " + dest.isMulticastAddress() + "]" );


			s = new MemoryStream();
			bf = new BinaryFormatter();

			bf.Serialize(s,msg);
			buffer = new byte[s.Length];
			s.Position = 0;
			int bytesRead = s.Read(buffer, 0, (int)s.Length);
			s.Close();
			
			if(dest.isMulticastAddress()) 
			{ // multicast message
				try 
				{
					mcastSendSocket.Send(buffer,buffer.Length,SocketFlags.None);
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("UDP.sendUdpMessage()", "exception sending mcast message: " + e);
					throw e;
				}
			}
			else 
			{ 
				try 
				{
					IPEndPoint remoteEP = new IPEndPoint(dest.IP, dest.Port);
					ucastSendSocket.SendTo(buffer,buffer.Length,SocketFlags.None, remoteEP);
				}
				catch(Exception e) 
				{
					if(Trace.trace)
						Trace.error("UDP.sendUdpMessage()", "exception sending ucast message: " + e);
				}
			}
		}

		/// <summary>
		/// Sets the source address on a <c>Message</c> to the local Address
		/// </summary>
		/// <param name="msg">Message to be set</param>
		private void setSourceAddress(Message msg)
		{
			if(msg.Source == null)
				msg.Source = bind_addr;
		}
	}
	
}

