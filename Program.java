import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import QViewer.*;
import QViewer.Models.*;

class PartFile implements Runnable {
	  
	
	public String PathFileName = null;
	public int FromLine = 0;
	public int ToLine = 0;
	public int DataOffset = 0;
	public Thread thread = null;
	public Document Document=null;
	ArrayList<String> rows = new ArrayList<String>();
	
	public void run() {
		try {
			SaveFromLineToCSV();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SaveFromLineToCSV() throws Exception {

		Path p = Paths.get(this.PathFileName);
		this.Document.RowCache = new RowCache(this.Document.FileBytes,
				this.Document.DataOffset + 1 + this.Document.Table.Offset,
				this.Document.Table.RecordByteSize, this.Document.Fields,
				this.Document.Table.TotalNoOfRecords);
		this.Document.FieldIndexMap.clear();
		int num = 0;
		for (int index = 0; index < this.Document.Fields.size(); ++index) {
			this.Document.FieldIndexMap.put(num++, index);
		}

		FileWriter strw = new FileWriter(PathFileName);
		for (int row = 0; row < this.Document.Table.NoOfRecords; row++) {
			StringBuffer str = new StringBuffer();
			for (int col = 0; col < this.Document.Fields.size(); col++) {
				int col1 = this.Document.FieldIndexMap.get(col);
				int entryIndex = (int) this.Document.RowCache.GetEntryIndex(
						row, col1);
				VocabularyEntry vocabularyEntry = this.Document.Fields.get(col)
						.GetVocabularyEntry(entryIndex);
				str.append(vocabularyEntry.String);
				str.append(",");
			}

			if (row % 200000 == 0)
				System.out.println("FileName/Row/Total: " + p.getFileName()
						+ "/" + row + "/" + Document.Table.NoOfRecords);

			rows.add(str.toString());
			if ((row - FromLine) % 100 == 0) {
				for (int i = 0; i < rows.size(); i++) {
					String ss = rows.get(i);
					strw.write(ss.substring(0, ss.length() - 1));
					strw.write("\r\n");
				}
				rows.clear();
				;
				strw.flush();
			}
			str.delete(0, str.length());
			// Thread.Sleep(5000);

		}
		if (rows.size() > 0) {
			for (int i = 0; i < rows.size(); i++) {
				String ss = rows.get(i);
				strw.write(ss.substring(0, ss.length() - 1));
				strw.write("\r\n");
			}
			rows.clear();
			;
			strw.flush();
			;
		}
		strw.close();

	}
}
public class Program {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Date now=new Date();
        //String qvdfile = args[0];
        String qvdfile = "c:\\Downloads\\Aif_Counters_IMSI_IMEI_CELL12.qvd";
        
        //Console.WriteLine(Path.GetFileNameWithoutExtension(qvdfile));

        Program prog = new Program();
        //prog.CompareFiles();
        //prog.Compress(@"c:\Downloads\QVD\Aif_Counters_IMSI_IMEI_CELL00.txt", @"c:\Downloads\QVD\Aif_Counters_IMSI_IMEI_CELL00.gz");
        
        Document doc=prog.OpenFileQVDTable(qvdfile);
        int TotalNumberOFrecords = doc.Table.TotalNoOfRecords;
        int NumRecPerPart = 6000000;
        /*
        int NumRecPerPart=doc.Table.TotalNoOfRecords;
        
        if(args.length>1)
            NumRecPerPart = Integer.parseInt(args[1]);
        */
        int NumOfParts = doc.Table.TotalNoOfRecords / NumRecPerPart;
        if ((doc.Table.TotalNoOfRecords % NumRecPerPart) != 0)
            NumOfParts++;

        PartFile [] parts=new PartFile[NumOfParts];
        for (int i = 0 ; i < NumOfParts; i++)
        {
            parts[i] = new PartFile();
            parts[i].PathFileName = qvdfile + ".part" + i;
            parts[i].Document = prog.OpenFileQVDTable(qvdfile);
            parts[i].Document = prog.OpenFileQVD(parts[i].Document, qvdfile, i, NumRecPerPart);
            //parts[i].SaveFromLineToCSV();
            Thread th=new Thread(parts[i]);
            parts[i].thread=th;
            th.start();
            
            //System.Threading.ThreadPool.QueueUserWorkItem(new WaitCallback(parts[i].SaveFromLineToCSV));
            //parts[i].SaveFromLineToCSV(null);

        }
        for (int i = 0 ; i < NumOfParts; i++)
        	parts[i].thread.join();
        
        File file=new File(qvdfile);
        String name=file.getName();
        int pos = name.lastIndexOf(".");
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        
        
        System.out.println("Time:  " + (new Date().getTime()-now.getTime()));
        
        prog.CombineFileParts(parts, file.getParent() + "/" + name + ".txt");

        //prog.Compress(file.getParent() + "/" + name + ".txt", file.getParent() + "/" + name + ".zip");
        

	}
	
	public void Compress(String FullPathFileToCom,String FullPathFileCompressed) throws Exception
    {
		GZIPOutputStream compressionStream=new GZIPOutputStream(new FileOutputStream(new File(FullPathFileCompressed)));
		FileInputStream originalFileStream = new FileInputStream(FullPathFileToCom);
		int len=0;
		byte[] buffer = new byte[10*1024*1024];
        while ((len = originalFileStream.read(buffer)) > 0) {
        	compressionStream.write(buffer, 0, len);
        }
 
        originalFileStream.close();
    	
        compressionStream.finish();
        compressionStream.close();
        Files.delete(Paths.get(FullPathFileToCom));
        
    }
	
	public void CombineFileParts(PartFile [] partlist, String FinalFilePath) throws Exception{
		
        FileOutputStream strw = new FileOutputStream(FinalFilePath);

        partlist[0].Document.RowCache = new RowCache(partlist[0].Document.FileBytes, partlist[0].Document.DataOffset + 1 + partlist[0].Document.Table.Offset, partlist[0].Document.Table.RecordByteSize, partlist[0].Document.Fields, partlist[0].Document.Table.TotalNoOfRecords);
        StringBuilder str = new StringBuilder();
        for (int index = 0; index < partlist[0].Document.Fields.size(); ++index){
        	str.append(partlist[0].Document.Fields.get(index).FieldName);
            str.append(",");
        }
        String strfinal = str.substring(0, str.length()-1)+"\r\n";
        byte[] bytes = strfinal.getBytes(Charset.forName("US-ASCII"));
        strw.write(bytes, 0, str.length());

        for (int i = 0; i < partlist.length; i++)
        {
            FileInputStream strr = new FileInputStream(partlist[i].PathFileName);
            int len=0;
    		byte[] buffer = new byte[10*1024*1024];
            while ((len = strr.read(buffer)) > 0) {
            	strw.write(buffer, 0, len);
            }
            strr.close();
            Files.delete(Paths.get(partlist[i].PathFileName));
        }
        strw.close();
    }
	
	public Document OpenFileQVDTable(String fileName) throws IOException{
        //// Try to Load only part of the file ///
        Document doc = new Document();
        doc.Table.FilePath = fileName;
        FileInputStream stream=null;
        try{
        	
        	int num1 = this.LoadXMLHeader(fileName, doc);
        	File file=new File(fileName);
            stream=new FileInputStream(file);
            doc.Table.PartialNoOfRecords = 0;
            doc.PartialLoad = true;
            int offset = 0;
            long length = file.length() - (long)num1;
            length -= ((long)doc.Table.TotalNoOfRecords - (long)doc.Table.PartialNoOfRecords) * (long)doc.Table.RecordByteSize;
            doc.Table.NoOfRecords = doc.Table.PartialNoOfRecords;
            doc.FileBytes = new byte[(int)length];
            long daoffset = num1;
            stream.skip((long)daoffset);
            while (length > 0L){
            	int num3 = stream.read(doc.FileBytes, offset, (int)length);
            	if (num3 <= 0)
            		throw new FileSystemException("End of stream reached with "+length+" bytes left to read");
            	length -= (long)num3;
            	offset += num3;
            }
            
            doc.DataOffset = 0;
            this.LoadQVD(doc);
            doc.DataOffset = num1;


        }
        catch (Exception ex){
        	ex.printStackTrace();
        }
        finally
        {
        	if(stream!=null)
        		stream.close();
        }
        return doc;
    }
	private int LoadXMLHeader(String fileName, Document doc) throws Exception{
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		
		
        int num = -1;
        FileInputStream stream=new FileInputStream(new File(fileName));
        byte[] buffer = new byte[16384];
        ByteArrayOutputStream memoryStream=new ByteArrayOutputStream();
        boolean flag = false;
        int count;
        while ((count = stream.read(buffer, 0, buffer.length)) > 0 && !flag){
        	
        	for (int index = 0; index < count; ++index){
        		if ((int)buffer[index] == 0){
        			count = index;
        			flag = true;
        			break;
        		}
        	}
        	memoryStream.write(buffer, 0, count);
        }
        stream.close();
        if (!flag)
        	throw new Exception("Bad (incomplete) XML header.");
        
        String str=new String(memoryStream.toByteArray(),"UTF-8");
        InputSource is = new InputSource(new StringReader(str));
        org.w3c.dom.Document xmlDocument=builder.parse(is);
        num=memoryStream.size();
        memoryStream.close();
        /*
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new File("c:\\Downloads\\output.xml"));
        Source input = new DOMSource(xmlDocument);
        transformer.transform(input, output);
        */
        Element documentElement=xmlDocument.getDocumentElement();    
        //XmlNode xmlNode1 = documentElement.SelectSingleNode("/QvdTableHeader");
        NodeList xmlNodeList1=documentElement.getElementsByTagName("QvdFieldHeader");
        
        //XmlNodeList xmlNodeList1 = documentElement.SelectNodes("/QvdTableHeader/Fields/QvdFieldHeader");
        if(documentElement==null || xmlNodeList1==null || xmlNodeList1.getLength()==0)
        	throw new Exception("Invalid XML header");
        
        
        //if (string.IsNullOrEmpty(xmlNode1.InnerText) || xmlNodeList1.Count == 0)
        //    throw new Exception("Invalid XML header");
        XPathExpression expr=xpath.compile("/QvdTableHeader/TableName/text()");
        Node n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
        doc.Table.TableName=n1.getNodeValue();
        
        expr=xpath.compile("/QvdTableHeader/CreatorDoc/text()");
        n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
        doc.Table.CreatorDoc = n1.getNodeValue();
        
        doc.Table.Modified = new Date(new File(fileName).lastModified());
        
        expr=xpath.compile("/QvdTableHeader/RecordByteSize/text()");
        n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
        doc.Table.RecordByteSize = Helper.TryToParse(n1.getNodeValue());
        
        expr=xpath.compile("/QvdTableHeader/NoOfRecords/text()");
        n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
        doc.Table.NoOfRecords = Helper.TryToParse(n1.getNodeValue());
        
        doc.Table.TotalNoOfRecords = doc.Table.NoOfRecords;
        
        expr=xpath.compile("/QvdTableHeader/Offset/text()");
        n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
        doc.Table.Offset = Helper.TryToParse(n1.getNodeValue());
        
        expr=xpath.compile("/QvdTableHeader/Length/text()");
        n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
        doc.Table.Length = Helper.TryToParse(n1.getNodeValue());

        for (int index1 = 0; index1 < xmlNodeList1.getLength(); ++index1)
        {
            FieldMetaData fieldMetaData = new FieldMetaData(doc);
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/FieldName/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.FieldName = n1.getNodeValue();
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/BitOffset/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.BitOffset = Helper.TryToParse(n1.getNodeValue());
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/BitWidth/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.BitWidth = Helper.TryToParse(n1.getNodeValue());
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/Bias/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.Bias = Helper.TryToParse(n1.getNodeValue());
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/NoOfSymbols/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.NoOfSymbols = Helper.TryToParse(n1.getNodeValue());
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/Offset/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.Offset = Helper.TryToParse(n1.getNodeValue());
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/Length/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.Length = Helper.TryToParse(n1.getNodeValue());
            
            fieldMetaData.Visible = true;
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/Comment/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            if (n1 != null)
                fieldMetaData.Comment = n1.getNodeValue();
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/NumberFormat/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            if (n1 != null){
            	fieldMetaData.NumberFormat = new NumberFormat();
            	expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/NumberFormat/Type/text()");
                n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
                fieldMetaData.NumberFormat.Type=n1.getNodeValue();
            }
            
            expr=xpath.compile("/QvdTableHeader/Fields/QvdFieldHeader["+String.valueOf(index1+1)+"]/Tags/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            fieldMetaData.Tags = "";
            if (n1 != null)
            {
            	NodeList nodel=n1.getChildNodes();
                StringBuilder strArray = new StringBuilder();
                for (int index2 = 0; index2 < nodel.getLength(); ++index2)
                	strArray.append(nodel.item(index2).getNodeValue()).append(",");
                
                fieldMetaData.Tags = strArray.substring(0, strArray.length()-1);
            }
            doc.Fields.add(fieldMetaData);
        }
        xmlNodeList1=documentElement.getElementsByTagName("LineageInfo");
        doc.Table.Lineage = new ArrayList<Lineage>();
        int yyy=xmlNodeList1.getLength();
        for(int index3=0;index3<xmlNodeList1.getLength();index3++){
        	expr=xpath.compile("/QvdTableHeader/Lineage/LineageInfo["+String.valueOf(index3+1)+"]/Discriminator/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            String Discriminator=n1.getNodeValue();
            
            expr=xpath.compile("/QvdTableHeader/Lineage/LineageInfo["+String.valueOf(index3+1)+"]/Statement/text()");
            n1=(Node)expr.evaluate(xmlDocument,XPathConstants.NODE);
            String Statement=n1!=null?n1.getNodeValue():null;
            doc.Table.Lineage.add(new Lineage(Discriminator,Statement));
        }
            
        //xmlDocument.Save("c:\\Downloads\\FileXML.xml");*/
        return num;
    }
	public Document OpenFileQVD(Document doc,String fileName, int PartNum,int NumRecPerPart) throws IOException{
        //// Try to Load only part of the file ///
        doc.Table.FilePath = fileName;
        FileInputStream stream=null;
        try
        {
        	File file=new File(fileName);
        	stream=new FileInputStream(file);
        	int NumOfParts = doc.Table.TotalNoOfRecords / NumRecPerPart;
        	if ((doc.Table.TotalNoOfRecords % NumRecPerPart) != 0)
        		NumOfParts++;
        	int NumOfRecsInLastPart = doc.Table.TotalNoOfRecords % NumRecPerPart;
            if (PartNum > (NumOfParts - 1))
            	return null;
            if (PartNum == (NumOfParts - 1)){
            	doc.Table.PartialNoOfRecords = NumOfRecsInLastPart;
            }
            else{
            	doc.Table.PartialNoOfRecords = NumRecPerPart;
            }
            doc.PartialLoad = true;
            doc.Table.NoOfRecords = doc.Table.PartialNoOfRecords;
            int offset = 0;
            long length = doc.Table.NoOfRecords * (long)doc.Table.RecordByteSize;
            long daoffset = doc.DataOffset+doc.FileBytes.length + (long)PartNum * doc.Table.RecordByteSize*NumRecPerPart;
                offset=doc.FileBytes.length;
                byte [] temp=new byte[(int) (doc.FileBytes.length+length)];
                System.arraycopy(doc.FileBytes,0, temp, 0,doc.FileBytes.length);
                doc.FileBytes = new byte[(int) (doc.FileBytes.length+length)];
                System.arraycopy(temp,0,doc.FileBytes,0, temp.length);
                stream.skip((long)daoffset);
                while (length > 0L)
                {
                    int num3 = stream.read(doc.FileBytes, offset, (int)length);
                    if (num3 <= 0)
                    	throw new FileSystemException("End of stream reached with "+length+" bytes left to read");
                    length -= (long)num3;
                    offset += num3;
                }
            
            doc.DataOffset = 0;
            //this.LoadQVD(doc);
            return doc;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
        	if(stream!=null)
        		stream.close();
        }
        return doc;
    }
	
	 private void LoadQVD(Document doc) throws Exception{
         Date now = new Date();
         int b = doc.Fields.size() > 10 ? (int)Math.ceil((double)doc.Fields.size() / 10.0) : doc.Fields.size();
         for (int index = 0; index < doc.Fields.size(); ++index)
         {
             int result=(index + 1)%b;
             doc.Fields.get(index).BuildVocabulary(doc.FileBytes, doc.DataOffset);
         }

         //this.Document = doc;
         //this.SaveToCSV();
         
         double num = (new Date().getTime()-now.getTime()) / 1000.0;
         doc.StatusInfo = doc.Table.NoOfRecords + " rows loaded in " + num + " seconds";

     }
}
