package QViewer.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {
	
  public TableMetaData Table;
  public ArrayList<FieldMetaData> Fields;
  public boolean PartialLoad;
  public boolean AnalysisDone;
  public RowCache RowCache;
  public SearchedItem SearchedItem;
  public byte[] FileBytes;
  public int DataOffset;
  public Map<Integer, Integer> FieldIndexMap;
  public String StatusInfo;

  public Document()
  {
    this.Table = new TableMetaData();
    this.Fields = new ArrayList<FieldMetaData>();
    this.FieldIndexMap = new HashMap<Integer, Integer>();
  }
}