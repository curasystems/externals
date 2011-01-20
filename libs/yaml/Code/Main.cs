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

namespace Yaml
{
	/// <summary>
	///   Test class
	/// </summary>
	class Test
	{
		/// <summary>
		///   The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main (string[] args)
		{

//			Node node2 = Node.FromFile ("test.yaml");
//			Console.WriteLine (node2);
//			node2.ToFile ("out.yaml");
/*
			Console.WriteLine
			(
				 "ToString: \n" +
				 node2.Write ()
			);

			Console.WriteLine
			(
				 "Info: \n" +
				 node2.Info () .Write ()
			);

			node2.ToFile ("out.yaml");
			node2.Info (). ToFile ("out.yaml");
// */

			Sequence sequence = new Sequence ( new Node [] 
			{
				new String ("item 1"),
				new String ("item 2"),
				new String ("item 3"),
				new Mapping ( new MappingNode []
				{
					new MappingNode (new String ("key 2"), new String ("value 1")),
					new MappingNode (new String ("key 2"), new String ("value 2"))
				} ),
				new String ("item 5")
			} );
			Console.WriteLine (sequence);
		

			foreach (Node s in sequence.Nodes)
			{
				if (s.Type == NodeType.String)
					Console.Write ("Found a string: " + ((String) s) . Content);
			}
// */
		}
	}
}
