package QViewer.Models;

import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;


public class FieldMetaData {
	private Document _doc;
    public String FieldName;
    public int BitOffset;
    public int BitWidth;
    public int Bias;
    public int NoOfSymbols;
    public int Offset;
    public int Length;
    public NumberFormat NumberFormat;
    public boolean Visible;
    public String Comment;
    public String Tags;
    public int textCount;
    public int numCount;
    public int[] VocabularyPointers;
    public int[] VocabularyCounts;   
    
    private class StringPos
    {
    	String str;
    	int NextPosition;
    }
    public FieldMetaData(Document doc) {
        this._doc = doc;
    }
    
    private StringPos getStr(int fieldPos){
    	
        int length = 0;
        while ((int) this._doc.FileBytes[fieldPos + length] != 0)
        	++length;
        
        byte[] bytes = new byte[length];
        System.arraycopy(this._doc.FileBytes, fieldPos, bytes, 0, length);
        StringPos strpos=new StringPos();
        strpos.str=new String(bytes,StandardCharsets.UTF_8);
        strpos.NextPosition=fieldPos+length+1;
        return strpos;
      }

      public VocabularyEntry GetVocabularyEntry(int index) throws Exception
      {
        if (index == 0){
        	return new VocabularyEntry();
           
        }
          
        byte[] numArray1 = new byte[4];
        byte[] numArray2 = new byte[8];
        double num1 = 0.0;
        byte[] fileBytes = this._doc.FileBytes;
        int index1 = this.VocabularyPointers[index - 1];
        FieldType fieldType;
        switch(fileBytes[index1])
        {
        case 1:
        	fieldType=FieldType.Int;
        	break;
        case 2:
        	fieldType=FieldType.Double;
        	break;
        case 4:
        	fieldType=FieldType.Text;
        	break;
        case 5:
        	fieldType=FieldType.DualInt;
        	break;
        case 6:
        	fieldType=FieldType.DualDouble;
        	break;
        default:
        	fieldType=FieldType.Unknown;
        }
        
        String str="";
        int num2=0;
        
        switch (fieldType)
        {
          case Int:
        	  
        	  System.arraycopy(fileBytes, index1 + 1,numArray1, 0, 4);
              ByteBuffer buff=ByteBuffer.wrap(numArray1);
              buff.order(ByteOrder.LITTLE_ENDIAN);
              num1=(double)buff.getInt();
              str = String.valueOf(num1);
              num2 = index1 + 5;
              break;
          case Double:
        	  System.arraycopy(fileBytes, index1 + 1,numArray2, 0, 8);
              ByteBuffer buff1=ByteBuffer.wrap(numArray2);
              buff1.order(ByteOrder.LITTLE_ENDIAN);
              num1 = buff1.getDouble();
              str = String.valueOf(num1);
              num2 = index1 + 9;
              break;
          case Text:
            int fieldPos1 = index1 + 1;
            StringPos strpos = this.getStr(fieldPos1);
            str=strpos.str;
            fieldPos1=strpos.NextPosition;
            break;
          case DualInt:
        	  System.arraycopy(fileBytes, index1 + 1, numArray1, 0, 4);
        	  ByteBuffer buf3=ByteBuffer.wrap(numArray1);
        	  buf3.order(ByteOrder.LITTLE_ENDIAN);
        	  num1=(double)buf3.getInt();
        	  int fieldPos2 = index1 + 5;
        	  strpos = this.getStr(fieldPos2);
        	  fieldPos2=strpos.NextPosition;
        	  str=strpos.str;
            break;
          case DualDouble:
        	  System.arraycopy(fileBytes, index1 + 1, numArray2, 0, 8);
        	  ByteBuffer buf4=ByteBuffer.wrap(numArray2);
        	  buf4.order(ByteOrder.LITTLE_ENDIAN);
        	  num1=buf4.getDouble();
        	  int fieldPos3 = index1 + 9;
        	  strpos=this.getStr(fieldPos3);
        	  fieldPos3=strpos.NextPosition;
        	  str=strpos.str;
            break;
          default:
        	  throw new Exception("Undefined field type");
        }
        VocabularyEntry voc=new VocabularyEntry();
        voc.Type=fieldType;
        voc.String=str;
        voc.Number=num1;
        return voc;
      }

      private void skipString(byte[] file, int fieldEnd, StringPos fieldPos){
        ++fieldPos.NextPosition;
        while (fieldPos.NextPosition < fieldEnd && (int) file[fieldPos.NextPosition] != 0)
          ++fieldPos.NextPosition;
        ++fieldPos.NextPosition;
      }

      public void BuildVocabulary(byte[] file, int dataOffset) throws Exception
      {
        this.VocabularyPointers = new int[this.NoOfSymbols];
        this.VocabularyCounts = new int[this.NoOfSymbols + 1];
        int fieldPos = this.Offset + 1 + dataOffset;
        int fieldEnd = fieldPos + this.Length - 1;
        int index = 0;
        StringPos strpos=new StringPos();
        strpos.str="";
        strpos.NextPosition=fieldPos;
        //String temp="";
        //FileWriter stream=new FileWriter(new File("c:\\Downloads\\IndJava.txt"));
        while (fieldPos <= fieldEnd && (int) file[fieldPos] != 0)
        {
          this.VocabularyPointers[index] = fieldPos;
          ++index;
          //int fff=file[fieldPos];
          switch (file[fieldPos])
          {
            case (byte) 1:
              fieldPos += 5;
            //temp = String.valueOf(fieldPos)+"\r\n";
            //stream.write(temp);
              continue;
            case (byte) 2:
              fieldPos += 9;
            //temp = String.valueOf(fieldPos)+"\r\n";
            //stream.write(temp);
              continue;
            case (byte) 4:
            	strpos.NextPosition=fieldPos;
            	this.skipString(file, fieldEnd, strpos);
                fieldPos=strpos.NextPosition;
                //temp = String.valueOf(fieldPos)+"\r\n";
                //stream.write(temp);
                continue;
            case (byte) 5:
            	fieldPos += 5;
                strpos.NextPosition=fieldPos;
                this.skipString(file, fieldEnd, strpos);
                fieldPos=strpos.NextPosition;
                //temp = String.valueOf(fieldPos)+"\r\n";
                //stream.write(temp);
                continue;
            case (byte) 6:
            	fieldPos += 9;
                strpos.NextPosition=fieldPos;
                this.skipString(file, fieldEnd, strpos);
                fieldPos=strpos.NextPosition;
                //temp = String.valueOf(fieldPos)+"\r\n";
                //stream.write(temp);
                continue;
            default:
            	//System.out.println(temp);
            	/*
            	temp = String.valueOf(fieldPos)+"\r\n";
                stream.write(temp);
                stream.close();*/
            	throw new Exception("Undefined field type");
          }
          
        }
      }
}
