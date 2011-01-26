using System;
using System.Threading;
using System.Collections;
using GCT;
using GCT.Blocks;

namespace Testing
{
	/// <summary>
	/// Summary description for RandomTester.
	/// </summary>
	public class RandomTester
	{
		static void Main(string[] args)
		{
			/*
			Hashtable ht = new Hashtable();
			ht.Add("string1","value1");
			ht.Add("string2","value2");
			ht.Add("string3","value3");

			Console.WriteLine("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			Console.WriteLine("Initial Hashtable");
			Console.WriteLine("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			foreach(DictionaryEntry e in ht)
			{
				Console.WriteLine("Key: " + e.Key.ToString() + "		Value:" + e.Key.ToString() );
			}
			Console.WriteLine("");
			Console.WriteLine("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			Console.WriteLine("Modified Hashtable");
			Console.WriteLine("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
			ICollection vals = ht.Values;
			IEnumerator enu = vals.GetEnumerator();
			*/
			string props = "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=32):" +
			"PING(timeout=3000;num_initial_members=6):" +
			//"FD(timeout=3000):" +
			//"Basic Chat|OtherNamespace.FD2(timeout=3000):" +
			//"VERIFY_SUSPECT(timeout=1500):" +
			"STABLE(desired_avg_gossip=10000):" +
			"DISCARD(up=0.4;excludeItself=true):" +
			"NAKACK(gc_lag=10;retransmit_timeout=3000):" +
			"UNICAST(timeout=2000):" +
			"GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";
		
			Channel chan = new GroupChannel(props);
			DistributedHashtable ht = new DistributedHashtable(chan,"testGroup", null);
			ht.Add(RandomTester.getRandom5Digit(),RandomTester.getRandom5Digit());

		}

		static public string getRandom5Digit()
		{
			Random r = new Random();
			string ret = "";
			for(int i = 0;i<5;i++)
				ret += (r.Next(26)+65);
			return ret;
		}
	}
}
