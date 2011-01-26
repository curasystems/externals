using System;
using System.Collections;
using System.Data;
using System.Reflection;
using System.Resources;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Runtime.Remoting.Channels.Http;

namespace GCT.Stack
{
	/// <remarks>
	/// This class is responsible for creating instances of the protocol layers
	/// from the textual configuration string. 
	/// Once the layers are created, they will be configured.
	/// </remarks>
	/// <summary>
	/// Sets up ProtocolSinkStack based on configuration string.
	/// <p><b>Author:</b> Chris Koiak, Bela Ban</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class Configurator
	{

		/// <remarks>
		/// The following steps are followed:
		/// <list type="">
		/// <item>A <c>ProtocolConfiguration</c> object is created for each protocol</item>
		/// <item>The <c>Protocol</c>s are created from the <c>ProtocolConfiguration</c> objects</item>
		/// <item>The Protocols are all connected together</item>
		/// </list>
		/// </remarks>
		/// <summary>
		/// Sets up the <c>ProtocolSinkStack</c> with the specified configuration string.
		/// </summary>
		/// <param name="configuration">The String configuration of the stack</param>
		/// <param name="st">Reference to the <c>ProtocolSinkStack</c> which each <c>Protocol</c> should reference</param>
		/// <returns>The top-most <c>Protocol</c> in the stack</returns>
		public Protocol setupStack(string configuration, ProtocolSinkStack st)
		{
			ProtocolConfiguration[]		layer_configs;
			Protocol[]					layers;
			Protocol					top_prot;

			layer_configs = parseConfigurations(configuration);
			layers = createProtocols(layer_configs, st);
			if(layers == null)
				return null;
			top_prot = connectProtocols(layers);
			return top_prot;
		}

		/// <summary>
		/// Creates a <c>ProtocolConfiguration</c> instance for each protocol in the configuration string
		/// </summary>
		/// <param name="config">The configuration string</param>
		/// <returns>The array of configuration objects</returns>
		private ProtocolConfiguration[] parseConfigurations(string config)
		{
			ProtocolConfiguration[]		retval; //= new ArrayList();
			string[]					component_strings = config.Split(new char[] {':'});
			String						component_string;
			ProtocolConfiguration		protocol_config;

			if(component_strings == null)
				return null;
			
			retval = new ProtocolConfiguration[component_strings.Length];
			
			for(int i=0; i < component_strings.Length; i++) 
			{
				component_string = (String)component_strings.GetValue(i);
				protocol_config = new ProtocolConfiguration(component_string);
				retval[i] = protocol_config;
			}
			return retval;
		}

		/// <summary>
		/// Creates an array of <c>Protocols</c> from the configurations
		/// </summary>
		/// <param name="protocol_configs">Array of <c>ProtocolConfiguration</c> objects</param>
		/// <param name="stack">Instance <c>ProtocolSinkStack</c> which the Protocol layers should reference</param>
		/// <returns>Array of <c>Protocol</c> objects</returns>
		private Protocol[] createProtocols(ProtocolConfiguration[] protocol_configs, ProtocolSinkStack stack)
		{
			Protocol[]             retval = new Protocol[protocol_configs.Length];
			Protocol               layer;

			for(int i=0; i < protocol_configs.Length; i++) 
			{
				layer = (Protocol)protocol_configs[i].createLayer(stack); //stack
				if(layer == null)
					return null;
				retval[i] = layer;
			}
	
			sanityCheck(retval);
			return retval;
		}

		/// <summary>
		/// Checks all <c>Protocol</c>s are unique and that 
		/// each required up/down service is provided
		/// </summary>
		/// <param name="protocols">Protocols that will be used for the stack</param>
		private void sanityCheck(Protocol[] protocols)
		{
			Protocol		prot;
			String			name;
			ProtocolReq		req;
			ArrayList		req_list=new ArrayList();
			int				evt_type;

			// Checks for unique names
			for(int i=0; i < protocols.Length; i++) 
			{
				prot = (Protocol)protocols[i];
				name = prot.Name;
				for(int j=0; j < protocols.Length; j++) 
				{
					if(i==j)
						continue;
					if(String.Compare(name,protocols[j].Name)==0) 
					{
						throw new Exception("Configurator.sanityCheck(): protocol name " + name +
							" has been used more than once; protocol names have to be unique !");
					}
				}
			}

			// Checks whether all requirements of all layers are met	
			for(int i=0; i < protocols.Length; i++) 
			{
				prot=(Protocol)protocols[i];
				req=new ProtocolReq(prot.Name);
				req.up_reqs=prot.requiredUpServices();
				req.down_reqs=prot.requiredDownServices();
				req.up_provides=prot.providedUpServices();
				req.down_provides=prot.providedDownServices();
				req_list.Add(req);
			}

	
			for(int i=0; i < req_list.Count; i++) 
			{
				req=(ProtocolReq)req_list[i];
	    
				// check whether layers above this one provide corresponding down services
				if(req.up_reqs != null) 
				{
					for(int j=0; j < req.up_reqs.Count; j++) 
					{
						evt_type=((int)req.up_reqs[j]);

						if(!providesDownServices(i, req_list, evt_type)) 
						{
							throw new Exception("Configurator.sanityCheck(): event " +
								Event.type2String(evt_type) + " is required by " +
								req.name + ", but not provided by any of the layers above");
						}
					}
				}

				// check whether layers below this one provide corresponding up services
				if(req.down_reqs != null) 
				{  // check whether layers above this one provide up_reqs
					for(int j=0; j < req.down_reqs.Count; j++) 
					{
						evt_type=((int)req.down_reqs[j]);

						if(!providesUpServices(i, req_list, evt_type)) 
						{
							throw new Exception("Configurator.sanityCheck(): event " +
								Event.type2String(evt_type) + " is required by " +
								req.name + ", but not provided by any of the layers below");
						}
					}
				}
	    
			}
		}

		/// <summary>
		/// Check whether any of the protocols 'below' end_index provide <c>Event</c> (evt_type)
		/// </summary>
		/// <param name="end_index">Position in the stack which Protocol must be below</param>
		/// <param name="req_list">List of services/events provided by the Procotols</param>
		/// <param name="evt_type">Service that is required</param>
		/// <returns>True if service is provided by a 'lower' protocol, otherwise false</returns>
		private bool providesUpServices(int end_index, ArrayList req_list, int evt_type) 
		{
			ProtocolReq req;

			for(int i=0; i < end_index; i++) 
			{
				req=(ProtocolReq)req_list[i];
				if(req.providesUpService(evt_type))
					return true;
			}
			return false;
		}


		/// <summary>
		/// Check whether any of the protocols 'above' end_index provide <c>Event</c> (evt_type)
		/// </summary>
		/// <param name="start_index">Position in the stack which Protocol must be above</param>
		/// <param name="req_list">List of services/events provided by the Procotols</param>
		/// <param name="evt_type">Service that is required</param>
		/// <returns>True if service is provided by a 'higher' protocol, otherwise false</returns>
		private bool providesDownServices(int start_index, ArrayList req_list, int evt_type) 
		{
			ProtocolReq req;

			for(int i=start_index; i < req_list.Count; i++) 
			{
				req=(ProtocolReq)req_list[i];
				if(req.providesDownService(evt_type))
					return true;
			}
			return false;
		}

		/// <remarks>
		/// Prootocols will be linked according to their position in the list.
		/// </remarks>
		/// <summary>
		/// Connects all the <c>Protocols</c> together.
		/// </summary>
		/// <param name="layer_list">List of <c>Protocol</c>s to be connected</param>
		/// <returns>The top-most <c>Protocol</c> in the connected stack</returns>
		private Protocol connectProtocols(Protocol[] layer_list) 
		{
			
			Protocol current_layer=null, next_layer=null;

			for(int i=0; i < layer_list.Length; i++) 
			{
				current_layer=(Protocol)layer_list[i];
				if(i+1 >= layer_list.Length)
					break;
				next_layer=(Protocol)layer_list[i+1];
				current_layer.UpProtocol = next_layer;
				next_layer.DownProtocol = current_layer;	    
			}

			return current_layer;
		}

		/// <summary>
		/// Moves down the stack until the last <c>Protocol</c> is discovered
		/// </summary>
		/// <param name="prot_stack">Top protocol in the stack</param>
		/// <returns>The Bottom-most <c>Protocol</c> found</returns>
		public Protocol getBottommostProtocol(Protocol prot_stack)  
		{
			Protocol  tmp=null, curr_prot=prot_stack;

			while(true) 
			{
				tmp=curr_prot.DownProtocol;
				if(tmp == null)
					break;
				curr_prot=tmp;
			}
			if(curr_prot == null)
				throw new Exception("Configurator.getBottommostProtocol(): bottommost protocol is null");
			
			return curr_prot;
		}

		/// <summary>
		/// Starts the Up and Down Handler threads in every Protocol.
		/// </summary>
		/// <param name="bottom_prot">Start every Protocol from this one and above</param>
		public void startProtocolStack(Protocol bottom_prot) 
		{
			while(bottom_prot != null) 
			{
				if(Trace.trace)
					Trace.info("Configutator.startProtocolStack()", "Starting Protocol: " + bottom_prot.Name);
				bottom_prot.startDownHandler();
				bottom_prot.startUpHandler();
				bottom_prot=bottom_prot.UpProtocol;
			}
		}

		/// <summary>
		/// Stops all <c>Protocol</c> Up and Down Handler threads.
		/// </summary>
		/// <param name="start_prot">Stop every Protocol from this one down</param>
		public void stopProtocolStack(Protocol start_prot) 
		{	
			while(start_prot != null) 
			{
				start_prot.stopInternal();
				start_prot = start_prot.DownProtocol;
			}
		}

		/// <summary>
		/// Internal Class for holding a Protocols required and provided services
		/// </summary>
		internal class ProtocolReq 
		{
			public ArrayList   up_reqs=null;
			public ArrayList   down_reqs=null;
			public ArrayList   up_provides=null;
			public ArrayList   down_provides=null;
			public String		name=null;

			/// <summary>
			/// Constructor
			/// </summary>
			/// <param name="name">Protocol Name</param>
			public ProtocolReq(String name) {this.name=name;}

			/// <summary>
			/// Checks if <c>Protocol</c> provides the up service
			/// </summary>
			/// <param name="evt_type">Service required</param>
			/// <returns>True if service is provided, otherwise false</returns>
			public bool providesUpService(int evt_type) 
			{
				int type;

				if(up_provides != null) 
				{
					for(int i=0; i < up_provides.Count; i++) 
					{
						type=(int)up_provides[i];
						if(type == evt_type)
							return true;
					}
				}
				return false;
			}

			/// <summary>
			/// Checks if <c>Protocol</c> provides the down service
			/// </summary>
			/// <param name="evt_type">Service required</param>
			/// <returns>True if service is provided, otherwise false</returns>
			public bool providesDownService(int evt_type) 
			{
				int type;

				if(down_provides != null) 
				{
					for(int i=0; i < down_provides.Count; i++) 
					{
						type=(int)down_provides[i];
						if(type == evt_type)
							return true;
					}
				}
				return false;
			}
		}


		/// <summary>
		/// Internal Class that holds the Configuration for a Protocol
		/// </summary>
		internal class ProtocolConfiguration
		{
			private string					protocol_name=null;
			private string					assembly_name=null;
			private string					properties_str=null;
			private PropertyCollection		properties = new PropertyCollection();
			const string  protocol_prefix = "org.javagroups.protocols";

			/// <remarks>
			/// Calls <c>setContents()</c> on initialisation
			/// </remarks>
			/// <summary>
			/// Constructor.
			/// </summary>
			/// <param name="config_str">The configuration string for a single Protocol</param>
			public ProtocolConfiguration(string config_str)
			{
				setContents(config_str);
			}

			/// <summary>
			/// Populates a collection with the properties specified in the config string
			/// </summary>
			/// <param name="config_str">Configuration string</param>
			private void setContents(string config_str)
			{
				int index = config_str.IndexOf('(');  // e.g. "UDP(in_port=3333)"

				int end_index=config_str.LastIndexOf(')');

				if(index == -1) 
				{
					protocol_name=config_str;
				}
				else 
				{
					if(end_index == -1) 
					{
						throw new Exception("Configurator.ProtocolConfiguration.setContents(): closing ')' " +
							"not found in " + config_str + ": properties cannot be set !");
					}
					else 
					{
						properties_str = config_str.Substring(index+1, end_index-(index+1));
						protocol_name = config_str.Substring(0, index);
						//Check for an Assembly Name in the Protocol String
						char[] sep = new char[1];
						sep[0] = '|';
						string[] str = protocol_name.Split(sep,2);
						if(str.Length == 2)
						{
							assembly_name = str[0];
							protocol_name = str[1];
						}
					}
				}

				// "in_port=5555;out_port=6666"
				if(properties_str != null) 
				{
					string[] components = properties_str.Split(new char[] {';'});
					if(components.Length > 0) 
					{
						for(int i=0; i < components.Length; i++) 
						{
							String  name, prop_value, comp=(String)components.GetValue(i);
							index=comp.IndexOf('=');
							if(index == -1) 
							{
								throw new Exception("Configurator.ProtocolConfiguration.setContents(): " +
									"'=' not found in " + comp);
							}
							name=comp.Substring(0, index);
							prop_value=comp.Substring(index+1, comp.Length-(index+1));
							properties.Add(name, prop_value);			
						}
					}
				}
			}

			/// <remarks>
			/// Uses <c>Activator.CreateInstance()</c> initialise each <c>Protocol</c>
			/// </remarks>
			/// <summary>
			/// Creates the Protocol, sets the properties and calls <c>init()</c> 
			/// </summary>
			/// <param name="st">The <c>ProtocolSinkStack</c> the <c>Protocol</c>
			/// should be linked to
			/// </param>
			/// <returns></returns>
			public Object createLayer(ProtocolSinkStack st)
			{
				ObjectHandle obj = null;
			
				int prefixSize = 3;

				String[] prefix = new String[prefixSize];
				prefix[0] = "GCT.Protocols.";
				prefix[1] = "GCT.";
				prefix[2] = "GCT.Protocols.";

				if(assembly_name != null)
					prefix[0] = "";

				for(int i = 0; i<prefixSize ; i++)
				{
					try
					{
						obj = Activator.CreateInstance(assembly_name, prefix[i] + protocol_name);
						if(Trace.trace)
							Trace.info("Configurator.createLayer()","Created Layer " + prefix[i] + protocol_name);
						if (obj != null)
						{
							break;
						}
					}
					catch(Exception e1)
					{
						Console.WriteLine(e1+ "\n"+e1.StackTrace);
						Console.WriteLine();
					}
				}

				Protocol gSink = null;
				if (obj != null)
				{
					gSink = (Protocol)obj.Unwrap();
					gSink.ProtocolSinkStack = st;
					if(!gSink.setPropertiesInternal(properties))
						return null;
					gSink.init();
				}
				else
				{
					if(Trace.trace)
						Trace.error("Configurator.createLayer()", "Couldn't create layer: " + protocol_name);
				}
			
				return gSink;

			}
		}
	
	}
}
