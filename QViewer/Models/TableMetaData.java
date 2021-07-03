package QViewer.Models;

import java.util.Date;
import java.util.List;

public class TableMetaData {
	public int PartialNoOfRecords = -1;
    public int RecordByteSize;
    public int NoOfRecords;
    public int TotalNoOfRecords;
    public int Offset;
    public int Length;
    public String TableName;
    public String CreatorDoc;
    public String FilePath;
    public Date Modified;
    public List<QViewer.Models.Lineage> Lineage;

}
