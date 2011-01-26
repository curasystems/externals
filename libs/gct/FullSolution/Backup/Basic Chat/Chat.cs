using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;
using GCT;
using GCT.Blocks;
using System.Runtime.Serialization;
using System.Diagnostics;

namespace Basic_Chat
{
	public class Chat : System.Windows.Forms.Form, MessageListener, MembershipListener, ChannelListener
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		private Channel chan = null;
		private System.Windows.Forms.GroupBox groupBox1;
		private System.Windows.Forms.RichTextBox txtMsgsReceived;
		private System.Windows.Forms.GroupBox groupBox2;
		private System.Windows.Forms.ListBox lstMembers;
		private System.Windows.Forms.GroupBox Connection;
		private System.Windows.Forms.Button btnConnect;
		private System.Windows.Forms.TextBox txtGroupName;
		private System.Windows.Forms.GroupBox groupBox3;
		private System.Windows.Forms.Button btnSendMessage;
		private System.Windows.Forms.TextBox txtToSend;
		private System.Windows.Forms.PictureBox pictureBox1;
		private System.Windows.Forms.TextBox txtHandle;
		private PullPushAdapter ad;
		private Hashtable members = new Hashtable();
		private bool closing = false;
		private System.Windows.Forms.GroupBox groupBox4;
		private System.Windows.Forms.CheckBox chkCausal;
		private System.Windows.Forms.Button btnClearMessages;
		
		private String props = "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=32):" +
			"PING(timeout=3000;num_initial_members=6):" +
			//"FD(timeout=3000):" +
			//"Basic Chat|OtherNamespace.FD2(timeout=3000):" +
			//"VERIFY_SUSPECT(timeout=1500):" +
			"STABLE(desired_avg_gossip=2000):" +
			"DISCARD(up=0.4):" + //;excludeItself=true
			"NAKACK(gc_lag=10;retransmit_timeout=3000):" +
			"UNICAST(timeout=2000):" +
			"GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true)";
			
		public Chat()
		{
			//
			// Required for Windows Form Designer support
			//
			String time = DateTime.Now.ToString("yyyy-MM-dd HH.mm.ss.ff");
			Debug.Listeners.Add(new TextWriterTraceListener("CLIENT LOG (" + time + ").txt")); 
			Debug.AutoFlush = true;
			GCT.Trace.trace = true;

			InitializeComponent();
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			closing = true;
			if(chan != null)
			{
				chan.close();
				ad.stop();
			}
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			System.Resources.ResourceManager resources = new System.Resources.ResourceManager(typeof(Chat));
			this.groupBox1 = new System.Windows.Forms.GroupBox();
			this.txtMsgsReceived = new System.Windows.Forms.RichTextBox();
			this.groupBox2 = new System.Windows.Forms.GroupBox();
			this.lstMembers = new System.Windows.Forms.ListBox();
			this.Connection = new System.Windows.Forms.GroupBox();
			this.btnConnect = new System.Windows.Forms.Button();
			this.txtGroupName = new System.Windows.Forms.TextBox();
			this.groupBox3 = new System.Windows.Forms.GroupBox();
			this.txtHandle = new System.Windows.Forms.TextBox();
			this.btnSendMessage = new System.Windows.Forms.Button();
			this.txtToSend = new System.Windows.Forms.TextBox();
			this.pictureBox1 = new System.Windows.Forms.PictureBox();
			this.groupBox4 = new System.Windows.Forms.GroupBox();
			this.chkCausal = new System.Windows.Forms.CheckBox();
			this.btnClearMessages = new System.Windows.Forms.Button();
			this.groupBox1.SuspendLayout();
			this.groupBox2.SuspendLayout();
			this.Connection.SuspendLayout();
			this.groupBox3.SuspendLayout();
			this.groupBox4.SuspendLayout();
			this.SuspendLayout();
			// 
			// groupBox1
			// 
			this.groupBox1.Controls.AddRange(new System.Windows.Forms.Control[] {
																					this.txtMsgsReceived});
			this.groupBox1.Location = new System.Drawing.Point(8, 80);
			this.groupBox1.Name = "groupBox1";
			this.groupBox1.Size = new System.Drawing.Size(352, 224);
			this.groupBox1.TabIndex = 9;
			this.groupBox1.TabStop = false;
			this.groupBox1.Text = "Messages";
			// 
			// txtMsgsReceived
			// 
			this.txtMsgsReceived.Cursor = System.Windows.Forms.Cursors.Default;
			this.txtMsgsReceived.Enabled = false;
			this.txtMsgsReceived.Location = new System.Drawing.Point(8, 16);
			this.txtMsgsReceived.Name = "txtMsgsReceived";
			this.txtMsgsReceived.ReadOnly = true;
			this.txtMsgsReceived.Size = new System.Drawing.Size(336, 200);
			this.txtMsgsReceived.TabIndex = 3;
			this.txtMsgsReceived.Text = "";
			// 
			// groupBox2
			// 
			this.groupBox2.Controls.AddRange(new System.Windows.Forms.Control[] {
																					this.lstMembers});
			this.groupBox2.Location = new System.Drawing.Point(368, 80);
			this.groupBox2.Name = "groupBox2";
			this.groupBox2.Size = new System.Drawing.Size(200, 224);
			this.groupBox2.TabIndex = 10;
			this.groupBox2.TabStop = false;
			this.groupBox2.Text = "Members";
			// 
			// lstMembers
			// 
			this.lstMembers.Enabled = false;
			this.lstMembers.Location = new System.Drawing.Point(8, 16);
			this.lstMembers.Name = "lstMembers";
			this.lstMembers.Size = new System.Drawing.Size(184, 199);
			this.lstMembers.TabIndex = 4;
			this.lstMembers.MouseUp += new System.Windows.Forms.MouseEventHandler(this.lstMembers_MouseUp);
			// 
			// Connection
			// 
			this.Connection.Controls.AddRange(new System.Windows.Forms.Control[] {
																					 this.btnConnect,
																					 this.txtGroupName});
			this.Connection.Location = new System.Drawing.Point(8, 16);
			this.Connection.Name = "Connection";
			this.Connection.Size = new System.Drawing.Size(344, 48);
			this.Connection.TabIndex = 11;
			this.Connection.TabStop = false;
			this.Connection.Text = "Connection";
			// 
			// btnConnect
			// 
			this.btnConnect.Location = new System.Drawing.Point(256, 16);
			this.btnConnect.Name = "btnConnect";
			this.btnConnect.Size = new System.Drawing.Size(72, 24);
			this.btnConnect.TabIndex = 2;
			this.btnConnect.Text = "Connect";
			this.btnConnect.Click += new System.EventHandler(this.btnConnect_Click);
			// 
			// txtGroupName
			// 
			this.txtGroupName.Location = new System.Drawing.Point(8, 20);
			this.txtGroupName.Name = "txtGroupName";
			this.txtGroupName.Size = new System.Drawing.Size(232, 20);
			this.txtGroupName.TabIndex = 1;
			this.txtGroupName.Text = "DefaultGroup";
			// 
			// groupBox3
			// 
			this.groupBox3.Controls.AddRange(new System.Windows.Forms.Control[] {
																					this.txtHandle,
																					this.btnSendMessage,
																					this.txtToSend});
			this.groupBox3.Location = new System.Drawing.Point(8, 312);
			this.groupBox3.Name = "groupBox3";
			this.groupBox3.Size = new System.Drawing.Size(560, 48);
			this.groupBox3.TabIndex = 12;
			this.groupBox3.TabStop = false;
			this.groupBox3.Text = "Send Message";
			// 
			// txtHandle
			// 
			this.txtHandle.Enabled = false;
			this.txtHandle.Location = new System.Drawing.Point(16, 20);
			this.txtHandle.Name = "txtHandle";
			this.txtHandle.Size = new System.Drawing.Size(64, 20);
			this.txtHandle.TabIndex = 5;
			this.txtHandle.Text = "MyName";
			// 
			// btnSendMessage
			// 
			this.btnSendMessage.Enabled = false;
			this.btnSendMessage.Location = new System.Drawing.Point(472, 16);
			this.btnSendMessage.Name = "btnSendMessage";
			this.btnSendMessage.Size = new System.Drawing.Size(72, 24);
			this.btnSendMessage.TabIndex = 7;
			this.btnSendMessage.Text = "Send";
			this.btnSendMessage.Click += new System.EventHandler(this.btnSendMessage_Click);
			// 
			// txtToSend
			// 
			this.txtToSend.Enabled = false;
			this.txtToSend.Location = new System.Drawing.Point(88, 20);
			this.txtToSend.Name = "txtToSend";
			this.txtToSend.Size = new System.Drawing.Size(376, 20);
			this.txtToSend.TabIndex = 6;
			this.txtToSend.Text = "";
			this.txtToSend.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.txtToSend_KeyPress);
			// 
			// pictureBox1
			// 
			this.pictureBox1.Image = ((System.Drawing.Bitmap)(resources.GetObject("pictureBox1.Image")));
			this.pictureBox1.Location = new System.Drawing.Point(424, 8);
			this.pictureBox1.Name = "pictureBox1";
			this.pictureBox1.Size = new System.Drawing.Size(104, 64);
			this.pictureBox1.TabIndex = 13;
			this.pictureBox1.TabStop = false;
			this.pictureBox1.Click += new System.EventHandler(this.pictureBox1_Click);
			// 
			// groupBox4
			// 
			this.groupBox4.Controls.AddRange(new System.Windows.Forms.Control[] {
																					this.btnClearMessages,
																					this.chkCausal});
			this.groupBox4.Location = new System.Drawing.Point(8, 376);
			this.groupBox4.Name = "groupBox4";
			this.groupBox4.Size = new System.Drawing.Size(560, 96);
			this.groupBox4.TabIndex = 14;
			this.groupBox4.TabStop = false;
			this.groupBox4.Text = "Secret Options";
			// 
			// chkCausal
			// 
			this.chkCausal.Checked = true;
			this.chkCausal.CheckState = System.Windows.Forms.CheckState.Checked;
			this.chkCausal.Location = new System.Drawing.Point(16, 24);
			this.chkCausal.Name = "chkCausal";
			this.chkCausal.Size = new System.Drawing.Size(200, 16);
			this.chkCausal.TabIndex = 0;
			this.chkCausal.Text = "Causal Ordering";
			// 
			// btnClearMessages
			// 
			this.btnClearMessages.Location = new System.Drawing.Point(408, 16);
			this.btnClearMessages.Name = "btnClearMessages";
			this.btnClearMessages.Size = new System.Drawing.Size(136, 24);
			this.btnClearMessages.TabIndex = 1;
			this.btnClearMessages.Text = "Clear Messages";
			this.btnClearMessages.Click += new System.EventHandler(this.btnClearMessages_Click);
			// 
			// Chat
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(576, 365);
			this.Controls.AddRange(new System.Windows.Forms.Control[] {
																		  this.groupBox4,
																		  this.pictureBox1,
																		  this.groupBox3,
																		  this.Connection,
																		  this.groupBox2,
																		  this.groupBox1});
			this.MaximizeBox = false;
			this.Name = "Chat";
			this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
			this.Text = "Group Communication: Basic Chat";
			this.Load += new System.EventHandler(this.Chat_Load);
			this.groupBox1.ResumeLayout(false);
			this.groupBox2.ResumeLayout(false);
			this.Connection.ResumeLayout(false);
			this.groupBox3.ResumeLayout(false);
			this.groupBox4.ResumeLayout(false);
			this.ResumeLayout(false);

		}
		#endregion

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			Chat frm = new Chat();
			Application.Run(frm);
		}

		private void Chat_Load(object sender, System.EventArgs e)
		{
		
		}

		// -=-=-=-=-=-=-=- MessageListener Interface -=-=-=-=-=-=-=-
		public void receive(GCT.Message msg) 
		{
			try 
			{
				Object obj = msg.getObject();
				if(obj!=null && (obj is String))
				{
					String str = (String)obj;
					String userHandler = "???";
					int pos = str.IndexOf("|:|");
					if(pos != -1)
					{
						userHandler = str.Substring(0,pos);
						str = str.Substring((pos+3),str.Length-(pos+3));
					}
					if(members.Contains(msg.Source))
					{
						members[msg.Source] = userHandler;
						updateMembers();
						txtMsgsReceived.AppendText(userHandler + ":   " + (String)str + "\n");
						
						txtMsgsReceived.SelectionStart = txtMsgsReceived.TextLength;
						txtMsgsReceived.Focus();
						txtMsgsReceived.ScrollToCaret();
					}
				}

			}
			catch(Exception e) 
			{
			}
		}

		// -=-=-=-=-=-=-=- MembershipListener Interface -=-=-=-=-=-=-=-
		public void viewAccepted(GCT.View new_view) 
		{
			members = new Hashtable();
			ArrayList tmpMbrs = new_view.getMembers();

			for (int x = 0; x < tmpMbrs.Count; x++)
			{
				members.Add((Address)tmpMbrs[x], "User" + x);
			}
			updateMembers();
      	}

		private void updateMembers()
		{
			// Shutdown the painting of the ListBox as items are added.
			lstMembers.BeginUpdate();
			lstMembers.Items.Clear();
			// Loop through and add 50 items to the ListBox.
			foreach(String userHandler in members.Values)
			{
				lstMembers.Items.Add(userHandler);
			}
			// Allow the ListBox to repaint and display the new items.
			lstMembers.EndUpdate();
		}

		public void suspect(Address suspected_mbr) {}
		public void block() {}

		private void btnConnect_Click(object sender, System.EventArgs e)
		{
			disconnectChannel();
		}

		private void disconnectChannel()
		{
			if(chan==null)
			{
				if(chkCausal.Checked)
					props += ":CAUSAL"; 
				chan = new GroupChannel(props);
				chan.setChannelListener(this);
				ad = new PullPushAdapter(chan, this, this);
			}
			if(!chan.isConnected())
			{
				if(String.Compare(txtGroupName.Text.Trim(),"")!=0)
				{
					chan.connect(txtGroupName.Text.Trim());
					ad.start();
				}
				else
				{
					MessageBox.Show(this,"text","caption",MessageBoxButtons.OK);
					return;
				}
				btnConnect.Text = "Disconnect";
				txtGroupName.Enabled = false;
				txtToSend.Enabled = true;
				txtHandle.Enabled = true;
				txtToSend.Text = "";
				btnSendMessage.Enabled = true;
				lstMembers.Enabled = true;
				txtMsgsReceived.Enabled = true;
				txtMsgsReceived.Text = "";
			}
			else
			{
				ad.stop();
				chan.disconnect();
				btnConnect.Text = "Connect";
				txtGroupName.Enabled = true;
				txtToSend.Enabled = false;
				btnSendMessage.Enabled = false;
				txtHandle.Enabled = false;
				lstMembers.Enabled = false;
				txtMsgsReceived.Enabled = false;
			}
		}

		private void btnSendMessage_Click(object sender, System.EventArgs e)
		{
			sendMessage();
		}

		private void sendMessage()
		{
			if(String.Compare(txtHandle.Text.Trim(),"")==0 || String.Compare(txtHandle.Text.Trim(),"|:|")==0)
			{
				txtHandle.Text = "DefaultName";
			}

			if(String.Compare(txtToSend.Text.Trim(),"")!=0)
			{
				GCT.Message msg = null;
				if(lstMembers.SelectedIndex!=-1)
				{
					foreach(DictionaryEntry dic in members)
					{
						if((String.Compare((String)dic.Value,(String)lstMembers.SelectedItem)==0))
						{
							if(!dic.Key.Equals(chan.getLocalAddress()))
							{
								msg = new GCT.Message((Address)dic.Key,chan.getLocalAddress(),txtHandle.Text + "|:|" + txtToSend.Text);
							}
							else
							{
								MessageBox.Show("Can't send message to yourself!!", "Private Message Error", MessageBoxButtons.OK,MessageBoxIcon.Exclamation);
							}
						}
					}
				}
				else
				{
					msg = new GCT.Message(null,null,txtHandle.Text + "|:|" + txtToSend.Text);
				}

				if(msg!=null)
					chan.send(msg);
				txtToSend.Text = "";
			}
		}

		private void txtToSend_KeyPress(object sender, KeyPressEventArgs e)
		{
			if(e.KeyChar == (char)13)
				sendMessage();
		}

		private void lstMembers_MouseUp(object sender, MouseEventArgs e)
		{
			if(e.Button == MouseButtons.Right)
				lstMembers.SelectedIndex = -1;
		}

	// -=-=-=-=-=-=-=- ChannelListener Interface -=-=-=-=-=-=--=-
		public void channelConnected(Channel channel)
		{
			txtMsgsReceived.Clear();
			txtMsgsReceived.AppendText("-=-=-=-=-" + "Channel is Connected!" + "-=-=-=-=-" + "\n");
		}

		public void channelDisconnected(Channel channel)
		{
			if (!closing)
				txtMsgsReceived.AppendText("-=-=-=-=-" + "Channel is Disconnected!" + "-=-=-=-=-" + "\n");
		}

		public void channelClosed(Channel channel)
		{
			if (!closing)
				txtMsgsReceived.AppendText("-=-=-=-=-" + "Channel is Closed!" + "-=-=-=-=-" + "\n");
		}

		public void channelShunned()
		{
			if (!closing)
				txtMsgsReceived.AppendText("-=-=-=-=-" + "Channel is Shunned!" + "-=-=-=-=-" + "\n");
		}

		public void channelReconnected(Address addr)
		{
			if (!closing)
				txtMsgsReceived.AppendText("-=-=-=-=-" + "Channel is Reconnected!" + "-=-=-=-=-" + "\n");
		}

		private void pictureBox1_Click(object sender, System.EventArgs e)
		{
			Console.WriteLine("Pressed");
			if(this.Height < 504)
				this.Height = 504;
			else
				this.Height = 392;
		}

		private void btnClearMessages_Click(object sender, System.EventArgs e)
		{
			txtMsgsReceived.Clear();
		}

	}
}
