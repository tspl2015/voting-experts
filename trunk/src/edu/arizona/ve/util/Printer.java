package edu.arizona.ve.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.List;
/**
 *
 * @author  Wesley Kerr
 */
public class Printer {
   
   /** Creates a new instance of Printer */
   public Printer() {
   }
   
   public static <T> void printList(List<T> list, PrintWriter out) { 
	   if (list == null) {
		   out.write("");
		   return;
	   }
	   
	   boolean first = true;
	   for (T obj : list) { 
		   if (!first) 
			   out.write("|");
		   out.write(obj.toString());
		   first = false;
	   }
   }
   
   
   
   public static String printArray(byte[] array) {
      StringBuffer buf = new StringBuffer();
      buf.append("[ ");
      for (int i = 0; i < array.length; ++i) {
         buf.append(array[i] + " ");
      }
      buf.append("]");
      return buf.toString();
   }
   
   public static String printArray(byte[] array, int len) {
      StringBuffer buf = new StringBuffer();
      buf.append("[ ");
      for (int i = 0; i < array.length && i < len; ++i) {
         buf.append(array[i] + " ");
      }
      buf.append("]");
      return buf.toString();
   }

   public static String printArray(int[] array) {
      StringBuffer buf = new StringBuffer();
      buf.append("[ ");
      for (int i = 0; i < array.length; ++i) {
         buf.append(array[i] + " ");
      }
      buf.append("]");
      return buf.toString();
   }
   
   public static String printArray(int[] array, int len) {
      StringBuffer buf = new StringBuffer();
      buf.append("[ ");
      for (int i = 0; i < array.length && i < len; ++i) {
         buf.append(array[i] + " ");
      }
      buf.append("]");
      return buf.toString();
   }

   public static String printArray(double[] array) {
      StringBuffer buf = new StringBuffer();
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(3);
      nf.setMinimumFractionDigits(3);
      
      buf.append("[ ");
      for (int i = 0; i < array.length; ++i) {
         buf.append(pad(nf.format(array[i]), 6) + " ");
      }
      buf.append("]");
      return buf.toString();
   }

   public static String pad(String s, int length) {
      StringBuffer buf = new StringBuffer(s);
      while (buf.length() < length) {
         buf.insert(0, ' ');
      }
      return buf.toString();
   }

   public static void write(String fileName, List<String> lines) {
	   try { 
		   BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		   for (String s : lines) { 
			   out.write(s + "\n");
		   }
		   out.close();
	   } catch (Exception e) { 
		   e.printStackTrace();
	   }
   }
   
}
