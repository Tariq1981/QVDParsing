package QViewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Helper {
	public static int TryToParse(String str)
    {
      int result;
      result =Integer.parseInt(str);
      return result;
    }
	
	public static byte[] Combine(byte [][] arrays) throws IOException
    {
		ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
      for(int i=0;i<arrays.length;i++){
    	  byteOut.write(arrays[i]);
      }
      byte [] numArray=byteOut.toByteArray();
      byteOut.close();
      return numArray;
    }

}
