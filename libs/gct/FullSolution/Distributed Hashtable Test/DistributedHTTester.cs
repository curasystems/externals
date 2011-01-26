using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using GCT;
using GCT.Blocks;

namespace Testing
{
	/// <summary>
	/// Summary description for DistributedHTTester.
	/// </summary>
	public class DistributedHTTester : System.Windows.Forms.Form, DistributedHashtable.Listener
	{
		private System.Windows.Forms.GroupBox groupBox1;
		private System.Windows.Forms.TextBox txtKey;
		private System.Windows.Forms.Label label1;
		private System.Windows.Forms.Label label2;
		private System.Windows.Forms.TextBox txtValue;
		private System.Windows.Forms.Button btnAdd;
		private System.Windows.Forms.Button btnRemove;
		private System.Windows.Forms.Label label3;
		private System.Windows.Forms.Button btnClear;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		private string props= "UDP(mcast_addr=228.1.2.3;mcast_port=45566;ip_ttl=32):" +
			"PING(timeout=3000;num_initial_members=6):" +
			//"FD(timeout=3000):" +
			//"Basic Chat|OtherNamespace.FD2(timeout=3000):" +
			//"VERIFY_SUSPECT(timeout=1500):" +
			"STABLE(desired_avg_gossip=10000):" +
			"DISCARD(up=0.05;excludeItself=true):" +
			"NAKACK(gc_lag=10;retransmit_timeout=3000):" +
			"UNICAST(timeout=2000):" +
			"GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true):" +
			"CAUSAL";
		private Channel chan;
		private System.Windows.Forms.TextBox txtRemoveKey;
		private System.Windows.Forms.Button btnUpdate;
		private System.Windows.Forms.Label label4;
		private System.Windows.Forms.Label label5;
		private System.Windows.Forms.TextBox txtUpdateValue;
		private System.Windows.Forms.TextBox txtUpdateKey;
		private System.Windows.Forms.RichTextBox txtOutput;
		private DistributedHashtable ht;

		public DistributedHTTester()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();
	
			chan = new GroupChannel(props);
			ht = new DistributedHashtable(chan,"testGroup", this);
			refreshOutput();
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if(ht != null)
			{
				ht.Close();
			}
			if( disposing )
			{
				if(components != null)
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
			this.groupBox1 = new System.Windows.Forms.GroupBox();
			this.btnClear = new System.Windows.Forms.Button();
			this.btnRemove = new System.Windows.Forms.Button();
			this.label3 = new System.Windows.Forms.Label();
			this.txtRemoveKey = new System.Windows.Forms.TextBox();
			this.btnAdd = new System.Windows.Forms.Button();
			this.label2 = new System.Windows.Forms.Label();
			this.txtValue = new System.Windows.Forms.TextBox();
			this.label1 = new System.Windows.Forms.Label();
			this.txtKey = new System.Windows.Forms.TextBox();
			this.btnUpdate = new System.Windows.Forms.Button();
			this.label4 = new System.Windows.Forms.Label();
			this.txtUpdateValue = new System.Windows.Forms.TextBox();
			this.label5 = new System.Windows.Forms.Label();
			this.txtUpdateKey = new System.Windows.Forms.TextBox();
			this.txtOutput = new System.Windows.Forms.RichTextBox();
			this.groupBox1.SuspendLayout();
			this.SuspendLayout();
			// 
			// groupBox1
			// 
			this.groupBox1.Controls.AddRange(new System.Windows.Forms.Control[] {
																					this.txtOutput,
																					this.btnUpdate,
																					this.label4,
																					this.txtUpdateValue,
																					this.label5,
																					this.txtUpdateKey,
																					this.btnClear,
																					this.btnRemove,
																					this.label3,
																					this.txtRemoveKey,
																					this.btnAdd,
																					this.label2,
																					this.txtValue,
																					this.label1,
																					this.txtKey});
			this.groupBox1.Location = new System.Drawing.Point(8, 8);
			this.groupBox1.Name = "groupBox1";
			this.groupBox1.Size = new System.Drawing.Size(408, 344);
			this.groupBox1.TabIndex = 0;
			this.groupBox1.TabStop = false;
			this.groupBox1.Text = "Basic Controls";
			// 
			// btnClear
			// 
			this.btnClear.Location = new System.Drawing.Point(16, 304);
			this.btnClear.Name = "btnClear";
			this.btnClear.Size = new System.Drawing.Size(128, 24);
			this.btnClear.TabIndex = 9;
			this.btnClear.Text = "Clear All";
			this.btnClear.Click += new System.EventHandler(this.btnClear_Click);
			// 
			// btnRemove
			// 
			this.btnRemove.Location = new System.Drawing.Point(16, 272);
			this.btnRemove.Name = "btnRemove";
			this.btnRemove.Size = new System.Drawing.Size(128, 24);
			this.btnRemove.TabIndex = 7;
			this.btnRemove.Text = "Remove Entry";
			this.btnRemove.Click += new System.EventHandler(this.btnRemove_Click);
			// 
			// label3
			// 
			this.label3.Location = new System.Drawing.Point(16, 240);
			this.label3.Name = "label3";
			this.label3.Size = new System.Drawing.Size(32, 24);
			this.label3.TabIndex = 6;
			this.label3.Text = "Key";
			// 
			// txtRemoveKey
			// 
			this.txtRemoveKey.Location = new System.Drawing.Point(56, 240);
			this.txtRemoveKey.Name = "txtRemoveKey";
			this.txtRemoveKey.Size = new System.Drawing.Size(96, 20);
			this.txtRemoveKey.TabIndex = 5;
			this.txtRemoveKey.Text = "";
			// 
			// btnAdd
			// 
			this.btnAdd.Location = new System.Drawing.Point(16, 96);
			this.btnAdd.Name = "btnAdd";
			this.btnAdd.Size = new System.Drawing.Size(128, 24);
			this.btnAdd.TabIndex = 4;
			this.btnAdd.Text = "Add Entry";
			this.btnAdd.Click += new System.EventHandler(this.btnAdd_Click);
			// 
			// label2
			// 
			this.label2.Location = new System.Drawing.Point(16, 64);
			this.label2.Name = "label2";
			this.label2.Size = new System.Drawing.Size(40, 24);
			this.label2.TabIndex = 3;
			this.label2.Text = "Value";
			// 
			// txtValue
			// 
			this.txtValue.Location = new System.Drawing.Point(56, 64);
			this.txtValue.Name = "txtValue";
			this.txtValue.Size = new System.Drawing.Size(96, 20);
			this.txtValue.TabIndex = 2;
			this.txtValue.Text = "";
			// 
			// label1
			// 
			this.label1.Location = new System.Drawing.Point(16, 32);
			this.label1.Name = "label1";
			this.label1.Size = new System.Drawing.Size(32, 24);
			this.label1.TabIndex = 1;
			this.label1.Text = "Key";
			// 
			// txtKey
			// 
			this.txtKey.Location = new System.Drawing.Point(56, 32);
			this.txtKey.Name = "txtKey";
			this.txtKey.Size = new System.Drawing.Size(96, 20);
			this.txtKey.TabIndex = 0;
			this.txtKey.Text = "";
			// 
			// btnUpdate
			// 
			this.btnUpdate.Location = new System.Drawing.Point(16, 200);
			this.btnUpdate.Name = "btnUpdate";
			this.btnUpdate.Size = new System.Drawing.Size(128, 24);
			this.btnUpdate.TabIndex = 14;
			this.btnUpdate.Text = "Update Entry";
			this.btnUpdate.Click += new System.EventHandler(this.btnUpdate_Click);
			// 
			// label4
			// 
			this.label4.Location = new System.Drawing.Point(16, 168);
			this.label4.Name = "label4";
			this.label4.Size = new System.Drawing.Size(40, 24);
			this.label4.TabIndex = 13;
			this.label4.Text = "Value";
			// 
			// txtUpdateValue
			// 
			this.txtUpdateValue.Location = new System.Drawing.Point(56, 168);
			this.txtUpdateValue.Name = "txtUpdateValue";
			this.txtUpdateValue.Size = new System.Drawing.Size(96, 20);
			this.txtUpdateValue.TabIndex = 12;
			this.txtUpdateValue.Text = "";
			// 
			// label5
			// 
			this.label5.Location = new System.Drawing.Point(16, 136);
			this.label5.Name = "label5";
			this.label5.Size = new System.Drawing.Size(32, 24);
			this.label5.TabIndex = 11;
			this.label5.Text = "Key";
			// 
			// txtUpdateKey
			// 
			this.txtUpdateKey.Location = new System.Drawing.Point(56, 136);
			this.txtUpdateKey.Name = "txtUpdateKey";
			this.txtUpdateKey.Size = new System.Drawing.Size(96, 20);
			this.txtUpdateKey.TabIndex = 10;
			this.txtUpdateKey.Text = "";
			// 
			// txtOutput
			// 
			this.txtOutput.Location = new System.Drawing.Point(168, 16);
			this.txtOutput.Name = "txtOutput";
			this.txtOutput.Size = new System.Drawing.Size(224, 320);
			this.txtOutput.TabIndex = 15;
			this.txtOutput.Text = "";
			// 
			// DistributedHTTester
			// 
			this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
			this.ClientSize = new System.Drawing.Size(424, 365);
			this.Controls.AddRange(new System.Windows.Forms.Control[] {
																		  this.groupBox1});
			this.Name = "DistributedHTTester";
			this.Text = "Distributed Hashtable Tester";
			this.Load += new System.EventHandler(this.DistributedHTTester_Load);
			this.groupBox1.ResumeLayout(false);
			this.ResumeLayout(false);

		}
		#endregion

		private void DistributedHTTester_Load(object sender, System.EventArgs e)
		{
		
		}

		private void btnAdd_Click(object sender, System.EventArgs e)
		{
			ht.Add(txtKey.Text,txtValue.Text);
		}

		private void btnRemove_Click(object sender, System.EventArgs e)
		{
			ht.Remove(txtRemoveKey.Text);
		}

		private void btnClear_Click(object sender, System.EventArgs e)
		{
			ht.Clear();
		}

		private void btnUpdate_Click(object sender, System.EventArgs e)
		{
			ht[txtUpdateKey.Text] = txtUpdateValue.Text;
		}

		//------------- Hashtable Listener Interface ---------------------
		public void entryAdded(object key, object val)
		{
			refreshOutput();
			txtOutput.Text += "*** Entry Added ***\n";
		}

		public void entryUpdated(object key, object val)
		{
			refreshOutput();
			txtOutput.Text += "*** Entry Updated ***\n";
		}

		public void entryDeleted(object key)
		{
			refreshOutput();
			txtOutput.Text += "*** Entry Deleted ***\n";
		}

		public void HTCleared()
		{
			refreshOutput();
			txtOutput.Text += "*** Hashtable Cleared ***" + "\n";
		}

		private void refreshOutput()
		{
			txtOutput.Clear();
			foreach(DictionaryEntry e in ht)
			{
				txtOutput.Text += "Key: " + Convert.ToString(e.Key).PadRight(15) + "Value: " + Convert.ToString(e.Value) + "\n";
			}
		}


		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			DistributedHTTester frm = new DistributedHTTester();
			Application.Run(frm);
		}

	}
}
