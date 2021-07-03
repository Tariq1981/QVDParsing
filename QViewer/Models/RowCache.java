package QViewer.Models;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RowCache {
	
    private int lastrow = -1;
    public ArrayList<FieldMetaData> fields;
    public int NoOfRecords;
    private byte[] file;
    private byte[] bytemap;
    private BitSet bitmap;
    private BitSet bitindex;
    private int recordSize;
    private int offset;
    private int [] r;

    public RowCache(byte[] File, int fileOffset, int recordByteSize, ArrayList<FieldMetaData> Fields, int noOfRecords) {
    	
    	this.file = File;
    	this.offset = fileOffset;
    	this.recordSize = recordByteSize;
    	this.fields = Fields;
    	this.NoOfRecords = noOfRecords;
    	this.bytemap = new byte[recordByteSize];
    	this.bitmap = new BitSet(recordByteSize * 8);
    	this.bitindex = new BitSet(32);
    	this.r = new int[1];
    }
    
    private int[] bits2Ints(BitSet bs) {
        int[] temp = new int[bs.size() / 32];

        for (int i = 0; i < temp.length; i++)
          for (int j = 0; j < 32; j++)
            if (bs.get(i * 32 + j))
              temp[i] |= 1 << j;

        return temp;
      }


    public long GetEntryIndex(int row, int col)
    {
      if (row != this.lastrow)
      {
    	  System.arraycopy(this.file, this.offset + row * this.recordSize,this.bytemap, 0, this.recordSize);
          this.bitmap = BitSet.valueOf(this.bytemap);
          this.lastrow = row;
      }
      int index1 = this.fields.get(col).BitOffset;
      int num = this.fields.get(col).BitOffset + this.fields.get(col).BitWidth;
      int index2 = 0;
      this.bitindex.clear();
      for (; index1 < num; ++index1)
      {
    	  this.bitindex.set(index2, this.bitmap.get(index1));
        ++index2;
      }
      this.r=bits2Ints(bitindex);
      return this.r[0] != 0 ? this.r[0] + (this.fields.get(col).Bias == 0 ? 1 : -1) : (this.fields.get(col).Bias == 0 ? 1 : 0);
    }

}
