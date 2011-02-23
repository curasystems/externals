//-------------------------------------------------------
//
//  These sources are provided by the company SMACCHIA.COM SARL
//  Download the trial edition of our flagship product: http://www.NDepend.com
//  NDepend is a tool for .NET developers.
//  It is a static analyzer that simplifies managing a complex .NET code base
//
//-------------------------------------------------------
namespace NDepend.Test {
   static class TestHelper {
      internal static void SetUpTests() {
         System.Diagnostics.DefaultTraceListener listener = (System.Diagnostics.DefaultTraceListener)System.Diagnostics.Trace.Listeners[0];
         listener.AssertUiEnabled = true;
      }
   }
}
