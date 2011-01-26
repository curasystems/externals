using System;
using System.Diagnostics;

namespace GCT
{
	/// <summary>
	/// Debug tool, used to output information and errors to a file/screen
	/// <p><b>Author:</b> Chris Koiak</p>
	/// <p><b>Date:</b>  12/03/2003</p>
	/// </summary>
	public class Trace
	{
		/// <summary>Determines if tracing is enabled</summary>
		public static bool trace = false;

		/// <summary>
		/// Information Trace
		/// </summary>
		/// <param name="module">Module responsible for the information</param>
		/// <param name="message">The message to be displayed</param>
		public static void info(String module, String message) 
		{
			writeToDebug("[INFO]",module,message);
		}

		/// <summary>
		/// Warning Trace
		/// </summary>
		/// <param name="module">Module responsible for the warning</param>
		/// <param name="message">The message to be displayed</param>
		public static void warn(String module, String message) 
		{
			writeToDebug("[WARN]",module,message);
		}

		/// <summary>
		/// Error Trace
		/// </summary>
		/// <param name="module">Module responsible for the error</param>
		/// <param name="message">The message to be displayed</param>
		public static void error(String module, String message) 
		{
			writeToDebug("[ERROR]",module,message);
			
		}

		/// <summary>
		/// Writes the trace to the Debug
		/// </summary>
		/// <param name="type">Type of trace</param>
		/// <param name="module">Module responsible for the error</param>
		/// <param name="message">The message to be displayed</param>
		private static void writeToDebug(String type, String module, String message)
		{
			int space1 = 10;
			int space2 = 45;
			//Debug.WriteLine(System.DateTime.Now.ToString("HH:mm:ss:ff") + "      " +  type.PadRight(space1,' ') + module.PadRight(space2,' ') + message);
			System.Diagnostics.Trace.WriteLine(System.DateTime.Now.ToString("HH:mm:ss:ff") + "      " +  type.PadRight(space1,' ') + module.PadRight(space2,' ') + message);
		}
	}
}
