using System;
using GCT;

namespace Testing
{
	public class Efficency: ChannelListener
	{
		private Channel chan;
		private String props = "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=32):" +
			"PING(timeout=3000;num_initial_members=6):" +
			"FD(timeout=3000):" +
			"VERIFY_SUSPECT(timeout=1500):" +
			"DISCARD(up=0.1;down=0.1):" +
			"STABLE(desired_avg_gossip=10000):" +
			"NAKACK(gc_lag=10;retransmit_timeout=3000):" +
			"UNICAST(timeout=2000):" +
			"GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";
		
		public Efficency()
		{
			chan = new GroupChannel(props);
			chan.setChannelListener(this);
		}

		// -=-=-=-=-=-=-=- ChannelListener Interface -=-=-=-=-=-=--=-
		public void channelConnected(Channel channel)
		{
			Console.WriteLine("-=-=-=-=-" + "Channel is Connected!" + "-=-=-=-=-" + "\n");
		}

		public void channelDisconnected(Channel channel)
		{
			Console.WriteLine("-=-=-=-=-" + "Channel is Disconnected!" + "-=-=-=-=-" + "\n");
		}

		public void channelClosed(Channel channel)
		{
			Console.WriteLine("-=-=-=-=-" + "Channel is Closed!" + "-=-=-=-=-" + "\n");
		}

		public void channelShunned()
		{
			Console.WriteLine("-=-=-=-=-" + "Channel is Shunned!" + "-=-=-=-=-" + "\n");
		}

		public void channelReconnected(Address addr)
		{
			Console.WriteLine("-=-=-=-=-" + "Channel is Reconnected!" + "-=-=-=-=-" + "\n");
		}

		static void Main(string[] args)
		{
			Efficency eff= new Efficency();
			Console.WriteLine("Finished2!!!");
		}
	}
}
