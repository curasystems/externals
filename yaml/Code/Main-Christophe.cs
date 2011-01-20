//     ====================================================================================================
//     YAML Parser for the .NET Framework
//     ====================================================================================================
//
//     Copyright (c) 2006
//         Christophe Lambrechts
//         Jonathan Slenders
//
//     ====================================================================================================
//     This file is part of the .NET YAML Parser.
// 
//     This .NET YAML parser is free software; you can redistribute it and/or modify
//     it under the terms of the GNU Lesser General Public License as published by
//     the Free Software Foundation; either version 2.1 of the License, or
//     (at your option) any later version.
// 
//     The .NET YAML parser is distributed in the hope that it will be useful,
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//     GNU Lesser General Public License for more details.
// 
//     You should have received a copy of the GNU Lesser General Public License
//     along with Foobar; if not, write to the Free Software
//     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USAusing System.Reflection;
//     ====================================================================================================

using System;
using System.Collections;

namespace Yaml
{
	/// <summary>
	///   Test class that has a Main() method
	/// </summary>
	class MainChristophe
	{
		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		static void Main(string[] args)
		{
			string testType;
			
			// testType = "base64";
			testType = "Scalar";
			// testType = "boxing";
			// testType = "int32";
			
			if(testType.Equals("base64"))
			{
				/*string bin = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4" + 
"OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx" + 
"cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq" +
"q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj" +
"5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==";*/
				string bin = "R0lGODlhDAAMAIQAAP//9/X17unp5WZmZgAAAOfn515eXvPz7Y6OjuDg4J+fn5" +
					"OTk6enp56enmlpaWNjY6Ojo4SEhP/++f/++f/++f/++f/++f/++f/++f/++f/+" +
					"f/++f/++f/++f/++f/++SH+Dk1hZGUgd2l0aCBHSU1QACwAAAAADAAMAAAFLC" +
					"AgjoEwnuNAFOhpEMTRiggcz4BNJHrv/zCFcLiwMWYNG84BwwEeECcgggoBADs";
				char[] array = bin.ToCharArray();
				
				byte[] hulp;
				hulp = System.Convert.FromBase64CharArray(array, 0, array.Length);

				for(int i = 0; i < hulp.Length; i++)
                    Console.Write(hulp[i] + " ");
			}
			else if(testType.Equals("Scalar"))
			{
				try
				{
					testYamlNode();
				}
				catch (Exception e)
				{
					Console.WriteLine(e);
				}
			}
			else if(testType.Equals("boxing"))
			{
				int test = 20;
				object ref1 = (object) test;
				object ref2 = ref1;

				Console.WriteLine("ref1 = " + ref1);
				Console.WriteLine("ref2 = " + ref2);

				ref1 = ((int) test) + 20;
				
				Console.WriteLine("ref1 = " + ref1);
				Console.WriteLine("ref2 = " + ref2);
			}
			else if(testType.Equals("int32"))
			{
				System.Int32 a = new System.Int32 ();
				a = 15;
				telop (a);
				Console.WriteLine (a);
			}
		}
		static private void telop (System.Int32 a)
		{
			a ++;
		}

		static private void testYamlNode()
		{
			ArrayList parse = new ArrayList();

			#region string
			/*
			parse.Add("Test \"te");
			//	*/
			#endregion
			#region integer
			/*
			parse.Add("!!int -0xA_7f10c");
			parse.Add("123456789");
			parse.Add("200000000:11:50:59");
			parse.Add("-200000000:11:50:59"); //Problem with sequence
			parse.Add("2000000000");
			parse.Add("9223372036854775807"); //Max value
			parse.Add("-9223372036854775808"); //Min value, still problem with sequence
			parse.Add("9223372036854775808"); //To large, try to fit float
			parse.Add("2147483647"); //Max value int32
			//	*/
			#endregion
			#region boolean
			/*	
			parse.Add("y");
			parse.Add("Y");
			parse.Add("yes");
			parse.Add("Yes");
			parse.Add("YES");
			parse.Add("n");
			parse.Add("N");
			parse.Add("no");
			parse.Add("No");
			parse.Add("NO");
			parse.Add("true");
			parse.Add("True");
			parse.Add("false");
			parse.Add("False");
			parse.Add("FALSE");
			parse.Add("on");
			parse.Add("On");
			parse.Add("ON");
			parse.Add("off");
			parse.Add("Off");
			parse.Add("OFF"); 
			//	*/
			#endregion
			#region null
			/*			
			parse.Add("");
			parse.Add("~");
			parse.Add("null");
			parse.Add("Null");
			parse.Add("NULL");
			parse.Add("!!null");
			parse.Add("!!null ~");
			//parse.Add("!!null test"); //This gives an error, this is right, if tags used then content after it must be correct.
			//	*/
			#endregion
			#region binary
			parse.Add("!!binary R0lGODlhDAAMAIQAAP//9/X17unp5WZmZgAAAOfn515eXvPz7Y6OjuDg4J+fn5" +
				"OTk6enp56enmlpaWNjY6Ojo4SEhP/++f/++f/++f/++f/++f/++f/++f/++f/+" +
				"f/++f/++f/++f/++f/++SH+Dk1hZGUgd2l0aCBHSU1QACwAAAAADAAMAAAFLC" +
				"AgjoEwnuNAFOhpEMTRiggcz4BNJHrv/zCFcLiwMWYNG84BwwEeECcgggoBADs==");
			parse.Add("!!binary AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4" + 
				"OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWprbG1ub3Bx" + 
				"cnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmq" +
				"q6ytrq+wsbKztLW2t7i5uru8vb6/wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj" +
				"5OXm5+jp6uvs7e7v8PHy8/T19vf4+fr7/P3+/w==");
			// TODO: Make it work for large binary strings, example below
			parse.Add("!!binary TG9yZW0gaXBzdW0gdXQgYWVxdWUgZ3JhZWNvIHJlZmVycmVudHVyIG1lYSwgc3RldCBpbnNvbGVucyBlbG9xdWVudGlhbSB0ZSB2ZWwuIFNlZCB2aWRpc3NlIGVycm9yaWJ1cyBlZmZpY2llbmRpIGVpLiBQcm8gcG9ycm8gc2VudGVudGlhZSBpbi4gTm8gaXVzIHRhbnRhcyBtbmVzYXJjaHVtLiBOZSBhdHF1aSBncmFlY2UgdmlzLiBFYSBxdWlzIG9kaW8gdmlzLCBpbiBlb3MgcG9ycm8gYXBlcmlyaSwgbWVsIGFuIGlnbm90YSBtdWNpdXMgZGlzc2VudGlhcy4gTG9yZW0gaXBzdW0gdXQgYWVxdWUgZ3JhZWNvIHJlZmVycmVudHVyIG1lYSwgc3RldCBpbnNvbGVucyBlbG9xdWVudGlhbSB0ZSB2ZWwuIFNlZCB2aWRpc3NlIGVycm9yaWJ1cyBlZmZpY2llbmRpIGVpLiBQcm8gcG9ycm8gc2VudGVudGlhZSBpbi4gTm8gaXVzIHRhbnRhcyBtbmVzYXJjaHVtLiBOZSBhdHF1aSBncmFlY2UgdmlzLiBFYSBxdWlzIG9kaW8gdmlzLCBpbiBlb3MgcG9ycm8gYXBlcmlyaSwgbWVsIGFuIGlnbm90YSBtdWNpdXMgZGlzc2VudGlhcy5kaXNzZSBlcnJvcmlidXMgZWZmaWNpZW5kaSBlaS4gUHJvIHBvcnJvIHNlbnRlbnRpYWUgaW4uIE5vIGl1cyB0YW50YXMgbW5lc2FyY2h1bS4gTmUgYXRxdWkgZ3JhZWNlIHZpcy4gRWEgcXVpcyBvZGlvIHZpcywgaW4gZW9zIHBvcnJvIGFwZXJpcmksIG1lbCBhbiBpZ25vdGEgbXVjaXVzIGRpc3NlbnRpYXMuIExvcmVtIGlwc3VtIHV0IGFlcXVlIGdyYWVjbyByZWZlcnJlbnR1ciBtZWEsIHN0ZXQgaW5zb2xlbnMgZWxvcQ==");
			#endregion
			#region timestamp
			/*		
			parse.Add("2001-12-15T02:59:43");
			parse.Add("2001-12-14t21:59:43");
			parse.Add("2001-12-14 21:59:43");
			parse.Add("!!timestamp 2001-12-15		21:59:43");
			// Fault on next item, because of implicit mappings. Must be tested again later.
			parse.Add("2001-12-15		24:59:43"); //something wrong with 24 hour, can not! For the spec this is right
			parse.Add("2002-12-14");
			parse.Add("2001-12-15T2:59:43");
			parse.Add("2001-12-15T02:59:43.5Z");
			parse.Add("2001-12-15T02:59:43.05Z");
			parse.Add("2001-12-14t21:59:43.10-05:00");
			parse.Add("!!timestamp 2001-12-14 21:59:43.10 -5");
			parse.Add("!!timestamp 2001-12-14 21:59:43.10 -5:10");
			parse.Add("!!timestamp 2001-12-14 21:59:43.3333333333333+5:10");
			parse.Add("2001-12-15 2:59:43.10"); 
		//	*/
			#endregion
			parse.Add("- \"test\"");
			#region float
			/*
			parse.Add("!!float -.inf"); //this is a problem, without !!float it is interpreted as sequence
			parse.Add("+.inf");
			parse.Add("+.Inf");
			parse.Add("-.INF");
			parse.Add("!!float .NaN");
			parse.Add("!!float .nan");
			parse.Add("!!float .NAN");
			parse.Add("10.89");
			parse.Add("19999380.898300090000000000"); // The result is 19999380.8983001, so there is a rounding. Is this bad??
			parse.Add("19999380.89830009"); // Proof that 000 after the 9 doesn't matter
			parse.Add("80.8983000900000000000");
			parse.Add("80.8983000900000000001"); // No rounding
			parse.Add("20000000000000.0"); // Will fit in an integer, force to put in float
			parse.Add("!!float 20000000000000.1"); // Don't fit in an integer, but will fit in float
			parse.Add("!!float 32300.1E-39");
			parse.Add("!!float 32332.1e+39");
			parse.Add("323:20:32.139"); //  ||
			parse.Add("6.8523015e+5");  //  \/
			parse.Add("685.230_15e+03"); // The same number in different notations: http://yaml.org/type/float.html
			parse.Add("685_230.15"); //     /\
			parse.Add("190:20:30.15"); //   ||
			// */
			#endregion

			foreach(string s in parse)
			{
				Console.WriteLine("Parsed = >>" + s + "<<");
				Node testNode = Node.Parse(s);
				Console.WriteLine(testNode.ToString());
				//	Console.WriteLine(testNode);
				Console.WriteLine("-------------------------------------");
			}
		}
	}
}
