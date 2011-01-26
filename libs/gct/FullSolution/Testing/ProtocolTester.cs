using System;
using GChannel;
using GChannel.Protocols;
using GChannel.Stack;

namespace LayerTester
{

	public class ProtocolTester 
	{
		Protocol     harness=null, top, bottom;
		String       props=null;
		Configurator config=null;

		public ProtocolTester(String prot_spec, Protocol harness)
		{
			if(prot_spec == null || harness == null)
				throw new Exception("ProtocolTester(): prot_spec or harness is null");

			props=prot_spec;
			this.harness=harness;
			props="LOOPBACK:" + props; // add a loopback layer at the bottom of the stack

			config=new Configurator();
			top=config.setupClientStack(props, null);
			harness.DownProtocol = top;
			top.UpProtocol = harness;

			bottom = getBottomProtocol(top);
			config.startProtocolStack(bottom);
		}
    
		public String getProtocolSpec() 
		{
			return props;
		}

		public void stop() 
		{
			Protocol p;
			if(harness != null) 
			{
				p=harness;
				while(p != null) 
				{
					p.stop();
					p=p.getDownProtocol();
				}
				config.stopProtocolStack(harness);
			}
			else if(top != null) 
			{
				p=top;
				while(p != null) 
				{
					p.stop();
					p=p.getDownProtocol();
				}
				config.stopProtocolStack(top);
			}
		}

    
		Protocol getBottomProtocol(Protocol top) 
		{
			Protocol tmp;

			if(top == null)
				return null;
	
			tmp=top;
			while(tmp.getDownProtocol() != null)
				tmp=tmp.getDownProtocol();
			return tmp;
		}


		public static void main(String[] args) 
		{
			String         props;
			ProtocolTester t;
			Harness        h;
			boolean        trace=false;

			if(args.length < 1 || args.length > 2) 
			{
				Console.WriteLine("ProtocolTester <protocol stack spec> [-trace]");
				return;
			}
			props=args[0];

			try 
			{
				h=new Harness();
				t=new ProtocolTester(props, h);
				Console.WriteLine("protocol specification is " + t.getProtocolSpec());
				h.down(new Event(Event.BECOME_SERVER));
				for(int i=0; i < 5; i++) 
				{
					Console.WriteLine("Sending msg #" + i);
					h.down(new Event(Event.MSG, new Message(null, null, "Hello world #" + i)));
				}
				Util.sleep(500);
				t.stop();
			}
			catch(Exception ex) 
			{
				System.err.println(ex);
			}
		}



		internal class Harness : Protocol 
		{

			public String getName() 
			{
				return "Harness";
			}


			public override void up(Event evt) 
			{
				Console.WriteLine("Harness.up(): " + evt);
			}
			
			public override void start(){}
			public override void stop(){}

		}
    
	}
}
